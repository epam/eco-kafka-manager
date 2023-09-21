package com.epam.eco.kafkamanager;

import java.util.Set;
import java.util.function.BiPredicate;
import java.util.function.Function;

/**
 * @author Mikhail_Vershkov
 */

record ClausesWithHandler<T, C, R>(Set<AbstractSearchCriteria.SingleClause<T>> clauses,
                                             BiPredicate<Set<AbstractSearchCriteria.SingleClause<T>>, C> clausesHandler,
                                             Function<R, C> valueExtractor) {
    boolean match(R obj) {
        return clausesHandler().test(clauses(), valueExtractor().apply(obj));
    }
}