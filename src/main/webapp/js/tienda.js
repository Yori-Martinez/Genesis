document.addEventListener('DOMContentLoaded', () => {
    cargarTienda();
});

let juegosGlobal = [];

function cargarTienda() {
    GenesisApi.request('api/tienda')
        .then(respuesta => respuesta.json())
        .then(juegos => {
            if (!juegos || juegos.length === 0) return;


            const juegoMasNuevo = juegos[0];
            actualizarHeroBanner(juegoMasNuevo);


            let restoJuegos = juegos.slice(1);
            restoJuegos = gestionarBarajado(restoJuegos);
            renderizarGridDestacados(restoJuegos);
        })
        .catch(error => console.error('Error cargando la tienda:', error));
}

function actualizarHeroBanner(juego) {
    const heroBanner = document.getElementById('hero-banner');
    const heroTitulo = document.getElementById('hero-titulo');
    const heroDesc = document.getElementById('hero-desc');
    const btnCarritoHero = document.getElementById('hero-btn-carrito');

    if (heroBanner) heroBanner.style.backgroundImage = `url('${juego.caratulaUrl}')`;
    if (heroTitulo) heroTitulo.textContent = juego.titulo;
    if (heroDesc) heroDesc.textContent = juego.descripcion;

    if (btnCarritoHero) {
        btnCarritoHero.onclick = () => anadirAlCarrito(juego.id);
    }
}

function gestionarBarajado(juegos) {
    let contador = parseInt(localStorage.getItem('refrescosTienda') || '0');
    contador++;
    localStorage.setItem('refrescosTienda', contador);

    let ordenGuardado = JSON.parse(localStorage.getItem('ordenTienda') || 'null');

    if (contador % 5 === 0 || !ordenGuardado || ordenGuardado.length !== juegos.length) {
        for (let i = juegos.length - 1; i > 0; i--) {
            const j = Math.floor(Math.random() * (i + 1));
            [juegos[i], juegos[j]] = [juegos[j], juegos[i]];
        }
        const nuevoOrdenIDs = juegos.map(j => j.id);
        localStorage.setItem('ordenTienda', JSON.stringify(nuevoOrdenIDs));
        return juegos;
    } else {
        juegos.sort((a, b) => ordenGuardado.indexOf(a.id) - ordenGuardado.indexOf(b.id));
        return juegos;
    }
}

function renderizarGridDestacados(juegos) {
    const grid = document.getElementById('grid-destacados');
    if (!grid) return;

    grid.innerHTML = '';


    const colIzq = juegos.slice(0, 2);
    const colCen = juegos.slice(2, 4);
    const colDer = juegos.slice(4, 5);

    const grupos = [
        [colIzq, 'col-pequenas', 'card-p', 'overlay-p', 'btn-p'],
        [colCen, 'col-pequenas', 'card-p', 'overlay-p', 'btn-p'],
        [colDer, 'col-grande', 'card-g', 'overlay-g', 'btn-g']
    ];
    grupos.forEach(([juegos, columna, card, overlay, boton]) => {
        const contenedor = document.createElement('div');
        contenedor.className = columna;
        juegos.forEach(juego => contenedor.appendChild(generarTarjeta(juego, card, overlay, boton)));
        grid.appendChild(contenedor);
    });
}

function generarTarjeta(juego, claseCard, claseOverlay, claseBtn) {
    const card = document.createElement('article');
    card.className = 'card ' + claseCard;
    card.style.backgroundImage = `url('${juego.caratulaUrl}')`;
    const overlay = document.createElement('div');
    overlay.className = 'overlay ' + claseOverlay;
    const superior = document.createElement('div');
    superior.className = 'bloque-superior-card';
    const titulo = document.createElement('h3');
    titulo.className = 'titulo-juego-card';
    titulo.textContent = juego.titulo;
    const descripcion = document.createElement('p');
    descripcion.className = 'desc-juego-card';
    descripcion.textContent = juego.descripcion || '';
    superior.append(titulo, descripcion);
    const boton = document.createElement('button');
    boton.className = 'btn ' + claseBtn;
    boton.textContent = 'Al Carrito';
    boton.addEventListener('click', () => anadirAlCarrito(juego.id));
    overlay.append(superior, boton);
    card.appendChild(overlay);
    return card;
}

function anadirAlCarrito(idJuego) {
    GenesisApi.request('api/biblioteca', {
        method: 'POST',
        headers: {'Content-Type': 'application/json'},
        body: JSON.stringify({id_juego: idJuego})
    })
        .then(r => r.json())
        .then(datos => {
            alert(datos.mensaje);
        })
        .catch(e => alert(e.message));
}