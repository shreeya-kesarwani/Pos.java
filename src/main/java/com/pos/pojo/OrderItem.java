package com.pos.pojo;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(uniqueConstraints = {
        @UniqueConstraint(columnNames = {"orderId","productId"})
})
public class OrderItem extends AbstractPojo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    protected Integer id;
    //composite of orderId and ProductId should be unique
    @Column(nullable = false)
    private Integer orderId;

    @Column(nullable = false)
    private Integer productId;

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false)
    private Double sellingPrice;
}
