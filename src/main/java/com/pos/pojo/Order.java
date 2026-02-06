package com.pos.pojo;

import com.pos.model.constants.OrderStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(
        indexes = {
                @Index(name = "pos_order_status_idx", columnList = "status")
        }
)
public class Order extends AbstractPojo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status;

    private String invoicePath;
}
