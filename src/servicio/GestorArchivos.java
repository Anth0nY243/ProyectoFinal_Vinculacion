package servicio;
import modelo.*;
import java.io.*;

public class GestorArchivos {
    private static final String RUTA = "datos_voluntariado.dat";

    public static void guardarDatos(SistemaGestionEventos sis) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(RUTA))) {
            oos.writeObject(sis);
        } catch (Exception e) { System.err.println("Error al guardar: " + e.getMessage()); }
    }

    public static SistemaGestionEventos cargarDatos() {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(RUTA))) {
            SistemaGestionEventos sis = (SistemaGestionEventos) ois.readObject();

            // Restaurar generadores de ID
            int maxEv = 0; for(Evento e : sis.getEventosDisponibles()) if(e.getId()>maxEv) maxEv = e.getId();
            Evento.setGeneradorId(maxEv + 1);

            int maxIns = 0; for(Inscripcion i : sis.getHistorialCompleto()) if(i.getId()>maxIns) maxIns = i.getId();
            Inscripcion.setGenId(maxIns + 1);

            return sis;
        } catch (Exception e) { return null; }
    }
}