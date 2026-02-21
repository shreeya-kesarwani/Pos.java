package com.pos.dto;

import com.pos.api.OrderApi;
import com.pos.api.ProductApi;
import com.pos.client.InvoiceClient;
import com.pos.exception.ApiException;
import com.pos.flow.OrderFlow;
import com.pos.model.constants.OrderStatus;
import com.pos.model.data.*;
import com.pos.model.form.InvoiceForm;
import com.pos.model.form.OrderForm;
import com.pos.model.form.OrderItemForm;
import com.pos.model.form.OrderSearchForm;
import com.pos.pojo.Order;
import com.pos.pojo.OrderItem;
import com.pos.pojo.Product;
import com.pos.utils.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Component;

import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static com.pos.model.constants.ErrorMessages.*;

@Component
public class OrderDto extends AbstractDto {

    private static final String INVOICE_DIR = "/tmp/invoices";

    @Autowired private OrderFlow orderFlow;
    @Autowired private ProductApi productApi;
    @Autowired private OrderApi orderApi;
    @Autowired private InvoiceClient invoiceClient;

    public Integer create(OrderForm form) throws ApiException {
        normalize(form);
        validateForm(form);

        List<OrderItem> items = toOrderItemPojos(form);
        return orderFlow.createOrder(items);
    }

    public PaginatedResponse<OrderData> search(OrderSearchForm form) throws ApiException {
        normalize(form);
        validateForm(form);

        if (form.getStart() != null && form.getEnd() != null && form.getStart().isAfter(form.getEnd())) {
            throw new ApiException(INVALID_DATE_RANGE.value());
        }

        OrderStatus orderStatus = validateStatus(form.getStatus());

        List<Order> orders = orderApi.search(form.getId(), form.getStart(), form.getEnd(), orderStatus, form.getPageNumber(), form.getPageSize());
        long totalCount = orderApi.getCount(form.getId(), form.getStart(), form.getEnd(), orderStatus);

        if (orders == null || orders.isEmpty()) {
            return PaginatedResponse.of(List.of(), totalCount, form.getPageNumber());
        }

        List<Integer> orderIds = extractOrderIds(orders);
        Map<Integer, List<OrderItem>> itemsByOrderId = getItemsGroupedByOrderId(orderIds);
        List<OrderData> data = OrderApi.toOrderDataList(orders, itemsByOrderId);

        return PaginatedResponse.of(data, totalCount, form.getPageNumber());
    }

    public List<OrderItemData> getItems(Integer orderId) throws ApiException {
        if (orderId == null) throw new ApiException(ORDER_ID_REQUIRED.value());

        List<OrderItem> items = orderApi.getItemsByOrderId(orderId);
        if (items == null || items.isEmpty()) return List.of();

        Map<Integer, Product> productById = getProductMapFromItems(items);
        return OrderConversion.toOrderItemDataList(items, productById);
    }

    public InvoiceData generateInvoice(Integer orderId) throws ApiException {
        if (orderId == null) {
            throw new ApiException(ORDER_ID_REQUIRED.value());
        }

        orderApi.getCheck(orderId);

        List<OrderItem> items = orderApi.getItemsByOrderId(orderId);
        if (items == null || items.isEmpty()) {
            throw new ApiException(CANNOT_INVOICE_EMPTY_ORDER.value() + ": orderId=" + orderId);
        }

        Map<Integer, Product> productById = getProductMapFromItems(items);

        InvoiceForm form = OrderConversion.toInvoiceForm(orderId, items, productById);
        normalize(form);
        validateForm(form);

        InvoiceData data = invoiceClient.generate(form);
        byte[] pdfBytes = InvoiceConversion.decodePdfBytes(data);
        InvoiceStorageUtil.storeAndAttach(orderApi, INVOICE_DIR, orderId, pdfBytes);

        return data;
    }

    public ResponseEntity<byte[]> downloadInvoice(Integer orderId) throws ApiException {
        if (orderId == null) throw new ApiException(ORDER_ID_REQUIRED.value());

        Order order = orderApi.getCheck(orderId);

        byte[] pdfBytes = InvoicePathUtil.tryReadInvoiceBytes(order.getInvoicePath(), orderId);
        if (pdfBytes == null) {
            InvoiceData data = generateInvoice(orderId);
            pdfBytes = InvoiceConversion.decodePdfBytes(data);
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=" + InvoicePathUtil.invoiceFileName(orderId))
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfBytes);
    }

    private Map<Integer, Product> getProductMapFromItems(List<OrderItem> items) throws ApiException {
        if (items == null || items.isEmpty()) return Map.of();

        Set<Integer> productIds = items.stream()
                .map(OrderItem::getProductId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        if (productIds.isEmpty()) return Map.of();

        List<Product> products = productApi.getByIds(productIds.stream().toList());
        if (products == null || products.isEmpty()) return Map.of();

        return CollectionIndexUtil.indexBy(products, Product::getId);
    }

    private OrderStatus validateStatus(String status) throws ApiException {
        if (status == null || status.trim().isEmpty()) return null;

        return EnumParseUtil.parseEnum(OrderStatus.class, status)
                .orElseThrow(() -> new ApiException(INVALID_STATUS.value() + ": " + status));
    }

    private List<OrderItem> toOrderItemPojos(OrderForm form) throws ApiException {
        List<OrderItemForm> itemForms = (form.getItems() == null) ? List.of() : form.getItems();
        if (itemForms.isEmpty()) return List.of();

        for (OrderItemForm itemForm : itemForms) {
            normalize(itemForm);
            validateForm(itemForm);
        }

        List<String> barcodes = itemForms.stream().map(OrderItemForm::getBarcode).filter(Objects::nonNull).distinct().toList();
        Map<String, Product> productByBarcode = getProductMapByBarcode(barcodes);

        List<OrderItem> items = new ArrayList<>(itemForms.size());
        for (OrderItemForm itemForm : itemForms) {
            String barcode = itemForm.getBarcode();
            Product product = productByBarcode.get(barcode);

            if (product == null) {
                throw new ApiException(PRODUCT_NOT_FOUND.value() + ": barcode=" + barcode);
            }
            items.add(OrderConversion.toOrderItemPojo(itemForm, product.getId()));
        }
        return items;
    }

    private List<Integer> extractOrderIds(List<Order> orders) {
        if (orders == null || orders.isEmpty()) return List.of();
        return orders.stream()
                .map(Order::getId)
                .filter(Objects::nonNull)
                .toList();
    }

    private Map<Integer, List<OrderItem>> getItemsGroupedByOrderId(List<Integer> orderIds) {
        if (orderIds == null || orderIds.isEmpty()) return Map.of();

        List<OrderItem> allItems = orderApi.getItemsByOrderIds(orderIds);
        if (allItems == null || allItems.isEmpty()) return Map.of();

        return allItems.stream()
                .filter(oi -> oi.getOrderId() != null)
                .collect(Collectors.groupingBy(OrderItem::getOrderId));
    }

    private Map<String, Product> getProductMapByBarcode(List<String> barcodes) throws ApiException {
        if (barcodes == null || barcodes.isEmpty()) return Map.of();
        List<Product> products = productApi.getCheckByBarcodes(barcodes);
        if (products == null || products.isEmpty()) return Map.of();

        return products.stream()
                .filter(p -> p.getBarcode() != null)
                .collect(Collectors.toMap(Product::getBarcode, p -> p, (a, b) -> a));
    }
}