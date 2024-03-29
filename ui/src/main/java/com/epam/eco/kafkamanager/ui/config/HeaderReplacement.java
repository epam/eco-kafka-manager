package com.epam.eco.kafkamanager.ui.config;

import org.springframework.expression.Expression;

/**
 * @author Mikhail_Vershkov
 */

public class HeaderReplacement {
    private String headerName;
    private Expression replacement;

    public HeaderReplacement() {}

    public HeaderReplacement(String headerName, Expression replacement) {
        this.headerName = headerName;
        this.replacement = replacement;
    }

    public String getHeaderName() {
        return headerName;
    }

    public void setHeaderName(String headerName) {
        this.headerName = headerName;
    }

    public Expression getReplacement() {
        return replacement;
    }

    public void setReplacement(Expression replacement) {
        this.replacement = replacement;
    }
}
