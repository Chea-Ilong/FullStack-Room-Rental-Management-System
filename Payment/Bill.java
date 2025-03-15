package Payment;

import Properties.Room;
import Properties.Building;
import Properties.Floor;
import Users.Tenant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

public class Bill {
    // Constants
    private static final double KHR_TO_USD_RATE = 4100.00;
    private static final double ELECTRIC_RATE = 620.00;
    private static final double WATER_RATE = 2500.00;

    // Fields
    private String billID;
    private Room room;
    private Tenant tenant;
    private String buildingName;
    private String floorNumber;
    private LocalDate billDate;
    private LocalDate dueDate;
    private double rentAmount;
    private double electricAmount;
    private double waterAmount;
    private double totalAmount;
    private boolean isPaid;
    private int electricUsage;
    private int waterUsage;

    // Constructor
    public Bill(Room room, String buildingName, String floorNumber,
                double rentAmount, int electricUsage, int waterUsage) {
        if (room == null || buildingName == null || buildingName.isEmpty() ||
                floorNumber == null || floorNumber.isEmpty()) {
            throw new IllegalArgumentException("Invalid room, building name, or floor number.");
        }
        if (!room.isOccupied() || room.getTenant() == null) {
            throw new IllegalArgumentException("Cannot create bill for a vacant room.");
        }

        this.billID = generateBillID();
        this.room = room;
        this.tenant = room.getTenant();
        this.buildingName = buildingName;
        this.floorNumber = floorNumber;
        this.billDate = LocalDate.now();
        this.dueDate = billDate.plusDays(7);
        this.isPaid = false;
        this.electricUsage = electricUsage;
        this.waterUsage = waterUsage;

        calculateBill(rentAmount, electricUsage, waterUsage);
    }

    private String generateBillID() {
        return "BILL-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private void calculateBill(double rentAmount, int electricUsage, int waterUsage) {
        this.rentAmount = rentAmount;
        this.electricAmount = electricUsage * ELECTRIC_RATE;
        this.waterAmount = waterUsage * WATER_RATE;
        this.totalAmount = rentAmount + electricAmount + waterAmount;
    }

    public void markAsPaid(double amount) {
        if (isPaid) {
            throw new IllegalStateException("Bill is already paid.");
        }
        if (amount < totalAmount) {
            throw new IllegalArgumentException("Payment amount is less than the total bill amount.");
        }
        this.isPaid = true;
    }

    public double calculateLateFee() {
        return isPaid || !LocalDate.now().isAfter(dueDate) ? 0
                : totalAmount * 0.01 * (LocalDate.now().toEpochDay() - dueDate.toEpochDay());
    }

    public String toString() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        double lateFee = calculateLateFee();
        double finalTotal = totalAmount + lateFee;

        return String.format(
                "===============================================\n" +
                        "                  INVOICE                      \n" +
                        "===============================================\n" +
                        "Bill ID            : %s\n" +
                        "Building           : %s\n" +
                        "Floor              : %s\n" +
                        "Room Number        : %s\n" +
                        "Tenant ID          : %s\n" +
                        "Tenant Name        : %s\n" +
                        "-----------------------------------------------\n" +
                        "Bill Date          : %s\n" +
                        "Due Date           : %s\n" +
                        "-----------------------------------------------\n" +
                        "Rent               : %s (%s)\n" +
                        "Electricity Usage  : %d -> %d (%s)\n" +
                        "Water Usage        : %d -> %d (%s)\n" +
                        "Subtotal           : %s (%s)\n" +
                        (lateFee > 0 ? "Late Fee           : %s (%s)\n" + "Total Due          : %s (%s)\n" : "") +
                        "-----------------------------------------------\n" +
                        "Status             : %s\n" +
                        "===============================================\n",
                billID, buildingName, floorNumber, room.getRoomNumber(), tenant.getIdCard(), tenant.getName(),
                billDate.format(formatter), dueDate.format(formatter),
                formatKHR(rentAmount), formatUSD(convertToUSD(rentAmount)),
                room.getCurrentElectricCounter(), room.getCurrentElectricCounter() + electricUsage, formatKHR(electricAmount),
                room.getCurrentWaterCounter(), room.getCurrentWaterCounter() + waterUsage, formatKHR(waterAmount),
                formatKHR(totalAmount), formatUSD(convertToUSD(totalAmount)),
                lateFee > 0 ? formatKHR(lateFee) : "", lateFee > 0 ? formatUSD(convertToUSD(lateFee)) : "",
                lateFee > 0 ? formatKHR(finalTotal) : "", lateFee > 0 ? formatUSD(convertToUSD(finalTotal)) : "",
                isPaid ? "PAID" : "UNPAID"
        );
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

    public Room getRoom() {
        return room;
    }

    public LocalDate getBillDate() {
        return billDate;
    }

    public boolean isPaid() {
        return isPaid;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public double getRentAmount() {
        return rentAmount;
    }

    public double getElectricAmount() {
        return electricAmount;
    }

    public double getWaterAmount() {
        return waterAmount;
    }

    public Tenant getTenant() {
        return tenant;
    }
    // Add these methods to the Bill class

    public String getBillID() {
        return billID;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public String getBuildingName() {
        return buildingName;
    }

    public String getFloorNumber() {
        return floorNumber;
    }

    public int getElectricUsage() {
        return electricUsage;
    }

    public int getWaterUsage() {
        return waterUsage;
    }
}
