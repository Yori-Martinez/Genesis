# Seguridad de Genesis

Esta actualización mantiene Java, Tomcat 11, MySQL y el diseño existente. Las API verifican la identidad, el rol vigente y, cuando corresponde, la propiedad del recurso en el servidor.

## Antes de actualizar una instalación existente

1. Renueva la credencial de MySQL que estaba incluida en el código público. Retirarla del último commit no la elimina del historial.
2. Configura las variables de `.env.example` en el proceso Java y en la herramienta interna. No se carga automáticamente un archivo .env. Producción requiere HTTPS y MySQL con TLS y certificado válido (`VERIFY_IDENTITY`). Para pruebas locales explícitamente usa `APP_ENV=development`.
3. Detén la aplicación y guarda una copia de seguridad de la base de datos en una ubicación protegida. No publiques esa copia, porque el esquema anterior contiene contraseñas en claro.
4. Compila y prepara las dependencias para la herramienta:
   ```bash
   bash ./mvnw verify
   bash ./mvnw dependency:copy-dependencies -DincludeScope=runtime -DoutputDirectory=target/auth-libs
   java -cp 'target/classes:target/auth-libs/*' com.example.proyectojavalenguajemarcas.tools.AuthAdmin migrate
   ```
5. La migración añade hashes Argon2id y campos de bloqueo/revocación; verifica las contraseñas antes de eliminar `password`. Conserva IDs, roles y relaciones. Es reanudable si se interrumpe; MySQL confirma los cambios de esquema por fases. No inicies el WAR antiguo después de migrar.
6. Renueva las credenciales de ejemplo bloqueadas con `set-password` antes de utilizarlas. No tienen una contraseña nueva compartida o predefinida.
7. Arranca el WAR actualizado y comprueba login, biblioteca y permisos. Los antiguos identificadores de sesión ya no son válidos.

La herramienta necesita permisos para alterar el esquema durante la migración. El usuario de base de datos usado por la aplicación debe tener únicamente los permisos de lectura/escritura necesarios; no necesita permisos para alterar tablas.

Si la verificación de un hash existente falla, la herramienta se detiene y conserva la columna antigua. Corrige el dato o restaura la copia protegida, con la aplicación detenida; no añadas un modo de login que acepte texto plano.

## Gestión manual de cuentas

Ejecuta Java directamente en un terminal. `set-password` pide y confirma una contraseña oculta; no acepta contraseñas por argumentos, variables de entorno ni registros de salida.

```bash
java -cp 'target/classes:target/auth-libs/*' com.example.proyectojavalenguajemarcas.tools.AuthAdmin set-password NOMBRE
java -cp 'target/classes:target/auth-libs/*' com.example.proyectojavalenguajemarcas.tools.AuthAdmin set-role NOMBRE EDITOR
```

Roles válidos: JUGADOR, EDITOR y ADMIN. El registro público crea siempre JUGADOR, aunque una petición incluya otro rol. La herramienta incrementa `auth_version` al renovar una contraseña o asignar un rol y revoca las sesiones anteriores. Una modificación directa del rol en SQL también exige volver a entrar al detectar una diferencia, pero debes usar la herramienta para garantizar la revocación incluso si cambias y restauras el rol entre peticiones.

Para una base nueva, `AuthAdmin init` o `src/main/resources/schema.sql` crea el esquema sin cuentas predefinidas. Docker Compose y las pruebas usan ese mismo archivo. La migración reutiliza únicamente su sección de tablas de seguridad para conservar las tablas y datos existentes. Registra tu cuenta como jugador y asígnale ADMIN con la herramienta interna.

## Sesiones y permisos

