package com.autoservice.ui;

import com.autoservice.AppContext;
import com.autoservice.domain.Customer;
import com.autoservice.employee.*;
import com.autoservice.ui.customer.CustomerWindow;
import com.autoservice.ui.dispatcher.DispatcherWindow;
import com.autoservice.ui.mechanic.MechanicWindow;
import com.autoservice.ui.receptionist.ReceptionistWindow;
import com.autoservice.ui.shared.UIUtils;
import com.autoservice.ui.storekeeper.StorekeeperWindow;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * Hlavné okno výberu profilu – bez prihlásenia.
 * Zákazníci a zamestnanci sú predseedy v AppContext.
 */
public class LoginWindow extends JFrame {

    public LoginWindow() {
        super("AutoServis – Výber profilu");
        UIUtils.applyLookAndFeel();
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(false);
        buildUI();
        pack();
        setMinimumSize(new Dimension(480, 400));
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void buildUI() {
        JPanel root = new JPanel(new BorderLayout(0, 0));
        root.setBackground(UIUtils.BG_LIGHT);

        // ── Hlavička ──────────────────────────────────────────
        JPanel header = new JPanel(new GridLayout(2, 1, 0, 4));
        header.setBackground(UIUtils.NAV_BG);
        header.setBorder(BorderFactory.createEmptyBorder(18, 28, 18, 28));

        JLabel title = new JLabel("AutoServis s.r.o.", SwingConstants.CENTER);
        title.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 22));
        title.setForeground(Color.WHITE);

        JLabel sub = new JLabel("Vyberte profil pre vstup do systému", SwingConstants.CENTER);
        sub.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
        sub.setForeground(new Color(200, 215, 230));

        header.add(title);
        header.add(sub);
        root.add(header, BorderLayout.NORTH);

        // ── Záložky ───────────────────────────────────────────
        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
        tabs.setBackground(UIUtils.BG_LIGHT);
        tabs.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        tabs.addTab("👤 Zákazníci",   buildCustomerPanel());
        tabs.addTab("🔧 Zamestnanci", buildEmployeePanel());

