package vista;

import excepciones.VoluntariadoException;
import modelo.*;
import servicio.SistemaGestionEventos;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.LocalTime;

public class AppGUI {
    private static SistemaGestionEventos sis;

    public static void iniciar(SistemaGestionEventos sistema) {
        sis = sistema;
        mostrarLogin();
    }

    private static void mostrarLogin() {
        JFrame frame = new JFrame("Login - Sistema de Eventos");
        frame.setSize(350, 200); frame.setLocationRelativeTo(null); frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new GridLayout(3, 2, 10, 10));

        JTextField txtUser = new JTextField();
        JPasswordField txtPass = new JPasswordField();
        JButton btnLogin = new JButton("Ingresar");
        JButton btnRegistro = new JButton("Registro Rápido");

        frame.add(new JLabel(" Usuario:")); frame.add(txtUser);
        frame.add(new JLabel(" Contraseña:")); frame.add(txtPass);
        frame.add(btnRegistro); frame.add(btnLogin);

        btnLogin.addActionListener(e -> {
            try {
                Usuario u = sis.iniciarSesion(txtUser.getText(), new String(txtPass.getPassword()));
                frame.dispose();
                mostrarVentanaPrincipal(u);
            } catch (Exception ex) { JOptionPane.showMessageDialog(frame, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE); }
        });

