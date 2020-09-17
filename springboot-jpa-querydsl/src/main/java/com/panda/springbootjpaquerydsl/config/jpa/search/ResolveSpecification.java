package com.panda.springbootjpaquerydsl.config.jpa.search;

import com.google.common.base.Joiner;
import org.springframework.data.jpa.domain.Specification;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author YIN
 */
public class ResolveSpecification<T> {

    public Specification<T> resolveSpecification(String searchParameters) {
        SpecificationsBuilder<T> builder = new SpecificationsBuilder<>();
        String operationSetExper = Joiner.on("|")
                .join(SearchOperation.SIMPLE_OPERATION_SET);
        Pattern pattern = Pattern.compile(
                "([\\u4E00-\\u9FA5A-Za-z0-9_]+?)("
                        + operationSetExper +
                        ")(\\p{Punct}?)([\\u4E00-\\u9FA5A-Za-z0-9_]+?)(\\p{Punct}?),");
        Matcher matcher = pattern.matcher(searchParameters + ",");
        while (matcher.find()) {
            builder.with(
                    matcher.group(1),
                    matcher.group(2),
                    matcher.group(4),
                    matcher.group(3),
                    matcher.group(5));
        }
        return builder.build();
    }


    public Specification<T> OrResolveSpecification(String searchParameters) {
        SpecificationsBuilder<T> builder = new SpecificationsBuilder<>();
        String operationSetExper = Joiner.on("|")
                .join(SearchOperation.SIMPLE_OPERATION_SET);
        Pattern pattern = Pattern.compile(
                "(\\p{Punct}?)([\\u4E00-\\u9FA5A-Za-z0-9_]+?)("
                        + operationSetExper
                        + ")(\\p{Punct}?)([\\u4E00-\\u9FA5A-Za-z0-9_]+?)(\\p{Punct}?),");
        Matcher matcher = pattern.matcher(searchParameters + ",");
        while (matcher.find()) {
            builder.with(matcher.group(1), matcher.group(2), matcher.group(3),
                    matcher.group(5), matcher.group(4), matcher.group(6));
        }
        return builder.build();
    }


    public Specification<T> resolveOrSpecification(String searchParameters) {
        SpecificationsBuilder<T> builder = new SpecificationsBuilder<>();
        String operationSetExper = Joiner.on("|")
                .join(SearchOperation.SIMPLE_OPERATION_SET);
        Pattern pattern = Pattern.compile(
                "(\\p{Punct}?)([\\u4E00-\\u9FA5A-Za-z0-9_]+?)("
                        + operationSetExper
                        + ")(\\p{Punct}?)([\\u4E00-\\u9FA5A-Za-z0-9_]+?)(\\p{Punct}?),");
        Matcher matcher = pattern.matcher(searchParameters + ",");
        while (matcher.find()) {
            builder.with(matcher.group(1), matcher.group(2), matcher.group(3),
                    matcher.group(5), matcher.group(4), matcher.group(6));
        }
        return builder.buildOr();
    }
}
