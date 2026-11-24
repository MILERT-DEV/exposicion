package pe.edu.upeu.systurismo.service;

import pe.edu.upeu.systurismo.dto.PersonaDto;
import pe.edu.upeu.systurismo.repository.CrudRepository;
import pe.edu.upeu.systurismo.util.ConsultaDNI;
import pe.edu.upeu.systurismo.util.PasswordUtil;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.*;

public class SistemaService {
    private final CrudRepository repo;

    public SistemaService(CrudRepository repo) {
        this.repo = repo;
    }

    public List<Map<String, Object>> listar(String tabla) throws SQLException {
        return repo.listar(tabla);
    }

    public List<Map<String, Object>> listarBoletas() throws SQLException {
        return repo.listarBoletas();
    }

    public Map<String, Object> detalleBoleta(Object idBoleta) throws SQLException {
        return repo.detalleBoleta(idBoleta);
    }

    /** Busca nombres y apellidos de una persona por DNI (consulta en línea). */
    public PersonaDto buscarPorDni(String dni) throws SQLException {
        if (dni == null || !dni.matches("\\d{8}"))
            throw new SQLException("El DNI debe contener exactamente 8 dígitos.");
        try {
            PersonaDto persona = new ConsultaDNI().consultarDNI(dni);
            if (!persona.encontrado()) throw new SQLException("No se encontraron datos para el DNI " + dni + ".");
            return persona;
        } catch (java.io.IOException e) {
            throw new SQLException("No se pudo consultar el DNI. Verifique su conexión a Internet.", e);
        }
    }

    /** Formato de dinero: S/ 1,450.00 */
    public static String dinero(double monto) {
        return String.format(Locale.US, "S/ %,.2f", monto);
    }

    public double precioPaquete(long idPaquete) throws SQLException {
        return repo.precioPaquete(idPaquete);
    }

    public double totalReserva(long idReserva) throws SQLException {
        return repo.totalReserva(idReserva);
    }

    /** Lo que falta por pagar de una reserva (total - pagos confirmados). */
    public double saldoReserva(long idReserva) throws SQLException {
        return Math.max(0, repo.totalReserva(idReserva) - repo.pagosConfirmados(idReserva, null));
    }

    public List<Map<String, Object>> catalogoPrecios() throws SQLException {
        return repo.catalogoPrecios();
    }

    /** Calcula el total de una reserva para mostrarlo en el formulario. */
    public double calcularTotalReserva(long idPaquete, int pasajeros) throws SQLException {
        if (pasajeros <= 0) return 0;
        return repo.precioPaquete(idPaquete) * pasajeros;
    }

    public void guardar(String tabla, String pk, Object id,
                        LinkedHashMap<String, Object> datos) throws SQLException {
        // Al editar un usuario, una clave vacía significa "no cambiar la clave".
        if (tabla.equals("usuario") && id != null && esVacio(datos.get("clave"))) datos.remove("clave");

        validar(tabla, datos, id == null);

        if (tabla.equals("usuario") && datos.containsKey("clave")) {
            datos.put("clave", PasswordUtil.hash(datos.get("clave").toString()));
        }

        if (tabla.equals("paquete")) {
            int dias = Integer.parseInt(datos.get("dias").toString());
            int cupos = Integer.parseInt(datos.get("cupos").toString());
            if (dias <= 0 || cupos <= 0) throw new SQLException("Los días y cupos deben ser mayores que cero.");
        }

        if (tabla.equals("reserva")) {
            long idPaquete = Long.parseLong(datos.get("id_paquete").toString());
            int pasajeros = Integer.parseInt(datos.get("pasajeros").toString());
            int ocupados = repo.pasajerosReservados(idPaquete, id);
            int disponibles = repo.cuposPaquete(idPaquete) - ocupados;

            if (!"Cancelada".equals(datos.get("estado")) && pasajeros > disponibles) {
                throw new SQLException("No hay cupos suficientes. Disponibles: " + disponibles);
            }
            datos.put("total", repo.precioPaquete(idPaquete) * pasajeros);
        }

        if (tabla.equals("pago")) {
            long idReserva = Long.parseLong(datos.get("id_reserva").toString());
            double monto = Double.parseDouble(datos.get("monto").toString());
            if ("Cancelada".equals(repo.estadoReserva(idReserva)) && !"Anulado".equals(datos.get("estado")))
                throw new SQLException("La reserva está cancelada: no se pueden registrar pagos.");
            if ("Pagado".equals(datos.get("estado"))) {
                double pagado = repo.pagosConfirmados(idReserva, id);
                double total = repo.totalReserva(idReserva);
                if (pagado + monto > total + 0.001) {
                    throw new SQLException("El pago supera el saldo de la reserva. Saldo disponible: S/ " +
                            String.format(Locale.US, "%.2f", total - pagado));
                }
            }
        }

        if (tabla.equals("boleta")) {
            long idReserva = Long.parseLong(datos.get("id_reserva").toString());
            if ("Cancelada".equals(repo.estadoReserva(idReserva)))
                throw new SQLException("No se puede emitir boleta de una reserva cancelada.");
            if (id == null) {
                for (Map<String, Object> b : repo.listar("boleta"))
                    if (String.valueOf(b.get("id_reserva")).equals(String.valueOf(idReserva)))
                        throw new SQLException("Esta reserva ya tiene una boleta: " + b.get("numero"));
            }
            datos.put("total", repo.totalReserva(idReserva));
        }

        try {
            if (id == null) repo.insertar(tabla, datos);
            else repo.actualizar(tabla, pk, id, datos);
        } catch (SQLException e) {
            if ("23503".equals(e.getSQLState()))
                throw new SQLException("El registro seleccionado no existe o está relacionado con otros datos.", e);
            if ("23505".equals(e.getSQLState()))
                throw new SQLException("Ya existe un registro con ese valor (DNI, usuario o número de boleta).", e);
            throw e;
        }
    }

