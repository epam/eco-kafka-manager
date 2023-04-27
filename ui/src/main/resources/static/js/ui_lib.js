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

location.params = function(params_set, params_append, params_subtract) {
    let obj = {}, i, parts, len, key, value;

    const _params = location.search.substr(1).split('&');

    for (i = 0, len = _params.length; i < len; i++) {
        parts = _params[i].split('=');
        if (! parts[0]) {
        	continue;
        }
        if (parts[1]) {
        	obj[parts[0]] = parts[1].split(',');
        } else {
        	obj[parts[0]] = true;
        }
    }

    for (key in params_set) {
        value = params_set[key];
        key = encodeURIComponent(key);
        if (typeof value === 'undefined' || value == null) {
            delete obj[key];
        } else {
            obj[key] = [encodeURIComponent(value)];
        }
    }
    for (key in params_append) {
        value = encodeURIComponent(params_append[key]);
        key = encodeURIComponent(key);
        if (obj[key] != null) {
        	if (obj[key].indexOf(value) < 0) {
        		obj[key].push(value);
        	}
        } else {
        	obj[key] = [value];
        }
    }
    for (key in params_subtract) {
    	value = encodeURIComponent(params_subtract[key]);
    	key = encodeURIComponent(key);
        if (obj[key] != null && obj[key].indexOf(value) >= 0) {
    		obj[key].splice(obj[key].indexOf(value), 1);
            if (obj[key].length === 0) {
            	delete obj[key];
            }
        }
    }

    parts = [];
    for (key in obj) {
        parts.push(key + (obj[key] === true ? '' : '=' + obj[key]));
    }

    location.search = parts.join('&');
};

function hidePopoverIfClickedOutside(e) {
    $('[data-bs-toggle="popover"],[data-original-title]').each(function () {
        //the 'is' for buttons that trigger popups
        //the 'has' for icons within a button that triggers a popup
        if (!$(this).is(e.target) && $(this).has(e.target).length === 0 && $('.popover').has(e.target).length === 0) {
            (($(this).popover('hide').data('bs.popover')||{}).inState||{}).click = false  // fix for BS 3.3.6
        }
    });
}

let infoModal;
let dataModal;

$(document).ready(function() {
    if(document.getElementById("infoModal")!==null) {
        infoModal = new bootstrap.Modal(document.getElementById("infoModal"));
    }
    if(document.getElementById("dataModal")!==null) {
        dataModal = new bootstrap.Modal(document.getElementById("dataModal"));
    }
})

function showInfo(type,infoHeader,infoText) {

    let headerClass = "info-modal-header-info";
    let textClass = "info-modal-text-info";
    let buttonClass = "btn-success";

    if(type==="error") {
        headerClass = "info-modal-header-danger";
        textClass = "info-modal-text-danger";
        buttonClass = "btn-danger";
    }
    const header = document.getElementById('infoModalHeader');
    header.textContent = infoHeader;
    header.classList.add(headerClass);

    const text = document.getElementById('infoModalText');
    text.textContent = infoText;
    text.classList.add(textClass)

    document.getElementById('infoModalCloseButton').classList.add(buttonClass);

    infoModal.show();
}

function showData(dialogHeader,dialogText) {

    const headerClass = "data-modal-header";
    const textClass = "data-modal-text";

    const header = document.getElementById('dataModalHeader');
    header.textContent = dialogHeader;
    header.classList.add(headerClass);

    var text = document.getElementById('dataModalText');
    text.textContent = dialogText;
    text.classList.add(textClass)

    document.getElementById('dataModalCopyButton').onclick = (event) => {
        copyText(dialogText, event.target)
    };

    dataModal.show();
}