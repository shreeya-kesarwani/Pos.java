package com.pos.inventory.unit;

import com.pos.api.InventoryApi;
import com.pos.api.ProductApi;
import com.pos.exception.ApiException;
import com.pos.flow.InventoryFlow;
import com.pos.pojo.Inventory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InventoryFlowTest {

    @InjectMocks
    private InventoryFlow inventoryFlow;

    @Mock
    private InventoryApi inventoryApi;

    @Mock
    private ProductApi productApi;

    private String barcode;
    private String productName;
    private int page;
    private int pageSize;

    private Inventory inv(Integer productId, Integer qty) {
        Inventory i = new Inventory();
        i.setProductId(productId);
        i.setQuantity(qty);
        return i;
    }

    @BeforeEach
    void setupData() {
        barcode = "B1";
        productName = "Soap";
        page = 1;
        pageSize = 20;
    }

    @Test
    void searchInventories_shouldCallProductApiThenInventoryApi() throws ApiException {
        List<Integer> productIds = List.of(10, 20);
        List<Inventory> expected = List.of(inv(10, 1), inv(20, 2));

        when(productApi.findProductIdsByBarcodeOrName(barcode, productName)).thenReturn(productIds);
        when(inventoryApi.findByProductIds(productIds, page, pageSize)).thenReturn(expected);

        List<Inventory> actual = inventoryFlow.searchInventories(barcode, productName, page, pageSize);

        assertSame(expected, actual);
        verify(productApi).findProductIdsByBarcodeOrName(barcode, productName);
        verify(inventoryApi).findByProductIds(productIds, page, pageSize);
        verifyNoMoreInteractions(productApi, inventoryApi);
    }

    @Test
    void searchInventories_shouldPassEmptyProductIds_whenProductApiReturnsEmpty() throws ApiException {
        List<Integer> empty = List.of();
        List<Inventory> expected = List.of();

        when(productApi.findProductIdsByBarcodeOrName(null, "Whatever")).thenReturn(empty);
        when(inventoryApi.findByProductIds(empty, 0, 10)).thenReturn(expected);

        List<Inventory> actual = inventoryFlow.searchInventories(null, "Whatever", 0, 10);

        assertSame(expected, actual);
        verify(productApi).findProductIdsByBarcodeOrName(null, "Whatever");
        verify(inventoryApi).findByProductIds(empty, 0, 10);
        verifyNoMoreInteractions(productApi, inventoryApi);
    }

    @Test
    void searchInventories_shouldPassNullProductIds_whenProductApiReturnsNull() throws ApiException {
        when(productApi.findProductIdsByBarcodeOrName(barcode, null)).thenReturn(null);
        when(inventoryApi.findByProductIds(null, 0, 10)).thenReturn(List.of());

        List<Inventory> actual = inventoryFlow.searchInventories(barcode, null, 0, 10);

        assertNotNull(actual);
        verify(productApi).findProductIdsByBarcodeOrName(barcode, null);
        verify(inventoryApi).findByProductIds(null, 0, 10);
        verifyNoMoreInteractions(productApi, inventoryApi);
    }

    @Test
    void getSearchCount_shouldCallProductApiThenInventoryApiCount() throws ApiException {
        String bc = "B2";
        String name = "Milk";
        List<Integer> productIds = List.of(1, 2, 3);

        when(productApi.findProductIdsByBarcodeOrName(bc, name)).thenReturn(productIds);
        when(inventoryApi.getCountByProductIds(productIds)).thenReturn(42L);

        long count = inventoryFlow.getSearchCount(bc, name);

        assertEquals(42L, count);
        verify(productApi).findProductIdsByBarcodeOrName(bc, name);
        verify(inventoryApi).getCountByProductIds(productIds);
        verifyNoMoreInteractions(productApi, inventoryApi);
    }

    @Test
    void getSearchCount_shouldPassEmptyIds_whenProductApiReturnsEmpty() throws ApiException {
        List<Integer> empty = List.of();

        when(productApi.findProductIdsByBarcodeOrName(null, null)).thenReturn(empty);
        when(inventoryApi.getCountByProductIds(empty)).thenReturn(0L);

        long count = inventoryFlow.getSearchCount(null, null);

        assertEquals(0L, count);
        verify(productApi).findProductIdsByBarcodeOrName(null, null);
        verify(inventoryApi).getCountByProductIds(empty);
        verifyNoMoreInteractions(productApi, inventoryApi);
    }

    @Test
    void searchInventories_shouldPropagateApiException_fromProductApi() throws ApiException {
        when(productApi.findProductIdsByBarcodeOrName(any(), any()))
                .thenThrow(new ApiException("boom"));

        assertThrows(ApiException.class, () -> inventoryFlow.searchInventories("B", "P", 0, 10));
        verify(productApi).findProductIdsByBarcodeOrName("B", "P");
        verifyNoInteractions(inventoryApi);
    }

    @Test
    void getSearchCount_shouldPropagateApiException_fromInventoryApi() throws ApiException {
        List<Integer> ids = List.of(1);

        when(productApi.findProductIdsByBarcodeOrName(any(), any())).thenReturn(ids);
        when(inventoryApi.getCountByProductIds(ids)).thenThrow(new ApiException("boom"));

        assertThrows(ApiException.class, () -> inventoryFlow.getSearchCount("B", "P"));
        verify(productApi).findProductIdsByBarcodeOrName("B", "P");
        verify(inventoryApi).getCountByProductIds(ids);
        verifyNoMoreInteractions(productApi, inventoryApi);
    }
}