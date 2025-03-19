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

    public void setFloorManagementGUI(FloorManagementGUI floorManagementGUI) {
        this.floorManagementGUI = floorManagementGUI;
    }

    public void setRoomManagementGUI(RoomManagementGUI roomManagementGUI) {
        this.roomManagementGUI = roomManagementGUI;
    }

    public BuildingManagementGUI() {
        buildingDML = new BuildingDML();
        setLayout(new BorderLayout(15, 15)); // Increased spacing
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15)); // Increased padding

        // Set larger default font for the entire panel
        setFont(new Font("SansSerif", Font.PLAIN, 16));

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
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Building Details"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8); // Increased insets
        gbc.anchor = GridBagConstraints.WEST;

        // Larger font for labels
        Font labelFont = new Font("SansSerif", Font.PLAIN, 16);

        // Building Name components
        gbc.gridx = 0;
        gbc.gridy = 0;
        JLabel nameLabel = new JLabel("Building Name:");
        nameLabel.setFont(labelFont);
        panel.add(nameLabel, gbc);

        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        nameField = new JTextField(20);
        nameField.setFont(new Font("SansSerif", Font.PLAIN, 16)); // Larger font
        nameField.setPreferredSize(new Dimension(250, 35)); // Slightly larger size
        panel.add(nameField, gbc);

        // Building Address components
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0.0;
        gbc.fill = GridBagConstraints.NONE;
        JLabel addressLabel = new JLabel("Address:");
        addressLabel.setFont(labelFont);
        panel.add(addressLabel, gbc);

        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        addressField = new JTextField(20);
        addressField.setFont(new Font("SansSerif", Font.PLAIN, 16)); // Larger font
        addressField.setPreferredSize(new Dimension(250, 35)); // Slightly larger size
        panel.add(addressField, gbc);

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
        buildingTable.setFont(new Font("SansSerif", Font.PLAIN, 16)); // Larger font for table
        buildingTable.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 16)); // Larger header font
        buildingTable.setRowHeight(30); // Increased row height
        buildingTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        buildingTable.getColumnModel().getColumn(0).setMaxWidth(60); // Slightly wider ID column

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
        refreshButton.setFont(new Font("SansSerif", Font.PLAIN, 16)); // Larger font
        refreshButton.setPreferredSize(new Dimension(150, 35)); // Slightly larger button
        refreshButton.addActionListener(e -> loadBuildingData());

        JPanel refreshPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        refreshPanel.add(refreshButton);

        panel.add(refreshPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10)); // Increased spacing

        Font buttonFont = new Font("SansSerif", Font.PLAIN, 16); // Larger font for buttons
        Dimension buttonSize = new Dimension(150, 35); // Slightly larger button size

        addButton = new JButton("Add Building");
        updateButton = new JButton("Update Building");
        deleteButton = new JButton("Delete Building");
        clearButton = new JButton("Clear Form");

        // Apply font and size to buttons
        for (JButton button : new JButton[]{addButton, updateButton, deleteButton, clearButton}) {
            button.setFont(buttonFont);
            button.setPreferredSize(buttonSize);
        }

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
            Building building = new Building(name, address);
            buildingDML.updateBuilding(selectedBuildingId, building);
            loadBuildingData();
            if (floorManagementGUI != null) {
                floorManagementGUI.refreshAfterBuildingChanges();
            }
            if (roomManagementGUI != null) {
                roomManagementGUI.refreshAfterBuildingChanges();
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
        frame.setSize(900, 700); // Slightly larger window
        frame.add(new BuildingManagementGUI());
        frame.setVisible(true);
    }
}