class FilterOperation {

    id;
    label;
    dataTypes;
    required;

    static from(json){
        return Object.assign(new FilterOperation(), json);
    }

    equals(testOperation) {
        if(testOperation===undefined) {
            return false;
        }
        return this.id === testOperation.id;
    }
}

class FilterColumn {

    id;
    label;
    type;
    values;
    placeholders;

    static from(json){
        return Object.assign(new FilterColumn(), json);
    }

    equals(testColumn) {
        if(testColumn===undefined) {
            return false;
        }
        return this.id === testColumn.id;
    }
}

class FilterClause {

    column;
    operation;
    value;
    valueLabel;

    constructor(column, operation, value, valueLabel) {
        this.column = column;
        this.operation = operation;
        this.value = value;
        this.valueLabel = valueLabel;
    }

    equals(testClause) {
        if(testClause===undefined) {
            return false;
        }
        return this.column.equals(testClause.column) &&  this.operation.equals(testClause.operation);
    }
    getFilterClauseText() {
        return this.column.label + ' ' + this.operation.label +' "' + (this.valueLabel ? this.valueLabel : this.value + '"');
    }
}

const stringPlaceholders = [
    { operation: 'LIKE', text: 'Format:%any string%any string%'},
    { operation: 'NOT_EMPTY', text: 'Format: leave it blank'},
    { operation: 'OTHERS', text: 'Format: any string'}
];

const numberPlaceholders = [{operation: 'OTHERS', text: 'Format: numeric'}];

const mapPlaceholders = [
    { operation: 'EQUALS', text: 'Format: config: value,...'},
    { operation: 'NOT_EMPTY', text: 'Format: leave it blank'},
    { operation: 'LIKE', text: 'Format: %any string%any other string%'},
    { operation: 'OTHERS', text: 'Format: any string'}
];

const filterOperationArray = [
    FilterOperation.from({id:'GREATER', label:'greater than', dataTypes:['number'], required: true}),
    FilterOperation.from({id:'LESS', label:'less than', dataTypes:['number'], required: true}),
    FilterOperation.from({id:'CONTAINS', label: 'contains', dataTypes:['string','map'], required: true}),
    FilterOperation.from({id:'EQUALS', label:'equals', dataTypes:['number','enum','string','map'], required: true}),
    FilterOperation.from({id:'LIKE', label: 'like', dataTypes:['string','map'], required: true}),
    FilterOperation.from({id:'NOT_EMPTY', label: 'not empty', dataTypes:['string','map'], required: false})
];

const getOperationById = (operationId) => {
    for(let ii=0;ii<filterOperationArray.length;ii++) {
        if(filterOperationArray[ii].id === operationId) {
            return filterOperationArray[ii];
        }
    }
    return undefined;
}

const getOperationByType = (dataType) => {
    for(let ii=0;ii<filterOperationArray.length;ii++) {
        if(filterOperationArray[ii].dataTypes.includes(dataType)) {
            return filterOperationArray[ii];
        }
    }
    return undefined;
}


let filterColumnArray=[];

const getColumnById = (columnId) => {
    for(let ii=0;ii<filterColumnArray.length;ii++) {
        if(filterColumnArray[ii].id === columnId) {
            return filterColumnArray[ii];
        }
    }
    return undefined;
}

let filterClauseArray=[];

const getClausesAsQueryParams = (forQuery) => {
    const result = filterClauseArray
        .map(clause=>clause.column.id+'_'+clause.operation.id+'='+(forQuery ? clause.value.replaceAll('%','%25') : clause.value))
        .join('&');
    return result!=='' ? '?'+result : '';
}

const isBrowserFilterSet = () => {
    return filterClauseArray!==null && filterClauseArray.length>0;
}

let setFilterColumnsArray = (columns) => {
    filterColumnArray = columns.map(column => FilterColumn.from(column));
}

const setFilterClauseArray = (clauses) => {
    filterClauseArray = clauses
        .map(clause=>new FilterClause(
            FilterColumn.from(clause.column),
            FilterOperation.from(clause.operation),
            clause.value));
}

const loadFilterColumnArray = () => {

    for(let ii=0;ii<filterColumnArray.length;ii++) {

        const filterSelectItem = document.createElement('option');
        filterSelectItem.setAttribute('value', filterColumnArray[ii].id);
        filterSelectItem.textContent = filterColumnArray[ii].label;

        document.getElementById('query-param-filter-column-select').appendChild(filterSelectItem);

    }
}

const loadFilterOperationArray = () => {

    for(let ii=0;ii<filterOperationArray.length;ii++) {

        const filterSelectItem = document.createElement('option');
        filterSelectItem.setAttribute('value',filterOperationArray[ii].id);
        filterSelectItem.textContent = filterOperationArray[ii].label;

        document.getElementById('query-param-filter-operation-select').appendChild(filterSelectItem);

    }
}

