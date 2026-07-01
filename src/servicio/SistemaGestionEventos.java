package servicio;

import modelo.*;
import excepciones.VoluntariadoException;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

public class SistemaGestionEventos implements Serializable {
    private static final long serialVersionUID = 1L;

    private Map<String, Usuario> usuarios;
    private Map<Integer, Evento> eventos; // Cambiado para permitir eventos a la misma hora
    private Queue<Inscripcion> filaEspera;
    private Stack<Inscripcion> historialAcciones;
    private List<Inscripcion> historialCompleto;
    private List<Requisito> catalogoRequisitos;
    private List<RegistroAsistencia> registrosAsistencia;

    public SistemaGestionEventos() {
        usuarios = new HashMap<>();
        eventos = new TreeMap<>(); // El TreeMap ahora ordena por ID
        filaEspera = new LinkedList<>();
        historialAcciones = new Stack<>();
        historialCompleto = new ArrayList<>();
        catalogoRequisitos = new ArrayList<>();
        registrosAsistencia = new ArrayList<>();

        Usuario admin = new Usuario("Anthony Balseca", "1700000000", "admin", "1234", Enums.RolUsuario.ADMINISTRADOR);
        Usuario coord1 = new Usuario("Mateo Peñaherrera", "1711111111", "mateo", "1234", Enums.RolUsuario.COORDINADOR);
        Usuario coord2 = new Usuario("Damian Esparza", "1722222222", "damian", "1234", Enums.RolUsuario.COORDINADOR);
        Usuario vol1 = new Usuario("Cristian Quillupangui", "1733333333", "cristian", "1234", Enums.RolUsuario.VOLUNTARIO);

        usuarios.put(admin.getUsername(), admin);
        usuarios.put(coord1.getUsername(), coord1);
        usuarios.put(coord2.getUsername(), coord2);
        usuarios.put(vol1.getUsername(), vol1);

        Evento ev1 = new Evento("Auditoría de Ciberseguridad", "Campus UDLA", LocalDate.now().plusDays(2), LocalTime.of(10, 0), LocalTime.of(13, 0), 30, "Laptops", 30);
        Evento ev2 = new Evento("Simulación Alta Disponibilidad", "Laboratorio Redes", LocalDate.now().plusDays(5), LocalTime.of(14, 0), LocalTime.of(16, 0), 50, "Servidor Linux", 1);

        eventos.put(ev1.getId(), ev1);
        eventos.put(ev2.getId(), ev2);

        Inscripcion ins1 = new Inscripcion(vol1, ev1);
        filaEspera.offer(ins1);
        historialCompleto.add(ins1);

        catalogoRequisitos.add(new Requisito(ev1, "Script de pruebas", Enums.TipoRequisito.HERRAMIENTA, 1));
    }

    public Usuario iniciarSesion(String user, String pass) {
        Usuario u = usuarios.get(user);
        if (u == null || !u.getPassword().equals(pass)) throw new VoluntariadoException("Credenciales incorrectas.");
        return u;
    }

    public void registrarUsuario(Usuario u) {
        if (!u.getCedula().matches("^[0-9]{10}$")) throw new VoluntariadoException("La cédula debe tener 10 dígitos.");
        if (!u.getNombreCompleto().matches("^[a-zA-ZáéíóúÁÉÍÓÚñÑ\\s]+$")) throw new VoluntariadoException("El nombre no puede contener números.");
        if (usuarios.containsKey(u.getUsername())) throw new VoluntariadoException("Usuario en uso.");
        for (Usuario ex : usuarios.values()) if (ex.getCedula().equals(u.getCedula())) throw new VoluntariadoException("Cédula registrada.");
        usuarios.put(u.getUsername(), u);
    }

    // --- NUEVA LÓGICA DE EVENTOS (FECHA + HORA + LUGAR) ---
    public void crearEvento(Evento evento) {
        for (Evento e : eventos.values()) {
            if (e.getNombre().equalsIgnoreCase(evento.getNombre())) {
                throw new VoluntariadoException("El evento ya existe con ese nombre.");
            }
            // Verifica que no coincida exactamente FECHA, HORA y LUGAR
            if (e.getFecha().equals(evento.getFecha()) &&
                    e.getHoraInicio().equals(evento.getHoraInicio()) &&
                    e.getLugar().equalsIgnoreCase(evento.getLugar())) {
                throw new VoluntariadoException("Ya existe un evento en esa fecha, hora y LUGAR físico.");
            }
        }
        eventos.put(evento.getId(), evento);
    }

    public List<Evento> getEventosDisponibles() { return new ArrayList<>(eventos.values()); }
    public Evento buscarEventoPorId(int id) { return busquedaRecursiva(getEventosDisponibles(), id, 0); }

    private Evento busquedaRecursiva(List<Evento> lista, int id, int index) {
        if (index >= lista.size()) return null;
        if (lista.get(index).getId() == id) return lista.get(index);
        return busquedaRecursiva(lista, id, index + 1);
    }

