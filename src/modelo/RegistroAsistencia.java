package modelo;

import java.io.Serializable;
import java.time.LocalDateTime;

public class RegistroAsistencia implements Serializable {
    private static final long serialVersionUID = 1L;
    private static int generadorId = 1;

    public static void setGeneradorId(int id) { generadorId = id; }

    private int id;
    private Inscripcion inscripcionAsociada;
    private LocalDateTime horaLlegada;
    private String coordinadorResponsable;

    public RegistroAsistencia(Inscripcion inscripcionAsociada, String coordinadorResponsable) {
        this.id = generadorId++;
        this.inscripcionAsociada = inscripcionAsociada;
        this.horaLlegada = LocalDateTime.now();
        this.coordinadorResponsable = coordinadorResponsable;
    }

    public int getId() { return id; }
    public Inscripcion getInscripcionAsociada() { return inscripcionAsociada; }
    public LocalDateTime getHoraLlegada() { return horaLlegada; }
    public String getCoordinadorResponsable() { return coordinadorResponsable; }
}