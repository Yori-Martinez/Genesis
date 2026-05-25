document.addEventListener('DOMContentLoaded', () => {
    cargarBiblioteca();
});

let juegosGlobal = [];

function cargarBiblioteca() {
    fetch('api/biblioteca')
        .then(respuesta => {
            if (!respuesta.ok) throw new Error('Debes iniciar sesión para ver tu biblioteca.');
            return respuesta.json();
        })
        .then(juegos => {
            juegosGlobal = juegos;
            renderizarGridBiblioteca(juegos);
            renderizarSidebar(juegos);
            configurarBuscador();
        })
        .catch(error => {
            console.error("Error al cargar biblioteca:", error);
            mostrarMensajeCentral(error.message);
        });
}


function mostrarMensajeCentral(mensajeHtml) {
    const grid = document.getElementById('grid-juegos');
    const msjContainer = document.getElementById('mensaje-vacio-grid');

    if (grid) grid.style.display = 'none';
    if (msjContainer) {
        msjContainer.style.display = 'block';
        msjContainer.innerHTML = `<h2>${mensajeHtml}</h2>`;
    }
}

function renderizarGridBiblioteca(juegos) {
    const grid = document.getElementById('grid-juegos');
    const msjContainer = document.getElementById('mensaje-vacio-grid');

    if (!grid) return;

    if (juegos.length === 0) {

        mostrarMensajeCentral("Agrega algún juego a tu biblioteca para viciar 🎮");
        return;
    }


    if (msjContainer) msjContainer.style.display = 'none';
    grid.style.display = 'grid';
    grid.innerHTML = '';

    juegos.forEach(juego => {
        const card = document.createElement('article');
        card.className = 'card-biblioteca';
        card.style.backgroundImage = `url('${juego.caratulaUrl}')`;

        card.innerHTML = `
            <div class="overlay-biblioteca">
                <button class="btn-jugar" onclick="jugar('${juego.archivoZipUrl}')">JUGAR</button>
            </div>
        `;
        grid.appendChild(card);
    });
}

function renderizarSidebar(juegos) {
    const contenedorSidebar = document.getElementById('lista-lateral-juegos');
    if (!contenedorSidebar) return;

    if (juegos.length === 0) {
        contenedorSidebar.innerHTML = `
            <div class="mensaje-vacio-sidebar" id="mensaje-vacio-sidebar">
                Aún no tienes títulos.
            </div>`;
        return;
    }

    contenedorSidebar.innerHTML = '';
    juegos.forEach(juego => {
        const item = document.createElement('div');
        item.className = 'item-juego';

        item.innerHTML = `
            <img src="${juego.caratulaUrl}" alt="Carátula" class="icono-juego">
            <span class="texto-juego" title="${juego.titulo}">${juego.titulo}</span>
        `;
        contenedorSidebar.appendChild(item);
    });
}

function configurarBuscador() {

    const inputBuscador = document.querySelector('.input-buscador');
    if (!inputBuscador) return;

    inputBuscador.addEventListener('input', (e) => {
        const textoBusqueda = e.target.value.toLowerCase();

        const juegosFiltrados = juegosGlobal.filter(juego =>
            juego.titulo.toLowerCase().includes(textoBusqueda) ||
            (juego.descripcion && juego.descripcion.toLowerCase().includes(textoBusqueda))
        );


        renderizarGridBiblioteca(juegosFiltrados);
        renderizarSidebar(juegosFiltrados);
    });
}

function jugar(url) {
    if (url && url !== 'null') {
        window.location.href = url;
    } else {
        alert("Archivo no disponible.");
    }
}