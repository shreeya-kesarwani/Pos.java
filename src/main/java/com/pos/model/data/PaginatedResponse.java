package com.pos.model.data;

import java.util.List;

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

    public List<T> getData() { return data; }
    public void setData(List<T> data) { this.data = data; }
    public Long getTotalCount() { return totalCount; }
    public void setTotalCount(Long totalCount) { this.totalCount = totalCount; }
    public Integer getPageNo() { return pageNo; }
    public void setPageNo(Integer pageNo) { this.pageNo = pageNo; }
}