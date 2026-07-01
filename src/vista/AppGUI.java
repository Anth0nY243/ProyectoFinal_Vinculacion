package vista;

import modelo.*;
import excepciones.VoluntariadoException;
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

        f.add(new JLabel(" Nombre:")); f.add(txtNom);
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
        f.setSize(1000, 650); f.setLocationRelativeTo(null); f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JTabbedPane tabs = new JTabbedPane();

        if (u.getRol() == Enums.RolUsuario.ADMINISTRADOR) {
            tabs.addTab("Auditoría y Reportes", panelReportes());
            tabs.addTab("Gestión de Recursos", panelGestionRequisitosAdmin());

            JPanel p = new JPanel(); JButton b = new JButton("Registrar Coordinador");
            b.addActionListener(e -> mostrarRegistro(u)); p.add(b);
            tabs.addTab("Gestión de Usuarios", p);

        } else if (u.getRol() == Enums.RolUsuario.COORDINADOR) {
            tabs.addTab("CRUD Eventos", panelCRUDEventos());
            tabs.addTab("Solicitar Recursos", panelRequisitos());
            tabs.addTab("Fila de Espera", panelInscripciones());
            tabs.addTab("Tomar Asistencia", panelAsistencia(u));
        } else {
            tabs.addTab("Cartelera de Eventos", panelVoluntario(u));
        }
        f.add(tabs); f.setVisible(true);
    }

    private static JPanel panelReportes() {
        JPanel p = new JPanel(new BorderLayout(10, 10));
        p.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel pTop = new JPanel(new FlowLayout(FlowLayout.LEFT));
        pTop.add(new JLabel("Seleccionar Evento para Auditoría: "));
        JComboBox<Evento> cbxEventos = new JComboBox<>();
        for (Evento ev : sis.getEventosDisponibles()) cbxEventos.addItem(ev);
        pTop.add(cbxEventos);
        p.add(pTop, BorderLayout.NORTH);

        DefaultTableModel mdl = new DefaultTableModel(new String[]{"ID Inscripción", "Voluntario", "Estado Asistencia", "Auditor (Coordinador)"}, 0);
        JTable t = new JTable(mdl);
        p.add(new JScrollPane(t), BorderLayout.CENTER);

        Runnable actualizarTabla = () -> {
            mdl.setRowCount(0);
            Evento evSeleccionado = (Evento) cbxEventos.getSelectedItem();
            if (evSeleccionado == null) return;

            for (Inscripcion ins : sis.getHistorialCompleto()) {
                if (ins.getEvento().getId() == evSeleccionado.getId() &&
                        (ins.getEstado() == Enums.EstadoInscripcion.APROBADA || ins.getEstado() == Enums.EstadoInscripcion.ASISTIO)) {

                    String estadoAsistencia = (ins.getEstado() == Enums.EstadoInscripcion.ASISTIO) ? "Sí Asistió" : "Faltó";
                    String coordinador = "-";

                    if (ins.getEstado() == Enums.EstadoInscripcion.ASISTIO) {
                        for (RegistroAsistencia reg : sis.getRegistrosAsistencia()) {
                            if (reg.getInscripcionAsociada().getId() == ins.getId()) {
                                coordinador = reg.getCoordinadorResponsable();
                                break;
                            }
                        }
                    }
                    mdl.addRow(new Object[]{ins.getId(), ins.getVoluntario().getNombreCompleto(), estadoAsistencia, coordinador});
                }
            }
        };

        cbxEventos.addActionListener(e -> actualizarTabla.run());
        JButton btnRefresh = new JButton("Actualizar Reporte");
        btnRefresh.addActionListener(e -> actualizarTabla.run());
        p.add(btnRefresh, BorderLayout.SOUTH);

        actualizarTabla.run();
        return p;
    }

    private static JPanel panelGestionRequisitosAdmin() {
        JPanel p = new JPanel(new BorderLayout(10, 10));
        p.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        DefaultTableModel mdl = new DefaultTableModel(new String[]{"ID Req.", "Evento", "Recurso", "Tipo", "Solicitado", "Estado", "Autorizado"}, 0);
        JTable t = new JTable(mdl);
        p.add(new JScrollPane(t), BorderLayout.CENTER);

        JPanel pBotones = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        JButton btnAprobar = new JButton("Autorizar (Total/Parcial)");
        JButton btnRechazar = new JButton("Rechazar Solicitud");
        JButton btnRefrescar = new JButton("Actualizar Tabla");

        pBotones.add(btnRefrescar); pBotones.add(btnAprobar); pBotones.add(btnRechazar);
        p.add(pBotones, BorderLayout.SOUTH);

        Runnable cargarDatos = () -> {
            mdl.setRowCount(0);
            for (Requisito r : sis.getCatalogoRequisitos()) {
                mdl.addRow(new Object[]{r.getId(), r.getEvento().getNombre(), r.getNombreRecurso(), r.getTipo(), r.getCantidadRequerida(), r.getEstado(), r.getCantidadAprobada()});
            }
        };

        btnRefrescar.addActionListener(e -> cargarDatos.run());

        btnAprobar.addActionListener(e -> {
            int row = t.getSelectedRow();
            if (row == -1) return;
            try {
                int idReq = (int) mdl.getValueAt(row, 0);
                int cantSolicitada = (int) mdl.getValueAt(row, 4);
                String input = JOptionPane.showInputDialog(p, "Solicitado: " + cantSolicitada + "\nCantidad a AUTORIZAR:");
                if (input != null && !input.trim().isEmpty()) {
                    sis.gestionarRequisito(idReq, Enums.EstadoRequisito.APROBADO, Integer.parseInt(input));
                    JOptionPane.showMessageDialog(p, "Autorizado.");
                    cargarDatos.run();
                }
            } catch (Exception ex) { JOptionPane.showMessageDialog(p, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE); }
        });

        btnRechazar.addActionListener(e -> {
            int row = t.getSelectedRow();
            if (row == -1) return;
            try {
                sis.gestionarRequisito((int) mdl.getValueAt(row, 0), Enums.EstadoRequisito.RECHAZADO, 0);
                JOptionPane.showMessageDialog(p, "Rechazado.");
                cargarDatos.run();
            } catch (Exception ex) { JOptionPane.showMessageDialog(p, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE); }
        });

        cargarDatos.run();
        return p;
    }

    private static JPanel panelCRUDEventos() {
        JPanel p = new JPanel(new GridLayout(8, 2, 5, 5));
        JTextField txtId = new JTextField(); txtId.setEditable(false);
        JTextField txtNom = new JTextField(), txtLug = new JTextField(), txtFec = new JTextField(LocalDate.now().plusDays(1).toString());
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

    private static JPanel panelRequisitos() {
        JPanel p = new JPanel(new BorderLayout());
        JPanel pForm = new JPanel(new GridLayout(3, 4, 5, 5));
        JTextField txtIdEv = new JTextField(), txtNomReq = new JTextField();
        JComboBox<Enums.TipoRequisito> cbxTipo = new JComboBox<>(Enums.TipoRequisito.values());
        JSpinner spCant = new JSpinner(new SpinnerNumberModel(1, 1, 100, 1));
        JButton btnGuardar = new JButton("Añadir Requisito");

        pForm.add(new JLabel(" ID Evento:")); pForm.add(txtIdEv);
        pForm.add(new JLabel(" Recurso (Ej. Palas):")); pForm.add(txtNomReq);
        pForm.add(new JLabel(" Tipo:")); pForm.add(cbxTipo);
        pForm.add(new JLabel(" Cantidad:")); pForm.add(spCant);
        pForm.add(new JLabel()); pForm.add(new JLabel()); pForm.add(new JLabel()); pForm.add(btnGuardar);

        p.add(pForm, BorderLayout.NORTH);

        DefaultTableModel mdl = new DefaultTableModel(new String[]{"Evento", "Recurso", "Tipo", "Cantidad", "Estado"}, 0);
        JTable t = new JTable(mdl); p.add(new JScrollPane(t), BorderLayout.CENTER);

        btnGuardar.addActionListener(e -> {
            try {
                Evento ev = sis.buscarEventoPorId(Integer.parseInt(txtIdEv.getText()));
                if (ev == null) throw new VoluntariadoException("Evento no encontrado.");
                sis.agregarRequisito(new Requisito(ev, txtNomReq.getText(), (Enums.TipoRequisito)cbxTipo.getSelectedItem(), (int)spCant.getValue()));
                JOptionPane.showMessageDialog(p, "Requisito guardado.");

                mdl.setRowCount(0);
                for (Requisito r : sis.getRequisitosPorEvento(ev.getId())) {
                    mdl.addRow(new Object[]{r.getEvento().getNombre(), r.getNombreRecurso(), r.getTipo(), r.getCantidadRequerida(), r.getEstado()});
                }
            } catch (Exception ex) { JOptionPane.showMessageDialog(p, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE); }
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
            JOptionPane.showMessageDialog(p, "Registros de asistencia guardados.");
            btnCargar.doClick();
        });
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
}