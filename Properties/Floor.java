package Properties;

import Exceptions.RoomException;
import java.util.ArrayList;
import java.util.List;

public class Floor {

    // ====================================================================================================
    // Floor Information
    // ====================================================================================================
    private String floorNumber;
    private List<Room> rooms;
    private Building building;

    // ====================================================================================================
    // Constructor
    // ====================================================================================================
    public Floor(String floorNumber) {
        this.floorNumber = floorNumber;
        this.rooms = new ArrayList<>();
    }

    // ====================================================================================================
    // Add/Remove/Update Rooms
    // ====================================================================================================
    public void addRoom(Room room) {
        if (room != null && !rooms.contains(room)) {
            rooms.add(room);
            System.out.println("Room " + room.getRoomNumber() + " added to Floor " + floorNumber);
        } else {
            System.out.println("Room already exists or is invalid.");
        }
    }

    // ====================================================================================================
    // Room Lookup
    // ====================================================================================================
    public Room getRoomByNumber(String roomNumber) {
        for (Room room : rooms) {
            if (room.getRoomNumber().equals(roomNumber)) {
                return room;
            }
        }
        return null;
    }

    // ====================================================================================================
    // Room Display
    // ====================================================================================================
    public void displayAllRooms() {
        System.out.println("\n===== All Rooms on Floor " + floorNumber + " =====");
        if (rooms.isEmpty()) {
            System.out.println("No rooms available on Floor " + floorNumber);
            return;
        }
        for (Room room : rooms) {
            System.out.println(room.toString());
            System.out.println("---------------------");
        }
    }

    // ====================================================================================================
    // Getters and Setters
    // ====================================================================================================
    public String getFloorNumber() {
        return floorNumber;
    }

    public void setFloorNumber(String newFloorNumber) {
        this.floorNumber = newFloorNumber;
    }

    public List<Room> getRooms() {
        return rooms;
    }

    public Building getBuilding() {
        return this.building;
    }
}