class FilterOperation {

    id;
    label;
    placeholder;
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

class FilterClause {

    column;
    operation;
    value;

    constructor(column, operation, value) {
        this.column = column;
        this.operation = operation;
        this.value = value;
    }

    equals(testClause) {
        if(testClause===undefined) {
            return false;
        }
        return this.column === testClause.column &&  this.operation.equals(testClause.operation);
    }
    getFilterClauseText() {
        return this.column + ' ' + this.operation.label +
            (this.operation.required ? ' "' + this.value + '"' : '');
    }
}

let filterOperationArray = [];

let getOperationById = (operationId) => {
    for(let ii=0;ii<filterOperationArray.length;ii++) {
        if(filterOperationArray[ii].id === operationId) {
            return filterOperationArray[ii];
        }
    }
    return undefined;
}

let filterClauseArray=[];

let excludesArray = [];

let filterColumnArray = [];

let isBrowserFilterSet = () => {
    return filterClauseArray!==null && filterClauseArray.length>0;
}
let setFilterOperationArray = (operations) => {
    filterOperationArray = operations
        .map(operation=>FilterOperation.from(operation));
}
let setExcludes = (excludes) => {
    excludesArray = excludes;
}

let setFilterColumnsArray = (topic, columns) => {

    let storedColumns = JSON.parse(localStorage.getItem(topic));

    if(!storedColumns || storedColumns.length===0) {
        localStorage.setItem(topic, JSON.stringify(columns));
        storedColumns = columns;
    }

    let addNewColumns = false;
    for(let ii=0;ii<columns.length;ii++) {
        if(!storedColumns.includes(columns[ii])) {
            storedColumns.push(columns[ii]);
            addNewColumns = true;
        }
    }
    if(addNewColumns) {
        localStorage.setItem(topic, JSON.stringify(storedColumns));
    }

    filterColumnArray = storedColumns;
}

let setFilterClauseArray = (clauses) => {
    filterClauseArray = clauses
        .map(clause=>new FilterClause(clause.column,
            FilterOperation.from(clause.operation),
            clause.value));
}

let loadFilterColumnArray = () => {

    for(let ii=0;ii<filterColumnArray.length;ii++) {

        const filterSelectItem = document.createElement('option');
        filterSelectItem.setAttribute('value', filterColumnArray[ii]);
        filterSelectItem.textContent = filterColumnArray[ii];

        document.getElementById('filter-column-select').appendChild(filterSelectItem);

    }
}

let getOperationIdsByColumn = (columnId) => {
    let operations = excludesArray.filter( e => e.columnName === columnId).flatMap(e => e.operations);
    if(operations && operations.length===0) {
        operations = excludesArray.filter(e => e.columnName === 'default').flatMap(e => e.operations);
    }
    return operations;
}

let getOperationsByColumn = ( columnId ) => {
    let operations = getOperationIdsByColumn(columnId);
    return filterOperationArray.filter(operation => operations.includes(operation.id));
}

let loadFilterOperationArray = (columnId) => {

    document.getElementById('filter-operation-select').innerHTML='';

    const filteredOperations = getOperationsByColumn(columnId);

    for(let ii=0;ii<filteredOperations.length;ii++) {

        const filterSelectItem = document.createElement('option');
        filterSelectItem.setAttribute('value',filteredOperations[ii].id);
        filterSelectItem.textContent = filteredOperations[ii].label;

        document.getElementById('filter-operation-select').appendChild(filterSelectItem);

    }
    setValuePlaceholder(filteredOperations[0].id);
}


let getFilterClauseByText = (filterClauseText) => {
    for(let ii=0;ii<filterClauseArray.length;ii++) {
        if(filterClauseArray[ii].getFilterClauseText() === filterClauseText) {
            return filterClauseArray[ii];
        }
    }
}

let drawFilterClause = (filterClause) => {

    const filterClauseSpan = document.createElement('span');
    filterClauseSpan.textContent=filterClause.getFilterClauseText();

    const filterClauseItem = document.createElement('div');
    filterClauseItem.classList.add('filter-clause-item');
    filterClauseItem.appendChild(filterClauseSpan);

    document.getElementById('filter-clause-container').appendChild(filterClauseItem);

    return filterClauseItem;

}

let loadFilterClauses = () => {
    const containerElement = document.getElementById('filter-clause-container');
    while(containerElement.firstChild && !containerElement.firstChild.remove());
    for(let ii=0;ii<filterClauseArray.length;ii++) {
        if(validateSingleFilterClause(filterClauseArray[ii])) {
            addRemoveListener(drawFilterClause(filterClauseArray[ii]));
        }
    }
}

const isColumnExists = (columnName) => {
    if(columnName==='key') {
        return true;
    }
    for(let ii=0;ii<filterColumnArray.length;ii++) {
        if(filterColumnArray[ii]===columnName) {
            return true;
        }
    }
    showInfo('error','Error while validating columns','Column "' + columnName + '" doesnt exists in topic.');
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

let thisClausePredicate = (clause,filterClause) => {
    return !clause.equals(filterClause);
}

let removeFilterClause = (filterClauseText) => {
    showConfirm("Remove filter clause",
        "Are you sure to remove filter clause?",
        () => confirmRemoveClause(filterClauseText));
}

let confirmRemoveClause = (filterClauseText) => {
    const filterClause = getFilterClauseByText(filterClauseText);
    filterClauseArray = filterClauseArray.filter(clause => thisClausePredicate(clause,filterClause));
    loadFilterClauses();
}

let addRemoveListener = (elem) => {
    elem.addEventListener('click', function() {
        removeFilterClause(elem.textContent);
    });
}

function setValuePlaceholder(operationId) {
    const operation = getOperationById(operationId);
    const filterValueElement = document.getElementById("filter-value");
    if(operation.required) {
        filterValueElement.removeAttribute('disabled');
    } else {
        filterValueElement.setAttribute('disabled', 'disabled');
    }

    filterValueElement.setAttribute('placeholder', operation.placeholder);
}


$(document).ready( function () {

    loadFilterColumnArray();
    loadFilterOperationArray(filterColumnArray[0]);
    loadFilterClauses();
    setValuePlaceholder(filterOperationArray[0].id);

    document.getElementById("filter-operation-select").addEventListener('change', (event) => {
        setValuePlaceholder(event.target.value)
    });

    document.getElementById("filter-column-select").addEventListener('change', (event) => {
        loadFilterOperationArray(event.target.value)
    });

    document.getElementById("add-filter-button").addEventListener('click', () => {

        let filterColumnName;
        let filterOperationId;

        $("#filter-column-select option:selected").each(function () {
            filterColumnName = $(this).val();
        });

        $("#filter-operation-select option:selected").each(function () {
            filterOperationId = $(this).val();
        });

        if (filterColumnName === undefined ||
            filterOperationId === undefined ) {
            return;
        }

        if ($('#filter-value').val() === '' && filterOperationArray.filter(op=>op.required).map(op=>op.id).includes(filterOperationId) ) {
            showInfo('error', 'Filter clause validation', 'Operation "' + filterOperationId + '" requires value.');
            return;
        }

        for (let ii = 0; ii < filterClauseArray.length; ii++) {
            if (filterClauseArray[ii].column === filterColumnName &&
                filterClauseArray[ii].operation.id === filterOperationId) {
                showInfo('error', 'Filter clause validation', 'Operation being added already exists in filer clause.');
                return;
            }
        }

        const newFilterClause = new FilterClause(filterColumnName, getOperationById(filterOperationId), $('#filter-value').val());

        filterClauseArray.push(newFilterClause);

        const filterClauseItem = drawFilterClause(newFilterClause);
        addRemoveListener(filterClauseItem);

    });
})