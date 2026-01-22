package com.pos.pojo;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "Inventory", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"productId"})
})
@Getter @Setter
public class InventoryPojo extends AbstractPojo {

    // Inherits primary ID from AbstractPojo
    @Column(nullable = false)
    private Integer productId;

    @Column(nullable = false)
    private Integer quantity;
}

//sep direcctory for api exception
//app client exception - external comm