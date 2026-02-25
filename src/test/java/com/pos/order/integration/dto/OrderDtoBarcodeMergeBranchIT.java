package com.pos.order.integration.dto;

import com.pos.api.ProductApi;
import com.pos.dto.OrderDto;
import com.pos.exception.ApiException;
import com.pos.flow.OrderFlow;
import com.pos.model.form.OrderForm;
import com.pos.model.form.OrderItemForm;
import com.pos.pojo.Product;
import com.pos.setup.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

class OrderDtoBarcodeMergeBranchIT extends AbstractIntegrationTest {

    @Autowired private OrderDto orderDto;

    @MockBean private ProductApi productApi;
    @MockBean private OrderFlow orderFlow; // so create() doesn't touch DB

    @Test
    void create_shouldHandleDuplicateBarcodesFromProductApi_andCoverMergeFunction() throws Exception {
        Product p1 = new Product();
        p1.setId(1);
        p1.setBarcode("b1");

        Product p2 = new Product();
        p2.setId(2);
        p2.setBarcode("b1"); // duplicate barcode

        when(productApi.getCheckByBarcodes(anyList())).thenReturn(List.of(p1, p2));
        when(orderFlow.createOrder(anyList())).thenReturn(123); // stop deeper flow

        OrderItemForm item = new OrderItemForm();
        item.setBarcode("b1");
        item.setQuantity(1);
        item.setSellingPrice(10.0);

        OrderForm form = new OrderForm();
        form.setItems(List.of(item));

        assertDoesNotThrow(() -> orderDto.create(form));
        verify(productApi).getCheckByBarcodes(anyList());
        verify(orderFlow).createOrder(anyList());
    }
}