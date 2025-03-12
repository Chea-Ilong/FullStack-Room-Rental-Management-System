package DataBase;

import DataBase.DataBaseConnection;
import Exceptions.RoomException;
import Exceptions.TenantException;
import Users.Tenant;
import Properties.Room;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class TenantDML {

    // ===== Get All Tenants for Landlord =====
    public List<Tenant> getAllTenantsForLandlord() {
        List<Tenant> tenants = new ArrayList<>();
        String query = "SELECT u.user_id, u.name, u.IdCard, u.contact, " +
                "r.room_number, r.rent, r.current_electric_counter, r.current_water_counter " +
                "FROM Users u " +
                "JOIN Tenants t ON u.user_id = t.user_id " +
                "LEFT JOIN Rooms r ON t.assigned_room_id = r.room_id " +
                "WHERE u.role = 'Tenant'";

        try (Connection conn = DataBaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Tenant tenant = createTenantFromResultSet(rs);
                tenants.add(tenant);
            }
        } catch (SQLException e) {
            System.out.println("SQL Error loading tenants: " + e.getMessage());
            e.printStackTrace();
        }

        return tenants;
    }

    // ===== Check if Tenant Exists =====
    public boolean tenantExists(String idCard) {
        String query = "SELECT COUNT(*) FROM Users WHERE IdCard = ? AND role = 'Tenant'";

        try (Connection conn = DataBaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setString(1, idCard);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.out.println("SQL Error: " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }

    // ===== Update Utility Payment Status =====
    public void updateUtilityPaymentStatus(String tenantId, LocalDate usageDate, boolean isPaid) {
        try (Connection conn = DataBaseConnection.getConnection()) {
            int roomId = getRoomIdForTenant(tenantId, conn);
            if (roomId == -1) {
                System.out.println("No room found for tenant with ID: " + tenantId);
                return;
            }

            updateUtilityPayment(conn, roomId, usageDate, isPaid);
        } catch (SQLException e) {
            System.out.println("SQL Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ===== Get Tenant by Credentials =====
    public Tenant getTenantByCredentials(String username, String password) {
        String query = "SELECT u.name, u.IdCard, u.contact, r.room_number, r.room_id, " +
                "r.current_electric_counter, r.current_water_counter, r.rent " +
                "FROM Users u " +
                "LEFT JOIN Tenants t ON u.user_id = t.user_id " +
                "LEFT JOIN Rooms r ON t.assigned_room_id = r.room_id " +
                "WHERE u.name = ? AND u.IdCard = ? AND u.role = 'Tenant'";

        try (Connection conn = DataBaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setString(1, username);
            ps.setString(2, password);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return createTenantFromResultSet(rs);
                }
            }
        } catch (SQLException e) {
            System.out.println("SQL Error: " + e.getMessage());
            e.printStackTrace();
        }

        return null; // No matching tenant found
    }

    // ===== Save a New Tenant =====
    public boolean saveTenant(Tenant tenant) {
        if (tenantExists(tenant.getIdCard())) {
            System.out.println("Error: Tenant with ID Card " + tenant.getIdCard() + " already exists in database.");
            return false;
        }

        try (Connection conn = DataBaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            int userId = insertUser(conn, tenant);
            insertTenant(conn, userId, tenant);
            conn.commit();
            System.out.println("Tenant saved successfully! User ID: " + userId);
            return true;
        } catch (SQLException e) {
            System.out.println("SQL Error: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // ===== Update Tenant =====
    public boolean updateTenant(Tenant tenant) {
        String query = "UPDATE Users u " +
                "JOIN Tenants t ON u.user_id = t.user_id " +
                "SET u.name = ?, u.contact = ? " +
                "WHERE u.IdCard = ? AND u.role = 'Tenant'";

        try (Connection conn = DataBaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setString(1, tenant.getName());
            ps.setString(2, tenant.getContact());
            ps.setString(3, tenant.getIdCard());

            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Tenant updated successfully: " + tenant.getName());
                if (tenant.getAssignedRoom() != null) {
                    updateTenantRoomAssignment(tenant);
                }
                return true;
            } else {
                System.out.println("No tenant found with ID Card: " + tenant.getIdCard());
                return false;
            }
        } catch (SQLException e) {
            System.out.println("SQL Error updating tenant: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // ===== Get Tenant by ID =====
    public Tenant getTenantById(int tenantId) {
        String query = "SELECT u.name, u.IdCard, u.contact, r.room_number, r.room_id, " +
                "r.current_electric_counter, r.current_water_counter, r.rent " +
                "FROM Tenants t " +
                "JOIN Users u ON t.user_id = u.user_id " +
                "LEFT JOIN Rooms r ON t.assigned_room_id = r.room_id " +
                "WHERE t.tenant_id = ?";

        try (Connection conn = DataBaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setInt(1, tenantId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return createTenantFromResultSet(rs);
                }
            }
        } catch (SQLException e) {
            System.out.println("SQL Error: " + e.getMessage());
            e.printStackTrace();
        }

        return null; // Tenant not found
    }

    // ===== Helper Methods =====

    private Tenant createTenantFromResultSet(ResultSet rs) throws SQLException {
        String name = rs.getString("name");
        String idCard = rs.getString("IdCard");
        String contact = rs.getString("contact");
        Tenant tenant = new Tenant(name, idCard, contact);

        String roomNumber = rs.getString("room_number");
        if (roomNumber != null) {
            int electricCounter = rs.getInt("current_electric_counter");
            int waterCounter = rs.getInt("current_water_counter");
            double rent = rs.getDouble("rent");

            Room room = new Room(roomNumber, electricCounter, waterCounter);
            room.setRent(rent);

            try {
                tenant.assignRoom(room);
            } catch (RoomException | TenantException e) {
                System.out.println("Error assigning room to tenant: " + e.getMessage());
            }
        }

        return tenant;
    }

    private int getRoomIdForTenant(String tenantId, Connection conn) throws SQLException {
        String query = "SELECT r.room_id FROM Rooms r " +
                "JOIN Tenants t ON r.room_id = t.assigned_room_id " +
                "JOIN Users u ON t.user_id = u.user_id " +
                "WHERE u.IdCard = ?";

        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, tenantId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt("room_id") : -1;
            }
        }
    }

    private void updateUtilityPayment(Connection conn, int roomId, LocalDate usageDate, boolean isPaid) throws SQLException {
        String query = "UPDATE UtilityUsage " +
                "SET is_paid = ? " +
                "WHERE room_id = ? AND usage_date = ?";

        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setBoolean(1, isPaid);
            ps.setInt(2, roomId);
            ps.setDate(3, java.sql.Date.valueOf(usageDate));

            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Utility payment status updated successfully.");
            } else {
                System.out.println("No utility usage record found for date: " + usageDate);
            }
        }
    }

    private int insertUser(Connection conn, Tenant tenant) throws SQLException {
        String query = "INSERT INTO Users (name, IdCard, contact, role) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, tenant.getName());
            ps.setString(2, tenant.getIdCard());
            ps.setString(3, tenant.getContact());
            ps.setString(4, "Tenant");

            int rowsAffected = ps.executeUpdate();
            if (rowsAffected <= 0) {
                throw new SQLException("Failed to create user record");
            }

            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1);
                } else {
                    throw new SQLException("Failed to get user_id");
                }
            }
        }
    }

    private void insertTenant(Connection conn, int userId, Tenant tenant) throws SQLException {
        String query = "INSERT INTO Tenants (user_id, assigned_room_id, rent_paid, balance_due) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, userId);

            Room assignedRoom = tenant.getAssignedRoom();
            if (assignedRoom != null) {
                ps.setInt(2, getRoomIdFromDatabase(assignedRoom, conn));
                ps.setBoolean(3, false);
                ps.setDouble(4, assignedRoom.getRent());
            } else {
                ps.setNull(2, java.sql.Types.INTEGER);
                ps.setBoolean(3, true);
                ps.setDouble(4, 0.0);
            }

            int rowsAffected = ps.executeUpdate();
            if (rowsAffected <= 0) {
                throw new SQLException("Failed to create tenant record");
            }
        }
    }

    private boolean updateTenantRoomAssignment(Tenant tenant) {
        String query = "UPDATE Tenants t " +
                "JOIN Users u ON t.user_id = u.user_id " +
                "SET t.assigned_room_id = ? " +
                "WHERE u.IdCard = ? AND u.role = 'Tenant'";

        try (Connection conn = DataBaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            Room room = tenant.getAssignedRoom();
            if (room != null) {
                ps.setInt(1, getRoomIdFromDatabase(room, conn));
            } else {
                ps.setNull(1, java.sql.Types.INTEGER);
            }
            ps.setString(2, tenant.getIdCard());

            int rowsAffected = ps.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.out.println("SQL Error updating room assignment: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

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
    // ===== Delete Tenant =====
    public boolean deleteTenant(String idCard) {
        String query = "DELETE FROM Users WHERE IdCard = ? AND role = 'Tenant'";

        try (Connection conn = DataBaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setString(1, idCard);

            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Tenant deleted successfully: " + idCard);
                return true;
            } else {
                System.out.println("No tenant found with ID Card: " + idCard);
                return false;
            }
        } catch (SQLException e) {
            System.out.println("SQL Error deleting tenant: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}
