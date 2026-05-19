document.addEventListener('DOMContentLoaded', () => {
    cargarBiblioteca();
});

function cargarBiblioteca() {
    fetch('api/biblioteca')
        .then(respuesta => respuesta.json())
        .then(juegos => {
            const gridJuegos = document.getElementById('grid-juegos');
            const mensajeVacio = document.getElementById('mensaje-vacio-grid');

            if (juegos && juegos.length > 0) {
                mensajeVacio.style.display = 'none';
                gridJuegos.style.display = 'grid';
                gridJuegos.innerHTML = '';

                juegos.forEach(juego => {
                    const card = document.createElement('article');
                    card.className = 'card-biblioteca';

                    if(juego.caratulaUrl) {
                        card.style.backgroundImage = `url('${juego.caratulaUrl}')`;
                    } else {
                        card.style.backgroundColor = '#4B2272';
                    }

                    card.innerHTML = `
                        <div class="overlay-biblioteca">
                            <button class="btn-jugar">Jugar</button>
                        </div>
                    `;
                    gridJuegos.appendChild(card);
                });
            } else {
                mensajeVacio.style.display = 'block';
                gridJuegos.style.display = 'none';
            }
        })
        .catch(error => console.error(error));
}