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
    private int billID;
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
    private LocalDate paymentDate;

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

        // Don't set billID here, it will be set after database insert
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

    // In the Bill class
    public void markAsPaid(double paymentAmount) {
        if (isPaid) {
            throw new IllegalStateException("Bill is already paid");
        }

        if (paymentAmount < totalAmount) {
            throw new IllegalArgumentException("Payment amount must be at least the total bill amount");
        }

        this.isPaid = true;
        this.paymentDate = LocalDate.now();
    }

    public double calculateLateFee() {
        return isPaid || !LocalDate.now().isAfter(dueDate) ? 0
                : totalAmount * 0.01 * (LocalDate.now().toEpochDay() - dueDate.toEpochDay());
    }
    @Override
    public String toString() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        double lateFee = calculateLateFee();
        double finalTotal = totalAmount + lateFee;

        StringBuilder sb = new StringBuilder();
        sb.append("===============================================\n")
                .append("                  INVOICE                      \n")
                .append("===============================================\n")
                .append("Bill ID            : ").append(billID).append("\n")
                .append("Building           : ").append(buildingName).append("\n")
                .append("Floor              : ").append(floorNumber).append("\n")
                .append("Room Number        : ").append(room.getRoomNumber()).append("\n")
                .append("Tenant ID          : ").append(tenant.getIdCard()).append("\n")
                .append("Tenant Name        : ").append(tenant.getName()).append("\n")
                .append("-----------------------------------------------\n")
                .append("Bill Date          : ").append(billDate.format(formatter)).append("\n")
                .append("Due Date           : ").append(dueDate.format(formatter)).append("\n")
                .append("-----------------------------------------------\n")
                .append("Water Rate         : ").append(formatKHR(WATER_RATE)).append("\n")
                .append("Electric Rate      : ").append(formatKHR(ELECTRIC_RATE)).append("\n")
                .append("-----------------------------------------------\n")
                .append("Rent               : ").append(formatKHR(rentAmount))
                .append(" (").append(formatUSD(convertToUSD(rentAmount))).append(")\n")
                .append("Electricity Usage  : ").append(room.getCurrentElectricCounter()).append(" -> ")
                .append(room.getCurrentElectricCounter() + electricUsage).append(" (")
                .append(formatKHR(electricAmount)).append(")\n")
                .append("Water Usage        : ").append(room.getCurrentWaterCounter()).append(" -> ")
                .append(room.getCurrentWaterCounter() + waterUsage).append(" (")
                .append(formatKHR(waterAmount)).append(")\n")
                .append("Subtotal           : ").append(formatKHR(totalAmount))
                .append(" (").append(formatUSD(convertToUSD(totalAmount))).append(")\n");

        // Append late fee only if applicable
        if (lateFee > 0) {
            sb.append("Late Fee           : ").append(formatKHR(lateFee))
                    .append(" (").append(formatUSD(convertToUSD(lateFee))).append(")\n")
                    .append("Total Due          : ").append(formatKHR(finalTotal))
                    .append(" (").append(formatUSD(convertToUSD(finalTotal))).append(")\n");
        }

        sb.append("-----------------------------------------------\n")
                .append("Status             : ").append(isPaid ? "PAID" : "UNPAID").append("\n")
                .append("===============================================\n");

        return sb.toString();
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

    public int getBillID() {
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


    public void setBillID(int billID) {
        this.billID = billID;
    }

    public void setBillDate(LocalDate billDate) {
        this.billDate = billDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

//    public void setPaid(boolean paid) {
//        this.isPaid = paid;
//    }

}
