package com.panda.springbootjpaquerydsl.config.jpa.search;


import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

/**
 * @author YIN
 */
public class SearchSpecification<T> implements Specification<T> {

    private final SpecSearchCriteria criteria;

    public SearchSpecification(final SpecSearchCriteria criteria) {
        super();
        this.criteria = criteria;
    }

    public SpecSearchCriteria getCriteria() {
        return criteria;
    }

    @Override
    public Predicate toPredicate(final Root<T> root, final CriteriaQuery<?> query, final CriteriaBuilder builder) {
        switch (criteria.getOperation()) {
            case EQUALITY:
                return builder.equal(root.get(criteria.getKey()), criteria.getValue());
            case NEGATION:
                return builder.notEqual(root.get(criteria.getKey()), criteria.getValue());
            case GREATER_THAN:
                return builder.greaterThan(root.get(criteria.getKey()), criteria.getValue().toString());
            case LESS_THAN:
                return builder.lessThan(root.get(criteria.getKey()), criteria.getValue().toString());
            case LIKE:
                return builder.like(root.get(criteria.getKey()).as(String.class), criteria.getValue().toString());
            case STARTS_WITH:
                return builder.like(root.get(criteria.getKey()).as(String.class), criteria.getValue() + "%");
            case ENDS_WITH:
                return builder.like(root.get(criteria.getKey()).as(String.class), "%" + criteria.getValue());
            case CONTAINS:
                return builder.like(root.get(criteria.getKey()).as(String.class), "%" + criteria.getValue() + "%");
            default:
                return null;
        }
    }
}

