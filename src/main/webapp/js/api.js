// The browser may display permissions; only Java decides whether an action is allowed.
window.GenesisApi = (() => {
    let sessionPromise;
    let csrfToken;

    async function failure(response) {
        let message = response.status === 429 ? 'Demasiados intentos. Espera un minuto.'
            : response.status === 403 ? 'Permisos insuficientes o sesión caducada. Recarga la página.'
            : 'No se pudo completar la petición.';
        try {
            const body = await response.json();
            if (typeof body.mensaje === 'string') message = body.mensaje;
        } catch (_) {}
        const error = new Error(message);
        error.status = response.status;
        return error;
    }

    function session(refresh = false) {
        if (refresh) { sessionPromise = undefined; csrfToken = undefined; }
        if (!sessionPromise) {
            sessionPromise = fetch('api/sesion', {
                credentials: 'same-origin',
                cache: 'no-store',
                headers: { 'X-CSRF-Token': 'Fetch' }
            }).then(async response => {
                if (!response.ok) throw await failure(response);
                csrfToken = response.headers.get('X-CSRF-Token');
                return response.json();
            }).catch(error => { sessionPromise = undefined; csrfToken = undefined; throw error; });
        }
        return sessionPromise;
    }

    async function request(url, options = {}) {
        const method = (options.method || 'GET').toUpperCase();
        const headers = new Headers(options.headers || {});
        if (!['GET', 'HEAD', 'OPTIONS'].includes(method)) {
            await session();
            if (!csrfToken) throw new Error('No se pudo verificar la sesión. Recarga la página.');
            headers.set('X-CSRF-Token', csrfToken);
        }
        const response = await fetch(url, { ...options, method, headers, credentials: 'same-origin' });
        if (!response.ok) {
            if (response.status === 401 && !url.startsWith('api/login')) {
                window.location.href = 'index.html';
            }
            throw await failure(response);
        }
        if (url === 'api/login') await session(true);
        return response;
    }

    return { session, request };
})();
