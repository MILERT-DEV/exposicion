# Entrega: GitHub y exposición

## Subir a GitHub (docente: `dmamanipar`)
```bash
cd SysTurismo-ENTREGA-HOY
git init
git add .
git commit -m "SysTurismo: proyecto final POO Unidad 3"
git branch -M main
git remote add origin https://github.com/TU_USUARIO/SysTurismo.git
git push -u origin main
```
Si el repositorio es **privado**: GitHub → Settings → Collaborators → *Add people* → `dmamanipar`.

## Lista de verificación de la rúbrica
| Punto | Dónde está |
|---|---|
| 0. Caso, requerimientos, modelo BD, GitHub | `docs/REQUISITOS.md`, `docs/MODELO_BD.md`, `database/systurismo.sql` (+ `.architect` por generar) |
| 1. Repositorio / Servicio / Controllers | `repository/`, `service/`, `controller/` |
| 2. Manual de usuario por roles | `docs/MANUAL_USUARIO.md` |
| 3. Ejecutable .jar / .exe | `mvnw.cmd clean package` y `empaquetar.bat` |
| 4. Interfaz JavaFX | `controller/`, `resources/view/login.fxml`, `css/style.css` |
| 5. Validación y mensajes (4 pts) | `SistemaService.validar()` + mensaje rojo dentro del formulario |
| 6. Investigación adicional | PBKDF2 con sal (`PasswordUtil`), paginación/búsqueda, consulta DNI con Jsoup |
| 7. Exposición | guion abajo |

## Guion sugerido (8–10 min)
1. Problema de la agencia y roles (1 min).
2. Arquitectura por capas y por qué (Controller no toca SQL) (2 min).
3. Demo con admin: catálogo → pestaña Precios → cliente con DNI → reserva → pago → boleta → reporte (4 min).
4. Demo con vendedor: ver lo que NO puede hacer (1 min).
5. Investigación extra: PBKDF2 + sal (1 min).

## Preguntas probables del docente
- **¿Por qué PBKDF2 y no SHA-256?** SHA-256 es rápido y sin sal: dos usuarios con la misma clave tienen igual hash y se pueden probar millones de claves por segundo. PBKDF2 añade sal aleatoria y 120 000 iteraciones.
- **¿Cómo evitan inyección SQL?** Todos los valores van con `PreparedStatement`; los nombres de tabla/columna salen solo de definiciones internas.
- **¿Dónde está la lógica del negocio?** En `SistemaService` (cupos, saldo de pagos, boleta única).
- **¿Qué pasa con usuarios antiguos con SHA-256?** Al iniciar sesión se verifican y se migran automáticamente al formato nuevo.
- **¿Por qué H2?** Base embebida: no requiere instalar un servidor y facilita el `.exe`.
