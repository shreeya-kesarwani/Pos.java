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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductFlowTest {

    @Mock private ProductApi productApi;
    @Mock private ClientApi clientApi;

    @InjectMocks private ProductFlow productFlow;

    @Test
    void add_shouldSetClientId_andCallProductApi() throws ApiException {
        Client c = new Client();
        c.setId(10);
        c.setName("ABC");

        when(clientApi.getByName("ABC")).thenReturn(c);

        Product p = new Product();
        p.setName("Pen");

        productFlow.add(p, "ABC");

        assertEquals(10, p.getClientId());
        verify(clientApi).getByName("ABC");
        verify(productApi).add(p);
        verifyNoMoreInteractions(clientApi, productApi);
    }

    @Test
    void add_shouldThrow_whenClientNameBlank() {
        Product p = new Product();
        ApiException ex = assertThrows(ApiException.class, () -> productFlow.add(p, "   "));
        assertTrue(ex.getMessage().contains("Client name is required"));
        verifyNoInteractions(clientApi, productApi);
    }

    @Test
    void add_shouldThrow_whenClientNotFound() throws ApiException {
        when(clientApi.getByName("ABC")).thenReturn(null);

        Product p = new Product();
        ApiException ex = assertThrows(ApiException.class, () -> productFlow.add(p, "ABC"));
        assertTrue(ex.getMessage().contains("Client not found"));
        verify(clientApi).getByName("ABC");
        verifyNoInteractions(productApi);
    }

}
