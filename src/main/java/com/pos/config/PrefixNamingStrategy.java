package com.pos.config;

import org.hibernate.boot.model.naming.CamelCaseToUnderscoresNamingStrategy;
import org.hibernate.boot.model.naming.Identifier;
import org.hibernate.engine.jdbc.env.spi.JdbcEnvironment;

public class PrefixNamingStrategy extends CamelCaseToUnderscoresNamingStrategy {

    private static final String TABLE_PREFIX = "pos_";

    @Override
    public Identifier toPhysicalTableName(Identifier name, JdbcEnvironment jdbcEnvironment) {
        Identifier snakeCase = super.toPhysicalTableName(name, jdbcEnvironment);

        if (snakeCase == null) {
            return null;
        }

        return Identifier.toIdentifier(
                TABLE_PREFIX + snakeCase.getText(),
                snakeCase.isQuoted()
        );
    }
}
