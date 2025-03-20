package Properties;

import DataBase.RoomDML;
import Users.Tenant;
import Exceptions.RoomException;


public class Room {

    // ====================================================================================================
    // Room Information
    // ====================================================================================================
    private String roomNumber;
    private boolean isOccupied;
    private Tenant tenant;
    private int currentElectricCounter;
    private int currentWaterCounter;
    private double rent;
    private int floor;

    // ====================================================================================================
    // Constructor
    // ====================================================================================================
    public Room(String roomNumber, int currentElectricCounter, int currentWaterCounter) {
        this.roomNumber = roomNumber;
        this.isOccupied = false;
        this.tenant = null;
        this.currentElectricCounter = Math.max(currentElectricCounter, 0);
        this.currentWaterCounter = Math.max(currentWaterCounter, 0);
    }

    // ====================================================================================================
    // Getters and Setters
    // ====================================================================================================
    public String getRoomNumber() {
        return roomNumber;
    }

    public void setRoomNumber(String roomNumber) {
        this.roomNumber = roomNumber;
    }

    public Tenant getTenant() {
        return tenant;
    }

    public boolean isOccupied() {
        return isOccupied;
    }

    public int getCurrentElectricCounter() {
        return currentElectricCounter;
    }

    public int getCurrentWaterCounter() {
        return currentWaterCounter;
    }

    public double getRent() {
        return rent;
    }

    public int getFloor() {
        return floor;
    }

    public void setFloor(int floor) {
        this.floor = floor;
    }

    // ====================================================================================================
    // Utility Management
    // ====================================================================================================

    public void updateUsage(int newElectricCounter, int newWaterCounter) throws RoomException {
        if (!isOccupied) {
            throw new RoomException("Cannot update usage for a vacant room.");
        }

        if (newElectricCounter < currentElectricCounter || newWaterCounter < currentWaterCounter) {
            throw new RoomException("New counters must be greater than the current counters.");
        }

        if (newElectricCounter == currentElectricCounter && newWaterCounter == currentWaterCounter) {
            System.out.println("No change in usage. No update needed.");
            return;
        }

        currentElectricCounter = newElectricCounter;
        currentWaterCounter = newWaterCounter;
    }

    // ====================================================================================================
    // Tenant Management
    // ====================================================================================================
    public void assignTenant(Tenant tenant) {
        this.tenant = tenant;
        this.isOccupied = (tenant != null);
        if (tenant != null) {
            RoomDML roomDML = new RoomDML();
            roomDML.syncRoomWithTenant(this);
        }
    }

    public void setTenantDirectly(Tenant tenant) {
        this.tenant = tenant;
        this.isOccupied = true;
        try {
            tenant.assignRoom(this);
        } catch (Exception e) {
            System.out.println(" ");
        }
    }

    public void removeTenant() {
        this.tenant = null;
        this.isOccupied = false;
    }

    public void markAsOccupied() throws RoomException {
        if (isOccupied) {
            throw new RoomException("Room " + roomNumber + " is already occupied.");
        } else {
            isOccupied = true;
        }
    }

    // ====================================================================================================
    // toString Method
    // ====================================================================================================
    @Override
    public String toString() {
        return "Room{" +
                "roomNumber='" + roomNumber + '\'' +
                ", isOccupied=" + isOccupied +
                ", tenant=" + tenant +
                ", currentElectricCounter=" + currentElectricCounter +
                ", currentWaterCounter=" + currentWaterCounter +
                '}';
    }
}