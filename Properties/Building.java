package Properties;

import java.util.ArrayList;
import java.util.List;

public class Building {

    // ====================================================================================================
    // Building Information
    // ====================================================================================================
    private String buildingName;
    private List<Floor> floors;
    private String address;

    // ====================================================================================================
    // Constructor
    // ====================================================================================================
    public Building(String buildingName, String address) {
        this.buildingName = buildingName;
        this.floors = new ArrayList<>();
        this.address = address;
    }

    // ====================================================================================================
    // Add/Remove/Update Floors
    // ====================================================================================================
    public void addFloor(Floor floor) {
        if (floor != null && !floors.contains(floor)) {
            floors.add(floor);
            System.out.println("Floor " + floor.getFloorNumber() + " added to Building " + buildingName);
        } else {
            System.out.println("Floor already exists or is invalid.");
        }
    }

    public void removeFloor(String floorNumber) {
        Floor floor = getFloorByNumber(floorNumber);
        if (floor != null) {
            floors.remove(floor);
            System.out.println("Floor " + floorNumber + " removed from Building " + buildingName);
        } else {
            System.out.println("Floor not found in Building " + buildingName);
        }
    }

    // ====================================================================================================
    // Floor Lookup
    // ====================================================================================================
    public Floor getFloorByNumber(String floorNumber) {
        for (Floor floor : floors) {
            if (floor.getFloorNumber().equals(floorNumber)) {
                return floor;
            }
        }
        return null;
    }

    // ====================================================================================================
    // Floor Display
    // ====================================================================================================
    public void displayAllFloors() {
        System.out.println("\n===== All Floors in Building " + buildingName + " =====");
        if (floors.isEmpty()) {
            System.out.println("No floors available in Building " + buildingName);
            return;
        }
        for (Floor floor : floors) {
            System.out.println("Floor Number: " + floor.getFloorNumber());
            floor.displayAllRooms();
            System.out.println("---------------------");
        }
    }

    // ====================================================================================================
    // Getters and Setters
    // ====================================================================================================
    public String getBuildingName() {
        return buildingName;
    }

    public void setBuildingName(String buildingName) {
        this.buildingName = buildingName;
    }

    public List<Floor> getFloors() {
        return floors;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String newAddress) {
        this.address = newAddress;
    }

    public String getName() {
        return buildingName;
    }

    // ====================================================================================================
    // toString Method
    // ====================================================================================================
    @Override
    public String toString() {
        return "Building{" +
                "buildingName='" + buildingName + '\'' +
                ", floors=" + floors +
                ", address='" + address + '\'' +
                '}';
    }
}