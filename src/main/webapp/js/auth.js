document.addEventListener('DOMContentLoaded', () => {
    // Leemos el rol de la memoria del navegador (por defecto 'JUGADOR')
    const rolUsuario = sessionStorage.getItem('rol') || 'JUGADOR';
    const nombreUsuario = sessionStorage.getItem('nombre') || 'Invitado';

    // 1. Ponemos tu nombre arriba a la derecha
    const usernameElement = document.getElementById('username');
    if (usernameElement) {
        usernameElement.textContent = nombreUsuario;
    }

    // 2. Lógica del Menú: Encendemos botones según tu nivel de poder
    if (rolUsuario === 'EDITOR' || rolUsuario === 'ADMIN') {
        document.querySelectorAll('.req-editor').forEach(el => el.style.display = 'block');
    }

    if (rolUsuario === 'ADMIN') {
        document.querySelectorAll('.req-admin').forEach(el => el.style.display = 'block');
    }

    // =========================================================================
    // 3. NUEVO: LÓGICA DEL MENÚ DE CERRAR SESIÓN (CLICK DERECHO)
    // =========================================================================
    const userSlot = document.querySelector('.user-slot');

    // Solo creamos el menú si estamos logueados (no somos Invitados)
    if (userSlot && nombreUsuario !== 'Invitado') {

        // Creamos el elemento visual del menú desplegable
        const menuLogout = document.createElement('div');
        menuLogout.textContent = 'Cerrar Sesión';

        // Le damos estilo directamente aquí para no tener que editar tus 4 archivos CSS
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

        // Efecto hover (cambia de color al pasar el ratón)
        menuLogout.addEventListener('mouseover', () => menuLogout.style.backgroundColor = '#37105B');
        menuLogout.addEventListener('mouseout', () => menuLogout.style.backgroundColor = '#8F1952');

        // Configuramos la caja del usuario para que el menú se posicione justo debajo
        userSlot.style.position = 'relative';
        userSlot.appendChild(menuLogout);

        // EVENTO 1: Interceptar el click derecho (contextmenu)
        userSlot.addEventListener('contextmenu', (e) => {
            e.preventDefault(); // Esto anula el menú gris feo de Windows/Chrome
            // Alternamos entre mostrar y ocultar nuestro menú
            menuLogout.style.display = menuLogout.style.display === 'none' ? 'block' : 'none';
        });

        // EVENTO 2: Si el usuario hace click izquierdo en cualquier otra parte de la pantalla, ocultamos el menú
        document.addEventListener('click', (e) => {
            if (!userSlot.contains(e.target)) {
                menuLogout.style.display = 'none';
            }
        });

        // EVENTO 3: ¿Qué pasa cuando hacemos click en "Cerrar Sesión"?
        menuLogout.addEventListener('click', () => {
            // 1. Avisamos al servidor (Java) para que destruya la sesión
            fetch('api/logout', { method: 'POST' })
                .then(() => {
                    // 2. Limpiamos las variables de la memoria del navegador
                    sessionStorage.clear();
                    // 3. Redirigimos violentamente al login
                    window.location.href = 'index.html';
                })
                .catch(e => console.error("Error cerrando sesión:", e));
        });
    }
});