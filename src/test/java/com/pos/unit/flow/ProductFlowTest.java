package com.pos.unit.flow;

import com.pos.api.ClientApi;
import com.pos.api.ProductApi;
import com.pos.exception.ApiException;
import com.pos.flow.ProductFlow;
import com.pos.model.form.ProductForm;
import com.pos.pojo.Client;
import com.pos.pojo.Product;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductFlowTest {

    @Mock
    private ProductApi productApi;

    @Mock
    private ClientApi clientApi;

    @InjectMocks
    private ProductFlow productFlow;

    @Test
    void add_shouldResolveClientId_andCallProductApi() throws ApiException {
        Product p = new Product();
        p.setName("Pen");

        Client client = new Client();
        client.setId(10);
        client.setName("ClientA");

        when(clientApi.getByName("ClientA")).thenReturn(client);

        productFlow.add(p, "ClientA");

        assertEquals(10, p.getClientId());
        verify(clientApi).getByName("ClientA");
        verify(productApi).add(p);
        verifyNoMoreInteractions(productApi, clientApi);
    }

    @Test
    void update_shouldResolveClientId_andCallProductApiUpdate() throws ApiException {
        Product p = new Product();
        p.setName("Pen");

        Client client = new Client();
        client.setId(20);
        client.setName("ClientB");

        when(clientApi.getByName("ClientB")).thenReturn(client);

        productFlow.update("B1", p, "ClientB");

        assertEquals(20, p.getClientId());
        verify(clientApi).getByName("ClientB");
        verify(productApi).update("B1", p);
        verifyNoMoreInteractions(productApi, clientApi);
    }

    @Test
    void addBulkFromForms_shouldResolveClients_andCallProductApiAddBulk() throws ApiException {
        ProductForm f1 = new ProductForm();
        f1.setBarcode("B1");
        f1.setClientName("ClientA");
        f1.setName("Pen");
        f1.setMrp(10.0);

        ProductForm f2 = new ProductForm();
        f2.setBarcode("B2");
        f2.setClientName("ClientA");
        f2.setName("Pencil");
        f2.setMrp(5.0);

        Client client = new Client();
        client.setId(1);
        client.setName("ClientA");

        when(clientApi.getByNames(anyList())).thenReturn(List.of(client));

        productFlow.addBulkFromForms(List.of(f1, f2));

        verify(clientApi).getByNames(anyList());
        verify(productApi).addBulk(argThat(products ->
                products.size() == 2 &&
                        products.get(0).getClientId().equals(1) &&
                        products.get(1).getClientId().equals(1)
        ));
        verifyNoMoreInteractions(productApi, clientApi);
    }
}
