package Properties;

import java.util.ArrayList;
import java.util.List;

public class Building {
    private String buildingName;
    private List<Floor> floors; // List to store floors

    public Building(String buildingName) {
        this.buildingName = buildingName;
        this.floors = new ArrayList<>(); // Initialize empty ArrayList
    }

    public String getBuildingName() {
        return buildingName;
    }

    public void setBuildingName(String buildingName) {
        this.buildingName = buildingName;
    }

    // Add a floor to the building
    public void addFloor(int floorNumber) {
        floors.add(new Floor(floorNumber));
        System.out.println("Floor " + floorNumber + " added to building " + buildingName);
    }

    // Remove a floor from the building
    public void removeFloor(int floorNumber) {
        floors.removeIf(floor -> floor.getFloorNumber() == floorNumber);
        System.out.println("Floor " + floorNumber + " removed from building " + buildingName);
    }

    // Update a floor in the building
    public void updateFloor(int floorNumber, Floor updatedFloor) {
        for (int i = 0; i < floors.size(); i++) {
            if (floors.get(i).getFloorNumber() == floorNumber) {
                floors.set(i, updatedFloor);
                System.out.println("Floor " + floorNumber + " updated in building " + buildingName);
                return;
            }
        }
        System.out.println("Floor " + floorNumber + " not found in building " + buildingName);
    }

    // Search for a floor by number
    public Floor searchFloor(int floorNumber) {
        for (Floor floor : floors) {
            if (floor.getFloorNumber() == floorNumber) {
                return floor;
            }
        }
        return null;
    }

    // Display all floors in the building
    public void displayBuildingDetails() {
        System.out.println("Building: " + buildingName);
        for (Floor floor : floors) {
            System.out.println("Floor " + floor.getFloorNumber() + ":");
            floor.displayFloorDetails();
        }
    }
}

