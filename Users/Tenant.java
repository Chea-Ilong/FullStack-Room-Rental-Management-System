package Users;

import DataBase.TenantDML;
import Exceptions.RoomException;
import Exceptions.TenantException;
import Properties.Room;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

public class Tenant extends User {

    // ====================================================================================================
    // Fields
    // ====================================================================================================
    private Room assignedRoom;
    private boolean rentPaid;
    private double balanceDue;
    private Map<LocalDate, Boolean> billPaymentStatus;
    private static final double KHR_TO_USD_RATE = 4100.00;

    // Constructor
    // ====================================================================================================
    public Tenant(String name, String IdCard, String contact) {
        super(name, IdCard, contact, "Tenant");
        this.rentPaid = false;
        this.balanceDue = 0.0;
        this.billPaymentStatus = new HashMap<>();
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
    }

    // ====================================================================================================
    // Bill Payment Methods
    // ====================================================================================================

    // Helper method to check if bill is paid for a specific date
    public boolean isBillPaid(LocalDate date) {
        // Normalize date to first day of month to track monthly payments
        LocalDate normalizedDate = LocalDate.of(date.getYear(), date.getMonth(), 1);

        // Return true if payment exists and is marked as paid, false otherwise
        return billPaymentStatus.getOrDefault(normalizedDate, false);
    }

    // Mark a bill as paid for a specific date
    public void markBillAsPaid(LocalDate date) {
        // Normalize date to first day of month to track monthly payments
        LocalDate normalizedDate = LocalDate.of(date.getYear(), date.getMonth(), 1);

        // Mark this month's bill as paid
        billPaymentStatus.put(normalizedDate, true);
        System.out.println("Bill payment for " + normalizedDate.getMonth() + " " +
                normalizedDate.getYear() + " marked as paid.");

    }

    // Check payment status for a specific month
    public boolean checkBillPaymentStatus(int year, int month) {
        LocalDate date = LocalDate.of(year, month, 1);
        boolean paid = isBillPaid(date);

        System.out.println("Bill payment for " + date.getMonth() + " " +
                date.getYear() + " status: " +
                (paid ? "PAID" : "UNPAID"));

        return paid;
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

    // Add this method to your Tenant class
    public void updateRoomInformation(Room updatedRoom) {
        if (this.assignedRoom != null &&
                this.assignedRoom.getRoomNumber().equals(updatedRoom.getRoomNumber())) {
            // It's the same room, just update the information
            this.assignedRoom = updatedRoom;
        } else {
            // It's a different room, would need formal assignment
            System.out.println("Cannot update room information - different room number.");
        }
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