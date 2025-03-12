package Users;

import DataBase.RentPaymentDML;
import DataBase.TenantDML;
import Exceptions.RoomException;
import Exceptions.TenantException;
import Payment.UtilityUsage;
import Properties.Room;
import Payment.RentPayment;

import java.time.LocalDate;
import java.util.*;

public class Tenant extends User {

    // ====================================================================================================
    // Constants
    // ====================================================================================================
    private static final double KHR_TO_USD_RATE = 4100.0; // Example exchange rate

    // ====================================================================================================
    // Fields
    // ====================================================================================================
    private Room assignedRoom;
    private final List<RentPayment> rentPaymentHistory;
    private boolean rentPaid;
    private double balanceDue;
    private Map<LocalDate, Boolean> utilityPaymentStatus = new HashMap<>();

    // ====================================================================================================
    // Constructor
    // ====================================================================================================
    public Tenant(String name, String IdCard, String contact) {
        super(name, IdCard, contact, "Tenant");
        this.rentPaymentHistory = new ArrayList<>();
        this.rentPaid = false;
        this.balanceDue = 0.0;
    }

    // ====================================================================================================
    // Methods for Utility Usage
    // ====================================================================================================

    // Method to view utility usage for a specific date
    public void viewUtilityUsage(Landlord landlord, LocalDate date) {
        UtilityUsage usage = landlord.getUtilityUsageForRoom(assignedRoom, date);
        if (usage != null) {
            System.out.println(name + "'s utility usage for " + date + ": " + usage.toString());
        } else {
            System.out.println("No utility data available for " + date);
        }
    }

    // ====================================================================================================
    // Room Assignment
    // ====================================================================================================

    // Assign a room to the tenant
    public void assignRoom(Room room) throws RoomException, TenantException {
        if (this.assignedRoom != null) {
            throw new TenantException("Error: Tenant is already assigned to Room " + assignedRoom.getRoomNumber() + ".");
        }

        if (room.isOccupied()) {
            throw new RoomException("Room " + room.getRoomNumber() + " is already occupied.");
        }

        this.assignedRoom = room;
        room.assignTenant(this);
        this.balanceDue = room.getRent();

        System.out.println(name + " has been assigned to Room " + room.getRoomNumber() +
                " with rent: " + formatKHR(room.getRent()) + " (" + formatUSD(convertToUSD(room.getRent())) + ")");
    }

    // ====================================================================================================
    // Rent Payment
    // ====================================================================================================

    // Pay Rent
    public void payRent(Scanner scanner) throws TenantException {
        if (assignedRoom == null) {
            throw new TenantException("Error: No room assigned to pay rent for.");
        }

        // Show the balance due before payment
        System.out.println("Current rent balance due: " + formatKHR(balanceDue) + " (" + formatUSD(convertToUSD(balanceDue)) + ")");

        System.out.print("Enter the amount to pay for rent: ");
        double amount = scanner.nextDouble();
        scanner.nextLine(); // Consume newline

        if (amount > balanceDue) {
            System.out.println("Error: Payment amount cannot be greater than the rent balance.");
            return;
        }

        if (amount == balanceDue) {
            rentPaid = true;
            balanceDue = 0.0;
        } else {
            balanceDue -= amount;
        }

        // Create payment record
        RentPayment rentPayment = new RentPayment(IdCard, amount, true, false);
        rentPaymentHistory.add(rentPayment);

        // Save to database
        RentPaymentDML paymentDML = new RentPaymentDML();
        paymentDML.savePayment(rentPayment);

        System.out.println(name + " has paid rent: " + formatKHR(amount) +
                " (" + formatUSD(convertToUSD(amount)) + ")");
        System.out.println("Remaining balance: " + formatKHR(balanceDue) +
                " (" + formatUSD(convertToUSD(balanceDue)) + ")");
    }

    // ====================================================================================================
    // Utility Payment
    // ====================================================================================================

    // Pay Utilities
    public void payUtilities(double amount) throws TenantException {
        if (assignedRoom == null) {
            throw new TenantException("Error: No room assigned to pay utilities for.");
        }

        UtilityUsage usage = assignedRoom.getUtilityUsage();
        if (usage == null) {
            throw new TenantException("Error: Utility usage data is not available. Please contact the landlord.");
        }

        // Check if the utility usage is unpaid (you may need to track this)
        if (isUtilityPaid(usage.getDate())) {
            throw new TenantException("Error: Utility bill for " + usage.getDate() + " has already been paid.");
        }

        double totalUtilityCost = calculateTotalUtilityCost(usage);
        System.out.println("Total Utility Cost: " + formatKHR(totalUtilityCost) + " (" + formatUSD(convertToUSD(totalUtilityCost)) + ")");

        if (amount < totalUtilityCost) {
            System.out.println("Error: Payment amount must cover the full utility cost.");
            return;
        }

        if (amount > totalUtilityCost) {
            System.out.println("Warning: Payment amount exceeds the utility balance. Processing exact amount only.");
            amount = totalUtilityCost;
        }

        // Record the payment
        RentPayment rentPayment = new RentPayment(IdCard, amount, false, true);
        rentPaymentHistory.add(rentPayment);

        // Mark the utility as paid for this date
        markUtilityAsPaid(usage.getDate());

        System.out.println(name + " has fully paid utilities: " + formatKHR(amount) +
                " (" + formatUSD(convertToUSD(amount)) + ") on " + rentPayment.getPaymentDate());
    }

