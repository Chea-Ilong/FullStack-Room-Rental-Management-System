package Properties;

import DataBase.RoomDML;
import Payment.UtilityUsage;
import Users.Tenant;
import Exceptions.RoomException;
import java.time.LocalDate;

public class Room {

    // ====================================================================================================
    // Constants
    // ====================================================================================================
    private static final double ELECTRIC_RATE = 620.00;
    private static final double WATER_RATE = 2500.00;
    private static final double KHR_TO_USD_RATE = 4100.00;

    // ====================================================================================================
    // Room Information
    // ====================================================================================================
    private String roomNumber;
    private double rent;
    private boolean isOccupied;
    private Tenant tenant;
    private UtilityUsage utilityUsage;
    private int currentElectricCounter;
    private int currentWaterCounter;

    // ====================================================================================================
    // Constructor
    // ====================================================================================================
    public Room(String roomNumber, int currentElectricCounter, int currentWaterCounter) {
        this.roomNumber = roomNumber;
        this.rent = 300000;
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

    public double getRent() {
        return rent;
    }

    public void setRent(double rent) {
        this.rent = rent;
    }

    public Tenant getTenant() {
        return tenant;
    }

    public boolean isOccupied() {
        return isOccupied;
    }

    public UtilityUsage getUtilityUsage() {
        return this.utilityUsage;
    }

    public static double getElectricRate() {
        return ELECTRIC_RATE;
    }

    public static double getWaterRate() {
        return WATER_RATE;
    }

    public int getCurrentElectricCounter() {
        return currentElectricCounter;
    }

    public void setCurrentElectricCounter(int counter) {
        this.currentElectricCounter = counter;
    }

    public int getCurrentWaterCounter() {
        return currentWaterCounter;
    }

    public void setCurrentWaterCounter(int counter) {
        this.currentWaterCounter = counter;
    }

    public int getElectricCounter() {
        if (utilityUsage != null) {
            return utilityUsage.getElectricUsage();
        }
        return this.currentElectricCounter;
    }

    public int getWaterCounter() {
        if (utilityUsage != null) {
            return utilityUsage.getWaterUsage();
        }
        return this.currentWaterCounter;
    }

    // ====================================================================================================
    // Utility Management
    // ====================================================================================================
    public void setUtilityUsage(int electricUsage, int waterUsage, LocalDate date) {
        this.utilityUsage = new UtilityUsage(electricUsage, waterUsage, date);
        currentElectricCounter += electricUsage;
        currentWaterCounter += waterUsage;
    }

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

    void resetUtilityUsage() {
        currentElectricCounter = 0;
        currentWaterCounter = 0;
    }

    public void updateUtilityUsage(int newElectricUsage, int newWaterUsage, LocalDate date) {
        this.currentElectricCounter = newElectricUsage;
        this.currentWaterCounter = newWaterUsage;
        this.utilityUsage = new UtilityUsage(newElectricUsage, newWaterUsage, date);

        // Update the database with the new utility usage
        RoomDML roomDML = new RoomDML();
        roomDML.updateUtilityUsage(this.roomNumber, newElectricUsage, newWaterUsage, date);
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
            System.out.println("Room " + roomNumber + " is now occupied.");
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

    // ====================================================================================================
    // Price Calculation
    // ====================================================================================================
    private double calculateElectricPrice(int usage) {
        return usage * ELECTRIC_RATE;
    }

    private double calculateWaterPrice(int usage) {
        return usage * WATER_RATE;
    }

    private double convertToUSD(double amount) {
        return amount / KHR_TO_USD_RATE;
    }

    private String formatKHR(double amount) {
        return String.format("%.0fKHR", amount);
    }

    private String formatUSD(double amount) {
        return String.format("%.2fUSD", amount);
    }

    // ====================================================================================================
    // Billing and Reporting
    // ====================================================================================================
    @Override
    public String toString() {
        if (!isOccupied) {
            return "Room " + roomNumber + " is vacant. No billing required.";
        }

        int electricCounterUsage = utilityUsage != null ? utilityUsage.getElectricUsage() : 0;
        int waterCounterUsage = utilityUsage != null ? utilityUsage.getWaterUsage() : 0;

        double electricPrice = calculateElectricPrice(electricCounterUsage);
        double waterPrice = calculateWaterPrice(waterCounterUsage);
        double totalUtilityPrice = electricPrice + waterPrice;
        double totalPrice = rent + totalUtilityPrice;

        return String.format(
                "==========================================\n" +
                        " Room Billing Summary\n" +
                        "==========================================\n" +
                        "Room Number       : %s\n" +
                        "------------------------------------------\n" +
                        "Rent              : %s (%s)\n" +
                        "Water Counter     : %d -> %d\n" +
                        "Electric Counter  : %d -> %d\n" +
                        "------------------------------------------\n" +
                        "Water Usage       : %d m³\n" +
                        "Electric Usage    : %d kWh\n" +
                        "------------------------------------------\n" +
                        "Water Price       : %s (%s)\n" +
                        "Electric Price    : %s (%s)\n" +
                        "Total Utility Cost: %s (%s)\n" +
                        "------------------------------------------\n" +
                        "Total Expense     : %s (%s)\n" +
                        "==========================================\n",
                roomNumber,
                formatKHR(rent), formatUSD(convertToUSD(rent)),
                currentWaterCounter - waterCounterUsage, currentWaterCounter,
                currentElectricCounter - electricCounterUsage, currentElectricCounter,
                waterCounterUsage, electricCounterUsage,
                formatKHR(waterPrice), formatUSD(convertToUSD(waterPrice)),
                formatKHR(electricPrice), formatUSD(convertToUSD(electricPrice)),
                formatKHR(totalUtilityPrice), formatUSD(convertToUSD(totalUtilityPrice)),
                formatKHR(totalPrice), formatUSD(convertToUSD(totalPrice))
        );
    }
    // In your tenant menu or wherever you display room information
    public void displayRoomInformation(Room room) {
        // First ensure room has up-to-date tenant information
        RoomDML roomDML = new RoomDML();
        roomDML.syncRoomWithTenant(room);

        // Now display the room information
        System.out.println(room.toString());
    }
}
