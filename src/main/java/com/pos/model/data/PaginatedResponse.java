package com.pos.model.data;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class PaginatedResponse<T> {
    private List<T> data;
    private Long totalCount;
    private Integer pageNo; // Task 1

    public static <T> PaginatedResponse<T> of(List<T> data, Long totalCount, Integer pageNo) {
        PaginatedResponse<T> res = new PaginatedResponse<>();
        res.data = data;
        res.totalCount = totalCount;
        res.pageNo = pageNo;
        return res;
    }

}