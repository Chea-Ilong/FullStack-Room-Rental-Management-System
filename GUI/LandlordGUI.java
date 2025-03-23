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
        floorManagementGUI = new FloorManagementGUI();
        RoomManagementGUI roomManagementGUI = new RoomManagementGUI(landlord);
        TenantManagementGUI tenantManagementGUI = new TenantManagementGUI(landlord);
        BillManagementGUI billManagementGUI = new BillManagementGUI();
        // In the LandlordGUI constructor, after creating the components:
        buildingManagementGUI.setFloorManagementGUI(floorManagementGUI);
        buildingManagementGUI.setRoomManagementGUI(roomManagementGUI);
        buildingManagementGUI.setBillManagementGUI(billManagementGUI); // Add this line
        // Set landlord references
        floorManagementGUI.setLandlord(landlord);
        billManagementGUI.setLandlord(landlord);

        // Connect the components that have existing methods
        buildingManagementGUI.setFloorManagementGUI(floorManagementGUI);
        buildingManagementGUI.setRoomManagementGUI(roomManagementGUI);
        floorManagementGUI.setRoomManagementGUI(roomManagementGUI);

        // Add tabs for each management section
        tabbedPane.addTab("Buildings", buildingManagementGUI);
        tabbedPane.addTab("Floors", floorManagementGUI);
        tabbedPane.addTab("Rooms", roomManagementGUI);
        tabbedPane.addTab("Tenants", tenantManagementGUI);
        tabbedPane.addTab("Bills", billManagementGUI);

        // Add tabbed pane to frame
        getContentPane().add(tabbedPane);
    }
}