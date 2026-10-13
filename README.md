# Genesis

Genesis es una tienda y biblioteca de videojuegos desarrollada como proyecto de clase. Combina una interfaz en HTML, CSS y JavaScript con un backend Java que gestiona usuarios, publicaciones, revisiones y archivos mediante una base de datos relacional.

El proyecto permite recorrer el proceso completo de publicación de un juego: subir un ZIP, revisarlo, publicarlo en la tienda, añadirlo a una biblioteca y descargarlo con los permisos correspondientes.

[Demo de Genesis](https://genesis-pied-sigma.vercel.app/)

## Funcionalidades y roles

El registro crea cuentas con el rol JUGADOR. La autorización se comprueba en el servidor en cada petición.

| Rol | Funcionalidades |
|---|---|
| JUGADOR | Consultar juegos publicados, añadirlos a su biblioteca, descargarlos y gestionar su avatar. |
| EDITOR | Funcionalidades de jugador, subida de juegos y consulta o eliminación de sus propias publicaciones. |
| ADMIN | Funcionalidades de editor y revisión de publicaciones: aprobar, rechazar y asignar carátulas. |

Las publicaciones pasan por los estados PENDIENTE, PUBLICADO y RECHAZADO. Los juegos publicados aparecen en la tienda; las publicaciones pendientes o rechazadas quedan restringidas al editor propietario y al administrador.

Los ZIP pueden descargarlos el administrador, el editor propietario o un usuario que tenga el juego publicado en su biblioteca.

## De Railway a Vercel con TiDB

La primera versión estuvo alojada en Railway. Para mantener una demo de portfolio utilizando modalidades gratuitas, el proyecto se adaptó a Vercel y TiDB Cloud Starter.

Vercel ejecuta el backend Java y sirve la interfaz mediante un contenedor con Tomcat. TiDB aloja los usuarios, los juegos, las bibliotecas, los archivos y las sesiones. Su compatibilidad con el protocolo MySQL permite conservar el controlador JDBC y el modelo relacional del proyecto.

La adaptación mantiene Java y el diseño original. Las sesiones y los límites de peticiones se almacenan en la base de datos para que los reinicios o reemplazos del contenedor no dependan de la memoria de una instancia concreta.

El despliegue se define en `vercel.json`, con el preset `container`, y se construye mediante `Dockerfile.vercel`. Las credenciales de conexión se proporcionan mediante variables de entorno y la conexión de producción utiliza TLS con verificación del certificado.

## Tecnologías y estructura

| Componente | Tecnología |
|---|---|
| Interfaz | HTML, CSS y JavaScript; peticiones a las API con Fetch. |
| Backend | Java 25, Jakarta Servlet y Tomcat 11. |
| Datos | MySQL / TiDB, JDBC y consultas parametrizadas. |
| Contraseñas | Password4j con Argon2id. |
| Sesiones compartidas | Spring Session JDBC. |
| Construcción | Maven y Docker. |
| Verificación | JUnit, pruebas HTTP, pruebas JavaScript y GitHub Actions. |

- `src/main/java/`: controladores, acceso a datos, configuración y reglas de seguridad.
- `src/main/webapp/`: páginas, estilos y scripts de la interfaz.
- `src/main/resources/schema.sql`: única definición de las tablas, compartida por la inicialización, Docker Compose, la herramienta interna y las pruebas.
- `src/test/` y `tests/`: pruebas Java, HTTP y JavaScript.
- `Dockerfile.vercel` y `vercel.json`: construcción y configuración del despliegue.
- [SEGURIDAD.md](SEGURIDAD.md): evolución de la autenticación y detalle de las protecciones actuales.

El esquema no incluye usuarios predefinidos. La configuración de conexión admite las variables `TIDB_*` y, como alternativa, las variables `MYSQL*` documentadas en [.env.example](.env.example). El archivo `.env` no se carga automáticamente en el proceso Java.

El wrapper de Maven permite construir el proyecto sin instalar Maven por separado. Los ajustes de IntelliJ (`.idea/`) se mantienen fuera del repositorio.

## Herramienta interna

`AuthAdmin` proporciona operaciones administrativas fuera de las API públicas. Su ejecución requiere Java 25 y las variables de conexión exportadas en el entorno del proceso.

Preparación de las clases y dependencias:

```bash
bash ./mvnw package
bash ./mvnw dependency:copy-dependencies -DincludeScope=runtime -DoutputDirectory=target/auth-libs
```

| Operación | Función |
|---|---|
| `init` | Crear las tablas en una base nueva y vacía. |
| `migrate` | Convertir el esquema anterior, verificar los hashes y conservar identificadores y relaciones. |
| `set-role USUARIO ROL` | Asignar JUGADOR, EDITOR o ADMIN e invalidar las sesiones anteriores. |
| `set-password USUARIO` | Renovar y desbloquear una credencial e invalidar las sesiones anteriores. |

Ejemplos de ejecución:

```bash
java -cp 'target/classes:target/auth-libs/*' com.example.proyectojavalenguajemarcas.tools.AuthAdmin init
java -cp 'target/classes:target/auth-libs/*' com.example.proyectojavalenguajemarcas.tools.AuthAdmin migrate
java -cp 'target/classes:target/auth-libs/*' com.example.proyectojavalenguajemarcas.tools.AuthAdmin set-role USUARIO EDITOR
java -cp 'target/classes:target/auth-libs/*' com.example.proyectojavalenguajemarcas.tools.AuthAdmin set-password USUARIO
```

Cada comando corresponde a una operación independiente. `init` y `migrate` se aplican a situaciones diferentes; no son una secuencia de pasos. La migración requiere la aplicación antigua detenida y una copia protegida de los datos, ya que el esquema anterior contiene contraseñas en claro y los cambios de esquema se confirman por fases.

La renovación de contraseñas utiliza una entrada oculta por terminal. Las contraseñas no se pasan como argumentos ni se incluyen en el repositorio. Estas operaciones no se ejecutan automáticamente al desplegar.

## Límites y pruebas

Los archivos nuevos admiten hasta 3 MiB, los formularios hasta 4 MiB y las descargas ZIP hasta 4 MiB. Estos tamaños mantienen las operaciones dentro del límite de 4,5 MB por petición o respuesta del runtime utilizado. Los archivos se almacenan en la base de datos.

Las sesiones sobreviven a los reemplazos del contenedor, pero la primera petición tras un periodo de inactividad puede necesitar que arranque el servidor Java. Los recursos disponibles también dependen de las cuotas de los servicios de alojamiento.

Pruebas unitarias y de la interfaz:

```bash
bash ./mvnw test
node --test tests/ui.test.cjs
```

Pruebas completas con Docker:

```bash
bash scripts/test-security.sh
```

El script utiliza un MySQL desechable y ejecuta pruebas de migración, sesiones, permisos, CSRF y recorridos de las API con Tomcat. GitHub Actions verifica además la construcción del contenedor y su respuesta HTTP. Las pruebas utilizan cuentas y datos de prueba, separados de la base desplegada.

Construcción local del contenedor:

```bash
docker build -f Dockerfile.vercel -t genesis .
```

## El error 404 NOT_FOUND durante el despliegue

Uno de los problemas de la adaptación fue una publicación marcada como Ready que devolvía 404. La configuración `framework: null`, equivalente a Other, dejó el despliegue sin la construcción del contenedor Java.

La corrección consistió en declarar el preset explícitamente:

```json
{
  "$schema": "https://openapi.vercel.sh/vercel.json",
  "framework": "container"
}
```

El estado Ready indica que terminó la publicación, pero no garantiza que la aplicación se haya construido o responda correctamente. Para este proyecto, los registros deben incluir la construcción del contenedor y la compilación de Maven; una publicación vacía en milisegundos es una señal de configuración incorrecta.

La raíz del despliegue es la del repositorio, donde están `pom.xml` y `Dockerfile.vercel`. Publicar únicamente `src/main/webapp` deja fuera el backend. Las direcciones de despliegues anteriores conservan su versión; el dominio de producción apunta a la versión publicada actualmente.
