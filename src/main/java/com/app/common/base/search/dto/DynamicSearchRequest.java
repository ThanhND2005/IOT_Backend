package com.app.common.base.search.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DynamicSearchRequest {
    private List<SearchParam> filters;
    private String sortBy;
    private String sortDirection;
}
