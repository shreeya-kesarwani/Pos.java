package com.pos.inventory.integration.dao;

import com.pos.dao.InventoryDao;
import com.pos.pojo.Inventory;
import com.pos.setup.AbstractDaoTest;
import com.pos.setup.TestEntities;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Import({InventoryDao.class})
class InventoryDaoTest extends AbstractDaoTest {

    @Autowired
    private InventoryDao dao;

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
        Inventory i = TestEntities.newInventory(101, 5);
        dao.insert(i);
        dao.insert(TestEntities.newInventory(102, 10));
        em.clear();

        Inventory out = dao.selectByProductId(101);

        assertNotNull(out);
        assertEquals(i.getId(), out.getId());
        assertEquals(5, out.getQuantity());
    }

    @Test
    void findByProductIdsWhenNull() {
        dao.insert(TestEntities.newInventory(1, 1));
        dao.insert(TestEntities.newInventory(2, 2));
        dao.insert(TestEntities.newInventory(3, 3));
        em.clear();

        assertEquals(3, dao.findByProductIds(null, 0, 10).size());
    }

    @Test
    void getCountByProductIdsWhenFiltered() {
        dao.insert(TestEntities.newInventory(10, 1));
        dao.insert(TestEntities.newInventory(20, 2));
        dao.insert(TestEntities.newInventory(30, 3));
        em.clear();

        assertEquals(2L, dao.getCountByProductIds(List.of(10, 20)));
    }

    @Test
    void findByProductIdsWhenFilteredAndPaged() {
        // product_id is UNIQUE in pos_inventory, so every inventory row must have a unique productId
        dao.insert(TestEntities.newInventory(10, 1));
        dao.insert(TestEntities.newInventory(20, 2));
        dao.insert(TestEntities.newInventory(30, 3));
        dao.insert(TestEntities.newInventory(40, 4));
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
        dao.insert(TestEntities.newInventory(1, 1));
        dao.insert(TestEntities.newInventory(2, 2));
        em.clear();

        assertEquals(2L, dao.getCountByProductIds(null));
        assertEquals(2L, dao.getCountByProductIds(List.of()));
    }

    @Test
    void selectByProductIdsHandlesEmptyAndNonEmpty() {
        Inventory a = TestEntities.newInventory(10, 1);
        Inventory b = TestEntities.newInventory(20, 2);
        dao.insert(a);
        dao.insert(b);
        em.clear();

        assertEquals(List.of(), dao.selectByProductIds(null));
        assertEquals(List.of(), dao.selectByProductIds(List.of()));

        List<Inventory> out = dao.selectByProductIds(List.of(10, 20));
        assertEquals(2, out.size());

        // IN (...) ordering isn't guaranteed; assert by ids.
        assertTrue(out.stream().anyMatch(x -> x.getId().equals(a.getId())));
        assertTrue(out.stream().anyMatch(x -> x.getId().equals(b.getId())));
    }

    @Test
    void selectByIdReturnsEntity() {
        Inventory i = TestEntities.newInventory(999, 7);
        dao.insert(i);
        em.clear();

        Inventory out = dao.selectById(i.getId());

        assertNotNull(out);
        assertEquals(i.getId(), out.getId());
    }
}