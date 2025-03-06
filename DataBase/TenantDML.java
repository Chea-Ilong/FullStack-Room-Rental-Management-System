package DataBase;

import Users.Tenant;
import Properties.Room;
import java.sql.*;

public class TenantDML {

    // Save a new tenant to the database
    public void saveTenant(Tenant tenant) {
        Connection conn = null;
        PreparedStatement userStmt = null;
        PreparedStatement tenantStmt = null;
        ResultSet generatedKeys = null;

        try {
            conn = DataBaseConnection.getConnection();
            conn.setAutoCommit(false); // Start transaction

            // First, save to Users table
            String userQuery = "INSERT INTO Users (name, IdCard, contact, role) VALUES (?, ?, ?, ?)";
            userStmt = conn.prepareStatement(userQuery, Statement.RETURN_GENERATED_KEYS);

            userStmt.setString(1, tenant.getName());
            userStmt.setString(2, tenant.getIdCard());
            userStmt.setString(3, tenant.getContact());
            userStmt.setString(4, "Tenant"); // Role is always "Tenant" for Tenant objects

            int userRowsAffected = userStmt.executeUpdate();
            if (userRowsAffected <= 0) {
                throw new SQLException("Failed to create user record");
            }

            // Get the generated user_id
            generatedKeys = userStmt.getGeneratedKeys();
            if (!generatedKeys.next()) {
                throw new SQLException("Failed to get user_id");
            }
            int userId = generatedKeys.getInt(1);

            // Then, save to Tenants table
            String tenantQuery = "INSERT INTO Tenants (user_id, assigned_room_id, rent_paid, balance_due) VALUES (?, ?, ?, ?)";
            tenantStmt = conn.prepareStatement(tenantQuery);

            tenantStmt.setInt(1, userId);

            // Check if tenant has an assigned room
            Room assignedRoom = tenant.getAssignedRoom();
            if (assignedRoom != null) {
                tenantStmt.setInt(2, getRoomIdFromDatabase(assignedRoom, conn));
                tenantStmt.setBoolean(3, false); // Initial rent_paid status
                tenantStmt.setDouble(4, assignedRoom.getRent()); // Initial balance due is the room rent
            } else {
                tenantStmt.setNull(2, java.sql.Types.INTEGER);
                tenantStmt.setBoolean(3, true); // No room, so no rent to pay
                tenantStmt.setDouble(4, 0.0); // No balance due
            }

            int tenantRowsAffected = tenantStmt.executeUpdate();
            if (tenantRowsAffected <= 0) {
                throw new SQLException("Failed to create tenant record");
            }

            // Commit the transaction
            conn.commit();
            System.out.println("Tenant saved successfully! User ID: " + userId);

        } catch (SQLException e) {
            try {
                if (conn != null) {
                    conn.rollback(); // Rollback transaction on error
                }
            } catch (SQLException ex) {
                System.out.println("Failed to rollback: " + ex.getMessage());
            }

            System.out.println("SQL Error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // Close resources
            try {
                if (generatedKeys != null) generatedKeys.close();
                if (tenantStmt != null) tenantStmt.close();
                if (userStmt != null) userStmt.close();
                if (conn != null) {
                    conn.setAutoCommit(true); // Reset auto-commit
                    conn.close();
                }
            } catch (SQLException e) {
                System.out.println("Error closing resources: " + e.getMessage());
            }
        }
    }

    // Helper method to get room_id from the database
    private int getRoomIdFromDatabase(Room room, Connection conn) throws SQLException {
        String query = "SELECT room_id FROM Rooms WHERE room_number = ?";
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, room.getRoomNumber());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("room_id");
                } else {
                    throw new SQLException("Room not found in database: " + room.getRoomNumber());
                }
            }
        }
    }

    // Get tenant by ID
    public Tenant getTenantById(int tenantId) {
        String query = "SELECT u.name, u.IdCard, u.contact, t.rent_paid, t.balance_due, r.room_number " +
                "FROM Tenants t " +
                "JOIN Users u ON t.user_id = u.user_id " +
                "LEFT JOIN Rooms r ON t.assigned_room_id = r.room_id " +
                "WHERE t.tenant_id = ?";

        try (Connection conn = DataBaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setInt(1, tenantId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String name = rs.getString("name");
                    String idCard = rs.getString("IdCard");
                    String contact = rs.getString("contact");

                    // Create and return the tenant object
                    Tenant tenant = new Tenant(name, idCard, contact);
                    // You would need to set the room and other details here
                    // This would require creating the Room object and assigning it

                    return tenant;
                }
            }
        } catch (SQLException e) {
            System.out.println("SQL Error: " + e.getMessage());
            e.printStackTrace();
        }

        return null; // Tenant not found
    }
}