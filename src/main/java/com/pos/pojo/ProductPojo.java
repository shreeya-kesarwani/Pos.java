package com.pos.pojo;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "Product")
@Getter
@Setter
public class ProductPojo extends AbstractPojo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    protected Integer id;

    @Column(nullable = false, unique = true) // Barcode should be unique
    private String barcode;

    @Column(nullable = false)
    private Integer clientId;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private Double mrp;

    @Column(nullable = true)
    private String imageUrl;
}