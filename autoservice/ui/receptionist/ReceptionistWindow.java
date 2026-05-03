package com.autoservice.ui.receptionist;

import com.autoservice.AppContext;
import com.autoservice.domain.*;
import com.autoservice.employee.ReceptionTechnician;
import com.autoservice.service.*;
import com.autoservice.ui.shared.NotificationPanel;
import com.autoservice.ui.shared.UIUtils;
import com.autoservice.ui.LoginWindow;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Window for Prijímací technik (Reception Technician).
 * Tabs: Rezervácie | Príjem vozidla | Dočasný profil | Notifikácie
 */
public class ReceptionistWindow extends JFrame {

    private final ReceptionTechnician employee;
    private final AppContext ctx = AppContext.getInstance();
    private final OrderService orderService     = ctx.getOrderService();
    private final ProfileManager profileManager = ctx.getProfileManager();
    private final VehicleManager vehicleManager = ctx.getVehicleManager();

    private JTable reservationTable;
    private JTable activeOrderTable;
    private final NotificationPanel notifPanel = new NotificationPanel("Prijímací technik");

    public ReceptionistWindow(ReceptionTechnician employee) {
        super("AutoServis – " + employee.getRole() + ": " + employee.getFullName());
        this.employee = employee;
        ctx.getNotificationManager().addInAppListener(notifPanel::addNotification);
        buildUI();
        setSize(980, 680);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setVisible(true);
    }

    private void buildUI() {
        setLayout(new BorderLayout());
        JPanel nav = UIUtils.buildNavBar("AutoProService",
                employee.getFullName() + "  |  " + employee.getRole(),
                () -> { if (UIUtils.confirm(this, "Naozaj sa chcete odhlásiť?")) { dispose(); new LoginWindow(); } });
        add(nav, BorderLayout.NORTH);
        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("📋 Rezervácie",        buildReservationsTab());
        tabs.addTab("🚗 Príjem vozidla",    buildReceiveVehicleTab());
        tabs.addTab("👤 Dočasný profil",    buildTempProfileTab());
        tabs.addTab("🔔 Notifikácie",       notifPanel);
        tabs.addChangeListener(e -> {
            if (tabs.getSelectedIndex() == 0) refreshReservations();
            if (tabs.getSelectedIndex() == 1) refreshActiveOrders();
        });
        add(tabs, BorderLayout.CENTER);
    }

    // =========================================================
    // TAB 1 – Reservations
    // =========================================================
    private JPanel buildReservationsTab() {
        JPanel p = new JPanel(new BorderLayout(8, 8));
        p.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        reservationTable = UIUtils.buildTable(new String[]{"ID", "Zákazník", "Vozidlo", "Popis", "Termín", "Stav"});
        refreshReservations();

        JButton refreshBtn = UIUtils.primaryButton("🔄 Obnoviť");
        refreshBtn.addActionListener(e -> refreshReservations());

        p.add(UIUtils.sectionLabel("Prijaté rezervácie"), BorderLayout.NORTH);
        p.add(new JScrollPane(reservationTable), BorderLayout.CENTER);
        JPanel south = new JPanel(new FlowLayout(FlowLayout.LEFT));
        south.add(refreshBtn);
        p.add(south, BorderLayout.SOUTH);
        return p;
    }

    private void refreshReservations() {
        DefaultTableModel m = (DefaultTableModel) reservationTable.getModel();
        m.setRowCount(0);
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
        for (Order o : orderService.getAllOrders()) {
            String custName = "";
            Customer c = profileManager.findCustomerById(o.getCustomerId());
            if (c != null) custName = c.getFullName();
            m.addRow(new Object[]{
                    o.getOrderId().substring(0, 8),
                    custName,
                    o.getVehicleId() != null ? o.getVehicleId().substring(0, 8) : "-",
                    o.getDescription(),
                    o.getAppointmentTime() != null ? o.getAppointmentTime().format(fmt) : "-",
                    o.getStatus().getDisplayName()
            });
        }
    }

