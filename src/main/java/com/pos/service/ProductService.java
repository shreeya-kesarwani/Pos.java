package com.pos.service;

import com.pos.dao.ProductDao;
import com.pos.exception.ApiException;
import com.pos.pojo.Product;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(rollbackFor = ApiException.class) // Task 8: Class-level transactions
public class ProductService {

    @Autowired
    private ProductDao productDao;

    public void add(Product p) throws ApiException {
        if (findByBarcode(p.getBarcode()) != null) {
            throw new ApiException(String.format("Product with barcode [%s] already exists", p.getBarcode()));
        }
        productDao.insert(p);
    }

    public void update(String barcode, Product p) throws ApiException {
        List<Product> results = productDao.search(null, barcode, null, 0, 1);

        if (results.isEmpty()) {
            throw new ApiException("Product with barcode [" + barcode + "] not found");
        }
        Product existing = results.get(0);
        existing.setName(p.getName());
        existing.setMrp(p.getMrp());
        existing.setImageUrl(p.getImageUrl());
    }

    @Transactional(readOnly = true)
    public Product getCheck(Integer id) throws ApiException {
        Product productPojo = productDao.select(id, Product.class);
        if (productPojo == null) {
            throw new ApiException(String.format("Product with ID %d does not exist", id));
        }
        return productPojo;
    }

    @Transactional(readOnly = true)
    public List<Product> search(String name, String barcode, String clientName, int page, int size) {
        return productDao.search(name, barcode, clientName, page, size);
    }

    @Transactional(readOnly = true)
    public Long getCount(String name, String barcode, String clientName) {
        return productDao.getCount(name, barcode, clientName);
    }

    // Private validation helper
    @Transactional(readOnly = true)
    private Product findByBarcode(String barcode) {
        List<Product> results = productDao.search(null, barcode, null, 0, 1);
        return results.isEmpty() ? null : results.get(0);
    }
}