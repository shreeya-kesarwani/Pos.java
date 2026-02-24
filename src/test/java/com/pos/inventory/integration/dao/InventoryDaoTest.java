package com.pos.inventory.integration.dao;

import com.pos.dao.InventoryDao;
import com.pos.pojo.Inventory;
import com.pos.setup.AbstractDaoTest;
import com.pos.setup.TestFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Import({InventoryDao.class, TestFactory.class})
class InventoryDaoTest extends AbstractDaoTest {

    @Autowired
    private InventoryDao dao;

    @Autowired
    private TestFactory testFactory;

    private List<Integer> productIds4;

    @BeforeEach
    void setupData() {
        productIds4 = List.of(10, 20, 30, 40);
    }

    @Test
    void selectByProductIdWhenNotFound() {
        assertNull(dao.selectByProductId(999));
    }

    @Test
    void selectByProductIdWhenExists() {
        Inventory i = testFactory.createInventory(101, 5);
        testFactory.createInventory(102, 10);
        em.clear();

        Inventory out = dao.selectByProductId(101);

        assertNotNull(out);
        assertEquals(i.getId(), out.getId());
        assertEquals(5, out.getQuantity());
    }

    @Test
    void findByProductIdsWhenNull() {
        testFactory.createInventory(1, 1);
        testFactory.createInventory(2, 2);
        testFactory.createInventory(3, 3);
        em.clear();

        assertEquals(3, dao.findByProductIds(null, 0, 10).size());
    }

    @Test
    void getCountByProductIdsWhenFiltered() {
        testFactory.createInventory(10, 1);
        testFactory.createInventory(20, 2);
        testFactory.createInventory(30, 3);
        em.clear();

        assertEquals(2L, dao.getCountByProductIds(List.of(10, 20)));
    }

    @Test
    void findByProductIdsWhenFilteredAndPaged() {
        // product_id is UNIQUE in pos_inventory, so every inventory row must have a unique productId
        testFactory.createInventory(10, 1);
        testFactory.createInventory(20, 2);
        testFactory.createInventory(30, 3);
        testFactory.createInventory(40, 4);
        em.clear();

        // page 0 size 1 => 1 row
        List<Inventory> page0 = dao.findByProductIds(productIds4, 0, 1);
        assertEquals(1, page0.size());

        // page 1 size 2 => 2 rows
        List<Inventory> page1 = dao.findByProductIds(productIds4, 1, 2);
        assertEquals(2, page1.size());

        assertTrue(productIds4.contains(page0.getFirst().getProductId()));
        assertTrue(productIds4.contains(page1.get(0).getProductId()));
        assertTrue(productIds4.contains(page1.get(1).getProductId()));
    }

    @Test
    void getCountByProductIdsWhenNullOrEmptyUsesTotalCount() {
        testFactory.createInventory(1, 1);
        testFactory.createInventory(2, 2);
        em.clear();

        assertEquals(2L, dao.getCountByProductIds(null));
        assertEquals(2L, dao.getCountByProductIds(List.of()));
    }

    @Test
    void selectByProductIdsHandlesEmptyAndNonEmpty() {
        Inventory a = testFactory.createInventory(10, 1);
        Inventory b = testFactory.createInventory(20, 2);
        em.clear();

        assertEquals(List.of(), dao.selectByProductIds(null));
        assertEquals(List.of(), dao.selectByProductIds(List.of()));

        List<Inventory> out = dao.selectByProductIds(List.of(10, 20));
        assertEquals(2, out.size());

        // Order is typically stable here because query uses IN (...) ordering is not guaranteed by SQL,
        // so assert by ids, not index.
        assertTrue(out.stream().anyMatch(x -> x.getId().equals(a.getId())));
        assertTrue(out.stream().anyMatch(x -> x.getId().equals(b.getId())));
    }

    @Test
    void selectByIdReturnsEntity() {
        Inventory i = testFactory.createInventory(999, 7);
        em.clear();

        Inventory out = dao.selectById(i.getId());

        assertNotNull(out);
        assertEquals(i.getId(), out.getId());
    }
}