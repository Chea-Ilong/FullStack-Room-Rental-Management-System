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

    public List<Room> getAllAssignedRoomsForTenant(String tenantIdCard) {
        List<Room> rooms = new ArrayList<>();
        String query = "SELECT r.room_id, r.room_number, r.current_electric_counter, " +
                "r.current_water_counter, r.is_occupied " +
                "FROM Rooms r " +
                "JOIN Tenants t ON r.room_id = t.assigned_room_id " +
                "JOIN Users u ON t.user_id = u.user_id " +
                "WHERE u.IdCard = ?";

        try (Connection conn = DataBaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setString(1, tenantIdCard);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String roomNumber = rs.getString("room_number");
                    int electricCounter = rs.getInt("current_electric_counter");
                    int waterCounter = rs.getInt("current_water_counter");

                    Room room = new Room(roomNumber, electricCounter, waterCounter);
                    rooms.add(room);
                }
            }
        } catch (SQLException e) {
            System.out.println("SQL Error loading tenant rooms: " + e.getMessage());
            e.printStackTrace();
        }

        return rooms;
    }

    // ===== Get All Tenants for Landlord =====
    // TenantDML.java
    public List<Tenant> getAllTenantsForLandlord() {
        List<Tenant> tenants = new ArrayList<>();
        String query = "SELECT u.user_id, u.name, u.IdCard, u.contact, " +
                "r.room_number, r.current_electric_counter, r.current_water_counter, " +
                "f.floor_number, b.building_name " +
                "FROM Users u " +
                "JOIN Tenants t ON u.user_id = t.user_id " +
                "LEFT JOIN Rooms r ON t.assigned_room_id = r.room_id " +
                "LEFT JOIN Floors f ON r.floor_id = f.floor_id " +
                "LEFT JOIN Buildings b ON f.building_id = b.building_id " +
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

    private Tenant createTenantFromResultSet(ResultSet rs) throws SQLException {
        String name = rs.getString("name");
        String idCard = rs.getString("IdCard");
        String contact = rs.getString("contact");
        Tenant tenant = new Tenant(name, idCard, contact);

        String roomNumber = rs.getString("room_number");
        if (roomNumber != null) {
            int electricCounter = rs.getInt("current_electric_counter");
            int waterCounter = rs.getInt("current_water_counter");

            Room room = new Room(roomNumber, electricCounter, waterCounter);

            try {
                tenant.assignRoom(room);
            } catch (RoomException | TenantException e) {
                System.out.println("Error assigning room to tenant: " + e.getMessage());
            }
        }

        return tenant;
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

    // ===== Get Tenant by Credentials =====
    public Tenant getTenantByCredentials(String username, String password) {
        System.out.println("Looking for tenant with username: " + username + " and ID Card: " + password);
        String query = "SELECT u.name, u.IdCard, u.contact, r.room_number, r.room_id, " +
                "r.current_electric_counter, r.current_water_counter " +
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
                    System.out.println("Found tenant: " + rs.getString("name") + " with ID Card: " + rs.getString("IdCard"));
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
    public boolean updateTenant(Tenant tenant, String oldIdCard) {
        String query = "UPDATE Users u " +
                "JOIN Tenants t ON u.user_id = t.user_id " +
                "SET u.name = ?, u.contact = ?" +
                (oldIdCard != null ? ", u.IdCard = ?" : "") +
                " WHERE u.IdCard = ? AND u.role = 'Tenant'";

        try (Connection conn = DataBaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setString(1, tenant.getName());
            ps.setString(2, tenant.getContact());

            int paramIndex = 3;
            if (oldIdCard != null) {
                ps.setString(paramIndex++, tenant.getIdCard());
            }
            ps.setString(paramIndex, oldIdCard != null ? oldIdCard : tenant.getIdCard());

            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Tenant updated successfully: " + tenant.getName());
                if (tenant.getAssignedRoom() != null) {
                    updateTenantRoomAssignment(tenant);
                }
                return true;
            } else {
                System.out.println("No tenant found with ID Card: " + (oldIdCard != null ? oldIdCard : tenant.getIdCard()));
                return false;
            }
        } catch (SQLException e) {
            System.out.println("SQL Error updating tenant: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public List<Tenant> getAllTenants(String landlordId) {
        List<Tenant> tenants = new ArrayList<>();

        try (Connection conn = DataBaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT t.tenant_id, u.name, u.contact, r.room_number, f.floor_number, b.building_name, " +
                             "r.current_electric_counter, r.current_water_counter " +
                             "FROM Tenants t " +
                             "JOIN Users u ON t.user_id = u.user_id " +
                             "LEFT JOIN Rooms r ON t.assigned_room_id = r.room_id " +
                             "LEFT JOIN Floors f ON r.floor_id = f.floor_id " +
                             "LEFT JOIN Buildings b ON f.building_id = b.building_id " +
                             "WHERE EXISTS (SELECT 1 FROM Landlords l WHERE l.landlord_id = ? AND l.user_id = u.user_id)")) {

            stmt.setString(1, landlordId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String tenantId = rs.getString("tenant_id");
                String name = rs.getString("name");
                String contact = rs.getString("contact");

                Tenant tenant = new Tenant(name, tenantId, contact);

                // If there's an assigned room
                String roomNumber = rs.getString("room_number");
                if (roomNumber != null) {
                    int electricCounter = rs.getInt("current_electric_counter");
                    int waterCounter = rs.getInt("current_water_counter");

                    Room room = new Room(roomNumber, electricCounter, waterCounter);
                    tenant.assignRoom(room); // Assuming this method exists in Tenant class

                    // Set the tenant back to the room
                    room.setTenantDirectly(tenant);
                }

                tenants.add(tenant);
            }
        } catch (SQLException | RoomException | TenantException e) {
            e.printStackTrace();
            return null;
        }

        return tenants;
    }
    public List<Tenant> getAllTenants() {
        List<Tenant> tenants = new ArrayList<>();
        String query = "SELECT t.tenant_id, u.name, u.IdCard, u.contact, r.room_number, r.room_id, " +
                "r.current_electric_counter, r.current_water_counter " +
                "FROM Tenants t " +
                "JOIN Users u ON t.user_id = u.user_id " +
                "LEFT JOIN Rooms r ON t.assigned_room_id = r.room_id";

        try (Connection conn = DataBaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                tenants.add(createTenantFromResultSet(rs));
            }
        } catch (SQLException e) {
            System.out.println("SQL Error: " + e.getMessage());
            e.printStackTrace();
        }

        return tenants;
    }
    /**
     * Retrieves a specific tenant by ID
     * @param tenantId The ID of the tenant
     * @param landlordId The ID of the landlord
     * @return Tenant object or null if not found
     */
    public Tenant getTenantById(String tenantId, String landlordId) {
        try (Connection conn = DataBaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT u.name, u.contact, r.room_number, f.floor_number, b.building_name, " +
                             "r.current_electric_counter, r.current_water_counter " +
                             "FROM Tenants t " +
                             "JOIN Users u ON t.user_id = u.user_id " +
                             "LEFT JOIN Rooms r ON t.assigned_room_id = r.room_id " +
                             "LEFT JOIN Floors f ON r.floor_id = f.floor_id " +
                             "LEFT JOIN Buildings b ON f.building_id = b.building_id " +
                             "WHERE t.tenant_id = ? AND EXISTS (SELECT 1 FROM Landlords l WHERE l.landlord_id = ?)")) {

            stmt.setString(1, tenantId);
            stmt.setString(2, landlordId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String name = rs.getString("name");
                String contact = rs.getString("contact");

                Tenant tenant = new Tenant(name, tenantId, contact);

                // If there's an assigned room
                String roomNumber = rs.getString("room_number");
                if (roomNumber != null) {
                    int electricCounter = rs.getInt("current_electric_counter");
                    int waterCounter = rs.getInt("current_water_counter");

                    Room room = new Room(roomNumber, electricCounter, waterCounter);
                    tenant.assignRoom(room); // Assuming this method exists in Tenant class

                    // Set the tenant back to the room
                    room.setTenantDirectly(tenant);
                }

                return tenant;
            }
        } catch (SQLException | RoomException | TenantException e) {
            e.printStackTrace();
        }

        return null;
    }
//     ===== Get Tenant by ID =====
    public Tenant getTenantById(String tenantId) {
        String query = "SELECT u.name, u.IdCard, u.contact, r.room_number, r.room_id, " +
                "r.current_electric_counter, r.current_water_counter " +
                "FROM Tenants t " +
                "JOIN Users u ON t.user_id = u.user_id " +
                "LEFT JOIN Rooms r ON t.assigned_room_id = r.room_id " +
                "WHERE t.tenant_id = ?";

        try (Connection conn = DataBaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setString(1, tenantId);

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

//    private Tenant createTenantFromResultSet(ResultSet rs) throws SQLException {
//        String name = rs.getString("name");
//        String idCard = rs.getString("IdCard");
//        String contact = rs.getString("contact");
//        Tenant tenant = new Tenant(name, idCard, contact);
//
//        String roomNumber = rs.getString("room_number");
//        if (roomNumber != null) {
//            int electricCounter = rs.getInt("current_electric_counter");
//            int waterCounter = rs.getInt("current_water_counter");
//
//            Room room = new Room(roomNumber, electricCounter, waterCounter);
//
//            try {
//                tenant.assignRoom(room);
//            } catch (RoomException | TenantException e) {
//                System.out.println("Error assigning room to tenant: " + e.getMessage());
//            }
//        }
//
//        return tenant;
//    }
//
//    private int getRoomIdForTenant(String tenantId, Connection conn) throws SQLException {
//        String query = "SELECT r.room_id FROM Rooms r " +
//                "JOIN Tenants t ON r.room_id = t.assigned_room_id " +
//                "JOIN Users u ON t.user_id = u.user_id " +
//                "WHERE u.IdCard = ?";
//
//        try (PreparedStatement ps = conn.prepareStatement(query)) {
//            ps.setString(1, tenantId);
//            try (ResultSet rs = ps.executeQuery()) {
//                return rs.next() ? rs.getInt("room_id") : -1;
//            }
//        }
//    }


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
        String query = "INSERT INTO Tenants (user_id, assigned_room_id) VALUES (?, ? )";
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, userId);

            Room assignedRoom = tenant.getAssignedRoom();
            if (assignedRoom != null) {
                ps.setInt(2, getRoomIdFromDatabase(assignedRoom, conn));


            } else {
                ps.setNull(2, java.sql.Types.INTEGER);


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
        Connection conn = null;
        try {
            conn = DataBaseConnection.getConnection();
            conn.setAutoCommit(false); // Start transaction

            // Step 1: Get the tenant's assigned room ID (if any)
            String getRoomIdQuery = "SELECT t.assigned_room_id " +
                    "FROM Tenants t " +
                    "JOIN Users u ON t.user_id = u.user_id " +
                    "WHERE u.IdCard = ? AND u.role = 'Tenant'";

            Integer roomId = null;
            try (PreparedStatement psGetRoom = conn.prepareStatement(getRoomIdQuery)) {
                psGetRoom.setString(1, idCard);
                try (ResultSet rs = psGetRoom.executeQuery()) {
                    if (rs.next()) {
                        roomId = rs.getInt("assigned_room_id");
                        if (rs.wasNull()) {
                            roomId = null; // No room assigned
                        }
                    }
                }
            }

            // Step 2: If there’s an assigned room, update its occupancy status and clear tenant assignment
            if (roomId != null) {
                // Update the Tenants table to set assigned_room_id to NULL
                String updateTenantQuery = "UPDATE Tenants t " +
                        "JOIN Users u ON t.user_id = u.user_id " +
                        "SET t.assigned_room_id = NULL " +
                        "WHERE u.IdCard = ? AND u.role = 'Tenant'";
                try (PreparedStatement psUpdateTenant = conn.prepareStatement(updateTenantQuery)) {
                    psUpdateTenant.setString(1, idCard);
                    psUpdateTenant.executeUpdate();
                }

                // Update the Rooms table to set is_occupied to FALSE
                String updateRoomQuery = "UPDATE Rooms SET is_occupied = FALSE WHERE room_id = ?";
                try (PreparedStatement psUpdateRoom = conn.prepareStatement(updateRoomQuery)) {
                    psUpdateRoom.setInt(1, roomId);
                    psUpdateRoom.executeUpdate();
                }
            }

            // Step 3: Delete the tenant from the Users table (this will cascade to Tenants due to foreign key)
            String deleteQuery = "DELETE FROM Users WHERE IdCard = ? AND role = 'Tenant'";
            try (PreparedStatement psDelete = conn.prepareStatement(deleteQuery)) {
                psDelete.setString(1, idCard);
                int rowsAffected = psDelete.executeUpdate();
                if (rowsAffected > 0) {
                    System.out.println("Tenant deleted successfully: " + idCard);
                    conn.commit(); // Commit the transaction
                    return true;
                } else {
                    System.out.println("No tenant found with ID Card: " + idCard);
                    conn.rollback(); // Rollback if no tenant was deleted
                    return false;
                }
            }
        } catch (SQLException e) {
            try {
                if (conn != null) {
                    conn.rollback(); // Rollback on error
                }
            } catch (SQLException ex) {
                System.out.println("Rollback failed: " + ex.getMessage());
            }
            System.out.println("SQL Error deleting tenant: " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            try {
                if (conn != null) {
                    conn.setAutoCommit(true); // Reset auto-commit
                    conn.close();
                }
            } catch (SQLException e) {
                System.out.println("Error closing connection: " + e.getMessage());
            }
        }
    }
    public int getTenantIdByIdCard(String idCard) {
        String query = "SELECT t.tenant_id FROM Tenants t " +
                "JOIN Users u ON t.user_id = u.user_id " +
                "WHERE u.IdCard = ?";

        try (Connection conn = DataBaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setString(1, idCard);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("tenant_id");
                }
            }
        } catch (SQLException e) {
            System.out.println("SQL Error: " + e.getMessage());
            e.printStackTrace();
        }

        return -1; // Return -1 if tenant not found
    }
}
