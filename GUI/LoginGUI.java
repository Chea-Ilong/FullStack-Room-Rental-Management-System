package GUI;

import DataBase.LandlordDML;
import DataBase.TenantDML;
import Users.Landlord;
import Users.Tenant;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class LoginGUI extends JFrame {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JPanel mainPanel;
    private JPanel pinPanel;
    private JPasswordField pinField;
    private Landlord landlord;
    private List<Tenant> tenants;
    private static Tenant currentLoggedInTenant = null;

    public LoginGUI() {
        // Load data from database
        loadDataFromDatabase();

        // Setup the main frame
        setTitle("House Rental System");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Create the main panel with login form
        setupMainPanel();

        // Create the PIN verification panel (initially invisible)
        setupPinPanel();

        // Add main panel to the frame
        getContentPane().add(mainPanel);
    }

    private void loadDataFromDatabase() {
        try {
            // Get the first landlord from database (similar to App.getFirstLandlordIdCard())
            LandlordDML landlordDML = new LandlordDML();
            landlord = landlordDML.getLandlordByIdCard(getFirstLandlordIdCard());

            if (landlord == null) {
                JOptionPane.showMessageDialog(this,
                        "Could not find landlord in database. Please create a landlord first.",
                        "Database Error",
                        JOptionPane.ERROR_MESSAGE);
                System.exit(1);
            }

            System.out.println("Successfully loaded landlord: " + landlord.getName());

            // Load tenants
            TenantDML tenantDML = new TenantDML();
            tenants = tenantDML.getAllTenantsForLandlord();

            if (tenants.isEmpty()) {
                System.out.println("Warning: No tenants found in database.");
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Database error: " + e.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
            System.exit(1);
        }
    }

    // Helper method to get the first landlord ID card from the database
    private String getFirstLandlordIdCard() {
        try {
            DataBase.DataBaseConnection conn = new DataBase.DataBaseConnection();
            java.sql.Connection connection = conn.getConnection();
            java.sql.PreparedStatement ps = connection.prepareStatement(
                    "SELECT u.IdCard FROM Users u JOIN Landlords l ON u.user_id = l.user_id WHERE u.role = 'Landlord' LIMIT 1");
            java.sql.ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                String idCard = rs.getString("IdCard");
                rs.close();
                ps.close();
                connection.close();
                return idCard;
            }

            rs.close();
            ps.close();
            connection.close();
        } catch (java.sql.SQLException e) {
            System.err.println("Error retrieving landlord: " + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }

    private void setupMainPanel() {
        mainPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);

        // Title
        JLabel titleLabel = new JLabel("Welcome to House Rental System");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        mainPanel.add(titleLabel, gbc);

        // Username field
        JLabel usernameLabel = new JLabel("Username:");
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        mainPanel.add(usernameLabel, gbc);

        usernameField = new JTextField(15);
        gbc.gridx = 1;
        gbc.gridy = 1;
        mainPanel.add(usernameField, gbc);

        // Password field
        JLabel passwordLabel = new JLabel("Password:");
        gbc.gridx = 0;
        gbc.gridy = 2;
        mainPanel.add(passwordLabel, gbc);

        passwordField = new JPasswordField(15);
        gbc.gridx = 1;
        gbc.gridy = 2;
        mainPanel.add(passwordField, gbc);

        // Login button
        JButton loginButton = new JButton("Login");
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.NONE;
        mainPanel.add(loginButton, gbc);

        // Exit button
        JButton exitButton = new JButton("Exit");
        gbc.gridy = 4;
        mainPanel.add(exitButton, gbc);

        // Add action listeners
        loginButton.addActionListener(e -> handleLogin());
        exitButton.addActionListener(e -> System.exit(0));
    }

    private void setupPinPanel() {
        pinPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);

        // Title
        JLabel titleLabel = new JLabel("Landlord PIN Verification");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        pinPanel.add(titleLabel, gbc);

        // PIN field
        JLabel pinLabel = new JLabel("Enter PIN:");
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        pinPanel.add(pinLabel, gbc);

        pinField = new JPasswordField(10);
        gbc.gridx = 1;
        gbc.gridy = 1;
        pinPanel.add(pinField, gbc);

        // Verify button
        JButton verifyButton = new JButton("Verify");
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.NONE;
        pinPanel.add(verifyButton, gbc);

        // Cancel button
        JButton cancelButton = new JButton("Cancel");
        gbc.gridy = 3;
        pinPanel.add(cancelButton, gbc);

        // Add action listeners
        verifyButton.addActionListener(e -> verifyPin());
        cancelButton.addActionListener(e -> showMainPanel());
    }

    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword()).trim();

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Please enter both username and password.",
                    "Login Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Check if landlord login (modified to match App's login method)
        if (landlord.getName().equals(username) && landlord.getIdCard().equals(password)) {
            // Show PIN verification for landlord
            showPinPanel();
            return;
        }

        // Check if tenant login
        for (Tenant tenant : tenants) {
            if (tenant.getName().equals(username) && tenant.getIdCard().equals(password)) {
                loginAsTenant(tenant);
                return;
            }
        }

        // Try to find tenant from database (similar to App's approach)
        try {
            TenantDML tenantDML = new TenantDML();
            Tenant databaseTenant = tenantDML.getTenantByCredentials(username, password);
            if (databaseTenant != null) {
                tenants.add(databaseTenant);
                loginAsTenant(databaseTenant);
                return;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Login failed - Offer retry or exit option
        int option = JOptionPane.showConfirmDialog(this,
                "Login failed. Would you like to try again?",
                "Login Failed",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);

        if (option == JOptionPane.NO_OPTION) {
            System.exit(0);
        } else {
            usernameField.setText("");
            passwordField.setText("");
            usernameField.requestFocus();
        }
    }

    private void showPinPanel() {
        getContentPane().remove(mainPanel);
        getContentPane().add(pinPanel);
        pinField.setText("");
        pinField.requestFocus();
        revalidate();
        repaint();
    }

    private void showMainPanel() {
        getContentPane().remove(pinPanel);
        getContentPane().add(mainPanel);
        usernameField.setText("");
        passwordField.setText("");
        usernameField.requestFocus();
        revalidate();
        repaint();
    }

    private void verifyPin() {
        String pin = new String(pinField.getPassword()).trim();

        // Use the LandlordDML class to verify the PIN
        LandlordDML landlordDML = new LandlordDML();
        if (landlordDML.verifyLandlordCredentials(landlord.getIdCard(), pin)) {
            // PIN is correct
            loginAsLandlord();
        } else {
            // PIN is incorrect
            JOptionPane.showMessageDialog(this,
                    "Invalid PIN. Please try again.",
                    "PIN Verification Failed",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loginAsLandlord() {
        JOptionPane.showMessageDialog(this,
                "Login successful as Landlord: " + landlord.getName() + "!",
                "Login Successful",
                JOptionPane.INFORMATION_MESSAGE);

        // Close the login window
        dispose();

        // Open the landlord menu GUI
        SwingUtilities.invokeLater(() -> {
            LandlordGUI landlordGUI = new LandlordGUI(landlord);
            landlordGUI.setVisible(true);
        });
    }

    private void loginAsTenant(Tenant tenant) {
        currentLoggedInTenant = tenant;

        JOptionPane.showMessageDialog(this,
                "Login successful as Tenant: " + tenant.getName() + "!",
                "Login Successful",
                JOptionPane.INFORMATION_MESSAGE);

        // Close the login window
        dispose();

        // Open the tenant menu GUI
        SwingUtilities.invokeLater(() -> {
            MenuGUI.TenantGUI tenantGUI = new MenuGUI.TenantGUI(tenant, landlord);
            tenantGUI.setVisible(true);
        });
    }

    public static void main(String[] args) {
        try {
            // Set system look and feel
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(() -> {
            LoginGUI loginGUI = new LoginGUI();
            loginGUI.setVisible(true);
        });
    }
}