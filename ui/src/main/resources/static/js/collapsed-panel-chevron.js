/*
 * Copyright 2023 EPAM Systems
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

$(document).ready(function() {

    isLocalStorageSuportsChevron = function () {
        return (typeof(Storage) !== "undefined");
    }

    getDestanationStateChevron = function (iconItem) {
        if (iconItem.classList.contains('fa-chevron-right')) {
            return "opened-state";
        } else {
            return "closed-state";
        }
    };

    changeCollapseButtonStateChevron = function (button, destinationState) {
        let iconItem = button.children[0];
        if (destinationState === 'opened-state') {
            iconItem.classList.replace('fa-chevron-right', 'fa-chevron-down');
        } else {
            iconItem.classList.replace('fa-chevron-down', 'fa-chevron-right');
        }
        if(isLocalStorageSuportsChevron()) {
            localStorage.setItem(button.id, destinationState);
        }
    };

    document.querySelectorAll(".collapse-button-chevron").forEach(item => item.addEventListener('click',
        function (event) {
            let button = event.currentTarget;
            let destinationState = getDestanationStateChevron(button.children[0]);
            changeCollapseButtonStateChevron(button, destinationState);
        }));

    document.querySelectorAll(".collapse-button-chevron").forEach(button => {
        if(isLocalStorageSuportsChevron()) {
            var storedState = localStorage.getItem(button.id);
            if (storedState !== null) {
                changeCollapseButtonStateChevron(button, storedState);
                let panel = document.querySelector(button.getAttribute("href"));
                if (storedState === 'opened-state') {
                    panel.classList.remove("show");
                    panel.classList.add("show");
                } else {
                    panel.classList.remove("show");
                }
            }
        }
    });

});