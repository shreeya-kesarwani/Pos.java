package com.pos.api;

import com.pos.dao.InventoryDao;
import com.pos.exception.ApiException;
import com.pos.pojo.Inventory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.pos.model.constants.ErrorMessages.*;

@Service
@Transactional(rollbackFor = Exception.class)
public class InventoryApi {

    @Autowired
    private InventoryDao inventoryDao;

    @Transactional(readOnly = true)
    public Inventory get(Integer id) {
        return inventoryDao.selectById(id);
    }

    @Transactional(readOnly = true)
    public Inventory getCheck(Integer id) throws ApiException {
        Inventory inventory = get(id);
        if (inventory == null) {
            throw new ApiException(INVENTORY_NOT_FOUND.value() + ": " + id);
        }
        return inventory;
    }

    @Transactional(readOnly = true)
    public Inventory getByProductId(Integer productId) {
        return inventoryDao.selectByProductId(productId);
    }

    @Transactional(readOnly = true)
    public Inventory getCheckByProductId(Integer productId) throws ApiException {
        Inventory inventory = getByProductId(productId);
        if (inventory == null) {
            throw new ApiException(INVENTORY_NOT_FOUND_FOR_PRODUCT.value() + ": " + productId);
        }
        return inventory;
    }

    @Transactional(readOnly = true)
    public List<Inventory> findByProductIds(List<Integer> productIds, int page, int size) {
        return inventoryDao.findByProductIds(productIds, page, size);
    }

    public void add(List<Inventory> inventories) throws ApiException {
        for (Inventory inventory : inventories) {
            Integer productId = inventory.getProductId();
            Inventory existing = inventoryDao.selectByProductId(productId);
            if (existing == null) {
                inventoryDao.insert(inventory);
            } else {
                existing.setQuantity(inventory.getQuantity());
            }
        }
    }

    public void reduceInventory(Integer productId, Integer quantity) throws ApiException {
        if (quantity == null || quantity <= 0) {
            throw new ApiException(QUANTITY_MUST_BE_POSITIVE.value() + ": " + quantity);
        }
        Inventory inventory = getCheckByProductId(productId);
        if (inventory.getQuantity() < quantity) {
            throw new ApiException(
                    INSUFFICIENT_INVENTORY.value() +
                            " | productId=" + productId +
                            ", available=" + inventory.getQuantity() +
                            ", requested=" + quantity
            );
        }
        inventory.setQuantity(inventory.getQuantity() - quantity);
    }

    public Long getCountByProductIds(List<Integer> productIds) {
        return inventoryDao.getCountByProductIds(productIds);
    }

}