package com.pos.unit.api;

import com.pos.api.ProductApi;
import com.pos.dao.ProductDao;
import com.pos.exception.ApiException;
import com.pos.pojo.Product;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductApiTest {

    @InjectMocks
    private ProductApi productApi;

    @Mock
    private ProductDao productDao;

    // -------------------- get / getCheck --------------------

    @Test
    void get_shouldCallDaoSelectById() {
        Product p = new Product();
        p.setId(1);

        when(productDao.selectById(1)).thenReturn(p);

        Product out = productApi.get(1);

        assertSame(p, out);
        verify(productDao).selectById(1);
    }

    @Test
    void getCheck_shouldReturnProduct_whenExists() throws ApiException {
        Product p = new Product();
        p.setId(7);

        when(productDao.selectById(7)).thenReturn(p);

        Product out = productApi.getCheck(7);

        assertSame(p, out);
    }

    @Test
    void getCheck_shouldThrow_whenNotFound() {
        when(productDao.selectById(99)).thenReturn(null);

        assertThrows(ApiException.class, () -> productApi.getCheck(99));
    }

    // -------------------- getByIds / getByBarcodes --------------------

    @Test
    void getByIds_shouldReturnEmpty_whenNullOrEmpty() {
        assertEquals(List.of(), productApi.getByIds(null));
        assertEquals(List.of(), productApi.getByIds(List.of()));
        verifyNoInteractions(productDao);
    }

    @Test
    void getByIds_shouldCallDao_whenNonEmpty() {
        List<Integer> ids = List.of(1, 2, 3);
        List<Product> found = List.of(new Product(), new Product());

        when(productDao.selectByIds(ids)).thenReturn(found);

        List<Product> out = productApi.getByIds(ids);

        assertSame(found, out);
        verify(productDao).selectByIds(ids);
    }

    @Test
    void getByBarcodes_shouldReturnEmpty_whenNullOrEmpty() {
        assertEquals(List.of(), productApi.getByBarcodes(null));
        assertEquals(List.of(), productApi.getByBarcodes(List.of()));
        verifyNoInteractions(productDao);
    }

    @Test
    void getByBarcodes_shouldCallDao_whenNonEmpty() {
        List<String> barcodes = List.of("A", "B");
        List<Product> found = List.of(new Product());

        when(productDao.selectByBarcodes(barcodes)).thenReturn(found);

        List<Product> out = productApi.getByBarcodes(barcodes);

        assertSame(found, out);
        verify(productDao).selectByBarcodes(barcodes);
    }

    // -------------------- getCheckByBarcodes --------------------

    @Test
    void getCheckByBarcodes_shouldThrow_whenEmptyInput() {
        assertThrows(ApiException.class, () -> productApi.getCheckByBarcodes(null));
        assertThrows(ApiException.class, () -> productApi.getCheckByBarcodes(List.of()));
        verifyNoInteractions(productDao);
    }

    @Test
    void getCheckByBarcodes_shouldReturnFound_whenAllExist() throws ApiException {
        List<String> req = List.of("A", "B");

        Product p1 = new Product(); p1.setBarcode("A");
        Product p2 = new Product(); p2.setBarcode("B");
        List<Product> found = List.of(p1, p2);

        when(productDao.selectByBarcodes(req)).thenReturn(found);

        List<Product> out = productApi.getCheckByBarcodes(req);

        assertEquals(2, out.size());
        assertSame(found, out);
        verify(productDao).selectByBarcodes(req);
    }

    @Test
    void getCheckByBarcodes_shouldThrow_whenSomeMissing() {
        // includes trim + distinct behavior
        List<String> req = new ArrayList<>(Arrays.asList(" A ", "B", "B", null, "  "));

        Product onlyA = new Product();
        onlyA.setBarcode("A");

        when(productDao.selectByBarcodes(req)).thenReturn(List.of(onlyA));

        ApiException ex = assertThrows(ApiException.class, () -> productApi.getCheckByBarcodes(req));

        assertTrue(ex.getMessage().contains("B"));
        verify(productDao).selectByBarcodes(req);
    }


    // -------------------- getByBarcode / getCheckByBarcode --------------------

    @Test
    void getByBarcode_shouldReturnNull_whenDaoReturnsEmpty() {
        when(productDao.selectByBarcodes(List.of("X"))).thenReturn(List.of());

        Product out = productApi.getByBarcode("X");

        assertNull(out);
        verify(productDao).selectByBarcodes(List.of("X"));
    }

    @Test
    void getByBarcode_shouldReturnFirst_whenDaoReturnsList() {
        Product p = new Product();
        p.setBarcode("X");

        when(productDao.selectByBarcodes(List.of("X"))).thenReturn(List.of(p));

        Product out = productApi.getByBarcode("X");

        assertSame(p, out);
    }

    @Test
    void getCheckByBarcode_shouldThrow_whenNotFound() {
        when(productDao.selectByBarcodes(List.of("NOPE"))).thenReturn(List.of());

        assertThrows(ApiException.class, () -> productApi.getCheckByBarcode("NOPE"));
    }

    @Test
    void getCheckByBarcode_shouldReturn_whenFound() throws ApiException {
        Product p = new Product();
        p.setBarcode("OK");

        when(productDao.selectByBarcodes(List.of("OK"))).thenReturn(List.of(p));

        Product out = productApi.getCheckByBarcode("OK");

        assertSame(p, out);
    }

    // -------------------- findProductIdsByBarcodeOrName --------------------

    @Test
    void findProductIdsByBarcodeOrName_shouldDelegateToDao() {
        List<Integer> ids = List.of(10, 11);
        when(productDao.findProductIdsByBarcodeOrName("b", "n")).thenReturn(ids);

        List<Integer> out = productApi.findProductIdsByBarcodeOrName("b", "n");

        assertSame(ids, out);
        verify(productDao).findProductIdsByBarcodeOrName("b", "n");
    }

    // -------------------- add --------------------

    @Test
    void add_shouldInsert_whenBarcodeNotExists() throws ApiException {
        Product p = new Product();
        p.setBarcode("NEW");

        when(productDao.selectByBarcodes(List.of("NEW"))).thenReturn(List.of()); // getByBarcode uses this

        productApi.add(p);

        verify(productDao).insert(p);
    }

    @Test
    void add_shouldThrow_whenBarcodeAlreadyExists() {
        Product p = new Product();
        p.setBarcode("DUP");

        Product existing = new Product();
        existing.setBarcode("DUP");

        when(productDao.selectByBarcodes(List.of("DUP"))).thenReturn(List.of(existing));

        assertThrows(ApiException.class, () -> productApi.add(p));
        verify(productDao, never()).insert(any());
    }

    // -------------------- addBulk --------------------

    @Test
    void addBulk_shouldDoNothing_whenEmpty() throws ApiException {
        productApi.addBulk(null);
        productApi.addBulk(List.of());
        verifyNoInteractions(productDao);
    }

    @Test
    void addBulk_shouldInsertAll_whenNoExistingBarcodes() throws ApiException {
        Product p1 = new Product(); p1.setBarcode("A");
        Product p2 = new Product(); p2.setBarcode("B");
        List<Product> products = List.of(p1, p2);

        when(productDao.selectByBarcodes(List.of("A", "B"))).thenReturn(List.of());

        productApi.addBulk(products);

        verify(productDao).selectByBarcodes(List.of("A", "B"));
        verify(productDao).insert(p1);
        verify(productDao).insert(p2);
    }

    @Test
    void addBulk_shouldThrow_whenSomeBarcodesExist() {
        Product p1 = new Product(); p1.setBarcode("A");
        Product p2 = new Product(); p2.setBarcode("B");
        List<Product> products = List.of(p1, p2);

        Product existing = new Product(); existing.setBarcode("B");

        when(productDao.selectByBarcodes(List.of("A", "B"))).thenReturn(List.of(existing));

        ApiException ex = assertThrows(ApiException.class, () -> productApi.addBulk(products));

        // should include existing barcode(s) in message
        assertTrue(ex.getMessage().contains("B"));
        verify(productDao, never()).insert(any());
    }

    // -------------------- update --------------------

    @Test
    void update_shouldUpdateMutableFields_whenBarcodeAndClientUnchanged() throws ApiException {
        Product existing = new Product();
        existing.setId(1);
        existing.setBarcode("BC");
        existing.setClientId(100);
        existing.setName("Old");
        existing.setMrp(10.0);
        existing.setImageUrl("old.png");

        when(productDao.selectById(1)).thenReturn(existing);

        Product incoming = new Product();
        incoming.setBarcode("BC");      // same
        incoming.setClientId(100);      // same
        incoming.setName("New");
        incoming.setMrp(99.0);
        incoming.setImageUrl("new.png");

        productApi.update(1, incoming);

        assertEquals("New", existing.getName());
        assertEquals(99.0, existing.getMrp());
        assertEquals("new.png", existing.getImageUrl());
        assertEquals("BC", existing.getBarcode());
        assertEquals(100, existing.getClientId());
    }

    @Test
    void update_shouldThrow_whenBarcodeModified() {
        Product existing = new Product();
        existing.setId(1);
        existing.setBarcode("OLD");
        existing.setClientId(100);

        when(productDao.selectById(1)).thenReturn(existing);

        Product incoming = new Product();
        incoming.setBarcode("NEW"); // changed
        incoming.setClientId(100);

        ApiException ex = assertThrows(ApiException.class, () -> productApi.update(1, incoming));
        assertTrue(ex.getMessage().toLowerCase().contains("barcode"));
    }

    @Test
    void update_shouldThrow_whenClientModified() {
        Product existing = new Product();
        existing.setId(1);
        existing.setBarcode("BC");
        existing.setClientId(100);

        when(productDao.selectById(1)).thenReturn(existing);

        Product incoming = new Product();
        incoming.setBarcode("BC");
        incoming.setClientId(999); // changed

        ApiException ex = assertThrows(ApiException.class, () -> productApi.update(1, incoming));
        assertTrue(ex.getMessage().toLowerCase().contains("client"));
    }

    @Test
    void update_shouldAllowNullBarcodeAndNullClientId_inIncoming() throws ApiException {
        Product existing = new Product();
        existing.setId(1);
        existing.setBarcode("BC");
        existing.setClientId(100);

        when(productDao.selectById(1)).thenReturn(existing);

        Product incoming = new Product();
        incoming.setBarcode(null);     // allowed
        incoming.setClientId(null);    // allowed by checks, but note: existing.setClientId(null) happens later!
        incoming.setName("X");
        incoming.setMrp(50.0);
        incoming.setImageUrl("img");

        productApi.update(1, incoming);

        assertEquals("X", existing.getName());
        assertEquals(50.0, existing.getMrp());
        assertEquals("img", existing.getImageUrl());
        assertNull(existing.getClientId());
        assertEquals("BC", existing.getBarcode()); // barcode not overwritten
    }

    // -------------------- validateSellingPrice --------------------

    @Test
    void validateSellingPrice_shouldPass_whenSellingPriceLessOrEqualMrp() throws ApiException {
        Product p = new Product();
        p.setId(1);
        p.setMrp(100.0);

        when(productDao.selectById(1)).thenReturn(p);

        assertDoesNotThrow(() -> productApi.validateSellingPrice(1, 100.0));
        assertDoesNotThrow(() -> productApi.validateSellingPrice(1, 99.99));
    }

    @Test
    void validateSellingPrice_shouldThrow_whenSellingPriceExceedsMrp() {
        Product p = new Product();
        p.setId(1);
        p.setMrp(100.0);

        when(productDao.selectById(1)).thenReturn(p);

        ApiException ex = assertThrows(ApiException.class, () -> productApi.validateSellingPrice(1, 100.01));
        assertTrue(ex.getMessage().contains("productId=1"));
        assertTrue(ex.getMessage().contains("mrp=100.0"));
        assertTrue(ex.getMessage().contains("sellingPrice=100.01"));
    }

    // -------------------- search / getCount --------------------

    @Test
    void search_shouldDelegateToDao() {
        List<Product> out = List.of(new Product());
        when(productDao.search("n", "b", 1, 0, 10)).thenReturn(out);

        List<Product> res = productApi.search("n", "b", 1, 0, 10);

        assertSame(out, res);
        verify(productDao).search("n", "b", 1, 0, 10);
    }

    @Test
    void getCount_shouldDelegateToDao() {
        when(productDao.getCount("n", "b", 1)).thenReturn(123L);

        long cnt = productApi.getCount("n", "b", 1);

        assertEquals(123L, cnt);
        verify(productDao).getCount("n", "b", 1);
    }

    // -------------------- Static helper tests --------------------

    @Test
    void extractBarcodes_shouldHandleNullAndEmpty() {
        assertEquals(List.of(), ProductApi.extractBarcodes(null));
        assertEquals(List.of(), ProductApi.extractBarcodes(List.of()));
    }

    @Test
    void extractBarcodes_shouldExtractInOrder() {
        Product a = new Product(); a.setBarcode("A");
        Product b = new Product(); b.setBarcode("B");
        assertEquals(List.of("A", "B"), ProductApi.extractBarcodes(List.of(a, b)));
    }

    @Test
    void toBarcodeSet_shouldHandleNullAndEmpty() {
        assertEquals(Set.of(), ProductApi.toBarcodeSet(null));
        assertEquals(Set.of(), ProductApi.toBarcodeSet(List.of()));
    }

    @Test
    void toBarcodeSet_shouldCollectDistinct() {
        Product a1 = new Product(); a1.setBarcode("A");
        Product a2 = new Product(); a2.setBarcode("A");
        Product b = new Product(); b.setBarcode("B");

        Set<String> set = ProductApi.toBarcodeSet(List.of(a1, a2, b));
        assertEquals(Set.of("A", "B"), set);
    }

    @Test
    void findMissingBarcodes_shouldHandleNullRequested() {
        assertEquals(List.of(), ProductApi.findMissingBarcodes(null, Set.of("A")));
        assertEquals(List.of(), ProductApi.findMissingBarcodes(List.of(), Set.of("A")));
    }

    @Test
    void findMissingBarcodes_shouldTrimIgnoreNullEmptyDistinct_andCompare() {
        List<String> requested = new ArrayList<>(Arrays.asList(" A ", "B", "B", null, "   ", "C"));
        Set<String> found = Set.of("A", "C");

        List<String> missing = ProductApi.findMissingBarcodes(requested, found);

        assertEquals(List.of("B"), missing);
    }


    @Test
    void findMissingBarcodes_shouldTreatNullFoundSetAsEmpty() {
        List<String> requested = List.of("A");
        List<String> missing = ProductApi.findMissingBarcodes(requested, null);
        assertEquals(List.of("A"), missing);
    }

    @Test
    void toProductIdByBarcode_shouldReturnEmpty_whenNullOrEmpty() {
        assertEquals(Map.of(), ProductApi.toProductIdByBarcode(null));
        assertEquals(Map.of(), ProductApi.toProductIdByBarcode(List.of()));
    }

    @Test
    void toProductIdByBarcode_shouldMapBarcodeToId_andIgnoreNulls_andResolveDuplicates() {
        Product p1 = new Product(); p1.setBarcode("A"); p1.setId(1);
        Product p2 = new Product(); p2.setBarcode("B"); p2.setId(2);
        Product p3 = new Product(); p3.setBarcode(null); p3.setId(3);  // ignored
        Product p4 = new Product(); p4.setBarcode("C"); p4.setId(null); // ignored
        Product p5 = new Product(); p5.setBarcode("A"); p5.setId(999); // duplicate barcode, keeps first by merge (a,b)->a

        Map<String, Integer> map = ProductApi.toProductIdByBarcode(List.of(p1, p2, p3, p4, p5));

        assertEquals(2, map.size());
        assertEquals(1, map.get("A"));   // kept first
        assertEquals(2, map.get("B"));
    }
}
