document.addEventListener('DOMContentLoaded', () => {
    cargarJuegosPendientes();
});


function cargarJuegosPendientes() {

    GenesisApi.request('api/revision')
        .then(respuesta => {

            if (!respuesta.ok) {
                throw new Error('Error en la petición al servidor');
            }

            return respuesta.json();
        })
        .then(juegos => {

            const listaContainer = document.getElementById('lista-revision');
            const mensajeVacio = document.getElementById('mensaje-vacio-revision');


            if (juegos.length === 0) {

                mensajeVacio.style.display = 'block';
            } else {

                mensajeVacio.style.display = 'none';


                juegos.forEach(juego => {

                    const item = document.createElement('div');
                    item.className = 'item-revision';
                    item.textContent = juego.titulo;


                    item.addEventListener('click', () => {
                        window.location.href = `revision.html?id=${juego.id}`;
                    });


                    listaContainer.appendChild(item);
                });
            }
        })
        .catch(error => {
            console.error("Fallo al cargar los juegos:", error);
        });
}