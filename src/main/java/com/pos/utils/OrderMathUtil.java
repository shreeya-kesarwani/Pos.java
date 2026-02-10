package com.pos.utils;

import com.pos.pojo.OrderItem;

import java.util.List;

public final class OrderMathUtil {

    public static double calculateTotalAmount(List<OrderItem> items) {
        if (items == null || items.isEmpty()) return 0.0;

        double total = 0.0;
        for (OrderItem item : items) {
            if (item == null) continue;

            Integer qty = item.getQuantity();
            Double price = item.getSellingPrice();

            if (qty == null || price == null) continue;
            total += qty * price;
        }
        return total;
    }
}
