package com.pos.order.integration.dao;

import com.pos.dao.OrderItemDao;
import com.pos.model.constants.OrderStatus;
import com.pos.pojo.Order;
import com.pos.pojo.OrderItem;
import com.pos.setup.AbstractDaoTest;
import com.pos.setup.TestFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Import({OrderItemDao.class, TestFactory.class})
class OrderItemDaoTest extends AbstractDaoTest {

    @Autowired
    private OrderItemDao dao;

    @Autowired
    private TestFactory testFactory;

    @BeforeEach
    void setupData() {
        // keep empty: don't insert shared rows here (keeps tests isolated)
    }

    @Test
    void selectByOrderIdWhenExists() {
        Order o10 = testFactory.createOrder(OrderStatus.CREATED, "inv");
        Order o11 = testFactory.createOrder(OrderStatus.CREATED, "inv");

        OrderItem a = new OrderItem();
        a.setProductId(101);
        a.setQuantity(2);
        a.setSellingPrice(50.0);

        OrderItem b = new OrderItem();
        b.setProductId(102);
        b.setQuantity(1);
        b.setSellingPrice(20.0);

        OrderItem c = new OrderItem();
        c.setProductId(103);
        c.setQuantity(5);
        c.setSellingPrice(10.0);

        testFactory.createOrderItems(o10.getId(), List.of(a, b));
        testFactory.createOrderItems(o11.getId(), List.of(c));
        em.clear();

        List<OrderItem> out = dao.selectByOrderId(o10.getId());

        assertEquals(2, out.size());
        assertTrue(out.stream().anyMatch(x -> x.getId().equals(a.getId())));
        assertTrue(out.stream().allMatch(x -> x.getOrderId().equals(o10.getId())));
    }

    @Test
    void selectByOrderIdsWhenMultipleProvided() {
        Order o10 = testFactory.createOrder(OrderStatus.CREATED, "inv");
        Order o11 = testFactory.createOrder(OrderStatus.CREATED, "inv");
        Order o12 = testFactory.createOrder(OrderStatus.CREATED, "inv");

        OrderItem i1 = new OrderItem();
        i1.setProductId(101);
        i1.setQuantity(2);
        i1.setSellingPrice(50.0);

        OrderItem i2 = new OrderItem();
        i2.setProductId(102);
        i2.setQuantity(1);
        i2.setSellingPrice(20.0);

        OrderItem i3 = new OrderItem();
        i3.setProductId(103);
        i3.setQuantity(1);
        i3.setSellingPrice(99.0);

        testFactory.createOrderItems(o10.getId(), List.of(i1));
        testFactory.createOrderItems(o11.getId(), List.of(i2));
        testFactory.createOrderItems(o12.getId(), List.of(i3));
        em.clear();

        List<OrderItem> out = dao.selectByOrderIds(List.of(o10.getId(), o11.getId()));

        assertEquals(2, out.size());
        assertTrue(out.stream().allMatch(x -> x.getOrderId().equals(o10.getId()) || x.getOrderId().equals(o11.getId())));
    }
}