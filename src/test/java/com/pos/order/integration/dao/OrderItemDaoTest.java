package com.pos.order.integration.dao;

import com.pos.dao.OrderDao;
import com.pos.dao.OrderItemDao;
import com.pos.model.constants.OrderStatus;
import com.pos.pojo.Order;
import com.pos.pojo.OrderItem;
import com.pos.setup.AbstractDaoTest;
import com.pos.setup.TestEntities;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Import({OrderItemDao.class, OrderDao.class})
class OrderItemDaoTest extends AbstractDaoTest {

    @Autowired
    private OrderItemDao dao;

    @Autowired
    private OrderDao orderDao;

    @BeforeEach
    void setupData() {
        // keep empty: don't insert shared rows here (keeps tests isolated)
    }

    @Test
    void selectByOrderIdWhenExists() {
        Order o10 = TestEntities.newOrder(OrderStatus.CREATED, "inv");
        Order o11 = TestEntities.newOrder(OrderStatus.CREATED, "inv");
        orderDao.insert(o10);
        orderDao.insert(o11);

        OrderItem a = TestEntities.newOrderItem(o10.getId(), 101, 2, 50.0);
        OrderItem b = TestEntities.newOrderItem(o10.getId(), 102, 1, 20.0);
        OrderItem c = TestEntities.newOrderItem(o11.getId(), 103, 5, 10.0);

        dao.insert(a);
        dao.insert(b);
        dao.insert(c);

        em.clear();

        List<OrderItem> out = dao.selectByOrderId(o10.getId());

        assertEquals(2, out.size());
        assertTrue(out.stream().anyMatch(x -> x.getId().equals(a.getId())));
        assertTrue(out.stream().allMatch(x -> x.getOrderId().equals(o10.getId())));
    }

    @Test
    void selectByOrderIdsWhenMultipleProvided() {
        Order o10 = TestEntities.newOrder(OrderStatus.CREATED, "inv");
        Order o11 = TestEntities.newOrder(OrderStatus.CREATED, "inv");
        Order o12 = TestEntities.newOrder(OrderStatus.CREATED, "inv");
        orderDao.insert(o10);
        orderDao.insert(o11);
        orderDao.insert(o12);

        OrderItem i1 = TestEntities.newOrderItem(o10.getId(), 101, 2, 50.0);
        OrderItem i2 = TestEntities.newOrderItem(o11.getId(), 102, 1, 20.0);
        OrderItem i3 = TestEntities.newOrderItem(o12.getId(), 103, 1, 99.0);

        dao.insert(i1);
        dao.insert(i2);
        dao.insert(i3);

        em.clear();

        List<OrderItem> out = dao.selectByOrderIds(List.of(o10.getId(), o11.getId()));

        assertEquals(2, out.size());
        assertTrue(out.stream().allMatch(x ->
                x.getOrderId().equals(o10.getId()) || x.getOrderId().equals(o11.getId())));
    }

    @Test
    void selectByOrderIds_whenNull_returnsEmpty() {
        assertEquals(List.of(), dao.selectByOrderIds(null));
    }

    @Test
    void selectByOrderIds_whenEmpty_returnsEmpty() {
        assertEquals(List.of(), dao.selectByOrderIds(List.of()));
    }
}