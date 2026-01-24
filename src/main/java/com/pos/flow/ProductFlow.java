package com.pos.flow;

import com.pos.pojo.Client;
import com.pos.pojo.Product;
import com.pos.exception.ApiException;
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
    @Autowired private ClientService clientService; // Used only for add/update ID resolution

    public void add(Product p, String clientName) throws ApiException {
        p.setClientId(resolveClientId(clientName));
        productService.add(p);
    }

    public void update(String barcode, Product p, String clientName) throws ApiException {
        p.setClientId(resolveClientId(clientName));
        productService.update(barcode, p);
    }

    @Transactional(readOnly = true)
    public List<Product> search(String name, String barcode, String clientName, int page, int size) {
        // No longer calls clientService.search; DAO handles the join
        return productService.search(name, barcode, clientName, page, size);
    }

    @Transactional(readOnly = true)
    public Long getCount(String name, String barcode, String clientName) {
        return productService.getCount(name, barcode, clientName);
    }

    @Transactional(readOnly = true)
    public String getClientName(Integer clientId) throws ApiException {
        return (clientId == null) ? "N/A" : clientService.getCheckById(clientId).getName();
    }

    private Integer resolveClientId(String clientName) throws ApiException {
        List<Client> clients = clientService.search(null, clientName, null, 0, 1);
        if (clients.isEmpty()) {
            throw new ApiException(String.format("Client [%s] does not exist", clientName));
        }
        return clients.get(0).getId();
    }

    public void addBulk(List<Product> pojos, List<String> clientNames) throws ApiException {
        for (int i = 0; i < pojos.size(); i++) {
            Product p = pojos.get(i);
            String clientName = clientNames.get(i);

            p.setClientId(resolveClientId(clientName));
            productService.add(p);
        }
    }
}