package com.pos.pojo;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.URL;

@Entity
@Getter
@Setter
public class ProductPojo extends AbstractPojo {

    // Overriding the ID to keep your IDENTITY strategy
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    protected Integer id;
    //indexing should be added here
    @Column(nullable = false)
    private String barcode;
    //indexing good to have, composite, order sensitive
    @Column(nullable = false)
    private Integer clientId;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private Double mrp;

    private String imageUrl;
}