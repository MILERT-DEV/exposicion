package pe.edu.upeu.systurismo.model;

public class Pago {
    private Long idPago;
    private Long idReserva;
    private String fechaPago;
    private double monto;
    private String metodo;
    private String estado;

    public Pago() {}
    public Pago(Long idPago, Long idReserva, String fechaPago, double monto, String metodo, String estado) {
        this.idPago = idPago;
        this.idReserva = idReserva;
        this.fechaPago = fechaPago;
        this.monto = monto;
        this.metodo = metodo;
        this.estado = estado;
    }
    public Long getIdPago() { return idPago; }
    public void setIdPago(Long idPago) { this.idPago = idPago; }
    public Long getIdReserva() { return idReserva; }
    public void setIdReserva(Long idReserva) { this.idReserva = idReserva; }
    public String getFechaPago() { return fechaPago; }
    public void setFechaPago(String fechaPago) { this.fechaPago = fechaPago; }
    public double getMonto() { return monto; }
    public void setMonto(double monto) { this.monto = monto; }
    public String getMetodo() { return metodo; }
    public void setMetodo(String metodo) { this.metodo = metodo; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
}