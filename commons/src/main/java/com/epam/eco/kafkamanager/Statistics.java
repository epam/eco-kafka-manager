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
package com.epam.eco.kafkamanager;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author Raman_Babich
 */
public class Statistics {

    private final double max;
    private final double min;
    private final double mean;

    public Statistics(
            @JsonProperty("max") double max,
            @JsonProperty("min") double min,
            @JsonProperty("mean") double mean) {
        this.max = max;
        this.min = min;
        this.mean = mean;
    }

    public double getMax() {
        return max;
    }

    public double getMin() {
        return min;
    }

    public double getMean() {
        return mean;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Statistics that = (Statistics) o;
        return Double.compare(that.max, max) == 0 &&
                Double.compare(that.min, min) == 0 &&
                Double.compare(that.mean, mean) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(max, min, mean);
    }

    @Override
    public String toString() {
        return "Statistics{" +
                "max=" + max +
                ", min=" + min +
                ", mean=" + mean +
                '}';
    }

    public static Statistics with(double max, double min, double mean) {
        return new Statistics(max, min, mean);
    }
}
