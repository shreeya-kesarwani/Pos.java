package com.pos.model.data;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProductData {
    private Integer id;
    private String barcode;
    private String name;
    private Double mrp;
    private Integer clientId;
    private String imageUrl;
}