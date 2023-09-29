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
package com.epam.eco.kafkamanager.ui.config;

import java.util.List;
import java.util.Set;

/**
 * @author Mikhail_Vershkov
 */

public class TopicBrowser {
    private Boolean useCache = true;
    private Long cacheExpirationPeriodMin = 60L;
    private Long cacheCleanerIntervalMin = 60L;
    private Boolean showGrid = Boolean.TRUE;
    private Boolean enableAnimation = Boolean.TRUE;
    private List<HeaderReplacement> tombstoneGeneratorReplacements;
    private Set<String> replacementPatterns;
    private Boolean filterByKeyPartition = false;
    public Boolean getUseCache() {
        return useCache;
    }

    public void setUseCache(Boolean useCache) {
        this.useCache = useCache;
    }

    public Long getCacheExpirationPeriodMin() {
        return cacheExpirationPeriodMin;
    }

    public void setCacheExpirationPeriodMin(Long cacheExpirationPeriodMin) {
        this.cacheExpirationPeriodMin = cacheExpirationPeriodMin;
    }

    public Long getCacheCleanerIntervalMin() {
        return cacheCleanerIntervalMin;
    }

    public void setCacheCleanerIntervalMin(Long cacheCleanerIntervalMin) {
        this.cacheCleanerIntervalMin = cacheCleanerIntervalMin;
    }

    public Boolean getShowGrid() {
        return showGrid;
    }

    public void setShowGrid(Boolean showGrid) {
        this.showGrid = showGrid;
    }

    public Boolean getEnableAnimation() {
        return enableAnimation;
    }

    public void setEnableAnimation(Boolean enableAnimation) {
        this.enableAnimation = enableAnimation;
    }

    public List<HeaderReplacement> getTombstoneGeneratorReplacements() {
        return tombstoneGeneratorReplacements;
    }

    public void setTombstoneGeneratorReplacements(List<HeaderReplacement> tombstoneGenerationReplacements) {
        this.tombstoneGeneratorReplacements = tombstoneGenerationReplacements;
    }

    public Set<String> getReplacementPatterns() {
        return replacementPatterns;
    }

    public void setReplacementPatterns(Set<String> replacementPatterns) {
        this.replacementPatterns = replacementPatterns;
    }

    public Boolean isFilterByKeyPartition() {
        return filterByKeyPartition;
    }

    public void setFilterByKeyPartition(Boolean filterByKeyPartition) {
        this.filterByKeyPartition = filterByKeyPartition;
    }
}
