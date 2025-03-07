package DataBase;

import Properties.Room;
import Users.Tenant;
import Payment.UtilityUsage;
import Exceptions.RoomException;
import java.sql.*;
import java.time.LocalDate;

public class RoomDML {

    // Save a new room to the database
    public void saveRoom(Room room, int floorId) {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet generatedKeys = null;

        try {
            conn = DataBaseConnection.getConnection();

            // Insert into Rooms table
            String query = "INSERT INTO Rooms (floor_id, room_number, rent, current_electric_counter, current_water_counter, is_occupied) VALUES (?, ?, ?, ?, ?, ?)";
            stmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);

            stmt.setInt(1, floorId);
            stmt.setString(2, room.getRoomNumber());
            stmt.setDouble(3, room.getRent());
            stmt.setInt(4, room.getCurrentElectricCounter());  // Use Room's getter method
            stmt.setInt(5, room.getCurrentWaterCounter());     // Use Room's getter method
            stmt.setBoolean(6, room.isOccupied());


            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected <= 0) {
                throw new SQLException("Failed to create room record");
            }

            // Get the generated room_id
            generatedKeys = stmt.getGeneratedKeys();
            if (generatedKeys.next()) {
                int roomId = generatedKeys.getInt(1);
                System.out.println("Room saved successfully! Room ID: " + roomId);

                // If the room has a tenant, update the tenant's assigned room
                if (room.isOccupied() && room.getTenant() != null) {
                    updateTenantAssignedRoom(room.getTenant(), roomId, conn);
                }

                // If there's utility usage, save it
                if (room.getUtilityUsage() != null) {
                    saveUtilityUsage(room.getUtilityUsage(), roomId, conn);
                }
            } else {
                throw new SQLException("Failed to get room_id");
            }

        } catch (SQLException e) {
            System.out.println("SQL Error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // Close resources
            try {
                if (generatedKeys != null) generatedKeys.close();
                if (stmt != null) stmt.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                System.out.println("Error closing resources: " + e.getMessage());
            }
        }
    }
    // Add this to your RoomDML class
    public void addRoomToFloor(String buildingName, String floorNumber, String roomNumber, double rent) {
        try {
            // Get building ID
            BuildingDML buildingDML = new BuildingDML();
            int buildingId = buildingDML.getBuildingIdByName(buildingName);

            if (buildingId == -1) {
                System.out.println("Building not found: " + buildingName);
                return;
            }

            // Get floor ID
            FloorDML floorDML = new FloorDML();
            int floorId = floorDML.getFloorIdByBuildingAndNumber(buildingId, floorNumber);

            if (floorId == -1) {
                System.out.println("Floor not found: " + floorNumber + " in building: " + buildingName);
                return;
            }

            // Create and save room
            // In your addRoomToFloor method:
            Room newRoom = new Room(roomNumber, 0, 0);
            newRoom.setRent(rent); // Make sure this method exists
            LocalDate today = LocalDate.now();
            newRoom.setUtilityUsage(0, 0, today); // Initialize with zero usage


            this.saveRoom(newRoom, floorId);
            System.out.println("Room " + roomNumber + " added successfully to floor " + floorNumber);

        } catch (Exception e) {
            System.out.println("Error adding room: " + e.getMessage());
            e.printStackTrace();
        }
    }
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

        return -1; // Room not found
    }
    // Helper method to update tenant's assigned room
    private void updateTenantAssignedRoom(Tenant tenant, int roomId, Connection conn) throws SQLException {
        // First, find the tenant's ID
        String findTenantQuery = "SELECT tenant_id FROM Tenants JOIN Users ON Tenants.user_id = Users.user_id WHERE Users.IdCard = ?";

        try (PreparedStatement ps = conn.prepareStatement(findTenantQuery)) {
            ps.setString(1, tenant.getIdCard());

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int tenantId = rs.getInt("tenant_id");

                    // Now update the tenant's assigned room
                    String updateQuery = "UPDATE Tenants SET assigned_room_id = ? WHERE tenant_id = ?";

                    try (PreparedStatement updatePs = conn.prepareStatement(updateQuery)) {
                        updatePs.setInt(1, roomId);
                        updatePs.setInt(2, tenantId);

                        updatePs.executeUpdate();
                    }
                }
            }
        }
    }

    // Helper method to save utility usage
    private void saveUtilityUsage(UtilityUsage usage, int roomId, Connection conn) throws SQLException {
        String query = "INSERT INTO utilityusage (room_id, electric_usage, water_usage, usage_date) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, roomId);
            ps.setInt(2, usage.getElectricUsage());
            ps.setInt(3, usage.getWaterUsage());
            ps.setDate(4, java.sql.Date.valueOf(usage.getDate()));
            int rowsAffected = ps.executeUpdate();
            System.out.println("Rows affected by utility usage insert: " + rowsAffected);
        } catch (SQLException e) {
            System.out.println("Failed to insert utility usage: " + e.getMessage());
            throw e;
        }
    }

