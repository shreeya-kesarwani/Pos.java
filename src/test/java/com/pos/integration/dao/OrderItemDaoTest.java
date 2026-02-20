package com.pos.integration.dao;

import com.pos.dao.OrderItemDao;
import com.pos.integration.setup.AbstractDaoTest;
import com.pos.integration.setup.TestEntities;
import com.pos.pojo.OrderItem;
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
        em.flush();
        em.clear();
    }

    @Test
    void selectByOrderIdWhenExists() {
        OrderItem a = persist(TestEntities.orderItem(10, 101, 2, 50.0));
        persist(TestEntities.orderItem(10, 102, 1, 20.0));
        persist(TestEntities.orderItem(11, 103, 5, 10.0));
        em.clear();

        List<OrderItem> out = dao.selectByOrderId(10);

        assertEquals(2, out.size());
        assertTrue(out.stream().anyMatch(x -> x.getId().equals(a.getId())));
    }

    @Test
    void selectByOrderIdsWhenMultipleProvided() {
        persist(TestEntities.orderItem(10, 101, 2, 50.0));
        persist(TestEntities.orderItem(11, 102, 1, 20.0));
        persist(TestEntities.orderItem(12, 103, 1, 99.0));
        em.clear();

        assertEquals(2, dao.selectByOrderIds(List.of(10, 11)).size());
    }
}