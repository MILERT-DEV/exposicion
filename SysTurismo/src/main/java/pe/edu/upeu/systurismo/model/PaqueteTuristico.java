package pe.edu.upeu.systurismo.model;

public class PaqueteTuristico {
    private Long idPaquete;
    private String nombre;
    private Long idDestino;
    private int dias;
    private double precio;
    private int cupos;

    public PaqueteTuristico() {}
    public PaqueteTuristico(Long idPaquete, String nombre, Long idDestino, int dias, double precio, int cupos) {
        this.idPaquete = idPaquete;
        this.nombre = nombre;
        this.idDestino = idDestino;
        this.dias = dias;
        this.precio = precio;
        this.cupos = cupos;
    }
    public Long getIdPaquete() { return idPaquete; }
    public void setIdPaquete(Long idPaquete) { this.idPaquete = idPaquete; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public Long getIdDestino() { return idDestino; }
    public void setIdDestino(Long idDestino) { this.idDestino = idDestino; }
    public int getDias() { return dias; }
    public void setDias(int dias) { this.dias = dias; }
    public double getPrecio() { return precio; }
    public void setPrecio(double precio) { this.precio = precio; }
    public int getCupos() { return cupos; }
    public void setCupos(int cupos) { this.cupos = cupos; }
}