    // Helper methods to track utility payment status
    private boolean isUtilityPaid(LocalDate date) {
        // Normalize date to first day of month to track monthly payments
        LocalDate normalizedDate = LocalDate.of(date.getYear(), date.getMonth(), 1);

        // Return true if payment exists and is marked as paid, false otherwise
        return utilityPaymentStatus.getOrDefault(normalizedDate, false);
    }

    private void markUtilityAsPaid(LocalDate date) {
        // Normalize date to first day of month to track monthly payments
        LocalDate normalizedDate = LocalDate.of(date.getYear(), date.getMonth(), 1);

        // Mark this month's utility as paid
        utilityPaymentStatus.put(normalizedDate, true);
        System.out.println("Utility payment for " + normalizedDate.getMonth() + " " +
                normalizedDate.getYear() + " marked as paid.");

        // Update in database if needed
        TenantDML tenantDML = new TenantDML();
        tenantDML.updateUtilityPaymentStatus(this.IdCard, normalizedDate, true);
    }

    // Additional useful methods for utility payments
    public void payUtilityBill(LocalDate date, double amount) {
        // Normalize date to first day of month
        LocalDate normalizedDate = LocalDate.of(date.getYear(), date.getMonth(), 1);

        if (!isUtilityPaid(normalizedDate)) {
            // Create payment record
            RentPayment payment = new RentPayment(this.IdCard, amount, false, true);

            // Mark as paid
            markUtilityAsPaid(normalizedDate);

            System.out.println("Utility payment of $" + amount + " for " +
                    normalizedDate.getMonth() + " " + normalizedDate.getYear() +
                    " has been processed.");
        } else {
            System.out.println("Utility bill for " + normalizedDate.getMonth() +
                    " " + normalizedDate.getYear() + " has already been paid.");
        }
    }

    public boolean checkUtilityPaymentStatus(int year, int month) {
        LocalDate date = LocalDate.of(year, month, 1);
        boolean paid = isUtilityPaid(date);

        System.out.println("Utility payment for " + date.getMonth() + " " +
                date.getYear() + " status: " +
                (paid ? "PAID" : "UNPAID"));

        return paid;
    }

    public double calculateTotalUtilityCost(UtilityUsage usage) {
        return (usage.getElectricUsage() * Room.getElectricRate()) + (usage.getWaterUsage() * Room.getWaterRate());
    }

    // ====================================================================================================
    // Payment History
    // ====================================================================================================

    // Display Payment History
    public void displayPaymentHistory() {
        System.out.println("\n===== Payment History for " + name + " =====");

        // First get payments from database
        RentPaymentDML paymentDML = new RentPaymentDML();
        List<RentPayment> databasePayments = paymentDML.getPaymentHistoryForTenant(IdCard);

        if (databasePayments.isEmpty() && rentPaymentHistory.isEmpty()) {
            System.out.println("No payment history found.");
            return;
        }

        // Display payments from database
        for (RentPayment payment : databasePayments) {
            System.out.println(payment.toString());
        }

        // If there are in-memory payments not yet in database, display those too
        for (RentPayment payment : rentPaymentHistory) {
            if (!databasePayments.contains(payment)) {
                System.out.println(payment.toString() + " (Not yet saved to database)");
            }
        }
        System.out.println("----------------------------------");
    }

    // ====================================================================================================
    // Vacating Room
    // ====================================================================================================

    // Vacate Room
    public void vacateRoom() throws TenantException, RoomException {
        if (assignedRoom == null) {
            throw new TenantException("Error: Tenant is not assigned to any room.");
        }

        System.out.println(name + " is vacating Room " + assignedRoom.getRoomNumber());
        assignedRoom.removeTenant();
        this.assignedRoom = null;
        this.balanceDue = 0.0;
        this.rentPaid = false;
    }

    // ====================================================================================================
    // Getter and Helper Methods
    // ====================================================================================================
    public Room getAssignedRoom() {
        return assignedRoom;
    }

    // Convert KHR to USD
    private double convertToUSD(double amount) {
        return amount / KHR_TO_USD_RATE;
    }

    // Format amount in KHR
    private String formatKHR(double amount) {
        return String.format("%.0f KHR", amount);
    }

    // Format amount in USD
    private String formatUSD(double amount) {
        return String.format("%.2f USD", amount);
    }

    // ====================================================================================================
    // Override toString
    // ====================================================================================================
    @Override
    public String toString() {
        return super.toString() +
                "Tenant{" +
                "assignedRoom=" + (assignedRoom != null ? assignedRoom.getRoomNumber() : "None") +
                ", balanceDue=" + formatKHR(balanceDue) +
                ", rentPaid=" + rentPaid +
                '}';
    }

}