//     Get room by ID
    public Room getRoomById(int roomId) {
        String query = "SELECT room_number, rent, current_electric_counter, current_water_counter, is_occupied FROM Rooms WHERE room_id = ?";

        try (Connection conn = DataBaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setInt(1, roomId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String roomNumber = rs.getString("room_number");
                    int currentElectricCounter = rs.getInt("current_electric_counter");
                    int currentWaterCounter = rs.getInt("current_water_counter");

                    // Create room
                    Room room = new Room(roomNumber, currentElectricCounter, currentWaterCounter);

                    // Set occupancy state
                    boolean isOccupied = rs.getBoolean("is_occupied");
                    if (isOccupied) {
                        try {
                            room.markAsOccupied();
                            // Load tenant information
                            loadTenantForRoom(room, roomId, conn);
                        } catch (RoomException e) {
                            System.out.println("Error marking room as occupied: " + e.getMessage());
                        }
                    }

                    // Load utility usage information
                    loadUtilityUsageForRoom(room, roomId, conn);

                    return room;
                }
            }
        } catch (SQLException e) {
            System.out.println("SQL Error: " + e.getMessage());
            e.printStackTrace();
        }

        return null; // Room not found
    }
    public int getRoomIdByRoomNumber(String roomNumber) {
        String query = "SELECT room_id FROM Rooms WHERE room_number = ?";

        try (Connection conn = DataBaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setString(1, roomNumber);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("room_id");
                }
            }
        } catch (SQLException e) {
            System.out.println("SQL Error: " + e.getMessage());
            e.printStackTrace();
        }

        return -1; // Return -1 or throw an exception if room not found
    }

    // Helper method to load tenant for a room
    private void loadTenantForRoom(Room room, int roomId, Connection conn) throws SQLException {
        String query = "SELECT u.name, u.IdCard, u.contact " +
                "FROM Tenants t " +
                "JOIN Users u ON t.user_id = u.user_id " +
                "WHERE t.assigned_room_id = ?";

        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, roomId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String name = rs.getString("name");
                    String idCard = rs.getString("IdCard");
                    String contact = rs.getString("contact");

                    Tenant tenant = new Tenant(name, idCard, contact);
                    room.assignTenant(tenant);
                }
            }
        }
    }

    // Helper method to load utility usage for a room
    private void loadUtilityUsageForRoom(Room room, int roomId, Connection conn) throws SQLException {
        String query = "SELECT electric_usage, water_usage, usage_date FROM UtilityUsage " +
                "WHERE room_id = ? ORDER BY usage_date DESC LIMIT 1";

        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, roomId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int electricUsage = rs.getInt("electric_usage");
                    int waterUsage = rs.getInt("water_usage");
                    LocalDate date = rs.getDate("usage_date").toLocalDate();

                    room.setUtilityUsage(electricUsage, waterUsage, date);
                }
            }
        }
    }

    // Update room information
//    public void updateRoom(int roomId, Room room) {
//        String query = "UPDATE Rooms SET room_number = ?, rent = ?, current_electric_counter = ?, " +
//                "current_water_counter = ?, is_occupied = ? WHERE room_id = ?";
//
//        try (Connection conn = DataBaseConnection.getConnection();
//             PreparedStatement ps = conn.prepareStatement(query)) {
//
//            ps.setString(1, room.getRoomNumber());
//            ps.setDouble(2, room.getRent());
//            ps.setInt(3, room.getUtilityUsage() != null ? room.getUtilityUsage().getElectricUsage() : 0);
//            ps.setInt(4, room.getUtilityUsage() != null ? room.getUtilityUsage().getWaterUsage() : 0);
//            ps.setBoolean(5, room.isOccupied());
//            ps.setInt(6, roomId);
//
//            int rowsAffected = ps.executeUpdate();
//            if (rowsAffected > 0) {
//                System.out.println("Room updated successfully!");
//            } else {
//                System.out.println("Room not found or not updated.");
//            }
//
//        } catch (SQLException e) {
//            System.out.println("SQL Error: " + e.getMessage());
//            e.printStackTrace();
//        }
//    }

    // Update room usage
