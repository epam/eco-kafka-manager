const buttons = document.querySelectorAll('.menu > button');
const menus = document.querySelectorAll('.menu');

const disableActive = (htmlElement) => [...htmlElement.classList].filter(item => item !== 'active').join(' ');

buttons.forEach(button => {
    button.addEventListener('click', () => {
        if (![...button.classList].includes('active')) {
            button.classList.add('active');
            button.nextElementSibling.classList.add('active');
        } else {
            button.className = disableActive(button);
            button.nextElementSibling.className = disableActive(button.nextElementSibling);
        }
    });
});

document.addEventListener('mousedown', (e) => {
    menus.forEach(menu => {
        if (!menu.contains(e.target)) {
            const button = menu.firstElementChild;
            if (button && button.nextElementSibling) {
                button.className = disableActive(button);
                button.nextElementSibling.className = disableActive(button.nextElementSibling);
            }
        }
    });
});