    private boolean esVacio(Object valor) {
        return valor == null || valor.toString().isBlank();
    }

    private void validar(String tabla, Map<String, Object> datos, boolean esNuevo) throws SQLException {
        for (Map.Entry<String, Object> campo : datos.entrySet()) {
            // El total de una reserva no lo escribe el usuario: se calcula abajo.
            if (tabla.equals("reserva") && campo.getKey().equals("total")) continue;
            if (tabla.equals("cliente") && (campo.getKey().equals("correo") || campo.getKey().equals("telefono"))) continue;
            if (tabla.equals("destino") && campo.getKey().equals("descripcion")) continue;
            if (campo.getValue() == null || campo.getValue().toString().isBlank()) {
                throw new SQLException("El campo " + campo.getKey() + " es obligatorio.");
            }
        }

        if (datos.containsKey("precio") && Double.parseDouble(datos.get("precio").toString()) <= 0)
            throw new SQLException("El precio debe ser mayor que cero.");
        if (datos.containsKey("monto") && Double.parseDouble(datos.get("monto").toString()) <= 0)
            throw new SQLException("El monto debe ser mayor que cero.");
        if (datos.containsKey("pasajeros") && Integer.parseInt(datos.get("pasajeros").toString()) <= 0)
            throw new SQLException("La cantidad de pasajeros debe ser mayor que cero.");

        if (tabla.equals("usuario")) {
            if (!datos.get("usuario").toString().matches("[A-Za-z0-9_]{4,30}"))
                throw new SQLException("El usuario debe tener de 4 a 30 letras, números o guion bajo.");
            if (datos.containsKey("clave") && datos.get("clave").toString().length() < 6)
                throw new SQLException("La contraseña debe tener al menos 6 caracteres.");
        }

        if (tabla.equals("cliente")) {
            String dni = datos.get("dni").toString();
            if (!dni.matches("\\d{8}")) throw new SQLException("El DNI debe contener exactamente 8 dígitos.");
            String correo = datos.get("correo").toString();
            if (!correo.isBlank() && !correo.matches("^[\\w.+-]+@[\\w.-]+\\.[A-Za-z]{2,}$"))
                throw new SQLException("El correo electrónico no es válido.");
            String telefono = datos.get("telefono").toString();
            if (!telefono.isBlank() && !telefono.matches("\\d{9}"))
                throw new SQLException("El teléfono debe contener 9 dígitos.");
        }

        for (String campo : List.of("fecha_reserva", "fecha_pago", "fecha_emision")) {
            if (datos.containsKey(campo)) {
                try {
                    LocalDate.parse(datos.get(campo).toString());
                } catch (Exception e) {
                    throw new SQLException("La fecha debe tener el formato AAAA-MM-DD.");
                }
            }
        }

        if (tabla.equals("reserva")) {
            LocalDate fecha = LocalDate.parse(datos.get("fecha_reserva").toString());
            // Solo al crear: editar el estado de una reserva antigua debe seguir siendo posible.
            if (esNuevo && fecha.isBefore(LocalDate.now())) throw new SQLException("La fecha de reserva no puede ser anterior a hoy.");
        }
    }

    public void eliminar(String tabla, String pk, Object id) throws SQLException {
        try {
            repo.eliminar(tabla, pk, id);
        } catch (SQLException e) {
            if ("23503".equals(e.getSQLState()))
                throw new SQLException("No se puede eliminar: el registro está siendo usado por otros datos " +
                        "(por ejemplo, un cliente con reservas). Elimine primero los registros que dependen de él.", e);
            throw e;
        }
    }

    /** Descripción legible de cada reserva para las listas desplegables. */
    public LinkedHashMap<Long, String> etiquetasReserva() throws SQLException {
        return repo.etiquetasReserva();
    }

    public boolean login(String usuario, String clave) throws SQLException {
        return repo.login(usuario, clave);
    }

    public String obtenerRol(String usuario) throws SQLException {
        return repo.obtenerRol(usuario);
    }

    public String resumen() throws SQLException {
        StringBuilder texto = new StringBuilder("REPORTE GENERAL DE SysTurismo\n\n");
        for (String tabla : List.of("cliente", "destino", "paquete", "reserva", "pago", "boleta")) {
            texto.append(tabla.toUpperCase()).append(": ").append(repo.listar(tabla).size()).append("\n");
        }
        texto.append("\nINGRESOS CONFIRMADOS: ").append(repo.reporteIngresos());
        return texto.toString();
    }
}
