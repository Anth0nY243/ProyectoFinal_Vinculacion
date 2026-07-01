package principal;

import servicio.*;
import vista.AppGUI;

public class Main {
    public static void main(String[] args) {
        SistemaGestionEventos sis = GestorArchivos.cargarDatos();
        if (sis == null) sis = new SistemaGestionEventos();

        final SistemaGestionEventos finalSis = sis;
        Runtime.getRuntime().addShutdownHook(new Thread(() -> GestorArchivos.guardarDatos(finalSis)));

        java.awt.EventQueue.invokeLater(() -> AppGUI.iniciar(finalSis));
    }
}