const loadFilterOperationArrayByType = (dataType) => {

    let operationsLoaded = [];

    let operationSelector = document.getElementById('query-param-filter-operation-select');
    while (operationSelector.options.length>0) {
        operationSelector.options.remove(0);
    }

    for(let ii=0;ii<filterOperationArray.length;ii++) {

        if(filterOperationArray[ii].dataTypes.includes(dataType)) {
            var filterSelectItem = document.createElement('option');
            filterSelectItem.setAttribute('value', filterOperationArray[ii].id);
            filterSelectItem.textContent = filterOperationArray[ii].label;
            operationSelector.appendChild(filterSelectItem);
            operationsLoaded.push(filterOperationArray[ii])
        }

    }
    return operationsLoaded;
}

const getFilterClauseByText = (filterClauseText) => {
    for(let ii=0;ii<filterClauseArray.length;ii++) {
        if(filterClauseArray[ii].getFilterClauseText() === filterClauseText) {
            return filterClauseArray[ii];
        }
    }
}

const drawFilterClause = (filterClause) => {

    var filterClauseSpan = document.createElement('span');
    filterClauseSpan.textContent=filterClause.getFilterClauseText();

    var filterClauseItem = document.createElement('div');
    filterClauseItem.classList.add('filter-clause-item');
    filterClauseItem.appendChild(filterClauseSpan);

    document.getElementById('query-param-filter-clause-container').appendChild(filterClauseItem);

    return filterClauseItem;

}

const loadFilterClauses = () => {
    const containerElement = document.getElementById('query-param-filter-clause-container');
    while (containerElement.firstChild && !containerElement.firstChild.remove());
    for(let ii=0;ii<filterClauseArray.length;ii++) {
        if(validateSingleFilterClause(filterClauseArray[ii])) {
            addRemoveListener(drawFilterClause(filterClauseArray[ii]));
        }
    }
}

const isColumnExists = (column) => {

    for(let ii=0;ii<filterColumnArray.length;ii++) {
        if(filterColumnArray[ii].id===column.id) {
            return true;
        }
    }
    showInfo('error','Error while validating columns','Column "' + columnName.id + '" doesnt exists.');
    return false;
}

const isOperationExists = (operation) => {
    for(let ii=0;ii<filterOperationArray.length;ii++) {
        if(filterOperationArray[ii].equals(operation)) {
            return true;
        }
    }
    showInfo('error','Error while validate operations','Operation "' + operation.id + '" doesnt exists.');
    return false;
}

const validateSingleFilterClause = ( singleFilterClause ) => {
    return isColumnExists(singleFilterClause.column) &&
        isOperationExists(singleFilterClause.operation);

}

const validateFilterClause = () => {
    for(let ii=0;ii<filterClauseArray.length;ii++) {
        if(!validateSingleFilterClause(filterClauseArray[ii])) {
            return false;
        }
    }
    return true;
}

function thisClausePredicate(clause,filterClause) {
    return !clause.equals(filterClause);
}

function removeFilterClause (filterClauseText) {
    showConfirm("Remove filter clause",
        "Remove filter clause : " + filterClauseText + "?",
        function () {confirmRemoveClause(filterClauseText)});
}

function confirmRemoveClause(filterClauseText) {
    const filterClause = getFilterClauseByText(filterClauseText);
    filterClauseArray = filterClauseArray.filter(clause => thisClausePredicate(clause,filterClause));
    loadFilterClauses();
}

function addRemoveListener(elem) {
    elem.addEventListener('click', function() {
        removeFilterClause(elem.textContent);
    });
}

function setValuePlaceholder(placeholders,operationId) {
    const placeholderText = findPlaceholderByOperationId(placeholders,operationId);
    $("#query-param-filter-value").attr('placeholder',placeholderText);
}

function findPlaceholderByOperationId(operationId, placeholders) {
    let otherPlaceholder = "";
    for(let placeholder of placeholders) {
       if(placeholder.operation === operationId) {
          return placeholder.text;
       }
       if(placeholder.operation === 'OTHERS') {
           otherPlaceholder = placeholder.text;
       }
   }
    return otherPlaceholder;
}


function initiateFilterClauseArray() {
    const searchString = window.location.search;
    const urlSearchParams = new URLSearchParams(searchString);
    const keysIterator = urlSearchParams.keys();

    const keys=[];
    for(const key of keysIterator) {
        keys.push({ columnId: key.substring(0,key.indexOf('_')), operationId: key.substring(key.indexOf('_')+1) });
    }

    filterClauseArray=[];

    for(const key of keys) {
        const column = getColumnById(key.columnId);
        if(column) {
            let operation = getOperationById(key.operationId);
            if(operation===undefined) {
                operation = 'EQUALS';
            }
            const value = urlSearchParams.get(key.columnId+'_'+key.operationId);
            const valueLabel = column.values ? getValueLabelById(column,value) : undefined;
            filterClauseArray.push(new FilterClause(column, operation, value, valueLabel));
        }
    }

}

