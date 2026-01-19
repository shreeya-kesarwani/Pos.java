package com.pos.pojo;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
//remove pojo names
//id, productid to be added
//sep direcctory for api exception
//appclient exception - external comm
public class InventoryPojo extends AbstractPojo {

    @Column(nullable = false)
    @Min(value = 1)
    private Integer quantity;
}