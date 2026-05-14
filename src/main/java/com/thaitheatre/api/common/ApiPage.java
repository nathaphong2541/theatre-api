package com.thaitheatre.api.common;

import java.util.List;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ApiPage<T> {

    private List<T> items;
    private long total;
    private int page;
    private int size;
    private int totalPages;

    public ApiPage(List<T> items, long total, int page, int size, int totalPages) {
        this.items = items;
        this.total = total;
        this.page = page;
        this.size = size;
        this.totalPages = totalPages;
    }
}
