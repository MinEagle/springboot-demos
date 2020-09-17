package com.panda.springbootjpaquerydsl.config.jpa;

import org.hibernate.dialect.Oracle12cDialect;
import org.hibernate.dialect.function.SQLFunctionTemplate;
import org.hibernate.type.StringType;

/**
 * @author YIN
 */
public class OracleDialect  extends Oracle12cDialect {

    public OracleDialect() {
        super();
        registerFunction("GET_FND_LOOKUP_MEANING", new SQLFunctionTemplate(StringType.INSTANCE, "FND_COMMON_PKG.GET_FND_LOOKUP_MEANING(?1, ?2)"));
        registerFunction("GET_BRANCH_ORG_NAME", new SQLFunctionTemplate(StringType.INSTANCE, "FND_COMMON_PKG.GET_BRANCH_ORG_NAME(?1)"));
    }
}
