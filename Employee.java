package com.autoservice.ui.storekeeper;

import com.autoservice.AppContext;
import com.autoservice.domain.OrderStatus;
import com.autoservice.domain.PartRequest;
import com.autoservice.domain.SparePart;
import com.autoservice.employee.Storekeeper;
import com.autoservice.service.Inventory;
import com.autoservice.service.OrderService;
import com.autoservice.ui.shared.NotificationPanel;
import com.autoservice.ui.shared.UIUtils;
import com.autoservice.ui.LoginWindow;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

/**
 * Storekeeper window – manage spare parts inventory, confirm deliveries,
 * view orders waiting for parts.
 *
 * Oprava: potvrdenie doručenia dielu používa combobox objednaných dielov
 * namiesto ručného zadávania ID.
 */
public class StorekeeperWindow extends JFrame {

    private final Storekeeper employee;
    private final AppContext ctx = AppContext.getInstance();
    private final Inventory inventory       = ctx.getInventory();
    private final OrderService orderService = ctx.getOrderService();

    private JTable inventoryTable;
    private JTable waitingOrdersTable;
    private final NotificationPanel notifPanel = new NotificationPanel("Skladník");

    // Combobox pre potvrdenie doručenia – referencie kvôli refreshu
    private JComboBox<OrderedPartItem> deliveryCombo;
    private JTextField deliveryQtyF;

    public StorekeeperWindow(Storekeeper employee) {
        super("AutoServis – " + employee.getRole() + ": " + employee.getFullName());
        this.employee = employee;
        ctx.getNotificationManager().addInAppListener(msg -> {
            if (msg.contains("SKLADNÍK") || msg.contains("storekeeper")) {
                notifPanel.addNotification(msg);
            }
        });
        buildUI();
        setSize(900, 680);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setVisible(true);
    }

    private void buildUI() {
        setLayout(new BorderLayout());
        JPanel nav = UIUtils.buildNavBar("AutoProService",
                employee.getFullName() + "  | " + employee.getRole(),
                () -> { if (UIUtils.confirm(this, "Naozaj sa chcete odhlásiť?")) { dispose(); new LoginWindow(); } });
        add(nav, BorderLayout.NORTH);
        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab(" Sklad dielov",              buildInventoryTab());
        tabs.addTab(" Požiadavky mechanikov",      buildRequestsTab());
        tabs.addTab("⏳ Zákazky čakajúce na diely",  buildWaitingOrdersTab());
        tabs.addTab(" Notifikácie",                notifPanel);
        tabs.addChangeListener(e -> {
            if (tabs.getSelectedIndex() == 0) { refreshInventory(); refreshDeliveryCombo(); }
            if (tabs.getSelectedIndex() == 1) refreshWaitingOrders();
        });
        add(tabs, BorderLayout.CENTER);
    }

