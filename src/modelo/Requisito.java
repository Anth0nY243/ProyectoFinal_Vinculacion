package modelo;

import java.io.Serializable;

public class Requisito implements Serializable {
    private static final long serialVersionUID = 1L;
    private static int generadorId = 1;

    public static void setGeneradorId(int id) { generadorId = id; }

    private int id;
    private Evento evento;
    private String nombreRecurso;
    private TipoRequisito tipo;
    private int cantidadRequerida;
    private Enums.EstadoRequisito estado; // PENDIENTE, APROBADO, RECHAZADO
    private int cantidadAprobada;

    public Requisito(Evento evento, String nombreRecurso, TipoRequisito tipo, int cantidadRequerida) {
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
    public void setNombreRecurso(String nombreRecurso) { this.nombreRecurso = nombreRecurso; }
    public TipoRequisito getTipo() { return tipo; }
    public void setTipo(TipoRequisito tipo) { this.tipo = tipo; }
    public int getCantidadRequerida() { return cantidadRequerida; }
    public void setCantidadRequerida(int cantidadRequerida) { this.cantidadRequerida = cantidadRequerida; }
    public Enums.EstadoRequisito getEstado() { return estado; }
    public void setEstado(Enums.EstadoRequisito estado) { this.estado = estado; }
    public int getCantidadAprobada() { return cantidadAprobada; }
    public void setCantidadAprobada(int cant) { this.cantidadAprobada = cant; }

    @Override
    public String toString() {
        return tipo + " - " + nombreRecurso + " (Cant: " + cantidadRequerida + ")";
    }
}