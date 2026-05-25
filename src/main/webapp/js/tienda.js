document.addEventListener('DOMContentLoaded', () => {
    cargarTienda();
});

let juegosGlobal = [];

function cargarTienda() {
    fetch('api/tienda')
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

    let htmlFinal = '';


    htmlFinal += `<div class="col-pequenas">`;
    colIzq.forEach(j => {
        htmlFinal += generarHTMLTarjeta(j, 'card-p', 'overlay-p', 'btn-p');
    });
    htmlFinal += `</div>`;


    htmlFinal += `<div class="col-pequenas">`;
    colCen.forEach(j => {
        htmlFinal += generarHTMLTarjeta(j, 'card-p', 'overlay-p', 'btn-p');
    });
    htmlFinal += `</div>`;


    htmlFinal += `<div class="col-grande">`;
    colDer.forEach(j => {
        htmlFinal += generarHTMLTarjeta(j, 'card-g', 'overlay-g', 'btn-g');
    });
    htmlFinal += `</div>`;

    grid.innerHTML = htmlFinal;
}

function generarHTMLTarjeta(juego, claseCard, claseOverlay, claseBtn) {

    return `
        <article class="card ${claseCard}" style="background-image: url('${juego.caratulaUrl}');">
            <div class="overlay ${claseOverlay}">
                <div class="bloque-superior-card">
                    <h3 class="titulo-juego-card">${juego.titulo}</h3>
                    <p class="desc-juego-card">${juego.descripcion || ''}</p>
                </div>
                <button class="btn ${claseBtn}" onclick="anadirAlCarrito(${juego.id})">Al Carrito</button>
            </div>
        </article>
    `;
}

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