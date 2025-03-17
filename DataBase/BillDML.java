package DataBase;

import Payment.Bill;
import Properties.Room;
import Users.Tenant;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static DataBase.DataBaseConnection.getConnection;

public class BillDML {

    // Save a new bill to the database
    public boolean saveBill(Bill bill) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet generatedKeys = null;

        try {
            conn = getConnection();
            conn.setAutoCommit(false); // Start transaction

            String query = "INSERT INTO Bills (room_id, tenant_id, due_date, is_paid, bill_date, " +
                    "electric_amount, water_amount, electric_usage, water_usage, building_name, floor_number, " +
                    "rent_amount, total_amount) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

            ps = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);

            // Get the room ID
            Room room = bill.getRoom();
            RoomDML roomDML = new RoomDML();
            int roomId = roomDML.getRoomIdByRoomNumber(room.getRoomNumber());
            if (roomId == -1) {
                return false;
            }

            // Set parameters
            ps.setInt(1, roomId);
            ps.setString(2, bill.getTenant().getIdCard());
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

            // Execute the insert
            int rowsAffected = ps.executeUpdate();

            // Verify rows affected
            if (rowsAffected > 0) {
                // Retrieve the generated bill_id
                generatedKeys = ps.getGeneratedKeys();
                if (generatedKeys.next()) {
                    int generatedBillId = generatedKeys.getInt(1);
                    bill.setBillID(generatedBillId);
                }
                conn.commit(); // Commit the transaction
                return true;
            } else {
                conn.rollback(); // Rollback on failure
                return false;
            }

        } catch (SQLException e) {
            System.out.println("SQL Error in saveBill method: " + e.getMessage());
            e.printStackTrace();

            // Try to rollback on error
            try {
                if (conn != null) {
                    conn.rollback();
                }
            } catch (SQLException ex) {
                System.out.println("Failed to rollback transaction: " + ex.getMessage());
            }

            return false;
        } finally {
            // Close resources properly
            try {
                if (generatedKeys != null) generatedKeys.close();
                if (ps != null) ps.close();
                if (conn != null) {
                    conn.setAutoCommit(true); // Reset auto-commit
                    conn.close();
                }
            } catch (SQLException e) {
                System.out.println("Error closing resources: " + e.getMessage());
            }
        }
    }


    // Update methods to use integer IDs
    public boolean updateBillPaymentStatus(int billID, boolean paid) {
        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            // Get database connection
            conn = getConnection();

            String sql = "UPDATE Bills SET is_paid = ? WHERE bill_id = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setBoolean(1, paid);
            pstmt.setInt(2, billID);

            // Execute update and check affected rows
            int affectedRows = pstmt.executeUpdate();

            // Print debug information
            System.out.println("Bill update affected rows: " + affectedRows + " for bill ID: " + billID);

            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("Error updating bill payment status: " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            // Properly close resources to prevent leaks
            try {
                if (pstmt != null) pstmt.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                System.err.println("Error closing database resources: " + e.getMessage());
            }
        }
    }

    // Update markBillAsPaid to use integer ID
    public boolean markBillAsPaid(int billId) {
        return updateBillPaymentStatus(billId, true);
    }

    // Update deleteBill to use integer ID
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

    // Get tenant balance due
    public double getTenantBalanceDue(String tenantId) {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        double balance = 0.0;

        try {
            conn = getConnection();

            // Query to get the sum of outstanding bill amounts for the tenant
            String query = "SELECT SUM(total_amount) AS balance " +
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

        String query = "SELECT b.*, bu.building_name, fl.floor_number, r.current_electric_counter AS electric_usage, r.current_water_counter AS water_usage " +
                "FROM Bills b " +
                "JOIN Rooms r ON b.room_id = r.room_id " +
                "JOIN Floors fl ON r.floor_id = fl.floor_id " +
                "JOIN Buildings bu ON fl.building_id = bu.building_id " +
                "JOIN Tenants t ON r.room_id = t.assigned_room_id " +
                "JOIN Users u ON t.user_id = u.user_id " +
                "WHERE u.IdCard = ? " +
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
            System.out.println("SQL Error: " + e.getMessage());
            e.printStackTrace();
        }

        return bills;
    }

    // Get bills for a specific month
    public List<Bill> getBillsByMonth(int year, int month) {
        List<Bill> bills = new ArrayList<>();

        String query = "SELECT b.*, bu.building_name, fl.floor_number, r.current_electric_counter AS electric_usage, r.current_water_counter AS water_usage " +
                "FROM Bills b " +
                "JOIN Rooms r ON b.room_id = r.room_id " +
                "JOIN Floors fl ON r.floor_id = fl.floor_id " +
                "JOIN Buildings bu ON fl.building_id = bu.building_id " +
                "WHERE YEAR(b.bill_date) = ? AND MONTH(b.bill_date) = ? " +
                "ORDER BY b.bill_date";

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
            System.out.println("SQL Error: " + e.getMessage());
            e.printStackTrace();
        }

        return bills;
    }

    // Get unpaid bills
    public List<Bill> getUnpaidBills() {
        List<Bill> bills = new ArrayList<>();

        String query = "SELECT b.*, bu.building_name, fl.floor_number, r.current_electric_counter AS electric_usage, r.current_water_counter AS water_usage " +
                "FROM Bills b " +
                "JOIN Rooms r ON b.room_id = r.room_id " +
                "JOIN Floors fl ON r.floor_id = fl.floor_id " +
                "JOIN Buildings bu ON fl.building_id = bu.building_id " +
                "WHERE b.is_paid = false ORDER BY b.due_date";

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

    // Helper method to create Bill from ResultSet
    private Bill createBillFromResultSet(ResultSet rs, Connection conn) throws SQLException {
        // Get room information
        int roomId = rs.getInt("room_id");

        RoomDML roomDML = new RoomDML();
        Room room = roomDML.getRoomById(roomId);

        if (room == null) {
            System.out.println("Room with ID " + roomId + " not found.");
            return null;
        }

        // Create the Bill instance
        try {
            Bill bill = createBillInstance(
                    room,
                    rs.getString("building_name"),
                    rs.getString("floor_number"),
                    rs.getDouble("rent_amount"),
                    rs.getInt("electric_usage"),
                    rs.getInt("water_usage")
            );

            // Set additional fields
            bill.setBillID(rs.getInt("bill_id"));

            // Set the payment status from the database
            boolean isPaid = rs.getBoolean("is_paid");
            if (isPaid) {
                // Mark the bill as paid with the total amount
                bill.markAsPaid(rs.getDouble("total_amount"));
            }

            // Set other bill properties if needed
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



    // Helper method to create Bill instance
    private Bill createBillInstance(Room room, String buildingName, String floorNumber,
                                    double rentAmount, int electricUsage, int waterUsage) {
        try {
            return new Bill(room, buildingName, floorNumber, rentAmount, electricUsage, waterUsage);
        } catch (Exception e) {
            System.out.println("Failed to create Bill instance: " + e.getMessage());
            return null;
        }
    }



    // Create the Bills table if it doesn't exist
    public void createBillsTableIfNotExists() {
        String createTableSQL = "CREATE TABLE IF NOT EXISTS Bills (" +
                "bill_id VARCHAR(50) PRIMARY KEY, " +  // Changed from INT to VARCHAR
                "room_id INT NOT NULL, " +
                "tenant_id VARCHAR(50), " +
                "building_name VARCHAR(100), " +
                "floor_number VARCHAR(20), " +
                "bill_date DATE DEFAULT CURRENT_DATE, " +
                "due_date DATE NOT NULL, " +
                "rent_amount DOUBLE, " +
                "electric_amount DOUBLE, " +
                "water_amount DOUBLE, " +
                "total_amount DOUBLE, " +
                "is_paid BOOLEAN DEFAULT FALSE, " +
                "electric_usage INT, " +
                "water_usage INT, " +
                "FOREIGN KEY (room_id) REFERENCES Rooms(room_id) ON DELETE CASCADE" +
                ") ENGINE=InnoDB";

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            stmt.execute(createTableSQL);
            System.out.println("Bills table created or already exists");

        } catch (SQLException e) {
            System.out.println("SQL Error in creating Bills table: " + e.getMessage());
            e.printStackTrace();
        }
    }

}