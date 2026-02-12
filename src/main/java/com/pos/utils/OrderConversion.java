package com.pos.utils;

import com.pos.exception.ApiException;
import com.pos.model.data.OrderData;
import com.pos.model.data.OrderItemData;
import com.pos.model.form.OrderForm;
import com.pos.model.form.OrderItemForm;
import com.pos.pojo.Order;
import com.pos.pojo.OrderItem;
import com.pos.pojo.Product;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class OrderConversion {

    public static OrderData toOrderData(Order order, Double totalAmount) {
        OrderData d = new OrderData();
        d.setId(order.getId());
        d.setCreatedAt(order.getCreatedAt());
        d.setStatus(order.getStatus().name());
        d.setTotalAmount(totalAmount);
        return d;
    }

    public static OrderItemData toOrderItemData(
            OrderItem item,
            String barcode,
            String productName) {

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

        for (OrderItem item : items) {
            OrderItemData d = new OrderItemData();
            d.setId(item.getId());
            d.setQuantity(item.getQuantity());
            d.setSellingPrice(item.getSellingPrice());
            list.add(d);
        }

        return list;
    }

    public static OrderItem toOrderItemPojo(OrderItemForm f, Integer productId) {
        OrderItem item = new OrderItem();
        item.setProductId(productId);
        item.setQuantity(f.getQuantity());
        item.setSellingPrice(f.getSellingPrice());
        return item;
    }

    public static OrderData toOrderDataWithTotal(Order order, List<OrderItem> items) {
        double totalAmount = OrderMathUtil.calculateTotalAmount(items);
        return OrderConversion.toOrderData(order, totalAmount);
    }

    public static List<OrderItemData> toOrderItemDataList(
            List<OrderItem> items,
            Map<Integer, Product> productById
    ) throws ApiException {
        if (items == null || items.isEmpty()) return List.of();

        List<OrderItemData> data = new ArrayList<>();
        for (OrderItem item : items) {
            Product product = productById.get(item.getProductId());
            if (product == null) {
                throw new ApiException("Product not found: productId=" + item.getProductId());
            }
            data.add(toOrderItemData(item, product.getBarcode(), product.getName()));
        }
        return data;
    }

}
