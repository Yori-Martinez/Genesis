document.addEventListener('DOMContentLoaded', () => {
    cargarTienda();
});

let juegosGlobal = [];

function cargarTienda() {
    fetch('api/tienda')
        .then(respuesta => respuesta.json())
        .then(juegos => {
            if (!juegos || juegos.length === 0) return;

            // 1. HERO BANNER: El primer elemento del array va arriba por ser el más reciente
            const juegoMasNuevo = juegos[0];
            actualizarHeroBanner(juegoMasNuevo);

            // 2. SECCIÓN DESTACADOS: El resto de juegos pueblan el grid inferior
            let restoJuegos = juegos.slice(1);

            // 3. CONTROL DE REFRESCOS: Algoritmo Fisher-Yates cada 5 cargas
            restoJuegos = gestionarBarajado(restoJuegos);

            // 4. RENDERIZADO FINAL
            renderizarGrid(restoJuegos);
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

function renderizarGrid(juegos) {
    const grid = document.getElementById('grid-tienda');
    if (!grid) return;

    grid.innerHTML = '';

    juegos.forEach(juego => {
        const card = document.createElement('article');
        card.className = 'card';
        card.style.backgroundImage = `url('${juego.caratulaUrl}')`;
        card.style.backgroundSize = 'cover';
        card.style.backgroundPosition = 'center';

        card.innerHTML = `
            <div class="overlay overlay-p" style="display: flex; flex-direction: column; justify-content: center; padding: 2rem;">
                <h3 style="color: white; font-size: 2rem; font-family: 'Kokoro', sans-serif;">${juego.titulo}</h3>
                <p style="color: rgba(255,255,255,0.7); font-size: 1.2rem; margin-top: 0.5rem;">${juego.precio} €</p>
                <button class="btn btn-p" style="margin-top: 1rem;" onclick="anadirAlCarrito(${juego.id})">Al Carrito</button>
            </div>
        `;
        grid.appendChild(card);
    });
}

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