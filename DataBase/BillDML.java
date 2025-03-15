package DataBase;

import Payment.Bill;
import Properties.Room;
import Users.Tenant;
import java.sql.*;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

public class BillDML {

    // Save a new bill to the database
    public boolean saveBill(Bill bill) {
        String query = "INSERT INTO Bills (bill_id, room_id, tenant_id, building_name, floor_number, " +
                "bill_date, due_date, rent_amount, electric_amount, water_amount, " +
                "total_amount, is_paid, electric_usage, water_usage) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DataBaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            // Get the tenant and room IDs
            Room room = bill.getRoom();
            Tenant tenant = bill.getTenant();

            // Get room_id and tenant_id from database
            RoomDML roomDML = new RoomDML();
            int roomId = roomDML.getRoomIdByRoomNumber(room.getRoomNumber());

            TenantDML tenantDML = new TenantDML();
            int tenantId = tenantDML.getTenantIdByIdCard(tenant.getIdCard());

            // Set parameters
            ps.setString(1, getOrGenerateBillId(bill));
            ps.setInt(2, roomId);
            ps.setInt(3, tenantId);
            ps.setString(4, bill.getBuildingName());
            ps.setString(5, bill.getFloorNumber());
            ps.setDate(6, java.sql.Date.valueOf(bill.getBillDate()));
            ps.setDate(7, java.sql.Date.valueOf(bill.getDueDate()));
            ps.setDouble(8, bill.getRentAmount());
            ps.setDouble(9, bill.getElectricAmount());
            ps.setDouble(10, bill.getWaterAmount());
            ps.setDouble(11, bill.getTotalAmount());
            ps.setBoolean(12, bill.isPaid());
            ps.setInt(13, bill.getElectricUsage());
            ps.setInt(14, bill.getWaterUsage());

            int rowsAffected = ps.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            System.out.println("SQL Error: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // Helper method to get or generate bill ID
    private String getOrGenerateBillId(Bill bill) {
        // If the bill has an existing ID, use it
        // This assumes Bill class has a getter for billID
        try {
            java.lang.reflect.Method getBillIdMethod = bill.getClass().getDeclaredMethod("getBillID");
            getBillIdMethod.setAccessible(true);
            String billId = (String) getBillIdMethod.invoke(bill);
            if (billId != null && !billId.isEmpty()) {
                return billId;
            }
        } catch (Exception e) {
            // If method not found or error, generate new ID
        }

        // Generate a new ID based on timestamp and random number
        return "BILL-" + System.currentTimeMillis() + "-" + (int)(Math.random() * 10000);
    }

    // Update bill status to paid
    public boolean markBillAsPaid(String billId) {
        String query = "UPDATE Bills SET is_paid = true WHERE bill_id = ?";

        try (Connection conn = DataBaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setString(1, billId);
            int rowsAffected = ps.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            System.out.println("SQL Error: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    // Add this method to BillDML class
    public double getTenantBalanceDue(String tenantId) {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        double balance = 0.0;

        try {
            conn = DataBaseConnection.getConnection();

            // Query to get the sum of outstanding bill amounts for the tenant
            String query = "SELECT SUM(b.total_amount) AS balance " +
                    "FROM Bills b " +
                    "JOIN Tenants t ON b.tenant_id = t.tenant_id " +
                    "JOIN Users u ON t.user_id = u.user_id " +
                    "WHERE u.IdCard = ? AND b.is_paid = FALSE";

            stmt = conn.prepareStatement(query);
            stmt.setString(1, tenantId);

            rs = stmt.executeQuery();
            if (rs.next()) {
                balance = rs.getDouble("balance");
            }

        } catch (SQLException e) {
            System.out.println("SQL Error retrieving tenant balance: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                System.out.println("Error closing resources: " + e.getMessage());
            }
        }

        return balance;
    }
    // Get bills by tenant ID
    public List<Bill> getBillsByTenantId(String tenantIdCard) {
        List<Bill> bills = new ArrayList<>();

        String query = "SELECT b.* FROM Bills b " +
                "JOIN Tenants t ON b.tenant_id = t.tenant_id " +
                "JOIN Users u ON t.user_id = u.user_id " +
                "WHERE u.IdCard = ? " +
                "ORDER BY b.bill_date DESC";

        try (Connection conn = DataBaseConnection.getConnection();
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
            System.out.println("SQL Error: " + e.getMessage());
            e.printStackTrace();
        }

        return bills;
    }

    // Get bills for a specific month
    public List<Bill> getBillsByMonth(int year, int month) {
        List<Bill> bills = new ArrayList<>();

        String query = "SELECT * FROM Bills WHERE YEAR(bill_date) = ? AND MONTH(bill_date) = ? " +
                "ORDER BY bill_date";

        try (Connection conn = DataBaseConnection.getConnection();
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
            System.out.println("SQL Error: " + e.getMessage());
            e.printStackTrace();
        }

        return bills;
    }

    // Get unpaid bills
    public List<Bill> getUnpaidBills() {
        List<Bill> bills = new ArrayList<>();

        String query = "SELECT * FROM Bills WHERE is_paid = false ORDER BY due_date";

        try (Connection conn = DataBaseConnection.getConnection();
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

    // Helper method to create Bill from ResultSet
    private Bill createBillFromResultSet(ResultSet rs, Connection conn) throws SQLException {
        // Get room and tenant information
        int roomId = rs.getInt("room_id");
        int tenantId = rs.getInt("tenant_id");

        RoomDML roomDML = new RoomDML();
        Room room = roomDML.getRoomById(roomId);

        if (room == null) {
            System.out.println("Room with ID " + roomId + " not found.");
            return null;
        }

        // Because we need to create a Bill with proper constructor parameters
        // We're creating a "reconstructed" bill using reflection
        try {
            // Create Bill instance using reflection since we don't have access to add new constructors
            Bill bill = createBillInstance(
                    room,
                    rs.getString("building_name"),
                    rs.getString("floor_number"),
                    rs.getDouble("rent_amount"),
                    rs.getInt("electric_usage"),
                    rs.getInt("water_usage")
            );

            // Set additional fields using reflection if needed
            // This could be refined further based on Bill class structure

            return bill;
        } catch (Exception e) {
            System.out.println("Error creating Bill instance: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    // Helper method to create Bill instance using reflection
    private Bill createBillInstance(Room room, String buildingName, String floorNumber,
                                    double rentAmount, int electricUsage, int waterUsage) {
        try {
            return new Bill(room, buildingName, floorNumber, rentAmount, electricUsage, waterUsage);
        } catch (Exception e) {
            System.out.println("Failed to create Bill instance: " + e.getMessage());
            return null;
        }
    }

    // Delete bill by ID
    public boolean deleteBill(String billId) {
        String query = "DELETE FROM Bills WHERE bill_id = ?";

        try (Connection conn = DataBaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setString(1, billId);
            int rowsAffected = ps.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            System.out.println("SQL Error: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // Create the Bills table if it doesn't exist
    public void createBillsTableIfNotExists() {
        String createTableSQL = "CREATE TABLE IF NOT EXISTS Bills (" +
                "bill_id VARCHAR(50) PRIMARY KEY, " +
                "room_id INT, " +
                "tenant_id INT, " +
                "building_name VARCHAR(100), " +
                "floor_number VARCHAR(20), " +
                "bill_date DATE, " +
                "due_date DATE, " +
                "rent_amount DOUBLE, " +
                "electric_amount DOUBLE, " +
                "water_amount DOUBLE, " +
                "total_amount DOUBLE, " +
                "is_paid BOOLEAN, " +
                "electric_usage INT, " +
                "water_usage INT, " +
                "FOREIGN KEY (room_id) REFERENCES Rooms(room_id), " +
                "FOREIGN KEY (tenant_id) REFERENCES Tenants(tenant_id)" +
                ")";

        try (Connection conn = DataBaseConnection.getConnection();
             Statement stmt = conn.createStatement()) {

            stmt.execute(createTableSQL);
            System.out.println("Bills table created or already exists");

        } catch (SQLException e) {
            System.out.println("SQL Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}