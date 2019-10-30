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

location.params = function(params_set, params_append, params_subtract) {
    var obj = {}, i, parts, len, key, value;

    var _params = location.search.substr(1).split('&');

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
            if (obj[key].length == 0) {
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
    $('[data-toggle="popover"],[data-original-title]').each(function () {
        //the 'is' for buttons that trigger popups
        //the 'has' for icons within a button that triggers a popup
        if (!$(this).is(e.target) && $(this).has(e.target).length === 0 && $('.popover').has(e.target).length === 0) {
            (($(this).popover('hide').data('bs.popover')||{}).inState||{}).click = false  // fix for BS 3.3.6
        }
    });
}
