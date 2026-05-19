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
});