- Cookies HttpOnly, SameSite=Lax, Secure en producción y seguimiento exclusivamente por cookies.
- Caducidad tras 30 minutos sin actividad y ocho horas desde el login. La sesión se renueva al entrar y se invalida al salir.
- `GET /api/sesion` devuelve únicamente `autenticado`, `nombre` y `rol`. Con `X-CSRF-Token: Fetch`, Tomcat devuelve el nonce en la cabecera del mismo nombre. La interfaz lo mantiene en memoria y lo envía en las peticiones que modifican datos, incluidas login y registro. Después del login obtiene un nonce nuevo.
- Las API privadas devuelven 401 sin sesión válida y 403 sin permisos. Las páginas privadas redirigen al login o a la tienda. Las rutas o métodos sin regla explícita se deniegan.
- Solo ADMIN puede consultar o enviar revisiones. EDITOR y ADMIN pueden publicar y gestionar sus propias publicaciones.
- ZIP: ADMIN, el editor propietario o un usuario con el juego PUBLICADO en su biblioteca. Un juego pendiente/rechazado no puede añadirse a la biblioteca.
- Carátulas publicadas: visibles a usuarios autenticados para mostrar la tienda. Las carátulas privadas solo las ven ADMIN o el editor propietario.
- Los títulos y descripciones se muestran como texto. No se interpreta HTML proporcionado por usuarios.
- Se desactiva la caché de respuestas de sesión y recursos privados y se añade `nosniff`.

Las sesiones se guardan en MySQL mediante Spring Session JDBC; el nonce CSRF viaja en la misma sesión. Sobreviven a los reinicios y se comparten entre contenedores. Las tablas `GENESIS_SESSION` y `GENESIS_SESSION_ATTRIBUTES` son privadas: contienen identidad y tokens, y deben protegerse igual que los usuarios.

El filtro SQL limita el login a 20 peticiones y el registro a cinco por IP y minuto, con contadores independientes y compartidos entre contenedores. La consulta de sesión admite 120 por minuto para limitar la creación de sesiones anónimas. Las actualizaciones se realizan dentro de una transacción para evitar que peticiones simultáneas evadan el límite. En Vercel (`VERCEL=1`) usa la IP de `X-Forwarded-For`, que su entrada reemplaza; fuera de Vercel usa la IP de conexión. No definas `VERCEL=1` en otro proxy sin validar esa garantía. Las peticiones a esos endpoints limpian sesiones caducadas y contadores antiguos. Si la base no responde, la autenticación se rechaza con 503.

## Pruebas

Java 25 o posterior, Node.js 20 o posterior y Docker disponible:

```bash
bash scripts/test-security.sh
```

El script crea un MySQL desechable con contraseña aleatoria, publicado solo en localhost, ejecuta JUnit, pruebas HTTP con Tomcat real y las pruebas JavaScript y retira el contenedor al terminar. Nunca utiliza tu base de datos de producción. Las pruebas SQL rechazan bases cuyo nombre no termine en `_test` o un entorno distinto de `development`.

También puedes usar un MySQL de prueba que tú hayas creado, con las variables MYSQL configuradas y `APP_ENV=development`, y ejecutar `bash ./mvnw -Pintegration verify`.

Pruebas que no requieren MySQL/Docker:

```bash
bash ./mvnw test
node --test tests/ui.test.cjs
```

Las pruebas DOM usan un doble que rechaza la interpretación de HTML dinámico; no sustituyen una revisión visual en un navegador. El script comprueba los mismos recorridos con las API reales, pero no usa cuentas ni datos reales.

## Vercel

Consulta [README.md](README.md) para conectar una base compatible con MySQL y desplegar el contenedor. No hay credenciales incluidas ni migraciones automáticas al arrancar. La migración de una base real se ejecuta por separado, con una copia protegida y la aplicación antigua detenida. Las pruebas automatizadas usan MySQL desechable; no certifican los datos reales ni la compatibilidad completa de otro proveedor.

Vercel limita los cuerpos de peticiones/respuestas a 4,5 MB. Se admiten archivos de hasta 3 MiB y formularios de hasta 4 MiB. Los ZIP antiguos mayores de 4 MiB no pueden descargarse a través de ese runtime: conservan sus datos, pero la API devuelve 413. Para juegos mayores se necesitaría almacenamiento de objetos, una ampliación que queda fuera de este cambio. El diseño y los recorridos existentes se mantienen.
