package pe.edu.upeu.systurismo.model;

public class Reserva {
    private Long idReserva;
    private Long idCliente;
    private Long idPaquete;
    private String fechaReserva;
    private int pasajeros;
    private double total;
    private String estado;

    public Reserva() {}
    public Reserva(Long idReserva, Long idCliente, Long idPaquete, String fechaReserva, int pasajeros, double total, String estado) {
        this.idReserva = idReserva;
        this.idCliente = idCliente;
        this.idPaquete = idPaquete;
        this.fechaReserva = fechaReserva;
        this.pasajeros = pasajeros;
        this.total = total;
        this.estado = estado;
    }
    public Long getIdReserva() { return idReserva; }
    public void setIdReserva(Long idReserva) { this.idReserva = idReserva; }
    public Long getIdCliente() { return idCliente; }
    public void setIdCliente(Long idCliente) { this.idCliente = idCliente; }
    public Long getIdPaquete() { return idPaquete; }
    public void setIdPaquete(Long idPaquete) { this.idPaquete = idPaquete; }
    public String getFechaReserva() { return fechaReserva; }
    public void setFechaReserva(String fechaReserva) { this.fechaReserva = fechaReserva; }
    public int getPasajeros() { return pasajeros; }
    public void setPasajeros(int pasajeros) { this.pasajeros = pasajeros; }
    public double getTotal() { return total; }
    public void setTotal(double total) { this.total = total; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
}