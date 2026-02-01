package com.pos.flow;

import com.pos.api.InventoryApi;
import com.pos.api.OrderApi;
import com.pos.api.OrderItemApi;
import com.pos.api.ProductApi;
import com.pos.exception.ApiException;
import com.pos.model.data.OrderItemData;
import com.pos.model.constants.OrderStatus;
import com.pos.model.form.InvoiceForm;
import com.pos.model.form.InvoiceItemForm;
import com.pos.model.form.OrderForm;
import com.pos.model.form.OrderItemForm;
import com.pos.pojo.Order;
import com.pos.pojo.OrderItem;
import com.pos.pojo.Product;
import com.pos.utils.OrderConversion;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@Transactional(rollbackFor = ApiException.class)
public class OrderFlow {

    @Autowired private OrderApi orderApi;
    @Autowired private OrderItemApi orderItemApi;
    @Autowired private InventoryApi inventoryApi;
    @Autowired private ProductApi productApi;

    public Integer createOrder(OrderForm form) throws ApiException {

        // 1) Create Order Pojo in Flow and persist via API
        Order order = new Order();
        order.setStatus(OrderStatus.CREATED);
        orderApi.add(order);

        // 2) Collect barcodes once, normalize, and validate
        List<String> barcodes = extractAndValidateBarcodes(form);

        // 3) Single DB call to fetch ALL products for barcodes
        List<Product> products = productApi.getCheckByBarcodes(barcodes);

        // 4) Map barcode -> product
        Map<String, Product> barcodeToProduct = products.stream()
                .collect(Collectors.toMap(
                        p -> p.getBarcode().trim(),
                        p -> p
                ));

        // 5) Process items using the map (no extra DB calls)
        for (OrderItemForm itemForm : form.getItems()) {
            String barcode = itemForm.getBarcode().trim();
            Product product = barcodeToProduct.get(barcode);

            if (product == null) {
                // Should not happen if getCheckByBarcodes validates properly, but safe.
                throw new ApiException("Product not found for barcode: " + barcode);
            }

            validateSellingPrice(itemForm.getSellingPrice());
            productApi.validateSellingPrice(product.getId(), itemForm.getSellingPrice());

            inventoryApi.allocate(product.getId(), itemForm.getQuantity());

            orderItemApi.add(
                    order.getId(),
                    product.getId(),
                    itemForm.getQuantity(),
                    itemForm.getSellingPrice()
            );
        }

        return order.getId();
    }

    @Transactional(readOnly = true)
    public List<Order> search(Integer id, ZonedDateTime start, ZonedDateTime end,
                              String status, int page, int size) throws ApiException {
        OrderStatus orderStatus = parseStatus(status);
        return orderApi.search(id, start, end, orderStatus, page, size);
    }

    @Transactional(readOnly = true)
    public Long getCount(Integer id, ZonedDateTime start, ZonedDateTime end, String status)
            throws ApiException {
        OrderStatus orderStatus = parseStatus(status);
        return orderApi.getCount(id, start, end, orderStatus);
    }

    @Transactional(readOnly = true)
    public List<OrderItemData> getOrderItemData(Integer orderId) throws ApiException {
        List<OrderItem> items = orderItemApi.getByOrderId(orderId);
        List<OrderItemData> data = new ArrayList<>();
        for (OrderItem item : items) {
            Product product = productApi.getCheck(item.getProductId());
            data.add(OrderConversion.toOrderItemData(item, product.getBarcode(), product.getName()));
        }
        return data;
    }

    @Transactional(readOnly = true)
    public Double calculateTotalAmount(Integer orderId) {
        List<OrderItem> items = orderItemApi.getByOrderId(orderId);
        return OrderConversion.calculateTotalAmount(items);
    }

    @Transactional(readOnly = true)
    public InvoiceForm buildInvoiceForm(Integer orderId) throws ApiException {
        Order order = orderApi.getCheck(orderId);
        if (order.getStatus() == OrderStatus.INVOICED) {
            throw new ApiException("Order already invoiced");
        }

        InvoiceForm form = new InvoiceForm();
        form.setOrderId(orderId);
        form.setItems(buildInvoiceItems(orderId));
        return form;
    }

    @Transactional(readOnly = true)
    private List<InvoiceItemForm> buildInvoiceItems(Integer orderId) throws ApiException {
        List<OrderItem> orderItems = orderItemApi.getByOrderId(orderId);
        List<InvoiceItemForm> invoiceItems = new ArrayList<>();
        for (OrderItem item : orderItems) {
            Product product = productApi.getCheck(item.getProductId());
            InvoiceItemForm f = new InvoiceItemForm();
            f.setName(product.getName());
            f.setQuantity(item.getQuantity());
            f.setSellingPrice(item.getSellingPrice());
            invoiceItems.add(f);
        }
        return invoiceItems;
    }

    public void attachInvoice(Integer orderId, String path) throws ApiException {
        orderApi.attachInvoice(orderId, path);
    }

    @Transactional(readOnly = true)
    public Order getOrder(Integer orderId) throws ApiException {
        return orderApi.getCheck(orderId);
    }

    private OrderStatus parseStatus(String status) throws ApiException {
        if (status == null || status.trim().isEmpty()) return null;

        try {
            return OrderStatus.valueOf(status.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ApiException("Invalid status: " + status + ". Allowed: CREATED, INVOICED");
        }
    }

    // -------------------- helpers --------------------

    private static List<String> extractAndValidateBarcodes(OrderForm form) throws ApiException {
        if (form == null || form.getItems() == null || form.getItems().isEmpty()) {
            throw new ApiException("Order must contain at least one item");
        }

        List<String> barcodes = new ArrayList<>();
        for (OrderItemForm itemForm : form.getItems()) {
            if (itemForm == null || itemForm.getBarcode() == null || itemForm.getBarcode().trim().isEmpty()) {
                throw new ApiException("Barcode cannot be empty");
            }
            barcodes.add(itemForm.getBarcode().trim());
        }
        return barcodes;
    }

    // reviewer comment: "static private method for validateSellingPrice in this class only"
    private static void validateSellingPrice(Double sellingPrice) throws ApiException {
        if (sellingPrice == null) {
            throw new ApiException("Selling price cannot be null");
        }
        if (sellingPrice < 0) {
            throw new ApiException("Selling price cannot be negative");
        }
    }
}