package com.pos.unit.flow;

import com.pos.api.ClientApi;
import com.pos.api.ProductApi;
import com.pos.exception.ApiException;
import com.pos.flow.ProductFlow;
import com.pos.pojo.Client;
import com.pos.pojo.Product;
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

    @Mock
    private ProductApi productApi;

    @Mock
    private ClientApi clientApi;

    @Test
    void add_shouldValidateClient_thenDelegateToProductApi() throws ApiException {
        Product p = new Product();
        p.setClientId(10);

        // clientApi.getCheck returns Client (based on your ClientApi)
        when(clientApi.getCheck(10)).thenReturn(new Client());

        productFlow.add(p);

        verify(clientApi).getCheck(10);
        verify(productApi).add(p);
        verifyNoMoreInteractions(productApi, clientApi);
    }

    @Test
    void add_shouldNotCallProductApi_whenClientValidationFails() throws ApiException {
        Product p = new Product();
        p.setClientId(10);

        when(clientApi.getCheck(10)).thenThrow(new ApiException("client missing"));

        assertThrows(ApiException.class, () -> productFlow.add(p));

        verify(clientApi).getCheck(10);
        verifyNoInteractions(productApi);
    }

    @Test
    void addBulk_shouldValidateClient_thenDelegateToProductApiAddBulk() throws ApiException {
        Integer clientId = 5;
        List<Product> products = List.of(new Product(), new Product());

        when(clientApi.getCheck(clientId)).thenReturn(new Client());

        productFlow.addBulk(products, clientId);

        verify(clientApi).getCheck(clientId);
        verify(productApi).addBulk(products);
        verifyNoMoreInteractions(productApi, clientApi);
    }

    @Test
    void addBulk_shouldNotCallProductApi_whenClientValidationFails() throws ApiException {
        Integer clientId = 5;
        List<Product> products = List.of(new Product());

        when(clientApi.getCheck(clientId)).thenThrow(new ApiException("client missing"));

        assertThrows(ApiException.class, () -> productFlow.addBulk(products, clientId));

        verify(clientApi).getCheck(clientId);
        verifyNoInteractions(productApi);
    }
}
