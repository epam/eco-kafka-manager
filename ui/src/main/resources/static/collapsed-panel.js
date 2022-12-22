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
        let iconItem = button.children[0];
        if (destinationState === 'opened-state') {
            iconItem.classList.replace('fa-caret-down', 'fa-caret-up');
        } else {
            iconItem.classList.replace('fa-caret-up', 'fa-caret-down');
        }
        if(isLocalStorageSuports()) {
            localStorage.setItem(button.id, destinationState);
        }
    };

    document.querySelectorAll(".collapse-button").forEach(item => item.addEventListener('click',
        function (event) {
            let button = event.currentTarget;
            let destinationState = getDestanationState(button.children[0]);
            changeCollapseButtonState(button, destinationState);
        }));

    document.querySelectorAll(".collapse-button").forEach(button => {
        if(isLocalStorageSuports()) {
            var storedState = localStorage.getItem(button.id);
            if (storedState !== null) {
                changeCollapseButtonState(button, storedState);
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