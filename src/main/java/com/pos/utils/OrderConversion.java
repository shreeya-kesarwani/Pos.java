package com.pos.utils;

import com.pos.model.data.OrderData;
import com.pos.model.data.OrderItemData;
import com.pos.model.form.OrderForm;
import com.pos.model.form.OrderItemForm;
import com.pos.pojo.Order;
import com.pos.pojo.OrderItem;

import java.util.ArrayList;
import java.util.List;

public class OrderConversion {

    /* =======================
       POJO -> DATA
       ======================= */

    /**
     * Convert Order + computed total into OrderData
     */
    public static OrderData toOrderData(Order order, Double totalAmount) {
        OrderData d = new OrderData();
        d.setId(order.getId());
        d.setCreatedAt(order.getCreatedAt());
        d.setStatus(order.getStatus().name());
        d.setTotalAmount(totalAmount);
        return d;
    }

    /**
     * ✅ PURE helper to calculate total amount
     * total = Σ (quantity × sellingPrice)
     */
    public static Double calculateTotalAmount(List<OrderItem> items) {
        double total = 0.0;

        for (OrderItem item : items) {
            total += item.getQuantity() * item.getSellingPrice();
        }

        return total;
    }

    /**
     * Convert OrderItem + product info into data
     */
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

    /**
     * Minimal conversion (enrichment happens in DTO)
     */
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

    /* =======================
       FORM -> POJO
       ======================= */

    public static Order toOrderPojo(OrderForm form) {
        Order o = new Order();
        // status & timestamps are set in Flow/API layer
        return o;
    }

    public static OrderItem toOrderItemPojo(
            OrderItemForm f,
            Integer productId) {

        OrderItem item = new OrderItem();
        item.setProductId(productId);
        item.setQuantity(f.getQuantity());
        item.setSellingPrice(f.getSellingPrice());
        return item;
    }
}
