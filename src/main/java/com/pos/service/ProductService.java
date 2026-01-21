package com.pos.service;

import com.pos.dao.ProductDao;
import com.pos.pojo.ProductPojo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProductService {

    @Autowired
    private ProductDao productDao;

    @Transactional(rollbackFor = ApiException.class)
    public void add(ProductPojo p) throws ApiException {
        // Validation: Exact barcode check using our specialized DAO method
        if (productDao.selectByBarcode(p.getBarcode()) != null) {
            throw new ApiException("Product with this barcode already exists: " + p.getBarcode());
        }
        productDao.insert(p);
    }

    @Transactional(rollbackFor = ApiException.class)
    public void update(Integer id, ProductPojo p) throws ApiException {
        ProductPojo existing = getCheck(id);

        // Update all fields that are allowed to change
        existing.setName(p.getName());
        existing.setMrp(p.getMrp());
        existing.setClientId(p.getClientId());
        existing.setImageUrl(p.getImageUrl());
        // Note: Barcode is usually NOT allowed to change after creation to maintain order history

        productDao.update(existing);
    }

    // Change access modifier from public to protected
    @Transactional(readOnly = true)
    protected ProductPojo getCheck(Integer id) throws ApiException {
        ProductPojo productPojo = productDao.selectById(id, ProductPojo.class);
        if (productPojo == null) {
            throw new ApiException("Product with ID " + id + " does not exist");
        }
        return productPojo;
    }
    @Transactional(readOnly = true)
    public List<ProductPojo> search(String name, String barcode, Integer clientId) {
        return productDao.search(name, barcode, clientId);
    }

    @Transactional(readOnly = true)
    public ProductPojo getByBarcode(String barcode) throws ApiException {
        ProductPojo p = productDao.selectByBarcode(barcode);
        if (p == null) {
            throw new ApiException("Product with barcode " + barcode + " not found");
        }
        return p;
    }

    // Standard helpers for UI Tables
    @Transactional(readOnly = true)
    public List<ProductPojo> getPaged(int page, int size) {
        return productDao.selectAllPaged(ProductPojo.class, page, size);
    }

    @Transactional(readOnly = true)
    public Long getCount() {
        return productDao.count(ProductPojo.class);
    }
}