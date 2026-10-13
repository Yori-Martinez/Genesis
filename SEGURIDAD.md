# Seguridad de Genesis

La seguridad de Genesis se concentra en el backend Java. La interfaz muestra los controles disponibles, mientras que el servidor verifica la identidad, el rol vigente y la propiedad de los recursos en cada petición protegida.

## Evolución de la autenticación

En la versión inicial, los controles visuales dependían del rol guardado en `sessionStorage`, las contraseñas se almacenaban en texto plano y algunas operaciones tenían comprobaciones incompletas. En particular, `POST /api/revision` permitía actuar a cualquier usuario con sesión, aunque no fuera administrador.

Modificar el navegador o enviar una petición directamente podía sortear restricciones que solo existían en la interfaz. La actualización trasladó las decisiones de acceso a un filtro central en Java y añadió comprobaciones sobre los recursos concretos.

| Aspecto | Versión inicial | Implementación actual |
|---|---|---|
| Rol mostrado por la interfaz | Leído desde `sessionStorage`. | Consultado al servidor mediante `/api/sesion`. |
| Autorización | Comprobaciones incompletas entre páginas y API. | Reglas explícitas por ruta y método, con denegación por defecto. |
| Revisión de juegos | El POST admitía cualquier sesión autenticada. | Consulta y modificación reservadas a ADMIN. |
| Contraseñas | Almacenadas en texto plano. | Hashes Argon2id con salt aleatorio. |
| Sesiones | Gestionadas en la instancia del contenedor. | Persistidas y compartidas mediante Spring Session JDBC. |
| Credenciales de conexión | Incluidas en el código. | Variables de entorno y TLS en producción. |
| Textos aportados por usuarios | Algunos se insertaban como HTML. | Renderizados como texto mediante nodos DOM seguros. |

La contraseña se almacena en `password_hash`. Argon2id utiliza 19 MiB de memoria, dos iteraciones, paralelismo uno y un salt aleatorio de 16 bytes. El login verifica únicamente hashes; el registro exige contraseñas de entre 15 y 128 caracteres y conserva los espacios introducidos.

La herramienta de migración transforma las credenciales del esquema anterior, verifica los hashes guardados antes de eliminar la columna `password` y conserva identificadores, roles y relaciones. Las credenciales de ejemplo publicadas quedan bloqueadas hasta su renovación. La migración puede reanudarse y se detiene si un hash existente no corresponde a la contraseña anterior.

La eliminación de una credencial del código actual no la elimina del historial de Git. La renovación de credenciales de conexión expuestas es una operación independiente de la retirada del código. La aplicación requiere HTTPS y la conexión de producción a la base utiliza `VERIFY_IDENTITY` para verificar el certificado TLS.

## Sesiones y permisos

La identidad procede de la sesión. El filtro consulta el rol y la versión de autenticación actuales en la base de datos; los parámetros del navegador no conceden permisos. El registro público crea siempre JUGADOR, aunque la petición intente seleccionar otro rol.

Un cambio de rol detectado por el servidor exige volver a iniciar sesión. La versión `auth_version` permite revocar las sesiones tras cambios de credenciales o permisos, incluso si un rol se cambia y posteriormente se restaura.

- Cookies HttpOnly, SameSite=Lax, Secure en producción y seguimiento exclusivamente por cookies.
- Caducidad tras 30 minutos sin actividad y ocho horas desde el login. La sesión se renueva al entrar y se invalida al salir.
- `GET /api/sesion` devuelve únicamente `autenticado`, `nombre` y `rol`, sin contraseñas ni identificadores de sesión.
- Las API privadas devuelven 401 sin sesión válida y 403 sin permisos. Las páginas privadas redirigen al login o a la tienda.
- Las rutas, roles o métodos sin una regla válida se deniegan.

