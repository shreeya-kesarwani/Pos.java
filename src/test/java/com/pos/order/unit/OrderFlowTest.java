package com.pos.order.unit;

import com.pos.api.InventoryApi;
import com.pos.api.OrderApi;
import com.pos.api.ProductApi;
import com.pos.exception.ApiException;
import com.pos.flow.OrderFlow;
import com.pos.pojo.OrderItem;
import org.junit.jupiter.api.BeforeEach;
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

    private OrderItem i1;
    private OrderItem i2;

    private OrderItem item(Integer productId, Integer qty, Double sp) {
        OrderItem oi = new OrderItem();
        oi.setProductId(productId);
        oi.setQuantity(qty);
        oi.setSellingPrice(sp);
        return oi;
    }

    @BeforeEach
    void setupData() {
        i1 = item(101, 2, 50.0);
        i2 = item(202, 1, 10.0);
    }

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
    void createOrder_shouldThrow_whenAnyItemHasNullProductId_andStopProcessing() {
        OrderItem bad = item(null, 2, 10.0);

        ApiException ex = assertThrows(ApiException.class, () -> orderFlow.createOrder(List.of(bad)));
        assertTrue(ex.getMessage().contains(PRODUCT_NOT_FOUND.value()));

        verifyNoInteractions(productApi, inventoryApi, orderApi);
    }

    //todo: write a happy flow test to check if the order is created or not, dont remove this add new
    @Test
    void createOrder_happyFlow_shouldReturnOrderId_andCallDepsInOrder() throws ApiException {
        when(orderApi.create(List.of(i1, i2))).thenReturn(999);

        Integer out = orderFlow.createOrder(List.of(i1, i2));

        assertEquals(999, out);

        InOrder inOrder = inOrder(productApi, inventoryApi, orderApi);
        inOrder.verify(productApi).validateSellingPrice(101, 50.0);
        inOrder.verify(inventoryApi).reduceInventory(101, 2);
        inOrder.verify(productApi).validateSellingPrice(202, 10.0);
        inOrder.verify(inventoryApi).reduceInventory(202, 1);
        inOrder.verify(orderApi).create(List.of(i1, i2));

        verifyNoMoreInteractions(orderApi, inventoryApi, productApi);
    }

    @Test
    void createOrder_shouldNotCallOrderCreate_whenValidationFails() throws ApiException {
        OrderItem expensive = item(101, 2, 5000.0);

        doThrow(new ApiException("selling price exceeds mrp"))
                .when(productApi).validateSellingPrice(101, 5000.0);

        assertThrows(ApiException.class, () -> orderFlow.createOrder(List.of(expensive)));

        verify(productApi).validateSellingPrice(101, 5000.0);
        verifyNoInteractions(inventoryApi);
        verify(orderApi, never()).create(anyList());
        verifyNoMoreInteractions(productApi, orderApi);
    }

    @Test
    void createOrder_shouldNotCallOrderCreate_whenInventoryReductionFails() throws ApiException {
        doThrow(new ApiException("insufficient inventory"))
                .when(inventoryApi).reduceInventory(101, 2);

        assertThrows(ApiException.class, () -> orderFlow.createOrder(List.of(i1)));

        verify(productApi).validateSellingPrice(101, 50.0);
        verify(inventoryApi).reduceInventory(101, 2);
        verify(orderApi, never()).create(anyList());

        verifyNoMoreInteractions(productApi, inventoryApi, orderApi);
    }
}