    // =========================================================
    // TAB 2 – Receive vehicle (UC05)
    // =========================================================
    private JPanel buildReceiveVehicleTab() {
        JPanel p = new JPanel(new BorderLayout(8, 8));
        p.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        activeOrderTable = UIUtils.buildTable(new String[]{"ID", "Zákazník", "Popis", "Termín", "Stav"});
        refreshActiveOrders();

        JButton receiveBtn  = UIUtils.primaryButton("📥 Prijať vozidlo");
        JButton handoverBtn = UIUtils.successButton("📤 Odovzdať vozidlo zákazníkovi");
        JButton refreshBtn  = UIUtils.primaryButton("🔄 Obnoviť");

        receiveBtn.addActionListener(e -> doReceiveVehicle());
        handoverBtn.addActionListener(e -> doHandoverVehicle());
        refreshBtn.addActionListener(e -> refreshActiveOrders());

        p.add(UIUtils.sectionLabel("Príjem a odovzdanie vozidla"), BorderLayout.NORTH);
        p.add(new JScrollPane(activeOrderTable), BorderLayout.CENTER);

        JPanel south = new JPanel(new FlowLayout(FlowLayout.LEFT));
        south.add(receiveBtn); south.add(handoverBtn); south.add(refreshBtn);
        p.add(south, BorderLayout.SOUTH);
        return p;
    }

    private void refreshActiveOrders() {
        DefaultTableModel m = (DefaultTableModel) activeOrderTable.getModel();
        m.setRowCount(0);
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
        for (Order o : orderService.getAllOrders()) {
            Customer c = profileManager.findCustomerById(o.getCustomerId());
            String custName = c != null ? c.getFullName() : o.getCustomerId().substring(0, 8);
            m.addRow(new Object[]{
                    o.getOrderId().substring(0, 8),
                    custName,
                    o.getDescription(),
                    o.getAppointmentTime() != null ? o.getAppointmentTime().format(fmt) : "-",
                    o.getStatus().getDisplayName()
            });
        }
    }

    private void doReceiveVehicle() {
        int row = activeOrderTable.getSelectedRow();
        if (row < 0) { UIUtils.showError(this, "Vyberte zákazku."); return; }
        String shortId = (String) activeOrderTable.getValueAt(row, 0);
        Order target = orderService.getAllOrders().stream()
                .filter(o -> o.getOrderId().startsWith(shortId))
                .findFirst().orElse(null);
        if (target == null) return;
        orderService.receiveVehicle(target.getOrderId(), List.of());
        UIUtils.showInfo(this, "Vozidlo prijaté do servisu. Zákazka prešla do stavu: Diagnostika.");
        refreshActiveOrders();
        notifPanel.addNotification("Vozidlo prijaté – zákazka #" + shortId);
    }

    private void doHandoverVehicle() {
        int row = activeOrderTable.getSelectedRow();
        if (row < 0) { UIUtils.showError(this, "Vyberte zákazku."); return; }
        String shortId = (String) activeOrderTable.getValueAt(row, 0);
        Order target = orderService.getAllOrders().stream()
                .filter(o -> o.getOrderId().startsWith(shortId))
                .findFirst().orElse(null);
        if (target == null) return;
        if (target.getStatus() != OrderStatus.UZAVRETA) {
            UIUtils.showError(this, "Zákazka musí byť uzavretá pred odovzdaním vozidla.");
            return;
        }
        UIUtils.showInfo(this, "Vozidlo odovzdané zákazníkovi. Zákazka: #" + shortId);
        refreshActiveOrders();
    }