        root.add(tabs, BorderLayout.CENTER);
        setContentPane(root);
    }

    // ── Panel zákazníkov ──────────────────────────────────────
    private JPanel buildCustomerPanel() {
        AppContext ctx = AppContext.getInstance();
        List<Customer> customers = ctx.getProfileManager().getAllCustomers();

        JPanel wrapper = new JPanel(new BorderLayout(8, 8));
        wrapper.setBackground(UIUtils.BG_LIGHT);
        wrapper.setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));

        JLabel hint = UIUtils.mutedLabel("Kliknite na zákazníka pre otvorenie jeho profilu:");
        hint.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        wrapper.add(hint, BorderLayout.NORTH);

        JPanel grid = new JPanel(new GridLayout(0, 1, 0, 8));
        grid.setBackground(UIUtils.BG_LIGHT);

        for (Customer c : customers) {
            if (c.isTemporary()) continue;

            JPanel card = buildProfileCard(
                    c.getFullName(),
                    c.getEmail() + "  |  " + c.getPhone(),
                    "Z",
                    UIUtils.ACCENT
            );
            card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            card.addMouseListener(new java.awt.event.MouseAdapter() {
                public void mouseClicked(java.awt.event.MouseEvent e) {
                    new CustomerWindow(c);
                    dispose();
                }
                public void mouseEntered(java.awt.event.MouseEvent e) {
                    card.setBackground(UIUtils.ACCENT_LIGHT);
                }
                public void mouseExited(java.awt.event.MouseEvent e) {
                    card.setBackground(UIUtils.BG_WHITE);
                }
            });
            grid.add(card);
        }

        wrapper.add(new JScrollPane(grid), BorderLayout.CENTER);
        return wrapper;
    }

    // ── Panel zamestnancov ────────────────────────────────────
    private JPanel buildEmployeePanel() {
        AppContext ctx = AppContext.getInstance();

        JPanel wrapper = new JPanel(new BorderLayout(8, 8));
        wrapper.setBackground(UIUtils.BG_LIGHT);
        wrapper.setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));

        JLabel hint = UIUtils.mutedLabel("Kliknite na zamestnanca pre otvorenie pracovného rozhrania:");
        hint.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        wrapper.add(hint, BorderLayout.NORTH);

        JPanel grid = new JPanel(new GridLayout(0, 1, 0, 8));
        grid.setBackground(UIUtils.BG_LIGHT);

        for (Employee emp : ctx.getEmployees().values()) {
            Color accent = roleColor(emp);
            JPanel card = buildProfileCard(
                    emp.getFullName(),
                    emp.getRole() + "  |  " + emp.getEmail(),
                    emp.getFirstName().substring(0, 1),
                    accent
            );
            card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            card.addMouseListener(new java.awt.event.MouseAdapter() {
                public void mouseClicked(java.awt.event.MouseEvent e) {
                    openEmployeeWindow(emp);
                    dispose();
                }
                public void mouseEntered(java.awt.event.MouseEvent e) {
                    card.setBackground(UIUtils.ACCENT_LIGHT);
                }
                public void mouseExited(java.awt.event.MouseEvent e) {
                    card.setBackground(UIUtils.BG_WHITE);
                }
            });
            grid.add(card);
        }

        wrapper.add(new JScrollPane(grid), BorderLayout.CENTER);
        return wrapper;
    }

    // ── Karta profilu ─────────────────────────────────────────
    private JPanel buildProfileCard(String name, String detail, String initial, Color accentColor) {
        JPanel card = new JPanel(new BorderLayout(12, 0));
        card.setBackground(UIUtils.BG_WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIUtils.BORDER_COLOR, 1, true),
                BorderFactory.createEmptyBorder(10, 14, 10, 14)
        ));

        // Avatar kruh s iniciálou
        JLabel avatar = new JLabel(initial.toUpperCase(), SwingConstants.CENTER) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(accentColor);
                g2.fillOval(0, 0, getWidth(), getHeight());
                g2.dispose();
                super.paintComponent(g);
            }
        };
        avatar.setForeground(Color.WHITE);
        avatar.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 16));
        avatar.setPreferredSize(new Dimension(40, 40));
        avatar.setOpaque(false);

        // Text
        JPanel text = new JPanel(new GridLayout(2, 1, 0, 2));
        text.setOpaque(false);
        JLabel nameLabel = new JLabel(name);
        nameLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        nameLabel.setForeground(UIUtils.TEXT_PRIMARY);
        JLabel detailLabel = new JLabel(detail);
        detailLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        detailLabel.setForeground(UIUtils.TEXT_MUTED);
        text.add(nameLabel);
        text.add(detailLabel);

        // Šípka napravo
        JLabel arrow = new JLabel("›");
        arrow.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 22));
        arrow.setForeground(UIUtils.TEXT_MUTED);

        card.add(avatar, BorderLayout.WEST);
        card.add(text,   BorderLayout.CENTER);
        card.add(arrow,  BorderLayout.EAST);
        return card;
    }

    // ── Farba podľa roly ──────────────────────────────────────
    private Color roleColor(Employee emp) {
        if (emp instanceof Mechanic)            return new Color(40, 140, 90);
        if (emp instanceof Dispatcher)          return new Color(180, 100, 20);
        if (emp instanceof ReceptionTechnician) return new Color(74, 110, 145);
        if (emp instanceof Storekeeper)         return new Color(120, 60, 160);
        return UIUtils.ACCENT;
    }

    // ── Otvorenie okna zamestnanca ────────────────────────────
    private void openEmployeeWindow(Employee emp) {
        if (emp instanceof ReceptionTechnician rt) new ReceptionistWindow(rt);
        else if (emp instanceof Dispatcher dp)     new DispatcherWindow(dp);
        else if (emp instanceof Mechanic m)        new MechanicWindow(m);
        else if (emp instanceof Storekeeper sk)    new StorekeeperWindow(sk);
    }
}