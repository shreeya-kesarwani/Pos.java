package com.pos.model.form;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.ZonedDateTime;

@Getter
@Setter
public class DaySalesForm {

    @NotNull
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private ZonedDateTime startDate;
//
//    @NotNull
//    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
//    private ZonedDateTime endDate;
}
