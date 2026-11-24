package pe.edu.upeu.systurismo.model;

public class Usuario {
    private Long idUsuario;
    private String usuario;
    private String clave;
    private String rol;

    public Usuario() {}
    public Usuario(Long idUsuario, String usuario, String clave, String rol) {
        this.idUsuario = idUsuario;
        this.usuario = usuario;
        this.clave = clave;
        this.rol = rol;
    }
    public Long getIdUsuario() { return idUsuario; }
    public void setIdUsuario(Long idUsuario) { this.idUsuario = idUsuario; }
    public String getUsuario() { return usuario; }
    public void setUsuario(String usuario) { this.usuario = usuario; }
    public String getClave() { return clave; }
    public void setClave(String clave) { this.clave = clave; }
    public String getRol() { return rol; }
    public void setRol(String rol) { this.rol = rol; }
}