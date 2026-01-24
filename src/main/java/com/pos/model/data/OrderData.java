package com.pos.model.data;

import lombok.Getter;
import lombok.Setter;
import java.time.ZonedDateTime;

@Getter
@Setter
public class OrderData {

    private Integer id;
    private ZonedDateTime createdAt;
    private String status;
    private Double totalAmount;
}
