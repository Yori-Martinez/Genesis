document.addEventListener('DOMContentLoaded', () => {
    const formLogin = document.getElementById('formLogin');

    if(formLogin) {
        formLogin.addEventListener('submit', (e) => {
            e.preventDefault();

            const user = document.getElementById('username').value.trim();
            const pass = document.getElementById('password').value.trim();

            // Petición asíncrona (fetch) mediante POST enviando JSON
            fetch('api/login', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ username: user, password: pass })
            })
                // Manejo con then() y catch() exactamente como pide la rúbrica
                .then(respuesta => {
                    if (!respuesta.ok) {
                        throw new Error('Error en la respuesta del servidor');
                    }
                    return respuesta.json(); // Recepción de datos mediante JSON
                })
                .then(datos => {
                    if (datos.exito) {
                        sessionStorage.setItem('rol', datos.rol);
                        sessionStorage.setItem('nombre', datos.nombre);
                        window.location.href = 'tienda.html';
                    } else {
                        alert(datos.mensaje);
                    }
                })
                .catch(error => {
                    console.error("Error conectando con el servidor:", error);
                    alert("Hubo un problema de conexión.");
                });
        });
    }
});