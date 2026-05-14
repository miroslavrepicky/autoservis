package com.autoservice;

import com.autoservice.ui.LoginWindow;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        // Initialize application context (seeds demo data)
        AppContext.getInstance();
        // Launch login window on EDT
        SwingUtilities.invokeLater(LoginWindow::new);
    }
}
