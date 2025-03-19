package GUI;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.event.DocumentListener;
import javax.swing.event.DocumentEvent;
import java.awt.*;
import java.util.List;
import DataBase.TenantDML;
import DataBase.LandlordDML;
import DataBase.BuildingDML;
import DataBase.FloorDML;
import DataBase.RoomDML;
import Exceptions.RoomException;
import Exceptions.TenantException;
import Properties.Room;
import Users.Landlord;
import Users.Tenant;

public class TenantManagementGUI extends JPanel {
    private Landlord landlord;
    private JTable tenantTable;
    private DefaultTableModel tableModel;
    private JTextField nameField, idField, contactField, searchField;
    private JButton addButton, updateButton, removeButton, clearButton, refreshButton, assignRoomButton;
    private TenantDML tenantDML;
    private int selectedTenantIndex = -1;
    private String selectedTenantId = null;

    public TenantManagementGUI(Landlord landlord) {
        this.landlord = landlord;
        this.tenantDML = new TenantDML();

        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel formPanel = createFormPanel();
        JPanel tablePanel = createTablePanel();
        JPanel buttonPanel = createButtonPanel();

        add(formPanel, BorderLayout.NORTH);
        add(tablePanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
        landlord.refreshTenants();
        loadTenantsData();
    }

    private JPanel createFormPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
        panel.setBorder(BorderFactory.createTitledBorder("Tenant Details"));

        panel.add(new JLabel("Search:"));
        searchField = new JTextField(15);
        searchField.addActionListener(e -> filterTenants(searchField.getText().trim()));
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            public void changedUpdate(DocumentEvent e) { update(); }
            public void removeUpdate(DocumentEvent e) { update(); }
            public void insertUpdate(DocumentEvent e) { update(); }
            public void update() { filterTenants(searchField.getText().trim()); }
        });
        panel.add(searchField);

        panel.add(new JLabel("Name:"));
        nameField = new JTextField(15);
        panel.add(nameField);

        panel.add(new JLabel("ID:"));
        idField = new JTextField(10);
        panel.add(idField);

        panel.add(new JLabel("Contact:"));
        contactField = new JTextField(15);
        panel.add(contactField);

        return panel;
    }

    private JPanel createTablePanel() {
        String[] columnNames = {"Name", "ID", "Contact", "Room", "Electric Counter", "Water Counter"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tenantTable = new JTable(tableModel);
        tenantTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        tenantTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && tenantTable.getSelectedRow() != -1) {
                selectedTenantIndex = tenantTable.getSelectedRow();
                selectedTenantId = (String) tableModel.getValueAt(selectedTenantIndex, 1);
                Tenant tenant = landlord.getTenantByID(selectedTenantId);
                if (tenant != null) {
                    nameField.setText(tenant.getName());
                    idField.setText(tenant.getIdCard());
                    contactField.setText(tenant.getContact());
                }
                updateButton.setEnabled(true);
                removeButton.setEnabled(true);
                assignRoomButton.setEnabled(true);
            }
        });

        JScrollPane scrollPane = new JScrollPane(tenantTable);

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Tenants"));

        refreshButton = new JButton("Refresh List");
        refreshButton.addActionListener(e -> loadTenantsData());

        JPanel refreshPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        refreshPanel.add(refreshButton);

        panel.add(refreshPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        addButton = new JButton("Add Tenant");
        updateButton = new JButton("Update Tenant");
        removeButton = new JButton("Remove Tenant");
        clearButton = new JButton("Clear Form");
        assignRoomButton = new JButton("Assign Room");

        updateButton.setEnabled(false);
        removeButton.setEnabled(false);
        assignRoomButton.setEnabled(false);

        addButton.addActionListener(e -> addTenant());
        updateButton.addActionListener(e -> updateTenant());
        removeButton.addActionListener(e -> removeTenant());
        clearButton.addActionListener(e -> clearForm());
        assignRoomButton.addActionListener(e -> assignRoom());

        panel.add(addButton);
        panel.add(updateButton);
        panel.add(removeButton);
        panel.add(assignRoomButton);
        panel.add(clearButton);

        return panel;
    }

    private void loadTenantsData() {
        landlord.refreshTenants();
        filterTenants(searchField.getText().trim());
    }

    private void filterTenants(String searchFilter) {
        tableModel.setRowCount(0);
        List<Tenant> tenants = landlord.getTenants();

        for (Tenant tenant : tenants) {
            if (searchFilter == null || searchFilter.isEmpty() ||
                    tenant.getName().toLowerCase().contains(searchFilter.toLowerCase()) ||
                    tenant.getIdCard().toLowerCase().contains(searchFilter.toLowerCase()) ||
                    tenant.getContact().toLowerCase().contains(searchFilter.toLowerCase())) {

                String name = tenant.getName();
                String id = tenant.getIdCard();
                String contact = tenant.getContact();
                String room = "-";
                String electric = "-";
                String water = "-";

                if (tenant.getAssignedRoom() != null) {
                    room = tenant.getAssignedRoom().getRoomNumber();
                    electric = String.valueOf(tenant.getAssignedRoom().getCurrentElectricCounter());
                    water = String.valueOf(tenant.getAssignedRoom().getCurrentWaterCounter());
                }

                tableModel.addRow(new Object[]{name, id, contact, room, electric, water});
            }
        }
    }

    private void addTenant() {
        String name = nameField.getText().trim();
        String id = idField.getText().trim();
        String contact = contactField.getText().trim();

        if (name.isEmpty() || id.isEmpty() || contact.isEmpty()) {
            JOptionPane.showMessageDialog(this, "All fields are required!", "Input Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (tenantDML.tenantExists(id)) {
            JOptionPane.showMessageDialog(this, "Tenant with ID " + id + " already exists.", "Duplicate ID", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Tenant newTenant = new Tenant(name, id, contact);
        if (tenantDML.saveTenant(newTenant)) {
            landlord.addTenant(newTenant);
            landlord.refreshTenants();
            loadTenantsData();
            JOptionPane.showMessageDialog(this, "Tenant added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this, "Failed to add tenant to database.", "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateTenant() {
        if (selectedTenantId == null) {
            JOptionPane.showMessageDialog(this, "Please select a tenant to update", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String name = nameField.getText().trim();
        String id = idField.getText().trim();
        String contact = contactField.getText().trim();

        if (name.isEmpty() || id.isEmpty() || contact.isEmpty()) {
            JOptionPane.showMessageDialog(this, "All fields are required!", "Input Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Tenant originalTenant = landlord.getTenantByID(selectedTenantId);
        if (originalTenant == null) {
            JOptionPane.showMessageDialog(this, "Tenant not found", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        boolean idChanged = !id.equals(selectedTenantId);
        if (idChanged && tenantDML.tenantExists(id)) {
            JOptionPane.showMessageDialog(this, "A tenant with ID " + id + " already exists.", "Duplicate ID", JOptionPane.ERROR_MESSAGE);
            return;
        }

        originalTenant.setName(name);
        originalTenant.setContact(contact);
        if (idChanged) {
            originalTenant.setIdCard(id);
        }

        if (tenantDML.updateTenant(originalTenant, idChanged ? selectedTenantId : null)) {
            landlord.updateTenant(originalTenant);
            landlord.refreshTenants();
            loadTenantsData();
            clearForm();
            JOptionPane.showMessageDialog(this, "Tenant updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this, "Failed to update tenant in database.", "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void removeTenant() {
        if (selectedTenantId == null) {
            JOptionPane.showMessageDialog(this, "Please select a tenant to remove", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to remove tenant with ID: " + selectedTenantId + "?",
                "Confirm Removal",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            if (tenantDML.deleteTenant(selectedTenantId)) {
                try {
                    landlord.removeTenant(selectedTenantId);
                    landlord.refreshTenants();
                    loadTenantsData();
                    clearForm();
                    JOptionPane.showMessageDialog(this, "Tenant removed successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                } catch (TenantException | RoomException ex) {
                    JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Removal Error", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(this, "Failed to remove tenant from database.", "Database Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void assignRoom() {
        if (selectedTenantId == null) {
            JOptionPane.showMessageDialog(this, "Please select a tenant to assign room", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        JPanel assignPanel = new JPanel(new GridLayout(4, 2, 5, 5));

        // Dropdowns for building, floor, and room
        JLabel buildingLabel = new JLabel("Building:");
        JComboBox<String> buildingComboBox = new JComboBox<>();
        JLabel floorLabel = new JLabel("Floor:");
        JComboBox<String> floorComboBox = new JComboBox<>();
        JLabel roomLabel = new JLabel("Room:");
        JComboBox<String> roomComboBox = new JComboBox<>();

        // Populate building dropdown
        BuildingDML buildingDML = new BuildingDML();
        List<String> buildings = buildingDML.getAllBuildingNames();
        buildingComboBox.addItem("Select Building");
        for (String building : buildings) {
            buildingComboBox.addItem(building);
        }

        // Listener to update floors based on selected building
        buildingComboBox.addActionListener(e -> {
            floorComboBox.removeAllItems();
            roomComboBox.removeAllItems();
            String selectedBuilding = (String) buildingComboBox.getSelectedItem();
            if (selectedBuilding != null && !selectedBuilding.equals("Select Building")) {
                FloorDML floorDML = new FloorDML();
                int buildingId = buildingDML.getBuildingIdByName(selectedBuilding);
                // Use getFloorNumbersByBuildingId from your FloorDML
                List<String> floors = floorDML.getFloorNumbersByBuildingId(buildingId);
                floorComboBox.addItem("Select Floor");
                for (String floor : floors) {
                    floorComboBox.addItem(floor);
                }
            }
        });

        // Listener to update rooms based on selected floor
        floorComboBox.addActionListener(e -> {
            roomComboBox.removeAllItems();
            String selectedBuilding = (String) buildingComboBox.getSelectedItem();
            String selectedFloor = (String) floorComboBox.getSelectedItem();
            if (selectedBuilding != null && selectedFloor != null &&
                    !selectedBuilding.equals("Select Building") && !selectedFloor.equals("Select Floor")) {
                RoomDML roomDML = new RoomDML();
                int buildingId = buildingDML.getBuildingIdByName(selectedBuilding);
                int floorId = new FloorDML().getFloorIdByBuildingAndNumber(buildingId, selectedFloor);
                List<RoomDML.RoomDetails> allRooms = roomDML.getAllRoomsWithDetails();
                roomComboBox.addItem("Select Room");
                for (RoomDML.RoomDetails room : allRooms) {
                    if (room.buildingName.equals(selectedBuilding) &&
                            room.floorNumber.equals(selectedFloor) &&
                            !room.isOccupied) { // Only add unoccupied rooms
                        roomComboBox.addItem(room.roomNumber);
                    }
                }
            }
        });

        assignPanel.add(buildingLabel);
        assignPanel.add(buildingComboBox);
        assignPanel.add(floorLabel);
        assignPanel.add(floorComboBox);
        assignPanel.add(roomLabel);
        assignPanel.add(roomComboBox);

        int result = JOptionPane.showConfirmDialog(this, assignPanel,
                "Assign Room to Tenant", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            String building = (String) buildingComboBox.getSelectedItem();
            String floor = (String) floorComboBox.getSelectedItem();
            String room = (String) roomComboBox.getSelectedItem();

            if (building == null || floor == null || room == null ||
                    building.equals("Select Building") || floor.equals("Select Floor") || room.equals("Select Room")) {
                JOptionPane.showMessageDialog(this, "Please select a valid building, floor, and room!", "Input Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            LandlordDML landlordDML = new LandlordDML();
            if (landlordDML.assignRoomToTenant(selectedTenantId, building, floor, room)) {
                landlord.refreshTenants();
                filterTenants(searchField.getText().trim());
                clearForm();
                JOptionPane.showMessageDialog(this, "Room assigned successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Failed to assign room. It might not exist or be occupied.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void clearForm() {
        nameField.setText("");
        idField.setText("");
        contactField.setText("");
        searchField.setText("");
        selectedTenantId = null;
        selectedTenantIndex = -1;
        tenantTable.clearSelection();
        updateButton.setEnabled(false);
        removeButton.setEnabled(false);
        assignRoomButton.setEnabled(false);
    }

    public static void showTenantManagement(Landlord landlord) {
        JFrame frame = new JFrame("Tenant Management");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setSize(800, 600);
        frame.add(new TenantManagementGUI(landlord));
        frame.setVisible(true);
    }
}