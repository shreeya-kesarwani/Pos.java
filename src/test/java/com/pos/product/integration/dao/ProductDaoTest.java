package com.pos.product.integration.dao;

import com.pos.dao.ProductDao;
import com.pos.pojo.Product;
import com.pos.setup.AbstractDaoTest;
import com.pos.setup.TestEntities;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Import({ProductDao.class})
class ProductDaoTest extends AbstractDaoTest {

    @Autowired
    private ProductDao dao;

    private Integer client1;
    private Integer client2;

    @BeforeEach
    void setupData() {
        client1 = 1;
        client2 = 2;
    }

    @Test
    void selectByIdWhenExists() {
        Product p = TestEntities.newProduct("B1", "Soap", client1, 100.0, "img");
        dao.insert(p);
        em.clear();

        Product out = dao.selectById(p.getId());

        assertNotNull(out);
        assertEquals(p.getId(), out.getId());
    }

    @Test
    void searchWhenFiltersProvided() {
        dao.insert(TestEntities.newProduct("B1", "Soap", client1, 100.0, "img"));
        dao.insert(TestEntities.newProduct("B2", "Soap Deluxe", client1, 120.0, "img"));
        dao.insert(TestEntities.newProduct("B3", "Milk", client2, 50.0, "img"));
        em.clear();

        assertEquals(2, dao.search("soap", null, client1, 0, 10).size());
    }

    @Test
    void getCountWhenFiltered() {
        dao.insert(TestEntities.newProduct("B1", "Soap", client1, 100.0, "img"));
        dao.insert(TestEntities.newProduct("B2", "Soap Deluxe", client1, 120.0, "img"));
        dao.insert(TestEntities.newProduct("B3", "Milk", client2, 50.0, "img"));
        em.clear();

        assertEquals(2, dao.getCount("soap", null, client1));
    }

    @Test
    void searchSupportsBarcodeAndPagination() {
        Product p1 = TestEntities.newProduct("B1", "Soap", client1, 100.0, "img");
        Product p2 = TestEntities.newProduct("B2", "Soap Deluxe", client1, 120.0, "img");
        Product p3 = TestEntities.newProduct("B3", "Soap Ultra", client1, 150.0, "img");
        dao.insert(p1);
        dao.insert(p2);
        dao.insert(p3);
        em.clear();

        // barcode filter is exact match
        List<Product> barcodeMatch = dao.search(null, "B2", null, 0, 10);
        assertEquals(1, barcodeMatch.size());
        assertEquals(p2.getId(), barcodeMatch.getFirst().getId());

        // pagination works on ORDER BY p.id
        List<Product> page0 = dao.search("soap", null, client1, 0, 2);
        List<Product> page1 = dao.search("soap", null, client1, 1, 2);

        assertEquals(2, page0.size());
        assertEquals(1, page1.size());

        // since order is by id, page0 should contain the first 2 created products among soap matches
        List<Integer> allSoapIdsInOrder = List.of(p1.getId(), p2.getId(), p3.getId());
        assertEquals(allSoapIdsInOrder.subList(0, 2),
                page0.stream().map(Product::getId).toList());
        assertEquals(allSoapIdsInOrder.subList(2, 3),
                page1.stream().map(Product::getId).toList());
    }

    @Test
    void selectByBarcodesHandlesEmptyAndNonEmpty() {
        Product p1 = TestEntities.newProduct("B1", "Soap", client1, 100.0, "img");
        Product p2 = TestEntities.newProduct("B2", "Milk", client1, 50.0, "img");
        dao.insert(p1);
        dao.insert(p2);
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
        Product p1 = TestEntities.newProduct("B1", "Soap", client1, 100.0, "img");
        Product p2 = TestEntities.newProduct("B2", "Milk", client1, 50.0, "img");
        dao.insert(p1);
        dao.insert(p2);
        em.clear();

        assertEquals(List.of(), dao.selectByIds(null));
        assertEquals(List.of(), dao.selectByIds(List.of()));

        List<Product> out = dao.selectByIds(List.of(p1.getId(), p2.getId()));
        assertEquals(2, out.size());
        assertTrue(out.stream().anyMatch(p -> p.getId().equals(p1.getId())));
        assertTrue(out.stream().anyMatch(p -> p.getId().equals(p2.getId())));
    }

    @Test
    void findProductIdsByBarcodeOrNameSupportsNameLikeAndBarcode() {
        Product p1 = TestEntities.newProduct("B1", "Soap", client1, 100.0, "img");
        Product p2 = TestEntities.newProduct("B2", "Soap Deluxe", client1, 120.0, "img");
        Product p3 = TestEntities.newProduct("B3", "Milk", client2, 50.0, "img");
        dao.insert(p1);
        dao.insert(p2);
        dao.insert(p3);
        em.clear();

        List<Integer> idsByName = dao.findProductIdsByBarcodeOrName(null, "soap");
        assertTrue(idsByName.contains(p1.getId()));
        assertTrue(idsByName.contains(p2.getId()));

        List<Integer> idsByBarcode = dao.findProductIdsByBarcodeOrName("B3", null);
        assertEquals(List.of(p3.getId()), idsByBarcode);
    }
}