package Payment;

import DataBase.BillDML;
import DataBase.DataBaseConnection;
import DataBase.RoomDML;
import Properties.Room;
import Users.Tenant;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BillRecord {
    private Map<String, Map<String, Map<String, Map<YearMonth, Bill>>>> billDatabase;

    // ====================================================================================================
    // Constructor
    // ====================================================================================================
    public BillRecord() {
        this.billDatabase = new HashMap<>();
    }

    // ====================================================================================================
    // Bill Storage and Management Methods
    // ====================================================================================================
    public void storeBill(Bill bill, String buildingName, String floorNumber) {
        String roomNumber = bill.getRoom().getRoomNumber();
        YearMonth billingPeriod = YearMonth.from(bill.getBillDate());

        billDatabase.putIfAbsent(buildingName, new HashMap<>());
        billDatabase.get(buildingName).putIfAbsent(floorNumber, new HashMap<>());
        billDatabase.get(buildingName).get(floorNumber).putIfAbsent(roomNumber, new HashMap<>());
        billDatabase.get(buildingName).get(floorNumber).get(roomNumber).put(billingPeriod, bill);
    }

    public void updateBill(Bill bill) {
        String buildingName = bill.getBuildingName();
        String floorNumber = bill.getFloorNumber();
        String roomNumber = bill.getRoom().getRoomNumber();
        YearMonth billingPeriod = YearMonth.from(bill.getBillDate());

        if (billDatabase.containsKey(buildingName) &&
                billDatabase.get(buildingName).containsKey(floorNumber) &&
                billDatabase.get(buildingName).get(floorNumber).containsKey(roomNumber) &&
                billDatabase.get(buildingName).get(floorNumber).get(roomNumber).containsKey(billingPeriod)) {
            billDatabase.get(buildingName).get(floorNumber).get(roomNumber).put(billingPeriod, bill);
            System.out.println("Bill updated successfully in BillRecord for Room " + roomNumber +
                    " in " + buildingName + ", Floor " + floorNumber +
                    " for period " + billingPeriod.getMonth() + " " + billingPeriod.getYear());
        } else {
            storeBill(bill, buildingName, floorNumber);
        }
    }

    // ====================================================================================================
    // Getter and Helper Methods
    // ====================================================================================================
    public List<Bill> getBillHistoryForTenant(String tenantId) {
        BillDML billDML = new BillDML();
        List<Bill> tenantBills = billDML.getBillsByTenantId(tenantId);
        RoomDML roomDML = new RoomDML();

        for (Bill bill : tenantBills) {
            if (bill.getRoom() == null || bill.getBuildingName() == null || bill.getFloorNumber() == null) {
                int roomId = billDML.getRoomIdByBuildingFloorAndNumber(
                        bill.getBuildingName(),
                        bill.getFloorNumber(),
                        bill.getRoom() != null ? bill.getRoom().getRoomNumber() : null
                );
                if (roomId != -1) {
                    Room room = roomDML.getRoomById(roomId);
                    if (room != null) {
                        bill.setRoom(room);
                        if (room.getTenant() != null) {
                            bill.setTenant(room.getTenant());
                        }
                    }
                }
            }

            if (bill.getTenant() == null && tenantId != null) {
                try (Connection conn = DataBaseConnection.getConnection()) {
                    String tenantQuery = "SELECT u.name, u.IdCard, u.contact " +
                            "FROM Users u " +
                            "JOIN Tenants t ON t.user_id = u.user_id " +
                            "WHERE u.IdCard = ?";
                    PreparedStatement ps = conn.prepareStatement(tenantQuery);
                    ps.setString(1, tenantId);
                    ResultSet rs = ps.executeQuery();
                    if (rs.next()) {
                        Tenant tenant = new Tenant(
                                rs.getString("name"),
                                rs.getString("IdCard"),
                                rs.getString("contact") != null ? rs.getString("contact") : ""
                        );
                        bill.setTenant(tenant);
                    }
                    rs.close();
                    ps.close();
                } catch (SQLException e) {
                    System.err.println("Error fetching tenant data: " + e.getMessage());
                }
            }
        }

        return tenantBills;
    }
}