package Properties;

import DataBase.RoomDML;
import Users.Tenant;
import Exceptions.RoomException;
import java.time.LocalDate;

public class Room {

    // ====================================================================================================
    // Room Information
    // ====================================================================================================
    private String roomNumber;
    private boolean isOccupied;
    private Tenant tenant;
    private int currentElectricCounter;
    private int currentWaterCounter;

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

    // ====================================================================================================
    // Utility Management
    // ====================================================================================================

    // Reset utility counters
    void resetUtilityUsage() {
        currentElectricCounter = 0;
        currentWaterCounter = 0;
    }

    // Update utility counters
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

        int electricCounterUsage = newElectricCounter - currentElectricCounter;
        int waterCounterUsage = newWaterCounter - currentWaterCounter;
        currentElectricCounter = newElectricCounter;
        currentWaterCounter = newWaterCounter;
    }

    // Get utility usage for a specific period
    public int getElectricUsage(int previousCounter) {
        return currentElectricCounter - previousCounter;
    }

    public int getWaterUsage(int previousCounter) {
        return currentWaterCounter - previousCounter;
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

    // Add this method to your Room class
    public void setTenantDirectly(Tenant tenant) {
        this.tenant = tenant;
        this.isOccupied = true;
        // Link the room back to the tenant
        try {
            tenant.assignRoom(this);
        } catch (Exception e) {
            // This is a special case where we're bypassing normal checks
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

    public void markAsVacant() throws RoomException {
        if (!isOccupied) {
            throw new RoomException("Room " + roomNumber + " is already vacant.");
        } else {
            isOccupied = false;
            resetUtilityUsage();
            System.out.println("Room " + roomNumber + " is now vacant. Utility usage has been reset.");
        }
    }

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