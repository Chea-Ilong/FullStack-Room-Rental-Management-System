package DataBase;

import Properties.Building;
import Users.Landlord;
import Users.Tenant;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class LandlordDML {

    public Landlord getLandlordByIdCard(String idCard) {
        String query = "SELECT u.user_id, u.name, u.IdCard, u.contact, l.landlord_pin " +
                "FROM Users u " +
                "JOIN Landlords l ON u.user_id = l.user_id " +
                "WHERE u.IdCard = ? AND u.role = 'Landlord'";

        try (Connection conn = DataBaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setString(1, idCard);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String name = rs.getString("name");
                    String contact = rs.getString("contact");

                    // Load tenants for this landlord
                    List<Tenant> tenants = loadTenantsForLandlord(conn);

                    // Load buildings for this landlord
                    BuildingDML buildingDML = new BuildingDML();
                    List<Building> buildings = loadBuildingsForLandlord(conn);

                    // Create and return the landlord
                    return new Landlord(name, idCard, contact, tenants, buildings);
                }
            }
        } catch (SQLException e) {
            System.out.println("SQL Error: " + e.getMessage());
            e.printStackTrace();
        }

        return null; // Landlord not found
    }

    private List<Users.Tenant> loadTenantsForLandlord(Connection conn) throws SQLException {
        List<Users.Tenant> tenants = new ArrayList<>();
        String query = "SELECT u.name, u.IdCard, u.contact " +
                "FROM Users u " +
                "JOIN Tenants t ON u.user_id = t.user_id " +
                "WHERE u.role = 'Tenant'";

        try (PreparedStatement ps = conn.prepareStatement(query)) {
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String name = rs.getString("name");
                    String idCard = rs.getString("IdCard");
                    String contact = rs.getString("contact");

                    tenants.add(new Users.Tenant(name, idCard, contact));
                }
            }
        }

        return tenants;
    }

    private List<Building> loadBuildingsForLandlord(Connection conn) throws SQLException {
        List<Building> buildings = new ArrayList<>();
        BuildingDML buildingDML = new BuildingDML();
        String query = "SELECT building_id FROM Buildings";

        try (PreparedStatement ps = conn.prepareStatement(query)) {
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int buildingId = rs.getInt("building_id");
                    Building building = buildingDML.getBuildingById(buildingId);
                    if (building != null) {
                        buildings.add(building);
                    }
                }
            }
        }

        return buildings;
    }
    public void assignRoomToTenant(String tenantIdCard, String buildingName, String floorNumber, String roomNumber) {
        RoomDML roomDML = new RoomDML();
        int roomId = roomDML.getRoomIdByBuildingFloorAndNumber(buildingName, floorNumber, roomNumber);

        if (roomId == -1) {
            System.out.println("Room not found: Building '" + buildingName + "', Floor " + floorNumber + ", Room " + roomNumber);
            return;
        }

        try (Connection conn = DataBaseConnection.getConnection()) {
            conn.setAutoCommit(false); // Start transaction

            // Check if room is already occupied
            String checkRoomQuery = "SELECT is_occupied FROM Rooms WHERE room_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(checkRoomQuery)) {
                ps.setInt(1, roomId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next() && rs.getBoolean("is_occupied")) {
                        System.out.println("Error: Room " + roomNumber + " on floor " + floorNumber + " in building '" + buildingName + "' is already occupied.");
                        return;
                    }
                }
            }

            // Get user_id from ID card in a single query
            int userId = getUserIdByIdCard(conn, tenantIdCard);
            if (userId == -1) {
                System.out.println("Tenant with ID card " + tenantIdCard + " not found");
                return;
            }

            // Update tenant assignment AND mark room as occupied in a single transaction
            String updateTenantQuery = "UPDATE Tenants SET assigned_room_id = ? WHERE user_id = ?";
            String updateRoomQuery = "UPDATE Rooms SET is_occupied = TRUE WHERE room_id = ?";

            try (
                    PreparedStatement tenantStmt = conn.prepareStatement(updateTenantQuery);
                    PreparedStatement roomStmt = conn.prepareStatement(updateRoomQuery)
            ) {
                // Set up and execute tenant update
                tenantStmt.setInt(1, roomId);
                tenantStmt.setInt(2, userId);
                int tenantRowsUpdated = tenantStmt.executeUpdate();

                if (tenantRowsUpdated <= 0) {
                    System.out.println("Error: Tenant record for ID card " + tenantIdCard + " not found.");
                    conn.rollback();
                    return;
                }

                // Set up and execute room update
                roomStmt.setInt(1, roomId);
                roomStmt.executeUpdate();

                // Commit transaction
                conn.commit();
                System.out.println("Tenant assigned to Building '" + buildingName + "', Floor " + floorNumber + ", Room " + roomNumber + " successfully.");
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            System.out.println("SQL Error: " + e.getMessage());
            e.printStackTrace();
        }
    }


    // Helper method to get user_id by ID card
    private int getUserIdByIdCard(Connection conn, String idCard) throws SQLException {
        String query = "SELECT user_id FROM Users WHERE IdCard = ?";
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, idCard);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("user_id");
                }
            }
        }
        return -1; // User not found
    }

    // Helper method to get tenant_id by user_id
    private int getTenantIdByUserId(Connection conn, int userId) throws SQLException {
        String query = "SELECT tenant_id FROM Tenants WHERE user_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("tenant_id");
                }
            }
        }
        return -1; // Tenant not found
    }

    // Helper method to get room_id by room number
    private int getRoomIdByRoomNumber(Connection conn, String roomNumber) throws SQLException {
        String query = "SELECT room_id FROM Rooms WHERE room_number = ?";
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, roomNumber);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("room_id");
                }
            }
        }
        return -1; // Room not found
    }

    // Helper method to check if a room is occupied
    private boolean isRoomOccupied(Connection conn, int roomId) throws SQLException {
        String query = "SELECT is_occupied FROM Rooms WHERE room_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, roomId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getBoolean("is_occupied");
                }
            }
        }
        return false; // Default to not occupied if room not found
    }
}