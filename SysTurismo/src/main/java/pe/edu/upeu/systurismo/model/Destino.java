package pe.edu.upeu.systurismo.model;

public class Destino {
    private Long idDestino;
    private String nombre;
    private String ciudad;
    private String descripcion;

    public Destino() {}
    public Destino(Long idDestino, String nombre, String ciudad, String descripcion) {
        this.idDestino = idDestino;
        this.nombre = nombre;
        this.ciudad = ciudad;
        this.descripcion = descripcion;
    }
    public Long getIdDestino() { return idDestino; }
    public void setIdDestino(Long idDestino) { this.idDestino = idDestino; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getCiudad() { return ciudad; }
    public void setCiudad(String ciudad) { this.ciudad = ciudad; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
}