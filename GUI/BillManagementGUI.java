package GUI;

import DataBase.BillDML;
import DataBase.BuildingDML;
import DataBase.FloorDML;
import DataBase.RoomDML;
import Payment.Bill;
import Properties.Building;
import Properties.Floor;
import Properties.Room;
import Users.Landlord;
import Users.Tenant;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class BillManagementGUI extends JPanel {
    private Landlord landlord;
    private BillDML billDML;
    private BuildingDML buildingDML;
    private FloorDML floorDML;
    private RoomDML roomDML;

    private JTable billTable;
    private DefaultTableModel tableModel;

    // Form components
    private JComboBox<String> buildingComboBox;
    private JComboBox<String> floorComboBox;
    private JComboBox<String> roomComboBox;
    private JComboBox<String> filterComboBox;
    private JTextField rentAmountField;
    private JTextField electricUsageField;
    private JTextField waterUsageField;
    private JTextField yearField;
    private JTextField monthField;
    private JTextField tenantIdField;

    // Buttons
    private JButton createBillButton;
    private JButton viewBillsButton;
    private JButton markAsPaidButton;
    private JButton deleteBillButton;
    private JButton clearFormButton;
    private JButton generateReportButton;
    private JButton refreshButton;

    public BillManagementGUI() {
        this.billDML = new BillDML();
        this.buildingDML = new BuildingDML();
        this.floorDML = new FloorDML();
        this.roomDML = new RoomDML();

        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        initializeUI();
    }

    public void setLandlord(Landlord landlord) {
        this.landlord = landlord;
        refreshBuildingList();
        refreshFilterOptions();
        viewBills("all", null, null); // Load all bills initially
    }

    private void initializeUI() {
        JPanel formPanel = createFormPanel();
        JPanel tablePanel = createTablePanel();
        JPanel buttonPanel = createButtonPanel();

        add(formPanel, BorderLayout.NORTH);
        add(tablePanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private JPanel createFormPanel() {
        JPanel panel = new JPanel(new GridLayout(3, 1, 5, 5));
        panel.setBorder(BorderFactory.createTitledBorder("Bill Details"));

        // Location panel
        JPanel locationPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
        locationPanel.add(new JLabel("Building:"));
        buildingComboBox = new JComboBox<>();
        buildingComboBox.addActionListener(e -> refreshFloorList());
        locationPanel.add(buildingComboBox);

        locationPanel.add(new JLabel("Floor:"));
        floorComboBox = new JComboBox<>();
        floorComboBox.addActionListener(e -> refreshRoomList());
        locationPanel.add(floorComboBox);

        locationPanel.add(new JLabel("Room:"));
        roomComboBox = new JComboBox<>();
        locationPanel.add(roomComboBox);

        // Billing details panel
        JPanel billingPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
        billingPanel.add(new JLabel("Rent Amount (KHR):"));
        rentAmountField = new JTextField(10);
        billingPanel.add(rentAmountField);

        billingPanel.add(new JLabel("Electric Usage (kWh):"));
        electricUsageField = new JTextField(5);
        billingPanel.add(electricUsageField);

        billingPanel.add(new JLabel("Water Usage (m³):"));
        waterUsageField = new JTextField(5);
        billingPanel.add(waterUsageField);

        // Filter panel
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
        filterPanel.add(new JLabel("Filter:"));
        filterComboBox = new JComboBox<>();
        filterPanel.add(filterComboBox);

        filterPanel.add(new JLabel("Year:"));
        yearField = new JTextField(4);
        filterPanel.add(yearField);

        filterPanel.add(new JLabel("Month:"));
        monthField = new JTextField(2);
        filterPanel.add(monthField);

        filterPanel.add(new JLabel("Tenant ID:"));
        tenantIdField = new JTextField(10);
        filterPanel.add(tenantIdField);

        panel.add(locationPanel);
        panel.add(billingPanel);
        panel.add(filterPanel);

        return panel;
    }

    private JPanel createTablePanel() {
        String[] columnNames = {"Bill ID", "Building", "Floor", "Room", "Tenant ID", "Bill Date",
                "Due Date", "Rent Amount", "Electric", "Water", "Total", "Status"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        billTable = new JTable(tableModel);
        billTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scrollPane = new JScrollPane(billTable);

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Bills"));

        refreshButton = new JButton("Refresh List");
        refreshButton.addActionListener(e -> viewFilteredBills());

        JPanel refreshPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        refreshPanel.add(refreshButton);

        panel.add(refreshPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        createBillButton = new JButton("Create Bill");
        viewBillsButton = new JButton("View Bills");
        markAsPaidButton = new JButton("Mark as Paid");
        deleteBillButton = new JButton("Delete Bill");
        clearFormButton = new JButton("Clear Form");
        generateReportButton = new JButton("Generate Report");

        createBillButton.addActionListener(e -> createBill());
        viewBillsButton.addActionListener(e -> viewFilteredBills());
        markAsPaidButton.addActionListener(e -> markBillAsPaid());
        deleteBillButton.addActionListener(e -> deleteBill());
        clearFormButton.addActionListener(e -> clearForm());
        generateReportButton.addActionListener(e -> generateReport());

        panel.add(createBillButton);
        panel.add(viewBillsButton);
        panel.add(markAsPaidButton);
        panel.add(deleteBillButton);
        panel.add(clearFormButton);
        panel.add(generateReportButton);

        return panel;
    }

    private void refreshBuildingList() {
        String currentSelection = (String) buildingComboBox.getSelectedItem();
        buildingComboBox.removeAllItems();

        if (landlord != null) {
            landlord.refreshBuildings();
            for (Building building : landlord.getBuildings()) {
                buildingComboBox.addItem(building.getBuildingName());
            }
        }

        if (currentSelection != null && buildingComboBox.getItemCount() > 0) {
            for (int i = 0; i < buildingComboBox.getItemCount(); i++) {
                if (currentSelection.equals(buildingComboBox.getItemAt(i))) {
                    buildingComboBox.setSelectedIndex(i);
                    return;
                }
            }
            buildingComboBox.setSelectedIndex(0);
        }

        refreshFloorList();
    }

    private void refreshFloorList() {
        String buildingName = (String) buildingComboBox.getSelectedItem();
        String currentSelection = (String) floorComboBox.getSelectedItem();
        floorComboBox.removeAllItems();

        if (buildingName != null && landlord != null) {
            Building building = landlord.getBuildingByName(buildingName);
            if (building != null) {
                int buildingId = buildingDML.getBuildingIdByName(buildingName);
                if (buildingId != -1) {
                    List<Floor> floors = floorDML.getFloorsByBuildingId(buildingId);
                    for (Floor floor : floors) {
                        floorComboBox.addItem(floor.getFloorNumber());
                    }
                }
            }
        }

        if (currentSelection != null && floorComboBox.getItemCount() > 0) {
            for (int i = 0; i < floorComboBox.getItemCount(); i++) {
                if (currentSelection.equals(floorComboBox.getItemAt(i))) {
                    floorComboBox.setSelectedIndex(i);
                    return;
                }
            }
            floorComboBox.setSelectedIndex(0);
        }

        refreshRoomList();
    }

    private void refreshRoomList() {
        String buildingName = (String) buildingComboBox.getSelectedItem();
        String floorNumber = (String) floorComboBox.getSelectedItem();
        String currentSelection = (String) roomComboBox.getSelectedItem();
        roomComboBox.removeAllItems();

        if (buildingName != null && floorNumber != null && landlord != null) {
            Building building = landlord.getBuildingByName(buildingName);
            if (building != null) {
                Floor floor = building.getFloorByNumber(floorNumber);
                if (floor != null && floor.getRooms() != null) {
                    for (Room room : floor.getRooms()) {
                        roomComboBox.addItem(room.getRoomNumber());
                    }
                }
            }
        }

        if (currentSelection != null && roomComboBox.getItemCount() > 0) {
            for (int i = 0; i < roomComboBox.getItemCount(); i++) {
                if (currentSelection.equals(roomComboBox.getItemAt(i))) {
                    roomComboBox.setSelectedIndex(i);
                    return;
                }
            }
            roomComboBox.setSelectedIndex(0);
        }
    }

    private void refreshFilterOptions() {
        filterComboBox.removeAllItems();
        filterComboBox.addItem("All Bills");
        filterComboBox.addItem("By Month");
        filterComboBox.addItem("By Tenant");
        filterComboBox.addItem("Unpaid Bills");

        // Set default to current year and month
        YearMonth currentYearMonth = YearMonth.now();
        yearField.setText(String.valueOf(currentYearMonth.getYear()));
        monthField.setText(String.valueOf(currentYearMonth.getMonthValue()));
    }

    private void clearForm() {
        rentAmountField.setText("");
        electricUsageField.setText("");
        waterUsageField.setText("");
        billTable.clearSelection();
    }

    private void createBill() {
        String buildingName = (String) buildingComboBox.getSelectedItem();
        String floorNumber = (String) floorComboBox.getSelectedItem();
        String roomNumber = (String) roomComboBox.getSelectedItem();

        if (buildingName == null || floorNumber == null || roomNumber == null) {
            JOptionPane.showMessageDialog(this, "Please select building, floor, and room.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Validate rent amount
        double rentAmount;
        try {
            rentAmount = Double.parseDouble(rentAmountField.getText().trim());
            if (rentAmount <= 0) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Please enter a valid rent amount.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Validate electric usage
        int electricUsage;
        try {
            electricUsage = Integer.parseInt(electricUsageField.getText().trim());
            if (electricUsage < 0) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Please enter a valid electric usage.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Validate water usage
        int waterUsage;
        try {
            waterUsage = Integer.parseInt(waterUsageField.getText().trim());
            if (waterUsage < 0) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Please enter a valid water usage.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Get room
        Room room = null;
        Building building = landlord.getBuildingByName(buildingName);
        if (building != null) {
            Floor floor = building.getFloorByNumber(floorNumber);
            if (floor != null) {
                room = floor.getRoomByNumber(roomNumber);
            }
        }

        if (room == null) {
            JOptionPane.showMessageDialog(this, "Room not found.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Check if room is occupied
        if (!room.isOccupied()) {
            JOptionPane.showMessageDialog(this, "Room " + roomNumber + " is not occupied. Cannot create a bill.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            // Create bill
            Bill bill = new Bill(room, buildingName, floorNumber, rentAmount, electricUsage, waterUsage);

            // Save bill to database
            boolean saved = billDML.saveBill(bill);

            if (saved) {
                JOptionPane.showMessageDialog(this, "Bill created successfully for room " + roomNumber, "Success", JOptionPane.INFORMATION_MESSAGE);
                clearForm();
                viewBills("all", null, null); // Refresh bills view
            } else {
                JOptionPane.showMessageDialog(this, "Failed to create bill for room " + roomNumber, "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error creating bill: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void viewFilteredBills() {
        String filterType = (String) filterComboBox.getSelectedItem();

        if (filterType == null) {
            JOptionPane.showMessageDialog(this, "Please select a filter option.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        switch (filterType) {
            case "All Bills":
                viewBills("all", null, null);
                break;

            case "By Month":
                try {
                    int year = Integer.parseInt(yearField.getText().trim());
                    int month = Integer.parseInt(monthField.getText().trim());

                    if (month < 1 || month > 12) {
                        throw new NumberFormatException();
                    }

                    viewBills("month", year, month);
                } catch (NumberFormatException e) {
                    JOptionPane.showMessageDialog(this, "Please enter valid year and month.", "Error", JOptionPane.ERROR_MESSAGE);
                }
                break;

            case "By Tenant":
                String tenantId = tenantIdField.getText().trim();
                if (tenantId.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Please enter a tenant ID.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                viewBills("tenant", tenantId, null);
                break;

            case "Unpaid Bills":
                viewBills("unpaid", null, null);
                break;
        }
    }

    private void viewBills(String filterType, Object param1, Object param2) {
        tableModel.setRowCount(0); // Clear the table

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        List<Bill> bills = null;

        switch (filterType) {
            case "all":
                bills = billDML.getUnpaidBills();
                break;

            case "month":
                int year = (Integer) param1;
                int month = (Integer) param2;
                bills = billDML.getBillsByMonth(year, month);
                break;

            case "tenant":
                String tenantId = (String) param1;
                bills = billDML.getBillsByTenantId(tenantId);
                break;

            case "unpaid":
                bills = billDML.getUnpaidBills();
                break;
        }

        if (bills != null && !bills.isEmpty()) {
            for (Bill bill : bills) {
                tableModel.addRow(new Object[]{
                        bill.getBillID(),
                        bill.getBuildingName(),
                        bill.getFloorNumber(),
                        bill.getRoom().getRoomNumber(),
                        bill.getTenant() != null ? bill.getTenant().getIdCard() : "N/A",
                        bill.getBillDate().format(formatter),
                        bill.getDueDate().format(formatter),
                        String.format("%.0f KHR", bill.getRentAmount()),
                        String.format("%.0f KHR", bill.getElectricAmount()),
                        String.format("%.0f KHR", bill.getWaterAmount()),
                        String.format("%.0f KHR", bill.getTotalAmount()),
                        bill.isPaid() ? "PAID" : "UNPAID"
                });
            }
        } else {
            JOptionPane.showMessageDialog(this, "No bills found for the selected criteria.", "Information", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void markBillAsPaid() {
        int selectedRow = billTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a bill to mark as paid.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int billID = (Integer) tableModel.getValueAt(selectedRow, 0);
        String status = (String) tableModel.getValueAt(selectedRow, 11);

        if (status.equals("PAID")) {
            JOptionPane.showMessageDialog(this, "This bill is already marked as paid.", "Information", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to mark this bill as paid?", "Confirm Payment", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            boolean success = billDML.markBillAsPaid(billID);

            if (success) {
                JOptionPane.showMessageDialog(this, "Bill marked as paid successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                viewFilteredBills(); // Refresh bills view
            } else {
                JOptionPane.showMessageDialog(this, "Failed to mark bill as paid.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void deleteBill() {
        int selectedRow = billTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a bill to delete.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int billID = (Integer) tableModel.getValueAt(selectedRow, 0);

        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete this bill? This action cannot be undone.", "Confirm Deletion", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            boolean success = billDML.deleteBill(billID);

            if (success) {
                JOptionPane.showMessageDialog(this, "Bill deleted successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                viewFilteredBills(); // Refresh bills view
            } else {
                JOptionPane.showMessageDialog(this, "Failed to delete bill.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void generateReport() {
        String filterType = (String) filterComboBox.getSelectedItem();

        if (filterType == null || !filterType.equals("By Month")) {
            JOptionPane.showMessageDialog(this, "Please select 'By Month' filter to generate a monthly report.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            int year = Integer.parseInt(yearField.getText().trim());
            int month = Integer.parseInt(monthField.getText().trim());

            if (month < 1 || month > 12) {
                throw new NumberFormatException();
            }

            generateMonthlyReport(year, month);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Please enter valid year and month.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void generateMonthlyReport(int year, int month) {
        List<Bill> bills = billDML.getBillsByMonth(year, month);

        if (bills == null || bills.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No bills found for " + month + "/" + year, "Information", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        // Calculate summary statistics
        double totalRent = 0;
        double totalElectric = 0;
        double totalWater = 0;
        double totalAmount = 0;
        double totalPaid = 0;
        double totalUnpaid = 0;
        int paidCount = 0;
        int unpaidCount = 0;

        for (Bill bill : bills) {
            totalRent += bill.getRentAmount();
            totalElectric += bill.getElectricAmount();
            totalWater += bill.getWaterAmount();
            totalAmount += bill.getTotalAmount();

            if (bill.isPaid()) {
                totalPaid += bill.getTotalAmount();
                paidCount++;
            } else {
                totalUnpaid += bill.getTotalAmount();
                unpaidCount++;
            }
        }

        // Build report string
        StringBuilder report = new StringBuilder();
        report.append("===============================================\n");
        report.append("        MONTHLY BILLING REPORT - ").append(month).append("/").append(year).append("\n");
        report.append("===============================================\n\n");
        report.append("Total bills: ").append(bills.size()).append("\n");
        report.append("Paid bills: ").append(paidCount).append("\n");
        report.append("Unpaid bills: ").append(unpaidCount).append("\n\n");
        report.append("Total rent collected: ").append(String.format("%.0f KHR", totalRent)).append("\n");
        report.append("Total electricity charges: ").append(String.format("%.0f KHR", totalElectric)).append("\n");
        report.append("Total water charges: ").append(String.format("%.0f KHR", totalWater)).append("\n");
        report.append("Total amount: ").append(String.format("%.0f KHR", totalAmount)).append("\n\n");
        report.append("Total paid: ").append(String.format("%.0f KHR", totalPaid)).append("\n");
        report.append("Total unpaid: ").append(String.format("%.0f KHR", totalUnpaid)).append("\n");
        report.append("Collection rate: ").append(String.format("%.1f%%",
                totalAmount > 0 ? (totalPaid / totalAmount) * 100 : 0)).append("\n");
        report.append("===============================================\n");

        // Show report in a dialog
        JTextArea textArea = new JTextArea(report.toString());
        textArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 14));
        textArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(500, 400));

        JOptionPane.showMessageDialog(this, scrollPane, "Monthly Report - " + month + "/" + year, JOptionPane.INFORMATION_MESSAGE);
    }

    public void refreshData() {
        refreshBuildingList();
        viewFilteredBills();
    }
}