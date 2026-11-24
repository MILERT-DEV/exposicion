# Manual de Usuario – SysTurismo

**Universidad Peruana Unión – Escuela Profesional de Ingeniería de Sistemas**
**Curso:** Programación Orientada a Objetos – **Estudiante:** Robert Paricahua Aguilar

## 1. ¿Qué es SysTurismo?

SysTurismo es un programa de escritorio para una agencia de turismo. Reemplaza el cuaderno y las hojas de cálculo: guarda **clientes, destinos, paquetes, reservas, pagos y boletas**, calcula los montos solo y evita errores comunes (vender más cupos de los que hay, cobrar de más o emitir dos boletas de la misma reserva).

Hay **dos perfiles**. Lo que ves en pantalla depende del perfil con el que entres:

| Perfil | ¿Quién es? | ¿Para qué sirve? |
|---|---|---|
| **Vendedor** | Personal de ventas | Atender clientes: consultar precios, registrar clientes, reservas, pagos y boletas. |
| **Administrador** | Dueño o gerente | Todo lo anterior, más: crear destinos y paquetes, administrar usuarios, eliminar registros y ver reportes. |

## 2. Antes de empezar

**Necesitas:** Windows 10/11 y JDK 21 o superior (si abres el proyecto desde IntelliJ). Internet solo se usa para descargar dependencias la primera vez y para autocompletar nombres por DNI.

**Formas de abrir el programa (elige una):**
1. **IntelliJ IDEA:** abre la carpeta como proyecto Maven y ejecuta la clase `SysTurismo`.
2. **Terminal:** dentro de la carpeta del proyecto escribe `mvnw.cmd clean javafx:run`.
3. **Ejecutable:** ejecuta `empaquetar.bat` y abre `dist\SysTurismo\SysTurismo.exe`.

La primera vez, el programa crea solo la base de datos y carga datos de ejemplo (12 destinos y 11 paquetes). No necesitas instalar nada más.

## 3. Iniciar sesión

Escribe tu usuario y contraseña y presiona **Ingresar** (o la tecla Enter). Usuarios de ejemplo:

| Usuario | Contraseña | Perfil |
|---|---|---|
| admin | admin123 | Administrador |
| carlos | carlos123 | Administrador |
| vendedor | vendedor123 | Vendedor |
| maria | maria123 | Vendedor |

Si te equivocas aparece el aviso *"Usuario o contraseña incorrectos."*. Arriba a la derecha verás tu usuario y perfil, y el botón **Cerrar sesión** para cambiar de usuario.

> Consejo: cambia las contraseñas de ejemplo antes de usar el sistema de verdad (Administrador → Usuarios → Editar).

## 4. Conociendo la pantalla

Todas las pestañas de datos funcionan igual:

- **Nuevo:** abre un formulario para registrar.
- **Editar:** primero selecciona una fila y luego presiona el botón.
- **Eliminar:** pide confirmación (solo Administrador).
- **Actualizar:** vuelve a leer los datos.
- **Buscar:** escribe cualquier texto y la tabla se filtra al instante.
- **◀ Anterior / Siguiente ▶:** la tabla muestra 10 registros por página.

Los montos se ven en soles, por ejemplo **S/ 1,450.00**.

## 5. ¿Qué puede hacer cada perfil?

| Pestaña | Vendedor | Administrador |
|---|---|---|
| Inicio | Ver | Ver |
| **Precios** | Consultar y calcular | Consultar y calcular |
| Clientes | Ver, Nuevo, Editar | Todo (incluye Eliminar) |
| Destinos | Solo ver | Todo |
| Paquetes | Solo ver | Todo |
| Reservas | Ver, Nuevo, Editar, Emitir boleta | Todo |
| Pagos | Ver, Nuevo, Editar | Todo |
| Boletas | Ver, Nueva, Generar comprobante | Todo |
| Usuarios | No aparece | Todo |
| Reportes | No aparece | Ver |

---

## 6. Guía del VENDEDOR

