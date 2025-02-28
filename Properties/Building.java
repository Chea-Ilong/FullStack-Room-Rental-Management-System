//package Properties;
//
//import java.util.ArrayList;
//import java.util.List;
//
//public class Building {
//    private String buildingName;
//    private List<Floor> floors; // List to store floors
//
//    public Building(String buildingName) {
//        this.buildingName = buildingName;
//        this.floors = new ArrayList<>(); // Initialize empty ArrayList
//    }
//
//    public String getBuildingName() {
//        return buildingName;
//    }
//
//    public void setBuildingName(String buildingName) {
//        this.buildingName = buildingName;
//    }
//
//    // Add a floor to the building
//    public void addFloor(int floorNumber) {
//        floors.add(new Floor(floorNumber));
//        System.out.println("Floor " + floorNumber + " added to building " + buildingName);
//    }
//
//    // Remove a floor from the building
//    public void removeFloor(int floorNumber) {
//        Floor floor = searchFloor(floorNumber);
//        if (floor != null && floor.getRooms().isEmpty()) {
//            floors.remove(floor);
//            System.out.println("Floor " + floorNumber + " removed from building " + buildingName);
//        } else {
//            System.out.println("Cannot remove Floor " + floorNumber + " because it contains rooms.");
//        }
//    }
//
//
//    // Update a floor in the building
//    public void updateFloor(int floorNumber, Floor updatedFloor) {
//        for (Floor floor : floors) {
//            if (floor.getFloorNumber() == floorNumber) {
//                floor.setFloorNumber(updatedFloor.getFloorNumber()); // Update floor number if necessary
//                System.out.println("Floor " + floorNumber + " updated in building " + buildingName);
//                return;
//            }
//        }
//        System.out.println("Floor " + floorNumber + " not found in building " + buildingName);
//    }
//
//    // Search for a floor by number
//    public Floor searchFloor(int floorNumber) {
//        for (Floor floor : floors) {
//            if (floor.getFloorNumber() == floorNumber) {
//                return floor;
//            }
//        }
//        return null;
//    }
//
//    // Display all floors in the building
//    public void displayBuildingDetails() {
//        System.out.println("Building: " + buildingName);
//        if (floors.isEmpty()) {
//            System.out.println("No floors available.");
//        } else {
//            for (Floor floor : floors) {
//                System.out.println("Floor " + floor.getFloorNumber() + ":");
//                floor.displayFloorDetails();
//            }
//        }
//    }
//
//}
//
