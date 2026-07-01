package modelo;
import java.io.Serializable;

public class Inscripcion implements Serializable {
    private static final long serialVersionUID = 1L;
    private static int genId = 1;
    public static void setGenId(int id) { genId = id; }

    private int id;
    private Usuario voluntario;
    private Evento evento;
    private Enums.EstadoInscripcion estado;

    public Inscripcion(Usuario vol, Evento ev) {
        this.id = genId++;
        this.voluntario = vol;
        this.evento = ev;
        this.estado = Enums.EstadoInscripcion.PENDIENTE;
    }

    public int getId() { return id; }
    public Usuario getVoluntario() { return voluntario; }
    public Evento getEvento() { return evento; }
    public Enums.EstadoInscripcion getEstado() { return estado; }
    public void setEstado(Enums.EstadoInscripcion est) { this.estado = est; }

    @Override
    public String toString() { return voluntario.getNombreCompleto() + " -> " + evento.getNombre(); }
}