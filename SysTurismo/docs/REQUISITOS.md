# Descripción del caso y lista de requerimientos – SysTurismo

## 1. Descripción del caso
Una agencia de turismo de Perú vende paquetes a sus clientes (Machu Picchu, Paracas, Colca, Titicaca, Amazonía, etc.).
Hoy lleva clientes, reservas y pagos en cuaderno y hojas de cálculo, lo que causa: sobreventa de cupos, pagos mayores a
lo reservado, boletas duplicadas y datos de clientes repetidos o mal escritos.

**SysTurismo** es una aplicación de escritorio que centraliza el registro de clientes, destinos, paquetes, reservas,
pagos y boletas, aplica las reglas del negocio automáticamente y separa lo que puede hacer cada tipo de usuario.

## 2. Actores / roles
| Rol | Descripción |
|---|---|
| Administrador | Dueño o gerente. Acceso total: catálogo, usuarios, eliminación de registros y reportes. |
| Vendedor | Personal de ventas. Atiende clientes, registra reservas, pagos y boletas. No elimina ni modifica el catálogo. |

## 3. Requerimientos funcionales
| Código | Requerimiento | Rol |
|---|---|---|
| RF01 | Iniciar sesión con usuario y contraseña; cerrar sesión. | Todos |
| RF02 | Registrar, editar, listar y eliminar clientes (DNI, nombres, correo, teléfono). | Admin: todo / Vendedor: sin eliminar |
| RF03 | Autocompletar nombres del cliente al escribir un DNI de 8 dígitos (consulta en línea). | Todos |
| RF04 | Registrar, editar y eliminar destinos turísticos. | Admin (Vendedor: lectura) |
| RF05 | Registrar, editar y eliminar paquetes (destino, días, precio, cupos). | Admin (Vendedor: lectura) |
| RF06 | Registrar y editar reservas (cliente, paquete, fecha, pasajeros, estado). El total se calcula solo. | Admin / Vendedor |
| RF07 | Validar cupos: no reservar más pasajeros que los cupos disponibles del paquete. | Sistema |
| RF08 | Registrar pagos de una reserva (fecha, monto, método, estado). | Admin / Vendedor |
| RF09 | Impedir que los pagos confirmados superen el total de la reserva y pagos sobre reservas canceladas. | Sistema |
| RF10 | Emitir una boleta por reserva (una sola, con número único) y generar su comprobante en archivo de texto. | Admin / Vendedor |
| RF11 | Gestionar usuarios (crear, cambiar rol o clave, eliminar). | Admin |
| RF12 | Generar reporte con totales por módulo e ingresos confirmados. | Admin |
| RF13 | Buscar y paginar (10 filas) en todas las tablas. | Todos |
| RF15 | Consultar el catálogo de precios con cupos disponibles y calcular el total según los pasajeros. | Todos |
| RF16 | Sugerir automáticamente el monto de un pago (saldo pendiente) y el total/número de una boleta al elegir la reserva. | Admin / Vendedor |
| RF14 | Mostrar mensaje de confirmación o error en cada acción y validar los campos del formulario. | Todos |

## 4. Requerimientos no funcionales
| Código | Requerimiento |
|---|---|
| RNF01 | Interfaz gráfica en JavaFX 21. |
| RNF02 | Arquitectura por capas: Controller → Service → Repository → Base de datos (H2). |
| RNF03 | Contraseñas guardadas con PBKDF2-HMAC-SHA256 y sal aleatoria (nunca en texto plano). |
| RNF04 | Consultas SQL parametrizadas (PreparedStatement) contra inyección SQL. |
| RNF05 | Empaquetado como JAR ejecutable y aplicación Windows (.exe) con jpackage. |
| RNF07 | Migración automática de bases de datos creadas con versiones anteriores del sistema. |
| RNF06 | Los datos se guardan en `~/SysTurismo/data`, carpeta con permisos de escritura. |

## 5. Reglas de negocio
1. DNI de 8 dígitos y único por cliente. Teléfono de 9 dígitos (opcional). Correo con formato válido (opcional).
2. Precio, monto, días, cupos y pasajeros mayores que cero.
3. Total de reserva = precio del paquete × pasajeros (no editable).
4. Pasajeros no puede superar los cupos libres (las reservas canceladas no ocupan cupo).
5. Una reserva nueva no puede tener fecha anterior a hoy.
6. Suma de pagos "Pagado" ≤ total de la reserva.
7. Una reserva tiene como máximo una boleta (se emite desde Reservas o desde Boletas → Nuevo); el total de la boleta es el de la reserva y no se escribe a mano; no hay boleta de reservas canceladas.
8. No se puede eliminar un registro usado por otros (p. ej. cliente con reservas).
9. Un administrador no puede eliminar su propia sesión activa.

## 6. Casos de uso principales
- **CU01 Vender un paquete:** Vendedor registra cliente → crea reserva → registra pago → emite boleta → genera comprobante.
- **CU02 Mantener catálogo:** Administrador crea destinos y paquetes.
- **CU03 Administrar usuarios:** Administrador crea vendedores y cambia claves.
- **CU04 Consultar ingresos:** Administrador genera el reporte.
