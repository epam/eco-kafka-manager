/*******************************************************************************
 *  Copyright 2023 EPAM Systems
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not
 *  use this file except in compliance with the License.  You may obtain a copy
 *  of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 *  WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 *  License for the specific language governing permissions and limitations under
 *  the License.
 *******************************************************************************/
package com.epam.eco.kafkamanager;

import java.util.Set;
import java.util.function.BiPredicate;
import java.util.regex.PatternSyntaxException;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;

import static java.util.Objects.isNull;

/**
 * @author Mikhail_Vershkov
 */
public abstract class AbstractSearchCriteria<D> implements SearchCriteria<D> {

    protected static final String OPERATION_SEPARATOR = "_";

    public enum Operation {
        EQUALS, CONTAINS, GREATER, LESS, LIKE, NOT_EMPTY
    }

    protected final Set<ClausesWithHandler> clauses;

    protected AbstractSearchCriteria(Set<ClausesWithHandler> clauses) {
        this.clauses = clauses;
    }

    protected Set<ClausesWithHandler> getClauses() {
        return clauses;
    }

    @Override
    public boolean matches(D obj) {
        Validate.notNull(obj, "Topic Info is null");
        return clauses.stream().allMatch(clause -> clause.match(obj));
    }

    protected static boolean like(String value, String regexp) {
        return value.toLowerCase()
                    .matches(regexp.toLowerCase().replace("?", ".")
                                   .replaceAll("%", ".*"));
    }

    protected record SingleClause<T>(T filterValue, Operation operation) {}

    protected static final BiPredicate<Set<SingleClause<String>>, String> stringClausesHandler = (Set<SingleClause<String>> stringClauses, String value) -> stringClauses.stream().allMatch(
            clause -> compareStringValues(clause.filterValue(), value, clause.operation()));

    protected static final BiPredicate<Set<SingleClause<Integer>>, Integer> numericClausesHandler = (Set<SingleClause<Integer>> numericClauses, Integer value) -> numericClauses.stream().allMatch(
            clause -> compareNumberValues(clause.filterValue(), value, clause.operation()));

    protected static String stripJsonString(String string) {
        return string
                .replace("{","")
                .replace("}","");
    }

    protected static boolean compareStringValues(String filterValue, String value, Operation operation) {
        if(isNull(value)) {
            return false;
        }
        switch (operation) {
            case EQUALS:
                return filterValue.equalsIgnoreCase(value);
            case CONTAINS:
                return StringUtils.containsIgnoreCase(value, filterValue);
            case NOT_EMPTY:
                return !StringUtils.isBlank(value);
            case LIKE: {
                try {
                    return like(value, filterValue);
                } catch (PatternSyntaxException exception) {
                    return false;
                }
            }
            default: {
                return false;
            }
        }
    }

    protected static boolean compareNumberValues(Integer filterValue, Integer value, Operation operation) {
        if(isNull(value)) {
            return false;
        }
        if(isNull(filterValue) && operation != Operation.NOT_EMPTY) {
            return true;
        }
        switch (operation) {
            case EQUALS:
                return value.equals(filterValue);
            case GREATER:
                return value > filterValue;
            case LESS:
                return value < filterValue;
            default: {
                return false;
            }
        }
    }


}
