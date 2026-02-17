package com.pos.unit.flow;

import com.pos.api.InventoryApi;
import com.pos.api.OrderApi;
import com.pos.api.ProductApi;
import com.pos.exception.ApiException;
import com.pos.flow.OrderFlow;
import com.pos.pojo.OrderItem;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static com.pos.model.constants.ErrorMessages.NO_ORDER_ITEMS_FOUND;
import static com.pos.model.constants.ErrorMessages.PRODUCT_NOT_FOUND;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderFlowTest {

    @InjectMocks
    private OrderFlow orderFlow;

    @Mock private OrderApi orderApi;
    @Mock private InventoryApi inventoryApi;
    @Mock private ProductApi productApi;

    @Test
    void createOrder_shouldThrow_whenItemsNull() {
        ApiException ex = assertThrows(ApiException.class, () -> orderFlow.createOrder(null));
        assertTrue(ex.getMessage().contains(NO_ORDER_ITEMS_FOUND.value()));

        verifyNoInteractions(orderApi, inventoryApi, productApi);
    }

    @Test
    void createOrder_shouldThrow_whenItemsEmpty() {
        ApiException ex = assertThrows(ApiException.class, () -> orderFlow.createOrder(List.of()));
        assertTrue(ex.getMessage().contains(NO_ORDER_ITEMS_FOUND.value()));

        verifyNoInteractions(orderApi, inventoryApi, productApi);
    }

    @Test
    void createOrder_shouldThrow_whenAnyItemHasNullProductId_andStopProcessing() throws ApiException {
        OrderItem item1 = new OrderItem();
        item1.setProductId(null);
        item1.setQuantity(2);
        item1.setSellingPrice(10.0);

        ApiException ex = assertThrows(ApiException.class, () -> orderFlow.createOrder(List.of(item1)));
        assertTrue(ex.getMessage().contains(PRODUCT_NOT_FOUND.value()));

        verifyNoInteractions(productApi, inventoryApi, orderApi);
    }

    @Test
    void createOrder_shouldValidatePriceAndReduceInventory_forEachItem_thenCreateOrder() throws ApiException {
        OrderItem i1 = new OrderItem();
        i1.setProductId(101);
        i1.setQuantity(2);
        i1.setSellingPrice(50.0);

        OrderItem i2 = new OrderItem();
        i2.setProductId(202);
        i2.setQuantity(1);
        i2.setSellingPrice(10.0);

        when(orderApi.create(List.of(i1, i2))).thenReturn(999);

        Integer orderId = orderFlow.createOrder(List.of(i1, i2));

        assertEquals(999, orderId);

        InOrder inOrder = inOrder(productApi, inventoryApi, orderApi);
        inOrder.verify(productApi).validateSellingPrice(101, 50.0);
        inOrder.verify(inventoryApi).reduceInventory(101, 2);

        inOrder.verify(productApi).validateSellingPrice(202, 10.0);
        inOrder.verify(inventoryApi).reduceInventory(202, 1);

        inOrder.verify(orderApi).create(List.of(i1, i2));

        verifyNoMoreInteractions(orderApi, inventoryApi, productApi);
    }

    @Test
    void createOrder_shouldNotCallOrderCreate_ifValidationFails() throws ApiException {
        OrderItem i1 = new OrderItem();
        i1.setProductId(101);
        i1.setQuantity(2);
        i1.setSellingPrice(5000.0);

        doThrow(new ApiException("selling price exceeds mrp"))
                .when(productApi).validateSellingPrice(101, 5000.0);

        assertThrows(ApiException.class, () -> orderFlow.createOrder(List.of(i1)));

        verify(productApi).validateSellingPrice(101, 5000.0);
        verifyNoInteractions(inventoryApi);
        verify(orderApi, never()).create(anyList());
    }

    @Test
    void createOrder_shouldNotCallOrderCreate_ifInventoryReductionFails() throws ApiException {
        OrderItem i1 = new OrderItem();
        i1.setProductId(101);
        i1.setQuantity(2);
        i1.setSellingPrice(50.0);

        doThrow(new ApiException("insufficient inventory"))
                .when(inventoryApi).reduceInventory(101, 2);

        assertThrows(ApiException.class, () -> orderFlow.createOrder(List.of(i1)));

        verify(productApi).validateSellingPrice(101, 50.0);
        verify(inventoryApi).reduceInventory(101, 2);
        verify(orderApi, never()).create(anyList());
    }
}
