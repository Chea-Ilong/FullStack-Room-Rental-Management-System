package DataBase;

import Payment.Bill;
import Properties.Room;
import Users.Tenant;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static DataBase.DataBaseConnection.getConnection;

public class BillDML {

    // ====================================================================================================
    // Bill CRUD Operations
    // ====================================================================================================
    public boolean saveBill(Bill bill) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet generatedKeys = null;

        try {
            conn = getConnection();
            conn.setAutoCommit(false);

            int roomId = getRoomIdByBuildingFloorAndNumber(
                    bill.getBuildingName(),
                    bill.getFloorNumber(),
                    bill.getRoom().getRoomNumber()
            );

            if (roomId == -1) {
                System.out.println("Room not found.");
                return false;
            }

            String tenantQuery = "SELECT u.IdCard, u.name, u.contact " +
                    "FROM Tenants t " +
                    "JOIN Users u ON t.user_id = u.user_id " +
                    "WHERE t.assigned_room_id = ?";
            Tenant tenant = null;
            String tenantIdCard = null;
            try (PreparedStatement tenantPs = conn.prepareStatement(tenantQuery)) {
                tenantPs.setInt(1, roomId);
                ResultSet rs = tenantPs.executeQuery();
                if (rs.next()) {
                    tenantIdCard = rs.getString("IdCard");
                    tenant = new Tenant(
                            rs.getString("name"),
                            tenantIdCard,
                            rs.getString("contact") != null ? rs.getString("contact") : ""
                    );
                    bill.setTenant(tenant);
                    System.out.println("Saving bill for tenant: " + tenant.getName() + " (ID Card: " + tenantIdCard + ")");
                } else {
                    System.out.println("Warning: No tenant assigned to room ID " + roomId);
                }
            }

            String query = "INSERT INTO Bills (room_id, tenant_id, due_date, is_paid, bill_date, " +
                    "electric_amount, water_amount, electric_usage, water_usage, building_name, floor_number, " +
                    "rent_amount, total_amount) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            ps = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);

            ps.setInt(1, roomId);
            ps.setString(2, tenantIdCard);
            ps.setDate(3, java.sql.Date.valueOf(bill.getDueDate()));
            ps.setBoolean(4, bill.isPaid());
            ps.setDate(5, java.sql.Date.valueOf(bill.getBillDate()));
            ps.setDouble(6, bill.getElectricAmount());
            ps.setDouble(7, bill.getWaterAmount());
            ps.setInt(8, bill.getElectricUsage());
            ps.setInt(9, bill.getWaterUsage());
            ps.setString(10, bill.getBuildingName());
            ps.setString(11, bill.getFloorNumber());
            ps.setDouble(12, bill.getRentAmount());
            ps.setDouble(13, bill.getTotalAmount());

            int rowsAffected = ps.executeUpdate();

            if (rowsAffected > 0) {
                generatedKeys = ps.getGeneratedKeys();
                if (generatedKeys.next()) {
                    bill.setBillID(generatedKeys.getInt(1));
                    String verifyQuery = "SELECT tenant_id FROM Bills WHERE bill_id = ?";
                    try (PreparedStatement verifyPs = conn.prepareStatement(verifyQuery)) {
                        verifyPs.setInt(1, bill.getBillID());
                        ResultSet rs = verifyPs.executeQuery();
                        if (rs.next()) {
                            String savedTenantId = rs.getString("tenant_id");
                            System.out.println("Bill saved with ID: " + bill.getBillID() + ", tenant_id in DB: " + savedTenantId);
                        }
                    }
                }
                conn.commit();
                return true;
            } else {
                conn.rollback();
                return false;
            }
        } catch (SQLException e) {
            System.out.println("SQL Error in saveBill method: " + e.getMessage());
            e.printStackTrace();
            try {
                if (conn != null) {
                    conn.rollback();
                }
            } catch (SQLException ex) {
                System.out.println("Failed to rollback transaction: " + ex.getMessage());
            }
            return false;
        } finally {
            try {
                if (generatedKeys != null) generatedKeys.close();
                if (ps != null) ps.close();
                if (conn != null) {
                    conn.setAutoCommit(true);
                    conn.close();
                }
            } catch (SQLException e) {
                System.out.println("Error closing resources: " + e.getMessage());
            }
        }
    }

    public boolean updateBillPaymentStatus(int billID, boolean paid) {
        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = getConnection();

            String sql = "UPDATE Bills SET is_paid = ? WHERE bill_id = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setBoolean(1, paid);
            pstmt.setInt(2, billID);

            int affectedRows = pstmt.executeUpdate();

            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("Error updating bill payment status: " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            try {
                if (pstmt != null) pstmt.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                System.err.println("Error closing database resources: " + e.getMessage());
            }
        }
    }

    public boolean markBillAsPaid(int billId) {
        return updateBillPaymentStatus(billId, true);
    }

    public boolean deleteBill(int billId) {
        String query = "DELETE FROM Bills WHERE bill_id = ?";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setInt(1, billId);
            int rowsAffected = ps.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            System.out.println("SQL Error: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // ====================================================================================================
    // Bill Retrieval Methods
    // ====================================================================================================
    public List<Bill> getBillsByTenantId(String tenantIdCard) {
        List<Bill> bills = new ArrayList<>();
        String query = "SELECT b.*, bu.building_name, fl.floor_number, " +
                "r.current_electric_counter AS electric_usage, r.current_water_counter AS water_usage, " +
                "r.room_number, " +
                "u.IdCard AS tenant_id, u.name AS tenant_name, u.contact AS tenant_contact " +
                "FROM Bills b " +
                "JOIN Rooms r ON b.room_id = r.room_id " +
                "JOIN Floors fl ON r.floor_id = fl.floor_id " +
                "JOIN Buildings bu ON fl.building_id = bu.building_id " +
                "LEFT JOIN Users u ON b.tenant_id = u.IdCard " +
                "WHERE b.tenant_id = ? " +
                "ORDER BY b.bill_date DESC";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, tenantIdCard);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Bill bill = createBillFromResultSet(rs, conn);
                    if (bill != null) {
                        bills.add(bill);
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println("SQL Error in getBillsByTenantId: " + e.getMessage());
            e.printStackTrace();
        }
        return bills;
    }

    public List<Bill> getBillsByMonth(int year, int month) {
        List<Bill> bills = new ArrayList<>();
        String query = "SELECT b.*, bu.building_name, fl.floor_number, " +
                "r.current_electric_counter AS electric_usage, r.current_water_counter AS water_usage, " +
                "r.room_number, " +
                "u.IdCard AS tenant_id, u.name AS tenant_name, u.contact AS tenant_contact " +
                "FROM Bills b " +
                "JOIN Rooms r ON b.room_id = r.room_id " +
                "JOIN Floors fl ON r.floor_id = fl.floor_id " +
                "JOIN Buildings bu ON fl.building_id = bu.building_id " +
                "LEFT JOIN Users u ON b.tenant_id = u.IdCard " +
                "WHERE YEAR(b.bill_date) = ? AND MONTH(b.bill_date) = ? " +
                "ORDER BY bu.building_name, fl.floor_number, r.room_number";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, year);
            ps.setInt(2, month);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Bill bill = createBillFromResultSet(rs, conn);
                    if (bill != null) {
                        bills.add(bill);
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println("SQL Error in getBillsByMonth: " + e.getMessage());
            e.printStackTrace();
        }
        return bills;
    }

    public List<Bill> getUnpaidBills() {
        List<Bill> bills = new ArrayList<>();
        String query = "SELECT b.*, bu.building_name, fl.floor_number, " +
                "r.current_electric_counter AS electric_usage, r.current_water_counter AS water_usage, " +
                "r.room_number, " +
                "u.IdCard AS tenant_id, u.name AS tenant_name, u.contact AS tenant_contact " +
                "FROM Bills b " +
                "JOIN Rooms r ON b.room_id = r.room_id " +
                "JOIN Floors fl ON r.floor_id = fl.floor_id " +
                "JOIN Buildings bu ON fl.building_id = bu.building_id " +
                "LEFT JOIN Users u ON b.tenant_id = u.IdCard " +
                "WHERE b.is_paid = false " +
                "ORDER BY bu.building_name, fl.floor_number, r.room_number";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Bill bill = createBillFromResultSet(rs, conn);
                if (bill != null) {
                    bills.add(bill);
                }
            }
        } catch (SQLException e) {
            System.out.println("SQL Error: " + e.getMessage());
            e.printStackTrace();
        }
        return bills;
    }

    public List<Bill> getAllBills() {
        List<Bill> bills = new ArrayList<>();
        String query = "SELECT b.*, bu.building_name, fl.floor_number, " +
                "r.current_electric_counter AS electric_usage, r.current_water_counter AS water_usage, " +
                "r.room_number, " +
                "u.IdCard AS tenant_id, u.name AS tenant_name, u.contact AS tenant_contact " +
                "FROM Bills b " +
                "JOIN Rooms r ON b.room_id = r.room_id " +
                "JOIN Floors fl ON r.floor_id = fl.floor_id " +
                "JOIN Buildings bu ON fl.building_id = bu.building_id " +
                "LEFT JOIN Users u ON b.tenant_id = u.IdCard " +
                "ORDER BY bu.building_name, fl.floor_number, r.room_number";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Bill bill = createBillFromResultSet(rs, conn);
                if (bill != null) {
                    bills.add(bill);
                }
            }
        } catch (SQLException e) {
            System.out.println("SQL Error in getAllBills: " + e.getMessage());
            e.printStackTrace();
        }
        return bills;
    }

    public Map<String, Object> getCurrentBill(String tenantIdCard) {
        Map<String, Object> billDetails = new HashMap<>();
        String query = "SELECT b.*, bu.building_name, fl.floor_number, " +
                "r.current_electric_counter AS electric_usage, r.current_water_counter AS water_usage, " +
                "r.room_number " +
                "FROM Bills b " +
                "JOIN Rooms r ON b.room_id = r.room_id " +
                "JOIN Floors fl ON r.floor_id = fl.floor_id " +
                "JOIN Buildings bu ON fl.building_id = bu.building_id " +
                "WHERE b.tenant_id = ? AND b.is_paid = false " +
                "ORDER BY b.bill_date DESC LIMIT 1";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setString(1, tenantIdCard);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    billDetails.put("bill_id", rs.getInt("bill_id"));
                    billDetails.put("room_id", rs.getInt("room_id"));
                    billDetails.put("bill_date", rs.getDate("bill_date").toLocalDate());
                    billDetails.put("due_date", rs.getDate("due_date").toLocalDate());
                    billDetails.put("rent_amount", rs.getDouble("rent_amount"));
                    billDetails.put("electric_amount", rs.getDouble("electric_amount"));
                    billDetails.put("water_amount", rs.getDouble("water_amount"));
                    billDetails.put("total_amount", rs.getDouble("total_amount"));
                    billDetails.put("building_name", rs.getString("building_name"));
                    billDetails.put("floor_number", rs.getString("floor_number"));
                    billDetails.put("room_number", rs.getString("room_number"));
                    return billDetails;
                }
            }
        } catch (SQLException e) {
            System.out.println("SQL Error in getCurrentBill: " + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }

    // ====================================================================================================
    // Helper Methods
    // ====================================================================================================
    public int getRoomIdByBuildingFloorAndNumber(String buildingName, String floorNumber, String roomNumber) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = getConnection();

            String query = "SELECT r.room_id FROM Rooms r " +
                    "JOIN Floors f ON r.floor_id = f.floor_id " +
                    "JOIN Buildings b ON f.building_id = b.building_id " +
                    "WHERE b.building_name = ? AND f.floor_number = ? AND r.room_number = ?";

            ps = conn.prepareStatement(query);
            ps.setString(1, buildingName);
            ps.setString(2, floorNumber);
            ps.setString(3, roomNumber);

            rs = ps.executeQuery();

            if (rs.next()) {
                int roomId = rs.getInt("room_id");
                System.out.println("Found room ID: " + roomId + " for room " + roomNumber +
                        " in building " + buildingName + ", floor " + floorNumber);
                return roomId;
            } else {
                System.out.println("Room not found: " + roomNumber +
                        " in building " + buildingName + ", floor " + floorNumber);
                return -1;
            }
        } catch (SQLException e) {
            System.out.println("Error getting room ID: " + e.getMessage());
            e.printStackTrace();
            return -1;
        } finally {
            try {
                if (rs != null) rs.close();
                if (ps != null) ps.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                System.out.println("Error closing resources: " + e.getMessage());
            }
        }
    }

    private Bill createBillFromResultSet(ResultSet rs, Connection conn) throws SQLException {
        int roomId = rs.getInt("room_id");
        RoomDML roomDML = new RoomDML();
        Room room = roomDML.getRoomById(roomId);

        if (room == null) {
            System.out.println("Room with ID " + roomId + " not found.");
            return null;
        }

        String tenantIdCard = rs.getString("tenant_id");
        Tenant tenant = null;
        if (tenantIdCard != null && !tenantIdCard.isEmpty()) {
            String tenantName = rs.getString("tenant_name");
            String tenantContact = rs.getString("tenant_contact");
            if (tenantName != null && !tenantName.isEmpty()) {
                tenant = new Tenant(tenantName, tenantIdCard, tenantContact != null ? tenantContact : "");
                room.assignTenant(tenant);
            } else {
                System.out.println("Warning: Tenant with IdCard " + tenantIdCard + " has no name.");
            }
        } else {
            System.out.println("Warning: No tenant associated with bill ID " + rs.getInt("bill_id"));
        }

        try {
            Bill bill = new Bill(
                    room,
                    rs.getString("building_name"),
                    rs.getString("floor_number"),
                    rs.getDouble("rent_amount"),
                    rs.getInt("electric_usage"),
                    rs.getInt("water_usage")
            );

            bill.setBillID(rs.getInt("bill_id"));
            bill.setTenant(tenant);
            boolean isPaid = rs.getBoolean("is_paid");
            if (isPaid) {
                bill.markAsPaid(rs.getDouble("total_amount"));
            }
            LocalDate billDate = rs.getDate("bill_date").toLocalDate();
            LocalDate dueDate = rs.getDate("due_date").toLocalDate();
            bill.setBillDate(billDate);
            bill.setDueDate(dueDate);

            return bill;
        } catch (Exception e) {
            System.out.println("Error creating Bill instance: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

}