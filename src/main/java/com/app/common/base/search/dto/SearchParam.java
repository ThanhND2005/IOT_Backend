package com.app.common.base.search.dto;

import com.app.common.base.search.enums.SearchDataType;
import com.app.common.base.search.enums.SearchOperation;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SearchParam {
    private String field;
    private Object value;
    private SearchOperation operate;
    private SearchDataType type;
}
