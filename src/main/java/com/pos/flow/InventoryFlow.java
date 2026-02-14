package com.pos.flow;

import com.pos.api.InventoryApi;
import com.pos.api.ProductApi;
import com.pos.exception.ApiException;
import com.pos.pojo.Inventory;
import com.pos.pojo.Product;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.pos.model.constants.ErrorMessages.*;

@Component
@Transactional(rollbackFor = Exception.class)
public class InventoryFlow {

    @Autowired private InventoryApi inventoryApi;
    @Autowired private ProductApi productApi;

    @Transactional(readOnly = true)
    public List<Inventory> searchInventories(String barcode, String productName, int page, int pageSize) throws ApiException {
        List<Integer> productIds = productApi.findProductIdsByBarcodeOrName(barcode, productName);
        return inventoryApi.findByProductIds(productIds, page, pageSize);
    }

    @Transactional(readOnly = true)
    public long getSearchCount(String barcode, String productName) throws ApiException {
        List<Integer> productIds = productApi.findProductIdsByBarcodeOrName(barcode, productName);
        return inventoryApi.getCountByProductIds(productIds);
    }
}
