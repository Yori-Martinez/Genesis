document.addEventListener('DOMContentLoaded', () => {
    const form = document.getElementById('formLogin');
    if (!form) return;
    form.addEventListener('submit', async event => {
        event.preventDefault();
        try {
            const response = await GenesisApi.request('api/login', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({
                    username: document.getElementById('username').value.trim(),
                    password: document.getElementById('password').value
                })
            });
            const data = await response.json();
            if (data.exito) window.location.href = 'tienda.html';
            else alert(data.mensaje);
        } catch (error) { alert(error.message); }
    });
});
