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
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductApiTest {

    @InjectMocks
    private ProductApi productApi;

    @Mock
    private ProductDao productDao;

    @Test
    void getShouldCallDaoSelectById() {
        Product p = new Product();
        p.setId(1);
        when(productDao.selectById(1)).thenReturn(p);

        Product out = productApi.get(1);

        assertSame(p, out);
        verify(productDao).selectById(1);
    }

    @Test
    void getCheckShouldReturnProductWhenExists() throws ApiException {
        Product p = new Product();
        p.setId(7);
        when(productDao.selectById(7)).thenReturn(p);

        Product out = productApi.getCheck(7);

        assertSame(p, out);
    }

    @Test
    void getCheckShouldThrowWhenNotFound() {
        when(productDao.selectById(99)).thenReturn(null);
        assertThrows(ApiException.class, () -> productApi.getCheck(99));
    }

    @Test
    void getByIdsShouldReturnEmptyWhenNullOrEmpty() {
        assertEquals(List.of(), productApi.getByIds(null));
        assertEquals(List.of(), productApi.getByIds(List.of()));
        verifyNoInteractions(productDao);
    }

    @Test
    void getByIdsShouldCallDaoWhenNonEmpty() {
        List<Integer> ids = List.of(1, 2, 3);
        List<Product> found = List.of(new Product(), new Product());
        when(productDao.selectByIds(ids)).thenReturn(found);

        List<Product> out = productApi.getByIds(ids);

        assertSame(found, out);
        verify(productDao).selectByIds(ids);
    }

    @Test
    void getByBarcodesShouldReturnEmptyWhenNullOrEmpty() {
        assertEquals(List.of(), productApi.getByBarcodes(null));
        assertEquals(List.of(), productApi.getByBarcodes(List.of()));
        verifyNoInteractions(productDao);
    }

    @Test
    void getByBarcodesShouldCallDaoWhenNonEmpty() {
        List<String> barcodes = List.of("A", "B");
        List<Product> found = List.of(new Product());
        when(productDao.selectByBarcodes(barcodes)).thenReturn(found);

        List<Product> out = productApi.getByBarcodes(barcodes);

        assertSame(found, out);
        verify(productDao).selectByBarcodes(barcodes);
    }

    @Test
    void getCheckByBarcodesShouldThrowWhenEmptyInput() {
        assertThrows(ApiException.class, () -> productApi.getCheckByBarcodes(null));
        assertThrows(ApiException.class, () -> productApi.getCheckByBarcodes(List.of()));
        verifyNoInteractions(productDao);
    }

    @Test
    void getCheckByBarcodesShouldReturnFoundWhenAllExist() throws ApiException {
        List<String> req = List.of("A", "B");

        Product p1 = new Product();
        p1.setBarcode("A");
        Product p2 = new Product();
        p2.setBarcode("B");
        List<Product> found = List.of(p1, p2);

        when(productDao.selectByBarcodes(req)).thenReturn(found);

        List<Product> out = productApi.getCheckByBarcodes(req);

        assertEquals(2, out.size());
        assertSame(found, out);
        verify(productDao).selectByBarcodes(req);
    }

    @Test
    void getCheckByBarcodesShouldThrowWhenSomeMissing() {
        List<String> req = new ArrayList<>(Arrays.asList(" A ", "B", "B", null, "  "));

        Product onlyA = new Product();
        onlyA.setBarcode("A");

        when(productDao.selectByBarcodes(anyList())).thenReturn(List.of(onlyA));

        ApiException ex = assertThrows(ApiException.class, () -> productApi.getCheckByBarcodes(req));
        assertTrue(ex.getMessage().contains("B"));
        verify(productDao).selectByBarcodes(anyList());
    }

    @Test
    void getByBarcodeShouldReturnNullWhenDaoReturnsEmpty() {
        when(productDao.selectByBarcodes(List.of("X"))).thenReturn(List.of());

        Product out = productApi.getByBarcode("X");

        assertNull(out);
        verify(productDao).selectByBarcodes(List.of("X"));
    }

    @Test
    void getByBarcodeShouldReturnFirstWhenDaoReturnsList() {
        Product p = new Product();
        p.setBarcode("X");
        when(productDao.selectByBarcodes(List.of("X"))).thenReturn(List.of(p));

        Product out = productApi.getByBarcode("X");

        assertSame(p, out);
    }

    @Test
    void getCheckByBarcodeShouldThrowWhenNotFound() {
        when(productDao.selectByBarcodes(List.of("NOPE"))).thenReturn(List.of());
        assertThrows(ApiException.class, () -> productApi.getCheckByBarcode("NOPE"));
    }

    @Test
    void getCheckByBarcodeShouldReturnWhenFound() throws ApiException {
        Product p = new Product();
        p.setBarcode("OK");
        when(productDao.selectByBarcodes(List.of("OK"))).thenReturn(List.of(p));

        Product out = productApi.getCheckByBarcode("OK");

        assertSame(p, out);
    }

    @Test
    void findProductIdsByBarcodeOrNameShouldDelegateToDao() {
        List<Integer> ids = List.of(10, 11);
        when(productDao.findProductIdsByBarcodeOrName("b", "n")).thenReturn(ids);

        List<Integer> out = productApi.findProductIdsByBarcodeOrName("b", "n");

        assertSame(ids, out);
        verify(productDao).findProductIdsByBarcodeOrName("b", "n");
    }

    @Test
    void addShouldInsertWhenBarcodeNotExists() throws ApiException {
        Product p = new Product();
        p.setBarcode("NEW");
        when(productDao.selectByBarcodes(List.of("NEW"))).thenReturn(List.of());

        productApi.add(p);

        verify(productDao).insert(p);
    }

    @Test
    void addShouldThrowWhenBarcodeAlreadyExists() {
        Product p = new Product();
        p.setBarcode("DUP");

        Product existing = new Product();
        existing.setBarcode("DUP");

        when(productDao.selectByBarcodes(List.of("DUP"))).thenReturn(List.of(existing));

        assertThrows(ApiException.class, () -> productApi.add(p));
        verify(productDao, never()).insert(any());
    }

    @Test
    void addBulkShouldDoNothingWhenEmpty() throws ApiException {
        productApi.addBulk(null);
        productApi.addBulk(List.of());
        verifyNoInteractions(productDao);
    }

    @Test
    void addBulkShouldInsertAllWhenNoExistingBarcodes() throws ApiException {
        Product p1 = new Product();
        p1.setBarcode("A");
        Product p2 = new Product();
        p2.setBarcode("B");
        List<Product> products = List.of(p1, p2);

        when(productDao.selectByBarcodes(List.of("A", "B"))).thenReturn(List.of());

        productApi.addBulk(products);

        verify(productDao).selectByBarcodes(List.of("A", "B"));
        verify(productDao).insert(p1);
        verify(productDao).insert(p2);
    }

    @Test
    void addBulkShouldThrowWhenSomeBarcodesExist() {
        Product p1 = new Product();
        p1.setBarcode("A");
        Product p2 = new Product();
        p2.setBarcode("B");
        List<Product> products = List.of(p1, p2);

        Product existing = new Product();
        existing.setBarcode("B");

        when(productDao.selectByBarcodes(List.of("A", "B"))).thenReturn(List.of(existing));

        ApiException ex = assertThrows(ApiException.class, () -> productApi.addBulk(products));
        assertTrue(ex.getMessage().contains("B"));
        verify(productDao, never()).insert(any());
    }

    @Test
    void updateShouldUpdateMutableFieldsWhenBarcodeAndClientUnchanged() throws ApiException {
        Product existing = new Product();
        existing.setId(1);
        existing.setBarcode("BC");
        existing.setClientId(100);
        existing.setName("Old");
        existing.setMrp(10.0);
        existing.setImageUrl("old.png");

        when(productDao.selectById(1)).thenReturn(existing);

        Product incoming = new Product();
        incoming.setBarcode("BC");
        incoming.setClientId(100);
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
    void updateShouldThrowWhenBarcodeModified() {
        Product existing = new Product();
        existing.setId(1);
        existing.setBarcode("OLD");
        existing.setClientId(100);
        when(productDao.selectById(1)).thenReturn(existing);

        Product incoming = new Product();
        incoming.setBarcode("NEW");
        incoming.setClientId(100);

        ApiException ex = assertThrows(ApiException.class, () -> productApi.update(1, incoming));
        assertTrue(ex.getMessage().toLowerCase().contains("barcode"));
    }

    @Test
    void updateShouldThrowWhenClientModified() {
        Product existing = new Product();
        existing.setId(1);
        existing.setBarcode("BC");
        existing.setClientId(100);
        when(productDao.selectById(1)).thenReturn(existing);

        Product incoming = new Product();
        incoming.setBarcode("BC");
        incoming.setClientId(999);

        ApiException ex = assertThrows(ApiException.class, () -> productApi.update(1, incoming));
        assertTrue(ex.getMessage().toLowerCase().contains("client"));
    }

    @Test
    void updateShouldAllowNullBarcodeAndNullClientIdInIncoming() throws ApiException {
        Product existing = new Product();
        existing.setId(1);
        existing.setBarcode("BC");
        existing.setClientId(100);

        when(productDao.selectById(1)).thenReturn(existing);

        Product incoming = new Product();
        incoming.setBarcode(null);     // allowed (should not overwrite)
        incoming.setClientId(null);    // allowed (should not overwrite)
        incoming.setName("X");
        incoming.setMrp(50.0);
        incoming.setImageUrl("img");

        productApi.update(1, incoming);

        assertEquals("X", existing.getName());
        assertEquals(50.0, existing.getMrp());
        assertEquals("img", existing.getImageUrl());

        assertEquals("BC", existing.getBarcode());
        assertEquals(100, existing.getClientId());
    }

    @Test
    void validateSellingPriceShouldPassWhenSellingPriceLessOrEqualMrp() throws ApiException {
        Product p = new Product();
        p.setId(1);
        p.setMrp(100.0);
        when(productDao.selectById(1)).thenReturn(p);

        assertDoesNotThrow(() -> productApi.validateSellingPrice(1, 100.0));
        assertDoesNotThrow(() -> productApi.validateSellingPrice(1, 99.99));
    }

    @Test
    void validateSellingPriceShouldThrowWhenSellingPriceExceedsMrp() {
        Product p = new Product();
        p.setId(1);
        p.setMrp(100.0);
        when(productDao.selectById(1)).thenReturn(p);

        ApiException ex = assertThrows(ApiException.class, () -> productApi.validateSellingPrice(1, 100.01));
        assertTrue(ex.getMessage().contains("productId=1"));
        assertTrue(ex.getMessage().contains("mrp=100.0"));
        assertTrue(ex.getMessage().contains("sellingPrice=100.01"));
    }

    @Test
    void searchShouldDelegateToDao() {
        List<Product> out = List.of(new Product());
        when(productDao.search("n", "b", 1, 0, 10)).thenReturn(out);

        List<Product> res = productApi.search("n", "b", 1, 0, 10);

        assertSame(out, res);
        verify(productDao).search("n", "b", 1, 0, 10);
    }

    @Test
    void getCountShouldDelegateToDao() {
        when(productDao.getCount("n", "b", 1)).thenReturn(123L);

        long cnt = productApi.getCount("n", "b", 1);

        assertEquals(123L, cnt);
        verify(productDao).getCount("n", "b", 1);
    }

    @Test
    void extractBarcodesShouldHandleNullAndEmpty() {
        assertEquals(List.of(), ProductApi.extractBarcodes(null));
        assertEquals(List.of(), ProductApi.extractBarcodes(List.of()));
    }

    @Test
    void extractBarcodesShouldExtractInOrder() {
        Product a = new Product();
        a.setBarcode("A");
        Product b = new Product();
        b.setBarcode("B");
        assertEquals(List.of("A", "B"), ProductApi.extractBarcodes(List.of(a, b)));
    }

    @Test
    void toBarcodeSetShouldHandleNullAndEmpty() {
        assertEquals(Set.of(), ProductApi.toBarcodeSet(null));
        assertEquals(Set.of(), ProductApi.toBarcodeSet(List.of()));
    }

    @Test
    void toBarcodeSetShouldCollectDistinct() {
        Product a1 = new Product();
        a1.setBarcode("A");
        Product a2 = new Product();
        a2.setBarcode("A");
        Product b = new Product();
        b.setBarcode("B");

        Set<String> set = ProductApi.toBarcodeSet(List.of(a1, a2, b));
        assertEquals(Set.of("A", "B"), set);
    }

    @Test
    void findMissingBarcodesShouldHandleNullRequested() {
        assertEquals(List.of(), ProductApi.findMissingBarcodes(null, Set.of("A")));
        assertEquals(List.of(), ProductApi.findMissingBarcodes(List.of(), Set.of("A")));
    }

    @Test
    void findMissingBarcodesShouldTrimIgnoreNullEmptyDistinctAndCompare() {
        List<String> requested = new ArrayList<>(Arrays.asList(" A ", "B", "B", null, "   ", "C"));
        Set<String> found = Set.of("A", "C");

        List<String> missing = ProductApi.findMissingBarcodes(requested, found);

        assertEquals(List.of("B"), missing);
    }

    @Test
    void findMissingBarcodesShouldTreatNullFoundSetAsEmpty() {
        List<String> requested = List.of("A");
        List<String> missing = ProductApi.findMissingBarcodes(requested, null);
        assertEquals(List.of("A"), missing);
    }

    @Test
    void toProductIdByBarcodeShouldReturnEmptyWhenNullOrEmpty() {
        assertEquals(Map.of(), ProductApi.toProductIdByBarcode(null));
        assertEquals(Map.of(), ProductApi.toProductIdByBarcode(List.of()));
    }

    @Test
    void toProductIdByBarcodeShouldMapBarcodeToIdAndIgnoreNullsAndResolveDuplicates() {
        Product p1 = new Product();
        p1.setBarcode("A");
        p1.setId(1);

        Product p2 = new Product();
        p2.setBarcode("B");
        p2.setId(2);

        Product p3 = new Product();
        p3.setBarcode(null);
        p3.setId(3);

        Product p4 = new Product();
        p4.setBarcode("C");
        p4.setId(null);

        Product p5 = new Product();
        p5.setBarcode("A");
        p5.setId(999);

        Map<String, Integer> map = ProductApi.toProductIdByBarcode(List.of(p1, p2, p3, p4, p5));

        assertEquals(2, map.size());
        assertEquals(1, map.get("A"));
        assertEquals(2, map.get("B"));
    }
}