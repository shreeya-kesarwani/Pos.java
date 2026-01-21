package com.pos.flow;

import com.pos.pojo.ClientPojo;
import com.pos.pojo.ProductPojo;
import com.pos.service.ApiException;
import com.pos.service.ClientService;
import com.pos.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@Transactional(rollbackFor = ApiException.class)
public class ProductFlow {

    @Autowired private ProductService productService;
    @Autowired private ClientService clientService;

    // KEEP: Orchestrates between Client and Product
    public void add(ProductPojo p, String clientName) throws ApiException {
        List<ClientPojo> clients = clientService.getFiltered(null, clientName, null);
        if (clients.isEmpty()) {
            throw new ApiException("Client with name '" + clientName + "' does not exist");
        }
        p.setClientId(clients.get(0).getId());
        productService.add(p);
    }

    // KEEP: Needs ClientService to resolve name to ID for the search filter
    @Transactional(readOnly = true)
    public List<ProductPojo> getAllFiltered(String name, String barcode, Integer clientId, String clientName) throws ApiException {
        if (clientId == null && clientName != null && !clientName.isEmpty()) {
            List<ClientPojo> clients = clientService.getFiltered(null, clientName, null);
            if (!clients.isEmpty()) {
                clientId = clients.get(0).getId();
            } else {
                return List.of();
            }
        }
        return productService.search(name, barcode, clientId);
    }

    // KEEP: DTO needs this to convert ID back to Name for display
    @Transactional(readOnly = true)
    public String getClientName(Integer clientId) {
        return clientService.getCheckById(clientId);
    }
}