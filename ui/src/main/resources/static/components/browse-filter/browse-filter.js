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

class FilterOperation {

    id;
    label;

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
       return this.column + ' ' + this.operation.label +' "' + this.value + '"';
    }
}

let filterOperationArray = [];
//           FilterOperation.from({id:'equals', label:'equals'}),
//           FilterOperation.from({id:'contains', label: 'contains'}),
//           FilterOperation.from({id:'startWith', label: 'startWith'})
// ];

let getOperationById = (operationId) => {
    for(let ii=0;ii<filterOperationArray.length;ii++) {
       if(filterOperationArray[ii].id === operationId) {
           return filterOperationArray[ii];
       }
    }
    return undefined;
}

let filterClauseArray=[
    // new FilterClause('operation_id', getOperationById('equals'), '123846756538000009'),
    // new FilterClause('key', getOperationById('equals'), '67800009998000009'),
    // new FilterClause('department_id', getOperationById('like'), 'EPM_ECO')
];

let filterColumnArray = [
        // 'key',
        // 'department_id',
        // 'document_id',
        // 'operation_id',
        // 'buiseness_time',
        // 'transaction_time'
];

let isBrowserFilterSet = () => {
    return filterClauseArray!==null && filterClauseArray.length>0;
}
let setFilterOperationArray = (operations) => {
    filterOperationArray = operations
        .map(operation=>FilterOperation.from(operation));
}
let setFilterColumnsArray = (columns) => {
    filterColumnArray = columns;
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

let loadFilterOperationArray = () => {

    for(let ii=0;ii<filterOperationArray.length;ii++) {

        const filterSelectItem = document.createElement('option');
        filterSelectItem.setAttribute('value',filterOperationArray[ii].id);
        filterSelectItem.textContent = filterOperationArray[ii].label;

        document.getElementById('filter-operation-select').appendChild(filterSelectItem);

    }
}   


let getFilterClauseByText = (filterClauseText) => {
    for(let ii=0;ii<filterClauseArray.length;ii++) {
       if(filterClauseArray[ii].getFilterClauseText() === filterClauseText) {
           return filterClauseArray[ii];
       }
    }
}

let drawfilterClause = (filterClause) => {

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
          addRemoveListener(drawfilterClause(filterClauseArray[ii]));
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
    const placeholderText = getOperationById(operationId).placeholder;
    $("#filter-value").attr('placeholder', placeholderText);
}

$(document).ready( function () {

    loadFilterColumnArray();
    loadFilterOperationArray();
    loadFilterClauses();
    setValuePlaceholder(filterOperationArray[0].id);

    document.getElementById("filter-operation-select").addEventListener('change', (event) => {
        setValuePlaceholder(event.target.value)
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
            filterOperationId === undefined ||
            ($('#filter-value').val() === '' && filterOperationId!=='notEmpty' )) {
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

        const filterClauseItem = drawfilterClause(newFilterClause);
        addRemoveListener(filterClauseItem);

    });
})