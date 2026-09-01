package com.app.common.wrapper;


import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Metadata {
    private Integer page;
    private Integer pageSize;
    private Long totalItems;
    private Integer totalPages;
    private Boolean hasNext;
    private Boolean hasPrevious;
    private Object extra;

    public static <T> Metadata pagination(Page<T> page){
        return Metadata.builder()
                .page(page.getNumber())
                .pageSize(page.getSize())
                .totalItems(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .hasNext(page.hasNext())
                .hasPrevious(page.hasPrevious())
                .build();
    }

    public static Metadata pagination(int page, int pageSize, long totalItems) {
        int totalPages = (int) Math.ceil((double) totalItems / pageSize);
        return Metadata.builder()
                .page(page)
                .pageSize(pageSize)
                .totalItems(totalItems)
                .totalPages(totalPages)
                .hasNext(page < totalPages - 1)
                .hasPrevious(page > 0)
                .build();
    }

    public static Metadata of(Object extra) {
        return Metadata.builder()
                .extra(extra)
                .build();
    }

    public static Metadata empty() {
        return Metadata.builder().build();
    }
}
