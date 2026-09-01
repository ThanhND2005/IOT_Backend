package com.app.common.base.search.specification;

import com.app.common.base.search.dto.SearchParam;
import com.app.common.base.search.service.GenericSpecification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public abstract class DynamicSearchService<T> {
    protected abstract JpaSpecificationExecutor<T> getRepository();
    public Page<T> dynamicSearch(List<SearchParam> params, Pageable pageable) {
        return dynamicSearch(params,null, pageable);
    }

    public Page<T> dynamicSearch(List<SearchParam> params, Specification<T> baseSpec, Pageable pageable) {
        List<SearchParam> safeParams = params != null ? params : List.of();
        Specification<T> spec = new GenericSpecification<>(safeParams);
        if(baseSpec != null) {
            spec = baseSpec.and(spec);
        }
        Pageable safePageable = (pageable != null && pageable.getSort().isSorted())
                ? pageable
                : PageRequest.of(
                        pageable != null ? pageable.getPageNumber() : 0,
                        pageable != null ? pageable.getPageSize() : 20,
                        Sort.by(Sort.Direction.DESC, "createdAt"));
        return getRepository().findAll(spec,safePageable);
    }
}
