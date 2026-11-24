package pe.edu.upeu.systurismo.model;

public class Boleta {
    private Long idBoleta;
    private Long idReserva;
    private String fechaEmision;
    private String numero;
    private double total;

    public Boleta() {}
    public Boleta(Long idBoleta, Long idReserva, String fechaEmision, String numero, double total) {
        this.idBoleta = idBoleta;
        this.idReserva = idReserva;
        this.fechaEmision = fechaEmision;
        this.numero = numero;
        this.total = total;
    }
    public Long getIdBoleta() { return idBoleta; }
    public void setIdBoleta(Long idBoleta) { this.idBoleta = idBoleta; }
    public Long getIdReserva() { return idReserva; }
    public void setIdReserva(Long idReserva) { this.idReserva = idReserva; }
    public String getFechaEmision() { return fechaEmision; }
    public void setFechaEmision(String fechaEmision) { this.fechaEmision = fechaEmision; }
    public String getNumero() { return numero; }
    public void setNumero(String numero) { this.numero = numero; }
    public double getTotal() { return total; }
    public void setTotal(double total) { this.total = total; }
}