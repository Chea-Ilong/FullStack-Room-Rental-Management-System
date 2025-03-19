package GUI;

import Users.Landlord;
import javax.swing.*;
import java.awt.*;

public class LandlordGUI extends JFrame {
    private Landlord landlord;
    private FloorManagementGUI floorManagementGUI;

    public LandlordGUI(Landlord landlord) {
        this.landlord = landlord;

        // Setup main frame
        setTitle("Landlord Management System");
        setSize(900, 650);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Create main panel with tabs
        JTabbedPane tabbedPane = new JTabbedPane();

        // Create GUI components with references
        BuildingManagementGUI buildingManagementGUI = new BuildingManagementGUI();
        FloorManagementGUI floorManagementGUI = new FloorManagementGUI();
        RoomManagementGUI roomManagementGUI = new RoomManagementGUI(landlord);

        // Set landlord in FloorManagementGUI
        floorManagementGUI.setLandlord(landlord);

        // Connect them
        buildingManagementGUI.setFloorManagementGUI(floorManagementGUI);
        buildingManagementGUI.setRoomManagementGUI(roomManagementGUI); // Connect to RoomManagementGUI
        floorManagementGUI.setRoomManagementGUI(roomManagementGUI);

        // Add tabs for each management section
        tabbedPane.addTab("Buildings", buildingManagementGUI);
        tabbedPane.addTab("Floors", floorManagementGUI);
        tabbedPane.addTab("Rooms", roomManagementGUI); // Use the connected instance

        // Tenant and Bill tabs
        tabbedPane.addTab("Tenants", new TenantManagementGUI(landlord));
        tabbedPane.addTab("Bills", createBillPanel());

        // Add tabbed pane to frame
        getContentPane().add(tabbedPane);
    }


    private JPanel createBillPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        // Create button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));

        // Create buttons with icons
        JButton createButton = createButton("Create Bill", "icons/create.png");
        JButton viewMonthButton = createButton("View by Month", "icons/calendar.png");
        JButton viewTenantButton = createButton("View by Tenant", "icons/tenant.png");
        JButton viewUnpaidButton = createButton("View Unpaid", "icons/unpaid.png");
        JButton reportButton = createButton("Generate Report", "icons/report.png");

        // Add action listeners
        createButton.addActionListener(e -> createBill());
        viewMonthButton.addActionListener(e -> viewBillsByMonth());
        viewTenantButton.addActionListener(e -> viewBillsByTenant());
        viewUnpaidButton.addActionListener(e -> viewUnpaidBills());
        reportButton.addActionListener(e -> generateReport());

        // Add buttons to panel
        buttonPanel.add(createButton);
        buttonPanel.add(viewMonthButton);
        buttonPanel.add(viewTenantButton);
        buttonPanel.add(viewUnpaidButton);
        buttonPanel.add(reportButton);

        // Add bill table placeholder
        JTable billTable = new JTable(); // Populate with bill data in real implementation
        JScrollPane scrollPane = new JScrollPane(billTable);

        panel.add(buttonPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    // Helper method to create buttons with icons and ensure text fits
    private JButton createButton(String text, String iconPath) {
        JButton button = new JButton(text, new ImageIcon(iconPath));
        button.setPreferredSize(new Dimension(150, 30)); // Adjust size to fit text and icon
        button.setHorizontalAlignment(SwingConstants.LEFT); // Align text/icon left
        return button;
    }


    private void createBill() {
        JOptionPane.showMessageDialog(this, "Create Bill functionality not implemented yet.");
    }

    private void viewBillsByMonth() {
        JOptionPane.showMessageDialog(this, "View Bills by Month functionality not implemented yet.");
    }

    private void viewBillsByTenant() {
        JOptionPane.showMessageDialog(this, "View Bills by Tenant functionality not implemented yet.");
    }

    private void viewUnpaidBills() {
        JOptionPane.showMessageDialog(this, "View Unpaid Bills functionality not implemented yet.");
    }

    private void generateReport() {
        JOptionPane.showMessageDialog(this, "Generate Report functionality not implemented yet.");
    }

    // Main method for testing

}