    // =========================================================
    // TAB 3 – Temporary profile (UC04)
    // =========================================================
    private JPanel buildTempProfileTab() {
        JPanel p = new JPanel(new BorderLayout(8, 8));
        p.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        JTextField firstF  = new JTextField(14);
        JTextField lastF   = new JTextField(14);
        JTextField phoneF  = new JTextField(14);
        JTextField emailF  = new JTextField(14);
        JTextField vinF    = new JTextField(17);
        JTextField makeF   = new JTextField(10);
        JTextField modelF  = new JTextField(10);
        JTextField yearF   = new JTextField(4);
        JTextField plateF  = new JTextField(8);
        JTextField engineF = new JTextField(10);

        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(4, 6, 4, 6);
        gc.fill = GridBagConstraints.HORIZONTAL;

        int row = 0;
        addRow(form, gc, row++, "Meno zákazníka:", firstF);
        addRow(form, gc, row++, "Priezvisko:", lastF);
        addRow(form, gc, row++, "Telefón:", phoneF);
        addRow(form, gc, row++, "Email:", emailF);
        addRow(form, gc, row++, "VIN vozidla:", vinF);
        addRow(form, gc, row++, "Značka:", makeF);
        addRow(form, gc, row++, "Model:", modelF);
        addRow(form, gc, row++, "Rok výroby:", yearF);
        addRow(form, gc, row++, "ŠPZ:", plateF);
        addRow(form, gc, row++, "Motor:", engineF);

        JButton createBtn = UIUtils.primaryButton("Vytvoriť dočasný profil a zákazku");
        createBtn.addActionListener(e -> {
            String first  = firstF.getText().trim();
            String last   = lastF.getText().trim();
            String phone  = phoneF.getText().trim();
            String email  = emailF.getText().trim();
            if (first.isEmpty() || last.isEmpty() || phone.isEmpty()) {
                UIUtils.showError(this, "Vyplňte aspoň meno, priezvisko a telefón.");
                return;
            }

            TempProfile tp = profileManager.createTempProfile(first, last, phone, email);

            // add vehicle if VIN provided
            String vin = vinF.getText().trim().toUpperCase();
            String plate = plateF.getText().trim().toUpperCase();
            Vehicle vehicle = null;
            if (!vin.isEmpty()) {
                if (!vehicleManager.validateVIN(vin)) {
                    UIUtils.showError(this, "Neplatný VIN."); return;
                }
                vehicle = vehicleManager.createVehicleCard(
                        makeF.getText().trim(), modelF.getText().trim(), vin,
                        yearF.getText().trim(), engineF.getText().trim(), plate);
                tp.addVehicle(vehicle);
            }

            // create order for this temp profile with a temp customer stub
            Customer tempCust = new Customer(first, last, email.isEmpty() ? phone + "@temp.local" : email, phone, "");
            tempCust.setTemporary(true);
            profileManager.saveProfile(tempCust);
            if (vehicle != null) vehicleManager.createAndRegisterCard(tempCust, vehicle);

            Order order = orderService.createReservation(tempCust.getCustomerId(),
                    vehicle != null ? vehicle.getVehicleId() : "—",
                    "Príjem vozidla – dočasný profil", LocalDateTime.now(), "");

            UIUtils.showInfo(this, "Dočasný profil vytvorený.\nID zákazky: " + order.getOrderId().substring(0, 8));
            firstF.setText(""); lastF.setText(""); phoneF.setText(""); emailF.setText("");
            vinF.setText(""); makeF.setText(""); modelF.setText(""); yearF.setText(""); plateF.setText(""); engineF.setText("");
            refreshReservations();
        });

        p.add(UIUtils.sectionLabel("Vytvorenie dočasného profilu (UC04)"), BorderLayout.NORTH);
        p.add(new JScrollPane(form), BorderLayout.CENTER);
        JPanel south = new JPanel(new FlowLayout(FlowLayout.LEFT));
        south.add(createBtn);
        p.add(south, BorderLayout.SOUTH);
        return p;
    }

    private void addRow(JPanel p, GridBagConstraints gc, int row, String label, JComponent field) {
        gc.gridx = 0; gc.gridy = row; p.add(new JLabel(label), gc);
        gc.gridx = 1;                p.add(field, gc);
    }
}