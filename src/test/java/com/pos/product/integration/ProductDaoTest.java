package com.pos.product.integration;

import com.pos.dao.ProductDao;
import com.pos.setup.AbstractDaoTest;
import com.pos.setup.TestEntities;
import com.pos.pojo.Product;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Import(ProductDao.class)
class ProductDaoTest extends AbstractDaoTest {

    @Autowired
    private ProductDao dao;

    @Test
    void selectByIdWhenExists() {
        Product p = persist(TestEntities.product("B1", "Soap", 1, 100.0, "img"));
        em.clear();

        assertEquals(p.getId(), dao.selectById(p.getId()).getId());
    }

    @Test
    void searchWhenFiltersProvided() {
        persist(TestEntities.product("B1", "Soap", 1, 100.0, "img"));
        persist(TestEntities.product("B2", "Soap Deluxe", 1, 120.0, "img"));
        persist(TestEntities.product("B3", "Milk", 2, 50.0, "img"));
        em.clear();

        assertEquals(2, dao.search("soap", null, 1, 0, 10).size());
    }

    @Test
    void getCountWhenFiltered() {
        persist(TestEntities.product("B1", "Soap", 1, 100.0, "img"));
        persist(TestEntities.product("B2", "Soap Deluxe", 1, 120.0, "img"));
        persist(TestEntities.product("B3", "Milk", 2, 50.0, "img"));
        em.clear();

        assertEquals(2, dao.getCount("soap", null, 1));
    }

    @Test
    void searchSupportsBarcodeAndPagination() {
        persist(TestEntities.product("B1", "Soap", 1, 100.0, "img"));
        persist(TestEntities.product("B2", "Soap Deluxe", 1, 120.0, "img"));
        persist(TestEntities.product("B3", "Soap Ultra", 1, 150.0, "img"));
        em.clear();

        // barcode filter is exact match
        assertEquals(1, dao.search(null, "B2", null, 0, 10).size());

        // pagination works on ORDER BY p.id
        assertEquals(2, dao.search("soap", null, 1, 0, 2).size());
        assertEquals(1, dao.search("soap", null, 1, 1, 2).size());
    }

    @Test
    void selectByBarcodesHandlesEmptyAndNonEmpty() {
        Product p1 = persist(TestEntities.product("B1", "Soap", 1, 100.0, "img"));
        Product p2 = persist(TestEntities.product("B2", "Milk", 1, 50.0, "img"));
        em.clear();

        assertEquals(List.of(), dao.selectByBarcodes(null));
        assertEquals(List.of(), dao.selectByBarcodes(List.of()));

        List<Product> out = dao.selectByBarcodes(List.of("B1", "B2"));
        assertEquals(2, out.size());
        assertTrue(out.stream().anyMatch(p -> p.getId().equals(p1.getId())));
        assertTrue(out.stream().anyMatch(p -> p.getId().equals(p2.getId())));
    }

    @Test
    void selectByIdsHandlesEmptyAndNonEmpty() {
        Product p1 = persist(TestEntities.product("B1", "Soap", 1, 100.0, "img"));
        Product p2 = persist(TestEntities.product("B2", "Milk", 1, 50.0, "img"));
        em.clear();

        assertEquals(List.of(), dao.selectByIds(null));
        assertEquals(List.of(), dao.selectByIds(List.of()));

        List<Product> out = dao.selectByIds(List.of(p1.getId(), p2.getId()));
        assertEquals(2, out.size());
    }

    @Test
    void findProductIdsByBarcodeOrNameSupportsNameLikeAndBarcode() {
        Product p1 = persist(TestEntities.product("B1", "Soap", 1, 100.0, "img"));
        Product p2 = persist(TestEntities.product("B2", "Soap Deluxe", 1, 120.0, "img"));
        Product p3 = persist(TestEntities.product("B3", "Milk", 2, 50.0, "img"));
        em.clear();

        // Query uses LOWER(p.name) LIKE :name, so pass lowercase input
        List<Integer> idsByName = dao.findProductIdsByBarcodeOrName(null, "soap");
        assertTrue(idsByName.contains(p1.getId()));
        assertTrue(idsByName.contains(p2.getId()));

        List<Integer> idsByBarcode = dao.findProductIdsByBarcodeOrName("B3", null);
        assertEquals(List.of(p3.getId()), idsByBarcode);
    }
}