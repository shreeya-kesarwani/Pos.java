package com.pos.product.unit;

import com.pos.api.ClientApi;
import com.pos.api.ProductApi;
import com.pos.exception.ApiException;
import com.pos.flow.ProductFlow;
import com.pos.pojo.Client;
import com.pos.pojo.Product;
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

    private Client client(Integer id, String name, String email) {
        Client c = new Client();
        c.setId(id);
        c.setName(name);
        c.setEmail(email);
        return c;
    }

    private Product product(Integer id, String barcode, Integer clientId, String name, Double mrp, String imageUrl) {
        Product p = new Product();
        p.setId(id);
        p.setBarcode(barcode);
        p.setClientId(clientId);
        p.setName(name);
        p.setMrp(mrp);
        p.setImageUrl(imageUrl);
        return p;
    }

    @BeforeEach
    void setupData() {
        clientId = 10;
        client = client(clientId, "c", "c@mail");
    }

    @Test
    void add_shouldValidateClient_thenDelegateToProductApiAdd() throws ApiException {
        Product p = product(null, "BC", clientId, null, null, null);
        when(clientApi.getCheck(clientId)).thenReturn(client);

        productFlow.add(p);

        verify(clientApi).getCheck(clientId);
        verify(productApi).add(p);
        verifyNoMoreInteractions(clientApi, productApi);
    }

    @Test
    void add_shouldNotCallProductApi_whenClientValidationFails() throws ApiException {
        Product p = product(null, "BC", clientId, null, null, null);
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
                product(null, "A", bulkClientId, null, null, null),
                product(null, "B", bulkClientId, null, null, null)
        );

        when(clientApi.getCheck(bulkClientId)).thenReturn(client(bulkClientId, "x", "x@mail"));

        productFlow.addBulk(products, bulkClientId);

        verify(clientApi).getCheck(bulkClientId);
        verify(productApi).addBulk(products);
        verifyNoMoreInteractions(clientApi, productApi);
    }

    @Test
    void addBulk_shouldNotCallProductApi_whenClientValidationFails() throws ApiException {
        Integer bulkClientId = 5;
        List<Product> products = List.of(product(null, "A", bulkClientId, null, null, null));

        when(clientApi.getCheck(bulkClientId)).thenThrow(new ApiException("client missing"));

        assertThrows(ApiException.class, () -> productFlow.addBulk(products, bulkClientId));

        verify(clientApi).getCheck(bulkClientId);
        verifyNoInteractions(productApi);
        verifyNoMoreInteractions(clientApi);
    }
}