    // =========================================================
    // TAB 1 – Inventory management
    // =========================================================
    private JPanel buildInventoryTab() {
        JPanel p = new JPanel(new BorderLayout(8, 8));
        p.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        inventoryTable = UIUtils.buildTable(new String[]{"ID dielu", "Názov", "Cena/ks (€)", "Na sklade"});
        refreshInventory();

        //  Pridať nový diel 
        JTextField idF    = new JTextField(8);
        JTextField nameF  = new JTextField(16);
        JTextField priceF = new JTextField(6);
        JTextField qtyF   = new JTextField(4);
        JButton addBtn    = UIUtils.primaryButton("+ Pridať diel na sklad");

        addBtn.addActionListener(e -> {
            String id   = idF.getText().trim().toUpperCase();
            String name = nameF.getText().trim();
            if (id.isEmpty() || name.isEmpty()) { UIUtils.showError(this, "Vyplňte ID a názov dielu."); return; }
            double price = 0;
            int qty = 0;
            try { price = Double.parseDouble(priceF.getText().trim()); } catch (NumberFormatException ex) {
                UIUtils.showError(this, "Neplatná cena."); return;
            }
            try { qty = Integer.parseInt(qtyF.getText().trim()); } catch (NumberFormatException ex) {
                UIUtils.showError(this, "Neplatné množstvo."); return;
            }
            SparePart sp = new SparePart(id, name, price, String.valueOf(qty));
            inventory.addPart(sp, qty);
            UIUtils.showInfo(this, "Diel " + name + " pridaný na sklad (qty: " + qty + ").");
            refreshInventory();
            refreshDeliveryCombo();
            idF.setText(""); nameF.setText(""); priceF.setText(""); qtyF.setText("");
        });

        //  Potvrdiť doručenie – combobox objednaných dielov 
        deliveryCombo = new JComboBox<>();
        deliveryQtyF  = new JTextField("1", 4);
        refreshDeliveryCombo();

        JButton confirmBtn = UIUtils.successButton(" Potvrdiť doručenie");
        confirmBtn.addActionListener(e -> {
            OrderedPartItem selected = (OrderedPartItem) deliveryCombo.getSelectedItem();
            if (selected == null) { UIUtils.showError(this, "Žiadne objednané diely na potvrdenie."); return; }
            int qty = 1;
            try { qty = Integer.parseInt(deliveryQtyF.getText().trim()); } catch (NumberFormatException ignored) {}

            if (selected.isFromInventory()) {
                // Diel existuje v sklade – len navýšiť zásoby
                SparePart sp = selected.getSparePart();
                inventory.addPart(sp, qty);
                ctx.getNotificationManager().notifyEmployee("mechanic",
 "Diel " + sp.getName() + " bol doručený na sklad (qty: " + qty + ").");
                UIUtils.showInfo(this, "Doručenie potvrdené. Sklad aktualizovaný.");
                checkAndNotifyWaitingOrders(sp.getPartId());
            } else {
                // Diel pochádza z požiadavky mechanika – označiť ako doručený
                PartRequest req = selected.getPartRequest();
                inventory.deliverRequest(req.getRequestId());

                // Vytvoriť/nájsť SparePart v sklade a pridať qty
                String partId = "REQ-" + req.getRequestId();
                SparePart sp = new SparePart(partId, req.getPartName(),
                        req.getFinalPrice(), String.valueOf(qty));
                inventory.addPart(sp, qty);

                ctx.getNotificationManager().notifyMechanic(req.getMechanicId(),
 "Diel \"" + req.getPartName() + "\" bol doručený na sklad (qty: " + qty + ").");
                UIUtils.showInfo(this, "Doručenie potvrdené.\nDiel: " + req.getPartName()
                        + "\nZákazka: " + req.getOrderId().substring(0, 8));
                checkAndNotifyWaitingOrders(partId);
            }

            refreshInventory();
            refreshDeliveryCombo();
            deliveryQtyF.setText("1");
        });

        JButton refreshDeliveryBtn = UIUtils.outlineButton("Obnoviť zoznam");
        refreshDeliveryBtn.addActionListener(e -> refreshDeliveryCombo());

        //  Formuláre 
        JPanel addForm = new JPanel(new FlowLayout(FlowLayout.LEFT));
        addForm.setBorder(BorderFactory.createTitledBorder("Pridať nový diel na sklad"));
        addForm.add(new JLabel("ID:")); addForm.add(idF);
        addForm.add(new JLabel("Názov:")); addForm.add(nameF);
        addForm.add(new JLabel("Cena €:")); addForm.add(priceF);
        addForm.add(new JLabel("Qty:")); addForm.add(qtyF);
        addForm.add(addBtn);

        JPanel deliveryForm = new JPanel(new FlowLayout(FlowLayout.LEFT));
        deliveryForm.setBorder(BorderFactory.createTitledBorder("Potvrdiť doručenie dielu"));
        deliveryForm.add(new JLabel("Diel:"));
        deliveryCombo.setPreferredSize(new Dimension(320, 26));
        deliveryForm.add(deliveryCombo);
        deliveryForm.add(new JLabel("Qty:"));
        deliveryForm.add(deliveryQtyF);
        deliveryForm.add(confirmBtn);
        deliveryForm.add(refreshDeliveryBtn);

        JButton refreshInventoryBtn = UIUtils.primaryButton("Obnoviť sklad");
        refreshInventoryBtn.addActionListener(e -> refreshInventory());
        JPanel rPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        rPanel.add(refreshInventoryBtn);

        JPanel south = new JPanel(new GridLayout(3, 1));
        south.add(addForm);
        south.add(deliveryForm);
        south.add(rPanel);

        p.add(UIUtils.sectionLabel("Sklad náhradných dielov"), BorderLayout.NORTH);
        p.add(new JScrollPane(inventoryTable), BorderLayout.CENTER);
        p.add(south, BorderLayout.SOUTH);
        return p;
    }

