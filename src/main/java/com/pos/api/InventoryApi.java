package com.pos.api;

import com.pos.dao.InventoryDao;
import com.pos.exception.ApiException;
import com.pos.pojo.Inventory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

import static com.pos.model.constants.ErrorMessages.*;

@Service
@Transactional(rollbackFor = ApiException.class)
public class InventoryApi {

    @Autowired
    private InventoryDao inventoryDao;

    @Transactional(readOnly = true)
    public Inventory get(Integer id) {
        return inventoryDao.selectById(id);
    }

    @Transactional(readOnly = true)
    public Inventory getCheck(Integer id) throws ApiException {
        Inventory inv = get(id);
        if (inv == null) {
            throw new ApiException(INVENTORY_NOT_FOUND.value() + ": " + id);
        }
        return inv;
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

    public void add(List<Inventory> inventories, List<String> barcodes, Map<String, Integer> productIdByBarcode)
            throws ApiException {

        attachProductIds(inventories, barcodes, productIdByBarcode);

        for (Inventory inventory : inventories) {
            Inventory existing = inventoryDao.selectByProductId(inventory.getProductId());
            if (existing == null) {
                inventoryDao.insert(inventory);
            } else {
                existing.setQuantity(inventory.getQuantity());
            }
        }
    }

    public void update(Integer id, Inventory inventory) throws ApiException {
        Inventory existing = getCheck(id);
        existing.setQuantity(inventory.getQuantity());
    }

    @Transactional(readOnly = true)
    public List<Inventory> search(String barcode, String productName, int page, int size) {
        return inventoryDao.search(barcode, productName, page, size);
    }

    @Transactional(readOnly = true)
    public Long getCount(String barcode, String productName) {
        return inventoryDao.getCount(barcode, productName);
    }

    public void allocate(Integer productId, Integer quantity) throws ApiException {
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

    private void attachProductIds(
            List<Inventory> inventories,
            List<String> barcodes,
            Map<String, Integer> productIdByBarcode
    ) throws ApiException {

        for (int i = 0; i < inventories.size(); i++) {
            String barcode = barcodes.get(i);
            Integer productId = productIdByBarcode.get(barcode);

            if (productId == null) {
                throw new ApiException(PRODUCT_NOT_FOUND_FOR_BARCODE.value() + ": " + barcode);
            }

            inventories.get(i).setProductId(productId);
        }
    }
}
