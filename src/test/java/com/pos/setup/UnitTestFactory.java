package com.pos.setup;

import com.pos.model.constants.OrderStatus;
import com.pos.model.constants.UserRole;
import com.pos.model.form.InventoryForm;
import com.pos.pojo.*;

import java.time.ZonedDateTime;

public class UnitTestFactory {

    private UnitTestFactory() {}

    public static Client client(Integer id, String name, String email) {
        Client c = new Client();
        c.setId(id);
        c.setName(name);
        c.setEmail(email);
        return c;
    }

    public static Client clientWithName(String name) {
        Client c = new Client();
        c.setName(name);
        return c;
    }

    public static Inventory inventory(Integer productId, Integer qty) {
        Inventory inv = new Inventory();
        inv.setProductId(productId);
        inv.setQuantity(qty);
        return inv;
    }

    public static InventoryForm inventoryForm(String barcode) {
        InventoryForm f = new InventoryForm();
        f.setBarcode(barcode);
        return f;
    }

    public static Order order(Integer id, OrderStatus status, String invoicePath) {
        Order o = new Order();
        o.setId(id);
        o.setStatus(status);
        o.setInvoicePath(invoicePath);
        o.setCreatedAt(ZonedDateTime.now());
        return o;
    }

    public static OrderItem orderItem(Integer productId, Integer qty, Double sellingPrice) {
        OrderItem oi = new OrderItem();
        oi.setProductId(productId);
        oi.setQuantity(qty);
        oi.setSellingPrice(sellingPrice);
        return oi;
    }

    public static Product product(Integer id, String barcode, Integer clientId, String name, Double mrp, String imageUrl) {
        Product p = new Product();
        p.setId(id);
        p.setBarcode(barcode);
        p.setClientId(clientId);
        p.setName(name);
        p.setMrp(mrp);
        p.setImageUrl(imageUrl);
        return p;
    }

    public static Product productWithBarcode(String barcode) {
        Product p = new Product();
        p.setBarcode(barcode);
        return p;
    }

    public static User user(String email, String rawPasswordOrHash, UserRole role) {
        User u = new User();
        u.setEmail(email);
        u.setPasswordHash(rawPasswordOrHash);
        u.setRole(role);
        return u;
    }
}