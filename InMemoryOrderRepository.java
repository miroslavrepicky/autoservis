package com.autoservice.ui.customer;

import com.autoservice.AppContext;
import com.autoservice.domain.*;
import com.autoservice.service.OrderService;
import com.autoservice.service.VehicleManager;
import com.autoservice.ui.LoginWindow;
import com.autoservice.ui.shared.UIUtils;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class CustomerWindow extends JFrame {

    private final Customer customer;
    private final AppContext ctx = AppContext.getInstance();
    private final OrderService orderService = ctx.getOrderService();
    private final VehicleManager vehicleManager = ctx.getVehicleManager();

    private JTable vehicleTable;
    private JTable orderTable;
    private JTable extensionTable;
    private JComboBox<String> vehicleBox;

    public CustomerWindow(Customer customer) {
        super("AutoServis – " + customer.getFullName());
        this.customer = customer;
        buildUI();
        setSize(920, 660);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setVisible(true);

    }

    private void buildUI() {
        setLayout(new BorderLayout());
        setBackground(UIUtils.BG_LIGHT);

        // Nav bar
        JPanel nav = UIUtils.buildNavBar(
 "AutoProService",
                customer.getFullName() + "  |  Zákazník",
                this::doLogout
        );
        add(nav, BorderLayout.NORTH);

        // Tabs
        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(tabs.getFont().deriveFont(Font.PLAIN, 13f));
        tabs.setBackground(UIUtils.BG_LIGHT);
        tabs.addTab("Moje vozidlá", buildVehiclesTab());
        tabs.addTab("Rezervácia termínu", buildReservationTab());
        tabs.addTab("Moje zákazky", buildOrdersTab());
        tabs.addTab("Schválenie rozšírenia", buildExtensionTab());
        tabs.addChangeListener(e -> {
            int i = tabs.getSelectedIndex();
            if (i == 0) refreshVehicleTable();
            if (i == 1) refreshVehicleBox();
            if (i == 2) refreshOrderTable();
            if (i == 3) refreshExtensionTable();
        });
        add(tabs, BorderLayout.CENTER);


    }

    private void doLogout() {
        if (UIUtils.confirm(this, "Naozaj sa chcete odhlásiť?")) {
            dispose();
            new LoginWindow();
        }
    }

    //  TAB 1: Moje vozidlá 

    private JPanel buildVehiclesTab() {
        JPanel p = new JPanel(new BorderLayout(8, 8));
        p.setBackground(UIUtils.BG_LIGHT);
        p.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.add(UIUtils.sectionLabel("Moje vozidlá"), BorderLayout.WEST);
        JButton addBtn = UIUtils.primaryButton("+ Pridať vozidlo");
        addBtn.addActionListener(e -> openAddVehicleDialog());
        header.add(addBtn, BorderLayout.EAST);

        vehicleTable = UIUtils.buildTable(new String[]{"Značka", "Model", "Rok", "ŠPZ", "VIN", "Motor"});
        refreshVehicleTable();

        p.add(header, BorderLayout.NORTH);
        p.add(UIUtils.scrollPane(vehicleTable), BorderLayout.CENTER);
        return p;
    }

    private void refreshVehicleTable() {
        DefaultTableModel m = (DefaultTableModel) vehicleTable.getModel();
        m.setRowCount(0);
        for (VehicleCard card : customer.getVehicleCards()) {
            Vehicle v = card.getVehicle();
            m.addRow(new Object[]{v.getMake(), v.getModel(), v.getYear(),
                    v.getLicensePlate(), v.getVin(), v.getEngine()});
        }
    }

    private void openAddVehicleDialog() {
        JTextField vinF   = UIUtils.formField(17);
        JTextField makeF  = UIUtils.formField(14);
        JTextField modelF = UIUtils.formField(14);
        JTextField yearF  = UIUtils.formField(5);
        JTextField plateF = UIUtils.formField(10);
        JTextField engF   = UIUtils.formField(12);

        JPanel form = new JPanel(new GridLayout(6, 2, 8, 8));
        form.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        form.add(new JLabel("VIN (17 znakov):")); form.add(vinF);
        form.add(new JLabel("Značka:"));          form.add(makeF);
        form.add(new JLabel("Model:"));           form.add(modelF);
        form.add(new JLabel("Rok výroby:"));      form.add(yearF);
        form.add(new JLabel("ŠPZ:"));             form.add(plateF);
        form.add(new JLabel("Motor:"));           form.add(engF);

        int res = JOptionPane.showConfirmDialog(this, form, "Pridať vozidlo", JOptionPane.OK_CANCEL_OPTION);
        if (res != JOptionPane.OK_OPTION) return;

        String vin   = vinF.getText().trim().toUpperCase();
        String plate = plateF.getText().trim().toUpperCase();

        while (!vehicleManager.validateVIN(vin) || !vehicleManager.validateLicensePlate(plate)) {
            UIUtils.showError(this, "Nesprávny formát VIN alebo ŠPZ. Opravte údaje.");
            res = JOptionPane.showConfirmDialog(this, form, "Oprava údajov", JOptionPane.OK_CANCEL_OPTION);
            if (res != JOptionPane.OK_OPTION) return;
            vin   = vinF.getText().trim().toUpperCase();
            plate = plateF.getText().trim().toUpperCase();
        }

        if (vehicleManager.isVinRegistered(vin)) {
            UIUtils.showError(this, "Vozidlo s VIN " + vin + " je už evidované v systéme.");
            return;
        }

        Vehicle v = vehicleManager.createVehicleCard(makeF.getText().trim(), modelF.getText().trim(),
                vin, yearF.getText().trim(), engF.getText().trim(), plate);
        vehicleManager.createAndRegisterCard(customer, v);
        refreshVehicleTable();
        refreshVehicleBox();
        UIUtils.showInfo(this, "Vozidlo úspešne pridané do profilu.");
    }

    //  TAB 2: Rezervácia 

    private JPanel buildReservationTab() {
        JPanel p = new JPanel(new BorderLayout(8, 8));
        p.setBackground(UIUtils.BG_LIGHT);
        p.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        p.add(UIUtils.sectionLabel("Rezervácia servisného termínu"), BorderLayout.NORTH);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(UIUtils.BG_WHITE);
        form.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIUtils.BORDER_COLOR),
                BorderFactory.createEmptyBorder(16, 20, 16, 20)
        ));
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(7, 7, 7, 7);
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.anchor = GridBagConstraints.WEST;

        vehicleBox = new JComboBox<>();
        refreshVehicleBox();

        JComboBox<String> serviceBox = new JComboBox<>(
                new String[]{"Oprava", "Diagnostika", "Servisná prehliadka", "Výmena oleja", "Iné"});
        JSpinner dateSpinner = new JSpinner(new SpinnerDateModel());
        dateSpinner.setEditor(new JSpinner.DateEditor(dateSpinner, "dd.MM.yyyy HH:mm"));
        JTextArea descArea  = new JTextArea(3, 28);
        descArea.setLineWrap(true);
        JTextArea notesArea = new JTextArea(2, 28);
        notesArea.setLineWrap(true);

        int row = 0;
        addFormRow(form, gc, row++, "Vozidlo:", vehicleBox);
        addFormRow(form, gc, row++, "Typ služby:", serviceBox);
        addFormRow(form, gc, row++, "Dátum a čas:", dateSpinner);
        addFormRow(form, gc, row++, "Popis problému:", new JScrollPane(descArea));
        addFormRow(form, gc, row++, "Poznámky:", new JScrollPane(notesArea));

        JButton confirmBtn = UIUtils.primaryButton("Potvrdiť rezerváciu");
        gc.gridx = 1; gc.gridy = row;
        form.add(confirmBtn, gc);

        confirmBtn.addActionListener(e -> {
            if (customer.getVehicleCards().isEmpty()) {
                UIUtils.showError(this, "Nemáte žiadne vozidlo. Pridajte ho v záložke 'Moje vozidlá'.");
                return;
            }
            int idx = vehicleBox.getSelectedIndex();
            if (idx < 0) { UIUtils.showError(this, "Vyberte vozidlo."); return; }
            String vehicleId = customer.getVehicleCards().get(idx).getVehicle().getVehicleId();
            java.util.Date d = (java.util.Date) dateSpinner.getValue();
            LocalDateTime appt = d.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDateTime();
            String desc  = serviceBox.getSelectedItem() + " – " + descArea.getText().trim();
            String notes = notesArea.getText().trim();
            Order order = orderService.createReservation(customer.getCustomerId(), vehicleId, desc, appt, notes);
            UIUtils.showInfo(this, "Rezervácia vytvorená!\nID: " + order.getOrderId().substring(0, 8)
                    + "\nTermín: " + appt.format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")));
            descArea.setText(""); notesArea.setText("");
        });

        p.add(form, BorderLayout.CENTER);
        return p;
    }

    private void refreshVehicleBox() {
        if (vehicleBox == null) return;
        vehicleBox.removeAllItems();
        for (VehicleCard card : customer.getVehicleCards()) {
            Vehicle v = card.getVehicle();
            vehicleBox.addItem(v.getMake() + " " + v.getModel() + " – " + v.getLicensePlate());
        }
        if (vehicleBox.getItemCount() == 0) {
            vehicleBox.addItem("— žiadne vozidlo —");
        }
    }

    private void addFormRow(JPanel p, GridBagConstraints gc, int row, String label, JComponent field) {
        gc.gridx = 0; gc.gridy = row; gc.weightx = 0;
        JLabel lbl = new JLabel(label);
        lbl.setForeground(UIUtils.TEXT_PRIMARY);
        p.add(lbl, gc);
        gc.gridx = 1; gc.weightx = 1;
        p.add(field, gc);
    }

    //  TAB 3: Moje zákazky 

    private JPanel buildOrdersTab() {
        JPanel p = new JPanel(new BorderLayout(8, 8));
        p.setBackground(UIUtils.BG_LIGHT);
        p.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.add(UIUtils.sectionLabel("Moje zákazky"), BorderLayout.WEST);
        JButton refreshBtn = UIUtils.outlineButton("Obnoviť");
        refreshBtn.addActionListener(e -> refreshOrderTable());
        header.add(refreshBtn, BorderLayout.EAST);

        orderTable = UIUtils.buildTable(new String[]{"ID", "Popis", "Stav", "Termín", "Cena (€)"});
        refreshOrderTable();

        p.add(header, BorderLayout.NORTH);
        p.add(UIUtils.scrollPane(orderTable), BorderLayout.CENTER);
        return p;
    }

    private void refreshOrderTable() {
        DefaultTableModel m = (DefaultTableModel) orderTable.getModel();
        m.setRowCount(0);
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
        for (Order o : orderService.getOrders(customer.getCustomerId())) {
            m.addRow(new Object[]{
                    o.getOrderId().substring(0, 8),
                    o.getDescription(),
                    o.getStatus().getDisplayName(),
                    o.getAppointmentTime() != null ? o.getAppointmentTime().format(fmt) : "-",
                    String.format("%.2f", o.getTotalCost())
            });
        }
    }

    //  TAB 4: Schválenie rozšírenia 

    private JPanel buildExtensionTab() {
        JPanel p = new JPanel(new BorderLayout(8, 8));
        p.setBackground(UIUtils.BG_LIGHT);
        p.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.add(UIUtils.sectionLabel("Zákazky čakajúce na schválenie"), BorderLayout.WEST);

        extensionTable = UIUtils.buildTable(new String[]{"ID zákazky", "Popis", "Stav", "Cena (€)"});
        refreshExtensionTable();

        JButton approveBtn = UIUtils.successButton("Schváliť");
        JButton rejectBtn  = UIUtils.dangerButton("Zamietnuť");
        JButton refreshBtn = UIUtils.outlineButton("Obnoviť");
        approveBtn.addActionListener(e -> handleExtension(true));
        rejectBtn.addActionListener(e -> handleExtension(false));
        refreshBtn.addActionListener(e -> refreshExtensionTable());

        JPanel south = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        south.setOpaque(false);
        south.add(approveBtn); south.add(rejectBtn); south.add(refreshBtn);

        p.add(header, BorderLayout.NORTH);
        p.add(UIUtils.scrollPane(extensionTable), BorderLayout.CENTER);
        p.add(south, BorderLayout.SOUTH);
        return p;
    }

    private void refreshExtensionTable() {
        DefaultTableModel m = (DefaultTableModel) extensionTable.getModel();
        m.setRowCount(0);
        for (Order o : orderService.getOrdersByStatus(OrderStatus.CAKA_NA_SCHVALENIE)) {
            if (o.getCustomerId().equals(customer.getCustomerId())) {
                m.addRow(new Object[]{
                        o.getOrderId().substring(0, 8),
                        o.getDescription(),
                        o.getStatus().getDisplayName(),
                        String.format("%.2f", o.getTotalCost())
                });
            }
        }
    }

    private void handleExtension(boolean approve) {
        int row = extensionTable.getSelectedRow();
        if (row < 0) { UIUtils.showError(this, "Vyberte zákazku."); return; }
        String shortId = (String) extensionTable.getValueAt(row, 0);
        Order target = orderService.getOrdersByStatus(OrderStatus.CAKA_NA_SCHVALENIE).stream()
                .filter(o -> o.getCustomerId().equals(customer.getCustomerId())
                        && o.getOrderId().startsWith(shortId))
                .findFirst().orElse(null);
        if (target == null) return;

        if (approve) {
            orderService.approveRepairExtension(target.getOrderId(), List.of());
            UIUtils.showInfo(this, "Rozšírenie opravy bolo schválené.");
        } else {
            orderService.rejectRepairExtension(target.getOrderId());
            UIUtils.showInfo(this, "Rozšírenie opravy bolo zamietnuté.");
        }
        refreshExtensionTable();
        refreshOrderTable();
    }
}