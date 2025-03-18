package GUI;

import Users.Landlord;
import Users.Tenant;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class MenuGUI {
    // Landlord Menu GUI
    public static class LandlordGUI extends JFrame {
        private Landlord landlord;

        public LandlordGUI(Landlord landlord) {
            this.landlord = landlord;

            // Setup frame
            setTitle("Landlord Menu - " + landlord.getName());
            setSize(400, 400);
            setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            setLocationRelativeTo(null);

            // Create main panel with slight padding
            JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
            mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

            // Welcome header
            JLabel welcomeLabel = new JLabel("Welcome, " + landlord.getName() + "!");
            welcomeLabel.setFont(new Font("Arial", Font.BOLD, 18));
            welcomeLabel.setHorizontalAlignment(SwingConstants.CENTER);
            mainPanel.add(welcomeLabel, BorderLayout.NORTH);

            // Create menu buttons panel
            JPanel buttonPanel = new JPanel(new GridLayout(6, 1, 10, 10));

            // Create buttons for each menu option
            JButton buildingBtn = createMenuButton("Building Management", 1);
            JButton floorBtn = createMenuButton("Floor Management", 2);
            JButton roomBtn = createMenuButton("Room Management", 3);
            JButton tenantBtn = createMenuButton("Tenant Management", 4);
            JButton billBtn = createMenuButton("Bill Management", 5);
            JButton logoutBtn = createMenuButton("Logout", 6);


            // Add buttons to panel
            buttonPanel.add(buildingBtn);
            buttonPanel.add(floorBtn);
            buttonPanel.add(roomBtn);
            buttonPanel.add(tenantBtn);
            buttonPanel.add(billBtn);
            buttonPanel.add(logoutBtn);

            // Add button panel to main panel
            mainPanel.add(buttonPanel, BorderLayout.CENTER);

            // Add status panel at bottom
            JPanel statusPanel = new JPanel(new BorderLayout());
            JLabel statusLabel = new JLabel("Status: Logged in as Landlord");
            statusLabel.setForeground(new Color(0, 128, 0));
            statusPanel.add(statusLabel, BorderLayout.WEST);
            mainPanel.add(statusPanel, BorderLayout.SOUTH);

            // Add main panel to frame
            add(mainPanel);
        }

        private JButton createMenuButton(String text, int optionNumber) {
            JButton button = new JButton(text);
            button.setFont(new Font("Arial", Font.PLAIN, 14));

            // Add action listener
            button.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    handleMenuOption(optionNumber);
                }
            });

            return button;
        }

        private void handleMenuOption(int option) {
            switch (option) {
                case 1:
                    JOptionPane.showMessageDialog(this,
                            "Building Management selected.\nThis feature will be implemented in the full GUI version.",
                            "Building Management",
                            JOptionPane.INFORMATION_MESSAGE);
                    break;

                case 2:
                    JOptionPane.showMessageDialog(this,
                            "Floor Management selected.\nThis feature will be implemented in the full GUI version.",
                            "Floor Management",
                            JOptionPane.INFORMATION_MESSAGE);
                    break;

                case 3:
                    JOptionPane.showMessageDialog(this,
                            "Room Management selected.\nThis feature will be implemented in the full GUI version.",
                            "Room Management",
                            JOptionPane.INFORMATION_MESSAGE);
                    break;

                case 4:
                    JOptionPane.showMessageDialog(this,
                            "Tenant Management selected.\nThis feature will be implemented in the full GUI version.",
                            "Tenant Management",
                            JOptionPane.INFORMATION_MESSAGE);
                    break;

                case 5:
                    JOptionPane.showMessageDialog(this,
                            "Bill Management selected.\nThis feature will be implemented in the full GUI version.",
                            "Bill Management",
                            JOptionPane.INFORMATION_MESSAGE);
                    break;

                case 6: // Logout
                    int confirm = JOptionPane.showConfirmDialog(this,
                            "Are you sure you want to logout?",
                            "Confirm Logout",
                            JOptionPane.YES_NO_OPTION);

                    if (confirm == JOptionPane.YES_OPTION) {
                        dispose(); // Close this window
                        // Return to login screen
                        SwingUtilities.invokeLater(() -> {
                            LoginGUI loginGUI = new LoginGUI();
                            loginGUI.setVisible(true);
                        });
                    }
                    break;
            }
        }
    }

    // Tenant Menu GUI
    public static class TenantGUI extends JFrame {
        private Tenant tenant;
        private Landlord landlord;

        public TenantGUI(Tenant tenant, Landlord landlord) {
            this.tenant = tenant;
            this.landlord = landlord;

            // Setup frame
            setTitle("Tenant Menu - " + tenant.getName());
            setSize(350, 250);
            setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            setLocationRelativeTo(null);

            // Create main panel with slight padding
            JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
            mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

            // Welcome header
            JLabel welcomeLabel = new JLabel("Welcome, " + tenant.getName() + "!");
            welcomeLabel.setFont(new Font("Arial", Font.BOLD, 18));
            welcomeLabel.setHorizontalAlignment(SwingConstants.CENTER);
            mainPanel.add(welcomeLabel, BorderLayout.NORTH);

            // Create menu buttons panel
            JPanel buttonPanel = new JPanel(new GridLayout(2, 1, 10, 10));

            // Create buttons for each menu option
            JButton payBillBtn = createMenuButton("Pay Bill", 1);
            JButton logoutBtn = createMenuButton("Logout", 2);

            // Add buttons to panel
            buttonPanel.add(payBillBtn);
            buttonPanel.add(logoutBtn);

            // Add button panel to main panel
            mainPanel.add(buttonPanel, BorderLayout.CENTER);

            // Add tenant info panel at bottom
            JPanel infoPanel = new JPanel(new BorderLayout());

            // Show tenant information
//            String tenantInfo = "Room: " + (tenant.getRoom() != null ?
//                    tenant.getRoom().getBuilding() + "-" +
//                            tenant.getRoom().getFloor() + "-" +
//                            tenant.getRoom().getNumber() : "Not assigned");
//
//            JLabel infoLabel = new JLabel("Status: Logged in | " + tenantInfo);
//            infoLabel.setForeground(new Color(0, 128, 0));
//            infoPanel.add(infoLabel, BorderLayout.WEST);
//            mainPanel.add(infoPanel, BorderLayout.SOUTH);

            // Add main panel to frame
            add(mainPanel);
        }

        private JButton createMenuButton(String text, int optionNumber) {
            JButton button = new JButton(text);
            button.setFont(new Font("Arial", Font.PLAIN, 14));

            // Add action listener
            button.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    handleMenuOption(optionNumber);
                }
            });

            return button;
        }

        private void handleMenuOption(int option) {
            switch (option) {
                case 1: // Pay Bill
                    JOptionPane.showMessageDialog(this,
                            "Pay Bill selected.\nThis feature will be implemented in the full GUI version.",
                            "Pay Bill",
                            JOptionPane.INFORMATION_MESSAGE);
                    break;

                case 2: // Logout
                    int confirm = JOptionPane.showConfirmDialog(this,
                            "Are you sure you want to logout?",
                            "Confirm Logout",
                            JOptionPane.YES_NO_OPTION);

                    if (confirm == JOptionPane.YES_OPTION) {
                        dispose(); // Close this window
                        // Return to login screen
                        SwingUtilities.invokeLater(() -> {
                            LoginGUI loginGUI = new LoginGUI();
                            loginGUI.setVisible(true);
                        });
                    }
                    break;
            }
        }
    }
}