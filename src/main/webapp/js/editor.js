document.addEventListener('DOMContentLoaded', () => {
    const formSubirJuego = document.getElementById('formSubirJuego');
    const inputZip = document.getElementById('zip');


    if (formSubirJuego) {
        formSubirJuego.addEventListener('submit', (e) => {
            e.preventDefault();

            const titulo = document.getElementById('titulo').value.trim();
            const descripcion = document.getElementById('descripcion').value.trim();


            if (inputZip.files.length === 0 || !inputZip.files[0].name.toLowerCase().endsWith('.zip')) {
                alert("Operación denegada: Es obligatorio adjuntar un archivo con extensión .zip.");
                return;
            }


            const formData = new FormData();
            formData.append('titulo', titulo);
            formData.append('descripcion', descripcion);
            formData.append('zip', inputZip.files[0]);

            GenesisApi.request('api/editor', {
                method: 'POST',
                body: formData
            })
                .then(respuesta => {
                    if (!respuesta.ok) throw new Error('Error en el servidor');
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
                    alert("No se pudo conectar. Asegúrate de que el archivo pesa menos de 50MB.");
                });
        });
    }
});