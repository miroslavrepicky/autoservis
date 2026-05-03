package com.autoservice.ui.dispatcher;

import com.autoservice.AppContext;
import com.autoservice.domain.*;
import com.autoservice.employee.Dispatcher;
import com.autoservice.employee.Employee;
import com.autoservice.employee.Mechanic;
import com.autoservice.service.OrderService;
import com.autoservice.service.ProfileManager;
import com.autoservice.ui.shared.NotificationPanel;
import com.autoservice.ui.shared.UIUtils;
import com.autoservice.ui.LoginWindow;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Dispatcher window – assign orders to mechanics, overview of all orders.
 * Fix: zákazky v stave REZERVOVANA sa nedajú priradiť mechanikovi.
 * Dispečer môže priradiť len zákazky v stave DIAGNOSTIKA alebo CAKA_NA_PRIRADENIE.
 */
public class DispatcherWindow extends JFrame {

    private final Dispatcher employee;
    private final AppContext ctx = AppContext.getInstance();
    private final OrderService orderService     = ctx.getOrderService();
    private final ProfileManager profileManager = ctx.getProfileManager();

    private JTable allOrdersTable;
    private JTable mechanicsTable;
    private final NotificationPanel notifPanel = new NotificationPanel("Dispečer");

    public DispatcherWindow(Dispatcher employee) {
        super("AutoServis – " + employee.getRole() + ": " + employee.getFullName());
        this.employee = employee;
        ctx.getNotificationManager().addInAppListener(notifPanel::addNotification);
        buildUI();
        setSize(1000, 700);
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
        tabs.addTab("📋 Všetky zákazky",    buildAllOrdersTab());
        tabs.addTab("🔧 Priradenie zákazky", buildAssignTab());
        tabs.addTab("👷 Mechanici",          buildMechanicsTab());
        tabs.addTab("🔔 Notifikácie",        notifPanel);
        tabs.addChangeListener(e -> {
            if (tabs.getSelectedIndex() == 0) refreshAllOrders();
            if (tabs.getSelectedIndex() == 2) refreshMechanics();
        });
        add(tabs, BorderLayout.CENTER);
    }

    // =========================================================
    // TAB 1 – All orders
    // =========================================================
    private JPanel buildAllOrdersTab() {
        JPanel p = new JPanel(new BorderLayout(8, 8));
        p.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        allOrdersTable = UIUtils.buildTable(new String[]{"ID", "Zákazník", "Popis", "Stav", "Mechanik", "Termín"});
        refreshAllOrders();

        JButton refreshBtn = UIUtils.primaryButton("🔄 Obnoviť");
        refreshBtn.addActionListener(e -> refreshAllOrders());

        p.add(UIUtils.sectionLabel("Prehľad všetkých zákaziek"), BorderLayout.NORTH);
        p.add(new JScrollPane(allOrdersTable), BorderLayout.CENTER);
        JPanel south = new JPanel(new FlowLayout(FlowLayout.LEFT));
        south.add(refreshBtn);
        p.add(south, BorderLayout.SOUTH);
        return p;
    }

    private void refreshAllOrders() {
        DefaultTableModel m = (DefaultTableModel) allOrdersTable.getModel();
        m.setRowCount(0);
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
        for (Order o : orderService.getAllOrders()) {
            String custName = "";
            var c = profileManager.findCustomerById(o.getCustomerId());
            if (c != null) custName = c.getFullName();
            String mechName = o.getMechanicId() != null ? resolveMechName(o.getMechanicId()) : "Nepriradená";
            m.addRow(new Object[]{
                    o.getOrderId().substring(0, 8),
                    custName,
                    o.getDescription(),
                    o.getStatus().getDisplayName(),
                    mechName,
                    o.getAppointmentTime() != null ? o.getAppointmentTime().format(fmt) : "-"
            });
        }
    }

    private String resolveMechName(String mechId) {
        for (Employee e : ctx.getEmployees().values()) {
            if (e instanceof Mechanic && String.valueOf(e.getEmployeeId()).equals(mechId)) {
                return e.getFullName();
            }
        }
        return mechId;
    }

