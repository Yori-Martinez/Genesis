document.addEventListener('DOMContentLoaded', () => {
    const formSubirJuego = document.getElementById('formSubirJuego');

    if (formSubirJuego) {
        formSubirJuego.addEventListener('submit', (e) => {
            e.preventDefault();


            const titulo = document.getElementById('titulo').value.trim();
            const descripcion = document.getElementById('descripcion').value.trim();


            const inputZip = document.getElementById('zip');
            let nombreZip = "";
            if (inputZip.files.length > 0) {
                nombreZip = inputZip.files[0].name;
            }


            fetch('api/editor', {
                method: 'POST',
                headers: {'Content-Type': 'application/json'},
                body: JSON.stringify({
                    titulo: titulo,
                    descripcion: descripcion,
                    archivoZipUrl: nombreZip
                })
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
                        formSubirJuego.reset();
                    } else {
                        alert("Error: " + datos.mensaje);
                    }
                })
                .catch(error => {
                    console.error("Hubo un error:", error);
                    alert("No se pudo conectar con el servidor.");
                });
        });
    }
});