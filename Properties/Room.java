package Properties;

import Payment.UtilityUsage;
import Users.Tenant;

import java.time.LocalDate;

public class Room {

    private String roomNumber;
    private double rent;
    private boolean isOccupied;
    private Tenant tenant;
    private UtilityUsage utilityUsage;
    private int currentElectricCounter;
    private int currentWaterCounter;

    private static final double ELECTRIC_RATE = 620.00;
    private static final double WATER_RATE = 2500.00;
    private static final double KHR_TO_USD_RATE = 4100.00;

    public Room(String roomNumber, int currentElectricCounter, int currentWaterCounter) {
        this.roomNumber = roomNumber;
        this.rent = 300000;
        this.isOccupied = false;
        this.tenant = null;
        this.currentElectricCounter = Math.max(currentElectricCounter, 0);
        this.currentWaterCounter = Math.max(currentWaterCounter, 0);
    }

    public void setUtilityUsage(int electricUsage, int waterUsage, LocalDate date) {
        this.utilityUsage = new UtilityUsage(electricUsage, waterUsage, date);
        currentElectricCounter += electricUsage;
        currentWaterCounter += waterUsage;
    }

    public UtilityUsage getUtilityUsage() {
        return this.utilityUsage;
    }

    public void assignTenant(Tenant tenant) {
        this.tenant = tenant;
        this.isOccupied = (tenant != null);
    }

    public void removeTenant() {
        this.tenant = null;
        this.isOccupied = false;
    }

    public void markAsOccupied() {
        if (isOccupied) {
            System.out.println("Room " + roomNumber + " is already occupied.");
        } else {
            isOccupied = true;
            System.out.println("Room " + roomNumber + " is now occupied.");
        }
    }

    public void markAsVacant() {
        if (!isOccupied) {
            System.out.println("Room " + roomNumber + " is already vacant.");
        } else {
            isOccupied = false;
            resetUtilityUsage();
            System.out.println("Room " + roomNumber + " is now vacant. Utility usage has been reset.");
        }
    }

    public void updateUsage(int newElectricCounter, int newWaterCounter) {
        if (!isOccupied) {
            System.out.println("Error: Cannot update usage for a vacant room.");
            return;
        }

        if (newElectricCounter < currentElectricCounter || newWaterCounter < currentWaterCounter) {
            System.out.println("Error: New counters must be greater than the current counters.");
            return;
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

        return "==========================================\n" +
                " Room Billing Summary\n" +
                "==========================================\n" +
                "Room Number       : " + roomNumber + "\n" +
                "------------------------------------------\n" +
                "Rent              : " + formatKHR(rent) + " (" + formatUSD(convertToUSD(rent)) + ")\n" +
                "Water Counter     : " + (currentWaterCounter - waterCounterUsage) + " -> " + currentWaterCounter + "\n" +
                "Electric Counter  : " + (currentElectricCounter - electricCounterUsage) + " -> " + currentElectricCounter + "\n" +
                "------------------------------------------\n" +
                "Water Usage       : " + waterCounterUsage + " m³\n" +
                "Electric Usage    : " + electricCounterUsage + " kWh\n" +
                "------------------------------------------\n" +
                "Water Price       : " + formatKHR(waterPrice) + " (" + formatUSD(convertToUSD(waterPrice)) + ")\n" +
                "Electric Price    : " + formatKHR(electricPrice) + " (" + formatUSD(convertToUSD(electricPrice)) + ")\n" +
                "Total Utility Cost: " + formatKHR(totalUtilityPrice) + " (" + formatUSD(convertToUSD(totalUtilityPrice)) + ")\n" +
                "------------------------------------------\n" +
                "Total Expense     : " + formatKHR(totalPrice) + " (" + formatUSD(convertToUSD(totalPrice)) + ")\n" +
                "==========================================\n";
    }


    private double calculateElectricPrice(int usage) {
        return isOccupied ? usage * ELECTRIC_RATE : 0;
    }

    private double calculateWaterPrice(int usage) {
        return isOccupied ? usage * WATER_RATE : 0;
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

    public String getRoomNumber() {
        return roomNumber;
    }

    public double getRent() {
        return rent;
    }

    public Tenant getTenant() {
        return tenant;
    }

    public void setRoomNumber(String roomNumber) {
        this.roomNumber = roomNumber;
    }

    public boolean isOccupied() {
        return isOccupied;
    }

    public static double getElectricRate() {
        return ELECTRIC_RATE;
    }

    public static double getWaterRate() {
        return WATER_RATE;
    }
}