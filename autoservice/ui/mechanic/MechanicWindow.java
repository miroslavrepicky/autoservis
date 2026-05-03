package com.autoservice.ui.mechanic;

import com.autoservice.AppContext;
import com.autoservice.domain.*;
import com.autoservice.employee.Mechanic;
import com.autoservice.service.Inventory;
import com.autoservice.service.OrderService;
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
 * Mechanic window.
 * Opravy:
 *  - Oprava č.4: Mechanik zadá výsledok diagnostiky do textového poľa
 *  - Oprava č.5: Po dokončení opravy sa vykonané práce zapíšu do servisnej knižky
 */
public class MechanicWindow extends JFrame {

    private final Mechanic employee;
    private final AppContext ctx = AppContext.getInstance();
    private final OrderService orderService = ctx.getOrderService();
    private final Inventory inventory       = ctx.getInventory();

    private JTable assignedTable;
    private JTable workItemTable;
    private JTable sparePartTable;

    private final NotificationPanel notifPanel = new NotificationPanel("Mechanik");

    public MechanicWindow(Mechanic employee) {
        super("AutoServis – " + employee.getRole() + ": " + employee.getFullName());
        this.employee = employee;
        ctx.getNotificationManager().addInAppListener(msg -> {
            if (msg.contains("MECHANIK " + employee.getEmployeeId()) || msg.contains("MECHANIK")) {
                notifPanel.addNotification(msg);
            }
        });
        buildUI();
        setSize(1020, 720);
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
        tabs.addTab("🔧 Moje zákazky",        buildAssignedTab());
        tabs.addTab("🛠 Práce a diely",         buildWorkItemsTab());
        tabs.addTab("⚠️ Dodatočné závady",     buildFaultsTab());
        tabs.addTab("🔔 Notifikácie",           notifPanel);
        tabs.addChangeListener(e -> {
            if (tabs.getSelectedIndex() == 0) refreshAssigned();
        });
        add(tabs, BorderLayout.CENTER);
    }

