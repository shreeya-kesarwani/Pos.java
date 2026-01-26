package com.pos.api;

import com.pos.dao.ProductDao;
import com.pos.exception.ApiException;
import com.pos.pojo.Product;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(rollbackFor = ApiException.class)
public class ProductApi {

    @Autowired
    private ProductDao productDao;

    // -------------------------------------------------
    // BASIC GET / GET-CHECK
    // -------------------------------------------------

    public Product get(Integer id) {
        return productDao.select(id, Product.class);
    }

    public Product getCheck(Integer id) throws ApiException {
        Product p = get(id);
        if (p == null) {
            throw new ApiException(String.format("Product with ID %d does not exist", id));
        }
        return p;
    }

    public Product getByBarcode(String barcode) {
        return productDao.selectByBarcode(barcode);
    }

    public Product getCheckByBarcode(String barcode) throws ApiException {
        Product p = getByBarcode(barcode);
        if (p == null) {
            throw new ApiException(String.format("Product with barcode [%s] not found", barcode));
        }
        return p;
    }

    // -------------------------------------------------
    // ✅ HELPERS (barcode ↔ productId)
    // -------------------------------------------------

    /**
     * Resolve barcode → productId
     */
    public Integer getIdByBarcode(String barcode) throws ApiException {
        Product p = getCheckByBarcode(barcode);
        return p.getId();
    }

    /**
     * Resolve productId → barcode
     */
    public String getBarcodeById(Integer productId) throws ApiException {
        Product p = getCheck(productId);
        return p.getBarcode();
    }

    /**
     * Resolve productId → product name
     */
    public String getNameById(Integer productId) throws ApiException {
        Product p = getCheck(productId);
        return p.getName();
    }

    // -------------------------------------------------
    // CORE LOGIC
    // -------------------------------------------------

    public void add(Product p) throws ApiException {
        if (getByBarcode(p.getBarcode()) != null) {
            throw new ApiException(
                    String.format("Product with barcode [%s] already exists", p.getBarcode())
            );
        }
        productDao.insert(p);
    }

    public void update(String barcode, Product p) throws ApiException {
        Product existing = getCheckByBarcode(barcode);
        existing.setName(p.getName());
        existing.setMrp(p.getMrp());
        // add other mutable fields if needed
    }

    @Transactional(readOnly = true)
    public List<Product> search(String name, String barcode, String clientName, int page, int size) {
        return productDao.search(name, barcode, clientName, page, size);
    }

    @Transactional(readOnly = true)
    public Long getCount(String name, String barcode, String clientName) {
        return productDao.getCount(name, barcode, clientName);
    }
}
