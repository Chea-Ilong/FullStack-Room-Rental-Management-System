package Properties;

import java.util.ArrayList;
import java.util.List;

public class Floor {

    // ============================ Floor Information ============================
    private String floorNumber;
    private List<Room> rooms;

    // ============================ Constructor ============================
    public Floor(String floorNumber) {
        this.floorNumber = floorNumber;
        this.rooms = new ArrayList<>();
    }

    // ============================ Add/Remove/Update Rooms ============================
    // Add a room to the floor (Create)
    public void addRoom(Room room) {
        if (room != null && !rooms.contains(room)) {
            rooms.add(room);
            System.out.println("Room " + room.getRoomNumber() + " added to Floor " + floorNumber);
        } else {
            System.out.println("Room already exists or is invalid.");
        }
    }

    // Remove a room from the floor (Delete)
    public void removeRoom(String roomNumber) {
        Room room = getRoomByNumber(roomNumber);
        if (room != null) {
            rooms.remove(room);
            System.out.println("Room " + roomNumber + " removed from Floor " + floorNumber);
        } else {
            System.out.println("Room not found on Floor " + floorNumber);
        }
    }

    // Update room details (Update)
    public void updateRoom(String roomNumber, Room updatedRoom) {
        Room room = getRoomByNumber(roomNumber);
        if (room != null) {
            room.setRoomNumber(updatedRoom.getRoomNumber());

            // Update other room properties as needed
            System.out.println("Room " + roomNumber + " updated on Floor " + floorNumber);
        } else {
            System.out.println("Room not found on Floor " + floorNumber);
        }
    }

    // ============================ Room Lookup ============================
    // Get a room by its room number (Read)
    public Room getRoomByNumber(String roomNumber) {
        for (Room room : rooms) {
            if (room.getRoomNumber().equals(roomNumber)) {
                return room;
            }
        }
        return null; // Room not found
    }

    // ============================ Room Display ============================
    // Display all rooms on the floor (Read)
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

    // ============================ Available and Occupied Rooms ============================
    // Get a list of available (vacant) rooms
    public List<Room> getAvailableRooms() {
        List<Room> availableRooms = new ArrayList<>();
        for (Room room : rooms) {
            if (!room.isOccupied()) {
                availableRooms.add(room);
            }
        }
        return availableRooms;
    }

    // Get a list of occupied rooms
    public List<Room> getOccupiedRooms() {
        List<Room> occupiedRooms = new ArrayList<>();
        for (Room room : rooms) {
            if (room.isOccupied()) {
                occupiedRooms.add(room);
            }
        }
        return occupiedRooms;
    }

    // ============================ Utility Management ============================
    // Reset utility usage for all rooms on the floor
    public void resetUtilityUsageForAllRooms() {
        for (Room room : rooms) {
            room.resetUtilityUsage();
        }
        System.out.println("Utility usage reset for all rooms on Floor " + floorNumber);
    }

    // ============================ Getters/Setters ============================
    // Getter for floor number
    public String getFloorNumber() {
        return floorNumber;
    }

    // Getter for rooms
    public List<Room> getRooms() {
        return rooms;
    }

    // Setter for floor number
    public void setFloorNumber(String newFloorNumber) {
        this.floorNumber = newFloorNumber;
    }
}
