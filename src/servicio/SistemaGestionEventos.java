package servicio;

import modelo.*;
import excepciones.VoluntariadoException;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

public class SistemaGestionEventos implements Serializable {
    private static final long serialVersionUID = 1L;

    private Map<String, Usuario> usuarios = new HashMap<>();
    private TreeMap<LocalDateTime, Evento> eventos = new TreeMap<>();
    private Queue<Inscripcion> filaEspera = new LinkedList<>();
    private Stack<Inscripcion> historialAcciones = new Stack<>();
    private List<Inscripcion> historialCompleto = new ArrayList<>();

    // NUEVAS ESTRUCTURAS PARA LOS MÓDULOS DE REQUISITOS Y ASISTENCIA
    private List<Requisito> catalogoRequisitos = new ArrayList<>();
    private List<RegistroAsistencia> registrosAsistencia = new ArrayList<>();

    public SistemaGestionEventos() {
        // Inicialización de colecciones
        usuarios = new HashMap<>();
        eventos = new TreeMap<>();
        filaEspera = new LinkedList<>();
        historialAcciones = new Stack<>();
        historialCompleto = new ArrayList<>();
        catalogoRequisitos = new ArrayList<>();
        registrosAsistencia = new ArrayList<>();

        // 1. USUARIOS QUEMADOS (Roles completos)
        Usuario admin = new Usuario("Anthony Balseca", "1700000000", "admin", "1234", Enums.RolUsuario.ADMINISTRADOR);
        Usuario coord1 = new Usuario("Mateo Peñaherrera", "1711111111", "mateo", "1234", Enums.RolUsuario.COORDINADOR);
        Usuario coord2 = new Usuario("Damian Esparza", "1722222222", "damian", "1234", Enums.RolUsuario.COORDINADOR);
        Usuario vol1 = new Usuario("Cristian Quillupangui", "1733333333", "cristian", "1234", Enums.RolUsuario.VOLUNTARIO);
        Usuario vol2 = new Usuario("Voluntario Prueba", "1744444444", "vol", "1234", Enums.RolUsuario.VOLUNTARIO);

        usuarios.put(admin.getUsername(), admin);
        usuarios.put(coord1.getUsername(), coord1);
        usuarios.put(coord2.getUsername(), coord2);
        usuarios.put(vol1.getUsername(), vol1);
        usuarios.put(vol2.getUsername(), vol2);

        // 2. EVENTOS QUEMADOS
        Evento ev1 = new Evento("Auditoría de Ciberseguridad", "Campus UDLA", LocalDate.now().plusDays(2), LocalTime.of(10, 0), LocalTime.of(13, 0), 30, "Laptops", 30);
        Evento ev2 = new Evento("Simulación Alta Disponibilidad", "Laboratorio Redes", LocalDate.now().plusDays(5), LocalTime.of(14, 0), LocalTime.of(16, 0), 50, "Servidor Linux", 1);

        LocalDateTime claveEv1 = LocalDateTime.of(ev1.getFecha(), ev1.getHoraInicio());
        LocalDateTime claveEv2 = LocalDateTime.of(ev2.getFecha(), ev2.getHoraInicio());

        eventos.put(claveEv1, ev1);
        eventos.put(claveEv2, ev2);

        // 3. INSCRIPCIONES QUEMADAS (Para probar la Cola FIFO)
        Inscripcion ins1 = new Inscripcion(vol1, ev1);
        Inscripcion ins2 = new Inscripcion(vol2, ev1);

        filaEspera.offer(ins1);
        filaEspera.offer(ins2);

        historialCompleto.add(ins1);
        historialCompleto.add(ins2);

        // 4. REQUISITOS QUEMADOS (Para probar el Módulo de Requisitos)
        Requisito req1 = new Requisito(ev1, "Script de pruebas", TipoRequisito.HERRAMIENTA, 1);
        catalogoRequisitos.add(req1);
    }

    // --- MÓDULO DE USUARIOS ---
    public Usuario iniciarSesion(String user, String pass) {
        Usuario u = usuarios.get(user);
        if (u == null || !u.getPassword().equals(pass)) throw new VoluntariadoException("Credenciales incorrectas.");
        return u;
    }

    public void registrarUsuario(Usuario u) {
        if (!u.getCedula().matches("^[0-9]{10}$")) throw new VoluntariadoException("La cédula debe tener exactamente 10 dígitos numéricos.");
        if (!u.getNombreCompleto().matches("^[a-zA-ZáéíóúÁÉÍÓÚñÑ\\s]+$")) throw new VoluntariadoException("El nombre no puede contener números.");
        if (usuarios.containsKey(u.getUsername())) throw new VoluntariadoException("Usuario en uso.");
        for (Usuario ex : usuarios.values()) if (ex.getCedula().equals(u.getCedula())) throw new VoluntariadoException("Cédula registrada.");
        usuarios.put(u.getUsername(), u);
    }

