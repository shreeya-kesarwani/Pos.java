package com.pos.flow;

import com.pos.api.InventoryApi;
import com.pos.api.OrderApi;
import com.pos.api.OrderItemApi;
import com.pos.api.ProductApi;
import com.pos.exception.ApiException;
import com.pos.model.data.OrderItemData;
import com.pos.model.data.OrderStatus;
import com.pos.model.form.InvoiceForm;
import com.pos.model.form.InvoiceItemForm;
import com.pos.model.form.OrderItemForm;
import com.pos.pojo.Inventory;
import com.pos.pojo.Order;
import com.pos.model.form.OrderForm;
import com.pos.pojo.OrderItem;
import com.pos.pojo.Product;
import com.pos.utils.OrderConversion;
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
    private ProductApi productApi;

    public Integer createOrder(OrderForm form) throws ApiException {

        Order order = orderApi.create();

        for (OrderItemForm itemForm : form.getItems()) {

            String barcode = itemForm.getBarcode();
            if (barcode == null || barcode.trim().isEmpty()) {
                throw new ApiException("Barcode cannot be empty");
            }

            Integer productId = productApi.getIdByBarcode(barcode.trim());

            Product product = productApi.getCheck(productId);

            if (itemForm.getSellingPrice() > product.getMrp()) {
                throw new ApiException(
                        "Selling price cannot be greater than MRP for product: " + product.getName()
                );
            }
            Inventory inventory = inventoryApi.getByProductId(product.getId());
            if (inventory == null || inventory.getQuantity() < itemForm.getQuantity()) {
                throw new ApiException("Insufficient inventory for productId: " + product.getId());
            }

            OrderItem item = new OrderItem();
            item.setOrderId(order.getId());
            item.setProductId(productId);
            item.setQuantity(itemForm.getQuantity());
            item.setSellingPrice(itemForm.getSellingPrice());

            inventory.setQuantity(inventory.getQuantity() - itemForm.getQuantity());
            inventoryApi.update(inventory.getId(), inventory);
            orderItemApi.add(item);
        }
        return order.getId();
    }

    @Transactional(readOnly = true)
    public List<Order> search(
            Integer id,
            ZonedDateTime start,
            ZonedDateTime end,
            String status,
            int page,
            int size
    ) throws ApiException {
        OrderStatus orderStatus = (status == null) ? null : OrderStatus.valueOf(status);
        return orderApi.search(id, start, end, orderStatus, page, size);
    }

    @Transactional(readOnly = true)
    public Long getCount(
            Integer id,
            ZonedDateTime start,
            ZonedDateTime end,
            String status
    ) {

        OrderStatus orderStatus = (status == null) ? null : OrderStatus.valueOf(status);
        return orderApi.getCount(id, start, end, orderStatus);
    }

    @Transactional(readOnly = true)
    public List<OrderItemData> getOrderItemData(Integer orderId)
            throws ApiException {
        List<OrderItem> items = orderItemApi.getByOrderId(orderId);
        List<OrderItemData> data = new ArrayList<>();
        for (OrderItem item : items) {
            Product product = productApi.getCheck(item.getProductId());
            data.add(
                    OrderConversion.toOrderItemData(
                            item,
                            product.getBarcode(),
                            product.getName()
                    )
            );
        }
        return data;
    }

    @Transactional(readOnly = true)
    public Double calculateTotalAmount(Integer orderId) {

        List<OrderItem> items = orderItemApi.getByOrderId(orderId);
        return OrderConversion.calculateTotalAmount(items);
    }

    @Transactional(readOnly = true)
    public InvoiceForm buildInvoiceForm(Integer orderId)
            throws ApiException {

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
    private List<InvoiceItemForm> buildInvoiceItems(Integer orderId)
            throws ApiException {

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

    public void attachInvoice(Integer orderId, String path)
            throws ApiException {
        orderApi.attachInvoice(orderId, path);
    }

    @Transactional(readOnly = true)
    public Order getOrder(Integer orderId) throws ApiException {
        return orderApi.getCheck(orderId);
    }
}
