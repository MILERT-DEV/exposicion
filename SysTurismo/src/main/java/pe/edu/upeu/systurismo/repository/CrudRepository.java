package pe.edu.upeu.systurismo.repository;

import pe.edu.upeu.systurismo.config.DatabaseConfig;
import pe.edu.upeu.systurismo.util.PasswordUtil;
import java.sql.*;
import java.util.*;

/*
 * Acceso a datos. Las tablas y columnas se reciben desde las definiciones
 * internas del Controller; los valores siempre se envian como parametros.
 */
public class CrudRepository {
    public List<Map<String, Object>> listar(String tabla) throws SQLException {
        List<Map<String, Object>> datos = new ArrayList<>();
        try (Statement st = DatabaseConfig.getConnection().createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM " + tabla + " ORDER BY 1")) {
            ResultSetMetaData md = rs.getMetaData();
            while (rs.next()) {
                Map<String, Object> fila = new LinkedHashMap<>();
                for (int i = 1; i <= md.getColumnCount(); i++) {
                    fila.put(md.getColumnName(i).toLowerCase(), rs.getObject(i));
                }
                datos.add(fila);
            }
        }
        return datos;
    }

    public void insertar(String tabla, LinkedHashMap<String, Object> valores) throws SQLException {
        String columnas = String.join(",", valores.keySet());
        String signos = String.join(",", Collections.nCopies(valores.size(), "?"));
        String sql = "INSERT INTO " + tabla + "(" + columnas + ") VALUES(" + signos + ")";
        try (PreparedStatement ps = DatabaseConfig.getConnection().prepareStatement(sql)) {
            int i = 1;
            for (Object valor : valores.values()) ps.setObject(i++, valor);
            ps.executeUpdate();
        }
    }

    public void actualizar(String tabla, String pk, Object id,
                           LinkedHashMap<String, Object> valores) throws SQLException {
        List<String> sets = new ArrayList<>();
        for (String columna : valores.keySet()) sets.add(columna + "=?");
        String sql = "UPDATE " + tabla + " SET " + String.join(",", sets) + " WHERE " + pk + "=?";
        try (PreparedStatement ps = DatabaseConfig.getConnection().prepareStatement(sql)) {
            int i = 1;
            for (Object valor : valores.values()) ps.setObject(i++, valor);
            ps.setObject(i, id);
            ps.executeUpdate();
        }
    }

    public void eliminar(String tabla, String pk, Object id) throws SQLException {
        try (PreparedStatement ps = DatabaseConfig.getConnection()
                .prepareStatement("DELETE FROM " + tabla + " WHERE " + pk + "=?")) {
            ps.setObject(1, id);
            ps.executeUpdate();
        }
    }

