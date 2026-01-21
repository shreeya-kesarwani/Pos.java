package com.pos.service;

import com.pos.dao.InventoryDao;
import com.pos.pojo.InventoryPojo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class InventoryService {

    @Autowired
    private InventoryDao inventoryDao;

    @Transactional(rollbackFor = ApiException.class)
    public void update(Integer productId, Integer quantity) throws ApiException {
        InventoryPojo inventoryPojo = inventoryDao.selectByProductId(productId);

        if (inventoryPojo == null) {
            inventoryPojo = new InventoryPojo();
            inventoryPojo.setProductId(productId);
            inventoryPojo.setQuantity(quantity);
            inventoryDao.insert(inventoryPojo);
        } else {
            inventoryPojo.setQuantity(quantity);
            inventoryDao.update(inventoryPojo);
        }
    }

    @Transactional(readOnly = true)
    public List<InventoryPojo> getAll() {
        return inventoryDao.selectAll(InventoryPojo.class);
    }

    @Transactional(readOnly = true)
    public InventoryPojo get(Integer productId) throws ApiException {
        InventoryPojo inventoryPojo = inventoryDao.selectById(productId, InventoryPojo.class);
        if (inventoryPojo == null) {
            throw new ApiException("Inventory record missing for Product ID: " + productId);
        }
        return inventoryPojo;
    }
    @Transactional(readOnly = true)
    public Integer getQuantitySafe(Integer id) {
        try {
            return get(id).getQuantity();
        } catch (Exception exception) {
            return 0;
        }
    }

    @Transactional(readOnly = true)
    public List<InventoryPojo> getPaged(int page, int size) {
        return inventoryDao.selectAllPaged(InventoryPojo.class, page, size);
    }

    @Transactional(readOnly = true)
    public List<InventoryPojo> search(String barcode, String productName, String clientName) {
        return inventoryDao.search(barcode, productName, clientName); //
    }
}