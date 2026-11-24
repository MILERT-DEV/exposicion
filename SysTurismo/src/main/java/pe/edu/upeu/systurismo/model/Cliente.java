package pe.edu.upeu.systurismo.model;

public class Cliente {
    private Long idCliente;
    private String dni;
    private String nombres;
    private String correo;
    private String telefono;

    public Cliente() {}
    public Cliente(Long idCliente, String dni, String nombres, String correo, String telefono) {
        this.idCliente = idCliente;
        this.dni = dni;
        this.nombres = nombres;
        this.correo = correo;
        this.telefono = telefono;
    }
    public Long getIdCliente() { return idCliente; }
    public void setIdCliente(Long idCliente) { this.idCliente = idCliente; }
    public String getDni() { return dni; }
    public void setDni(String dni) { this.dni = dni; }
    public String getNombres() { return nombres; }
    public void setNombres(String nombres) { this.nombres = nombres; }
    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }
    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }
}