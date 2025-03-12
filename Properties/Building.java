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

    public Building(String buildingName) {
        this.buildingName = buildingName;
    }

    // ====================================================================================================
    // Add/Remove/Update Floors
    // ====================================================================================================
    // Create: Add a floor to the building
    public void addFloor(Floor floor) {
        if (floor != null && !floors.contains(floor)) {
            floors.add(floor);
            System.out.println("Floor " + floor.getFloorNumber() + " added to Building " + buildingName);
        } else {
            System.out.println("Floor already exists or is invalid.");
        }
    }

    // Delete: Remove a floor from the building
    public void removeFloor(String floorNumber) {
        Floor floor = getFloorByNumber(floorNumber);
        if (floor != null) {
            floors.remove(floor);
            System.out.println("Floor " + floorNumber + " removed from Building " + buildingName);
        } else {
            System.out.println("Floor not found in Building " + buildingName);
        }
    }

    // Update: Update floor details (e.g., floor number)
    public void updateFloor(String floorNumber, String newFloorNumber) {
        Floor floor = getFloorByNumber(floorNumber);
        if (floor != null) {
            floor.setFloorNumber(newFloorNumber);
            System.out.println("Floor " + floorNumber + " updated to Floor " + newFloorNumber + " in Building " + buildingName);
        } else {
            System.out.println("Floor not found in Building " + buildingName);
        }
    }

    // ====================================================================================================
    // Floor Lookup
    // ====================================================================================================
    // Read: Get a floor by its floor number
    public Floor getFloorByNumber(String floorNumber) {
        for (Floor floor : floors) {
            if (floor.getFloorNumber().equals(floorNumber)) {
                return floor;
            }
        }
        return null; // Floor not found
    }

    // ====================================================================================================
    // Floor Display
    // ====================================================================================================
    // Read: Display all floors in the building
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
    // Getters/Setters
    // ====================================================================================================
    // Getter for building name
    public String getBuildingName() {
        return buildingName;
    }

    // Getter for floors
    public List<Floor> getFloors() {
        return floors;
    }

    // Setter for building name
    public void setBuildingName(String buildingName) {
        this.buildingName = buildingName;
    }

    // Getter for address
    public String getAddress() {
        return address;
    }

    // Setter for address
    public void setAddress(String newAddress) {
        this.address = newAddress;
    }

    // Method to get room by its room number across all floors
    public Room getRoomByNumber(String roomNumber) {
        for (Floor floor : floors) {
            for (Room room : floor.getRooms()) {
                if (room.getRoomNumber().equals(roomNumber)) {
                    return room; // Return the room if the room number matches
                }
            }
        }
        return null; // Room not found
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
