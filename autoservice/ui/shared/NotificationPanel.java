package com.autoservice.ui.shared;

import javax.swing.*;
import java.awt.*;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * Reusable notification panel that can be embedded in any employee window.
 * Registers itself with NotificationManager to receive in-app alerts.
 */
public class NotificationPanel extends JPanel {

    private final DefaultListModel<String> model = new DefaultListModel<>();
    private final JList<String> list = new JList<>(model);
    private int unreadCount = 0;
    private final JLabel badge;

    public NotificationPanel(String ownerLabel) {
        setLayout(new BorderLayout(4, 4));
        setBorder(BorderFactory.createTitledBorder("🔔 Notifikácie – " + ownerLabel));

        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        list.setCellRenderer(new NotifCellRenderer());
        add(new JScrollPane(list), BorderLayout.CENTER);

        JPanel south = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        badge = new JLabel("0 neprečítaných");
        badge.setForeground(new Color(180, 0, 0));
        badge.setFont(badge.getFont().deriveFont(Font.BOLD));
        JButton clearBtn = new JButton("Vymazať všetko");
        clearBtn.addActionListener(e -> { model.clear(); unreadCount = 0; updateBadge(); });
        south.add(badge);
        south.add(clearBtn);
        add(south, BorderLayout.SOUTH);

        // mark read on click
        list.addListSelectionListener(e -> { if (!e.getValueIsAdjusting()) { unreadCount = 0; updateBadge(); } });
    }

    public void addNotification(String message) {
        String ts = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
        SwingUtilities.invokeLater(() -> {
            model.insertElementAt("[" + ts + "] " + message, 0);
            unreadCount++;
            updateBadge();
        });
    }

    private void updateBadge() {
        badge.setText(unreadCount + " neprečítaných");
        badge.setForeground(unreadCount > 0 ? new Color(180, 0, 0) : new Color(0, 120, 0));
    }

    // ---- cell renderer ---------------------------------------------------
    private static class NotifCellRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value,
                int index, boolean isSelected, boolean cellHasFocus) {
            JLabel lbl = (JLabel) super.getListCellRendererComponent(
                    list, value, index, isSelected, cellHasFocus);
            if (index == 0 && !isSelected) lbl.setForeground(new Color(0, 80, 160));
            return lbl;
        }
    }
}
