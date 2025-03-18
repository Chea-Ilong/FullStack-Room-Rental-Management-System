package GUI;

import DataBase.BuildingDML;
import DataBase.FloorDML;
import Properties.Building;
import Properties.Floor;
import Users.Landlord;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class FloorManagementGUI extends JPanel {
    private Landlord landlord;
    private FloorDML floorDML;
    private BuildingDML buildingDML;
    private JTable floorTable;
    private DefaultTableModel tableModel;
    private JComboBox<String> buildingComboBox;
    private JTextField floorNumberField;
    private Timer refreshTimer;

    public FloorManagementGUI() {
        this.floorDML = new FloorDML();
        this.buildingDML = new BuildingDML();

        setLayout(new BorderLayout());
        initializeUI();

        // Start the auto-refresh timer
//        startAutoRefresh();
    }

    public void setLandlord(Landlord landlord) {
        this.landlord = landlord;
        refreshBuildingList();
        viewFloors(); // Initial load
    }

    private void initializeUI() {
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Create control panel
        JPanel controlPanel = new JPanel(new BorderLayout());

        // Create building selection panel
        JPanel buildingPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buildingPanel.add(new JLabel("Select Building: "));
        buildingComboBox = new JComboBox<>();
        buildingComboBox.addActionListener(e -> viewFloors()); // Refresh floors when building changes
        buildingPanel.add(buildingComboBox);
        controlPanel.add(buildingPanel, BorderLayout.NORTH);

        // Create input panel
        JPanel inputPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        inputPanel.add(new JLabel("Floor Number: "));
        floorNumberField = new JTextField(10);
        inputPanel.add(floorNumberField);

        // Create button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 5)); // Adjusted hgap and vgap
        JButton addButton = new JButton("Add Floor");
        JButton removeButton = new JButton("Remove Floor");
        JButton updateButton = new JButton("Update Floor");

        // Adjust button sizes to fit content
        addButton.setMargin(new Insets(2, 5, 2, 5)); // Reduce internal padding
        removeButton.setMargin(new Insets(2, 5, 2, 5));
        updateButton.setMargin(new Insets(2, 5, 2, 5));

        addButton.addActionListener(e -> addFloor());
        removeButton.addActionListener(e -> removeFloor());
        updateButton.addActionListener(e -> updateFloor());

        buttonPanel.add(addButton);
        buttonPanel.add(removeButton);
        buttonPanel.add(updateButton);

        controlPanel.add(inputPanel, BorderLayout.CENTER);
        controlPanel.add(buttonPanel, BorderLayout.SOUTH);

        add(controlPanel, BorderLayout.NORTH);

        // Create floor table
        String[] columnNames = {"Floor Number", "Building", "Number of Rooms"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        floorTable = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(floorTable);
        add(scrollPane, BorderLayout.CENTER);

        // Create status panel
        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel statusLabel = new JLabel("Ready");
        statusPanel.add(statusLabel);
        add(statusPanel, BorderLayout.SOUTH);
    }

    private void startAutoRefresh() {
        refreshTimer = new Timer();
        refreshTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                SwingUtilities.invokeLater(() -> {
                    refreshBuildingList();
                    viewFloors();
                });
            }
        }, 0, 5000); // Refresh every 5 seconds
    }

    @Override
    public void removeNotify() {
        super.removeNotify();
        if (refreshTimer != null) {
            refreshTimer.cancel(); // Stop timer when panel is removed
        }
    }

    private void refreshBuildingList() {
        buildingComboBox.removeAllItems();
        if (landlord != null) {
            for (Building building : landlord.getBuildings()) {
                buildingComboBox.addItem(building.getName());
            }
        }
    }

    private void addFloor() {
        String buildingName = (String) buildingComboBox.getSelectedItem();
        String floorNumber = floorNumberField.getText().trim();

        if (buildingName == null || buildingName.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select a building.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (floorNumber.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a floor number.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Building building = landlord.getBuildingByName(buildingName);
        if (building != null) {
            int buildingId = buildingDML.getBuildingIdByName(building.getName());
            if (buildingId != -1) {
                Floor newFloor = new Floor(floorNumber);
                boolean success = floorDML.saveFloor(newFloor, buildingId);

                if (success) {
                    landlord.addFloorToBuilding(buildingName, newFloor);
                    JOptionPane.showMessageDialog(this, "Floor added successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                    floorNumberField.setText(""); // Clear input
                    viewFloors(); // Immediate refresh after adding
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to add floor.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(this, "Building not found in database.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(this, "Building not found.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void removeFloor() {
        int selectedRow = floorTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a floor to remove.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String floorNumber = (String) tableModel.getValueAt(selectedRow, 0);
        String buildingName = (String) tableModel.getValueAt(selectedRow, 1);

        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to remove Floor " + floorNumber + " from " + buildingName + "?",
                "Confirm Removal", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            Building building = landlord.getBuildingByName(buildingName);
            if (building != null) {
                int buildingId = buildingDML.getBuildingIdByName(buildingName);
                if (buildingId != -1) {
                    boolean success = floorDML.deleteFloorByBuildingAndNumber(buildingId, floorNumber);

                    if (success) {
                        landlord.removeFloorFromBuilding(buildingName, floorNumber);
                        JOptionPane.showMessageDialog(this, "Floor removed successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                        viewFloors(); // Immediate refresh after removal
                    } else {
                        JOptionPane.showMessageDialog(this, "Failed to remove floor.", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                } else {
                    JOptionPane.showMessageDialog(this, "Building not found in database.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(this, "Building not found.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void viewFloors() {
        String buildingName = (String) buildingComboBox.getSelectedItem();
        if (buildingName == null || buildingName.isEmpty()) {
            tableModel.setRowCount(0); // Clear table if no building selected
            return;
        }

        Building building = landlord.getBuildingByName(buildingName);
        if (building != null) {
            int buildingId = buildingDML.getBuildingIdByName(buildingName);
            if (buildingId != -1) {
                List<Floor> floors = floorDML.getFloorsByBuildingId(buildingId);

                // Clear the table
                tableModel.setRowCount(0);

                // Add floors to the table
                for (Floor floor : floors) {
                    tableModel.addRow(new Object[]{
                            floor.getFloorNumber(),
                            buildingName,
                            floor.getRooms() != null ? floor.getRooms().size() : 0
                    });
                }
            } else {
                tableModel.setRowCount(0);
            }
        } else {
            tableModel.setRowCount(0);
        }
    }

    private void updateFloor() {
        int selectedRow = floorTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a floor to update.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String currentFloorNumber = (String) tableModel.getValueAt(selectedRow, 0);
        String buildingName = (String) tableModel.getValueAt(selectedRow, 1);

        String newFloorNumber = floorNumberField.getText().trim();
        if (newFloorNumber.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a new floor number.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Building building = landlord.getBuildingByName(buildingName);
        if (building != null) {
            int buildingId = buildingDML.getBuildingIdByName(buildingName);
            if (buildingId != -1) {
                int floorId = floorDML.getFloorIdByBuildingAndNumber(buildingId, currentFloorNumber);

                if (floorId != -1) {
                    Floor updatedFloor = new Floor(newFloorNumber);
                    boolean success = floorDML.updateFloor(floorId, updatedFloor);

                    if (success) {
                        JOptionPane.showMessageDialog(this, "Floor updated successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                        floorNumberField.setText(""); // Clear input
                        viewFloors(); // Immediate refresh after update
                    } else {
                        JOptionPane.showMessageDialog(this, "Failed to update floor.", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                } else {
                    JOptionPane.showMessageDialog(this, "Floor not found in database.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(this, "Building not found in database.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(this, "Building not found.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}