    // =========================================================
    // TAB 2 – Assign order
    // Oprava č.1: zákazku možno priradiť len ak prešla príjmom
    // (stav DIAGNOSTIKA alebo CAKA_NA_PRIRADENIE) – nie REZERVOVANA
    // =========================================================
    private JPanel buildAssignTab() {
        JPanel p = new JPanel(new BorderLayout(8, 8));
        p.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        JTable unassignedTable = UIUtils.buildTable(new String[]{"ID", "Zákazník", "Popis", "Stav", "Termín"});
        JComboBox<String> mechanicBox = new JComboBox<>();

        JLabel infoLabel = UIUtils.mutedLabel(
                "ℹ️  Zobrazujú sa len zákazky po príjme vozidla (Diagnostika / Čaká na priradenie).");
        infoLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));

        Runnable refresh = () -> {
            DefaultTableModel m = (DefaultTableModel) unassignedTable.getModel();
            m.setRowCount(0);
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
            for (Order o : orderService.getAllOrders()) {
                // Oprava: preskočiť REZERVOVANA a už priradené zákazky
                boolean assignable = (o.getStatus() == OrderStatus.DIAGNOSTIKA
                        || o.getStatus() == OrderStatus.CAKA_NA_PRIRADENIE)
                        && (o.getMechanicId() == null || o.getMechanicId().isEmpty());
                if (!assignable) continue;

                var c = profileManager.findCustomerById(o.getCustomerId());
                String custName = c != null ? c.getFullName() : "-";
                m.addRow(new Object[]{
                        o.getOrderId().substring(0, 8), custName,
                        o.getDescription(), o.getStatus().getDisplayName(),
                        o.getAppointmentTime() != null ? o.getAppointmentTime().format(fmt) : "-"
                });
            }
            mechanicBox.removeAllItems();
            for (Employee e : ctx.getEmployees().values()) {
                if (e instanceof Mechanic mech) {
                    int load = orderService.getWorkloadOf(mech.getEmployeeId());
                    mechanicBox.addItem(mech.getFullName() + " [" + mech.getEmployeeId() + "]"
                            + "  –  zákazky: " + load);
                }
            }
        };
        refresh.run();

        JButton assignBtn  = UIUtils.primaryButton("Priradiť zákazku");
        JButton refreshBtn = UIUtils.primaryButton("🔄 Obnoviť");
        refreshBtn.addActionListener(e -> refresh.run());

        assignBtn.addActionListener(e -> {
            int selRow = unassignedTable.getSelectedRow();
            if (selRow < 0) { UIUtils.showError(this, "Vyberte zákazku."); return; }
            if (mechanicBox.getSelectedIndex() < 0) { UIUtils.showError(this, "Vyberte mechanika."); return; }
            String shortId = (String) unassignedTable.getValueAt(selRow, 0);

            String mechEntry = (String) mechanicBox.getSelectedItem();
            // parse mechanicId from "[id]" token
            int mechId = Integer.parseInt(mechEntry.replaceAll(".*\\[(\\d+)\\].*", "$1"));

            Order target = orderService.getAllOrders().stream()
                    .filter(o -> o.getOrderId().startsWith(shortId))
                    .findFirst().orElse(null);
            if (target == null) return;

            orderService.assignOrder(target.getOrderId(), mechId, target.getVehicleId());
            ctx.getNotificationManager().notifyMechanic(String.valueOf(mechId),
                    "Bola vám priradená nová zákazka #" + shortId + ": " + target.getDescription());
            UIUtils.showInfo(this, "Zákazka priradená mechanikovi.");
            refresh.run();
            refreshAllOrders();
        });

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.add(new JLabel("Mechanik:"));
        top.add(mechanicBox);
        top.add(assignBtn);
        top.add(refreshBtn);

        p.add(UIUtils.sectionLabel("Priradenie zákaziek"), BorderLayout.NORTH);
        p.add(infoLabel, BorderLayout.NORTH); // note: last NORTH wins in BorderLayout; use wrapper
        JPanel north = new JPanel(new BorderLayout(0, 4));
        north.add(UIUtils.sectionLabel("Priradenie zákaziek"), BorderLayout.NORTH);
        north.add(infoLabel, BorderLayout.SOUTH);
        p.add(north, BorderLayout.NORTH);
        p.add(new JScrollPane(unassignedTable), BorderLayout.CENTER);
        p.add(top, BorderLayout.SOUTH);
        return p;
    }

    // =========================================================
    // TAB 3 – Mechanics overview
    // =========================================================
    private JPanel buildMechanicsTab() {
        JPanel p = new JPanel(new BorderLayout(8, 8));
        p.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        mechanicsTable = UIUtils.buildTable(new String[]{"ID", "Meno", "Špecializácia", "Aktívne zákazky"});
        refreshMechanics();

        JButton refreshBtn = UIUtils.primaryButton("🔄 Obnoviť");
        refreshBtn.addActionListener(e -> refreshMechanics());

        p.add(UIUtils.sectionLabel("Prehľad mechanikov"), BorderLayout.NORTH);
        p.add(new JScrollPane(mechanicsTable), BorderLayout.CENTER);
        JPanel south = new JPanel(new FlowLayout(FlowLayout.LEFT));
        south.add(refreshBtn);
        p.add(south, BorderLayout.SOUTH);
        return p;
    }

    private void refreshMechanics() {
        DefaultTableModel m = (DefaultTableModel) mechanicsTable.getModel();
        m.setRowCount(0);
        for (Employee e : ctx.getEmployees().values()) {
            if (e instanceof Mechanic mech) {
                int activeOrders = orderService.getOrdersForMechanic(
                        String.valueOf(mech.getEmployeeId())).size();
                m.addRow(new Object[]{mech.getEmployeeId(), mech.getFullName(),
                        mech.getSpecialization(), activeOrders});
            }
        }
    }
}