package com.pos.api;

import com.pos.dao.InventoryDao;
import com.pos.exception.ApiException;
import com.pos.pojo.Inventory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@Transactional(rollbackFor = ApiException.class)
public class InventoryApi {

    @Autowired
    private InventoryDao inventoryDao;

    public Inventory get(Integer id) {
        return inventoryDao.select(id, Inventory.class);
    }

    public Inventory getCheck(Integer id) throws ApiException {
        Inventory inventory = get(id);
        if (inventory == null) {
            throw new ApiException(String.format("Inventory record ID %d not found", id));
        }
        return inventory;
    }

    public Inventory getByProductId(Integer productId) {
        return inventoryDao.selectByProductId(productId);
    }

    public Inventory getCheckByProductId(Integer productId) throws ApiException {
        Inventory inventory = getByProductId(productId);
        if (inventory == null) {
            throw new ApiException(String.format("Inventory for Product ID %d not found", productId));
        }
        return inventory;
    }

    public void add(Inventory inventory) {
        inventoryDao.insert(inventory);
    }

    public void update(Integer id, Inventory inventory) throws ApiException {
        Inventory existing = getCheck(id);
        existing.setQuantity(inventory.getQuantity());
    }

    @Transactional(readOnly = true)
    public List<Inventory> search(String barcode, String productName, String clientName, int page, int size) {
        return inventoryDao.search(barcode, productName, clientName, page, size);
    }

    @Transactional(readOnly = true)
    public Long getCount(String barcode, String productName, String clientName) {
        return inventoryDao.getCount(barcode, productName, clientName);
    }

    public void allocate(Integer productId, Integer quantity) throws ApiException {
        if (productId == null) {
            throw new ApiException("Product id cannot be null");
        }
        if (quantity == null || quantity <= 0) {
            throw new ApiException("Quantity must be > 0");
        }
        //todo can use getCheckbyprodid ->no neeed for this if validation again
        Inventory inventory = getByProductId(productId);
        if (inventory == null) {
            throw new ApiException("Inventory not found for productId: " + productId);
        }
        if (inventory.getQuantity() < quantity) {
            throw new ApiException("Insufficient inventory for productId: " + productId);
        }

        inventory.setQuantity(inventory.getQuantity() - quantity);
//        // your update() copies quantity into managed entity (dirty checking will persist it)
//        update(inventory.getId(), inventory);
    }


}