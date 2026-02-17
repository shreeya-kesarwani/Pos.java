package com.pos.unit.api;

import com.pos.api.InventoryApi;
import com.pos.dao.InventoryDao;
import com.pos.exception.ApiException;
import com.pos.model.form.InventoryForm;
import com.pos.pojo.Inventory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.pos.model.constants.ErrorMessages.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InventoryApiTest {

    @InjectMocks
    private InventoryApi inventoryApi;

    @Mock
    private InventoryDao inventoryDao;

    // ---------------- get / getCheck ----------------

    @Test
    void get_shouldCallDaoSelectById() {
        Inventory inv = new Inventory();
        when(inventoryDao.selectById(1)).thenReturn(inv);

        Inventory out = inventoryApi.get(1);

        assertSame(inv, out);
        verify(inventoryDao).selectById(1);
        verifyNoMoreInteractions(inventoryDao);
    }

    @Test
    void getCheck_shouldReturn_whenFound() throws ApiException {
        Inventory inv = new Inventory();
        when(inventoryDao.selectById(10)).thenReturn(inv);

        Inventory out = inventoryApi.getCheck(10);

        assertSame(inv, out);
        verify(inventoryDao).selectById(10);
    }

    @Test
    void getCheck_shouldThrow_whenNotFound() {
        when(inventoryDao.selectById(99)).thenReturn(null);

        ApiException ex = assertThrows(ApiException.class, () -> inventoryApi.getCheck(99));
        assertTrue(ex.getMessage().contains(INVENTORY_NOT_FOUND.value()));
        assertTrue(ex.getMessage().contains("99"));

        verify(inventoryDao).selectById(99);
    }

    // ---------------- getByProductId / getCheckByProductId ----------------

    @Test
    void getByProductId_shouldCallDao() {
        Inventory inv = new Inventory();
        when(inventoryDao.selectByProductId(5)).thenReturn(inv);

        Inventory out = inventoryApi.getByProductId(5);

        assertSame(inv, out);
        verify(inventoryDao).selectByProductId(5);
    }

    @Test
    void getCheckByProductId_shouldReturn_whenFound() throws ApiException {
        Inventory inv = new Inventory();
        when(inventoryDao.selectByProductId(7)).thenReturn(inv);

        Inventory out = inventoryApi.getCheckByProductId(7);

        assertSame(inv, out);
        verify(inventoryDao).selectByProductId(7);
    }

    @Test
    void getCheckByProductId_shouldThrow_whenNotFound() {
        when(inventoryDao.selectByProductId(7)).thenReturn(null);

        ApiException ex = assertThrows(ApiException.class, () -> inventoryApi.getCheckByProductId(7));
        assertTrue(ex.getMessage().contains(INVENTORY_NOT_FOUND_FOR_PRODUCT.value()));
        assertTrue(ex.getMessage().contains("7"));

        verify(inventoryDao).selectByProductId(7);
    }

    // ---------------- findByProductIds ----------------

    @Test
    void findByProductIds_shouldReturnEmpty_whenNullOrEmptyIds() {
        assertEquals(List.of(), inventoryApi.findByProductIds(null, 0, 10));
        assertEquals(List.of(), inventoryApi.findByProductIds(List.of(), 0, 10));

        verifyNoInteractions(inventoryDao);
    }

    @Test
    void findByProductIds_shouldCallDao_whenIdsPresent() {
        List<Integer> ids = List.of(1, 2);
        List<Inventory> expected = List.of(new Inventory());

        when(inventoryDao.findByProductIds(ids, 1, 20)).thenReturn(expected);

        List<Inventory> out = inventoryApi.findByProductIds(ids, 1, 20);

        assertSame(expected, out);
        verify(inventoryDao).findByProductIds(ids, 1, 20);
    }

    // ---------------- add ----------------

    @Test
    void add_shouldInsert_whenNoExistingInventoryForProduct() throws ApiException {
        Inventory inv = new Inventory();
        inv.setProductId(10);
        inv.setQuantity(5);

        when(inventoryDao.selectByProductId(10)).thenReturn(null);

        inventoryApi.add(List.of(inv));

        verify(inventoryDao).selectByProductId(10);
        verify(inventoryDao).insert(inv);
        verifyNoMoreInteractions(inventoryDao);
    }

    @Test
    void add_shouldUpdateExistingQuantity_whenExists() throws ApiException {
        Inventory incoming = new Inventory();
        incoming.setProductId(10);
        incoming.setQuantity(9);

        Inventory existing = new Inventory();
        existing.setProductId(10);
        existing.setQuantity(1);

        when(inventoryDao.selectByProductId(10)).thenReturn(existing);

        inventoryApi.add(List.of(incoming));

        assertEquals(9, existing.getQuantity()); // updated in-place
        verify(inventoryDao).selectByProductId(10);
        verify(inventoryDao, never()).insert(any());
    }

    @Test
    void add_shouldHandleMultipleInventories_mixedInsertAndUpdate() throws ApiException {
        Inventory inv1 = new Inventory(); inv1.setProductId(1); inv1.setQuantity(5);
        Inventory inv2 = new Inventory(); inv2.setProductId(2); inv2.setQuantity(7);

        Inventory existing2 = new Inventory(); existing2.setProductId(2); existing2.setQuantity(1);

        when(inventoryDao.selectByProductId(1)).thenReturn(null);
        when(inventoryDao.selectByProductId(2)).thenReturn(existing2);

        inventoryApi.add(List.of(inv1, inv2));

        assertEquals(7, existing2.getQuantity());
        verify(inventoryDao).selectByProductId(1);
        verify(inventoryDao).insert(inv1);
        verify(inventoryDao).selectByProductId(2);
        verify(inventoryDao, never()).insert(inv2);
    }

    // ---------------- reduceInventory ----------------

    @Test
    void reduceInventory_shouldThrow_whenQuantityNull() {
        ApiException ex = assertThrows(ApiException.class, () -> inventoryApi.reduceInventory(1, null));
        assertTrue(ex.getMessage().contains(QUANTITY_MUST_BE_POSITIVE.value()));
        verifyNoInteractions(inventoryDao);
    }

    @Test
    void reduceInventory_shouldThrow_whenQuantityZeroOrNegative() {
        ApiException ex1 = assertThrows(ApiException.class, () -> inventoryApi.reduceInventory(1, 0));
        assertTrue(ex1.getMessage().contains(QUANTITY_MUST_BE_POSITIVE.value()));

        ApiException ex2 = assertThrows(ApiException.class, () -> inventoryApi.reduceInventory(1, -1));
        assertTrue(ex2.getMessage().contains(QUANTITY_MUST_BE_POSITIVE.value()));

        verifyNoInteractions(inventoryDao);
    }

    @Test
    void reduceInventory_shouldThrow_whenInsufficientInventory() {
        Inventory existing = new Inventory();
        existing.setProductId(5);
        existing.setQuantity(2);

        when(inventoryDao.selectByProductId(5)).thenReturn(existing);

        ApiException ex = assertThrows(ApiException.class, () -> inventoryApi.reduceInventory(5, 3));
        assertTrue(ex.getMessage().contains(INSUFFICIENT_INVENTORY.value()));
        assertTrue(ex.getMessage().contains("productId=5"));

        verify(inventoryDao).selectByProductId(5);
    }

    @Test
    void reduceInventory_shouldReduceQuantity_whenSufficient() throws ApiException {
        Inventory existing = new Inventory();
        existing.setProductId(5);
        existing.setQuantity(10);

        when(inventoryDao.selectByProductId(5)).thenReturn(existing);

        inventoryApi.reduceInventory(5, 3);

        assertEquals(7, existing.getQuantity());
        verify(inventoryDao).selectByProductId(5);
    }

    // ---------------- getCountByProductIds ----------------

    @Test
    void getCountByProductIds_shouldReturnZero_whenNullOrEmpty() {
        assertEquals(0L, inventoryApi.getCountByProductIds(null));
        assertEquals(0L, inventoryApi.getCountByProductIds(List.of()));
        verifyNoInteractions(inventoryDao);
    }

    @Test
    void getCountByProductIds_shouldCallDao_whenIdsPresent() {
        List<Integer> ids = List.of(1, 2, 3);
        when(inventoryDao.getCountByProductIds(ids)).thenReturn(12L);

        long out = inventoryApi.getCountByProductIds(ids);

        assertEquals(12L, out);
        verify(inventoryDao).getCountByProductIds(ids);
    }

    // ---------------- static helpers ----------------

    @Test
    void extractBarcodes_shouldReturnEmpty_whenNullOrEmpty() {
        assertEquals(List.of(), InventoryApi.extractBarcodes(null));
        assertEquals(List.of(), InventoryApi.extractBarcodes(List.of()));
    }

    @Test
    void extractBarcodes_shouldTrimAndFilterNullEmpty() {
        InventoryForm f1 = new InventoryForm(); f1.setBarcode(" A ");
        InventoryForm f2 = new InventoryForm(); f2.setBarcode(null);
        InventoryForm f3 = new InventoryForm(); f3.setBarcode("   ");
        InventoryForm f4 = new InventoryForm(); f4.setBarcode("B");

        List<String> out = InventoryApi.extractBarcodes(List.of(f1, f2, f3, f4));

        assertEquals(List.of("A", "B"), out);
    }

    @Test
    void extractDistinctProductIds_shouldReturnEmpty_whenNullOrEmpty() {
        assertEquals(List.of(), InventoryApi.extractDistinctProductIds(null));
        assertEquals(List.of(), InventoryApi.extractDistinctProductIds(List.of()));
    }

    @Test
    void extractDistinctProductIds_shouldFilterNullAndDistinct() {
        Inventory i1 = new Inventory(); i1.setProductId(1);
        Inventory i2 = new Inventory(); i2.setProductId(1);
        Inventory i3 = new Inventory(); i3.setProductId(null);
        Inventory i4 = new Inventory(); i4.setProductId(2);

        List<Integer> out = InventoryApi.extractDistinctProductIds(List.of(i1, i2, i3, i4));

        assertEquals(List.of(1, 2), out);
    }
}
