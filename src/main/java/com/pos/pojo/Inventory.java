package com.pos.pojo;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(uniqueConstraints = {
        @UniqueConstraint(columnNames = {"productId"})
})
@Getter @Setter
public class Inventory extends AbstractPojo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    protected Integer id;
    // Inherits primary ID from AbstractPojo
    @Column(nullable = false)
    private Integer productId;

    @Column(nullable = false)
    private Integer quantity;
}

//sep direcctory for api exception
//app client exception - external comm