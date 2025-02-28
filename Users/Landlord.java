package Users;

import Properties.Floor;
import Properties.Room;

import java.util.ArrayList;
import java.util.List;
import java.util.Collections;

public class Landlord extends User {
    private List<Floor> floors;
    private String landlordName;
    private String landlordId;
    private String landlordPhoneNumber;
    private double revenue;

    private static List<Landlord> landlordList = new ArrayList<>();

    public Landlord(String landlordName, String landlordId, String landlordPhoneNumber) {
        super(landlordName, landlordId, String.valueOf(landlordPhoneNumber), "Users.Landlord");
        this.landlordName = landlordName;
        this.landlordId = landlordId;
        this.landlordPhoneNumber = landlordPhoneNumber;
        this.floors = new ArrayList<>();
        this.revenue = 0.0;
        landlordList.add(this); // Add this landlord to the list
    }

    // Getter and Setter

    public void setPassword(String newPassword) {
        this.password = newPassword;
    }

    public static List<Landlord> getLandlordList() {
        return Collections.unmodifiableList(landlordList);
    }

    public String getLandlordName() {
        return landlordName;
    }

    public void setLandlordName(String landlordName) {
        this.landlordName = landlordName;
    }

    public String getLandlordId() {
        return landlordId;
    }

    public void setLandlordId(String landlordId) {
        this.landlordId = landlordId;
    }

    public String getLandlordPhoneNumber() {
        return landlordPhoneNumber;
    }

    public void setLandlordPhoneNumber(String landlordPhoneNumber) {
        this.landlordPhoneNumber = landlordPhoneNumber;
    }

    public static void setLandlordList(List<Landlord> landlordList) {
        Landlord.landlordList = landlordList;
    }

    // Function

    public void addRevenue(double amount) {
        this.revenue += amount;
    }

    public double getRevenue() {
        return this.revenue;
    }

    public void addFloor(int floorNumber) {
        if (findFloorByNumber(floorNumber) != null) {
            System.out.println("Floor " + floorNumber + " already exists.");
            return;
        }
        floors.add(new Floor(floorNumber));
    }

    public void insertRoomToFloor(int floorNumber, Room room) {
        Floor floor = findFloorByNumber(floorNumber);
        if (floor == null) {
            System.out.println("Floor " + floorNumber + " not found.");
            return;
        }
        floor.addRoom(room);
    }

    public void displayReportByMonth(String month) {
        // Placeholder implementation
        System.out.println("Report for month: " + month);
        // Add logic to generate a monthly report
    }

    public void displayAllReport() {
        // Placeholder implementation
        System.out.println("All reports:");
        // Add logic to generate all reports
    }
//
//    public void updateRoom(String roomId, Room updatedRoom) {
//        for (Floor floor : floors) {
//            Room room = floor.findRoomById(roomId);
//            if (room != null) {
//                floor.updateRoom(roomId, updatedRoom);
//                System.out.println("Room " + roomId + " updated successfully.");
//                return;
//            }
//        }
//        System.out.println("Room " + roomId + " not found.");
//    }
//
//    public void searchRoom(String roomId) {
//        for (Floor floor : floors) {
//            Room room = floor.findRoomById(roomId);
//            if (room != null) {
//                System.out.println("Room found: " + room);
//                return;
//            }
//        }
//        System.out.println("Room " + roomId + " not found.");
//    }

    // Helper Function

    private Floor findFloorByNumber(int floorNumber) {
        for (Floor floor : floors) {
            if (floor.getFloorNumber() == floorNumber) {
                return floor;
            }
        }
        return null;
    }
}