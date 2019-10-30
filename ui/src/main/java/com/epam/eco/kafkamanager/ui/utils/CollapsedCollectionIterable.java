/*
 * Copyright 2019 EPAM Systems
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.  You may obtain a copy
 * of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.epam.eco.kafkamanager.ui.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

import org.apache.commons.lang3.Range;
import org.apache.commons.lang3.Validate;

import com.epam.eco.kafkamanager.ui.utils.CollapsedCollectionIterable.Element;

/**
 * @author Andrei_Tytsik
 */
public class CollapsedCollectionIterable<T> implements Iterable<Element<T>> {

    private static final int AFTER_DELIMITER_SIZE = 1;

    private static final Element<?> DELIMITER = new Element<>();

    public static class Element<T> {

        private final T value;
        private final boolean divider;

        public Element() {
            this(null, true);
        }

        public Element(T value) {
            this(value, false);
        }

        private Element(T value, boolean divider) {
            this.value = value;
            this.divider = divider;
        }

        public T getValue() {
            return value;
        }
        public boolean isDivider() {
            return divider;
        }

        @Override
        public int hashCode() {
            return Objects.hash(value);
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null || obj.getClass() != this.getClass()) {
                return false;
            }
            if (this == obj) {
                return true;
            }
            Element<?> that = (Element<?>)obj;
            return Objects.equals(this.value, that.value);
        }

        @Override
        public String toString() {
            return Objects.toString(value, "...");
        }

    }

    private final List<Element<T>> collapsedCollection;

    public CollapsedCollectionIterable(Collection<T> collection, int collapsedSize) {
        Validate.notNull(collection, "Collection is null");

        this.collapsedCollection = collapseCollection(collection, null, collapsedSize);
    }

    public <V> CollapsedCollectionIterable(Collection<V> collection, Function<V, T> mapper, int collapsedSize) {
        Validate.notNull(collection, "Collection is null");

        this.collapsedCollection = collapseCollection(collection, mapper, collapsedSize);
    }

    @Override
    public Iterator<Element<T>> iterator() {
        return collapsedCollection.iterator();
    }

    @SuppressWarnings("unchecked")
    private List<Element<T>> collapseCollection(Collection<?> collection, Function<?, ?> mapper, int collapsedSize) {
        collapsedSize = adjustCollapsedSize(collapsedSize, collection.size());

        int delimiterIndex = getDelimiterIndex(collapsedSize, collection.size());
        Range<Integer> rangeBeforeDelimiter = getRangeBeforeDelimiter(delimiterIndex, collapsedSize);
        Range<Integer> rangeAfterDelimiter = getRangeAfterDelimiter(delimiterIndex, collection.size());

        List<Element<T>> collapsed = new ArrayList<>();
        int idx = 0;
        for (Object value : collection) {
            if (rangeBeforeDelimiter.contains(idx) || rangeAfterDelimiter.contains(idx)) {
                collapsed.add(asElement(value, (Function<Object, Object>)mapper));
            }
            if (idx == delimiterIndex) {
                collapsed.add((Element<T>)DELIMITER);
            }
            if (collapsed.size() == collapsedSize) {
                break;
            }
            idx++;
        }
        return collapsed;
    }

    @SuppressWarnings("unchecked")
    private Element<T> asElement(Object value, Function<Object, Object> mapper) {
        value = mapper != null ? mapper.apply(value) : value;
        return new Element<T>((T)value);
    }

    private int adjustCollapsedSize(int collapsedSize, int collectionSize) {
        return collapsedSize <= 0 || collapsedSize > collectionSize ? collectionSize : collapsedSize;
    }

    private int getDelimiterIndex(int collapsedSize, int collectionSize) {
        return collectionSize > collapsedSize && collapsedSize > AFTER_DELIMITER_SIZE + 2 ? collapsedSize - AFTER_DELIMITER_SIZE - 1 : -1;
    }

    private Range<Integer> getRangeBeforeDelimiter(int delimiterIndex, int collapsedSize) {
        return delimiterIndex > 0 ? Range.between(0, delimiterIndex - 1) : Range.between(0, collapsedSize - 1);
    }

    private Range<Integer> getRangeAfterDelimiter(int delimiterIndex, int collectionSize) {
        return delimiterIndex > 0 ? Range.between(collectionSize - AFTER_DELIMITER_SIZE, collectionSize - 1) : Range.between(-1, -1);
    }

}
