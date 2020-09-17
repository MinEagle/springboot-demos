package com.panda.springbootjpaquerydsl.config.jpa.search;

import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author YIN
 */
public class SpecificationsBuilder<T> {

    private final List<SpecSearchCriteria> params;

    public SpecificationsBuilder() {
        params = new ArrayList<>();
    }

    // API

    public final SpecificationsBuilder<T> with(final String key, final String operation, final Object value, final String prefix, final String suffix) {
        return with(null, key, operation, value, prefix, suffix);
    }

    public final SpecificationsBuilder<T> with(final String orPredicate, final String key, final String operation, final Object value, final String prefix, final String suffix) {
        SearchOperation op = SearchOperation.getSimpleOperation(operation.charAt(0));
        if (op != null) {
            if (op == SearchOperation.EQUALITY) { // the operation may be complex operation
                final boolean startWithAsterisk = prefix != null && prefix.contains(SearchOperation.ZERO_OR_MORE_REGEX);
                final boolean endWithAsterisk = suffix != null && suffix.contains(SearchOperation.ZERO_OR_MORE_REGEX);

                if (startWithAsterisk && endWithAsterisk) {
                    op = SearchOperation.CONTAINS;
                } else if (startWithAsterisk) {
                    op = SearchOperation.ENDS_WITH;
                } else if (endWithAsterisk) {
                    op = SearchOperation.STARTS_WITH;
                }
            }
            params.add(new SpecSearchCriteria(orPredicate, key, op, value));
        }
        return this;
    }

    public Specification<T> build() {
        if (params.size() == 0) {
            return null;
        }

        Specification<T> result = new SearchSpecification<>(params.get(0));

        for (int i = 1; i < params.size(); i++) {
            result = params.get(i).isOrPredicate()
                    ? Objects.requireNonNull(Specification.where(result)).or(new SearchSpecification<>(params.get(i)))
                    : Objects.requireNonNull(Specification.where(result)).and(new SearchSpecification<>(params.get(i)));
        }

        return result;
    }

    public Specification<T> buildOr() {
        if (params.size() == 0) {
            return null;
        }

        Specification<T> result = null;

        for (int i = 0; i < params.size(); ) {
            boolean orPredicate = params.get(i).isOrPredicate();
            Specification<T> result1 = new SearchSpecification<>(params.get(i));
            Specification<T> result2 = null;
            if (i > 0) {
                result2 = new SearchSpecification<>(params.get(i + 1));
            }
            result = orPredicate
                    ? Objects.requireNonNull(Specification.where(result)).and(Specification.where(result1).or(result2))
                    : Objects.requireNonNull(Specification.where(result)).and(new SearchSpecification<>(params.get(i)));
            if (orPredicate) {
                i = i + 2;
            } else {
                i = i + 1;
            }

        }

        return result;
    }

    public final SpecificationsBuilder with(SearchSpecification<T> spec) {
        params.add(spec.getCriteria());
        return this;
    }

    public final SpecificationsBuilder with(SpecSearchCriteria criteria) {
        params.add(criteria);
        return this;
    }

}
