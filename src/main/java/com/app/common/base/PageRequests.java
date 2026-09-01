package com.app.common.base;


import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.PageRequest;

public final class PageRequests {
    public static final  int DEFAULT_LIMIT = 20;

    private PageRequests(){

    }

    public static Pageable of(Integer page, Integer pageSize) {
        return of(page, pageSize, Sort.by(Sort.Direction.DESC,"createdAt"));
    }

    public static Pageable of(Integer page, Integer pageSize, Sort sort) {
        int safePageSize = pageSize == null || pageSize < 1 ? DEFAULT_LIMIT : pageSize;
        int safePage = page == null || page < 0 ? 0 : page;
        return sort != null && sort.isSorted()
                ? PageRequest.of(safePage, safePageSize, sort)
                : PageRequest.of(safePage, safePageSize);
    }


}
