package com.pos.order.integration;

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

@Import(OrderItemDao.class)
class OrderItemDaoTest extends AbstractDaoTest {

    @Autowired
    private OrderItemDao dao;

    @BeforeEach
    void clean() {
        em.createNativeQuery("DELETE FROM pos_order_item").executeUpdate();
        em.createNativeQuery("DELETE FROM pos_order").executeUpdate(); // ensure clean if FK exists / later added
        em.flush();
        em.clear();
    }

    @Test
    void selectByOrderIdWhenExists() {
        Order o10 = persist(TestEntities.order(OrderStatus.CREATED, "inv"));
        Order o11 = persist(TestEntities.order(OrderStatus.CREATED, "inv"));

        OrderItem a = persist(TestEntities.orderItem(o10.getId(), 101, 2, 50.0));
        persist(TestEntities.orderItem(o10.getId(), 102, 1, 20.0));
        persist(TestEntities.orderItem(o11.getId(), 103, 5, 10.0));
        em.clear();

        List<OrderItem> out = dao.selectByOrderId(o10.getId());

        assertEquals(2, out.size());
        assertTrue(out.stream().anyMatch(x -> x.getId().equals(a.getId())));
    }

    @Test
    void selectByOrderIdsWhenMultipleProvided() {
        Order o10 = persist(TestEntities.order(OrderStatus.CREATED, "inv"));
        Order o11 = persist(TestEntities.order(OrderStatus.CREATED, "inv"));
        Order o12 = persist(TestEntities.order(OrderStatus.CREATED, "inv"));

        persist(TestEntities.orderItem(o10.getId(), 101, 2, 50.0));
        persist(TestEntities.orderItem(o11.getId(), 102, 1, 20.0));
        persist(TestEntities.orderItem(o12.getId(), 103, 1, 99.0));
        em.clear();

        assertEquals(2, dao.selectByOrderIds(List.of(o10.getId(), o11.getId())).size());
    }
}