package com.pos.setup;

import com.pos.model.constants.OrderStatus;
import com.pos.model.constants.UserRole;
import com.pos.pojo.*;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

public final class TestFactory {

    private TestFactory() {}

    public static Client newClient(String name, String email) {
        return TestEntities.newClient(name, email);
    }

    public static Client persistentClient(Integer id, String name, String email) {
        return TestEntities.persistentClient(id, name, email);
    }

    // ---------- PRODUCT ----------
    public static Product newProduct(String barcode, String name, Integer clientId, Double mrp, String imageUrl) {
        return TestEntities.newProduct(barcode, name, clientId, mrp, imageUrl);
    }

    public static Product persistentProduct(Integer id, String barcode, String name, Integer clientId, Double mrp, String imageUrl) {
        return TestEntities.persistentProduct(id, barcode, name, clientId, mrp, imageUrl);
    }

    // ---------- INVENTORY ----------
    public static Inventory newInventory(Integer productId, Integer qty) {
        return TestEntities.newInventory(productId, qty);
    }

    public static Inventory persistentInventory(Integer id, Integer productId, Integer qty) {
        return TestEntities.persistentInventory(id, productId, qty);
    }

    // ---------- USER ----------
    public static User newUser(String email, String passwordHash, UserRole role) {
        return TestEntities.newUser(email, passwordHash, role);
    }

    public static User persistentUser(Integer id, String email, String passwordHash, UserRole role) {
        return TestEntities.persistentUser(id, email, passwordHash, role);
    }

    // ---------- ORDER ----------
    public static Order newOrder(OrderStatus status, String invoicePath) {
        return TestEntities.newOrder(status, invoicePath);
    }

    public static Order persistentOrder(Integer id, OrderStatus status, String invoicePath, ZonedDateTime createdAt) {
        return TestEntities.persistentOrder(id, status, invoicePath, createdAt);
    }

    // ---------- ORDER ITEM ----------
    public static OrderItem newOrderItem(Integer orderId, Integer productId, Integer qty, Double sp) {
        return TestEntities.newOrderItem(orderId, productId, qty, sp);
    }

    public static OrderItem persistentOrderItem(Integer id, Integer orderId, Integer productId, Integer qty, Double sp) {
        return TestEntities.persistentOrderItem(id, orderId, productId, qty, sp);
    }

    /**
     * Convenience: create N items for an orderId (still NOT persisted).
     * It sets orderId on each item and returns a new list.
     */
    public static List<OrderItem> newOrderItems(Integer orderId, List<OrderItem> items) {
        if (items == null) return List.of();
        List<OrderItem> out = new ArrayList<>(items.size());
        for (OrderItem oi : items) {
            oi.setOrderId(orderId);
            out.add(oi);
        }
        return out;
    }

    // ---------- DAY SALES ----------
    public static DaySales newDaySales(ZonedDateTime date, int orders, int items, double revenue) {
        return TestEntities.newDaySales(date, orders, items, revenue);
    }

    public static DaySales persistentDaySales(Integer id, ZonedDateTime date, int orders, int items, double revenue) {
        return TestEntities.persistentDaySales(id, date, orders, items, revenue);
    }
}