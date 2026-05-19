document.addEventListener('DOMContentLoaded', () => {
    cargarJuegosTienda();
});

function cargarJuegosTienda() {
    // Hacemos la petición asíncrona mediante GET (por defecto fetch hace GET)
    fetch('api/tienda')
        .then(respuesta => {
            if (!respuesta.ok) {
                throw new Error('Error al conectar con el servidor');
            }
            return respuesta.json(); // Recibimos y traducimos el JSON
        })
        .then(juegos => {
            const gridTienda = document.getElementById('grid-tienda');
            const mensajeVacio = document.getElementById('mensaje-vacio-tienda');

            if (juegos.length === 0) {
                // Si no hay juegos publicados, mostramos el mensaje de la tienda vacía
                gridTienda.style.display = 'none';
                mensajeVacio.style.display = 'block';
            } else {
                // Si hay juegos, ocultamos el mensaje vacío y mostramos el grid
                mensajeVacio.style.display = 'none';
                gridTienda.style.display = 'grid';

                gridTienda.innerHTML = ''; // Limpiamos el grid antes de pintar

                // Recorremos los juegos y creamos el HTML (igual que tu CSS original)
                juegos.forEach(juego => {
                    const card = document.createElement('article');
                    card.className = 'card card-p'; // Clases de tu CSS

                    // Si tiene carátula la ponemos, si no, un color por defecto
                    if(juego.caratulaUrl) {
                        card.style.backgroundImage = `url('${juego.caratulaUrl}')`;
                    } else {
                        card.style.backgroundColor = '#4B2272'; // Color morado de tu paleta
                    }

                    // Insertamos el título, precio y botón
                    card.innerHTML = `
                        <div class="overlay overlay-p" style="display: flex; flex-direction: column; justify-content: center; padding: 2rem;">
                            <h3 style="color: white; font-size: 2rem; font-family: 'Kokoro', sans-serif;">${juego.titulo}</h3>
                            <p style="color: rgba(255,255,255,0.7); font-size: 1.2rem; margin-top: 0.5rem;">${juego.precio} €</p>
                            <button class="btn btn-p" style="margin-top: 1rem;" onclick="anadirAlCarrito(${juego.id})">Al Carrito</button>
                        </div>
                    `;

                    gridTienda.appendChild(card);
                });
            }
        })
        .catch(error => {
            console.error("Hubo un problema:", error);
            // Si hay error (ej: servidor caído), mostramos el mensaje de tienda vacía por seguridad
            document.getElementById('grid-tienda').style.display = 'none';
            document.getElementById('mensaje-vacio-tienda').style.display = 'block';
        });

    
    function anadirAlCarrito(idJuego) {
        fetch('api/biblioteca', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ id_juego: idJuego })
        })
            .then(r => r.json())
            .then(datos => {
                alert(datos.mensaje);
            })
            .catch(e => console.error(e));
    }
}