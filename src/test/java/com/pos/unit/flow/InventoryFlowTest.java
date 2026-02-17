package com.pos.unit.flow;

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
        String barcode = "B1";
        String productName = "Soap";
        int page = 1;
        int pageSize = 20;

        List<Integer> productIds = List.of(10, 20);
        List<Inventory> expected = List.of(new Inventory(), new Inventory());

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
        String barcode = null;
        String productName = "Whatever";
        int page = 0;
        int pageSize = 10;

        List<Integer> productIds = List.of(); // empty
        List<Inventory> expected = List.of();

        when(productApi.findProductIdsByBarcodeOrName(barcode, productName)).thenReturn(productIds);
        when(inventoryApi.findByProductIds(productIds, page, pageSize)).thenReturn(expected);

        List<Inventory> actual = inventoryFlow.searchInventories(barcode, productName, page, pageSize);

        assertSame(expected, actual);
        verify(productApi).findProductIdsByBarcodeOrName(barcode, productName);
        verify(inventoryApi).findByProductIds(productIds, page, pageSize);
        verifyNoMoreInteractions(productApi, inventoryApi);
    }

    @Test
    void searchInventories_shouldPassNullProductIds_ifProductApiReturnsNull() throws ApiException {
        String barcode = "B1";
        String productName = null;
        int page = 0;
        int pageSize = 10;

        when(productApi.findProductIdsByBarcodeOrName(barcode, productName)).thenReturn(null);
        when(inventoryApi.findByProductIds(null, page, pageSize)).thenReturn(List.of());

        List<Inventory> actual = inventoryFlow.searchInventories(barcode, productName, page, pageSize);

        assertNotNull(actual);
        verify(productApi).findProductIdsByBarcodeOrName(barcode, productName);
        verify(inventoryApi).findByProductIds(null, page, pageSize);
        verifyNoMoreInteractions(productApi, inventoryApi);
    }

    @Test
    void getSearchCount_shouldCallProductApiToFindIds_thenCallInventoryApiCount() throws ApiException {
        String barcode = "B2";
        String productName = "Milk";

        List<Integer> productIds = List.of(1, 2, 3);

        when(productApi.findProductIdsByBarcodeOrName(barcode, productName)).thenReturn(productIds);
        when(inventoryApi.getCountByProductIds(productIds)).thenReturn(42L);

        long count = inventoryFlow.getSearchCount(barcode, productName);

        assertEquals(42L, count);
        verify(productApi).findProductIdsByBarcodeOrName(barcode, productName);
        verify(inventoryApi).getCountByProductIds(productIds);
        verifyNoMoreInteractions(productApi, inventoryApi);
    }

    @Test
    void getSearchCount_shouldPassEmptyIds_whenProductApiReturnsEmpty() throws ApiException {
        String barcode = null;
        String productName = null;

        List<Integer> empty = List.of();

        when(productApi.findProductIdsByBarcodeOrName(barcode, productName)).thenReturn(empty);
        when(inventoryApi.getCountByProductIds(empty)).thenReturn(0L);

        long count = inventoryFlow.getSearchCount(barcode, productName);

        assertEquals(0L, count);
        verify(productApi).findProductIdsByBarcodeOrName(barcode, productName);
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
    }
}
