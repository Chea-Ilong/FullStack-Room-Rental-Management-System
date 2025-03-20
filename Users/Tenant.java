package Users;

import DataBase.BillDML;
import Exceptions.RoomException;
import Exceptions.TenantException;
import Payment.Bill;
import Properties.Room;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Tenant extends User {

    // ====================================================================================================
    // Fields
    // ====================================================================================================
    private Room assignedRoom;
    private Map<LocalDate, Boolean> billPaymentStatus;

    // ====================================================================================================
    // Constructor
    // ====================================================================================================
    public Tenant(String name, String IdCard, String contact) {
        super(name, IdCard, contact, "Tenant");
        this.billPaymentStatus = new HashMap<>();
    }

    // ====================================================================================================
    // Room Assignment
    // ====================================================================================================
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
    public boolean isBillPaid(LocalDate date) {
        YearMonth yearMonth = YearMonth.from(date);
        BillDML billDML = new BillDML();
        List<Bill> bills = billDML.getBillsByTenantId(this.getIdCard());
        for (Bill bill : bills) {
            if (YearMonth.from(bill.getBillDate()).equals(yearMonth)) {
                return bill.isPaid();
            }
        }
        return false;
    }

    public void markBillAsPaid(LocalDate date) {
        LocalDate normalizedDate = LocalDate.of(date.getYear(), date.getMonth(), 1);

        billPaymentStatus.put(normalizedDate, true);
        System.out.println("Bill payment for " + normalizedDate.getMonth() + " " +
                normalizedDate.getYear() + " marked as paid.");
    }

    // ====================================================================================================
    // Getter and Helper Methods
    // ====================================================================================================
    public Room getAssignedRoom() {
        return assignedRoom;
    }

    // ====================================================================================================
    // Override toString
    // ====================================================================================================
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(super.toString()).append("\n");
        sb.append("Tenant Details:\n");
        sb.append("Assigned Room: ").append(assignedRoom != null ? assignedRoom.getRoomNumber() : "None").append("\n");
        return sb.toString();
    }
}