    /**
     * Naplní combobox doručenia:
     * 1. Objednané diely z požiadaviek mechanikov (stav ORDERED)
     * 2. Existujúce diely v sklade (pre doplnenie zásob)
     */
    private void refreshDeliveryCombo() {
        if (deliveryCombo == null) return;
        deliveryCombo.removeAllItems();

        // Najprv – požiadavky mechanikov v stave ORDERED (priorita)
        boolean hasOrdered = false;
        for (PartRequest req : inventory.getAllRequests()) {
            if (req.getStatus() == PartRequest.Status.ORDERED) {
                deliveryCombo.addItem(new OrderedPartItem(req));
                hasOrdered = true;
            }
        }

        // Potom – existujúce diely v sklade (doplnenie)
        for (SparePart sp : inventory.getAllParts()) {
            deliveryCombo.addItem(new OrderedPartItem(sp));
        }

        if (deliveryCombo.getItemCount() == 0) {
            deliveryCombo.addItem(null); // prázdny stav
        }
    }

    private void refreshInventory() {
        DefaultTableModel m = (DefaultTableModel) inventoryTable.getModel();
        m.setRowCount(0);
        for (SparePart sp : inventory.getAllParts()) {
            m.addRow(new Object[]{
                    sp.getPartId(),
                    sp.getName(),
                    String.format("%.2f", sp.getUnitPrice()),
                    inventory.getStock(sp.getPartId())
            });
        }
    }

    // =========================================================
    // TAB 2 – Mechanic part requests
    // =========================================================
    private JTable requestsTable;

    private JPanel buildRequestsTab() {
        JPanel p = new JPanel(new BorderLayout(8, 8));
        p.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        requestsTable = UIUtils.buildTable(
                new String[]{"ID", "Zákazka", "Diel", "Qty", "Cena (€)", "Stav"});
        refreshRequestsTable();

        JTextField priceF  = UIUtils.formField(8);
        JButton orderBtn   = UIUtils.primaryButton(" Objednať (doplniť cenu)");
        JButton refreshBtn = UIUtils.outlineButton("Obnoviť");

        orderBtn.addActionListener(e -> {
            int row = requestsTable.getSelectedRow();
            if (row < 0) { UIUtils.showError(this, "Vyberte požiadavku."); return; }
            String reqId = (String) requestsTable.getValueAt(row, 0);
            String priceStr = priceF.getText().trim();
            if (priceStr.isEmpty()) { UIUtils.showError(this, "Zadajte cenu dielu."); return; }
            double price;
            try { price = Double.parseDouble(priceStr); } catch (NumberFormatException ex) {
                UIUtils.showError(this, "Neplatná cena."); return;
            }

            inventory.fulfillRequest(reqId, price);

            inventory.getAllRequests().stream()
                    .filter(r -> r.getRequestId().equals(reqId))
                    .findFirst()
                    .ifPresent(req -> {
                        orderService.getOrders(null).stream()
                                .filter(o -> o.getOrderId().equals(req.getOrderId()))
                                .findFirst()
                                .ifPresent(o -> {
                                    String partId = "ORD-" + reqId;
                                    SparePart sp = new SparePart(partId, req.getPartName(),
                                            price, String.valueOf(req.getQuantity()));
                                    o.addSparePart(sp);
                                    o.calculateCost(o.getWorkItems(), o.getSpareParts());
                                    ctx.getOrderRepository().save(o);
                                });
                        ctx.getNotificationManager().notifyMechanic(req.getMechanicId(),
 "Diel \"" + req.getPartName() + "\" objednaný za "
                                        + String.format("%.2f", price) + " € – zákazka "
                                        + req.getOrderId().substring(0, 8));
                    });

            UIUtils.showInfo(this, "Objednávka potvrdená.\nCena: " + price + " €\nMechanik bol notifikovaný.");
            priceF.setText("");
            refreshRequestsTable();
            refreshDeliveryCombo(); // objednaný diel sa teraz objaví v comboboxe doručenia
        });

        refreshBtn.addActionListener(e -> refreshRequestsTable());

        JPanel south = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        south.add(new JLabel("Cena (€):")); south.add(priceF);
        south.add(orderBtn); south.add(refreshBtn);

        p.add(UIUtils.sectionLabel("Požiadavky mechanikov na diely"), BorderLayout.NORTH);
        p.add(UIUtils.scrollPane(requestsTable), BorderLayout.CENTER);
        p.add(south, BorderLayout.SOUTH);
        return p;
    }

