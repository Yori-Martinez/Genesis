document.addEventListener('DOMContentLoaded', () => {
    cargarTienda();
});

function cargarTienda() {
    fetch('api/tienda')
        .then(respuesta => respuesta.json())
        .then(juegos => {
            if (!juegos || juegos.length === 0) return; // Si no hay juegos en la BD, no hacemos nada

            // 1. HERO BANNER: El primer juego del array es siempre el último en subirse (gracias al DESC de SQL)
            const juegoMasNuevo = juegos[0];
            actualizarHeroBanner(juegoMasNuevo);

            // 2. EL RESTO DE JUEGOS: Los separamos para la zona de abajo
            let restoJuegos = juegos.slice(1);

            // 3. LÓGICA DE BARAJADO: Gestionamos el contador de 5 refrescos
            restoJuegos = gestionarBarajado(restoJuegos);

            // 4. DIBUJAR LA CUADRÍCULA
            renderizarGrid(restoJuegos);
        })
        .catch(error => console.error('Error cargando la tienda:', error));
}

function actualizarHeroBanner(juego) {
    const heroBanner = document.getElementById('hero-banner');
    const heroTitulo = document.getElementById('hero-titulo');
    const heroDesc = document.getElementById('hero-desc');
    const btnCarritoHero = document.getElementById('hero-btn-carrito');

    // Inyectamos la imagen, el título y la descripción del último lanzamiento
    if (heroBanner) heroBanner.style.backgroundImage = `url('${juego.caratulaUrl}')`;
    if (heroTitulo) heroTitulo.textContent = juego.titulo;
    if (heroDesc) heroDesc.textContent = juego.descripcion;

    // Conectamos el botón para que guarde ESTE juego en la biblioteca
    if (btnCarritoHero) {
        btnCarritoHero.onclick = () => anadirAlCarrito(juego.id);
    }
}

function gestionarBarajado(juegos) {
    // 1. Leemos el contador de la memoria persistente del navegador
    let contador = parseInt(localStorage.getItem('refrescosTienda') || '0');
    contador++; // Sumamos un refresco
    localStorage.setItem('refrescosTienda', contador); // Lo guardamos actualizado

    // 2. Leemos el último orden de cartas guardado
    let ordenGuardado = JSON.parse(localStorage.getItem('ordenTienda') || 'null');

    // 3. ¿TOCA BARAJAR? (Si es múltiplo de 5, o si no hay orden guardado, o si se añadieron juegos nuevos)
    if (contador % 5 === 0 || !ordenGuardado || ordenGuardado.length !== juegos.length) {

        // Aplicamos el algoritmo matemático de barajado "Fisher-Yates"
        for (let i = juegos.length - 1; i > 0; i--) {
            const j = Math.floor(Math.random() * (i + 1));
            [juegos[i], juegos[j]] = [juegos[j], juegos[i]]; // Intercambio de posiciones
        }

        // Guardamos este nuevo orden aleatorio para mantenerlo durante los próximos 4 refrescos
        const nuevoOrdenIDs = juegos.map(j => j.id);
        localStorage.setItem('ordenTienda', JSON.stringify(nuevoOrdenIDs));

        return juegos;
    } else {
        // SI NO TOCA BARAJAR: Ordenamos los juegos siguiendo el array que teníamos guardado
        juegos.sort((a, b) => ordenGuardado.indexOf(a.id) - ordenGuardado.indexOf(b.id));
        return juegos;
    }
}

function renderizarGrid(juegos) {
    const grid = document.getElementById('grid-tienda'); // Asegúrate de que tu contenedor tenga este id o ajústalo
    if (!grid) return;

    grid.innerHTML = ''; // Limpiamos las cartas de carga

    juegos.forEach(juego => {
        const card = document.createElement('article');
        card.className = 'card';
        // Usamos background-size cover igual que en la biblioteca para evitar deformaciones
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