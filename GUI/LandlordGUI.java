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

        BillManagementGUI billManagementGUI = new BillManagementGUI();
        billManagementGUI.setLandlord(landlord);

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
        tabbedPane.addTab("Bills", billManagementGUI); // Use BillManagementGUI instead of createBillPanel()

        // Add tabbed pane to frame
        getContentPane().add(tabbedPane);
    }
}