    /** Verifica usuario y clave. Si el hash es del formato antiguo (SHA-256), lo migra a PBKDF2. */
    public boolean login(String usuario, String clave) throws SQLException {
        String guardado;
        try (PreparedStatement ps = DatabaseConfig.getConnection()
                .prepareStatement("SELECT clave FROM usuario WHERE usuario=?")) {
            ps.setString(1, usuario);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return false;
                guardado = rs.getString(1);
            }
        }
        if (!PasswordUtil.verificar(clave, guardado)) return false;
        if (PasswordUtil.esAntiguo(guardado)) {
            try (PreparedStatement ps = DatabaseConfig.getConnection()
                    .prepareStatement("UPDATE usuario SET clave=? WHERE usuario=?")) {
                ps.setString(1, PasswordUtil.hash(clave));
                ps.setString(2, usuario);
                ps.executeUpdate();
            }
        }
        return true;
    }

    /** Estado actual de una reserva (Pendiente, Confirmada, Cancelada). */
    public String estadoReserva(long idReserva) throws SQLException {
        try (PreparedStatement ps = DatabaseConfig.getConnection()
                .prepareStatement("SELECT estado FROM reserva WHERE id_reserva=?")) {
            ps.setLong(1, idReserva);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) throw new SQLException("La reserva no existe.");
                return rs.getString(1);
            }
        }
    }

    /** Texto legible de cada reserva: "#3 - Juan Perez - Machu Picchu Clásico". */
    public LinkedHashMap<Long, String> etiquetasReserva() throws SQLException {
        LinkedHashMap<Long, String> mapa = new LinkedHashMap<>();
        String sql = "SELECT r.id_reserva, c.nombres, p.nombre FROM reserva r " +
                "JOIN cliente c ON c.id_cliente=r.id_cliente " +
                "JOIN paquete p ON p.id_paquete=r.id_paquete ORDER BY r.id_reserva";
        try (Statement st = DatabaseConfig.getConnection().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) mapa.put(rs.getLong(1), rs.getString(2) + " - " + rs.getString(3));
        }
        return mapa;
    }

    private static final String SQL_DETALLE_BOLETA =
            "SELECT b.*, c.nombres AS cliente, c.dni AS dni, p.nombre AS paquete, d.nombre AS destino, " +
            "d.ciudad AS ciudad, p.dias AS dias, r.pasajeros AS pasajeros, r.fecha_reserva AS fecha_reserva " +
            "FROM boleta b " +
            "LEFT JOIN reserva r ON r.id_reserva=b.id_reserva " +
            "LEFT JOIN cliente c ON c.id_cliente=r.id_cliente " +
            "LEFT JOIN paquete p ON p.id_paquete=r.id_paquete " +
            "LEFT JOIN destino d ON d.id_destino=p.id_destino ";

    /** Boletas con los datos del viaje: cliente, destino, días y pasajeros. */
    public List<Map<String, Object>> listarBoletas() throws SQLException {
        List<Map<String, Object>> datos = new ArrayList<>();
        try (Statement st = DatabaseConfig.getConnection().createStatement();
             ResultSet rs = st.executeQuery(SQL_DETALLE_BOLETA + "ORDER BY b.id_boleta")) {
            ResultSetMetaData md = rs.getMetaData();
            while (rs.next()) {
                Map<String, Object> fila = new LinkedHashMap<>();
                for (int i = 1; i <= md.getColumnCount(); i++) fila.put(md.getColumnLabel(i).toLowerCase(), rs.getObject(i));
                datos.add(fila);
            }
        }
        return datos;
    }

    /** Datos completos de una boleta (cliente, destino, días, pasajeros) para el comprobante. */
    public Map<String, Object> detalleBoleta(Object idBoleta) throws SQLException {
        try (PreparedStatement ps = DatabaseConfig.getConnection()
                .prepareStatement(SQL_DETALLE_BOLETA + "WHERE b.id_boleta=?")) {
            ps.setObject(1, idBoleta);
            try (ResultSet rs = ps.executeQuery()) {
                Map<String, Object> fila = new LinkedHashMap<>();
                if (!rs.next()) return fila;
                ResultSetMetaData md = rs.getMetaData();
                for (int i = 1; i <= md.getColumnCount(); i++) fila.put(md.getColumnLabel(i).toLowerCase(), rs.getObject(i));
                return fila;
            }
        }
    }

    public String obtenerRol(String usuario) throws SQLException {
        try (PreparedStatement ps = DatabaseConfig.getConnection()
                .prepareStatement("SELECT rol FROM usuario WHERE usuario=?")) {
            ps.setString(1, usuario);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getString(1) : "Usuario";
            }
        }
    }

    public double precioPaquete(long id) throws SQLException {
        try (PreparedStatement ps = DatabaseConfig.getConnection()
                .prepareStatement("SELECT precio FROM paquete WHERE id_paquete=?")) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) throw new SQLException("No existe el paquete seleccionado.");
                return rs.getDouble(1);
            }
        }
    }

    public int cuposPaquete(long id) throws SQLException {
        try (PreparedStatement ps = DatabaseConfig.getConnection()
                .prepareStatement("SELECT cupos FROM paquete WHERE id_paquete=?")) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) throw new SQLException("No existe el paquete seleccionado.");
                return rs.getInt(1);
            }
        }
    }

    public int pasajerosReservados(long idPaquete, Object reservaExcluir) throws SQLException {
        String sql = "SELECT COALESCE(SUM(pasajeros),0) FROM reserva " +
                "WHERE id_paquete=? AND estado<>'Cancelada' " +
                (reservaExcluir == null ? "" : "AND id_reserva<>?");
        try (PreparedStatement ps = DatabaseConfig.getConnection().prepareStatement(sql)) {
            ps.setLong(1, idPaquete);
            if (reservaExcluir != null) ps.setObject(2, reservaExcluir);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    public double totalReserva(long idReserva) throws SQLException {
        try (PreparedStatement ps = DatabaseConfig.getConnection()
                .prepareStatement("SELECT total FROM reserva WHERE id_reserva=?")) {
            ps.setLong(1, idReserva);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) throw new SQLException("La reserva no existe.");
                return rs.getDouble(1);
            }
        }
    }

    public double pagosConfirmados(long idReserva, Object pagoExcluir) throws SQLException {
        String sql = "SELECT COALESCE(SUM(monto),0) FROM pago " +
                "WHERE id_reserva=? AND estado='Pagado' " +
                (pagoExcluir == null ? "" : "AND id_pago<>?");
        try (PreparedStatement ps = DatabaseConfig.getConnection().prepareStatement(sql)) {
            ps.setLong(1, idReserva);
            if (pagoExcluir != null) ps.setObject(2, pagoExcluir);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getDouble(1);
            }
        }
    }

    public String reporteIngresos() throws SQLException {
        String sql = "SELECT COALESCE(SUM(monto),0) FROM pago WHERE estado='Pagado'";
        try (Statement st = DatabaseConfig.getConnection().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            rs.next();
            return String.format(java.util.Locale.US, "S/ %.2f", rs.getDouble(1));
        }
    }

    /** Catálogo de precios: paquete + destino + cupos que aún quedan libres. */
    public List<Map<String, Object>> catalogoPrecios() throws SQLException {
        String sql = "SELECT p.id_paquete, p.nombre AS paquete, d.nombre AS destino, d.ciudad AS ciudad, " +
                "p.dias AS dias, p.precio AS precio, " +
                "p.cupos - COALESCE((SELECT SUM(r.pasajeros) FROM reserva r " +
                "WHERE r.id_paquete=p.id_paquete AND r.estado<>'Cancelada'),0) AS disponibles " +
                "FROM paquete p JOIN destino d ON d.id_destino=p.id_destino ORDER BY p.nombre";
        List<Map<String, Object>> datos = new ArrayList<>();
        try (Statement st = DatabaseConfig.getConnection().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            ResultSetMetaData md = rs.getMetaData();
            while (rs.next()) {
                Map<String, Object> fila = new LinkedHashMap<>();
                for (int i = 1; i <= md.getColumnCount(); i++) fila.put(md.getColumnLabel(i).toLowerCase(), rs.getObject(i));
                datos.add(fila);
            }
        }
        return datos;
    }
}
