# SysTurismo - Sistema de Agencia de Turismo

Proyecto final de Programación Orientada a Objetos - Universidad Peruana Unión.

## Tecnologías
- Java 21
- JavaFX 21
- Maven
- H2 Database (modo archivo)
- Jsoup (consulta de DNI)

## Ejecutar en IntelliJ IDEA
1. Descomprimir el proyecto.
2. Abrir la carpeta `SysTurismo` como proyecto Maven.
3. Esperar la descarga de dependencias.
4. En Windows, abrir la terminal dentro de la carpeta `SysTurismo` y ejecutar:
```bat
mvnw.cmd clean javafx:run
```
También puede abrir el proyecto desde IntelliJ IDEA y ejecutar la clase `SysTurismo.java`.

## Usuario inicial
- Usuario: `admin`
- Contraseña: `admin123`

Cambiar la contraseña antes de usar el sistema en un entorno real. Las contraseñas se guardan con PBKDF2-HMAC-SHA256 + sal aleatoria (`util/PasswordUtil`); las cuentas antiguas con SHA-256 se migran al iniciar sesión.

## Usuarios de ejemplo
| Usuario | Contraseña | Rol |
|---|---|---|
| admin | admin123 | Administrador |
| carlos | carlos123 | Administrador |
| vendedor | vendedor123 | Vendedor |
| maria | maria123 | Vendedor |

También se cargan 12 destinos (Machu Picchu, Paracas, Colca, Titicaca, etc.) y 11 paquetes turísticos. Solo se insertan si las tablas están vacías; para recargarlos borre la carpeta `data`.

## Base de datos
H2 se crea automáticamente en `data/systurismo.mv.db` (si existe una carpeta `data` junto al proyecto) o en `~/SysTurismo/data` (programa instalado). No se debe borrar ese archivo si se desea conservar la información.

## Módulos
Inicio, precios, clientes, destinos, paquetes, reservas, pagos, boletas, usuarios y reportes.

## Reglas principales
- DNI: 8 dígitos.
- Teléfono: 9 dígitos cuando se registra.
- El precio y el monto deben ser positivos.
- La cantidad de pasajeros debe ser positiva y no superar los cupos disponibles.
- El total de una reserva se calcula con el precio del paquete por la cantidad de pasajeros.
- Los pagos confirmados no pueden superar el total de la reserva.
- Una reserva tiene una sola boleta: se emite con `Emitir boleta` (Reservas) o con `Nuevo` en Boletas (el total y el número se llenan solos).
- En Pagos, el monto se propone con el saldo pendiente de la reserva.
- La pestaña `Precios` permite consultar precios, cupos disponibles y calcular totales (todos los perfiles).

## Empaquetar
Ejecutar:
```bash
mvnw.cmd clean package
```
El ensamblado Maven genera `target/SysTurismo-1.0.0-jar-with-dependencies.jar` (clase principal `Launcher`). Ejecutar con `java -jar target\SysTurismo-1.0.0-jar-with-dependencies.jar`. Para generar el `.exe` ejecute `empaquetar.bat` en Windows. Para aplicaciones JavaFX en Windows, el método recomendado de distribución es `jpackage` ejecutado en Windows con un JDK 21 instalado. El JAR ensamblado no sustituye las bibliotecas nativas JavaFX del sistema operativo.

## Instalador para Windows (.exe)
Dos formas de generar `SysTurismo-1.0.0.exe` (un solo archivo que se copia e instala en cualquier Windows 10/11 de 64 bits, sin instalar Java):

**A) En tu PC Windows:** instala JDK 21 (Temurin) y WiX Toolset 3.x, abre una terminal en la carpeta del proyecto y ejecuta `crear-instalador.bat`. El resultado queda en `installer\SysTurismo-1.0.0.exe`.

**B) En GitHub (sin instalar nada):** sube el proyecto a GitHub, entra a Actions > "Instalador Windows" > Run workflow y descarga el .exe desde "Artifacts" (`.github/workflows/build-windows.yml`).

El instalador crea accesos directos en el Escritorio y el Menu Inicio. Los datos quedan en `%USERPROFILE%\SysTurismo\data` y no se borran al desinstalar o actualizar.
Antes de entregarlo, pruebalo en un Windows limpio: instalar, iniciar sesion con admin y con vendedor, crear una venta completa y desinstalar.

## Documentación
`docs/REQUISITOS.md`, `docs/MODELO_BD.md`, `docs/MANUAL_USUARIO.md`, `docs/GITHUB_Y_EXPOSICION.md`.

## Estructura
- `model`: clases del negocio.
- `repository`: consultas y operaciones SQL.
- `service`: reglas y validaciones.
- `controller`: eventos e interfaz JavaFX.
- `config`: conexión y creación de tablas.
- `util`: funciones auxiliares, incluida la investigación adicional de hash de claves.

## Conectar H2 con DBeaver
1. Ejecutar SysTurismo al menos una vez para crear `data/systurismo.mv.db`.
2. En DBeaver, crear una conexión nueva y elegir H2 Embedded.
3. URL JDBC: `jdbc:h2:file:RUTA_COMPLETA_DEL_PROYECTO/SysTurismo/data/systurismo`
4. Usuario: `sa`. Contraseña: dejar vacía.
5. Si la aplicación está abierta, `AUTO_SERVER=TRUE` permite la conexión compartida.

## Roles y permisos
| Función | Administrador | Vendedor |
|---|---|---|
| Clientes, Reservas, Pagos, Boletas | Completo | Nuevo / Editar (sin Eliminar) |
| Destinos y Paquetes | Completo | Solo lectura |
| Usuarios (crear, cambiar rol/clave) | Sí | No |
| Reportes | Sí | No |

## Funciones adicionales
- **Búsqueda automática por DNI**: en Clientes > Nuevo, escriba el DNI; al llegar a 8 dígitos se completa solo el nombre (`util/ConsultaDNI`, Jsoup sobre eldni.com). Requiere Internet; si falla, se puede escribir el nombre a mano.
- **Migración automática de la base**: `DatabaseConfig.adaptarEsquemaAntiguo` repara bases de versiones anteriores (ej. columna `ID_PERFIL` obligatoria) sin borrar datos.
- **Paginación y búsqueda** en todas las tablas (10 filas por página).
- **Gestión de usuarios** con contraseñas protegidas con PBKDF2 + sal.
- **Formularios que no se cierran** al haber errores: el mensaje aparece dentro del formulario.

## Entrega y limitaciones conocidas
- El proyecto incluye el código fuente y el manual en `docs/MANUAL_USUARIO.md`.
- Las capturas deben tomarse ejecutando la aplicación real en el equipo de presentación.
- La consulta de DNI depende de un sitio web de terceros (eldni.com); puede dejar de funcionar si ese sitio cambia.
- El código fue revisado sin compilar con Maven en el entorno de edición (sin acceso a Maven Central): compile y pruebe en su equipo con `mvnw.cmd clean javafx:run`.
- El empaquetado final para Windows debe realizarse en Windows mediante `jpackage` con JDK 21; no se debe afirmar que existe un instalador EXE hasta generarlo y probarlo en ese equipo.
