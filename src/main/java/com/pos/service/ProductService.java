package com.pos.service;

import com.pos.dao.ProductDao;
import com.pos.pojo.ProductPojo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(rollbackFor = ApiException.class) // Task 8: Class-level transactions
public class ProductService {

    @Autowired
    private ProductDao productDao;

    public void add(ProductPojo p) throws ApiException {
        if (findByBarcode(p.getBarcode()) != null) {
            throw new ApiException(String.format("Product with barcode [%s] already exists", p.getBarcode()));
        }
        productDao.insert(p);
    }

    public void update(String barcode, ProductPojo p) throws ApiException {
        List<ProductPojo> results = productDao.search(null, barcode, null, 0, 1);

        if (results.isEmpty()) {
            throw new ApiException("Product with barcode [" + barcode + "] not found");
        }
        ProductPojo existing = results.get(0);
        existing.setName(p.getName());
        existing.setMrp(p.getMrp());
        existing.setImageUrl(p.getImageUrl());
    }

    @Transactional(readOnly = true)
    public ProductPojo getCheck(Integer id) throws ApiException {
        ProductPojo productPojo = productDao.select(id, ProductPojo.class);
        if (productPojo == null) {
            throw new ApiException(String.format("Product with ID %d does not exist", id));
        }
        return productPojo;
    }

    @Transactional(readOnly = true)
    public List<ProductPojo> search(String name, String barcode, String clientName, int page, int size) {
        return productDao.search(name, barcode, clientName, page, size);
    }

    @Transactional(readOnly = true)
    public Long getCount(String name, String barcode, String clientName) {
        return productDao.getCount(name, barcode, clientName);
    }

    // Private validation helper
    @Transactional(readOnly = true)
    private ProductPojo findByBarcode(String barcode) {
        List<ProductPojo> results = productDao.search(null, barcode, null, 0, 1);
        return results.isEmpty() ? null : results.get(0);
    }
}