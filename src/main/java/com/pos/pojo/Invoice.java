package com.pos.pojo;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter @Setter
@Table(uniqueConstraints = {
        @UniqueConstraint(columnNames = {"orderId"})
})
public class Invoice extends AbstractPojo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    protected Integer id;

    @Column(nullable = false)
    private Integer orderId;
    //path can be added in orderPojo
    @Column(nullable = false)
    private String path;
}