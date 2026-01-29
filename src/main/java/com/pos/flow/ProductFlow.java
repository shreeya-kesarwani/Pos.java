package com.pos.flow;

import com.pos.pojo.Client;
import com.pos.pojo.Product;
import com.pos.exception.ApiException;
import com.pos.api.ClientApi;
import com.pos.api.ProductApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@Transactional(rollbackFor = ApiException.class)
public class ProductFlow {

    @Autowired private ProductApi productApi;
    @Autowired private ClientApi clientApi;

    public void add(Product p, String clientName) throws ApiException {
        p.setClientId(getClientIdByName(clientName));
        productApi.add(p);
    }

    public void update(String barcode, Product p, String clientName) throws ApiException {
        p.setClientId(getClientIdByName(clientName));
        productApi.update(barcode, p);
    }

    @Transactional(readOnly = true)
    public List<Product> search(String name, String barcode, String clientName, int page, int size) {
        return productApi.search(name, barcode, clientName, page, size);
    }

    @Transactional(readOnly = true)
    public Long getCount(String name, String barcode, String clientName) {
        return productApi.getCount(name, barcode, clientName);
    }

    @Transactional(readOnly = true)
    public String getClientName(Integer clientId) throws ApiException {
        if (clientId == null) {
            throw new ApiException("Client ID is missing for product");
        }

        Client client = clientApi.get(clientId); // <-- make sure this exists in ClientApi
        if (client == null) {
            throw new ApiException("Client not found: " + clientId);
        }

        return client.getName();
    }

    private Integer getClientIdByName(String clientName) throws ApiException {
        if (clientName == null || clientName.trim().isEmpty()) {
            throw new ApiException("Client name is required");
        }

        Client client = clientApi.getByName(clientName); // <-- you already have this
        if (client == null) {
            throw new ApiException("Client not found: " + clientName);
        }
        return client.getId();
    }
    //TODO: Why cant we fetch all the products in one query, how to solve N+1 problem
    //TODO: can use java streams instead of for loop
    public void addBulk(List<Product> products, List<String> clientNames) throws ApiException {

        if (products == null || clientNames == null) {
            throw new ApiException("Products/clientNames cannot be null");
        }
        if (products.size() != clientNames.size()) {
            throw new ApiException("Products and clientNames size mismatch");
        }

        // 1) collect unique client names
        java.util.Set<String> uniqueClientNames = new java.util.HashSet<>();
        for (String cn : clientNames) {
            if (cn == null || cn.trim().isEmpty()) {
                throw new ApiException("Client name is required");
            }
            uniqueClientNames.add(cn.trim());
        }

        // 2) batch fetch clients -> map name -> id
        List<Client> clients = clientApi.getByNames(new java.util.ArrayList<>(uniqueClientNames));
        java.util.Map<String, Integer> clientIdByName = new java.util.HashMap<>();
        for (Client c : clients) {
            clientIdByName.put(c.getName(), c.getId());
        }

        // 3) validate all client names exist
        for (String cn : uniqueClientNames) {
            if (!clientIdByName.containsKey(cn)) {
                throw new ApiException("Client not found: " + cn);
            }
        }

        // 4) collect barcodes + validate not empty + validate duplicates in upload itself
        java.util.Set<String> uploadBarcodes = new java.util.HashSet<>();
        for (Product p : products) {
            if (p == null) throw new ApiException("Product cannot be null");
            if (p.getBarcode() == null || p.getBarcode().trim().isEmpty()) {
                throw new ApiException("Product barcode cannot be empty");
            }
            String bc = p.getBarcode().trim();
            if (!uploadBarcodes.add(bc)) {
                throw new ApiException("Duplicate barcode in upload: " + bc);
            }
        }

        // 5) batch check existing products by barcode (instead of N times getByBarcode)
        List<Product> existing = productApi.getByBarcodes(new java.util.ArrayList<>(uploadBarcodes));
        if (!existing.isEmpty()) {
            // build a message with a few examples
            java.util.List<String> existingBarcodes = new java.util.ArrayList<>();
            for (Product e : existing) existingBarcodes.add(e.getBarcode());
            throw new ApiException("Some barcodes already exist: " + existingBarcodes);
        }

        // 6) finally insert (N inserts are expected)
        for (int i = 0; i < products.size(); i++) {
            Product p = products.get(i);
            String cn = clientNames.get(i).trim();

            p.setClientId(clientIdByName.get(cn));
            productApi.add(p); // now this will not hit DB for barcode existence ideally, but see note below
        }
    }

}