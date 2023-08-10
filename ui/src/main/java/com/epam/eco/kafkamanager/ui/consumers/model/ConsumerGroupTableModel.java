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
package com.epam.eco.kafkamanager.ui.consumers.model;

import java.io.Serializable;
import java.util.Collection;

import com.fasterxml.jackson.annotation.JsonGetter;

/**
 * @author Mikhail_Vershkov
 */

public class ConsumerGroupTableModel implements Serializable {
    private Integer draw;
    private Integer recordsTotal;
    private Integer recordsFiltered;
    private Collection<ConsumerGroupRecordModel> data;

    public ConsumerGroupTableModel() {
    }

    @JsonGetter("draw")
    public Integer getDraw() {
        return draw;
    }

    public void setDraw(Integer draw) {
        this.draw = draw;
    }

    @JsonGetter("recordsTotal")
    public Integer getRecordsTotal() {
        return recordsTotal;
    }

    public void setRecordsTotal(Integer recordsTotal) {
        this.recordsTotal = recordsTotal;
    }

    @JsonGetter("recordsFiltered")
    public Integer getRecordsFiltered() {
        return recordsFiltered;
    }

    public void setRecordsFiltered(Integer recordsFiltered) {
        this.recordsFiltered = recordsFiltered;
    }

    @JsonGetter("data")
    public Collection<ConsumerGroupRecordModel> getData() {
        return data;
    }

    public void setData(Collection<ConsumerGroupRecordModel> data) {
        this.data = data;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private ConsumerGroupTableModel tableModel = new ConsumerGroupTableModel();

        public Builder draw(Integer draw) {
            tableModel.setDraw(draw);
            return this;
        }

        public Builder recordsTotal(Integer recordsTotal) {
            tableModel.setRecordsTotal(recordsTotal);
            return this;
        }

        public Builder recordsFiltered(Integer recordsFiltered) {
            tableModel.setRecordsFiltered(recordsFiltered);
            return this;
        }

        public Builder data(Collection<ConsumerGroupRecordModel> data) {
            tableModel.setData(data);
            return this;
        }

        public ConsumerGroupTableModel build() {
            return tableModel;
        }
    }
}
