package com.pos.utils;

import com.pos.pojo.OrderItem;
import com.pos.utils.OrderMathUtil;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class OrderMathUtilTest {

    private static OrderItem item(Integer qty, Double price) {
        OrderItem oi = new OrderItem();
        oi.setQuantity(qty);
        oi.setSellingPrice(price);
        return oi;
    }

    @Test
    void calculateTotalAmount_shouldReturnZero_whenItemsNull() {
        assertEquals(0.0, OrderMathUtil.calculateTotalAmount(null), 0.0001);
    }

    @Test
    void calculateTotalAmount_shouldReturnZero_whenItemsEmpty() {
        assertEquals(0.0, OrderMathUtil.calculateTotalAmount(Collections.emptyList()), 0.0001);
    }

    @Test
    void calculateTotalAmount_shouldSumValidItems() {
        List<OrderItem> items = Arrays.asList(
                item(2, 10.0),   // 20
                item(1, 5.5)     // 5.5
        );
        assertEquals(25.5, OrderMathUtil.calculateTotalAmount(items), 0.0001);
    }

    @Test
    void calculateTotalAmount_shouldIgnoreNullItems() {
        List<OrderItem> items = Arrays.asList(
                null,
                item(2, 10.0)
        );
        assertEquals(20.0, OrderMathUtil.calculateTotalAmount(items), 0.0001);
    }

    @Test
    void calculateTotalAmount_shouldIgnoreItemsWithNullQuantity() {
        List<OrderItem> items = Arrays.asList(
                item(null, 10.0),
                item(2, 10.0)
        );
        assertEquals(20.0, OrderMathUtil.calculateTotalAmount(items), 0.0001);
    }

    @Test
    void calculateTotalAmount_shouldIgnoreItemsWithNullSellingPrice() {
        List<OrderItem> items = Arrays.asList(
                item(2, null),
                item(2, 10.0)
        );
        assertEquals(20.0, OrderMathUtil.calculateTotalAmount(items), 0.0001);
    }

    @Test
    void calculateTotalAmount_shouldHandleMixedValidAndInvalidItems() {
        List<OrderItem> items = Arrays.asList(
                null,
                item(2, 10.0),     // valid => 20
                item(null, 99.0),  // ignored
                item(3, null),     // ignored
                item(1, 2.5)       // valid => 2.5
        );

        assertEquals(22.5, OrderMathUtil.calculateTotalAmount(items), 0.0001);
    }
}
