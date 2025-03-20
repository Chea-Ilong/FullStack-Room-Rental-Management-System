package DataBase;

import Exceptions.TenantException;
import Properties.Floor;
import Properties.Room;
import Users.Tenant;
import Exceptions.RoomException;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class RoomDML {
    // ====================================================================================================
    // Room CRUD Operations
    // ====================================================================================================
    public void saveRoom(Room room, int floorId) {
        Connection conn = null;
        PreparedStatement roomStmt = null;
        ResultSet generatedKeys = null;

        try {
            conn = DataBaseConnection.getConnection();
            conn.setAutoCommit(false);

            String roomQuery = "INSERT INTO Rooms (floor_id, room_number, current_electric_counter, current_water_counter, is_occupied) VALUES (?, ?, ?, ?, ?)";
            roomStmt = conn.prepareStatement(roomQuery, Statement.RETURN_GENERATED_KEYS);

            roomStmt.setInt(1, floorId);
            roomStmt.setString(2, room.getRoomNumber());
            roomStmt.setInt(3, room.getCurrentElectricCounter());
            roomStmt.setInt(4, room.getCurrentWaterCounter());
            roomStmt.setBoolean(5, room.isOccupied());

            int roomRowsAffected = roomStmt.executeUpdate();
            if (roomRowsAffected <= 0) {
                throw new SQLException("Failed to create room record");
            }

            generatedKeys = roomStmt.getGeneratedKeys();
            if (!generatedKeys.next()) {
                throw new SQLException("Failed to get room_id");
            }

            int roomId = generatedKeys.getInt(1);

            conn.commit();
            System.out.println("Room saved successfully! Room ID: " + roomId);

        } catch (SQLException e) {
            try {
                if (conn != null) {
                    conn.rollback();
                }
            } catch (SQLException ex) {
                System.out.println("Failed to rollback: " + ex.getMessage());
            }

            System.out.println("SQL Error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                if (generatedKeys != null) generatedKeys.close();
                if (roomStmt != null) roomStmt.close();
                if (conn != null) {
                    conn.setAutoCommit(true);
                    conn.close();
                }
            } catch (SQLException e) {
                System.out.println("Error closing resources: " + e.getMessage());
            }
        }
    }

    public void updateRoom(int roomId, Room room) {
        String query = "UPDATE Rooms SET room_number = ? WHERE room_id = ?";

        try (Connection conn = DataBaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setString(1, room.getRoomNumber());
            ps.setInt(2, roomId);

            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Room updated successfully in database.");
            } else {
                System.out.println("No room was updated. Room ID may not exist.");
            }
        } catch (SQLException e) {
            System.out.println("SQL Error updating room: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void deleteRoom(int roomId) {
        String query = "DELETE FROM Rooms WHERE room_id = ?";

        try (Connection conn = DataBaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setInt(1, roomId);
            int rowsAffected = ps.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println("Room successfully deleted from database.");
            } else {
                System.out.println("No room was deleted. Room ID may not exist.");
            }
        } catch (SQLException e) {
            System.out.println("SQL Error deleting room: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ====================================================================================================
    // Room Retrieval Methods
    // ====================================================================================================
    public Room getRoomById(int roomId) {
        String query = "SELECT r.room_number, r.current_electric_counter, r.current_water_counter, r.is_occupied, " +
                "u.name, u.IdCard, u.contact " +
                "FROM Rooms r " +
                "LEFT JOIN Tenants t ON t.assigned_room_id = r.room_id " +
                "LEFT JOIN Users u ON t.user_id = u.user_id " +
                "WHERE r.room_id = ?";

        try (Connection conn = DataBaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setInt(1, roomId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String roomNumber = rs.getString("room_number");
                    int currentElectricCounter = rs.getInt("current_electric_counter");
                    int currentWaterCounter = rs.getInt("current_water_counter");

                    Room room = new Room(roomNumber, currentElectricCounter, currentWaterCounter);
                    boolean isOccupied = rs.getBoolean("is_occupied");

                    if (isOccupied) {
                        String tenantName = rs.getString("name");
                        String tenantIdCard = rs.getString("IdCard");
                        String tenantContact = rs.getString("contact");

                        if (tenantIdCard != null) {
                            Tenant tenant = new Tenant(tenantName, tenantIdCard, tenantContact != null ? tenantContact : "");
                            room.assignTenant(tenant);
                        }
                    }
                    return room;
                }
            }
        } catch (SQLException e) {
            System.out.println("SQL Error: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    public List<RoomDetails> getAllRoomsWithDetails() {
        List<RoomDetails> roomDetailsList = new ArrayList<>();
        String query = "SELECT r.room_id, r.room_number, r.current_electric_counter, r.current_water_counter, r.is_occupied, " +
                "b.building_name, f.floor_number, u.name AS tenant_name " +
                "FROM Rooms r " +
                "LEFT JOIN Floors f ON r.floor_id = f.floor_id " +
                "LEFT JOIN Buildings b ON f.building_id = b.building_id " +
                "LEFT JOIN Tenants t ON t.assigned_room_id = r.room_id " +
                "LEFT JOIN Users u ON t.user_id = u.user_id " +
                "ORDER BY b.building_name, f.floor_number, r.room_number";

        try (Connection conn = DataBaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                RoomDetails details = new RoomDetails();
                details.roomId = rs.getInt("room_id");
                details.roomNumber = rs.getString("room_number");
                details.electricCounter = rs.getInt("current_electric_counter");
                details.waterCounter = rs.getInt("current_water_counter");
                details.isOccupied = rs.getBoolean("is_occupied");
                details.buildingName = rs.getString("building_name");
                details.floorNumber = rs.getString("floor_number");
                details.tenantName = rs.getString("tenant_name") != null ? rs.getString("tenant_name") : "N/A";
                roomDetailsList.add(details);
            }
        } catch (SQLException e) {
            System.err.println("SQL Error retrieving rooms with details: " + e.getMessage());
            e.printStackTrace();
        }

        return roomDetailsList;
    }

    // ====================================================================================================
    // Room Management Methods
    // ====================================================================================================
    public void updateRoomCounters(int roomId, int electricCounter, int waterCounter) {
        String query = "UPDATE Rooms SET current_electric_counter = ?, current_water_counter = ? WHERE room_id = ?";

        try (Connection conn = DataBaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setInt(1, electricCounter);
            ps.setInt(2, waterCounter);
            ps.setInt(3, roomId);

            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Room counters updated successfully in database.");
            } else {
                System.out.println("No room was updated. Room ID may not exist.");
            }
        } catch (SQLException e) {
            System.out.println("SQL Error updating room counters: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void syncRoomWithTenant(Room room) {
        String query = "SELECT u.name, u.IdCard, u.contact " +
                "FROM Rooms r " +
                "JOIN Tenants t ON r.room_id = t.assigned_room_id " +
                "JOIN Users u ON t.user_id = u.user_id " +
                "WHERE r.room_number = ?";

        try (Connection conn = DataBaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setString(1, room.getRoomNumber());

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String name = rs.getString("name");
                    String idCard = rs.getString("IdCard");
                    String contact = rs.getString("contact");

                    Tenant tenant = new Tenant(name, idCard, contact);
                    try {
                        room.setTenantDirectly(tenant);
                    } catch (Exception e) {
                        System.out.println("Failed to set tenant: " + e.getMessage());
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println("SQL Error syncing room with tenant: " + e.getMessage());
        }
    }

    // ====================================================================================================
    // Helper Methods
    // ====================================================================================================

    public int getRoomIdByFloorAndNumber(int floorId, String roomNumber) {
        String query = "SELECT room_id FROM Rooms WHERE floor_id = ? AND room_number = ?";

        try (Connection conn = DataBaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setInt(1, floorId);
            ps.setString(2, roomNumber);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("room_id");
                }
            }
        } catch (SQLException e) {
            System.out.println("SQL Error: " + e.getMessage());
            e.printStackTrace();
        }

        return -1;
    }

    public int getRoomIdByBuildingFloorAndNumber(String buildingName, String floorNumber, String roomNumber) {
        String query = "SELECT r.room_id FROM Rooms r " +
                "JOIN Floors f ON r.floor_id = f.floor_id " +
                "JOIN Buildings b ON f.building_id = b.building_id " +
                "WHERE b.building_name = ? AND f.floor_number = ? AND r.room_number = ?";

        try (Connection conn = DataBaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setString(1, buildingName);
            ps.setString(2, floorNumber);
            ps.setString(3, roomNumber);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("room_id");
                }
            }
        } catch (SQLException e) {
            System.out.println("SQL Error: " + e.getMessage());
            e.printStackTrace();
        }

        return -1;
    }

    // ====================================================================================================
    // Inner Classes
    // ====================================================================================================
    public static class RoomDetails {
        public int roomId;
        public String roomNumber;
        public int electricCounter;
        public int waterCounter;
        public boolean isOccupied;
        public String buildingName;
        public String floorNumber;
        public String tenantName;
    }
}