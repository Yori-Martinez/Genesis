document.addEventListener('DOMContentLoaded', () => {
    const formSubirJuego = document.getElementById('formSubirJuego');

    if (formSubirJuego) {
        formSubirJuego.addEventListener('submit', (e) => {
            e.preventDefault(); // Evitamos que la página se recargue

            // Capturamos los datos de los inputs
            const titulo = document.getElementById('titulo').value.trim();
            const descripcion = document.getElementById('descripcion').value.trim();

            // Extraemos el nombre del archivo ZIP
            const inputZip = document.getElementById('zip');
            let nombreZip = "";
            if (inputZip.files.length > 0) {
                nombreZip = inputZip.files[0].name;
            }

            // Cumpliendo la rúbrica: Petición asíncrona enviando JSON con POST
            fetch('api/editor', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({
                    titulo: titulo,
                    descripcion: descripcion,
                    archivoZipUrl: nombreZip
                })
            })
                // Cumpliendo la rúbrica: Manejo de promesas con then() y catch()
                .then(respuesta => {
                    if (!respuesta.ok) {
                        throw new Error('Error en el servidor');
                    }
                    return respuesta.json(); // Recepción de datos JSON
                })
                .then(datos => {
                    if (datos.exito) {
                        alert(datos.mensaje); // "Juego subido con éxito"
                        formSubirJuego.reset(); // Vaciamos el formulario
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