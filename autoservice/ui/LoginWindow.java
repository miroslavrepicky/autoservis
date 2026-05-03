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

public class LoginWindow extends JFrame {

    private final JTextField loginField = UIUtils.formField(20);
    private final JPasswordField passField = UIUtils.passwordField(20);

    public LoginWindow() {
        super("AutoServis – Prihlásenie");
        UIUtils.applyLookAndFeel();
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(false);
        buildUI();
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void buildUI() {
        JPanel root = new JPanel(new BorderLayout(10, 10));
        root.setBorder(BorderFactory.createEmptyBorder(24, 32, 24, 32));
        root.setBackground(UIUtils.BG_LIGHT);

        // header
        JLabel title = new JLabel("AutoServis s.r.o.", SwingConstants.CENTER);
        title.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 22));
        title.setForeground(UIUtils.ACCENT);
        JLabel sub = new JLabel("Systém správy zákaziek", SwingConstants.CENTER);
        sub.setForeground(Color.GRAY);
        JPanel header = new JPanel(new GridLayout(2, 1, 0, 4));
        header.setOpaque(false);
        header.add(title);
        header.add(sub);
        root.add(header, BorderLayout.NORTH);

        // form
        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(6, 6, 6, 6);
        gc.fill = GridBagConstraints.HORIZONTAL;

        gc.gridx = 0; gc.gridy = 0;
        form.add(new JLabel("Prihlásenie:"), gc);
        gc.gridx = 1;
        form.add(loginField, gc);

        gc.gridx = 0; gc.gridy = 1;
        form.add(new JLabel("Heslo:"), gc);
        gc.gridx = 1;
        form.add(passField, gc);

        root.add(form, BorderLayout.CENTER);

        // hint
        JLabel hint = new JLabel("<html><small>" +
                "<b>Zákazníci</b> – zadajte email + heslo<br>" +
                "<b>Zamestnanci</b> – zadajte rolu (bez hesla):<br>" +
                "&nbsp;&nbsp;technik &nbsp;|&nbsp; dispecer &nbsp;|&nbsp; mechanik &nbsp;|&nbsp; skladnik" +
                "</small></html>", SwingConstants.CENTER);
        hint.setForeground(Color.GRAY);

        // buttons
        JButton loginBtn = UIUtils.primaryButton("Prihlásiť sa");
        JButton regBtn   = new JButton("Registrovať zákazníka");
        loginBtn.addActionListener(e -> doLogin());
        regBtn.addActionListener(e -> new RegistrationWindow(this));

        JPanel south = new JPanel(new GridLayout(3, 1, 6, 6));
        south.setOpaque(false);
        south.add(loginBtn);
        south.add(regBtn);
        south.add(hint);
        root.add(south, BorderLayout.SOUTH);

        setContentPane(root);
        getRootPane().setDefaultButton(loginBtn);
    }

    private void doLogin() {
        String login    = loginField.getText().trim().toLowerCase();
        String password = new String(passField.getPassword());
        AppContext ctx  = AppContext.getInstance();

        if (login.isEmpty()) {
            UIUtils.showError(this, "Zadajte email alebo rolu.");
            return;
        }

        // ── Zamestnanec podľa kľúčového slova (bez hesla) ──
        switch (login) {
            case "technik"   -> { openEmployeeWindow(ctx.findEmployeeByRole("technik"));   return; }
            case "dispecer"  -> { openEmployeeWindow(ctx.findEmployeeByRole("dispecer"));  return; }
            case "mechanik"  -> { openEmployeeWindow(ctx.findEmployeeByRole("mechanik"));  return; }
            case "skladnik"  -> { openEmployeeWindow(ctx.findEmployeeByRole("skladnik"));  return; }
        }

        // ── Zákazník podľa emailu + hesla ──
        Customer customer = ctx.getProfileManager().findCustomerByEmail(login);
        if (customer == null) {
            UIUtils.showError(this, "Zákazník nenájdený.\nSkontrolujte email alebo sa zaregistrujte.");
            return;
        }
        if (!customer.checkPassword(password)) {
            UIUtils.showError(this, "Nesprávne heslo.");
            return;
        }
        new CustomerWindow(customer);
        dispose();
    }

    private void openEmployeeWindow(Employee emp) {
        if (emp == null) { UIUtils.showError(this, "Zamestnanec nenájdený."); return; }
        if (emp instanceof ReceptionTechnician rt) new ReceptionistWindow(rt);
        else if (emp instanceof Dispatcher dp)     new DispatcherWindow(dp);
        else if (emp instanceof Mechanic m)        new MechanicWindow(m);
        else if (emp instanceof Storekeeper sk)    new StorekeeperWindow(sk);
        dispose();
    }
}