package com.pos.model.form;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ClientSearchForm {

    private static final int MAX_PAGE_SIZE = 100;

    private Integer id;

    private String name;

    private String email;

    private Integer pageNumber = 0;

    @Max(MAX_PAGE_SIZE)
    private Integer pageSize = 10;
}