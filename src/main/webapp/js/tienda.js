document.addEventListener('DOMContentLoaded', () => {
    cargarJuegosTienda();
});

function cargarJuegosTienda() {

    fetch('api/tienda')
        .then(respuesta => {
            if (!respuesta.ok) {
                throw new Error('Error al conectar con el servidor');
            }
            return respuesta.json();
        })
        .then(juegos => {
            const gridTienda = document.getElementById('grid-tienda');
            const mensajeVacio = document.getElementById('mensaje-vacio-tienda');

            if (juegos.length === 0) {

                gridTienda.style.display = 'none';
                mensajeVacio.style.display = 'block';
            } else {

                mensajeVacio.style.display = 'none';
                gridTienda.style.display = 'grid';

                gridTienda.innerHTML = '';


                juegos.forEach(juego => {
                    const card = document.createElement('article');
                    card.className = 'card card-p';


                    if (juego.caratulaUrl) {
                        card.style.backgroundImage = `url('${juego.caratulaUrl}')`;
                    } else {
                        card.style.backgroundColor = '#4B2272';
                    }


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

            document.getElementById('grid-tienda').style.display = 'none';
            document.getElementById('mensaje-vacio-tienda').style.display = 'block';
        });


    function anadirAlCarrito(idJuego) {
        fetch('api/biblioteca', {
            method: 'POST',
            headers: {'Content-Type': 'application/json'},
            body: JSON.stringify({id_juego: idJuego})
        })
            .then(r => r.json())
            .then(datos => {
                alert(datos.mensaje);
            })
            .catch(e => console.error(e));
    }
}