### 6.1 Consultar precios (pestaña Precios)
1. Abre **Precios**: verás cada paquete con destino, ciudad, días, **precio por persona** y **cupos disponibles**.
2. Escribe en **Buscar** (por ejemplo "Cusco") para filtrar. Haz clic en el título de una columna para ordenar (por ejemplo por precio).
3. Selecciona un paquete y escribe el número de **Pasajeros**: abajo aparece el total, por ejemplo *S/ 1,450.00 x 2 pasajeros = S/ 2,900.00*.

### 6.2 Vender un paquete paso a paso
Ejemplo: Juan Pérez compra "Machu Picchu Clásico" para 2 personas.

**Paso 1 – Registrar al cliente** (pestaña Clientes → Nuevo)
1. Escribe el **DNI** de 8 dígitos. Al completar el octavo dígito el sistema busca y llena los **nombres** solo. Si no hay Internet o no lo encuentra, escríbelos a mano.
2. Correo y teléfono son opcionales (teléfono de 9 dígitos).
3. Presiona **Aceptar**. Si ya existe un cliente con ese DNI, el sistema te avisa.

**Paso 2 – Crear la reserva** (pestaña Reservas → Nuevo)
1. Elige el **cliente** y el **paquete** (en la lista cada paquete muestra su precio).
2. Revisa la **fecha** (por defecto hoy; no puede ser anterior a hoy) y escribe los **pasajeros**.
3. Debajo verás el cálculo, por ejemplo *S/ 1,450.00 x 2 pasajeros = S/ 2,900.00*. El **total** se llena solo y no se puede escribir a mano.
4. Deja el estado en *Pendiente* (o *Confirmada*) y presiona **Aceptar**.

**Paso 3 – Registrar el pago** (pestaña Pagos → Nuevo)
1. Elige la **reserva**. El **monto** se propone solo con el saldo pendiente; puedes cambiarlo si el cliente paga una parte.
2. Elige el método (Efectivo, Tarjeta, Transferencia, Yape/Plin).
3. Pon el estado en **Pagado** cuando ya recibiste el dinero (solo los pagos *Pagado* cuentan como ingreso).
4. La suma de pagos *Pagado* nunca puede superar el total de la reserva.

**Paso 4 – Emitir la boleta** (dos formas)
- En **Reservas**: selecciona la reserva y presiona **Emitir boleta**; o
- En **Boletas → Nuevo**: elige la reserva; el **total** y el **número** de boleta se llenan solos.

Cada reserva tiene **una sola boleta**. No se puede emitir boleta de una reserva *Cancelada*.

**Paso 5 – Dar el comprobante**
En **Boletas**, selecciona la boleta y presiona **Generar comprobante**. El sistema crea un archivo `.txt` en la carpeta `comprobantes` y te muestra la ruta.

### 6.3 Cancelar una reserva
Reservas → selecciona la fila → **Editar** → cambia el estado a *Cancelada*. Los cupos de esa reserva quedan libres otra vez.

### 6.4 Lo que el vendedor NO puede hacer
No puede eliminar registros, ni crear o modificar destinos y paquetes (se ven en solo lectura), ni gestionar usuarios ni ver reportes. Estas pestañas o botones simplemente no aparecen.

---

## 7. Guía del ADMINISTRADOR

El administrador hace todo lo del vendedor y además:

### 7.1 Mantener el catálogo
- **Destinos → Nuevo:** nombre, ciudad y descripción. Registra primero el destino.
- **Paquetes → Nuevo:** nombre, **destino**, días, **precio** y **cupos** (todos mayores que cero). Para cambiar un precio: selecciona el paquete → **Editar**. Las reservas ya hechas conservan su total anterior.

### 7.2 Administrar usuarios (pestaña Usuarios)
1. **Nuevo:** usuario (4 a 30 letras, números o guion bajo), contraseña (mínimo 6 caracteres) y rol.
2. **Cambiar clave o rol:** selecciona al usuario → **Editar**. Si dejas la clave vacía, no cambia.
3. No puedes eliminar el usuario con el que iniciaste sesión.
4. Las contraseñas nunca se muestran: se guardan cifradas (ver sección 10).

