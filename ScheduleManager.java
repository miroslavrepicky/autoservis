package com.autoservice.ui;

import com.autoservice.AppContext;
import com.autoservice.domain.Customer;
import com.autoservice.ui.shared.UIUtils;

import javax.swing.*;
import java.awt.*;

public class RegistrationWindow extends JDialog {

    private final JTextField firstNameField  = new JTextField(16);
    private final JTextField lastNameField   = new JTextField(16);
    private final JTextField emailField      = new JTextField(16);
    private final JTextField phoneField      = new JTextField(16);
    private final JPasswordField passField   = new JPasswordField(16);
    private final JPasswordField pass2Field  = new JPasswordField(16);

    public RegistrationWindow(Frame owner) {
        super(owner, "Registrácia zákazníka", true);
        buildUI();
        pack();
        setLocationRelativeTo(owner);
        setVisible(true);
    }

    private void buildUI() {
        JPanel root = new JPanel(new BorderLayout(8, 8));
        root.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));

        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(5, 5, 5, 5);
        gc.fill = GridBagConstraints.HORIZONTAL;

        addRow(form, gc, 0, "Meno:", firstNameField);
        addRow(form, gc, 1, "Priezvisko:", lastNameField);
        addRow(form, gc, 2, "Email:", emailField);
        addRow(form, gc, 3, "Telefón:", phoneField);
        addRow(form, gc, 4, "Heslo:", passField);
        addRow(form, gc, 5, "Potvrď heslo:", pass2Field);

        root.add(UIUtils.sectionLabel("Nový zákazník"), BorderLayout.NORTH);
        root.add(form, BorderLayout.CENTER);

        JButton save = UIUtils.primaryButton("Registrovať");
        save.addActionListener(e -> doRegister());
        JPanel south = new JPanel();
        south.add(save);
        root.add(south, BorderLayout.SOUTH);

        setContentPane(root);
    }

    private void addRow(JPanel p, GridBagConstraints gc, int row, String label, JComponent field) {
        gc.gridx = 0; gc.gridy = row; p.add(new JLabel(label), gc);
        gc.gridx = 1;                p.add(field, gc);
    }

    private void doRegister() {
        String first    = firstNameField.getText().trim();
        String last     = lastNameField.getText().trim();
        String email    = emailField.getText().trim();
        String phone    = phoneField.getText().trim();
        String password = new String(passField.getPassword());
        String password2 = new String(pass2Field.getPassword());

        if (first.isEmpty() || last.isEmpty() || email.isEmpty() || phone.isEmpty() || password.isEmpty()) {
            UIUtils.showError(this, "Vyplňte všetky polia.");
            return;
        }
        if (!password.equals(password2)) {
            UIUtils.showError(this, "Heslá sa nezhodujú.");
            return;
        }
        if (password.length() < 4) {
            UIUtils.showError(this, "Heslo musí mať aspoň 4 znaky.");
            return;
        }
        AppContext ctx = AppContext.getInstance();
        if (ctx.getProfileManager().isEmailRegistered(email)) {
            UIUtils.showError(this, "Email je už registrovaný.");
            return;
        }
        Customer customer = Customer.register(first, last, email, phone, password);
        ctx.getProfileManager().saveProfile(customer);
        UIUtils.showInfo(this, "Zákazník " + customer.getFullName() + " bol zaregistrovaný.\nEmail: " + email);
        dispose();
    }
}