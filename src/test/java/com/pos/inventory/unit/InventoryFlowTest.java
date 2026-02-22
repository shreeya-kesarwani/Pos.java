package com.pos.inventory.unit;

import com.pos.api.InventoryApi;
import com.pos.api.ProductApi;
import com.pos.exception.ApiException;
import com.pos.flow.InventoryFlow;
import com.pos.pojo.Inventory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InventoryFlowTest {

    @InjectMocks
    private InventoryFlow inventoryFlow;

    @Mock
    private InventoryApi inventoryApi;

    @Mock
    private ProductApi productApi;

    @Test
    void searchInventories_shouldCallProductApiToFindIds_thenCallInventoryApiFindByProductIds() throws ApiException {
        // Setup
        String barcode = "B1";
        String productName = "Soap";
        int page = 1;
        int pageSize = 20;

        List<Integer> productIds = List.of(10, 20);
        List<Inventory> expected = List.of(new Inventory(), new Inventory());

        when(productApi.findProductIdsByBarcodeOrName(barcode, productName)).thenReturn(productIds);
        when(inventoryApi.findByProductIds(productIds, page, pageSize)).thenReturn(expected);

        // Execute
        List<Inventory> actual = inventoryFlow.searchInventories(barcode, productName, page, pageSize);

        // Verify
        assertSame(expected, actual);
        verify(productApi).findProductIdsByBarcodeOrName(barcode, productName);
        verify(inventoryApi).findByProductIds(productIds, page, pageSize);
        verifyNoMoreInteractions(productApi, inventoryApi);
    }

    @Test
    void searchInventories_shouldPassEmptyProductIds_whenProductApiReturnsEmpty() throws ApiException {
        // Setup
        String barcode = null;
        String productName = "Whatever";
        int page = 0;
        int pageSize = 10;

        List<Integer> productIds = List.of();
        List<Inventory> expected = List.of();

        when(productApi.findProductIdsByBarcodeOrName(barcode, productName)).thenReturn(productIds);
        when(inventoryApi.findByProductIds(productIds, page, pageSize)).thenReturn(expected);

        // Execute
        List<Inventory> actual = inventoryFlow.searchInventories(barcode, productName, page, pageSize);

        // Verify
        assertSame(expected, actual);
        verify(productApi).findProductIdsByBarcodeOrName(barcode, productName);
        verify(inventoryApi).findByProductIds(productIds, page, pageSize);
        verifyNoMoreInteractions(productApi, inventoryApi);
    }

    @Test
    void searchInventories_shouldPassNullProductIds_whenProductApiReturnsNull() throws ApiException {
        // Setup
        String barcode = "B1";
        String productName = null;
        int page = 0;
        int pageSize = 10;

        when(productApi.findProductIdsByBarcodeOrName(barcode, productName)).thenReturn(null);
        when(inventoryApi.findByProductIds(null, page, pageSize)).thenReturn(List.of());

        // Execute
        List<Inventory> actual = inventoryFlow.searchInventories(barcode, productName, page, pageSize);

        // Verify
        assertNotNull(actual);
        verify(productApi).findProductIdsByBarcodeOrName(barcode, productName);
        verify(inventoryApi).findByProductIds(null, page, pageSize);
        verifyNoMoreInteractions(productApi, inventoryApi);
    }

    @Test
    void getSearchCount_shouldCallProductApiToFindIds_thenCallInventoryApiCount() throws ApiException {
        // Setup
        String barcode = "B2";
        String productName = "Milk";

        List<Integer> productIds = List.of(1, 2, 3);

        when(productApi.findProductIdsByBarcodeOrName(barcode, productName)).thenReturn(productIds);
        when(inventoryApi.getCountByProductIds(productIds)).thenReturn(42L);

        // Execute
        long count = inventoryFlow.getSearchCount(barcode, productName);

        // Verify
        assertEquals(42L, count);
        verify(productApi).findProductIdsByBarcodeOrName(barcode, productName);
        verify(inventoryApi).getCountByProductIds(productIds);
        verifyNoMoreInteractions(productApi, inventoryApi);
    }

    @Test
    void getSearchCount_shouldPassEmptyIds_whenProductApiReturnsEmpty() throws ApiException {
        // Setup
        String barcode = null;
        String productName = null;

        List<Integer> empty = List.of();

        when(productApi.findProductIdsByBarcodeOrName(barcode, productName)).thenReturn(empty);
        when(inventoryApi.getCountByProductIds(empty)).thenReturn(0L);

        // Execute
        long count = inventoryFlow.getSearchCount(barcode, productName);

        // Verify
        assertEquals(0L, count);
        verify(productApi).findProductIdsByBarcodeOrName(barcode, productName);
        verify(inventoryApi).getCountByProductIds(empty);
        verifyNoMoreInteractions(productApi, inventoryApi);
    }

    @Test
    void searchInventories_shouldPropagateApiException_fromProductApi() throws ApiException {
        // Setup
        when(productApi.findProductIdsByBarcodeOrName(any(), any()))
                .thenThrow(new ApiException("boom"));

        // Execute & Verify
        assertThrows(ApiException.class, () -> inventoryFlow.searchInventories("B", "P", 0, 10));
        verify(productApi).findProductIdsByBarcodeOrName("B", "P");
        verifyNoInteractions(inventoryApi);
    }

    @Test
    void getSearchCount_shouldPropagateApiException_fromInventoryApi() throws ApiException {
        // Setup
        List<Integer> ids = List.of(1);
        when(productApi.findProductIdsByBarcodeOrName(any(), any())).thenReturn(ids);
        when(inventoryApi.getCountByProductIds(ids)).thenThrow(new ApiException("boom"));

        // Execute & Verify
        assertThrows(ApiException.class, () -> inventoryFlow.getSearchCount("B", "P"));
        verify(productApi).findProductIdsByBarcodeOrName("B", "P");
        verify(inventoryApi).getCountByProductIds(ids);
    }
}