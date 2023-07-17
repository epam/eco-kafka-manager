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

    isLocalStorageSuports = function () {
        return (typeof(Storage) !== "undefined");
    }

    getDestanationState = function (iconItem) {
        if (iconItem.classList.contains('fa-caret-down')) {
            return "opened-state";
        } else {
            return "closed-state";
        }
    };

    changeCollapseButtonState = function (button, destinationState) {
        const iconItem = button.children[0];
        if (destinationState === 'opened-state') {
            iconItem.classList.replace('fa-caret-down', 'fa-caret-up');
        } else {
            iconItem.classList.replace('fa-caret-up', 'fa-caret-down');
        }
        if(isLocalStorageSuports()) {
            localStorage.setItem(button.id, destinationState);
        }
    };

    document.querySelectorAll(".collapsed-header").forEach(item =>
        item.addEventListener('click',
            function (event) {
                const button = event.currentTarget.querySelector(".collapse-button");
                if(button) {
                    button.click();
                }
            })
    );

    document.querySelectorAll(".collapse-button")
        .forEach(item => item.addEventListener('click',
            function (event) {
                const button = event.currentTarget;
                const destinationState = getDestanationState(button.children[0]);
                changeCollapseButtonState(button, destinationState);
            })
        );

    document.querySelectorAll(".collapse-button").forEach(button => {
        if(isLocalStorageSuports()) {
            const storedState = localStorage.getItem(button.id);
            if (storedState !== null) {
                changeCollapseButtonState(button, storedState);
                const panel = document.querySelector(button.getAttribute("href"));
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