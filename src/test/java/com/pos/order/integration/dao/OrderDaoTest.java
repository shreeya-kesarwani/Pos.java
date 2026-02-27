package com.pos.order.integration.dao;

import com.pos.dao.OrderDao;
import com.pos.model.constants.OrderStatus;
import com.pos.pojo.Order;
import com.pos.setup.AbstractDaoTest;
import com.pos.setup.TestEntities;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Import({OrderDao.class})
class OrderDaoTest extends AbstractDaoTest {

    @Autowired
    private OrderDao dao;

    @BeforeEach
    void setupData() {
        // keep empty: don't insert shared rows here (keeps tests isolated)
    }

    @Test
    void searchWhenStatusProvided() {
        dao.insert(TestEntities.newOrder(OrderStatus.CREATED, "inv"));
        Order invoiced = TestEntities.newOrder(OrderStatus.INVOICED, "inv");
        dao.insert(invoiced);
        em.clear();

        List<Order> out = dao.search(null, null, null, OrderStatus.INVOICED, 0, 10);

        assertEquals(1, out.size());
        assertEquals(invoiced.getId(), out.getFirst().getId());
    }

    @Test
    void getCountWhenFiltersProvided() {
        dao.insert(TestEntities.newOrder(OrderStatus.INVOICED, "inv"));
        dao.insert(TestEntities.newOrder(OrderStatus.INVOICED, "inv"));
        dao.insert(TestEntities.newOrder(OrderStatus.CREATED, "inv"));
        em.clear();

        assertEquals(2, dao.getCount(null, null, null, OrderStatus.INVOICED));
    }
}