//    public void updateRoomUsage(int roomId, int newElectricCounter, int newWaterCounter, LocalDate date) {
//        Connection conn = null;
//        try {
//            conn = DataBaseConnection.getConnection();
//            conn.setAutoCommit(false); // Start transaction
//
//            // First, update the Rooms table with new counter values
//            String updateRoomQuery = "UPDATE Rooms SET current_electric_counter = ?, current_water_counter = ? WHERE room_id = ?";
//
//            try (PreparedStatement ps = conn.prepareStatement(updateRoomQuery)) {
//                ps.setInt(1, newElectricCounter);
//                ps.setInt(2, newWaterCounter);
//                ps.setInt(3, roomId);
//
//                ps.executeUpdate();
//            }
//
//            // Get current counter values to calculate usage
//            String getCurrentCountersQuery = "SELECT current_electric_counter, current_water_counter FROM Rooms WHERE room_id = ?";
//            int currentElectricCounter = 0;
//            int currentWaterCounter = 0;
//
//            try (PreparedStatement ps = conn.prepareStatement(getCurrentCountersQuery)) {
//                ps.setInt(1, roomId);
//
//                try (ResultSet rs = ps.executeQuery()) {
//                    if (rs.next()) {
//                        currentElectricCounter = rs.getInt("current_electric_counter");
//                        currentWaterCounter = rs.getInt("current_water_counter");
//                    }
//                }
//            }
//
//            // Calculate usage
//            int electricUsage = newElectricCounter - currentElectricCounter;
//            int waterUsage = newWaterCounter - currentWaterCounter;
//
//            // Then, insert a new utility usage record
//            String insertUsageQuery = "INSERT INTO UtilityUsage (room_id, electric_usage, water_usage, usage_date) VALUES (?, ?, ?, ?)";
//
//            try (PreparedStatement ps = conn.prepareStatement(insertUsageQuery)) {
//                ps.setInt(1, roomId);
//                ps.setInt(2, electricUsage);
//                ps.setInt(3, waterUsage);
//                ps.setDate(4, java.sql.Date.valueOf(date));
//
//                ps.executeUpdate();
//            }
//
//            // Commit the transaction
//            conn.commit();
//            System.out.println("Room usage updated successfully!");
//
//        } catch (SQLException e) {
//            try {
//                if (conn != null) {
//                    conn.rollback(); // Rollback transaction on error
//                }
//            } catch (SQLException ex) {
//                System.out.println("Failed to rollback: " + ex.getMessage());
//            }
//
//            System.out.println("SQL Error: " + e.getMessage());
//            e.printStackTrace();
//        } finally {
//            try {
//                if (conn != null) {
//                    conn.setAutoCommit(true); // Reset auto-commit
//                    conn.close();
//                }
//            } catch (SQLException e) {
//                System.out.println("Error closing connection: " + e.getMessage());
//            }
//        }
//    }

//    // Delete room
//    public void deleteRoom(int roomId) {
//        // Note: This will only work if there are no tenants or utility usage records
//        // associated with this room due to foreign key constraints
//        String query = "DELETE FROM Rooms WHERE room_id = ?";
//
//        try (Connection conn = DataBaseConnection.getConnection();
//             PreparedStatement ps = conn.prepareStatement(query)) {
//
//            ps.setInt(1, roomId);
//
//            int rowsAffected = ps.executeUpdate();
//            if (rowsAffected > 0) {
//                System.out.println("Room deleted successfully!");
//            } else {
//                System.out.println("Room not found or not deleted.");
//            }
//
//        } catch (SQLException e) {
//            System.out.println("SQL Error: " + e.getMessage());
//            e.printStackTrace();
//        }
//    }
}