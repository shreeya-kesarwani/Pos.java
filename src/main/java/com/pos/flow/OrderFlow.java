package com.pos.flow;

import com.pos.api.*;
import com.pos.exception.ApiException;
import com.pos.model.data.OrderStatus;
import com.pos.model.form.InvoiceForm;
import com.pos.model.form.InvoiceItemForm;
import com.pos.pojo.Inventory;
import com.pos.pojo.Order;
import com.pos.pojo.OrderItem;
import com.pos.pojo.Product;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
@Transactional(rollbackFor = Exception.class)
public class OrderFlow {

    @Autowired
    private OrderApi orderApi;

    @Autowired
    private OrderItemApi orderItemApi;

    @Autowired
    private InventoryApi inventoryApi;

    @Autowired
    private InvoiceApi invoiceApi;

    @Autowired
    private ProductApi productApi;

    // -------------------------------------------------
    // CREATE ORDER
    // -------------------------------------------------
    public Integer createOrder(List<OrderItem> items) throws ApiException {

        // 1️⃣ Create order
        Order order = orderApi.create();

        // 2️⃣ Process each item
        for (OrderItem item : items) {

            item.setOrderId(order.getId());

            Inventory inventory =
                    inventoryApi.getByProductId(item.getProductId());

            if (inventory == null || inventory.getQuantity() < item.getQuantity()) {
                throw new ApiException(
                        "Insufficient inventory for productId: " + item.getProductId()
                );
            }

            inventory.setQuantity(
                    inventory.getQuantity() - item.getQuantity()
            );
            inventoryApi.update(inventory.getId(), inventory);

            orderItemApi.add(item);
        }

        return order.getId();
    }

    // -------------------------------------------------
    // SEARCH (PAGINATED)
    // -------------------------------------------------
    @Transactional(readOnly = true)
    public List<Order> search(
            Integer id,
            ZonedDateTime start,
            ZonedDateTime end,
            String status,
            int page,
            int size
    ) throws ApiException {

        OrderStatus orderStatus = null;
        if (status != null) {
            orderStatus = OrderStatus.valueOf(status.toUpperCase());
        }

        return orderApi.search(id, start, end, orderStatus, page, size);
    }

    @Transactional(readOnly = true)
    public Long getCount(
            Integer id,
            ZonedDateTime start,
            ZonedDateTime end,
            String status
    ) throws ApiException {

        OrderStatus orderStatus = null;
        if (status != null) {
            orderStatus = OrderStatus.valueOf(status.toUpperCase());
        }

        return orderApi.getCount(id, start, end, orderStatus);
    }

    // -------------------------------------------------
    // ORDER ITEMS
    // -------------------------------------------------
    @Transactional(readOnly = true)
    public List<OrderItem> getItems(Integer orderId) {
        return orderItemApi.getByOrderId(orderId);
    }

    // -------------------------------------------------
    // INVOICE
    // -------------------------------------------------
    public void markOrderInvoiced(Integer orderId) throws ApiException {

        // 1️⃣ Update order state
        orderApi.updateStatus(orderId, OrderStatus.INVOICED);

        // 2️⃣ Build invoice items
        List<InvoiceItemForm> invoiceItems = buildInvoiceItems(orderId);

        InvoiceForm invoiceForm = new InvoiceForm();
        invoiceForm.setOrderId(orderId);
        invoiceForm.setItems(invoiceItems);

        // 3️⃣ Generate & persist invoice
        invoiceApi.generateAndSaveInvoice(orderId, invoiceForm);
    }

    private List<InvoiceItemForm> buildInvoiceItems(Integer orderId)
            throws ApiException {

        List<OrderItem> orderItems =
                orderItemApi.getByOrderId(orderId);

        List<InvoiceItemForm> invoiceItems = new ArrayList<>();

        for (OrderItem item : orderItems) {

            Product product =
                    productApi.getCheck(item.getProductId());

            InvoiceItemForm f = new InvoiceItemForm();
            f.setName(product.getName());
            f.setQuantity(item.getQuantity());
            f.setSellingPrice(item.getSellingPrice());

            invoiceItems.add(f);
        }

        return invoiceItems;
    }
}
