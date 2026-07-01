package modelo;
import java.io.Serializable;

public class Requisito implements Serializable {
    private static final long serialVersionUID = 1L;
    private static int generadorId = 1;
    public static void setGeneradorId(int id) { generadorId = id; }

    private int id;
    private Evento evento;
    private String nombreRecurso;
    private Enums.TipoRequisito tipo;
    private int cantidadRequerida;
    private Enums.EstadoRequisito estado;
    private int cantidadAprobada;

    public Requisito(Evento evento, String nombreRecurso, Enums.TipoRequisito tipo, int cantidadRequerida) {
        this.id = generadorId++;
        this.evento = evento;
        this.nombreRecurso = nombreRecurso;
        this.tipo = tipo;
        this.cantidadRequerida = cantidadRequerida;
        this.estado = Enums.EstadoRequisito.PENDIENTE;
        this.cantidadAprobada = 0;
    }

    public int getId() { return id; }
    public Evento getEvento() { return evento; }
    public String getNombreRecurso() { return nombreRecurso; }
    public Enums.TipoRequisito getTipo() { return tipo; }
    public int getCantidadRequerida() { return cantidadRequerida; }
    public Enums.EstadoRequisito getEstado() { return estado; }
    public void setEstado(Enums.EstadoRequisito estado) { this.estado = estado; }
    public int getCantidadAprobada() { return cantidadAprobada; }
    public void setCantidadAprobada(int cantidad) { this.cantidadAprobada = cantidad; }

    @Override
    public String toString() {
        return evento.getNombre() + " | " + nombreRecurso + " (Req: " + cantidadRequerida + " | Auto: " + cantidadAprobada + ")";
    }
}