| Recurso u operación | Permiso requerido |
|---|---|
| Tienda, biblioteca y avatar | Usuario autenticado. |
| Subir juegos y consultar publicaciones propias | EDITOR o ADMIN. |
| Eliminar una publicación | EDITOR o ADMIN, únicamente si es propietario. |
| Consultar, aprobar o rechazar revisiones | ADMIN. |
| Añadir un juego a la biblioteca | Usuario autenticado y juego PUBLICADO. |
| Descargar un ZIP | ADMIN, editor propietario o usuario con el juego PUBLICADO en su biblioteca. |
| Consultar una carátula publicada | Usuario autenticado. |
| Consultar una carátula pendiente o rechazada | ADMIN o editor propietario. |

Las sesiones se guardan en las tablas `GENESIS_SESSION` y `GENESIS_SESSION_ATTRIBUTES` mediante Spring Session JDBC. El token CSRF se conserva en la misma sesión, por lo que la sesión y el token sobreviven a los reinicios y se comparten entre contenedores.

## Protección de peticiones y contenido

El filtro `RestCsrfPreventionFilter` de Tomcat protege las operaciones que modifican datos, incluidos login y registro. La consulta de sesión con `X-CSRF-Token: Fetch` devuelve un token en la cabecera del mismo nombre. La interfaz lo mantiene en memoria y lo envía en las peticiones de escritura; después del login obtiene uno nuevo.

Un filtro SQL aplica límites de 20 peticiones de login, cinco de registro y 120 consultas de sesión por IP y minuto. Los contadores son independientes para cada endpoint y se comparten entre instancias. Las actualizaciones transaccionales impiden que peticiones simultáneas evadan el límite. El exceso devuelve 429; los fallos de conexión durante esas comprobaciones devuelven 503.

Las peticiones a esos endpoints eliminan sesiones caducadas y contadores antiguos. La obtención de la IP distingue la entrada de la plataforma de las conexiones directas; las cabeceras de IP proporcionadas por clientes no se consideran fiables de forma general.

Los títulos, descripciones y mensajes aportados por usuarios se muestran mediante `textContent` o nodos DOM seguros. La validación de entradas rechaza identificadores y cuerpos malformados, y los errores de login no revelan si una cuenta existe. Las respuestas de sesión y recursos privados desactivan la caché y añaden `nosniff`.

## Pruebas

La verificación utiliza Java 25 o posterior, Node.js 22 o posterior y Docker para los recorridos que requieren base de datos.

```bash
bash scripts/test-security.sh
```

El script crea un MySQL desechable con contraseña aleatoria, publicado solo en localhost. Ejecuta JUnit, pruebas HTTP con Tomcat y pruebas JavaScript; el contenedor se retira al terminar. Las pruebas SQL rechazan bases cuyo nombre no termine en `_test` o un entorno distinto de `development`.

El perfil Maven `integration` también permite ejecutar las pruebas contra una base MySQL de prueba configurada mediante variables de entorno:

```bash
bash ./mvnw -Pintegration verify
```

Pruebas que no requieren MySQL ni Docker:

```bash
bash ./mvnw test
node --test tests/ui.test.cjs
```

Las comprobaciones cubren:

- Denegación de acceso a usuarios anónimos y roles sin permisos, incluidas las revisiones enviadas directamente a la API.
- Propiedad de publicaciones, visibilidad de carátulas y acceso a descargas.
- Renovación, caducidad y revocación de sesiones; persistencia tras reemplazar el contenedor.
- Rechazo de peticiones sin token CSRF y límites compartidos frente a solicitudes simultáneas.
- Migración de contraseñas, bloqueo de credenciales públicas y conservación de cuentas y relaciones.
- Renderizado de contenido malicioso como texto y recorridos habituales de jugador, editor y administrador.

GitHub Actions ejecuta estas comprobaciones y valida la construcción y la respuesta HTTP del contenedor. Las pruebas DOM utilizan un doble que rechaza HTML dinámico; no sustituyen una revisión visual en un navegador. La verificación automatizada usa cuentas y datos de prueba y no valida por sí sola los datos de una instalación real.
