document.addEventListener('DOMContentLoaded', () => {
    const urlParams = new URLSearchParams(window.location.search);
    const juegoId = urlParams.get('id');

    if (juegoId) {
        obtenerDetalleJuego(juegoId);
        configurarBotonesDecision(juegoId);
    }
});

function obtenerDetalleJuego(id) {
    fetch(`api/revision?id=${id}`)
        .then(respuesta => {
            if (!respuesta.ok) {
                throw new Error('Error al obtener detalles');
            }
            return respuesta.json();
        })
        .then(juego => {
            if (juego) {
                document.getElementById('titulo').value = juego.titulo;
                document.getElementById('descripcion').value = juego.descripcion;
            }
        })
        .catch(error => {
            console.error(error);
        });
}

function configurarBotonesDecision(id) {
    const botones = document.querySelectorAll('.botones-decision .btn-enviar');

    if (botones.length >= 2) {
        const btnAceptar = botones[0];
        const btnDenegar = botones[1];

        btnAceptar.addEventListener('click', () => {
            enviarDecision(id, 'PUBLICADO');
        });

        btnDenegar.addEventListener('click', () => {
            enviarDecision(id, 'RECHAZADO');
        });
    }
}

function enviarDecision(id, estado) {
    fetch('api/revision', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ id: parseInt(id), estado: estado })
    })
        .then(respuesta => {
            if (!respuesta.ok) {
                throw new Error('Error en el servidor');
            }
            return respuesta.json();
        })
        .then(datos => {
            if (datos.exito) {
                alert(datos.mensaje);
                window.location.href = 'lista_revision.html';
            } else {
                alert(datos.mensaje);
            }
        })
        .catch(error => {
            console.error(error);
        });
}