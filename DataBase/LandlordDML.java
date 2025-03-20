package DataBase;

import Exceptions.RoomException;
import Exceptions.TenantException;
import Properties.Building;
import Properties.Room;
import Users.Landlord;
import Users.Tenant;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class LandlordDML {
    // ====================================================================================================
    // Landlord Retrieval Methods
    // ====================================================================================================
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

                    List<Tenant> tenants = loadTenantsForLandlord(conn);
                    List<Building> buildings = loadBuildingsForLandlord(conn);

                    return new Landlord(name, idCard, contact, tenants, buildings);
                }
            }
        } catch (SQLException e) {
            System.out.println("SQL Error: " + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }

    // ====================================================================================================
    // Authentication and Security Methods
    // ====================================================================================================
    public boolean verifyLandlordCredentials(String idCard, String pin) {
        String query = "SELECT l.landlord_pin FROM Users u " +
                "JOIN Landlords l ON u.user_id = l.user_id " +
                "WHERE u.IdCard = ? AND u.role = 'Landlord'";

        try (Connection conn = DataBaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setString(1, idCard);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String storedPin = rs.getString("landlord_pin");
                    return storedPin.equals(hashPin(pin));
                }
            }
        } catch (SQLException e) {
            System.out.println("SQL Error: " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }

    private String hashPin(String pin) {
        try {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(pin.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            System.out.println("Error hashing PIN: " + e.getMessage());
            return "";
        }
    }


    // ====================================================================================================
    // Tenant and Room Assignment Methods
    // ====================================================================================================
    public boolean assignRoomToTenant(String tenantIdCard, String buildingName, String floorNumber, String roomNumber) {
        RoomDML roomDML = new RoomDML();
        int roomId = roomDML.getRoomIdByBuildingFloorAndNumber(buildingName, floorNumber, roomNumber);

        if (roomId == -1) {
            System.out.println("Room not found: Building '" + buildingName + "', Floor " + floorNumber + ", Room " + roomNumber);
            return false;
        }

        try (Connection conn = DataBaseConnection.getConnection()) {
            conn.setAutoCommit(false);

            String checkRoomQuery = "SELECT is_occupied FROM Rooms WHERE room_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(checkRoomQuery)) {
                ps.setInt(1, roomId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next() && rs.getBoolean("is_occupied")) {
                        System.out.println("Error: Room " + roomNumber + " on floor " + floorNumber + " in building '" + buildingName + "' is already occupied.");
                        return false;
                    }
                }
            }

            int userId = getUserIdByIdCard(conn, tenantIdCard);
            if (userId == -1) {
                System.out.println("Tenant with ID card " + tenantIdCard + " not found");
                return false;
            }

            String updateTenantQuery = "UPDATE Tenants SET assigned_room_id = ? WHERE user_id = ?";
            String updateRoomQuery = "UPDATE Rooms SET is_occupied = TRUE WHERE room_id = ?";

            try (
                    PreparedStatement tenantStmt = conn.prepareStatement(updateTenantQuery);
                    PreparedStatement roomStmt = conn.prepareStatement(updateRoomQuery)
            ) {
                tenantStmt.setInt(1, roomId);
                tenantStmt.setInt(2, userId);
                int tenantRowsUpdated = tenantStmt.executeUpdate();

                if (tenantRowsUpdated <= 0) {
                    System.out.println("Error: Tenant record for ID card " + tenantIdCard + " not found.");
                    conn.rollback();
                    return false;
                }

                roomStmt.setInt(1, roomId);
                roomStmt.executeUpdate();

                conn.commit();
                System.out.println("Tenant assigned to Building '" + buildingName + "', Floor " + floorNumber + ", Room " + roomNumber + " successfully.");
                return true;

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
        return false;
    }

    // ====================================================================================================
    // Helper Methods
    // ====================================================================================================
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
        return -1;
    }
}