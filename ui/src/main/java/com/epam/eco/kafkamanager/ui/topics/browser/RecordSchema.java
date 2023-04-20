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
package com.epam.eco.kafkamanager.ui.topics.browser;

import com.epam.eco.kafkamanager.TopicRecordFetchParams;
import com.epam.eco.kafkamanager.TopicRecordFetchParams.DataFormat;

/**
 * @author Mikhail_Vershkov
 */
public class RecordSchema {

    private final long schemaId;
    private final String schemaName;
    private final String schemaKey;
    private final String schemaValue;
    private final String schemaAsString;

    private final TopicRecordFetchParams.DataFormat schemaType;

    public RecordSchema(
            long schemaId,
            String schemaName,
            String schemaKey,
            String schemaValue,
            String schemaAsString,
            DataFormat schemaType) {
        this.schemaId = schemaId;
        this.schemaName = schemaName;
        this.schemaKey = schemaKey;
        this.schemaValue = schemaValue;
        this.schemaAsString = schemaAsString;
        this.schemaType = schemaType;
    }

    public long getSchemaId() {
        return schemaId;
    }

    public String getSchemaName() {
        return schemaName;
    }

    public String getSchemaKey() {
        return schemaKey;
    }

    public String getSchemaValue() {
        return schemaValue;
    }

    public String getSchemaAsString() {
        return schemaAsString;
    }

    public TopicRecordFetchParams.DataFormat getSchemaType() {
        return schemaType;
    }


}
