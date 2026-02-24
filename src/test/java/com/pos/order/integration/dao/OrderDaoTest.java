package com.pos.order.integration.dao;

import com.pos.dao.OrderDao;
import com.pos.model.constants.OrderStatus;
import com.pos.pojo.Order;
import com.pos.setup.AbstractDaoTest;
import com.pos.setup.TestFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Import({OrderDao.class, TestFactory.class})
class OrderDaoTest extends AbstractDaoTest {

    @Autowired
    private OrderDao dao;

    @Autowired
    private TestFactory testFactory;

    @BeforeEach
    void setupData() {
        // keep empty: don't insert shared rows here (keeps tests isolated)
    }

    @Test
    void searchWhenStatusProvided() {
        testFactory.createOrder(OrderStatus.CREATED, "inv");
        Order invoiced = testFactory.createOrder(OrderStatus.INVOICED, "inv");
        em.clear();

        List<Order> out = dao.search(null, null, null, OrderStatus.INVOICED, 0, 10);

        assertEquals(1, out.size());
        assertEquals(invoiced.getId(), out.getFirst().getId());
    }

    @Test
    void getCountWhenFiltersProvided() {
        testFactory.createOrder(OrderStatus.INVOICED, "inv");
        testFactory.createOrder(OrderStatus.INVOICED, "inv");
        testFactory.createOrder(OrderStatus.CREATED, "inv");
        em.clear();

        assertEquals(2, dao.getCount(null, null, null, OrderStatus.INVOICED));
    }
}