    // --- MÓDULO DE EVENTOS ---
    public void crearEvento(Evento evento) {
        LocalDateTime clave = LocalDateTime.of(evento.getFecha(), evento.getHoraInicio());
        if (eventos.containsKey(clave)) throw new VoluntariadoException("Ya existe un evento que inicia a esa misma fecha y hora.");
        for (Evento e : eventos.values()) if (e.getNombre().equalsIgnoreCase(evento.getNombre())) throw new VoluntariadoException("El evento ya existe.");
        eventos.put(clave, evento);
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
        if (original == null) throw new VoluntariadoException("El evento no existe.");

        LocalDateTime claveVieja = LocalDateTime.of(original.getFecha(), original.getHoraInicio());
        LocalDateTime nuevaClave = LocalDateTime.of(datosNuevos.getFecha(), datosNuevos.getHoraInicio());

        if (!claveVieja.equals(nuevaClave) && eventos.containsKey(nuevaClave)) throw new VoluntariadoException("Ya existe un evento en esa nueva fecha/hora.");

        int cuposOcupados = original.getCuposTotales() - original.getCuposDisponibles();
        int nuevosDisponibles = datosNuevos.getCuposTotales() - cuposOcupados;
        if (nuevosDisponibles < 0) throw new VoluntariadoException("No puedes reducir tanto. Hay " + cuposOcupados + " inscritos.");

        eventos.remove(claveVieja);
        original.setNombre(datosNuevos.getNombre());
        original.setLugar(datosNuevos.getLugar());
        original.setFecha(datosNuevos.getFecha());
        original.setHoraInicio(datosNuevos.getHoraInicio());
        original.setHoraFin(datosNuevos.getHoraFin());
        original.setCuposTotales(datosNuevos.getCuposTotales());
        original.setCuposDisponibles(nuevosDisponibles);
        eventos.put(nuevaClave, original);
    }

    // --- MÓDULO DE INSCRIPCIONES ---
    public void solicitarInscripcion(Usuario vol, Evento ev) {
        for (Inscripcion ins : historialCompleto) {
            if (ins.getVoluntario().getUsername().equals(vol.getUsername())) {
                if (ins.getEvento().getId() == ev.getId()) {
                    if (ins.getEstado() == Enums.EstadoInscripcion.RECHAZADA) throw new VoluntariadoException("Solicitud rechazada previamente.");
                    throw new VoluntariadoException("Ya tienes una solicitud para este evento.");
                }
                if (ins.getEstado() != Enums.EstadoInscripcion.RECHAZADA && ins.getEvento().getFecha().equals(ev.getFecha())) {
                    if (ev.getHoraInicio().isBefore(ins.getEvento().getHoraFin()) && ev.getHoraFin().isAfter(ins.getEvento().getHoraInicio())) {
                        throw new VoluntariadoException("Cruce de horarios con el evento '" + ins.getEvento().getNombre() + "'.");
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
        if (historialAcciones.isEmpty()) throw new VoluntariadoException("No hay acciones para deshacer.");
        Inscripcion rev = historialAcciones.pop();
        if (rev.getEstado() == Enums.EstadoInscripcion.APROBADA) rev.getEvento().modificarCupos(1);
        rev.setEstado(Enums.EstadoInscripcion.PENDIENTE);
        ((LinkedList<Inscripcion>) filaEspera).addFirst(rev);
    }

    public Queue<Inscripcion> getFilaEspera() { return filaEspera; }
    public List<Inscripcion> getHistorialCompleto() { return historialCompleto; }

    // --- NUEVO MÓDULO DE REQUISITOS ---
    public void agregarRequisito(Requisito req) {
        catalogoRequisitos.add(req);
    }

    public List<Requisito> getRequisitosPorEvento(int idEvento) {
        List<Requisito> lista = new ArrayList<>();
        for (Requisito r : catalogoRequisitos) {
            if (r.getEvento().getId() == idEvento) lista.add(r);
        }
        return lista;
    }

    // --- NUEVO MÓDULO DE ASISTENCIA PROFESIONAL ---
    public void registrarAsistenciaAuditable(int idInscripcion, String coordinador) {
        for (Inscripcion ins : historialCompleto) {
            if (ins.getId() == idInscripcion && ins.getEstado() == Enums.EstadoInscripcion.APROBADA) {
                ins.setEstado(Enums.EstadoInscripcion.ASISTIO);
                RegistroAsistencia reg = new RegistroAsistencia(ins, coordinador);
                registrosAsistencia.add(reg);
            }
        }
    }

    public void gestionarRequisito(int idRequisito, Enums.EstadoRequisito nuevoEstado, int cantidadAutorizada) {
        for (Requisito r : catalogoRequisitos) {
            if (r.getId() == idRequisito) {
                if (nuevoEstado == Enums.EstadoRequisito.APROBADO && cantidadAutorizada > r.getCantidadRequerida()) {
                    throw new VoluntariadoException("El admin no puede autorizar más de lo solicitado.");
                }
                r.setEstado(nuevoEstado);
                r.setCantidadAprobada(cantidadAutorizada);
                return;
            }
        }
        throw new VoluntariadoException("Requisito no encontrado.");
    }
}