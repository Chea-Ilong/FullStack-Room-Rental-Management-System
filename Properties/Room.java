package Properties;

import Payment.UtilityUsage;
import Users.Tenant;

import java.time.LocalDate;

public class Room {

    // ============================ Room Information ============================
    private String roomNumber;
    private double rent;
    private boolean isOccupied;
    private Tenant tenant; // Assigned tenant (if any)
    private UtilityUsage utilityUsage;

    // ============================ Utility Tracking ============================
    private int waterCounterUsage = 0;
    private int electricCounterUsage = 0;
    private int currentElectricCounter;
    private int currentWaterCounter;

    // ============================ Utility Rates (Constants) ============================
    private static final double ELECTRIC_RATE = 620.00; // per kWh
    private static final double WATER_RATE = 2500.00;   // per cubic meter
    private static final double KHR_TO_USD_RATE = 4100.00;

    // ============================ Constructor ============================
    public Room(String roomNumber, int currentElectricCounter, int currentWaterCounter) {
        this.roomNumber = roomNumber;
        this.rent = 300000; // Default rent in khmer
        this.isOccupied = false;
        this.tenant = null;
        this.currentElectricCounter = Math.max(currentElectricCounter, 0);
        this.currentWaterCounter = Math.max(currentWaterCounter, 0);
    }

    // ============================ Utility Usage Management ============================
    public void setUtilityUsage(int electricUsage, int waterUsage, LocalDate date) {
        this.utilityUsage = new UtilityUsage(electricUsage, waterUsage, date);

        // Update the current counters with the usage
        currentElectricCounter += electricUsage;
        currentWaterCounter += waterUsage;
    }

    public UtilityUsage getUtilityUsage() {
        return this.utilityUsage;
    }

    // ============================ Tenant Management ============================
    public void assignTenant(Tenant tenant) {
        this.tenant = tenant;
        this.isOccupied = (tenant != null);
    }

    public void removeTenant() {
        this.tenant = null;
        this.isOccupied = false;
    }

    // ============================ Room Status Management ============================
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

    // ============================ Utility Usage Update ============================
    public void updateUsage(int newElectricCounter, int newWaterCounter) {
        if (!isOccupied) {
            System.out.println("Error: Cannot update usage for a vacant room.");
            return;
        }

        if (newElectricCounter < currentElectricCounter) {
            System.out.println("Error: New electric counter must be greater than the current counter.");
            return;
        }

        if (newWaterCounter < currentWaterCounter) {
            System.out.println("Error: New water counter must be greater than the current counter.");
            return;
        }

        if (newElectricCounter == currentElectricCounter && newWaterCounter == currentWaterCounter) {
            System.out.println("No change in usage. No update needed.");
            return;
        }

        electricCounterUsage = newElectricCounter - currentElectricCounter;
        waterCounterUsage = newWaterCounter - currentWaterCounter;
        currentElectricCounter = newElectricCounter;
        currentWaterCounter = newWaterCounter;
    }

    void resetUtilityUsage() {
        electricCounterUsage = 0;
        waterCounterUsage = 0;
    }

    // ============================ displayRoomBilling ============================
    public void displayRoomBilling() {
        if (!isOccupied) {
            System.out.println("Room " + roomNumber + " is vacant. No billing required.");
            return;
        }

        // Get the utility usage from the utilityUsage object (which is set by landlord)
        if (utilityUsage != null) {
            electricCounterUsage = utilityUsage.getElectricUsage();
            waterCounterUsage = utilityUsage.getWaterUsage();
        }

        double electricPrice = calculateElectricPrice();
        double waterPrice = calculateWaterPrice();
        double totalPrice = rent + electricPrice + waterPrice;
        double totalPriceUSD = convertToUSD(totalPrice);

        System.out.println("Room Number: " + roomNumber);
        System.out.println("Rent: " + formatKHR(rent) + " (" + formatUSD(convertToUSD(rent)) + ")");
        System.out.println("Water counter: " + (currentWaterCounter - waterCounterUsage) + " -> " + currentWaterCounter);
        System.out.println("Electric counter: " + (currentElectricCounter - electricCounterUsage) + " -> " + currentElectricCounter);
        System.out.println("Water Usage: " + waterCounterUsage + " m³");
        System.out.println("Electric Usage: " + electricCounterUsage + " kWh");
        System.out.println("Water Price: " + formatKHR(waterPrice) + " (" + formatUSD(convertToUSD(waterPrice)) + ")");
        System.out.println("Electric Price: " + formatKHR(electricPrice) + " (" + formatUSD(convertToUSD(electricPrice)) + ")");
        System.out.println("Total Expense: " + formatKHR(totalPrice) + " (" + formatUSD(totalPriceUSD) + ")");
        System.out.println();
    }

    // ============================ Display Methods ============================
    public void displayRoomInfo() {
        System.out.println("Room Number: " + roomNumber);
        System.out.println("Rent: " + rent);
        System.out.println("Occupied: " + (isOccupied ? "Yes" : "No"));
        if (isOccupied && tenant != null) {
            System.out.println("Tenant: " + tenant.getName());
        }
        System.out.println("Electricity Usage: " + electricCounterUsage + " kWh");
        System.out.println("Water Usage: " + waterCounterUsage + " Liters");
    }

    @Override
    public String toString() {
        return "Room Details:\n" +
                "  Room Number: " + roomNumber + "\n" +
                "  Rent: " + formatKHR(rent) + " (" + formatUSD(convertToUSD(rent)) + ")\n" +
                "  Status: " + (isOccupied ? "Occupied" : "Vacant") + "\n" +
                "  Current Water Counter: " + currentWaterCounter + " m³\n" +
                "  Current Electric Counter: " + currentElectricCounter + " kWh\n";
    }

    // ============================ Private Helper Methods ============================
    private double calculateElectricPrice() {
        return isOccupied ? electricCounterUsage * ELECTRIC_RATE : 0;
    }

    private double calculateWaterPrice() {
        return isOccupied ? waterCounterUsage * WATER_RATE : 0;
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

    // ============================ Getters ============================
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
}
