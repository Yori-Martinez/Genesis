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

    // =========================================================================
    // GESTIÓN DEL MENÚ DESPLEGABLE DE USUARIO (CLICK IZQUIERDO)
    // =========================================================================
    const userSlot = document.querySelector('.user-slot');
    const userAvatar = document.querySelector('.user-avatar');

    if (userSlot && nombreUsuario !== 'Invitado') {
        userSlot.style.position = 'relative';
        userSlot.style.cursor = 'pointer';

        // 1. Intentamos cargar el avatar dinámico de la base de datos
        if (userAvatar) {
            userAvatar.style.backgroundImage = "url('api/avatar')";
            userAvatar.style.backgroundSize = "cover";
            userAvatar.style.backgroundPosition = "center";
        }

        // 2. Creamos la caja del menú desplegable estructurada con estilos limpios
        const dropdownMenu = document.createElement('div');
        dropdownMenu.style.cssText = `
            position: absolute;
            top: 115%;
            right: 0;
            background-color: #37105B;
            border: 2px solid #8F1952;
            border-radius: 8px;
            box-shadow: 0 6px 16px rgba(0,0,0,0.6);
            display: none;
            flex-direction: column;
            z-index: 3000;
            min-width: 190px;
            overflow: hidden;
        `;

        // Elemento: Cambiar Foto
        const btnCambiarFoto = document.createElement('div');
        btnCambiarFoto.textContent = 'Cambiar Foto';
        btnCambiarFoto.style.cssText = `
            padding: 0.9rem 1.3rem;
            color: white;
            font-size: 1.2rem;
            font-family: sans-serif;
            cursor: pointer;
            transition: background 0.2s;
        `;
        btnCambiarFoto.addEventListener('mouseover', () => btnCambiarFoto.style.backgroundColor = '#8F1952');
        btnCambiarFoto.addEventListener('mouseout', () => btnCambiarFoto.style.backgroundColor = 'transparent');

        // Elemento: Cerrar Sesión
        const btnLogout = document.createElement('div');
        btnLogout.textContent = 'Cerrar Sesión';
        btnLogout.style.cssText = `
            padding: 0.9rem 1.3rem;
            color: white;
            font-size: 1.2rem;
            font-family: sans-serif;
            cursor: pointer;
            border-top: 1px solid rgba(255,255,255,0.1);
            transition: background 0.2s;
        `;
        btnLogout.addEventListener('mouseover', () => btnLogout.style.backgroundColor = '#8F1952');
        btnLogout.addEventListener('mouseout', () => btnLogout.style.backgroundColor = 'transparent');

        // Input de tipo archivo oculto para gestionar la ventana del sistema operativo
        const fileInput = document.createElement('input');
        fileInput.type = 'file';
        fileInput.accept = 'image/png, image/jpeg';
        fileInput.style.display = 'none';

        // Armamos el árbol de nodos del DOM
        dropdownMenu.appendChild(btnCambiarFoto);
        dropdownMenu.appendChild(btnLogout);
        userSlot.appendChild(dropdownMenu);
        userSlot.appendChild(fileInput);

        // EVENTO 1: Alternar apertura y cierre al hacer click izquierdo en el slot de usuario
        userSlot.addEventListener('click', (e) => {
            if (dropdownMenu.contains(e.target) || e.target === fileInput) return;
            dropdownMenu.style.display = dropdownMenu.style.display === 'none' ? 'flex' : 'none';
        });

        // EVENTO 2: Cerrar el menú si el usuario hace click fuera del componente
        document.addEventListener('click', (e) => {
            if (!userSlot.contains(e.target)) {
                dropdownMenu.style.display = 'none';
            }
        });

        // EVENTO 3: Ejecutar la llamada al selector de archivos al pulsar "Cambiar Foto"
        btnCambiarFoto.addEventListener('click', (e) => {
            e.stopPropagation(); // Evitamos que se cierre el menú antes de tiempo
            dropdownMenu.style.display = 'none';
            fileInput.click();
        });

        // EVENTO 4: Procesar el envío binario Multipart de la imagen seleccionada
        fileInput.addEventListener('change', () => {
            if (fileInput.files.length === 0) return;

            const formData = new FormData();
            formData.append('avatar', fileInput.files[0]);

            fetch('api/avatar', {
                method: 'POST',
                body: formData
            })
                .then(r => r.json())
                .then(datos => {
                    if (datos.exito) {
                        alert(datos.mensaje);
                        // Forzamos al elemento HTML a recargar la foto añadiendo un parámetro de tiempo para destruir la caché
                        if (userAvatar) {
                            userAvatar.style.backgroundImage = `url('api/avatar?t=${new Date().getTime()}')`;
                        }
                    } else {
                        alert("Error: " + datos.mensaje);
                    }
                })
                .catch(e => console.error("Error en la subida del avatar:", e));
        });

        // EVENTO 5: Lógica de destrucción de la sesión
        btnLogout.addEventListener('click', (e) => {
            e.stopPropagation();
            fetch('api/logout', { method: 'POST' })
                .then(() => {
                    sessionStorage.clear();
                    window.location.href = 'index.html';
                })
                .catch(e => console.error("Error cerrando sesión:", e));
        });
    }
});