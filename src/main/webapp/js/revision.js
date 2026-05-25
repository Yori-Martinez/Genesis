document.addEventListener('DOMContentLoaded', () => {
    const urlParams = new URLSearchParams(window.location.search);
    const juegoId = urlParams.get('id');

    if (juegoId) {
        obtenerDetalleJuego(juegoId);
        configurarBotonesDecision(juegoId);
    }
});

document.getElementById('caratulaFile').addEventListener('change', function () {
    const archivo = this.files[0];
    document.getElementById('nombreArchivoSel').textContent = archivo ? archivo.name : "Ningún archivo seleccionado";
});

function obtenerDetalleJuego(id) {
    fetch(`api/revision?id=${id}`)
        .then(respuesta => {
            if (!respuesta.ok) throw new Error('Error al obtener detalles');
            return respuesta.json();
        })
        .then(juego => {
            if (juego) {
                document.getElementById('titulo').value = juego.titulo;
                document.getElementById('descripcion').value = juego.descripcion;


                const btnDescarga = document.querySelector('.btn-archivo');
                if (btnDescarga) {
                    btnDescarga.href = juego.archivoZipUrl;
                }
            }
        })
        .catch(error => console.error(error));
}

function configurarBotonesDecision(id) {
    const botones = document.querySelectorAll('.botones-decision .btn-enviar');

    if (botones.length >= 2) {
        const btnAceptar = botones[0];
        const btnDenegar = botones[1];

        btnAceptar.addEventListener('click', () => {
            const archivoImagen = document.getElementById('caratulaFile').files[0];
            if (!archivoImagen) {
                alert("Debes cargar una carátula desde tu equipo para poder aprobar el videojuego.");
                return;
            }
            enviarDecisionForm(id, 'PUBLICADO', archivoImagen);
        });

        btnDenegar.addEventListener('click', () => {

            enviarDecisionForm(id, 'RECHAZADO', null);
        });
    }
}

function enviarDecisionForm(id, estado, archivoImagen) {
    const paqueteDatos = new FormData();
    paqueteDatos.append("id", id);
    paqueteDatos.append("estado", estado);

    if (archivoImagen) {
        paqueteDatos.append("caratula", archivoImagen);
    }

    fetch('api/revision', {
        method: 'POST',
        body: paqueteDatos
    })
        .then(r => r.json())
        .then(datos => {
            if (datos.exito) {
                alert(estado === 'PUBLICADO' ? "Juego publicado de forma persistente." : "Juego rechazado. Ha sido eliminado de la cola.");
                window.location.href = 'lista_revision.html';
            } else {
                alert("Error: " + datos.mensaje);
            }
        })
        .catch(e => console.error("Fallo de red:", e));
}