    private void refreshRequestsTable() {
        if (requestsTable == null) return;
        DefaultTableModel m = (DefaultTableModel) requestsTable.getModel();
        m.setRowCount(0);
        for (var req : inventory.getAllRequests()) {
            m.addRow(new Object[]{
                    req.getRequestId(),
                    req.getOrderId().substring(0, 8),
                    req.getPartName(),
                    req.getQuantity(),
                    req.getFinalPrice() > 0 ? String.format("%.2f", req.getFinalPrice()) : "—",
                    req.getStatus().name()
            });
        }
    }

    // =========================================================
    // TAB 3 – Orders waiting for parts
    // =========================================================
    private JPanel buildWaitingOrdersTab() {
        JPanel p = new JPanel(new BorderLayout(8, 8));
        p.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        waitingOrdersTable = UIUtils.buildTable(
                new String[]{"ID zákazky", "Popis", "Potrebné diely"});
        refreshWaitingOrders();

        JButton refreshBtn = UIUtils.primaryButton(" Obnoviť");
        refreshBtn.addActionListener(e -> refreshWaitingOrders());

        p.add(UIUtils.sectionLabel("Zákazky čakajúce na diely"), BorderLayout.NORTH);
        p.add(new JScrollPane(waitingOrdersTable), BorderLayout.CENTER);
        JPanel south = new JPanel(new FlowLayout(FlowLayout.LEFT));
        south.add(refreshBtn);
        p.add(south, BorderLayout.SOUTH);
        return p;
    }

    private void refreshWaitingOrders() {
        DefaultTableModel m = (DefaultTableModel) waitingOrdersTable.getModel();
        m.setRowCount(0);
        for (var o : orderService.getOrdersByStatus(OrderStatus.CAKA_NA_DIELY)) {
            StringBuilder parts = new StringBuilder();
            for (SparePart sp : o.getSpareParts()) {
                parts.append(sp.getName()).append(" (qty: ").append(sp.getQuantity()).append(") ");
            }
            m.addRow(new Object[]{
                    o.getOrderId().substring(0, 8),
                    o.getDescription(),
                    parts.toString()
            });
        }
    }

    private void checkAndNotifyWaitingOrders(String partId) {
        for (var o : orderService.getOrdersByStatus(OrderStatus.CAKA_NA_DIELY)) {
            boolean allAvailable = o.getSpareParts().stream().allMatch(sp -> {
                int qty = 1;
                try { qty = Integer.parseInt(sp.getQuantity()); } catch (Exception ignored) {}
                return inventory.isAvailable(sp.getPartId(), qty);
            });
            if (allAvailable) {
                orderService.setStatus(o.getOrderId(), OrderStatus.OPRAVA);
                ctx.getNotificationManager().notifyMechanic(o.getMechanicId(),
 "Všetky diely pre zákazku #" + o.getOrderId().substring(0, 8)
                                + " sú dostupné. Môžete začať opravu.");
                notifPanel.addNotification("Zákazka #" + o.getOrderId().substring(0, 8)
                        + " – diely dostupné, stav zmenený na Oprava.");
            }
        }
        refreshWaitingOrders();
    }

    // =========================================================
    // Pomocná trieda pre položky v comboboxe doručenia
    // =========================================================
    private static class OrderedPartItem {
        private final PartRequest partRequest;
        private final SparePart   sparePart;

        /** Položka z požiadavky mechanika (ORDERED) */
        OrderedPartItem(PartRequest req) {
            this.partRequest = req;
            this.sparePart   = null;
        }

        /** Položka z existujúceho skladu */
        OrderedPartItem(SparePart sp) {
            this.sparePart   = sp;
            this.partRequest = null;
        }

        boolean isFromInventory() { return sparePart != null; }
        PartRequest getPartRequest() { return partRequest; }
        SparePart   getSparePart()   { return sparePart; }

        @Override
        public String toString() {
            if (partRequest != null) {
                return "[Objednané] " + partRequest.getPartName()
                        + " – zákazka " + partRequest.getOrderId().substring(0, 8)
                        + " (" + String.format("%.2f €", partRequest.getFinalPrice()) + ")";
            } else {
                return "[Sklad] " + sparePart.getName()
                        + " [" + sparePart.getPartId() + "]";
            }
        }
    }
}