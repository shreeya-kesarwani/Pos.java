package com.pos.utils;

import com.pos.exception.ApiException;
import com.pos.model.constants.OrderStatus;
import com.pos.model.data.OrderData;
import com.pos.model.data.OrderItemData;
import com.pos.model.form.InvoiceForm;
import com.pos.model.form.InvoiceItemForm;
import com.pos.model.form.OrderForm;
import com.pos.model.form.OrderItemForm;
import com.pos.pojo.Order;
import com.pos.pojo.OrderItem;
import com.pos.pojo.Product;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.pos.model.constants.ErrorMessages.ORDER_NOT_FOUND;
import static com.pos.model.constants.ErrorMessages.PRODUCT_NOT_FOUND;

public class OrderConversion {

    private OrderConversion() {}

    public static Order toOrderPojo(OrderForm form) {
        Order order = new Order();
        order.setStatus(OrderStatus.CREATED);
        return order;
    }

    public static OrderItem toOrderItemPojo(OrderItemForm f, Integer productId) {
        OrderItem item = new OrderItem();
        item.setProductId(productId);
        item.setQuantity(f.getQuantity());
        item.setSellingPrice(f.getSellingPrice());
        return item;
    }

    public static OrderData toOrderData(Order order, Double totalAmount) {
        OrderData d = new OrderData();
        d.setId(order.getId());
        d.setCreatedAt(order.getCreatedAt());
        d.setStatus(order.getStatus().name());
        d.setTotalAmount(totalAmount);
        return d;
    }

    public static OrderItemData toOrderItemData(OrderItem item, Product p) {
        return toOrderItemData(item, p.getBarcode(), p.getName());
    }

    public static OrderItemData toOrderItemData(OrderItem item, String barcode, String productName) {
        OrderItemData d = new OrderItemData();
        d.setId(item.getId());
        d.setBarcode(barcode);
        d.setProductName(productName);
        d.setQuantity(item.getQuantity());
        d.setSellingPrice(item.getSellingPrice());
        return d;
    }

    public static List<OrderItemData> toOrderItemDataList(List<OrderItem> items) {
        List<OrderItemData> list = new ArrayList<>();
        if (items == null || items.isEmpty()) return list;

        for (OrderItem item : items) {
            OrderItemData d = new OrderItemData();
            d.setId(item.getId());
            d.setQuantity(item.getQuantity());
            d.setSellingPrice(item.getSellingPrice());
            list.add(d);
        }
        return list;
    }

    public static OrderData toOrderDataWithTotal(Order order, List<OrderItem> items) {
        double totalAmount = OrderMathUtil.calculateTotalAmount(items);
        return OrderConversion.toOrderData(order, totalAmount);
    }

    public static List<OrderItemData> toOrderItemDataList(List<OrderItem> items, Map<Integer, Product> productById) throws ApiException {
        if (items == null || items.isEmpty()) return List.of();

        List<OrderItemData> data = new ArrayList<>();
        for (OrderItem item : items) {
            Product product = productById.get(item.getProductId());
            if (product == null) {
                throw new ApiException(PRODUCT_NOT_FOUND.value() + ": productId=" + item.getProductId());
            }
            data.add(toOrderItemData(item, product));
        }
        return data;
    }

    public static InvoiceForm toInvoiceForm(Order order, List<OrderItem> items, Map<Integer, Product> productById) throws ApiException {
        if (order == null || order.getId() == null) {
            throw new ApiException(ORDER_NOT_FOUND.value());
        }
        return toInvoiceForm(order.getId(), items, productById);
    }

    public static InvoiceForm toInvoiceForm(Integer orderId, List<OrderItem> items, Map<Integer, Product> productById) throws ApiException {

        InvoiceForm form = new InvoiceForm();
        form.setOrderId(orderId);
        form.setItems(toInvoiceItems(items, productById));
        return form;
    }

    private static List<InvoiceItemForm> toInvoiceItems(List<OrderItem> items, Map<Integer, Product> productById) throws ApiException {

        List<InvoiceItemForm> invoiceItems = new ArrayList<>();
        if (items == null || items.isEmpty()) return invoiceItems;

        for (OrderItem item : items) {
            Integer productId = item.getProductId();
            Product p = productById.get(productId);

            if (p == null) {
                throw new ApiException("Product not found: " + productId);
            }

            InvoiceItemForm f = new InvoiceItemForm();
            f.setName(p.getName());
            f.setQuantity(item.getQuantity());
            f.setSellingPrice(item.getSellingPrice());
            invoiceItems.add(f);
        }

        return invoiceItems;
    }
}