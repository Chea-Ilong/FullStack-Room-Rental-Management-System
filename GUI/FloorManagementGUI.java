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

public class FloorManagementGUI extends JPanel {
    private Landlord landlord;
    private FloorDML floorDML;
    private BuildingDML buildingDML;
    private JTable floorTable;
    private DefaultTableModel tableModel;
    private JComboBox<String> buildingComboBox;
    private JTextField floorNumberField;
    private JButton addButton, removeButton, updateButton, clearButton, refreshButton;
    private RoomManagementGUI roomManagementGUI;

    public FloorManagementGUI() {
        this.floorDML = new FloorDML();
        this.buildingDML = new BuildingDML();

        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        initializeUI();
    }

    public void setRoomManagementGUI(RoomManagementGUI roomManagementGUI) {
        this.roomManagementGUI = roomManagementGUI;
    }

    public void setLandlord(Landlord landlord) {
        this.landlord = landlord;
        refreshBuildingList();
        viewFloors(); // Load all floors initially
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
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
        panel.setBorder(BorderFactory.createTitledBorder("Floor Details"));

        panel.add(new JLabel("Select Building:"));
        buildingComboBox = new JComboBox<>();
        buildingComboBox.addItem("All Buildings");
        buildingComboBox.addActionListener(e -> viewFloors());
        panel.add(buildingComboBox);

        panel.add(new JLabel("Floor Number:"));
        floorNumberField = new JTextField(10);
        panel.add(floorNumberField);

        return panel;
    }

    private JPanel createTablePanel() {
        String[] columnNames = {"Floor Number", "Building", "Number of Rooms"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        floorTable = new JTable(tableModel);
        floorTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scrollPane = new JScrollPane(floorTable);

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Floors"));

        refreshButton = new JButton("Refresh List");
        refreshButton.addActionListener(e -> viewFloors());

        JPanel refreshPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        refreshPanel.add(refreshButton);

        panel.add(refreshPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        addButton = new JButton("Add Floor");
        removeButton = new JButton("Remove Floor");
        updateButton = new JButton("Update Floor");
        clearButton = new JButton("Clear Form");

        addButton.addActionListener(e -> addFloor());
        removeButton.addActionListener(e -> removeFloor());
        updateButton.addActionListener(e -> updateFloor());
        clearButton.addActionListener(e -> clearForm());

        panel.add(addButton);
        panel.add(updateButton);
        panel.add(removeButton);
        panel.add(clearButton);

        return panel;
    }

    private void addFloor() {
        String buildingName = (String) buildingComboBox.getSelectedItem();
        String floorNumber = floorNumberField.getText().trim();

        if (buildingName == null || buildingName.isEmpty() || buildingName.equals("All Buildings")) {
            JOptionPane.showMessageDialog(this, "Please select a specific building.", "Error", JOptionPane.ERROR_MESSAGE);
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
                    viewFloors();
                    if (roomManagementGUI != null) {
                        roomManagementGUI.refreshAfterFloorChanges();
                    }
                    JOptionPane.showMessageDialog(this, "Floor added successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                    clearForm();
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
                        viewFloors();
                        if (roomManagementGUI != null) {
                            roomManagementGUI.refreshAfterFloorChanges();
                        }
                        JOptionPane.showMessageDialog(this, "Floor removed successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                        clearForm();
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
                        viewFloors();
                        if (roomManagementGUI != null) {
                            roomManagementGUI.refreshAfterFloorChanges();
                        }
                        clearForm();
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

    private void clearForm() {
        floorNumberField.setText("");
        floorTable.clearSelection();
    }

    private void refreshBuildingList() {
        String currentSelection = (String) buildingComboBox.getSelectedItem();
        buildingComboBox.removeAllItems();
        buildingComboBox.addItem("All Buildings");

        if (landlord != null) {
            landlord.refreshBuildings();
            for (Building building : landlord.getBuildings()) {
                buildingComboBox.addItem(building.getBuildingName());
            }
        }

        if (currentSelection != null) {
            for (int i = 0; i < buildingComboBox.getItemCount(); i++) {
                if (currentSelection.equals(buildingComboBox.getItemAt(i))) {
                    buildingComboBox.setSelectedIndex(i);
                    return;
                }
            }
        }
        buildingComboBox.setSelectedIndex(0);
    }

    public void refreshAfterBuildingChanges() {
        refreshBuildingList();
        viewFloors();
    }

    private void viewFloors() {
        String buildingName = (String) buildingComboBox.getSelectedItem();

        tableModel.setRowCount(0);

        if (buildingName == null || buildingName.equals("All Buildings")) {
            if (landlord != null) {
                for (Building building : landlord.getBuildings()) {
                    int buildingId = buildingDML.getBuildingIdByName(building.getBuildingName());
                    if (buildingId != -1) {
                        List<Floor> floors = floorDML.getFloorsByBuildingId(buildingId);
                        for (Floor floor : floors) {
                            tableModel.addRow(new Object[]{
                                    floor.getFloorNumber(),
                                    building.getBuildingName(),
                                    floor.getRooms() != null ? floor.getRooms().size() : 0
                            });
                        }
                    }
                }
            }
        } else {
            Building building = landlord.getBuildingByName(buildingName);
            if (building != null) {
                int buildingId = buildingDML.getBuildingIdByName(buildingName);
                if (buildingId != -1) {
                    List<Floor> floors = floorDML.getFloorsByBuildingId(buildingId);
                    for (Floor floor : floors) {
                        tableModel.addRow(new Object[]{
                                floor.getFloorNumber(),
                                buildingName,
                                floor.getRooms() != null ? floor.getRooms().size() : 0
                        });
                    }
                }
            }
        }
    }
}