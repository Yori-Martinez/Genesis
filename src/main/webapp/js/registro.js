document.addEventListener('DOMContentLoaded', () => {
    const formRegistro = document.getElementById('formRegistro');
    if (formRegistro) {
        formRegistro.addEventListener('submit', (e) => {
            e.preventDefault();
            const user = document.getElementById('reg-usuario').value.trim();
            const email = document.getElementById('reg-email').value.trim();
            const pass = document.getElementById('reg-password').value;
            const passConfirm = document.getElementById('reg-password-confirm').value;

            if ([...pass].length < 15 || [...pass].length > 128) {
                alert('La contraseña debe tener entre 15 y 128 caracteres.');
                return;
            }
            if (pass !== passConfirm) {
                alert("Las contraseñas no coinciden");
                return;
            }

            GenesisApi.request('api/registro', {
                method: 'POST',
                headers: {'Content-Type': 'application/json'},
                body: JSON.stringify({username: user, email: email, password: pass})
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
                .catch(e => alert(e.message));
        });
    }
});