    // =========================================================
    // TAB 1 – Assigned orders
    // Oprava č.4: pole na výsledok diagnostiky
    // Oprava č.5: zápis do servisnej knižky pri dokončení
    // =========================================================
    private JPanel buildAssignedTab() {
        JPanel p = new JPanel(new BorderLayout(8, 8));
        p.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        assignedTable = UIUtils.buildTable(
                new String[]{"ID", "Popis", "Stav", "Termín", "Cena (€)"});
        refreshAssigned();

        // ── Detail zákazky ────────────────────────────────────
        JTextArea detailArea = new JTextArea(4, 40);
        detailArea.setEditable(false);
        detailArea.setBorder(BorderFactory.createTitledBorder("Detail zákazky"));

        // ── Výsledok diagnostiky (oprava č.4) ────────────────
        JTextArea diagResultArea = new JTextArea(3, 40);
        diagResultArea.setLineWrap(true);
        diagResultArea.setWrapStyleWord(true);
        diagResultArea.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIUtils.BORDER_COLOR),
                BorderFactory.createEmptyBorder(4, 6, 4, 6)));
        JPanel diagPanel = new JPanel(new BorderLayout(4, 0));
        diagPanel.setBorder(BorderFactory.createTitledBorder("🔍 Výsledok diagnostiky"));
        diagPanel.add(new JScrollPane(diagResultArea), BorderLayout.CENTER);

        JButton saveDiagBtn = UIUtils.primaryButton("Uložiť výsledok diagnostiky");
        saveDiagBtn.addActionListener(e -> {
            Order o = getSelectedOrder();
            if (o == null) { UIUtils.showError(this, "Vyberte zákazku."); return; }
            if (o.getStatus() != OrderStatus.DIAGNOSTIKA) {
                UIUtils.showError(this, "Zákazka musí byť v stave Diagnostika."); return;
            }
            String result = diagResultArea.getText().trim();
            if (result.isEmpty()) { UIUtils.showError(this, "Zadajte výsledok diagnostiky."); return; }

            // Uloží výsledok diagnostiky do poznámok zákazky
            String existing = o.getNotes() != null ? o.getNotes() + "\n" : "";
            o.setNotes(existing + "[Diagnostika] " + result);
            ctx.getOrderRepository().save(o);

            UIUtils.showInfo(this, "Výsledok diagnostiky uložený.");
            diagResultArea.setText("");
            updateDetail(detailArea);
        });
        diagPanel.add(saveDiagBtn, BorderLayout.EAST);

        // ── Tlačidlá stavov ───────────────────────────────────
        JButton startBtn    = UIUtils.primaryButton("▶ Spustiť diagnostiku");
        JButton repairBtn   = UIUtils.primaryButton("🔧 Začať opravu");
        JButton completeBtn = UIUtils.successButton(" Dokončiť opravu");
        JButton refreshBtn  = UIUtils.primaryButton("🔄 Obnoviť");

        startBtn.addActionListener(e -> changeStatus(OrderStatus.DIAGNOSTIKA));
        repairBtn.addActionListener(e -> changeStatus(OrderStatus.OPRAVA));
        completeBtn.addActionListener(e -> doComplete());
        refreshBtn.addActionListener(e -> refreshAssigned());

        assignedTable.getSelectionModel().addListSelectionListener(ev -> {
            if (!ev.getValueIsAdjusting()) updateDetail(detailArea);
        });

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttons.add(startBtn); buttons.add(repairBtn);
        buttons.add(completeBtn); buttons.add(refreshBtn);

        JPanel south = new JPanel(new BorderLayout(0, 4));
        south.add(new JScrollPane(detailArea), BorderLayout.NORTH);
        south.add(diagPanel, BorderLayout.CENTER);
        south.add(buttons, BorderLayout.SOUTH);

        p.add(UIUtils.sectionLabel("Priradené zákazky – " + employee.getFullName()), BorderLayout.NORTH);
        p.add(new JScrollPane(assignedTable), BorderLayout.CENTER);
        p.add(south, BorderLayout.SOUTH);
        return p;
    }

    private void refreshAssigned() {
        DefaultTableModel m = (DefaultTableModel) assignedTable.getModel();
        m.setRowCount(0);
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
        List<Order> orders = orderService.getOrdersForMechanic(String.valueOf(employee.getEmployeeId()));
        for (Order o : orders) {
            m.addRow(new Object[]{
                    o.getOrderId().substring(0, 8),
                    o.getDescription(),
                    o.getStatus().getDisplayName(),
                    o.getAppointmentTime() != null ? o.getAppointmentTime().format(fmt) : "-",
                    String.format("%.2f", o.getTotalCost())
            });
        }
    }

    private void updateDetail(JTextArea area) {
        Order o = getSelectedOrder();
        if (o == null) { area.setText(""); return; }
        StringBuilder sb = new StringBuilder();
        sb.append("ID: ").append(o.getOrderId()).append("\n");
        sb.append("Stav: ").append(o.getStatus().getDisplayName()).append("\n");
        sb.append("Popis: ").append(o.getDescription()).append("\n");
        sb.append("Poznámky: ").append(o.getNotes() != null ? o.getNotes() : "-").append("\n");
        sb.append("Pracovné položky: ").append(o.getWorkItems().size()).append("\n");
        sb.append("Diely: ").append(o.getSpareParts().size()).append("\n");
        sb.append("Celková cena: ").append(String.format("%.2f €", o.getTotalCost())).append("\n");
        area.setText(sb.toString());
    }

    private void changeStatus(OrderStatus status) {
        Order o = getSelectedOrder();
        if (o == null) { UIUtils.showError(this, "Vyberte zákazku."); return; }
        orderService.setStatus(o.getOrderId(), status);
        UIUtils.showInfo(this, "Stav zmenený na: " + status.getDisplayName());
        refreshAssigned();
    }

    /**
     * Dokončenie opravy – oprava č.5:
     * Vykonané práce sa zapíšu do servisnej knižky vozidla.
     */
    private void doComplete() {
        Order o = getSelectedOrder();
        if (o == null) { UIUtils.showError(this, "Vyberte zákazku."); return; }

        // Zostaviť záznam pre servisnú knižku
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
        StringBuilder record = new StringBuilder();
        record.append("Dátum: ").append(LocalDateTime.now().format(fmt)).append("\n");
        record.append("Mechanik: ").append(employee.getFullName()).append("\n");
        record.append("Zákazka: #").append(o.getOrderId().substring(0, 8)).append("\n");
        record.append("Popis: ").append(o.getDescription()).append("\n");

        if (o.getNotes() != null && !o.getNotes().isBlank()) {
            record.append("Poznámky/Diagnostika: ").append(o.getNotes()).append("\n");
        }

        if (!o.getWorkItems().isEmpty()) {
            record.append("Vykonané práce:\n");
            for (WorkItem wi : o.getWorkItems()) {
                record.append("  • ").append(wi.getDescription())
                        .append(" (").append(String.format("%.2f €", wi.getLaborCost())).append(")\n");
            }
        }
        if (!o.getSpareParts().isEmpty()) {
            record.append("Použité diely:\n");
            for (SparePart sp : o.getSpareParts()) {
                record.append("  • ").append(sp.getName())
                        .append(" (qty: ").append(sp.getQuantity())
                        .append(", ").append(String.format("%.2f €", sp.getUnitPrice())).append("/ks)\n");
            }
        }
        record.append("Celková cena: ").append(String.format("%.2f €", o.getTotalCost()));

        // Zapísať do servisnej knižky
        if (o.getVehicleId() != null) {
            VehicleCard card = ctx.getVehicleManager().getVehicleCard(o.getVehicleId());
            if (card != null) {
                card.addServiceRecord(record.toString());
            }
        }

        // Uzavrieť zákazku
        orderService.completeRepair(o.getOrderId());
        UIUtils.showInfo(this, "Oprava dokončená. Zákazka uzavretá.\nZáznam bol pridaný do servisnej knižky vozidla.");
        refreshAssigned();
    }

    private Order getSelectedOrder() {
        int row = assignedTable.getSelectedRow();
        if (row < 0) return null;
        String shortId = (String) assignedTable.getValueAt(row, 0);
        return orderService.getOrdersForMechanic(String.valueOf(employee.getEmployeeId()))
                .stream().filter(o -> o.getOrderId().startsWith(shortId)).findFirst().orElse(null);
    }

    // =========================================================
    // TAB 2 – Work items & spare parts
    // =========================================================
    private JPanel buildWorkItemsTab() {
        JPanel p = new JPanel(new BorderLayout(8, 8));
        p.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        JComboBox<String> orderBox = new JComboBox<>();
        Runnable fillOrders = () -> {
            orderBox.removeAllItems();
            for (Order o : orderService.getOrdersForMechanic(String.valueOf(employee.getEmployeeId()))) {
                orderBox.addItem(o.getOrderId().substring(0, 8) + " – " + o.getDescription());
            }
        };
        fillOrders.run();

        workItemTable  = UIUtils.buildTable(new String[]{"Popis práce", "Cena práce (€)"});
        sparePartTable = UIUtils.buildTable(new String[]{"Diel", "Qty", "Cena/ks (€)"});

        JTextField wiDescF = new JTextField(18);
        JTextField wiCostF = new JTextField(6);
        JButton addWiBtn   = UIUtils.primaryButton("+ Pridať prácu");
        addWiBtn.addActionListener(e -> {
            if (orderBox.getSelectedIndex() < 0) return;
            String desc = wiDescF.getText().trim();
            String costStr = wiCostF.getText().trim();
            if (desc.isEmpty() || costStr.isEmpty()) { UIUtils.showError(this, "Vyplňte popis a cenu."); return; }
            double cost;
            try { cost = Double.parseDouble(costStr); } catch (NumberFormatException ex) {
                UIUtils.showError(this, "Neplatná cena."); return;
            }
            Order o = resolveSelectedOrder(orderBox);
            if (o == null) return;
            WorkItem wi = new WorkItem(java.util.UUID.randomUUID().toString().substring(0, 8), desc, cost);
            o.addWorkItem(wi);
            o.calculateCost(o.getWorkItems(), o.getSpareParts());
            ctx.getOrderRepository().save(o);
            refreshWorkTables(o);
            wiDescF.setText(""); wiCostF.setText("");
        });

        JTextField reqNameF = UIUtils.formField(18);
        JTextField reqQtyF  = new JTextField("1", 4);
        JButton requestPartBtn = UIUtils.primaryButton("📋 Požiadať o diel");
        requestPartBtn.addActionListener(e -> {
            String name = reqNameF.getText().trim();
            if (name.isEmpty()) { UIUtils.showError(this, "Zadajte názov dielu."); return; }
            int qty = 1;
            try { qty = Integer.parseInt(reqQtyF.getText().trim()); } catch (NumberFormatException ignored) {}
            Order o = resolveSelectedOrder(orderBox);
            if (o == null) return;
            PartRequest req = inventory.createRequest(o.getOrderId(), employee.getEmail(), name, qty);
            orderService.setStatus(o.getOrderId(), OrderStatus.CAKA_NA_DIELY);
            ctx.getOrderRepository().save(o);
            ctx.getNotificationManager().notifyStorekeeper("SKLADNIK",
                    "📋 Nová požiadavka [" + req.getRequestId() + "]: "
                            + name + " (qty: " + qty + ") – zákazka "
                            + o.getOrderId().substring(0, 8));
            UIUtils.showInfo(this, "Požiadavka odoslaná skladníkovi.\n"
                    + "Diel: " + name + " (qty: " + qty + ")\n"
                    + "ID požiadavky: " + req.getRequestId() + "\n"
                    + "Zákazka → Čaká na diely.");
            reqNameF.setText(""); reqQtyF.setText("1");
            refreshAssigned();
        });

        orderBox.addActionListener(e -> {
            Order o = resolveSelectedOrder(orderBox);
            if (o != null) refreshWorkTables(o);
        });

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.add(new JLabel("Zákazka:")); top.add(orderBox);

        JPanel wiForm = new JPanel(new FlowLayout(FlowLayout.LEFT));
        wiForm.setBorder(BorderFactory.createTitledBorder("Pridať pracovnú položku"));
        wiForm.add(new JLabel("Popis:")); wiForm.add(wiDescF);
        wiForm.add(new JLabel("€:"));    wiForm.add(wiCostF);
        wiForm.add(addWiBtn);

        JPanel reqForm = new JPanel(new FlowLayout(FlowLayout.LEFT));
        reqForm.setBorder(BorderFactory.createTitledBorder("Požiadať o náhradný diel"));
        reqForm.add(new JLabel("Názov dielu:")); reqForm.add(reqNameF);
        reqForm.add(new JLabel("Qty:"));         reqForm.add(reqQtyF);
        reqForm.add(requestPartBtn);

        JSplitPane tables = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                new JScrollPane(workItemTable), new JScrollPane(sparePartTable));
        tables.setResizeWeight(0.5);

        JPanel forms = new JPanel(new GridLayout(2, 1));
        forms.add(wiForm); forms.add(reqForm);

        p.add(top,    BorderLayout.NORTH);
        p.add(tables, BorderLayout.CENTER);
        p.add(forms,  BorderLayout.SOUTH);
        return p;
    }

    private void refreshWorkTables(Order o) {
        DefaultTableModel wm = (DefaultTableModel) workItemTable.getModel();
        wm.setRowCount(0);
        for (WorkItem wi : o.getWorkItems()) {
            wm.addRow(new Object[]{wi.getDescription(), String.format("%.2f", wi.getLaborCost())});
        }
        DefaultTableModel pm = (DefaultTableModel) sparePartTable.getModel();
        pm.setRowCount(0);
        for (SparePart sp : o.getSpareParts()) {
            pm.addRow(new Object[]{sp.getName(), sp.getQuantity(), String.format("%.2f", sp.getUnitPrice())});
        }
    }

    private Order resolveSelectedOrder(JComboBox<String> box) {
        int idx = box.getSelectedIndex();
        if (idx < 0) return null;
        List<Order> orders = orderService.getOrdersForMechanic(String.valueOf(employee.getEmployeeId()));
        if (idx >= orders.size()) return null;
        return orders.get(idx);
    }

    // =========================================================
    // TAB 3 – Additional faults (UC07)
    // =========================================================
    private JPanel buildFaultsTab() {
        JPanel p = new JPanel(new BorderLayout(8, 8));
        p.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        JComboBox<String> orderBox2 = new JComboBox<>();
        Runnable fill = () -> {
            orderBox2.removeAllItems();
            for (Order o : orderService.getOrdersForMechanic(String.valueOf(employee.getEmployeeId()))) {
                orderBox2.addItem(o.getOrderId().substring(0, 8) + " – " + o.getDescription());
            }
        };
        fill.run();

        JTextArea faultDesc = new JTextArea(5, 40);
        faultDesc.setLineWrap(true);
        faultDesc.setBorder(BorderFactory.createTitledBorder("Popis dodatočne zistenej závady"));

        JTextField estCostF = new JTextField("0.00", 8);

        JButton reportBtn = UIUtils.dangerButton("⚠️ Nahlásiť závadu zákazníkovi (čaká na schválenie)");
        reportBtn.addActionListener(e -> {
            List<Order> orders = orderService.getOrdersForMechanic(String.valueOf(employee.getEmployeeId()));
            int idx = orderBox2.getSelectedIndex();
            if (idx < 0 || idx >= orders.size()) { UIUtils.showError(this, "Vyberte zákazku."); return; }
            Order o = orders.get(idx);
            String desc = faultDesc.getText().trim();
            if (desc.isEmpty()) { UIUtils.showError(this, "Zadajte popis závady."); return; }
            double cost = 0;
            try { cost = Double.parseDouble(estCostF.getText().trim()); } catch (NumberFormatException ignored) {}
            WorkItem fault = new WorkItem(java.util.UUID.randomUUID().toString().substring(0, 8),
                    "[DODATOČNÁ ZÁVADA] " + desc, cost);
            o.addWorkItem(fault);
            o.calculateCost(o.getWorkItems(), o.getSpareParts());
            orderService.setStatus(o.getOrderId(), OrderStatus.CAKA_NA_SCHVALENIE);
            ctx.getNotificationManager().notifyCustomer(o.getCustomerId(),
                    "Mechanik zistil dodatočnú závadu na Vašom vozidle: " + desc
                            + ". Odhadovaná cena: " + String.format("%.2f €", cost)
                            + ". Prosíme o schválenie v zákazníckej aplikácii.");
            UIUtils.showInfo(this, "Závada nahlásená. Zákazka čaká na schválenie zákazníka.");
            faultDesc.setText(""); estCostF.setText("0.00");
            fill.run();
            refreshAssigned();
        });

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.add(new JLabel("Zákazka:")); top.add(orderBox2);

        JPanel costPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        costPanel.add(new JLabel("Odhadovaná cena opravy (€):")); costPanel.add(estCostF);

        JPanel south = new JPanel(new BorderLayout());
        south.add(costPanel, BorderLayout.NORTH);
        south.add(reportBtn, BorderLayout.SOUTH);

        p.add(UIUtils.sectionLabel("Nahlásiť dodatočne zistenú závadu (UC07)"), BorderLayout.NORTH);
        p.add(top, BorderLayout.NORTH);
        p.add(new JScrollPane(faultDesc), BorderLayout.CENTER);
        p.add(south, BorderLayout.SOUTH);
        return p;
    }
}