const loadValuesList = (values) => {

    const valueItem = document.getElementById('query-param-filter-value-select');
    valueItem.innerHTML='';

    for(let value of values) {

        const filterValueItem = document.createElement('option');
        filterValueItem.setAttribute('value', value.id);
        filterValueItem.textContent = value.value;

        valueItem.appendChild(filterValueItem);

    }
}

const removeValuesList = () => {
    document.getElementById('query-param-filter-value-select').innerHTML='';
}

function handleValuesOnColumnChange(column) {

    const queryParamFilterValueSelect = $('#query-param-filter-value-select');
    const queryParamFilterValue = $('#query-param-filter-value');

    if(column.values) {
        queryParamFilterValueSelect.removeClass('display-none');
        queryParamFilterValueSelect.addClass("display-block");
        queryParamFilterValue.removeClass('display-block');
        queryParamFilterValue.addClass('display-none');
        loadValuesList(column.values);
        queryParamFilterValue.val(queryParamFilterValueSelect.val());
    } else {
        queryParamFilterValueSelect.addClass('display-none');
        queryParamFilterValueSelect.removeClass("display-block");
        queryParamFilterValue.addClass('display-block');
        queryParamFilterValue.removeClass('display-none');
        const operationId = $('#query-param-filter-operation-select').val();
        setValuePlaceholder(operationId, column.placeholders);
        queryParamFilterValue.val('')
        removeValuesList();
    }
}

function getValueLabelById(filterColumn,valueId) {
    if(filterColumn.values && filterColumn.values.length>0) {
        for(const val of filterColumn.values) {
            if(val.id===valueId) {
                return val.value;
            }
        }
        return undefined;
    } else {
        return undefined;
    }
}

$(document).ready( function () {

    initColumns();
    loadFilterColumnArray();
    const operationLoaded = loadFilterOperationArrayByType(filterColumnArray[0].type);
    setValuePlaceholder(operationLoaded[0].id, filterColumnArray[0].placeholders);
    initiateFilterClauseArray();
    loadFilterClauses();

    document.getElementById("query-param-filter-column-select").addEventListener('change', (event) => {
        const column = getColumnById(event.target.value);
        loadFilterOperationArrayByType(column.type);
        $('#query-param-filter-value').val('');
        handleValuesOnColumnChange(column);
    });

    document.getElementById("query-param-filter-operation-select").addEventListener('change', (event) => {
        const columnValue = $("#query-param-filter-column-select").val();
        const column = getColumnById(columnValue);
        setValuePlaceholder(event.target.value,column.placeholders);
    });

    document.getElementById("query-param-filter-value-select").addEventListener('change', (event) => {
        $('#query-param-filter-value').val(event.target.value);
    });

    function isEnumValueExists(filterColumn,value) {
        return filterColumn.values.map(m=>m.id).includes(value);
    }

    document.getElementById("query-param-filter-value").addEventListener('keydown', (event) => {
       if(event.key==='Enter') {
           document.getElementById("add-query-param-filter-button").click();
       }
    });

    document.getElementById("add-query-param-filter-button").addEventListener('click', () => {

        let filterColumn;
        let filterOperationId;

        $("#query-param-filter-column-select option:selected").each(function () {
            filterColumn = getColumnById($(this).val());
        });

        $("#query-param-filter-operation-select option:selected").each(function () {
            filterOperationId = $(this).val();
        });

        const queryParamFilterValue = $('#query-param-filter-value');

        if (filterColumn === undefined ||
            filterOperationId === undefined ||
            (queryParamFilterValue.val()==='' && getOperationById(filterOperationId).required)) {
            return;
        }

        switch (filterColumn.type) {
            case 'number':
                if (isNaN(parseInt(queryParamFilterValue.val()))) {
                    showInfo('error', 'Column values validation', 'Value for column "' + filterColumn.label + '" should be numeric.')
                    return;
                }
                break;
            case 'enum':
                if (!isEnumValueExists(filterColumn,queryParamFilterValue.val())) {
                    showInfo('error', 'Column values validation', 'Value for column "' + filterColumn.label +
                        '" should one of: ' + filterColumn.values.join(','));
                    return;
                }
        }

        for (let ii = 0; ii < filterClauseArray.length; ii++) {
            if (filterClauseArray[ii].column.id === filterColumn.id &&
                filterClauseArray[ii].operation.id === filterOperationId) {
                showInfo('error', 'Filter clause validation', 'Operation being added already exists in filer clause.');
                return;
            }
        }

        const newFilterClause = new FilterClause(filterColumn,
                                                 getOperationById(filterOperationId),
                                                 queryParamFilterValue.val(),
                                                 getValueLabelById(filterColumn,queryParamFilterValue.val()));

        filterClauseArray.push(newFilterClause);

        const filterClauseItem = drawFilterClause(newFilterClause);
        addRemoveListener(filterClauseItem);

    });

});