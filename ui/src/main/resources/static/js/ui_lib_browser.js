/*
 * Copyright 2020 EPAM Systems
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

function showHideColumnSelectorDependingOnFormatSelected(fetchedValueFormat) {
    if (fetchedValueFormat == null) {
        return;
    }
    if (fetchedValueFormat === $('#valueFormat').val()) {
        $('#column-selector-panel').show();
    } else {
        $('#column-selector-panel').hide();
    }
}

function removeColumnSelectorIfFormatNotEqualToFecthedOne(fetchedValueFormat) {
    if (fetchedValueFormat != null && fetchedValueFormat !== $('#valueFormat').val()) {
        $('#column-selector-panel').remove();
    }
}

function storeOrApplySelectedColumns() {
    const key = $('#topicName').val() + '_' + $('#valueFormat').val();
    const columnCheckboxSelector = $('.column-checkbox');
    if (columnCheckboxSelector.length > 0) {
        let columns = Array();
        columnCheckboxSelector.each(function(idx, elem) {
            const enabled = $(elem).is(':checked');
            const column = $(elem).data('column');
            if (enabled && column) {
                columns.push(column);
            }
        });
        localStorage.setItem(key, JSON.stringify(columns));
    } else {
        const columnsJson = localStorage.getItem(key);
        if (columnsJson && columnsJson.length > 0) {
            const columns = JSON.parse(columnsJson);
            $('#fetch-form').append($.map(columns, function (column) {
                return $('<input/>', {
                    type: 'hidden',
                    name: 'ce_' + column,
                    value: 1
                });
            }));
        }
    }
}

function setBeginOffsetsToMin() {
    $('.partition-checkbox').each(function (idx, elem) {
        if ($(elem).is(':checked')) {
            const partition = $(elem).data('partition');
            $('#p_min_' + partition).val($('#p_min_range_' + partition).val());
        }
    });
    $('#previous-offsets-link-in-grid').css("display", "none");
    $('#previous-offsets-link').css("display", "none");

}
function setEndOffsetsToMax() {
    const checkBoxList = $('.partition-checkbox');
    const limit = parseInt($('#limit').val());
    let partitionsCount = 0;
    checkBoxList.each(function (idx, elem) {
        if ($(elem).is(':checked')) {
            partitionsCount++;
        }
    });
    const counts = divideLimitOnPartitions(partitionsCount,limit);
    let index = 0;
    checkBoxList.each(function (idx, elem) {
        if ($(elem).is(':checked')) {
            const partition = $(elem).data('partition');
            const maxRangeValue = parseInt($('#p_max_range_' + partition).val());
            const minRangeValue = parseInt($('#p_min_range_' + partition).val());
            const minOffset = maxRangeValue - counts[index] + 1;
            $('#p_max_' + partition).val(maxRangeValue);
            $('#p_min_' + partition).val(minOffset < minRangeValue ? minRangeValue : minOffset);
            index++;
        }
    });
    $('#next-offsets-link-in-grid').css("display", "none");
    $('#next-offsets-link').css("display", "none");
}

function divideLimitOnPartitions(partitionCount,limit) {
    const result = [partitionCount];
    const firstChunk = parseInt(limit/partitionCount);
    for(let ii=0;ii<partitionCount;ii++) {
        result[ii]=firstChunk;
    }
    let rest = limit - partitionCount*firstChunk;
    let ii=0;
    while(rest>0) {
        let index = (ii++)%partitionCount;
        result[index]+=1;
        rest--;
    }
    return result;
}

function blink(elem) {
    elem.addClass("after-blinking");
    elem.addClass("blinked");
    elem.removeClass("inactive-partition");
}