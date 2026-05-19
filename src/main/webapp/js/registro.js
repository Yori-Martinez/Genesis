document.addEventListener('DOMContentLoaded', () => {
    const formRegistro = document.getElementById('formRegistro');
    if (formRegistro) {
        formRegistro.addEventListener('submit', (e) => {
            e.preventDefault();
            const user = document.getElementById('reg-usuario').value.trim();
            const email = document.getElementById('reg-email').value.trim();
            const pass = document.getElementById('reg-password').value.trim();
            const passConfirm = document.getElementById('reg-password-confirm').value.trim();

            if (pass !== passConfirm) {
                alert("Las contraseñas no coinciden");
                return;
            }

            fetch('api/registro', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ username: user, email: email, password: pass })
            })
                .then(r => r.json())
                .then(datos => {
                    if (datos.exito) {
                        alert(datos.mensaje);
                        window.location.href = 'index.html';
                    } else {
                        alert(datos.mensaje);
                    }
                })
                .catch(e => console.error(e));
        });
    }
});