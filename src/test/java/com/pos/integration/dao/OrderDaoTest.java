package com.pos.integration.dao;

import com.pos.dao.OrderDao;
import com.pos.integration.setup.AbstractDaoTest;
import com.pos.integration.setup.TestEntities;
import com.pos.model.constants.OrderStatus;
import com.pos.pojo.Order;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Import(OrderDao.class)
class OrderDaoTest extends AbstractDaoTest {

    @Autowired
    private OrderDao dao;

    @BeforeEach
    void clean() {
        em.createNativeQuery("DELETE FROM pos_order_item").executeUpdate();
        em.createNativeQuery("DELETE FROM pos_order").executeUpdate();
        em.flush();
        em.clear();
    }

    @Test
    void searchWhenStatusProvided() {
        persist(TestEntities.order(OrderStatus.CREATED, "inv"));
        Order invoiced = persist(TestEntities.order(OrderStatus.INVOICED, "inv"));
        em.clear();

        List<Order> out = dao.search(null, null, null, OrderStatus.INVOICED, 0, 10);

        assertEquals(1, out.size());
        assertEquals(invoiced.getId(), out.get(0).getId());
    }

    @Test
    void getCountWhenFiltersProvided() {
        persist(TestEntities.order(OrderStatus.INVOICED, "inv"));
        persist(TestEntities.order(OrderStatus.INVOICED, "inv"));
        persist(TestEntities.order(OrderStatus.CREATED, "inv"));
        em.clear();

        assertEquals(2, dao.getCount(null, null, null, OrderStatus.INVOICED));
    }
}