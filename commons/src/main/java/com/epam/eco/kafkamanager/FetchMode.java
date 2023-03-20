/*******************************************************************************
 *  Copyright 2022 EPAM Systems
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

import com.epam.eco.commons.kafka.helpers.BiDirectionalTopicRecordFetcher.FetchDirection;

/**
 * @author Mikhail_Vershkov
 *
 * FetchMode - it is a fetching mode. It shows in what direction records should fetch.
 *   (actually it shows what button in UI was pressed )
 *
 *  - FETCH_FORWARD - It shows that was '>' button pressed in UI, and we should fetch a limit records
 *                    from the largest position in a FORWARD direction
 *  - FETCH_BACKWARD - It shows that was '<' button pressed in UI, and we should fetch a limit records
 *                    from the smallest position in a BACKWARD direction
 *  - FETCH_RANGE - It shows that was 'Fetch' button pressed in UI, and we should fetch a limit records
 *                    from the largest position in a FORWARD direction
 *  - FETCH_UNTIL_TIME - It shows that was '<' button pressed in a timestamp item in UI, and we should fetch a limit records
 *                    from the given timestamp in a BACKWARD direction
 *  - FETCH_FROM_TIME - It shows that was '>' button pressed in a timestamp item in UI, and we should fetch a limit records
 *                    from the given timestamp in a FORWARD direction
 */
public enum FetchMode {

        FETCH_FORWARD(FetchDirection.FORWARD, false),
        FETCH_BACKWARD(FetchDirection.BACKWARD, false),
        FETCH_RANGE(FetchDirection.FORWARD, false),
        FETCH_UNTIL_TIME(FetchDirection.BACKWARD, true),
        FETCH_FROM_TIME(FetchDirection.FORWARD, true);

        private final FetchDirection fetchDirection;
        private final boolean isItTimeFetch;

        FetchMode(FetchDirection fetchDirection, boolean isItTimeFetch) {
            this.fetchDirection = fetchDirection;
            this.isItTimeFetch = isItTimeFetch;
        }

        public FetchDirection getFetchDirection() {
            return this.fetchDirection;
        }

        public boolean isItTimeFetch() {
            return isItTimeFetch;
        }

        public long getBaseOffset( long smallest, long largest) {
            if (this == FetchMode.FETCH_RANGE) {
                return smallest;
            } else if(fetchDirection == FetchDirection.BACKWARD) {
                return smallest > 1 ? smallest - 1 : 0;
            } else if(fetchDirection == FetchDirection.FORWARD) {
                return largest + 1;
            }
            return largest;
    }
}