    public void modificarEvento(int idViejo, Evento datosNuevos) {
        Evento original = buscarEventoPorId(idViejo);
        if (original == null) throw new VoluntariadoException("Evento no encontrado.");

        for (Evento e : eventos.values()) {
            if (e.getId() != idViejo &&
                    e.getFecha().equals(datosNuevos.getFecha()) &&
                    e.getHoraInicio().equals(datosNuevos.getHoraInicio()) &&
                    e.getLugar().equalsIgnoreCase(datosNuevos.getLugar())) {
                throw new VoluntariadoException("Cruce de fecha, hora y LUGAR con otro evento.");
            }
        }

        int cuposOcupados = original.getCuposTotales() - original.getCuposDisponibles();
        int nuevosDisponibles = datosNuevos.getCuposTotales() - cuposOcupados;
        if (nuevosDisponibles < 0) throw new VoluntariadoException("Hay " + cuposOcupados + " inscritos. No puedes reducir tanto los cupos.");

        original.setNombre(datosNuevos.getNombre());
        original.setLugar(datosNuevos.getLugar());
        original.setFecha(datosNuevos.getFecha());
        original.setHoraInicio(datosNuevos.getHoraInicio());
        original.setHoraFin(datosNuevos.getHoraFin());
        original.setCuposTotales(datosNuevos.getCuposTotales());
        original.setCuposDisponibles(nuevosDisponibles);
    }

    public void solicitarInscripcion(Usuario vol, Evento ev) {
        for (Inscripcion ins : historialCompleto) {
            if (ins.getVoluntario().getUsername().equals(vol.getUsername())) {
                if (ins.getEvento().getId() == ev.getId()) {
                    if (ins.getEstado() == Enums.EstadoInscripcion.RECHAZADA) throw new VoluntariadoException("Solicitud rechazada previamente.");
                    throw new VoluntariadoException("Ya solicitaste este evento.");
                }
                if (ins.getEstado() != Enums.EstadoInscripcion.RECHAZADA && ins.getEvento().getFecha().equals(ev.getFecha())) {
                    if (ev.getHoraInicio().isBefore(ins.getEvento().getHoraFin()) && ev.getHoraFin().isAfter(ins.getEvento().getHoraInicio())) {
                        throw new VoluntariadoException("Cruce de horarios.");
                    }
                }
            }
        }
        if (ev.getCuposDisponibles() <= 0) throw new VoluntariadoException("No hay cupos.");
        Inscripcion nueva = new Inscripcion(vol, ev);
        filaEspera.offer(nueva);
        historialCompleto.add(nueva);
    }

    public void procesarInscripcion(boolean aprobar) {
        if (filaEspera.isEmpty()) throw new VoluntariadoException("Fila vacía.");
        Inscripcion proc = filaEspera.peek();
        if (aprobar) {
            if (proc.getEvento().getCuposDisponibles() <= 0) throw new VoluntariadoException("Evento lleno.");
            proc.getEvento().modificarCupos(-1);
        }
        proc = filaEspera.poll();
        proc.setEstado(aprobar ? Enums.EstadoInscripcion.APROBADA : Enums.EstadoInscripcion.RECHAZADA);
        historialAcciones.push(proc);
    }

    public void deshacerAccion() {
        if (historialAcciones.isEmpty()) throw new VoluntariadoException("Nada que deshacer.");
        Inscripcion rev = historialAcciones.pop();
        if (rev.getEstado() == Enums.EstadoInscripcion.APROBADA) rev.getEvento().modificarCupos(1);
        rev.setEstado(Enums.EstadoInscripcion.PENDIENTE);
        ((LinkedList<Inscripcion>) filaEspera).addFirst(rev);
    }

    public Queue<Inscripcion> getFilaEspera() { return filaEspera; }
    public List<Inscripcion> getHistorialCompleto() { return historialCompleto; }

    public void agregarRequisito(Requisito req) { catalogoRequisitos.add(req); }
    public List<Requisito> getCatalogoRequisitos() { return catalogoRequisitos; }

    public List<Requisito> getRequisitosPorEvento(int idEvento) {
        List<Requisito> lista = new ArrayList<>();
        for (Requisito r : catalogoRequisitos) {
            if (r.getEvento().getId() == idEvento) lista.add(r);
        }
        return lista;
    }

    public void gestionarRequisito(int idRequisito, Enums.EstadoRequisito nuevoEstado, int cantidadAutorizada) {
        for (Requisito r : catalogoRequisitos) {
            if (r.getId() == idRequisito) {

                if (cantidadAutorizada < 0) {
                    throw new VoluntariadoException("No puedes autorizar cantidades negativas.");
                }

                if (nuevoEstado == Enums.EstadoRequisito.APROBADO && cantidadAutorizada > r.getCantidadRequerida()) {
                    throw new VoluntariadoException("El admin no puede autorizar más de lo solicitado. (Solicitado: "
                            + r.getCantidadRequerida() + ", Intentaste autorizar: " + cantidadAutorizada + ")");
                }

                r.setEstado(nuevoEstado);
                r.setCantidadAprobada(cantidadAutorizada);
                return;
            }
        }
        throw new VoluntariadoException("Requisito no encontrado.");
    }

    public void registrarAsistenciaAuditable(int idInscripcion, String coordinador) {
        for (Inscripcion ins : historialCompleto) {
            if (ins.getId() == idInscripcion && ins.getEstado() == Enums.EstadoInscripcion.APROBADA) {
                ins.setEstado(Enums.EstadoInscripcion.ASISTIO);
                registrosAsistencia.add(new RegistroAsistencia(ins, coordinador));
            }
        }
    }
    public List<RegistroAsistencia> getRegistrosAsistencia() { return registrosAsistencia; }
}