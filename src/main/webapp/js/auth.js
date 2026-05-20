document.addEventListener('DOMContentLoaded', () => {

    const rolUsuario = sessionStorage.getItem('rol') || 'JUGADOR';
    const nombreUsuario = sessionStorage.getItem('nombre') || 'Invitado';


    const usernameElement = document.getElementById('username');
    if (usernameElement) {
        usernameElement.textContent = nombreUsuario;
    }


    if (rolUsuario === 'EDITOR' || rolUsuario === 'ADMIN') {
        document.querySelectorAll('.req-editor').forEach(el => el.style.display = 'block');
    }

    if (rolUsuario === 'ADMIN') {
        document.querySelectorAll('.req-admin').forEach(el => el.style.display = 'block');
    }


    const userSlot = document.querySelector('.user-slot');


    if (userSlot && nombreUsuario !== 'Invitado') {


        const menuLogout = document.createElement('div');
        menuLogout.textContent = 'Cerrar Sesión';


        menuLogout.style.cssText = `
            position: absolute;
            top: 110%;
            right: 0;
            background-color: #8F1952;
            color: white;
            padding: 0.8rem 1.5rem;
            border-radius: 8px;
            box-shadow: 0 4px 8px rgba(0,0,0,0.5);
            cursor: pointer;
            font-size: 1.2rem;
            display: none; /* Oculto por defecto */
            z-index: 2000;
            transition: background 0.2s;
        `;


        menuLogout.addEventListener('mouseover', () => menuLogout.style.backgroundColor = '#37105B');
        menuLogout.addEventListener('mouseout', () => menuLogout.style.backgroundColor = '#8F1952');


        userSlot.style.position = 'relative';
        userSlot.appendChild(menuLogout);


        userSlot.addEventListener('contextmenu', (e) => {
            e.preventDefault();

            menuLogout.style.display = menuLogout.style.display === 'none' ? 'block' : 'none';
        });


        document.addEventListener('click', (e) => {
            if (!userSlot.contains(e.target)) {
                menuLogout.style.display = 'none';
            }
        });


        menuLogout.addEventListener('click', () => {

            fetch('api/logout', {method: 'POST'})
                .then(() => {

                    sessionStorage.clear();

                    window.location.href = 'index.html';
                })
                .catch(e => console.error("Error cerrando sesión:", e));
        });
    }
});