### 7.3 Eliminar registros
Selecciona la fila → **Eliminar** → confirma con *Sí*. Si el registro está en uso (por ejemplo, un cliente con reservas) el sistema no lo borra y explica por qué: primero hay que eliminar lo que depende de él.

### 7.4 Reportes
Pestaña **Reportes** → **Generar reporte**: muestra cuántos registros hay en cada módulo y los **ingresos confirmados** (suma de pagos en estado *Pagado*).

---

## 8. Mensajes y validaciones

Si un dato es incorrecto, el formulario **no se cierra**: aparece un mensaje rojo con el motivo y no pierdes lo que escribiste. Cuando todo sale bien, se muestra *"La información se guardó correctamente."*

| Mensaje | Qué significa y qué hacer |
|---|---|
| El campo … es obligatorio. | Falta un dato: complétalo. |
| El DNI debe contener exactamente 8 dígitos. | Revisa el DNI (solo números). |
| Ya existe un registro con ese valor… | DNI, usuario o número de boleta repetido. |
| El teléfono debe contener 9 dígitos. / El correo electrónico no es válido. | Corrige el formato o deja el campo vacío. |
| El precio / monto debe ser mayor que cero. | Escribe un número positivo. |
| El campo … debe ser un número válido. | Usa solo números (decimales con punto). |
| La fecha debe tener el formato AAAA-MM-DD. | Ejemplo: 2026-12-15. |
| La fecha de reserva no puede ser anterior a hoy. | Usa la fecha de hoy o una futura. |
| No hay cupos suficientes. Disponibles: N | Baja los pasajeros o elige otro paquete. |
| El pago supera el saldo de la reserva. Saldo disponible: S/ X | Reduce el monto al saldo indicado. |
| La reserva está cancelada: no se pueden registrar pagos. | No se cobra una reserva cancelada. |
| Esta reserva ya tiene una boleta: … | Solo se permite una boleta por reserva. |
| No se puede emitir boleta de una reserva cancelada. | Cambia el estado de la reserva si fue un error. |
| No se puede eliminar: el registro está siendo usado… | Elimina primero los registros que dependen de él. |
| La contraseña debe tener al menos 6 caracteres. | Elige una clave más larga. |

## 9. Problemas frecuentes

- **"No se pudo iniciar la base de datos… ID_PERFIL no permite valores nulos":** la base guardada es de una versión antigua. Esta versión la repara sola al abrir. Si aun así falla, cierra el programa y borra la carpeta `data` (se vuelve a crear con los datos de ejemplo; se pierden los datos anteriores).
- **El nombre no se llena al escribir el DNI:** no hay Internet o la página de consulta no responde. Escribe el nombre manualmente.
- **No veo Destinos/Paquetes para editar:** estás con perfil Vendedor; pide a un Administrador.
- **El total de una reserva antigua sale en 0:** abre la reserva con *Editar* y guárdala para recalcular.
- **Dónde están mis datos:** en la carpeta `data` del proyecto, o en `C:\Users\TU_USUARIO\SysTurismo\data`. No la borres si quieres conservar la información; haz copia de `systurismo.mv.db`.

## 10. Investigación adicional (no vista en clases)

- **Contraseñas con PBKDF2 + sal:** `PasswordUtil` nunca guarda la clave escrita. Usa PBKDF2-HMAC-SHA256 con sal aleatoria de 16 bytes y 120 000 iteraciones. Dos usuarios con la misma clave tienen hashes distintos, y adivinar claves es lento para un atacante. Es un hash, no un cifrado: la clave no se puede recuperar.
- **Paginación y búsqueda** en todas las tablas.
- **Consulta de DNI en línea** con Jsoup.
- **Migración automática de la base de datos:** al iniciar, el programa adapta bases creadas con versiones anteriores sin perder datos.
- **Consultas SQL parametrizadas** (`PreparedStatement`) contra inyección SQL.

## 11. Capturas para la entrega

Tómalas ejecutando el sistema (no se incluyen imágenes simuladas): inicio de sesión, menú principal, Precios, Clientes, Reservas con el cálculo, Pagos, Boletas y Reportes. Pégalas en esta sección o en `docs/capturas/`.
