package com.pos.service;

import com.pos.dao.InventoryDao;
import com.pos.exception.ApiException;
import com.pos.pojo.Inventory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(rollbackFor = ApiException.class) // Pattern match: Class-level transactions
public class InventoryService {

    @Autowired
    private InventoryDao inventoryDao;

    public void add(Inventory p) {
        // Simple insert as per minimalist requirement
        inventoryDao.insert(p);
    }

    @Transactional(readOnly = true)
    public Inventory getCheck(Integer id) throws ApiException {
        Inventory inventoryPojo = inventoryDao.select(id, Inventory.class);
        if (inventoryPojo == null) {
            throw new ApiException(String.format("Inventory record with ID %d does not exist", id));
        }
        return inventoryPojo;
    }

    @Transactional(readOnly = true)
    public List<Inventory> search(String barcode, String productName, String clientName) {
        return inventoryDao.search(barcode, productName, clientName);
    }

    @Transactional
    public Inventory selectByProductId(Integer productId) {
        return inventoryDao.selectByProductId(productId);
    }

    // Matching the ProductService pattern of having a search-based count if needed
    @Transactional(readOnly = true)
    public Long getCount() {
        return inventoryDao.count(Inventory.class);
    }

    @Transactional
    public void update(Integer id, Inventory p) throws ApiException {
        // You don't strictly need the ID if the Pojo has it, but to match your current flow:
        inventoryDao.update(p);
    }
}