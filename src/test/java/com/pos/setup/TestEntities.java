package com.pos.setup;

import com.pos.model.constants.OrderStatus;
import com.pos.model.constants.UserRole;
import com.pos.pojo.*;

import java.time.ZonedDateTime;

public final class TestEntities {

    private TestEntities() {}

    public static Client client(String name, String email) {
        Client c = new Client();
        c.setName(name);
        c.setEmail(email);
        return c;
    }

    public static Product product(String barcode, String name, Integer clientId, Double mrp, String imageUrl) {
        Product p = new Product();
        p.setBarcode(barcode);
        p.setName(name);
        p.setClientId(clientId);
        p.setMrp(mrp);
        p.setImageUrl(imageUrl);
        return p;
    }

    public static Inventory inventory(Integer productId, Integer quantity) {
        Inventory i = new Inventory();
        i.setProductId(productId);
        i.setQuantity(quantity);
        return i;
    }

    public static Order order(OrderStatus status, String invoicePath) {
        Order o = new Order();
        o.setStatus(status);
        o.setInvoicePath(invoicePath);
        return o;
    }

    public static OrderItem orderItem(Integer orderId, Integer productId, Integer quantity, Double sellingPrice) {
        OrderItem oi = new OrderItem();
        oi.setOrderId(orderId);
        oi.setProductId(productId);
        oi.setQuantity(quantity);
        oi.setSellingPrice(sellingPrice);
        return oi;
    }

    public static DaySales daySales(ZonedDateTime date, Integer orders, Integer items, Double revenue) {
        DaySales d = new DaySales();
        d.setDate(date);
        d.setInvoicedOrdersCount(orders);
        d.setInvoicedItemsCount(items);
        d.setTotalRevenue(revenue);
        return d;
    }

    public static User user(String email, String passwordHash, UserRole role) {
        User u = new User();
        u.setEmail(email);
        u.setPasswordHash(passwordHash);
        u.setRole(role);
        return u;
    }
}