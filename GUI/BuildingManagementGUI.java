package GUI;

import Properties.Building;
import DataBase.BuildingDML;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.Map;

public class BuildingManagementGUI extends JPanel {
    private JTable buildingTable;
    private DefaultTableModel tableModel;
    private BuildingDML buildingDML;
    private JTextField nameField, addressField;
    private JButton addButton, updateButton, deleteButton, clearButton, refreshButton;
    private int selectedBuildingId = -1;
    private FloorManagementGUI floorManagementGUI;
    private RoomManagementGUI roomManagementGUI;

    // In BuildingManagementGUI class, add a field for BillManagementGUI
    private BillManagementGUI billManagementGUI;

    // Add a setter method
    public void setBillManagementGUI(BillManagementGUI billManagementGUI) {
        this.billManagementGUI = billManagementGUI;
    }

// Then, after any building operation (add, update, delete), add:

    public void setFloorManagementGUI(FloorManagementGUI floorManagementGUI) {
        this.floorManagementGUI = floorManagementGUI;
    }

    public void setRoomManagementGUI(RoomManagementGUI roomManagementGUI) {
        this.roomManagementGUI = roomManagementGUI;
    }

    public BuildingManagementGUI() {
        buildingDML = new BuildingDML();
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Create form panel
        JPanel formPanel = createFormPanel();

        // Create table panel
        JPanel tablePanel = createTablePanel();

        // Create button panel
        JPanel buttonPanel = createButtonPanel();

        // Add components to main panel
        add(formPanel, BorderLayout.NORTH);
        add(tablePanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        // Load initial data
        loadBuildingData();
    }

    private JPanel createFormPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
        panel.setBorder(BorderFactory.createTitledBorder("Building Details"));

        panel.add(new JLabel("Building Name:"));
        nameField = new JTextField(15);
        panel.add(nameField);

        panel.add(new JLabel("Address:"));
        addressField = new JTextField(15);
        panel.add(addressField);

        return panel;
    }

    private JPanel createTablePanel() {
        // Create table model with column names
        String[] columnNames = {"ID", "Building Name", "Address"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        buildingTable = new JTable(tableModel);
        buildingTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Add selection listener
        buildingTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && buildingTable.getSelectedRow() != -1) {
                int row = buildingTable.getSelectedRow();
                selectedBuildingId = (int) tableModel.getValueAt(row, 0);
                nameField.setText((String) tableModel.getValueAt(row, 1));
                addressField.setText((String) tableModel.getValueAt(row, 2));
                updateButton.setEnabled(true);
                deleteButton.setEnabled(true);
            }
        });

        JScrollPane scrollPane = new JScrollPane(buildingTable);

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Buildings"));

        // Add refresh button at the top of the table
        refreshButton = new JButton("Refresh List");
        refreshButton.addActionListener(e -> loadBuildingData());

        JPanel refreshPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        refreshPanel.add(refreshButton);

        panel.add(refreshPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        addButton = new JButton("Add Building");
        updateButton = new JButton("Update Building");
        deleteButton = new JButton("Delete Building");
        clearButton = new JButton("Clear Form");

        // Initial state
        updateButton.setEnabled(false);
        deleteButton.setEnabled(false);

        // Add action listeners
        addButton.addActionListener(e -> addBuilding());
        updateButton.addActionListener(e -> updateBuilding());
        deleteButton.addActionListener(e -> deleteBuilding());
        clearButton.addActionListener(e -> clearForm());

        panel.add(addButton);
        panel.add(updateButton);
        panel.add(deleteButton);
        panel.add(clearButton);

        return panel;
    }

    private void addBuilding() {
        String name = nameField.getText().trim();
        String address = addressField.getText().trim();

        if (name.isEmpty() || address.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Building name and address are required",
                    "Validation Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            // Check if a building with the same name already exists
            if (buildingDML.buildingExistsByName(name)) {
                JOptionPane.showMessageDialog(this,
                        "A building with the name '" + name + "' already exists",
                        "Duplicate Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            Building building = new Building(name, address);
            buildingDML.saveBuilding(building);
            loadBuildingData();
            clearForm();
            if (floorManagementGUI != null) {
                floorManagementGUI.refreshAfterBuildingChanges();
            }
            if (roomManagementGUI != null) {
                roomManagementGUI.refreshAfterBuildingChanges();
            }
            if (billManagementGUI != null) {
                billManagementGUI.refreshAfterBuildingChanges();
            }
            JOptionPane.showMessageDialog(this,
                    "Building added successfully",
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Error adding building: " + ex.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
    private void updateBuilding() {
        if (selectedBuildingId == -1) {
            JOptionPane.showMessageDialog(this,
                    "Please select a building to update",
                    "Selection Required",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        String name = nameField.getText().trim();
        String address = addressField.getText().trim();

        if (name.isEmpty() || address.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Building name and address are required",
                    "Validation Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            // Check if another building with the same name exists (excluding the current one)
            if (buildingDML.buildingExistsByNameAndAddress(name, address, selectedBuildingId)) {
                JOptionPane.showMessageDialog(this,
                        "Another building with the name '" + name + "' and address '" + address + "' already exists",
                        "Duplicate Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            Building building = new Building(name, address);
            buildingDML.updateBuilding(selectedBuildingId, building);
            loadBuildingData();
            clearForm(); // Clear form after successful update
            if (floorManagementGUI != null) {
                floorManagementGUI.refreshAfterBuildingChanges();
            }
            if (roomManagementGUI != null) {
                roomManagementGUI.refreshAfterBuildingChanges();
            }
            if (billManagementGUI != null) {
                billManagementGUI.refreshAfterBuildingChanges();
            }
            JOptionPane.showMessageDialog(this,
                    "Building updated successfully",
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Error updating building: " + ex.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteBuilding() {
        if (selectedBuildingId == -1) {
            JOptionPane.showMessageDialog(this,
                    "Please select a building to delete",
                    "Selection Required",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete this building?\nThis will also delete all associated floors and rooms.",
                "Confirm Deletion",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                buildingDML.deleteBuilding(selectedBuildingId);
                loadBuildingData();
                clearForm();
                if (floorManagementGUI != null) {
                    floorManagementGUI.refreshAfterBuildingChanges();
                }
                if (roomManagementGUI != null) {
                    roomManagementGUI.refreshAfterBuildingChanges();
                }
                if (billManagementGUI != null) {
                    billManagementGUI.refreshAfterBuildingChanges();
                }
                JOptionPane.showMessageDialog(this,
                        "Building deleted successfully",
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this,
                        "Error deleting building: " + ex.getMessage() +
                                "\nMake sure there are no floors or rooms associated with this building.",
                        "Database Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void clearForm() {
        nameField.setText("");
        addressField.setText("");
        selectedBuildingId = -1;
        buildingTable.clearSelection();
        updateButton.setEnabled(false);
        deleteButton.setEnabled(false);
    }

    private void loadBuildingData() {
        tableModel.setRowCount(0);
        try {
            List<Map<String, Object>> buildingsWithIds = buildingDML.getAllBuildingsWithIds();
            for (Map<String, Object> buildingData : buildingsWithIds) {
                tableModel.addRow(new Object[]{
                        buildingData.get("id"),
                        buildingData.get("name"),
                        buildingData.get("address")
                });
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Error loading buildings: " + ex.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("Building Management");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);
        frame.add(new BuildingManagementGUI());
        frame.setVisible(true);
    }
}