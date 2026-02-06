package com.pos.pojo;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(uniqueConstraints = {
        @UniqueConstraint(name = "pos_product_barcode_uk", columnNames = {"barcode"})
})
public class Product extends AbstractPojo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    protected Integer id;

    @Column(nullable = false)
    private String barcode;

    @Column(nullable = false)
    private Integer clientId;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private Double mrp;

    private String imageUrl;
}