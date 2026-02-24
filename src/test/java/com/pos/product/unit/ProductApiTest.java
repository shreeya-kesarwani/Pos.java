package com.pos.product.unit;

import com.pos.api.ProductApi;
import com.pos.dao.ProductDao;
import com.pos.exception.ApiException;
import com.pos.pojo.Product;
import com.pos.setup.UnitTestFactory;
import org.junit.jupiter.api.BeforeEach;
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

    private Product existing;
    private Integer productId;

    @BeforeEach
    void setupData() {
        productId = 1;
        existing = UnitTestFactory.product(productId, "BC", 100, "Old", 10.0, "old.png");
    }

    @Test
    void getShouldCallDaoSelectById() {
        when(productDao.selectById(productId)).thenReturn(existing);

        Product out = productApi.get(productId);

        assertSame(existing, out);
        verify(productDao).selectById(productId);
        verifyNoMoreInteractions(productDao);
    }

    @Test
    void getCheckShouldReturnProductWhenExists() throws ApiException {
        when(productDao.selectById(7)).thenReturn(existing);

        Product out = productApi.getCheck(7);

        assertSame(existing, out);
        verify(productDao).selectById(7);
        verifyNoMoreInteractions(productDao);
    }

    @Test
    void getCheckShouldThrowWhenNotFound() {
        when(productDao.selectById(99)).thenReturn(null);

        assertThrows(ApiException.class, () -> productApi.getCheck(99));

        verify(productDao).selectById(99);
        verifyNoMoreInteractions(productDao);
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
        List<Product> found = List.of(UnitTestFactory.product(1, "A", 1, null, null, null),
                UnitTestFactory.product(2, "B", 1, null, null, null));
        when(productDao.selectByIds(ids)).thenReturn(found);

        List<Product> out = productApi.getByIds(ids);

        assertSame(found, out);
        verify(productDao).selectByIds(ids);
        verifyNoMoreInteractions(productDao);
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
        List<Product> found = List.of(UnitTestFactory.productWithBarcode("A"));
        when(productDao.selectByBarcodes(barcodes)).thenReturn(found);

        List<Product> out = productApi.getByBarcodes(barcodes);

        assertSame(found, out);
        verify(productDao).selectByBarcodes(barcodes);
        verifyNoMoreInteractions(productDao);
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

        Product p1 = UnitTestFactory.productWithBarcode("A");
        Product p2 = UnitTestFactory.productWithBarcode("B");
        List<Product> found = List.of(p1, p2);

        when(productDao.selectByBarcodes(req)).thenReturn(found);

        List<Product> out = productApi.getCheckByBarcodes(req);

        assertSame(found, out);
        assertEquals(2, out.size());
        verify(productDao).selectByBarcodes(req);
        verifyNoMoreInteractions(productDao);
    }

    @Test
    void getCheckByBarcodesShouldThrowWhenSomeMissing() {
        List<String> req = new ArrayList<>(Arrays.asList(" A ", "B", "B", null, "  "));

        Product onlyA = UnitTestFactory.productWithBarcode("A");
        when(productDao.selectByBarcodes(anyList())).thenReturn(List.of(onlyA));

        ApiException ex = assertThrows(ApiException.class, () -> productApi.getCheckByBarcodes(req));
        assertTrue(ex.getMessage().contains("B"));

        verify(productDao).selectByBarcodes(anyList());
        verifyNoMoreInteractions(productDao);
    }

    @Test
    void getByBarcodeShouldReturnNullWhenDaoReturnsEmpty() {
        when(productDao.selectByBarcodes(List.of("X"))).thenReturn(List.of());

        Product out = productApi.getByBarcode("X");

        assertNull(out);
        verify(productDao).selectByBarcodes(List.of("X"));
        verifyNoMoreInteractions(productDao);
    }

    @Test
    void getByBarcodeShouldReturnFirstWhenDaoReturnsList() {
        Product p = UnitTestFactory.productWithBarcode("X");
        when(productDao.selectByBarcodes(List.of("X"))).thenReturn(List.of(p));

        Product out = productApi.getByBarcode("X");

        assertSame(p, out);
        verify(productDao).selectByBarcodes(List.of("X"));
        verifyNoMoreInteractions(productDao);
    }

    @Test
    void getCheckByBarcodeShouldThrowWhenNotFound() {
        when(productDao.selectByBarcodes(List.of("NOPE"))).thenReturn(List.of());

        assertThrows(ApiException.class, () -> productApi.getCheckByBarcode("NOPE"));

        verify(productDao).selectByBarcodes(List.of("NOPE"));
        verifyNoMoreInteractions(productDao);
    }

    @Test
    void getCheckByBarcodeShouldReturnWhenFound() throws ApiException {
        Product p = UnitTestFactory.productWithBarcode("OK");
        when(productDao.selectByBarcodes(List.of("OK"))).thenReturn(List.of(p));

        Product out = productApi.getCheckByBarcode("OK");

        assertSame(p, out);
        verify(productDao).selectByBarcodes(List.of("OK"));
        verifyNoMoreInteractions(productDao);
    }

    @Test
    void findProductIdsByBarcodeOrNameShouldDelegateToDao() {
        List<Integer> ids = List.of(10, 11);
        when(productDao.findProductIdsByBarcodeOrName("b", "n")).thenReturn(ids);

        List<Integer> out = productApi.findProductIdsByBarcodeOrName("b", "n");

        assertSame(ids, out);
        verify(productDao).findProductIdsByBarcodeOrName("b", "n");
        verifyNoMoreInteractions(productDao);
    }

    @Test
    void addShouldInsertWhenBarcodeNotExists() throws ApiException {
        Product p = UnitTestFactory.productWithBarcode("NEW");
        when(productDao.selectByBarcodes(List.of("NEW"))).thenReturn(List.of());

        productApi.add(p);

        verify(productDao).selectByBarcodes(List.of("NEW"));
        verify(productDao).insert(p);
        verifyNoMoreInteractions(productDao);
    }

    @Test
    void addShouldThrowWhenBarcodeAlreadyExists() {
        Product p = UnitTestFactory.productWithBarcode("DUP");
        Product existingDup = UnitTestFactory.productWithBarcode("DUP");

        when(productDao.selectByBarcodes(List.of("DUP"))).thenReturn(List.of(existingDup));

        assertThrows(ApiException.class, () -> productApi.add(p));

        verify(productDao).selectByBarcodes(List.of("DUP"));
        verify(productDao, never()).insert(any());
        verifyNoMoreInteractions(productDao);
    }

    @Test
    void addBulkShouldDoNothingWhenEmpty() throws ApiException {
        productApi.addBulk(null);
        productApi.addBulk(List.of());
        verifyNoInteractions(productDao);
    }

    @Test
    void addBulkShouldInsertAllWhenNoExistingBarcodes() throws ApiException {
        Product p1 = UnitTestFactory.productWithBarcode("A");
        Product p2 = UnitTestFactory.productWithBarcode("B");
        List<Product> products = List.of(p1, p2);

        when(productDao.selectByBarcodes(List.of("A", "B"))).thenReturn(List.of());

        productApi.addBulk(products);

        verify(productDao).selectByBarcodes(List.of("A", "B"));
        verify(productDao).insert(p1);
        verify(productDao).insert(p2);
        verifyNoMoreInteractions(productDao);
    }

    @Test
    void addBulkShouldThrowWhenSomeBarcodesExist() {
        Product p1 = UnitTestFactory.productWithBarcode("A");
        Product p2 = UnitTestFactory.productWithBarcode("B");
        List<Product> products = List.of(p1, p2);

        Product existingB = UnitTestFactory.productWithBarcode("B");
        when(productDao.selectByBarcodes(List.of("A", "B"))).thenReturn(List.of(existingB));

        ApiException ex = assertThrows(ApiException.class, () -> productApi.addBulk(products));
        assertTrue(ex.getMessage().contains("B"));

        verify(productDao).selectByBarcodes(List.of("A", "B"));
        verify(productDao, never()).insert(any());
        verifyNoMoreInteractions(productDao);
    }

    @Test
    void updateShouldUpdateMutableFieldsWhenBarcodeAndClientUnchanged() throws ApiException {
        when(productDao.selectById(1)).thenReturn(existing);

        Product incoming = UnitTestFactory.product(null, "BC", 100, "New", 99.0, "new.png");

        productApi.update(1, incoming);

        assertEquals("New", existing.getName());
        assertEquals(99.0, existing.getMrp());
        assertEquals("new.png", existing.getImageUrl());
        assertEquals("BC", existing.getBarcode());
        assertEquals(100, existing.getClientId());

        verify(productDao).selectById(1);
        verifyNoMoreInteractions(productDao);
    }

    @Test
    void updateShouldThrowWhenBarcodeModified() {
        Product ex = UnitTestFactory.product(1, "OLD", 100, null, null, null);
        when(productDao.selectById(1)).thenReturn(ex);

        Product incoming = UnitTestFactory.product(null, "NEW", 100, null, null, null);

        ApiException thrown = assertThrows(ApiException.class, () -> productApi.update(1, incoming));
        assertTrue(thrown.getMessage().toLowerCase().contains("barcode"));

        verify(productDao).selectById(1);
        verifyNoMoreInteractions(productDao);
    }

    @Test
    void updateShouldThrowWhenClientModified() {
        Product ex = UnitTestFactory.product(1, "BC", 100, null, null, null);
        when(productDao.selectById(1)).thenReturn(ex);

        Product incoming = UnitTestFactory.product(null, "BC", 999, null, null, null);

        ApiException thrown = assertThrows(ApiException.class, () -> productApi.update(1, incoming));
        assertTrue(thrown.getMessage().toLowerCase().contains("client"));

        verify(productDao).selectById(1);
        verifyNoMoreInteractions(productDao);
    }

    @Test
    void updateShouldAllowNullBarcodeAndNullClientIdInIncoming() throws ApiException {
        Product ex = UnitTestFactory.product(1, "BC", 100, "Old", 10.0, "old.png");
        when(productDao.selectById(1)).thenReturn(ex);

        Product incoming = UnitTestFactory.product(null, null, null, "X", 50.0, "img");

        productApi.update(1, incoming);

        assertEquals("X", ex.getName());
        assertEquals(50.0, ex.getMrp());
        assertEquals("img", ex.getImageUrl());
        assertEquals("BC", ex.getBarcode());
        assertEquals(100, ex.getClientId());

        verify(productDao).selectById(1);
        verifyNoMoreInteractions(productDao);
    }

    @Test
    void validateSellingPriceShouldThrowWhenSellingPriceExceedsMrp() {
        Product p = UnitTestFactory.product(1, null, null, null, 100.0, null);
        when(productDao.selectById(1)).thenReturn(p);

        ApiException ex = assertThrows(ApiException.class, () -> productApi.validateSellingPrice(1, 100.01));
        assertTrue(ex.getMessage().contains("productId=1"));
        assertTrue(ex.getMessage().contains("mrp=100.0"));
        assertTrue(ex.getMessage().contains("sellingPrice=100.01"));

        verify(productDao).selectById(1);
        verifyNoMoreInteractions(productDao);
    }

    @Test
    void searchShouldDelegateToDao() {
        List<Product> out = List.of(UnitTestFactory.productWithBarcode("A"));
        when(productDao.search("n", "b", 1, 0, 10)).thenReturn(out);

        List<Product> res = productApi.search("n", "b", 1, 0, 10);

        assertSame(out, res);
        verify(productDao).search("n", "b", 1, 0, 10);
        verifyNoMoreInteractions(productDao);
    }

    @Test
    void getCountShouldDelegateToDao() {
        when(productDao.getCount("n", "b", 1)).thenReturn(123L);

        long cnt = productApi.getCount("n", "b", 1);

        assertEquals(123L, cnt);
        verify(productDao).getCount("n", "b", 1);
        verifyNoMoreInteractions(productDao);
    }

    // -------- Static helpers in ProductApi (no mocks needed) --------

    @Test
    void extractBarcodesShouldHandleNullAndEmpty() {
        assertEquals(List.of(), ProductApi.extractBarcodes(null));
        assertEquals(List.of(), ProductApi.extractBarcodes(List.of()));
    }

    @Test
    void extractBarcodesShouldExtractInOrder() {
        Product a = UnitTestFactory.productWithBarcode("A");
        Product b = UnitTestFactory.productWithBarcode("B");

        assertEquals(List.of("A", "B"), ProductApi.extractBarcodes(List.of(a, b)));
    }

    @Test
    void toBarcodeSetShouldHandleNullAndEmpty() {
        assertEquals(Set.of(), ProductApi.toBarcodeSet(null));
        assertEquals(Set.of(), ProductApi.toBarcodeSet(List.of()));
    }

    @Test
    void toBarcodeSetShouldCollectDistinct() {
        Product a1 = UnitTestFactory.productWithBarcode("A");
        Product a2 = UnitTestFactory.productWithBarcode("A");
        Product b = UnitTestFactory.productWithBarcode("B");

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
        Product p1 = UnitTestFactory.product(1, "A", null, null, null, null);
        Product p2 = UnitTestFactory.product(2, "B", null, null, null, null);
        Product p3 = UnitTestFactory.product(3, null, null, null, null, null);
        Product p4 = UnitTestFactory.product(null, "C", null, null, null, null);
        Product p5 = UnitTestFactory.product(999, "A", null, null, null, null);

        Map<String, Integer> map = ProductApi.toProductIdByBarcode(List.of(p1, p2, p3, p4, p5));

        assertEquals(2, map.size());
        assertEquals(1, map.get("A")); // keeps first
        assertEquals(2, map.get("B"));
    }
}