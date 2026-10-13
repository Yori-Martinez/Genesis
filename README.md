# Genesis

Tienda de juegos creada con Java, Tomcat, MySQL y HTML/CSS/JavaScript. Autenticación con Argon2id, permisos comprobados en el servidor, CSRF y sesiones y límites de intentos compartidos en la base de datos. Se conserva el diseño del proyecto de clase.

## Vercel sin Railway

El repositorio incluye `Dockerfile.vercel`, que Vercel detecta para ejecutar Java en su Container Runtime (actualmente beta y disponible en todos los planes). No necesitas convertir el backend a JavaScript. La base de datos se contrata/configura por separado: Vercel no aloja MySQL dentro de este contenedor.

1. Crea un clúster **TiDB Cloud Starter** en su modalidad gratuita, compatible con el protocolo MySQL. Mantén el límite de gasto en **0** y no actives modalidades de pago. Consulta los límites vigentes antes de crearlo. Puedes usar otra base MySQL si ya la tienes.
2. Si vas a empezar sin datos, abre el SQL Editor del proveedor, selecciona la base elegida (por ejemplo `test`) y ejecuta el contenido de [`src/main/resources/schema.sql`](src/main/resources/schema.sql). Crea tablas vacías, sin usuarios ni contraseñas de ejemplo. Si vas a conservar tu base anterior, sigue primero la migración de [SEGURIDAD.md](SEGURIDAD.md); después exporta/importa los datos ya migrados, preservando IDs y relaciones. **No ejecutes el esquema vacío para sustituir datos existentes.**
3. En Vercel, importa `Yori-Martinez/Genesis`, rama `main`, raíz del repositorio y framework **Other**. Deja la construcción del contenedor a Vercel. Añade las variables de conexión desde la sección **Connect** del clúster, sin subirlas a GitHub:

   | Variable | Valor |
   |---|---|
   | `APP_ENV` | `production` |
   | `TIDB_HOST` | Host del clúster |
   | `TIDB_PORT` | Puerto indicado por el proveedor |
   | `TIDB_DATABASE` | La base donde ejecutaste el esquema |
   | `TIDB_USER` | Usuario de conexión |
   | `TIDB_PASSWORD` | Contraseña de conexión |
   | `MYSQL_SSL_MODE` | `VERIFY_IDENTITY` |

   También se aceptan las variables `MYSQLHOST`, `MYSQLPORT`, `MYSQLDATABASE`, `MYSQLUSER` y `MYSQLPASSWORD`. Elige un único grupo. La integración TiDB del Marketplace de Vercel puede añadir las variables `TIDB_*` al vincular un clúster existente. Usa credenciales y bases distintas para Preview y Production.
4. Despliega desde Vercel. Comprueba registro, login, biblioteca y logout. Para obtener una cuenta de editor o administrador, registra tu usuario y asigna su rol con la herramienta interna descrita abajo. El navegador no puede conceder permisos.

Si faltan la base o sus tablas, el backend rechaza la autenticación. Subir el código no ejecuta la migración ni configura automáticamente esos servicios.

## Herramienta interna

Necesitas Java 25 y las variables de conexión exportadas en tu terminal. No publiques las credenciales ni las escribas en los argumentos de los comandos.

```bash
bash ./mvnw package
bash ./mvnw dependency:copy-dependencies -DincludeScope=runtime -DoutputDirectory=target/auth-libs
java -cp 'target/classes:target/auth-libs/*' com.example.proyectojavalenguajemarcas.tools.AuthAdmin init
```

`init` se usa **solo con una base nueva y vacía**, como alternativa al SQL Editor. Para una instalación antigua usa `migrate` siguiendo [SEGURIDAD.md](SEGURIDAD.md). Después de registrar tu cuenta:

```bash
java -cp 'target/classes:target/auth-libs/*' com.example.proyectojavalenguajemarcas.tools.AuthAdmin set-role TU_USUARIO ADMIN
java -cp 'target/classes:target/auth-libs/*' com.example.proyectojavalenguajemarcas.tools.AuthAdmin set-password TU_USUARIO
```

La contraseña se pide oculta. Un cambio de contraseña o rol revoca las sesiones anteriores. Estas operaciones no se exponen en la web.

## Límites y pruebas

Los archivos nuevos admiten hasta 3 MiB, los formularios hasta 4 MiB y las descargas ZIP hasta 4 MiB, por el límite de 4,5 MB del runtime. Las sesiones se guardan en MySQL y sobreviven a reemplazos del contenedor. El sitio necesita HTTPS; Vercel lo proporciona.

```bash
bash ./mvnw test
node --test tests/ui.test.cjs
bash scripts/test-security.sh
```

El último comando necesita Docker y ejecuta pruebas HTTP sobre Tomcat y MySQL de prueba. GitHub Actions comprueba también la construcción del contenedor. No utiliza la base real del proyecto.

Documentación: [Container Runtime de Vercel](https://vercel.com/docs/functions/runtimes/container), [TiDB Starter y precios](https://www.pingcap.com/tidb-cloud-starter-pricing-details/), [integración TiDB](https://vercel.com/marketplace/tidb-cloud).
