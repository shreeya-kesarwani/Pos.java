package com.pos.setup;

import com.pos.model.constants.OrderStatus;
import com.pos.model.constants.UserRole;
import com.pos.pojo.*;

import java.time.ZonedDateTime;

public final class TestEntities {

    private TestEntities() {}

    // -------------------- CLIENT --------------------

    /** Mock/new object (no id set) */
    public static Client newClient(String name, String email) {
        Client c = new Client();
        c.setName(name);
        c.setEmail(email);
        return c;
    }

    /** Persistent object (id set) */
    public static Client persistentClient(Integer id, String name, String email) {
        Client c = newClient(name, email);
        c.setId(id);
        return c;
    }

    // -------------------- PRODUCT --------------------

    /** Mock/new object (no id set) */
    public static Product newProduct(String barcode, String name, Integer clientId, Double mrp, String imageUrl) {
        Product p = new Product();
        p.setBarcode(barcode);
        p.setName(name);
        p.setClientId(clientId);
        p.setMrp(mrp);
        p.setImageUrl(imageUrl);
        return p;
    }

    /** Persistent object (id set) */
    public static Product persistentProduct(
            Integer id,
            String barcode,
            String name,
            Integer clientId,
            Double mrp,
            String imageUrl
    ) {
        Product p = newProduct(barcode, name, clientId, mrp, imageUrl);
        p.setId(id);
        return p;
    }

    // -------------------- INVENTORY --------------------

    /** Mock/new object (no id set) */
    public static Inventory newInventory(Integer productId, Integer quantity) {
        Inventory i = new Inventory();
        i.setProductId(productId);
        i.setQuantity(quantity);
        return i;
    }

    /** Persistent object (id set) */
    public static Inventory persistentInventory(Integer id, Integer productId, Integer quantity) {
        Inventory i = newInventory(productId, quantity);
        i.setId(id);
        return i;
    }

    // -------------------- ORDER --------------------

    /** Mock/new object (no id set) */
    public static Order newOrder(OrderStatus status, String invoicePath) {
        Order o = new Order();
        o.setStatus(status);
        o.setInvoicePath(invoicePath);
        return o;
    }

    /**
     * Persistent object (id set).
     * createdAt is optional; pass null if your Order POJO / tests don't need it.
     */
    public static Order persistentOrder(Integer id, OrderStatus status, String invoicePath, ZonedDateTime createdAt) {
        Order o = newOrder(status, invoicePath);
        o.setId(id);
        if (createdAt != null) {
            o.setCreatedAt(createdAt);
        }
        return o;
    }

    // -------------------- ORDER ITEM --------------------

    /** Mock/new object (no id set) */
    public static OrderItem newOrderItem(Integer orderId, Integer productId, Integer quantity, Double sellingPrice) {
        OrderItem oi = new OrderItem();
        oi.setOrderId(orderId);
        oi.setProductId(productId);
        oi.setQuantity(quantity);
        oi.setSellingPrice(sellingPrice);
        return oi;
    }

    /** Persistent object (id set) */
    public static OrderItem persistentOrderItem(
            Integer id,
            Integer orderId,
            Integer productId,
            Integer quantity,
            Double sellingPrice
    ) {
        OrderItem oi = newOrderItem(orderId, productId, quantity, sellingPrice);
        oi.setId(id);
        return oi;
    }

    // -------------------- DAY SALES --------------------

    /** Mock/new object (no id set) */
    public static DaySales newDaySales(ZonedDateTime date, Integer orders, Integer items, Double revenue) {
        DaySales d = new DaySales();
        d.setDate(date);
        d.setInvoicedOrdersCount(orders);
        d.setInvoicedItemsCount(items);
        d.setTotalRevenue(revenue);
        return d;
    }

    /** Persistent object (id set) */
    public static DaySales persistentDaySales(Integer id, ZonedDateTime date, Integer orders, Integer items, Double revenue) {
        DaySales d = newDaySales(date, orders, items, revenue);
        d.setId(id);
        return d;
    }

    // -------------------- USER --------------------

    /** Mock/new object (no id set) */
    public static User newUser(String email, String passwordHash, UserRole role) {
        User u = new User();
        u.setEmail(email);
        u.setPasswordHash(passwordHash);
        u.setRole(role);
        return u;
    }

    /** Persistent object (id set) */
    public static User persistentUser(Integer id, String email, String passwordHash, UserRole role) {
        User u = newUser(email, passwordHash, role);
        u.setId(id);
        return u;
    }
}