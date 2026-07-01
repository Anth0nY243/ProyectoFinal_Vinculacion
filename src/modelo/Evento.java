package modelo;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalTime;

public class Evento implements Serializable {
    private static final long serialVersionUID = 1L;
    private static int generadorId = 1;
    public static void setGeneradorId(int id) { generadorId = id; }

    private int id;
    private String nombre, lugar, tipoRecurso;
    private LocalDate fecha;
    private LocalTime horaInicio, horaFin;
    private int cuposTotales, cuposDisponibles, cantidadRecurso;
    private boolean asistenciaTomada = false;
    private String coordinadorAsistencia = null;

    public Evento(String nombre, String lugar, LocalDate fecha, LocalTime hInicio, LocalTime hFin, int cupos, String recurso, int cantRecurso) {
        this.id = generadorId++;
        this.nombre = nombre; this.lugar = lugar; this.fecha = fecha;
        this.horaInicio = hInicio; this.horaFin = hFin;
        this.cuposTotales = cupos; this.cuposDisponibles = cupos;
        this.tipoRecurso = recurso; this.cantidadRecurso = cantRecurso;
    }

    // ==========================================
    // GETTERS Y SETTERS COMPLETOS
    // ==========================================
    public int getId() { return id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    // AQUÍ ESTÁ EL MÉTODO QUE FALTABA
    public String getLugar() { return lugar; }
    public void setLugar(String lugar) { this.lugar = lugar; }

    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate fecha) { this.fecha = fecha; }

    public LocalTime getHoraInicio() { return horaInicio; }
    public void setHoraInicio(LocalTime horaInicio) { this.horaInicio = horaInicio; }

    public LocalTime getHoraFin() { return horaFin; }
    public void setHoraFin(LocalTime horaFin) { this.horaFin = horaFin; }

    public int getCuposTotales() { return cuposTotales; }
    public void setCuposTotales(int cupos) { this.cuposTotales = cupos; }

    public int getCuposDisponibles() { return cuposDisponibles; }
    public void setCuposDisponibles(int cupos) { this.cuposDisponibles = cupos; }
    public void modificarCupos(int v) { this.cuposDisponibles += v; }

    public String getTipoRecurso() { return tipoRecurso; }
    public void setTipoRecurso(String tipoRecurso) { this.tipoRecurso = tipoRecurso; }

    public int getCantidadRecurso() { return cantidadRecurso; }
    public void setCantidadRecurso(int cantidadRecurso) { this.cantidadRecurso = cantidadRecurso; }

    public boolean isAsistenciaTomada() { return asistenciaTomada; }
    public void setAsistenciaTomada(boolean b) { this.asistenciaTomada = b; }

    public String getCoordinadorAsistencia() { return coordinadorAsistencia; }
    public void setCoordinadorAsistencia(String c) { this.coordinadorAsistencia = c; }

    @Override
    public String toString() { return id + ". " + nombre + " (" + cuposDisponibles + " cupos)"; }
}