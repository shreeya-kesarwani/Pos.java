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

    public PaginatedResponse<OrderData> search(Integer id, ZonedDateTime start, ZonedDateTime end, String status, Integer pageNumber, Integer pageSize) throws ApiException {

        OrderStatus orderStatus = validateStatus(status);

        List<Order> orders = orderApi.search(id, start, end, orderStatus, pageNumber, pageSize);
        long totalCount = orderApi.getCount(id, start, end, orderStatus);

        List<OrderData> data = new ArrayList<>();
        for (Order order : orders) {
            List<OrderItem> items = orderFlow.getOrderItems(order.getId());
            data.add(OrderConversion.toOrderDataWithTotal(order, items));
        }

        return PaginatedResponse.of(data, totalCount, pageNumber);
    }

    public List<OrderItemData> getItems(Integer orderId) throws ApiException {
        List<OrderItem> items = orderFlow.getOrderItems(orderId);
        if (items.isEmpty()) return List.of();

        Map<Integer, Product> productById = getProductMapFromItems(items);
        return OrderConversion.toOrderItemDataList(items, productById);
    }

    public InvoiceData generateInvoice(Integer orderId) throws ApiException {
        List<OrderItem> items = orderFlow.getOrderItems(orderId);
        if (items.isEmpty()) {
            throw new ApiException(CANNOT_INVOICE_EMPTY_ORDER.value() + ": orderId=" + orderId);
        }

        Map<Integer, Product> productById = getProductMapFromItems(items);
        InvoiceForm form = InvoiceConversion.toInvoiceForm(orderId, items, productById);
        validateForm(form);

        InvoiceData data = invoiceClient.generate(form);
        byte[] pdfBytes = InvoiceConversion.decodePdfBytes(data);
        InvoiceStorageUtil.storeAndAttach(orderApi, INVOICE_DIR, orderId, pdfBytes);

        return data;
    }

    public ResponseEntity<byte[]> downloadInvoice(Integer orderId) throws ApiException {
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
        Set<Integer> productIds = items.stream()
                .map(OrderItem::getProductId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        if (productIds.isEmpty()) return Map.of();
        List<Product> products = productApi.getByIds(productIds.stream().toList());
        return CollectionIndexUtil.indexBy(products, Product::getId);
    }

    private OrderStatus validateStatus(String status) throws ApiException {
        if (status == null || status.trim().isEmpty()) return null;

        return EnumParseUtil.parseEnum(OrderStatus.class, status)
                .orElseThrow(() -> new ApiException(INVALID_STATUS.value() + ": " + status));
    }

    private List<OrderItem> toOrderItemPojos(OrderForm form) throws ApiException {
        List<String> barcodes = new ArrayList<>();

        for (OrderItemForm itemForm : form.getItems()) {
            normalize(itemForm);
            String barcode = normalize(itemForm.getBarcode());
            if (barcode == null) {
                throw new ApiException("Barcode cannot be empty");
            }
            barcodes.add(barcode);
        }

        List<Product> products = productApi.getCheckByBarcodes(barcodes);
        Map<String, Product> productByBarcode = products.stream()
                .collect(Collectors.toMap(Product::getBarcode, p -> p, (a, b) -> a));

        List<OrderItem> items = new ArrayList<>();
        for (OrderItemForm itemForm : form.getItems()) {
            String barcode = normalize(itemForm.getBarcode());

            Product product = productByBarcode.get(barcode);
            if (product == null) {
                throw new ApiException(PRODUCT_NOT_FOUND.value() + ": barcode=" + barcode);
            }
            items.add(OrderConversion.toOrderItemPojo(itemForm, product.getId()));
        }
        return items;
    }
}