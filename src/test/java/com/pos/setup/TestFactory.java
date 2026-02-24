package com.pos.setup;

import com.pos.dao.*;
import com.pos.model.constants.OrderStatus;
import com.pos.model.constants.UserRole;
import com.pos.pojo.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.List;

@Component
@Transactional
public class TestFactory {

    @Autowired private ClientDao clientDao;
    @Autowired private ProductDao productDao;
    @Autowired private InventoryDao inventoryDao;
    @Autowired private UserDao userDao;
    @Autowired private OrderDao orderDao;
    @Autowired private OrderItemDao orderItemDao;
    @Autowired private DaySalesDao daySalesDao;

    public Client createClient(String name, String email) {
        Client c = TestEntities.client(name, email);
        clientDao.insert(c);
        return c;
    }

    public Product createProduct(String barcode, String name, Integer clientId, Double mrp, String imageUrl) {
        Product p = TestEntities.product(barcode, name, clientId, mrp, imageUrl);
        productDao.insert(p);
        return p;
    }

    public Inventory createInventory(Integer productId, Integer qty) {
        Inventory i = TestEntities.inventory(productId, qty);
        inventoryDao.insert(i);
        return i;
    }

    public User createUser(String email, String passwordHash, UserRole role) {
        User u = TestEntities.user(email, passwordHash, role);
        userDao.insert(u);
        return u;
    }

    public Order createOrder(OrderStatus status, String invoicePath) {
        Order o = TestEntities.order(status, invoicePath);
        orderDao.insert(o);
        return o;
    }

    public List<OrderItem> createOrderItems(Integer orderId, List<OrderItem> items) {
        for (OrderItem oi : items) {
            oi.setOrderId(orderId);
            orderItemDao.insert(oi);
        }
        return items;
    }

    public DaySales createDaySales(ZonedDateTime date, int orders, int items, double revenue) {
        DaySales d = TestEntities.daySales(date, orders, items, revenue);
        daySalesDao.insert(d);
        return d;
    }
}