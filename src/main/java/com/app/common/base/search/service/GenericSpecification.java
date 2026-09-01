package com.app.common.base.search.service;

import com.app.common.base.search.dto.SearchParam;
import com.app.common.base.search.enums.SearchDataType;
import com.app.common.base.search.enums.SearchOperation;
import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;
public class GenericSpecification<T> implements Specification<T> {
    private final List<SearchParam> params;

    public GenericSpecification() {
        this.params = new ArrayList<>();
    }

    public GenericSpecification(List<SearchParam> params){
        this.params = params != null ? new ArrayList<>(params) : new ArrayList<>();
    }

    public void add(SearchParam param) {
        if (param != null) {
            this.params.add(param);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public Predicate toPredicate(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder builder) {
        List<Predicate> predicates = new ArrayList<>();

        if (params != null) {
            for (SearchParam param : params) {
                if (param == null || param.getField() == null || param.getField().isBlank()) {
                    continue;
                }
                if (param.getOperate() == null) {
                    continue;
                }
                if (param.getValue() == null
                        && param.getOperate() != SearchOperation.IS_NULL
                        && param.getOperate() != SearchOperation.IS_NOT_NULL) {
                    continue;
                }

                try {
                    Path<?> path = root;
                    if (param.getField().contains(".")) {
                        String[] parts = param.getField().split("\\.");
                        From<?, ?> from = root;
                        for (int i = 0; i < parts.length - 1; i++) {
                            from = from.join(parts[i], JoinType.LEFT);
                        }
                        path = from.get(parts[parts.length - 1]);
                    } else {
                        path = root.get(param.getField());
                    }

                    Object castedValue = castValue(param.getType(), param.getValue(), path.getJavaType());

                    switch (param.getOperate()) {
                        case EQUAL -> predicates.add(builder.equal(path, castedValue));
                        case LIKE -> predicates.add(builder.like(builder.lower(path.as(String.class)), "%" + String.valueOf(castedValue).toLowerCase() + "%"));
                        case IN -> {
                            CriteriaBuilder.In<Object> inClause = builder.in(path);
                            if (castedValue instanceof List<?> list) {
                                list.forEach(inClause::value);
                            } else if (castedValue != null) {
                                inClause.value(castedValue);
                            }
                            predicates.add(inClause);
                        }
                        case GREATER_THAN -> predicates.add(builder.greaterThan((Expression<Comparable>) path, (Comparable) castedValue));
                        case NOT_EQUAL -> predicates.add(builder.notEqual(path, castedValue));
                        case IS_NULL -> predicates.add(builder.isNull(path));
                        case LESS_THAN -> predicates.add(builder.lessThan((Expression<Comparable>) path, (Comparable) castedValue));
                        case IS_NOT_NULL -> predicates.add(builder.isNotNull(path));
                        case LESS_THAN_EQUAL -> predicates.add(builder.lessThanOrEqualTo((Expression<Comparable>) path, (Comparable) castedValue));
                        case GREATER_THAN_EQUAL -> predicates.add(builder.greaterThanOrEqualTo((Expression<Comparable>) path, (Comparable) castedValue));
                    }
                } catch (Exception e) {
                    // Ignore invalid field path or conversion failure gracefully
                }
            }
        }
        query.distinct(true);
        return builder.and(predicates.toArray(new Predicate[0]));
    }

    @SuppressWarnings("unchecked")
    private Object castValue(SearchDataType type, Object value, Class<?> javaType) {
        if (value == null || type == null) {
            return value;
        }
        String valStr = value.toString();

        return switch (type) {
            case STRING -> valStr;
            case NUMBER -> {
                if (javaType.equals(Long.class) || javaType.equals(long.class)) yield Long.valueOf(valStr);
                if (javaType.equals(Integer.class) || javaType.equals(int.class)) yield Integer.valueOf(valStr);
                if (javaType.equals(Double.class) || javaType.equals(double.class)) yield Double.valueOf(valStr);
                if (javaType.equals(Float.class) || javaType.equals(float.class)) yield Float.valueOf(valStr);
                if (javaType.equals(Short.class) || javaType.equals(short.class)) yield Short.valueOf(valStr);
                if (javaType.equals(java.math.BigDecimal.class)) yield new java.math.BigDecimal(valStr);
                if (javaType.equals(java.math.BigInteger.class)) yield new java.math.BigInteger(valStr);
                yield Double.valueOf(valStr);
            }
            case UUID -> {
                if (javaType.equals(java.util.UUID.class)) yield java.util.UUID.fromString(valStr);
                yield valStr;
            }
            case BOOLEAN -> Boolean.valueOf(valStr);
            case ENUM -> Enum.valueOf((Class<Enum>) javaType, valStr);
            case DATE -> parseDateValue(valStr, javaType);
            default -> valStr;
        };
    }

    private Object parseDateValue(String valStr, Class<?> javaType) {
        if (javaType.equals(java.time.LocalDate.class)) {
            return java.time.LocalDate.parse(valStr);
        }
        if (javaType.equals(java.time.LocalDateTime.class)) {
            if (valStr.contains(" ")) {
                return java.time.LocalDateTime.parse(valStr, java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            }
            if (!valStr.contains("T")) {
                return java.time.LocalDate.parse(valStr).atStartOfDay();
            }
            return java.time.LocalDateTime.parse(valStr);
        }
        if (javaType.equals(java.time.OffsetDateTime.class)) {
            if (valStr.contains(" ")) {
                return java.time.LocalDateTime.parse(valStr, java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                        .atOffset(java.time.ZoneOffset.UTC);
            }
            if (!valStr.contains("T")) {
                return java.time.LocalDate.parse(valStr).atStartOfDay().atOffset(java.time.ZoneOffset.UTC);
            }
            return java.time.OffsetDateTime.parse(valStr);
        }
        if (javaType.equals(java.time.Instant.class)) {
            return java.time.Instant.parse(valStr);
        }
        return valStr;
    }

}
