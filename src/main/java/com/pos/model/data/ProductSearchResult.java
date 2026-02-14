package com.pos.model.data;

import com.pos.pojo.Product;
import java.util.List;

public record ProductSearchResult(
        List<Product> products,
        Long total
) {}
