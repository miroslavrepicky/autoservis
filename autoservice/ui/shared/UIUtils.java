package com.autoservice.ui.shared;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public final class UIUtils {

    // Color palette matching the Figma mockup
    public static final Color ACCENT       = new Color(74, 110, 145);
    public static final Color ACCENT_DARK  = new Color(54, 85, 115);
    public static final Color ACCENT_LIGHT = new Color(235, 240, 246);
    public static final Color BG_LIGHT     = new Color(248, 249, 251);
    public static final Color BG_WHITE     = Color.WHITE;
    public static final Color TEXT_PRIMARY = new Color(30, 35, 40);
    public static final Color TEXT_MUTED   = new Color(120, 130, 145);
    public static final Color BORDER_COLOR = new Color(220, 225, 232);
    public static final Color SUCCESS      = new Color(40, 160, 90);
    public static final Color WARN         = new Color(210, 120, 0);
    public static final Color DANGER       = new Color(190, 50, 50);
    public static final Color NAV_BG       = new Color(55, 80, 105);

    private UIUtils() {}

    public static void applyLookAndFeel() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}
        UIManager.put("Panel.background", BG_LIGHT);
        UIManager.put("OptionPane.background", BG_WHITE);
        UIManager.put("OptionPane.messageForeground", TEXT_PRIMARY);
    }

    // ── Buttons ──────────────────────────────────────────────

    public static JButton primaryButton(String text) {
        JButton btn = styledButton(text, ACCENT, Color.WHITE);
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) { btn.setBackground(ACCENT_DARK); }
            public void mouseExited(java.awt.event.MouseEvent e)  { btn.setBackground(ACCENT); }
        });
        return btn;
    }

    public static JButton dangerButton(String text) {
        return styledButton(text, DANGER, Color.WHITE);
    }

    public static JButton successButton(String text) {
        return styledButton(text, SUCCESS, Color.WHITE);
    }

    public static JButton outlineButton(String text) {
        JButton btn = new JButton(text);
        btn.setForeground(ACCENT);
        btn.setBackground(BG_WHITE);
        btn.setFocusPainted(false);
        btn.setFont(btn.getFont().deriveFont(Font.PLAIN, 13f));
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ACCENT, 1, true),
                BorderFactory.createEmptyBorder(5, 14, 5, 14)
        ));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private static JButton styledButton(String text, Color bg, Color fg) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setOpaque(false);
        btn.setFont(btn.getFont().deriveFont(Font.BOLD, 13f));
        btn.setBorder(BorderFactory.createEmptyBorder(7, 18, 7, 18));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    // ── Labels ───────────────────────────────────────────────

    public static JLabel sectionLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(lbl.getFont().deriveFont(Font.BOLD, 15f));
        lbl.setForeground(TEXT_PRIMARY);
        return lbl;
    }

    public static JLabel mutedLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(lbl.getFont().deriveFont(Font.PLAIN, 12f));
        lbl.setForeground(TEXT_MUTED);
        return lbl;
    }

    public static JLabel pageTitle(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(lbl.getFont().deriveFont(Font.BOLD, 20f));
        lbl.setForeground(TEXT_PRIMARY);
        return lbl;
    }

    // ── Navigation bar ───────────────────────────────────────

    /**
     * Creates a top navigation bar matching the Figma mockup style.
     * onLogout is called when user clicks "Odhlásiť".
     */
    public static JPanel buildNavBar(String appName, String userLabel, Runnable onLogout) {
        JPanel nav = new JPanel(new BorderLayout());
        nav.setBackground(NAV_BG);
        nav.setPreferredSize(new Dimension(0, 44));
        nav.setBorder(BorderFactory.createEmptyBorder(0, 16, 0, 16));

        JLabel brand = new JLabel(appName);
        brand.setFont(brand.getFont().deriveFont(Font.BOLD, 14f));
        brand.setForeground(Color.WHITE);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        right.setOpaque(false);
        JLabel user = new JLabel(userLabel);
        user.setForeground(new Color(200, 215, 230));
        user.setFont(user.getFont().deriveFont(Font.PLAIN, 12f));

        JButton logoutBtn = new JButton("Odhlásiť");
        logoutBtn.setForeground(Color.WHITE);
        logoutBtn.setBackground(new Color(180, 60, 60));
        logoutBtn.setFont(logoutBtn.getFont().deriveFont(Font.BOLD, 12f));
        logoutBtn.setBorder(BorderFactory.createEmptyBorder(4, 12, 4, 12));
        logoutBtn.setFocusPainted(false);
        logoutBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        logoutBtn.setOpaque(true);
        logoutBtn.addActionListener(e -> {
            if (onLogout != null) onLogout.run();
        });

        right.add(user);
        right.add(logoutBtn);
        nav.add(brand, BorderLayout.WEST);
        nav.add(right, BorderLayout.EAST);
        return nav;
    }

    // ── Card panel ───────────────────────────────────────────

    public static JPanel card(JComponent content) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(BG_WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1, true),
                BorderFactory.createEmptyBorder(12, 14, 12, 14)
        ));
        card.add(content);
        return card;
    }

    // ── Table ────────────────────────────────────────────────

    public static JTable buildTable(String[] columns) {
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(model);
        table.setRowHeight(28);
        table.setBackground(BG_WHITE);
        table.setForeground(TEXT_PRIMARY);
        table.setGridColor(BORDER_COLOR);
        table.setFont(table.getFont().deriveFont(Font.PLAIN, 13f));
        table.setShowHorizontalLines(true);
        table.setShowVerticalLines(false);
        table.getTableHeader().setBackground(ACCENT_LIGHT);
        table.getTableHeader().setForeground(TEXT_PRIMARY);
        table.getTableHeader().setFont(table.getTableHeader().getFont().deriveFont(Font.BOLD, 13f));
        table.getTableHeader().setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_COLOR));
        table.setSelectionBackground(new Color(210, 225, 245));
        table.setSelectionForeground(TEXT_PRIMARY);
        table.setFillsViewportHeight(true);
        // alternating row colors
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object val,
                                                           boolean sel, boolean foc, int row, int col) {
                super.getTableCellRendererComponent(t, val, sel, foc, row, col);
                setForeground(TEXT_PRIMARY);
                if (!sel) setBackground(row % 2 == 0 ? BG_WHITE : new Color(248, 250, 253));
                setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
                return this;
            }
        });
        return table;
    }

    // ── Scrollpane ───────────────────────────────────────────

    public static JScrollPane scrollPane(Component c) {
        JScrollPane sp = new JScrollPane(c);
        sp.setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1));
        sp.getViewport().setBackground(BG_WHITE);
        return sp;
    }

    // ── Form field ───────────────────────────────────────────

    public static JTextField formField(int cols) {
        JTextField f = new JTextField(cols);
        f.setFont(f.getFont().deriveFont(Font.PLAIN, 13f));
        f.setForeground(TEXT_PRIMARY);
        f.setBackground(BG_WHITE);
        f.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1, true),
                BorderFactory.createEmptyBorder(5, 8, 5, 8)
        ));
        return f;
    }

    public static JPasswordField passwordField(int cols) {
        JPasswordField f = new JPasswordField(cols);
        f.setFont(f.getFont().deriveFont(Font.PLAIN, 13f));
        f.setForeground(TEXT_PRIMARY);
        f.setBackground(BG_WHITE);
        f.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1, true),
                BorderFactory.createEmptyBorder(5, 8, 5, 8)
        ));
        return f;
    }

    // ── Dialogs ──────────────────────────────────────────────

    public static void showError(Component parent, String msg) {
        JOptionPane.showMessageDialog(parent, msg, "Chyba", JOptionPane.ERROR_MESSAGE);
    }

    public static void showInfo(Component parent, String msg) {
        JOptionPane.showMessageDialog(parent, msg, "Info", JOptionPane.INFORMATION_MESSAGE);
    }

    public static boolean confirm(Component parent, String msg) {
        return JOptionPane.showConfirmDialog(parent, msg, "Potvrdiť",
                JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
    }
}