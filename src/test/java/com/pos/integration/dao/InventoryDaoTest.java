package com.pos.integration.dao;

import com.pos.dao.InventoryDao;
import com.pos.integration.setup.AbstractDaoTest;
import com.pos.integration.setup.TestEntities;
import com.pos.pojo.Inventory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Import(InventoryDao.class)
class InventoryDaoTest extends AbstractDaoTest {

    @Autowired
    private InventoryDao dao;

    @BeforeEach
    void clean() {
        em.createNativeQuery("DELETE FROM pos_inventory").executeUpdate();
        em.flush();
        em.clear();
    }

    @Test
    void selectByProductIdWhenNotFound() {
        assertNull(dao.selectByProductId(999));
    }

    @Test
    void selectByProductIdWhenExists() {
        Inventory i = persist(TestEntities.inventory(101, 5));
        persist(TestEntities.inventory(102, 10));
        em.clear();

        Inventory out = dao.selectByProductId(101);

        assertNotNull(out);
        assertEquals(i.getId(), out.getId());
        assertEquals(5, out.getQuantity());
    }

    @Test
    void findByProductIdsWhenNull() {
        persist(TestEntities.inventory(1, 1));
        persist(TestEntities.inventory(2, 2));
        persist(TestEntities.inventory(3, 3));
        em.clear();

        assertEquals(3, dao.findByProductIds(null, 0, 10).size());
    }

    @Test
    void getCountByProductIdsWhenFiltered() {
        persist(TestEntities.inventory(10, 1));
        persist(TestEntities.inventory(20, 2));
        persist(TestEntities.inventory(30, 3));
        em.clear();

        assertEquals(2, dao.getCountByProductIds(List.of(10, 20)));
    }
}