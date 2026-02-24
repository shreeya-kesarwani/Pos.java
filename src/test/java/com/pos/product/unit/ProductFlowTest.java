package com.pos.product.unit;

import com.pos.api.ClientApi;
import com.pos.api.ProductApi;
import com.pos.exception.ApiException;
import com.pos.flow.ProductFlow;
import com.pos.pojo.Client;
import com.pos.pojo.Product;
import com.pos.setup.UnitTestFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductFlowTest {

    @InjectMocks
    private ProductFlow productFlow;

    @Mock private ProductApi productApi;
    @Mock private ClientApi clientApi;

    private Integer clientId;
    private Client client;

    @BeforeEach
    void setupData() {
        clientId = 10;
        client = UnitTestFactory.client(clientId, "c", "c@mail");
    }

    @Test
    void add_shouldValidateClient_thenDelegateToProductApiAdd() throws ApiException {
        Product p = UnitTestFactory.product(null, "BC", clientId, null, null, null);
        when(clientApi.getCheck(clientId)).thenReturn(client);

        productFlow.add(p);

        verify(clientApi).getCheck(clientId);
        verify(productApi).add(p);
        verifyNoMoreInteractions(clientApi, productApi);
    }

    @Test
    void add_shouldNotCallProductApi_whenClientValidationFails() throws ApiException {
        Product p = UnitTestFactory.product(null, "BC", clientId, null, null, null);
        when(clientApi.getCheck(clientId)).thenThrow(new ApiException("client missing"));

        assertThrows(ApiException.class, () -> productFlow.add(p));

        verify(clientApi).getCheck(clientId);
        verifyNoInteractions(productApi);
        verifyNoMoreInteractions(clientApi);
    }

    @Test
    void addBulk_shouldValidateClient_thenDelegateToProductApiAddBulk() throws ApiException {
        Integer bulkClientId = 5;
        List<Product> products = List.of(
                UnitTestFactory.productWithBarcode("A"),
                UnitTestFactory.productWithBarcode("B")
        );

        when(clientApi.getCheck(bulkClientId)).thenReturn(UnitTestFactory.client(bulkClientId, "x", "x@mail"));

        productFlow.addBulk(products, bulkClientId);

        verify(clientApi).getCheck(bulkClientId);
        verify(productApi).addBulk(products);
        verifyNoMoreInteractions(clientApi, productApi);
    }

    @Test
    void addBulk_shouldNotCallProductApi_whenClientValidationFails() throws ApiException {
        Integer bulkClientId = 5;
        List<Product> products = List.of(UnitTestFactory.productWithBarcode("A"));

        when(clientApi.getCheck(bulkClientId)).thenThrow(new ApiException("client missing"));

        assertThrows(ApiException.class, () -> productFlow.addBulk(products, bulkClientId));

        verify(clientApi).getCheck(bulkClientId);
        verifyNoInteractions(productApi);
        verifyNoMoreInteractions(clientApi);
    }
}