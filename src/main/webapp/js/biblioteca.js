document.addEventListener('DOMContentLoaded', () => {
    cargarBiblioteca();
});

let juegosGlobal = [];

function cargarBiblioteca() {
    GenesisApi.request('api/biblioteca')
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
        const titulo = document.createElement('h2');
        titulo.textContent = mensajeHtml;
        msjContainer.replaceChildren(titulo);
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

        const overlay = document.createElement('div');
        overlay.className = 'overlay-biblioteca';
        const boton = document.createElement('button');
        boton.className = 'btn-jugar';
        boton.textContent = 'JUGAR';
        boton.addEventListener('click', () => jugar(juego.archivoZipUrl));
        overlay.appendChild(boton);
        card.appendChild(overlay);
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

        const imagen = document.createElement('img');
        imagen.src = juego.caratulaUrl;
        imagen.alt = 'Carátula';
        imagen.className = 'icono-juego';
        const titulo = document.createElement('span');
        titulo.className = 'texto-juego';
        titulo.title = juego.titulo;
        titulo.textContent = juego.titulo;
        item.append(imagen, titulo);
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