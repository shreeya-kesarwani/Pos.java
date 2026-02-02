package com.pos.api;

import com.pos.dao.ProductDao;
import com.pos.exception.ApiException;
import com.pos.pojo.Product;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(rollbackFor = ApiException.class)
public class ProductApi {

    @Autowired private ProductDao productDao;


    @Transactional(readOnly = true)
    public Product get(Integer id) {
        return productDao.select(id, Product.class);
    }

    @Transactional(readOnly = true)
    public Product getCheck(Integer id) throws ApiException {
        Product p = get(id);
        if (p == null) throw new ApiException("Product not found: " + id);
        return p;
    }

    @Transactional(readOnly = true)
    public List<Product> getByIds(List<Integer> ids) {
        if (ids == null || ids.isEmpty()) return List.of();
        return productDao.selectByIds(ids);
    }

    @Transactional(readOnly = true)
    public List<Product> getByBarcodes(List<String> barcodes) {
        if (barcodes == null || barcodes.isEmpty()) return List.of();
        return productDao.selectByBarcodes(barcodes);
    }

    @Transactional(readOnly = true)
    public List<Product> getCheckByBarcodes(List<String> barcodes) throws ApiException {
        if (barcodes == null || barcodes.isEmpty()) {
            throw new ApiException("At least one barcode is required");
        }

        List<Product> found = productDao.selectByBarcodes(barcodes);
        Set<String> foundSet = found.stream()
                .map(Product::getBarcode)
                .collect(Collectors.toSet());

        List<String> missing = barcodes.stream()
                .distinct()
                .filter(b -> !foundSet.contains(b))
                .toList();

        if (!missing.isEmpty()) {
            throw new ApiException("Products not found for barcodes: " + missing);
        }
        return found;
    }

    @Transactional(readOnly = true)
    public Product getByBarcode(String barcode) {
        List<Product> list = productDao.selectByBarcodes(List.of(barcode));
        return list.isEmpty() ? null : list.get(0);
    }

    @Transactional(readOnly = true)
    public Product getCheckByBarcode(String barcode) throws ApiException {
        Product p = getByBarcode(barcode);
        if (p == null) throw new ApiException("Product not found for barcode: " + barcode);
        return p;
    }

    public void add(Product product) throws ApiException {
        Product existing = getByBarcode(product.getBarcode());
        if (existing != null) {
            throw new ApiException("Product with barcode already exists: " + product.getBarcode());
        }
        productDao.insert(product);
    }

    public void addBulk(List<Product> products) throws ApiException {
        if (products == null || products.isEmpty()) return;

        Set<String> uploadBarcodes = new HashSet<>();
        for (Product p : products) {
            if (!uploadBarcodes.add(p.getBarcode())) {
                throw new ApiException("Duplicate barcode in upload: " + p.getBarcode());
            }
        }

        List<Product> existing = getByBarcodes(new ArrayList<>(uploadBarcodes));
        if (!existing.isEmpty()) {
            List<String> existingBarcodes = existing.stream().map(Product::getBarcode).toList();
            throw new ApiException("Some barcodes already exist: " + existingBarcodes);
        }

        for (Product p : products) {
            productDao.insert(p);
        }
    }

    public void update(String barcode, Product product) throws ApiException {
        Product existing = getCheckByBarcode(barcode);
        if (!product.getBarcode().equals(existing.getBarcode())) {
            Product other = getByBarcode(product.getBarcode());
            if (other != null && !other.getId().equals(existing.getId())) {
                throw new ApiException("Product with barcode already exists: " + product.getBarcode());
            }
            existing.setBarcode(product.getBarcode());
        }

        existing.setName(product.getName());
        existing.setMrp(product.getMrp());
        existing.setImageUrl(product.getImageUrl());
        existing.setClientId(product.getClientId());
    }

    @Transactional(readOnly = true)
    public List<Product> search(String name, String barcode, String clientName, int page, int size) {
        return productDao.search(name, barcode, clientName, page, size);
    }

    @Transactional(readOnly = true)
    public Long getCount(String name, String barcode, String clientName) {
        return productDao.getCount(name, barcode, clientName);
    }

    public void validateSellingPrice(Integer productId, Double sellingPrice) throws ApiException {
        Product product = getCheck(productId);
        if (sellingPrice > product.getMrp()) {
            throw new ApiException("Selling price cannot exceed MRP for product: " + product.getName());
        }
    }
}
