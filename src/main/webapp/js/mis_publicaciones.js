document.addEventListener('DOMContentLoaded', () => {
    cargarMisPublicaciones();
});

function cargarMisPublicaciones() {
    fetch('api/mispublicaciones')
        .then(respuesta => {
            if (!respuesta.ok) {
                throw new Error('Error en la peticion');
            }
            return respuesta.json();
        })
        .then(juegos => {
            const listaContainer = document.getElementById('lista-mis-publicaciones');
            const mensajeVacio = document.getElementById('mensaje-vacio-publicaciones');

            if (juegos.length === 0) {
                mensajeVacio.style.display = 'block';
            } else {
                mensajeVacio.style.display = 'none';
                juegos.forEach(juego => {
                    const item = document.createElement('div');
                    item.className = 'item-revision';

                    // Modificamos el estilo de tu CSS por defecto para empujar el texto a los lados
                    item.style.justifyContent = 'space-between';
                    item.style.cursor = 'default'; // Quitamos la mano de click porque aquí no hace nada

                    // Izquierda: El título
                    const tituloSpan = document.createElement('span');
                    tituloSpan.textContent = juego.titulo;

                    // Derecha: El estado (Mapeamos la BD a lo que pediste visualmente)
                    const estadoSpan = document.createElement('span');
                    estadoSpan.style.fontWeight = 'bold';
                    estadoSpan.style.textTransform = 'uppercase';
                    estadoSpan.style.fontSize = '1rem';

                    if (juego.estado === 'PENDIENTE') {
                        estadoSpan.textContent = 'Espera';
                        estadoSpan.style.color = '#F5B041'; // Amarillo/Naranja
                    } else if (juego.estado === 'RECHAZADO') {
                        estadoSpan.textContent = 'Denegado';
                        estadoSpan.style.color = '#E74C3C'; // Rojo
                    } else if (juego.estado === 'PUBLICADO') {
                        estadoSpan.textContent = 'Aprobado';
                        estadoSpan.style.color = '#2ECC71'; // Verde
                    }

                    item.appendChild(tituloSpan);
                    item.appendChild(estadoSpan);

                    listaContainer.appendChild(item);
                });
            }
        })
        .catch(error => {
            console.error(error);
        });
}