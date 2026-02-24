package com.pos.inventory.integration;

import com.pos.dao.InventoryDao;
import com.pos.setup.AbstractDaoTest;
import com.pos.setup.TestEntities;
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

        assertEquals(2L, dao.getCountByProductIds(List.of(10, 20)));
    }

    @Test
    void findByProductIdsWhenFilteredAndPaged() {
        // product_id is UNIQUE in pos_inventory, so every inventory row must have a unique productId
        persist(TestEntities.inventory(10, 1));
        persist(TestEntities.inventory(20, 2));
        persist(TestEntities.inventory(30, 3));
        persist(TestEntities.inventory(40, 4));
        em.clear();

        // Filtered list
        List<Integer> productIds = List.of(10, 20, 30, 40);

        // page 0 size 1 => 1 row
        List<Inventory> page0 = dao.findByProductIds(productIds, 0, 1);
        assertEquals(1, page0.size());

        // page 1 size 2 => 2 rows
        List<Inventory> page1 = dao.findByProductIds(productIds, 1, 2);
        assertEquals(2, page1.size());
        assertTrue(productIds.contains(page0.get(0).getProductId()));
        assertTrue(productIds.contains(page1.get(0).getProductId()));
        assertTrue(productIds.contains(page1.get(1).getProductId()));
    }

    @Test
    void getCountByProductIdsWhenNullOrEmptyUsesTotalCount() {
        persist(TestEntities.inventory(1, 1));
        persist(TestEntities.inventory(2, 2));
        em.clear();

        assertEquals(2L, dao.getCountByProductIds(null));
        assertEquals(2L, dao.getCountByProductIds(List.of()));
    }

    @Test
    void selectByProductIdsHandlesEmptyAndNonEmpty() {
        Inventory a = persist(TestEntities.inventory(10, 1));
        Inventory b = persist(TestEntities.inventory(20, 2));
        em.clear();

        assertEquals(List.of(), dao.selectByProductIds(null));
        assertEquals(List.of(), dao.selectByProductIds(List.of()));

        List<Inventory> out = dao.selectByProductIds(List.of(10, 20));
        assertEquals(2, out.size());
        assertEquals(a.getId(), out.get(0).getId());
        assertEquals(b.getId(), out.get(1).getId());
    }

    @Test
    void selectByIdReturnsEntity() {
        Inventory i = persist(TestEntities.inventory(999, 7));
        em.clear();

        Inventory out = dao.selectById(i.getId());
        assertNotNull(out);
        assertEquals(i.getId(), out.getId());
    }
}