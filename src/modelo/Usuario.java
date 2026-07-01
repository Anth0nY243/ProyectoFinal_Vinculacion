package modelo;
import java.io.Serializable;

public class Usuario implements Serializable {
    private static final long serialVersionUID = 1L;
    private String nombreCompleto, cedula, username, password;
    private Enums.RolUsuario rol;

    public Usuario(String nombre, String cedula, String username, String pass, Enums.RolUsuario rol) {
        this.nombreCompleto = nombre; this.cedula = cedula;
        this.username = username; this.password = pass; this.rol = rol;
    }

    public String getNombreCompleto() { return nombreCompleto; }
    public String getCedula() { return cedula; }
    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public Enums.RolUsuario getRol() { return rol; }

    @Override
    public String toString() { return nombreCompleto + " (CI: " + cedula + ")"; }
}