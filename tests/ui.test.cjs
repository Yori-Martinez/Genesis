const { test } = require('node:test');
const assert = require('node:assert/strict');
const fs = require('node:fs');
const path = require('node:path');
const vm = require('node:vm');
const js = name => fs.readFileSync(path.join(__dirname, '../src/main/webapp/js', name), 'utf8');

// Deliberately reject HTML parsing in this DOM double: untrusted values must be text,
// including markup which a browser would execute through innerHTML.
class Element {
    constructor(tag) { this.tagName = tag; this.children = []; this.style = {}; this.listeners = {}; }
    appendChild(child) { this.children.push(child); return child; }
    append(...children) { children.forEach(child => this.appendChild(child)); }
    replaceChildren(...children) { this.children = []; this.append(...children); }
    addEventListener(event, callback) { this.listeners[event] = callback; }
    set innerHTML(value) {
        assert.equal(value, '', 'Dynamic content must not be parsed as HTML');
        this.children = [];
    }
}
function documentWith(ids = {}) {
    const listeners = {};
    return {
        ids, listeners,
        getElementById: id => ids[id] || null,
        createElement: tag => new Element(tag),
        addEventListener: (event, cb) => { listeners[event] = cb; },
        querySelector: () => null,
        querySelectorAll: () => []
    };
}
function context(document, extra = {}) {
    return vm.createContext({ document, console, FormData, Blob, alert: () => {}, window: { location: {} }, ...extra });
}
function descendants(element) { return [element, ...element.children.flatMap(descendants)]; }

test('store cards render hostile titles and descriptions as literal text', () => {
    const grid = new Element('div');
    const document = documentWith({ 'grid-destacados': grid });
    const ctx = context(document);
    vm.runInContext(js('tienda.js'), ctx);
    const payload = '<img src=x onerror="window.attacked=true">';
    ctx.games = [{ id: 10, titulo: payload, descripcion: payload, caratulaUrl: 'api/caratula?id=10' }];
    vm.runInContext('renderizarGridDestacados(games)', ctx);
    const rendered = descendants(grid);
    assert.equal(rendered.find(el => el.tagName === 'h3').textContent, payload);
    assert.equal(rendered.find(el => el.tagName === 'p').textContent, payload);
    assert.equal(rendered.filter(el => el.tagName === 'img').length, 0);
    assert.equal(ctx.window.attacked, undefined);
});

test('library renders hostile titles and error messages as text and binds download without inline code', () => {
    const list = new Element('div'), grid = new Element('div'), message = new Element('div');
    const document = documentWith({ 'lista-lateral-juegos': list, 'grid-juegos': grid, 'mensaje-vacio-grid': message });
    const ctx = context(document);
    vm.runInContext(js('biblioteca.js'), ctx);
    const payload = '"><svg onload="window.attacked=true">';
    ctx.games = [{ id: 10, titulo: payload, caratulaUrl: 'api/caratula?id=10', archivoZipUrl: 'api/zip?id=10' }];
    vm.runInContext('renderizarSidebar(games); renderizarGridBiblioteca(games)', ctx);
    const title = descendants(list).find(el => el.tagName === 'span');
    assert.equal(title.textContent, payload);
    assert.equal(title.title, payload);
    const button = descendants(grid).find(el => el.tagName === 'button');
    button.listeners.click();
    assert.equal(ctx.window.location.href, 'api/zip?id=10');
    ctx.payload = payload;
    vm.runInContext('mostrarMensajeCentral(payload)', ctx);
    assert.equal(message.children[0].textContent, payload);
    assert.equal(ctx.window.attacked, undefined);
});

test('forged sessionStorage administrator role is ignored by the interface', async () => {
    const admin = new Element('a'), editor = new Element('a'), username = new Element('span');
    const document = documentWith({ username });
    document.querySelectorAll = selector => selector === '.req-editor, .req-admin' ? [admin, editor]
        : selector === '.req-admin' ? [admin] : [editor];
    const ctx = context(document, {
        sessionStorage: { getItem() { throw new Error('Browser role must never be trusted'); } },
        GenesisApi: { session: async () => ({ autenticado: true, nombre: 'Player from server', rol: 'JUGADOR' }) }
    });
    vm.runInContext(js('auth.js'), ctx);
    await document.listeners.DOMContentLoaded();
    assert.equal(admin.style.display, 'none');
    assert.equal(editor.style.display, 'none');
    assert.equal(username.textContent, 'Player from server');
});

test('API helper bootstraps CSRF, refreshes after login and sends the new nonce', async () => {
    const calls = [];
    let bootstraps = 0;
    const ctx = context(documentWith(), {
        Headers,
        fetch: async (url, options) => {
            calls.push({ url, options });
            if (url === 'api/sesion') {
                bootstraps++;
                return new Response(JSON.stringify({ autenticado: bootstraps > 1, rol: bootstraps > 1 ? 'JUGADOR' : null }),
                    { headers: { 'X-CSRF-Token': bootstraps === 1 ? 'before-login' : 'after-login' } });
            }
            return new Response(JSON.stringify({ exito: true }));
        }
    });
    vm.runInContext(js('api.js'), ctx);
    await ctx.window.GenesisApi.request('api/login', { method: 'POST', body: '{}' });
    await ctx.window.GenesisApi.request('api/biblioteca', { method: 'POST', body: '{"id_juego":10}' });
    assert.equal(calls.find(c => c.url === 'api/login').options.headers.get('X-CSRF-Token'), 'before-login');
    assert.equal(calls.find(c => c.url === 'api/biblioteca').options.headers.get('X-CSRF-Token'), 'after-login');
    assert.equal(bootstraps, 2);
});

test('API helper refuses modifying calls without a CSRF token', async () => {
    const calls = [];
    const ctx = context(documentWith(), { Headers, fetch: async url => {
        calls.push(url);
        return new Response(JSON.stringify({ autenticado: false }));
    } });
    vm.runInContext(js('api.js'), ctx);
    await assert.rejects(ctx.window.GenesisApi.request('api/registro', { method: 'POST' }), /verificar la sesión/);
    assert.deepEqual(calls, ['api/sesion']);
});

test('every page loads the API helper before its application scripts', () => {
    const folder = path.join(__dirname, '../src/main/webapp');
    for (const name of fs.readdirSync(folder).filter(name => name.endsWith('.html'))) {
        const html = fs.readFileSync(path.join(folder, name), 'utf8');
        assert.match(html, /<script src="js\/api.js"><\/script>/);
        assert.equal(html.indexOf('<script src='), html.indexOf('<script src="js/api.js">'));
    }
});
