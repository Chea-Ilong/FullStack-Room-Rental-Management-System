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

        // Add tabs for each management section
        // Replace the old building panel with our new one
        tabbedPane.addTab("Buildings", new BuildingManagementGUI());

        // Use our new FloorManagementGUI
        floorManagementGUI = new FloorManagementGUI();
        floorManagementGUI.setLandlord(landlord);
        tabbedPane.addTab("Floors", floorManagementGUI);

        // Keep the other tabs as they were
        tabbedPane.addTab("Rooms", createRoomPanel());
        tabbedPane.addTab("Tenants", createTenantPanel());
        tabbedPane.addTab("Bills", createBillPanel());

        getContentPane().add(tabbedPane);
    }

    private JPanel createRoomPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        // Create button panel
        JPanel buttonPanel = new JPanel(new FlowLayout());

        // Create buttons with icons
        JButton addButton = new JButton("Add Room", new ImageIcon("icons/add.png"));
        JButton removeButton = new JButton("Remove Room", new ImageIcon("icons/remove.png"));
        JButton viewDetailsButton = new JButton("View Room Details", new ImageIcon("icons/details.png"));
        JButton viewAllButton = new JButton("View All Rooms", new ImageIcon("icons/view.png"));
        JButton updateButton = new JButton("Update Room", new ImageIcon("icons/update.png"));

        // Add action listeners
        addButton.addActionListener(e -> addRoom());
        removeButton.addActionListener(e -> removeRoom());
        viewDetailsButton.addActionListener(e -> viewRoomDetails());
        viewAllButton.addActionListener(e -> viewAllRooms());
        updateButton.addActionListener(e -> updateRoom());

        // Add buttons to panel
        buttonPanel.add(addButton);
        buttonPanel.add(removeButton);
        buttonPanel.add(viewDetailsButton);
        buttonPanel.add(viewAllButton);
        buttonPanel.add(updateButton);

        // Add room table
        JTable roomTable = new JTable(); // You would populate this with room data
        JScrollPane scrollPane = new JScrollPane(roomTable);

        panel.add(buttonPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createTenantPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        // Create button panel
        JPanel buttonPanel = new JPanel(new FlowLayout());

        // Create buttons with icons
        JButton addButton = new JButton("Add Tenant", new ImageIcon("icons/add.png"));
        JButton removeButton = new JButton("Remove Tenant", new ImageIcon("icons/remove.png"));
        JButton assignButton = new JButton("Assign to Room", new ImageIcon("icons/assign.png"));
        JButton viewDetailsButton = new JButton("View Tenant Details", new ImageIcon("icons/details.png"));
        JButton viewAllButton = new JButton("View All Tenants", new ImageIcon("icons/view.png"));
        JButton updateButton = new JButton("Update Tenant", new ImageIcon("icons/update.png"));

        // Add action listeners
        addButton.addActionListener(e -> addTenant());
        removeButton.addActionListener(e -> removeTenant());
        assignButton.addActionListener(e -> assignTenant());
        viewDetailsButton.addActionListener(e -> viewTenantDetails());
        viewAllButton.addActionListener(e -> viewAllTenants());
        updateButton.addActionListener(e -> updateTenant());

        // Add buttons to panel
        buttonPanel.add(addButton);
        buttonPanel.add(removeButton);
        buttonPanel.add(assignButton);
        buttonPanel.add(viewDetailsButton);
        buttonPanel.add(viewAllButton);
        buttonPanel.add(updateButton);

        // Add tenant table
        JTable tenantTable = new JTable(); // You would populate this with tenant data
        JScrollPane scrollPane = new JScrollPane(tenantTable);

        panel.add(buttonPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createBillPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        // Create button panel
        JPanel buttonPanel = new JPanel(new FlowLayout());

        // Create buttons with icons
        JButton createButton = new JButton("Create Bill", new ImageIcon("icons/create.png"));
        JButton viewMonthButton = new JButton("View Bills by Month", new ImageIcon("icons/calendar.png"));
        JButton viewTenantButton = new JButton("View Bills by Tenant", new ImageIcon("icons/tenant.png"));
        JButton viewUnpaidButton = new JButton("View Unpaid Bills", new ImageIcon("icons/unpaid.png"));
        JButton reportButton = new JButton("Generate Report", new ImageIcon("icons/report.png"));

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

        // Add bill table
        JTable billTable = new JTable(); // You would populate this with bill data
        JScrollPane scrollPane = new JScrollPane(billTable);

        panel.add(buttonPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    // Method stubs for button actions
    private void addRoom() {
        // Implementation for adding a room
    }

    private void removeRoom() {
        // Implementation for removing a room
    }

    private void viewRoomDetails() {
        // Implementation for viewing room details
    }

    private void viewAllRooms() {
        // Implementation for viewing all rooms
    }

    private void updateRoom() {
        // Implementation for updating room information
    }

    private void addTenant() {
        // Implementation for adding a tenant
    }

    private void removeTenant() {
        // Implementation for removing a tenant
    }

    private void assignTenant() {
        // Implementation for assigning a tenant to a room
    }

    private void viewTenantDetails() {
        // Implementation for viewing tenant details
    }

    private void viewAllTenants() {
        // Implementation for viewing all tenants
    }

    private void updateTenant() {
        // Implementation for updating tenant information
    }

    private void createBill() {
        // Implementation for creating a bill
    }

    private void viewBillsByMonth() {
        // Implementation for viewing bills by month
    }

    private void viewBillsByTenant() {
        // Implementation for viewing bills by tenant
    }

    private void viewUnpaidBills() {
        // Implementation for viewing unpaid bills
    }

    private void generateReport() {
        // Implementation for generating a monthly report
    }
}