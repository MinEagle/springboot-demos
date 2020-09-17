package com.panda.springbootjpaquerydsl.config.jpa.search;

/**
 * @author YIN
 */

public enum SearchOperation {

    /**
     * ==
     */
    EQUALITY,
    /**
     * !=
     */
    NEGATION,
    /**
     * gt
     */
    GREATER_THAN,
    /**
     * lt
     */
    LESS_THAN,
    /**
     * like ’param‘
     */
    LIKE,
    /**
     * like ’%param‘
     */
    STARTS_WITH,
    /**
     * like ’param%‘
     */
    ENDS_WITH,
    /**
     * like ‘%param%’
     */
    CONTAINS;

    public static final String[] SIMPLE_OPERATION_SET = {":", "!", ">", "<", "~"};

    public static final String OR_PREDICATE_FLAG = "'";

    public static final String ZERO_OR_MORE_REGEX = "*";

    public static SearchOperation getSimpleOperation(final char input) {
        switch (input) {
            case ':':
                return EQUALITY;
            case '!':
                return NEGATION;
            case '>':
                return GREATER_THAN;
            case '<':
                return LESS_THAN;
            case '~':
                return LIKE;
            default:
                return null;
        }
    }
}