        btnRegistro.addActionListener(e -> mostrarRegistro(null));
        frame.setVisible(true);
    }

    private static void mostrarRegistro(Usuario admin) {
        JFrame f = new JFrame("Registro de Usuarios");
        f.setSize(400, 300); f.setLocationRelativeTo(null); f.setLayout(new GridLayout(6, 2, 5, 5));

        JTextField txtNom = new JTextField(), txtCed = new JTextField(), txtUser = new JTextField();
        JPasswordField txtPass = new JPasswordField();
        JComboBox<Enums.RolUsuario> cbxRol = new JComboBox<>();

        if (admin != null && admin.getRol() == Enums.RolUsuario.ADMINISTRADOR) cbxRol.addItem(Enums.RolUsuario.COORDINADOR);
        else cbxRol.addItem(Enums.RolUsuario.VOLUNTARIO);

        f.add(new JLabel(" Nombre (Sin números):")); f.add(txtNom);
        f.add(new JLabel(" Cédula (10 dígitos):")); f.add(txtCed);
        f.add(new JLabel(" Usuario:")); f.add(txtUser);
        f.add(new JLabel(" Clave:")); f.add(txtPass);
        f.add(new JLabel(" Rol:")); f.add(cbxRol);

        JButton btn = new JButton("Guardar");
        f.add(new JLabel()); f.add(btn);

        btn.addActionListener(e -> {
            try {
                sis.registrarUsuario(new Usuario(txtNom.getText(), txtCed.getText(), txtUser.getText(), new String(txtPass.getPassword()), (Enums.RolUsuario)cbxRol.getSelectedItem()));
                JOptionPane.showMessageDialog(f, "Registrado con éxito.");
                f.dispose();
            } catch (Exception ex) { JOptionPane.showMessageDialog(f, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE); }
        });
        f.setVisible(true);
    }

    private static void mostrarVentanaPrincipal(Usuario u) {
        JFrame f = new JFrame("Panel - " + u.getNombreCompleto() + " (" + u.getRol() + ")");
        f.setSize(950, 650); f.setLocationRelativeTo(null); f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JTabbedPane tabs = new JTabbedPane();

        if (u.getRol() == Enums.RolUsuario.ADMINISTRADOR) {
            tabs.addTab("Reportes", panelReportes());
            JPanel p = new JPanel(); JButton b = new JButton("Registrar Coordinador");
            b.addActionListener(e -> mostrarRegistro(u)); p.add(b);
            tabs.addTab("Gestión", p);
        } else if (u.getRol() == Enums.RolUsuario.COORDINADOR) {
            tabs.addTab("CRUD Eventos", panelCRUDEventos());
            tabs.addTab("Recursos/Requisitos", panelRequisitos()); // NUEVA PESTAÑA
            tabs.addTab("Fila de Espera", panelInscripciones());
            tabs.addTab("Tomar Asistencia", panelAsistencia(u));   // NUEVA PESTAÑA
        } else {
            tabs.addTab("Cartelera", panelVoluntario(u));
        }
        f.add(tabs); f.setVisible(true);
    }

    // --- PANELES PARA EL COORDINADOR ---

    private static JPanel panelRequisitos() {
        JPanel p = new JPanel(new BorderLayout());

        // Formulario Superior
        JPanel pForm = new JPanel(new GridLayout(3, 4, 5, 5));
        JTextField txtIdEv = new JTextField();
        JTextField txtNomReq = new JTextField();
        JComboBox<TipoRequisito> cbxTipo = new JComboBox<>(TipoRequisito.values());
        JSpinner spCant = new JSpinner(new SpinnerNumberModel(1, 1, 100, 1));
        JButton btnGuardar = new JButton("Añadir Requisito");

        pForm.add(new JLabel(" ID Evento:")); pForm.add(txtIdEv);
        pForm.add(new JLabel(" Recurso (Ej. Palas):")); pForm.add(txtNomReq);
        pForm.add(new JLabel(" Tipo:")); pForm.add(cbxTipo);
        pForm.add(new JLabel(" Cantidad:")); pForm.add(spCant);
        pForm.add(new JLabel()); pForm.add(new JLabel()); pForm.add(new JLabel()); pForm.add(btnGuardar);

        p.add(pForm, BorderLayout.NORTH);

        // Tabla Inferior
        DefaultTableModel mdl = new DefaultTableModel(new String[]{"Evento", "Recurso", "Tipo", "Cantidad"}, 0);
        JTable t = new JTable(mdl); p.add(new JScrollPane(t), BorderLayout.CENTER);

        btnGuardar.addActionListener(e -> {
            try {
                Evento ev = sis.buscarEventoPorId(Integer.parseInt(txtIdEv.getText()));
                if (ev == null) throw new VoluntariadoException("Evento no encontrado.");
                Requisito req = new Requisito(ev, txtNomReq.getText(), (TipoRequisito)cbxTipo.getSelectedItem(), (int)spCant.getValue());
                sis.agregarRequisito(req);
                JOptionPane.showMessageDialog(p, "Requisito guardado.");

                mdl.setRowCount(0); // Refresca tabla
                for (Requisito r : sis.getRequisitosPorEvento(ev.getId())) {
                    mdl.addRow(new Object[]{r.getEvento().getNombre(), r.getNombreRecurso(), r.getTipo(), r.getCantidadRequerida()});
                }
            } catch (Exception ex) { JOptionPane.showMessageDialog(p, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE); }
        });

        return p;
    }

    private static JPanel panelAsistencia(Usuario coordinador) {
        JPanel p = new JPanel(new BorderLayout());
        DefaultTableModel mdl = new DefaultTableModel(new String[]{"ID Insc.", "Voluntario", "Evento", "Presente"}, 0) {
            @Override public Class<?> getColumnClass(int col) { return col == 3 ? Boolean.class : String.class; }
            @Override public boolean isCellEditable(int r, int c) { return c == 3; }
        };
        JTable t = new JTable(mdl); p.add(new JScrollPane(t), BorderLayout.CENTER);

        JPanel pTop = new JPanel();
        JButton btnCargar = new JButton("Cargar Aprobados"), btnGuardar = new JButton("Registrar Asistencia en BD");
        pTop.add(btnCargar); pTop.add(btnGuardar); p.add(pTop, BorderLayout.NORTH);

        btnCargar.addActionListener(e -> {
            mdl.setRowCount(0);
            for(Inscripcion i : sis.getHistorialCompleto()) {
                if (i.getEstado() == Enums.EstadoInscripcion.APROBADA) {
                    mdl.addRow(new Object[]{i.getId(), i.getVoluntario().getNombreCompleto(), i.getEvento().getNombre(), false});
                }
            }
        });

        btnGuardar.addActionListener(e -> {
            for (int i = 0; i < t.getRowCount(); i++) {
                if ((Boolean) t.getValueAt(i, 3)) {
                    sis.registrarAsistenciaAuditable((int) t.getValueAt(i, 0), coordinador.getNombreCompleto());
                }
            }
            JOptionPane.showMessageDialog(p, "Registros de asistencia guardados con fecha y hora exacta.");
            btnCargar.doClick();
        });
        return p;
    }

    // --- DEMÁS PANELES (Reportes, CRUD Eventos, Inscripciones, Voluntario) ---
    // (Estos se mantienen exactamente iguales a la versión que te di en el mensaje anterior,
    // pero ya integrados bajo esta misma clase AppGUI).

    private static JPanel panelReportes() {
        JPanel p = new JPanel(new BorderLayout());
        DefaultTableModel mdl = new DefaultTableModel(new String[]{"ID", "Evento", "Fecha", "Cupos", "Aprobados"}, 0);
        JTable t = new JTable(mdl); p.add(new JScrollPane(t), BorderLayout.CENTER);
        JButton btn = new JButton("Generar Reporte"); p.add(btn, BorderLayout.SOUTH);
        btn.addActionListener(e -> {
            mdl.setRowCount(0);
            for (Evento ev : sis.getEventosDisponibles()) {
                long apr = sis.getHistorialCompleto().stream().filter(i -> i.getEvento().getId() == ev.getId() && i.getEstado() == Enums.EstadoInscripcion.APROBADA).count();
                mdl.addRow(new Object[]{ev.getId(), ev.getNombre(), ev.getFecha(), ev.getCuposTotales(), apr});
            }
        });
        return p;
    }

    private static JPanel panelCRUDEventos() {
        JPanel p = new JPanel(new GridLayout(8, 2, 5, 5));
        JTextField txtId = new JTextField(); txtId.setEditable(false);
        JTextField txtNom = new JTextField(), txtLug = new JTextField(), txtFec = new JTextField("2026-06-30");
        JTextField txtIni = new JTextField("08:00"), txtFin = new JTextField("12:00");
        JSpinner spCupos = new JSpinner(new SpinnerNumberModel(20, 1, 500, 1));

        p.add(new JLabel(" ID (Auto):")); p.add(txtId);
        p.add(new JLabel(" Nombre:")); p.add(txtNom);
        p.add(new JLabel(" Lugar:")); p.add(txtLug);
        p.add(new JLabel(" Fecha (YYYY-MM-DD):")); p.add(txtFec);
        p.add(new JLabel(" Horas (Inicio - Fin):")); JPanel ph = new JPanel(); ph.add(txtIni); ph.add(new JLabel("-")); ph.add(txtFin); p.add(ph);
        p.add(new JLabel(" Cupos Totales:")); p.add(spCupos);

        JButton bGuardar = new JButton("Crear"), bModificar = new JButton("Modificar ID"), bBuscar = new JButton("Buscar ID");
        JPanel pb = new JPanel(); pb.add(bGuardar); pb.add(bBuscar); pb.add(bModificar);
        p.add(new JLabel(" Acciones:")); p.add(pb);

        bGuardar.addActionListener(e -> {
            try {
                sis.crearEvento(new Evento(txtNom.getText(), txtLug.getText(), LocalDate.parse(txtFec.getText()), LocalTime.parse(txtIni.getText()), LocalTime.parse(txtFin.getText()), (int)spCupos.getValue(), "N/A", 0));
                JOptionPane.showMessageDialog(p, "Guardado.");
            } catch (Exception ex) { JOptionPane.showMessageDialog(p, ex.getMessage()); }
        });

        bBuscar.addActionListener(e -> {
            try {
                Evento ev = sis.buscarEventoPorId(Integer.parseInt(JOptionPane.showInputDialog("ID:")));
                if (ev != null) {
                    txtId.setText(String.valueOf(ev.getId())); txtNom.setText(ev.getNombre());
                    txtLug.setText(ev.getLugar()); txtFec.setText(ev.getFecha().toString());
                    txtIni.setText(ev.getHoraInicio().toString()); txtFin.setText(ev.getHoraFin().toString());
                    spCupos.setValue(ev.getCuposTotales());
                }
            } catch (Exception ex) { JOptionPane.showMessageDialog(p, "No encontrado."); }
        });

        bModificar.addActionListener(e -> {
            try {
                Evento mod = new Evento(txtNom.getText(), txtLug.getText(), LocalDate.parse(txtFec.getText()), LocalTime.parse(txtIni.getText()), LocalTime.parse(txtFin.getText()), (int)spCupos.getValue(), "N/A", 0);
                sis.modificarEvento(Integer.parseInt(txtId.getText()), mod);
                JOptionPane.showMessageDialog(p, "Modificado.");
            } catch (Exception ex) { JOptionPane.showMessageDialog(p, ex.getMessage()); }
        });
        return p;
    }

    private static JPanel panelInscripciones() {
        JPanel p = new JPanel(new BorderLayout());
        DefaultListModel<Inscripcion> mdl = new DefaultListModel<>(); JList<Inscripcion> lista = new JList<>(mdl);
        p.add(new JScrollPane(lista), BorderLayout.CENTER);
        JPanel pBtn = new JPanel(); JButton bVer = new JButton("Ver Cola"), bOk = new JButton("Aprobar"), bNo = new JButton("Rechazar"), bDes = new JButton("Deshacer");
        pBtn.add(bVer); pBtn.add(bOk); pBtn.add(bNo); pBtn.add(bDes); p.add(pBtn, BorderLayout.SOUTH);

        Runnable ref = () -> { mdl.clear(); sis.getFilaEspera().forEach(mdl::addElement); };
        bVer.addActionListener(e -> ref.run());
        bOk.addActionListener(e -> { try{ sis.procesarInscripcion(true); ref.run(); }catch(Exception ex){ JOptionPane.showMessageDialog(p, ex.getMessage()); }});
        bNo.addActionListener(e -> { try{ sis.procesarInscripcion(false); ref.run(); }catch(Exception ex){ JOptionPane.showMessageDialog(p, ex.getMessage()); }});
        bDes.addActionListener(e -> { try{ sis.deshacerAccion(); ref.run(); }catch(Exception ex){ JOptionPane.showMessageDialog(p, ex.getMessage()); }});
        return p;
    }

    private static JPanel panelVoluntario(Usuario u) {
        JPanel p = new JPanel(new BorderLayout());
        DefaultTableModel mdl = new DefaultTableModel(new String[]{"ID", "Evento", "Fecha", "Lugar", "Cupos"}, 0);
        JTable t = new JTable(mdl); p.add(new JScrollPane(t), BorderLayout.CENTER);
        JPanel pSur = new JPanel(); JButton bLoad = new JButton("Ver Eventos"), bIns = new JButton("Postular");
        pSur.add(bLoad); pSur.add(bIns); p.add(pSur, BorderLayout.SOUTH);

        bLoad.addActionListener(e -> {
            mdl.setRowCount(0);
            sis.getEventosDisponibles().forEach(ev -> mdl.addRow(new Object[]{ev.getId(), ev.getNombre(), ev.getFecha(), ev.getLugar(), ev.getCuposDisponibles()}));
        });

        bIns.addActionListener(e -> {
            int row = t.getSelectedRow();
            if (row == -1) return;
            try {
                sis.solicitarInscripcion(u, sis.buscarEventoPorId((int) t.getValueAt(row, 0)));
                JOptionPane.showMessageDialog(p, "Solicitud en cola.");
            } catch (Exception ex) { JOptionPane.showMessageDialog(p, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE); }
        });
        return p;
    }

    private static JPanel panelGestionRequisitosAdmin() {
        JPanel p = new JPanel(new BorderLayout());
        DefaultTableModel mdl = new DefaultTableModel(new String[]{"ID", "Evento", "Recurso", "Solicitado", "Estado", "Autorizado"}, 0);
        JTable t = new JTable(mdl);

        // Botones de acción
        JButton btnAprobar = new JButton("Aprobar Parcial/Total");
        btnAprobar.addActionListener(e -> {
            int row = t.getSelectedRow();
            int cant = Integer.parseInt(JOptionPane.showInputDialog("Cantidad a autorizar:"));
            sis.gestionarRequisito((int)t.getValueAt(row, 0), Enums.EstadoRequisito.APROBADO, cant);
            JOptionPane.showMessageDialog(p, "Recurso gestionado.");
        });

        p.add(new JScrollPane(t), BorderLayout.CENTER);
        p.add(btnAprobar, BorderLayout.SOUTH);
        return p;
    }
}