// Este evento "DOMContentLoaded" espera a que todo el HTML esté dibujado en la pantalla antes de ejecutar el código
document.addEventListener('DOMContentLoaded', () => {
    cargarJuegosPendientes();
});

// Función principal que conecta con el Backend
function cargarJuegosPendientes() {
    // 1. Hacemos la petición GET a nuestro Servlet de Java
    fetch('api/revision')
        .then(respuesta => {
            // Si el servidor devuelve un error (ej: 403 Forbidden), lanzamos una excepción
            if (!respuesta.ok) {
                throw new Error('Error en la petición al servidor');
            }
            // Transformamos la respuesta de texto plano a objetos JSON de JavaScript
            return respuesta.json();
        })
        .then(juegos => {
            // Capturamos los contenedores del HTML usando sus IDs
            const listaContainer = document.getElementById('lista-revision');
            const mensajeVacio = document.getElementById('mensaje-vacio-revision');

            // 2. Lógica de renderizado
            if (juegos.length === 0) {
                // Si la lista viene vacía de MySQL, mostramos el mensaje de "Buen trabajo"
                mensajeVacio.style.display = 'block';
            } else {
                // Si hay juegos, ocultamos el mensaje vacío
                mensajeVacio.style.display = 'none';

                // Recorremos la lista de juegos pendientes
                juegos.forEach(juego => {
                    // Creamos un <div> "al vuelo" por cada juego encontrado
                    const item = document.createElement('div');
                    item.className = 'item-revision';
                    item.textContent = juego.titulo; // Le ponemos el nombre del juego

                    // Le añadimos la interactividad: al hacer clic, viaja a la pantalla de detalles pasándole el ID en la URL
                    item.addEventListener('click', () => {
                        window.location.href = `revision.html?id=${juego.id}`;
                    });

                    // Finalmente, inyectamos ese <div> dentro de nuestra lista en el HTML
                    listaContainer.appendChild(item);
                });
            }
        })
        .catch(error => {
            console.error("Fallo al cargar los juegos:", error);
        });
}