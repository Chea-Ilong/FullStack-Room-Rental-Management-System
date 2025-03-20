//package GUI;
//
//import DataBase.BillDML;
//import Users.Landlord;
//import Users.Tenant;
//import Property.Bill;
//
//import javax.swing.*;
//import javax.swing.table.DefaultTableModel;
//import java.awt.*;
//import java.util.List;
//
//public class TenantGUI extends JFrame {
//    private Tenant tenant;
//    private Landlord landlord;
//    private JTable billsTable;
//    private DefaultTableModel billsTableModel;
//
//    public TenantGUI(Tenant tenant, Landlord landlord) {
//        this.tenant = tenant;
//        this.landlord = landlord;
//
//        // Setup frame
//        setTitle("Tenant Portal - " + tenant.getName());
//        setSize(800, 600);
//        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//        setLocationRelativeTo(null);
//
//        // Create tabbed pane for different sections
//        JTabbedPane tabbedPane = new JTabbedPane();
//
//        // Add tabs
//        tabbedPane.addTab("Dashboard", createDashboardPanel());
//        tabbedPane.addTab("Bills", createBillsPanel());
//        tabbedPane.addTab("Profile", createProfilePanel());
//
//        // Add logout button at bottom
//        JPanel mainPanel = new JPanel(new BorderLayout());
//        mainPanel.add(tabbedPane, BorderLayout.CENTER);
//        mainPanel.add(createLogoutPanel(), BorderLayout.SOUTH);
//
//        // Add to frame
//        add(mainPanel);
//    }
//
//    private JPanel createDashboardPanel() {
//        JPanel panel = new JPanel(new BorderLayout(10, 10));
//        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
//
//        // Welcome header
//        JPanel headerPanel = new JPanel(new BorderLayout());
//        JLabel welcomeLabel = new JLabel("Welcome, " + tenant.getName() + "!");
//        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 24));
//        headerPanel.add(welcomeLabel, BorderLayout.NORTH);
//
//        // Sub-header with tenant information
//        JLabel infoLabel = new JLabel("Tenant ID: " + tenant.getIdCard());
//        infoLabel.setFont(new Font("Arial", Font.ITALIC, 14));
//        headerPanel.add(infoLabel, BorderLayout.SOUTH);
//
//        panel.add(headerPanel, BorderLayout.NORTH);
//
//        // Quick summary panel (center)
//        JPanel summaryPanel = new JPanel(new GridLayout(3, 1, 10, 10));
//        summaryPanel.setBorder(BorderFactory.createTitledBorder("Account Summary"));
//
//        // Would normally fetch these from the database
//        int pendingBills = 2; // Placeholder - replace with actual data
//        double totalDue = 1250.00; // Placeholder - replace with actual data
//        String roomInfo = "Building A, Floor 2, Room 205"; // Placeholder - replace with actual data
//
//        summaryPanel.add(new JLabel("Pending Bills: " + pendingBills));
//        summaryPanel.add(new JLabel("Total Amount Due: $" + totalDue));
//        summaryPanel.add(new JLabel("Room: " + roomInfo));
//
//        panel.add(summaryPanel, BorderLayout.CENTER);
//
//        // Quick actions panel (bottom)
//        JPanel actionsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
//        actionsPanel.setBorder(BorderFactory.createTitledBorder("Quick Actions"));
//
//        JButton payBillsButton = new JButton("Pay Bills");
//        JButton contactLandlordButton = new JButton("Contact Landlord");
//        JButton maintenanceRequestButton = new JButton("Submit Maintenance Request");
//
//        // Add action listeners
//        payBillsButton.addActionListener(e -> {
//            // Switch to bills tab and maybe highlight pay button
//            JOptionPane.showMessageDialog(this, "Pay Bills feature coming soon!");
//        });
//
//        contactLandlordButton.addActionListener(e -> {
//            JOptionPane.showMessageDialog(this,
//                    "Landlord Contact Information:\nName: " + landlord.getName() +
//                            "\nPhone: " + landlord.getPhone() + "\nEmail: [landlord email]");
//        });
//
//        maintenanceRequestButton.addActionListener(e -> {
//            JOptionPane.showMessageDialog(this, "Maintenance Request feature coming soon!");
//        });
//
//        actionsPanel.add(payBillsButton);
//        actionsPanel.add(contactLandlordButton);
//        actionsPanel.add(maintenanceRequestButton);
//
//        panel.add(actionsPanel, BorderLayout.SOUTH);
//
//        return panel;
//    }
//
//    private JPanel createBillsPanel() {
//        JPanel panel = new JPanel(new BorderLayout(10, 10));
//        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
//
//        // Table for bills
//        String[] columns = {"Bill ID", "Date", "Amount", "Due Date", "Status", "Description"};
//        billsTableModel = new DefaultTableModel(columns, 0) {
//            @Override
//            public boolean isCellEditable(int row, int column) {
//                return false; // Make table non-editable
//            }
//        };
//
//        billsTable = new JTable(billsTableModel);
//        JScrollPane scrollPane = new JScrollPane(billsTable);
//        panel.add(scrollPane, BorderLayout.CENTER);
//
//        // Load bills data - You would replace this with actual database queries
//        loadBillsData();
//
//        // Buttons panel
//        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
//        JButton refreshButton = new JButton("Refresh");
//        JButton paySelectedButton = new JButton("Pay Selected");
//        JButton viewDetailsButton = new JButton("View Details");
//
//        refreshButton.addActionListener(e -> loadBillsData());
//
//        paySelectedButton.addActionListener(e -> {
//            int selectedRow = billsTable.getSelectedRow();
//            if (selectedRow == -1) {
//                JOptionPane.showMessageDialog(this,
//                        "Please select a bill to pay.",
//                        "No Selection",
//                        JOptionPane.WARNING_MESSAGE);
//                return;
//            }
//
//            String status = (String) billsTableModel.getValueAt(selectedRow, 4);
//            if ("Paid".equals(status)) {
//                JOptionPane.showMessageDialog(this,
//                        "This bill has already been paid.",
//                        "Payment Error",
//                        JOptionPane.INFORMATION_MESSAGE);
//                return;
//            }
//
//            // Process payment
//            JOptionPane.showMessageDialog(this,
//                    "Payment processing will be implemented in the full version.",
//                    "Payment Processing",
//                    JOptionPane.INFORMATION_MESSAGE);
//        });
//
//        viewDetailsButton.addActionListener(e -> {
//            int selectedRow = billsTable.getSelectedRow();
//            if (selectedRow == -1) {
//                JOptionPane.showMessageDialog(this,
//                        "Please select a bill to view.",
//                        "No Selection",
//                        JOptionPane.WARNING_MESSAGE);
//                return;
//            }
//
//            // Show bill details
//            String billId = (String) billsTableModel.getValueAt(selectedRow, 0);
//            JOptionPane.showMessageDialog(this,
//                    "Bill details for Bill #" + billId + " will be shown here.",
//                    "Bill Details",
//                    JOptionPane.INFORMATION_MESSAGE);
//        });
//
//        buttonsPanel.add(refreshButton);
//        buttonsPanel.add(viewDetailsButton);
//        buttonsPanel.add(paySelectedButton);
//
//        panel.add(buttonsPanel, BorderLayout.SOUTH);
//
//        return panel;
//    }
//
//    private void loadBillsData() {
//        // Clear existing data
//        billsTableModel.setRowCount(0);
//
//        try {
//            // Get bills from database using BillDML
//            BillDML billDML = new BillDML();
//            List<Bill> bills = billDML.getBillsForTenant(tenant.getIdCard());
//
//            // Add bills to table
//            for (Bill bill : bills) {
//                billsTableModel.addRow(new Object[]{
//                        bill.getBillId(),
//                        bill.getDate(),
//                        bill.getAmount(),
//                        bill.getDueDate(),
//                        bill.isPaid() ? "Paid" : "Unpaid",
//                        bill.getDescription()
//                });
//            }
//
//            // If no bills were found, add sample data for demo
//            if (bills.isEmpty()) {
//                // Add sample data for demonstration
//                billsTableModel.addRow(new Object[]{"B001", "2025-03-01", "$850.00", "2025-03-15", "Unpaid", "March Rent"});
//                billsTableModel.addRow(new Object[]{"B002", "2025-03-01", "$50.00", "2025-03-15", "Unpaid", "Water Bill"});
//                billsTableModel.addRow(new Object[]{"B003", "2025-02-01", "$850.00", "2025-02-15", "Paid", "February Rent"});
//            }
//        } catch (Exception e) {
//            JOptionPane.showMessageDialog(this,
//                    "Error loading bills: " + e.getMessage(),
//                    "Database Error",
//                    JOptionPane.ERROR_MESSAGE);
//            e.printStackTrace();
//        }
//    }
//
//    private JPanel createProfilePanel() {
//        JPanel panel = new JPanel(new BorderLayout(10, 10));
//        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
//
//        // Profile form
//        JPanel formPanel = new JPanel(new GridLayout(5, 2, 10, 10));
//        formPanel.setBorder(BorderFactory.createTitledBorder("Personal Information"));
//
//        // Name field
//        formPanel.add(new JLabel("Name:"));
//        JTextField nameField = new JTextField(tenant.getName());
//        nameField.setEditable(false);
//        formPanel.add(nameField);
//
//        // ID Card field
//        formPanel.add(new JLabel("ID Card:"));
//        JTextField idCardField = new JTextField(tenant.getIdCard());
//        idCardField.setEditable(false);
//        formPanel.add(idCardField);
//
//        // Phone field
//        formPanel.add(new JLabel("Phone:"));
//        JTextField phoneField = new JTextField(tenant.getContact());
//        formPanel.add(phoneField);
//
//        // Email field
//        formPanel.add(new JLabel("Email:"));
//        JTextField emailField = new JTextField("tenant@example.com"); // Replace with actual data
//        formPanel.add(emailField);
//
//        // Lease details
//        formPanel.add(new JLabel("Lease Expiry:"));
//        JTextField leaseField = new JTextField("2025-12-31"); // Replace with actual data
//        leaseField.setEditable(false);
//        formPanel.add(leaseField);
//
//        panel.add(formPanel, BorderLayout.NORTH);
//
//        // Change password panel
//        JPanel passwordPanel = new JPanel(new GridLayout(3, 2, 10, 10));
//        passwordPanel.setBorder(BorderFactory.createTitledBorder("Change Password"));
//
//        passwordPanel.add(new JLabel("Current Password:"));
//        JPasswordField currentPasswordField = new JPasswordField();
//        passwordPanel.add(currentPasswordField);
//
//        passwordPanel.add(new JLabel("New Password:"));
//        JPasswordField newPasswordField = new JPasswordField();
//        passwordPanel.add(newPasswordField);
//
//        passwordPanel.add(new JLabel("Confirm New Password:"));
//        JPasswordField confirmPasswordField = new JPasswordField();
//        passwordPanel.add(confirmPasswordField);
//
//        panel.add(passwordPanel, BorderLayout.CENTER);
//
//        // Save button
//        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
//        JButton saveButton = new JButton("Save Changes");
//
//        saveButton.addActionListener(e -> {
//            // Validate password change
//            String currentPassword = new String(currentPasswordField.getPassword());
//            String newPassword = new String(newPasswordField.getPassword());
//            String confirmPassword = new String(confirmPasswordField.getPassword());
//
//            if (!currentPassword.isEmpty() || !newPassword.isEmpty() || !confirmPassword.isEmpty()) {
//                if (currentPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
//                    JOptionPane.showMessageDialog(this,
//                            "All password fields must be filled to change password.",
//                            "Validation Error",
//                            JOptionPane.WARNING_MESSAGE);
//                    return;
//                }
//
//                if (!newPassword.equals(confirmPassword)) {
//                    JOptionPane.showMessageDialog(this,
//                            "New password and confirmation do not match.",
//                            "Validation Error",
//                            JOptionPane.WARNING_MESSAGE);
//                    return;
//                }
//
//                // Implement password change
//                JOptionPane.showMessageDialog(this,
//                        "Password change will be implemented in the full version.",
//                        "Password Change",
//                        JOptionPane.INFORMATION_MESSAGE);
//            }
//
//            // Update profile info
//            tenant.get(phoneField.getText());
//            // Would also update email in a full implementation
//
//            JOptionPane.showMessageDialog(this,
//                    "Profile updated successfully!",
//                    "Profile Update",
//                    JOptionPane.INFORMATION_MESSAGE);
//        });
//
//        buttonPanel.add(saveButton);
//        panel.add(buttonPanel, BorderLayout.SOUTH);
//
//        return panel;
//    }
//
//    private JPanel createLogoutPanel() {
//        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
//        panel.setBorder(BorderFactory.createEmptyBorder(5, 20, 10, 20));
//
//        JLabel statusLabel = new JLabel("Logged in as: " + tenant.getName());
//        statusLabel.setForeground(new Color(0, 128, 0));
//        panel.add(statusLabel);
//
//        panel.add(Box.createHorizontalStrut(20)); // Add space
//
//        JButton logoutButton = new JButton("Logout");
//        logoutButton.addActionListener(e -> {
//            int confirm = JOptionPane.showConfirmDialog(this,
//                    "Are you sure you want to logout?",
//                    "Confirm Logout",
//                    JOptionPane.YES_NO_OPTION);
//
//            if (confirm == JOptionPane.YES_OPTION) {
//                dispose(); // Close this window
//                // Return to login screen
//                SwingUtilities.invokeLater(() -> {
//                    LoginGUI loginGUI = new LoginGUI();
//                    loginGUI.setVisible(true);
//                });
//            }
//        });
//
//        panel.add(logoutButton);
//
//        return panel;
//    }
//}