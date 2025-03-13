package DataBase;

import Exceptions.TenantException;
import Properties.Floor;
import Properties.Room;
import Users.Tenant;
import Payment.UtilityUsage;
import Exceptions.RoomException;
import java.sql.*;
import java.time.LocalDate;

public class RoomDML {


    public void saveRoom(Room room, int floorId) {
        Connection conn = null;
        PreparedStatement roomStmt = null;
        ResultSet generatedKeys = null;

        try {
            conn = DataBaseConnection.getConnection();
            conn.setAutoCommit(false); // Start transaction

            // Save room to Rooms table
            String roomQuery = "INSERT INTO Rooms (floor_id, room_number, rent, current_electric_counter, current_water_counter, is_occupied) VALUES (?, ?, ?, ?, ?, ?)";
            roomStmt = conn.prepareStatement(roomQuery, Statement.RETURN_GENERATED_KEYS);

            roomStmt.setInt(1, floorId);
            roomStmt.setString(2, room.getRoomNumber());
            roomStmt.setDouble(3, room.getRent());
            roomStmt.setInt(4, room.getElectricCounter());
            roomStmt.setInt(5, room.getWaterCounter());
            roomStmt.setBoolean(6, room.isOccupied());

            int roomRowsAffected = roomStmt.executeUpdate();
            if (roomRowsAffected <= 0) {
                throw new SQLException("Failed to create room record");
            }

            // Get the generated room_id
            generatedKeys = roomStmt.getGeneratedKeys();
            if (!generatedKeys.next()) {
                throw new SQLException("Failed to get room_id");
            }

            int roomId = generatedKeys.getInt(1);

            // Commit the transaction
            conn.commit();
            System.out.println("Room saved successfully! Room ID: " + roomId);

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
                if (roomStmt != null) roomStmt.close();
                if (conn != null) {
                    conn.setAutoCommit(true); // Reset auto-commit
                    conn.close();
                }
            } catch (SQLException e) {
                System.out.println("Error closing resources: " + e.getMessage());
            }
        }
    }

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
            Room newRoom = new Room(roomNumber, 0, 0);
            newRoom.setRent(rent);
            LocalDate today = LocalDate.now();
            newRoom.setUtilityUsage(0, 0, today); // Initialize with zero usage

            this.saveRoom(newRoom, floorId);
            System.out.println("Room " + roomNumber + " added successfully to floor " + floorNumber);

        } catch (Exception e) {
            System.out.println("Error adding room: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ===== Utility Usage Methods =====

    public void updateUtilityUsage(String roomNumber, int newElectricUsage, int newWaterUsage, LocalDate date) {
        String query = "UPDATE UtilityUsage SET electric_usage = ?, water_usage = ?, usage_date = ? WHERE room_id = (SELECT room_id FROM Rooms WHERE room_number = ?)";

        try (Connection conn = DataBaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setInt(1, newElectricUsage);
            ps.setInt(2, newWaterUsage);
            ps.setDate(3, java.sql.Date.valueOf(date));
            ps.setString(4, roomNumber);

            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Utility usage updated successfully for room: " + roomNumber);
            } else {
                System.out.println("Failed to update utility usage for room: " + roomNumber);
            }

        } catch (SQLException e) {
            System.out.println("SQL Error updating utility usage: " + e.getMessage());
            e.printStackTrace();
        }
    }
    public UtilityUsage getUtilityUsageFromDatabase(String roomNumber, LocalDate date) {
        String query = "SELECT u.electric_usage, u.water_usage, u.usage_date " +
                "FROM UtilityUsage u " +
                "JOIN Rooms r ON u.room_id = r.room_id " +
                "WHERE r.room_number = ? AND u.usage_date = ?";

        try (Connection conn = DataBaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setString(1, roomNumber);
            ps.setDate(2, java.sql.Date.valueOf(date));

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int electricUsage = rs.getInt("electric_usage");
                    int waterUsage = rs.getInt("water_usage");


                    UtilityUsage usage = new UtilityUsage(electricUsage, waterUsage, date);
//                    usage.setIsPaid(isPaid);
                    return usage;
                }
            }
        } catch (SQLException e) {
            System.out.println("SQL Error retrieving utility usage: " + e.getMessage());
            e.printStackTrace();
        }

        return null; // Not found
    }
    // Enhanced saveUtilityUsage method
    public void saveUtilityUsage(String roomNumber, int electricUsage, int waterUsage, LocalDate date) {
        System.out.println("Attempting to save utility usage for room: " + roomNumber);

        int roomId = getRoomIdByRoomNumber(roomNumber);
        if (roomId == -1) {
            System.out.println("Error: Room not found: " + roomNumber);
            System.out.println("Please check if the room exists and try again.");
            return;
        }

        try (Connection conn = DataBaseConnection.getConnection()) {
            conn.setAutoCommit(false);

            // Check if there's already a utility usage record for this date
            String checkQuery = "SELECT * FROM UtilityUsage WHERE room_id = ? AND usage_date = ?";
            try (PreparedStatement checkPs = conn.prepareStatement(checkQuery)) {
                checkPs.setInt(1, roomId);
                checkPs.setDate(2, Date.valueOf(date));

                boolean recordExists = false;
                try (ResultSet rs = checkPs.executeQuery()) {
                    recordExists = rs.next();
                }

                // Use an insert or update based on whether the record exists
                String query;
                if (recordExists) {
                    query = "UPDATE UtilityUsage SET electric_usage = ?, water_usage = ? " +
                            "WHERE room_id = ? AND usage_date = ?";
                    System.out.println("Updating existing utility record...");
                } else {
                    query = "INSERT INTO UtilityUsage (electric_usage, water_usage, room_id, usage_date) " +
                            "VALUES (?, ?, ?, ?)";
                    System.out.println("Creating new utility record...");
                }

                try (PreparedStatement ps = conn.prepareStatement(query)) {
                    ps.setInt(1, electricUsage);
                    ps.setInt(2, waterUsage);
                    ps.setInt(3, roomId);
                    ps.setDate(4, Date.valueOf(date));

                    int affected = ps.executeUpdate();

                    // Update room counters in the same transaction
                    updateRoomCounters(conn, roomId, electricUsage, waterUsage);

                    conn.commit();
                    System.out.println("Utility usage " + (recordExists ? "updated" : "saved") +
                            " successfully for room: " + roomNumber +
                            " (" + affected + " rows affected)");
                }
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            System.out.println("SQL Error saving utility usage: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Support method that accepts a connection for transaction support
    private void updateRoomCounters(Connection conn, int roomId, int electricCounter, int waterCounter) throws SQLException {
        String query = "UPDATE Rooms SET current_electric_counter = ?, current_water_counter = ? WHERE room_id = ?";

        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, electricCounter);
            ps.setInt(2, waterCounter);
            ps.setInt(3, roomId);
            ps.executeUpdate();
        }
    }

    // ===== Room Retrieval Methods =====
    // Add this method to the RoomDML class
    public Room getRoomByRoomNumber(String roomNumber) {
        String query = "SELECT r.room_id, r.room_number, r.rent, r.current_electric_counter, " +
                "r.current_water_counter, r.is_occupied FROM Rooms r WHERE r.room_number = ?";

        try (Connection conn = DataBaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setString(1, roomNumber);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int roomId = rs.getInt("room_id");
                    double rent = rs.getDouble("rent");
                    int electricCounter = rs.getInt("current_electric_counter");
                    int waterCounter = rs.getInt("current_water_counter");
                    boolean isOccupied = rs.getBoolean("is_occupied");

                    // Create room object
                    Room room = new Room(roomNumber, electricCounter, waterCounter);
                    room.setRent(rent);

                    // Set occupancy state if needed
                    if (isOccupied) {
                        try {
                            room.markAsOccupied();
                            // Load tenant data
                            loadTenantForRoom(room, roomId, conn);
                        } catch (RoomException e) {
                            System.out.println("Error marking room as occupied: " + e.getMessage());
                        }
                    }

                    return room;
                }
            }
        } catch (SQLException e) {
            System.out.println("SQL Error retrieving room: " + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }
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
        // Try to normalize the room number format (remove leading zeros)
        String normalizedRoomNumber = roomNumber.replaceFirst("^0+(?!$)", "");

        String query = "SELECT room_id, room_number FROM Rooms WHERE room_number = ? OR room_number = ?";

        try (Connection conn = DataBaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setString(1, roomNumber);
            ps.setString(2, normalizedRoomNumber);

            System.out.println("Searching for room with number: " + roomNumber +
                    " or normalized: " + normalizedRoomNumber);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int roomId = rs.getInt("room_id");
                    String foundRoomNumber = rs.getString("room_number");
                    System.out.println("Found room: " + foundRoomNumber + " with ID: " + roomId);
                    return roomId;
                } else {
                    System.out.println("No room found matching '" + roomNumber + "' or '" +
                            normalizedRoomNumber + "'");

                    // Print a list of all available rooms to assist debugging
                    displayAllRoomNumbers();
                }
            }
        } catch (SQLException e) {
            System.out.println("SQL Error finding room: " + e.getMessage());
            e.printStackTrace();
        }

        return -1; // Return -1 if room not found
    }
    private void displayAllRoomNumbers() {
        String query = "SELECT room_number FROM Rooms";

        try (Connection conn = DataBaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            System.out.println("\nAvailable room numbers in database:");
            boolean hasRooms = false;
            while (rs.next()) {
                hasRooms = true;
                System.out.println(" - " + rs.getString("room_number"));
            }

            if (!hasRooms) {
                System.out.println("No rooms found in the database.");
            }
        } catch (SQLException e) {
            System.out.println("Error listing room numbers: " + e.getMessage());
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

    // ===== Tenant Assignment Methods =====

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

    // ===== Helper Methods =====

    private void loadRoomsForFloor(Floor floor, int floorId, Connection conn) throws SQLException {
        String query = "SELECT room_id, room_number, rent, current_electric_counter, current_water_counter, is_occupied FROM Rooms WHERE floor_id = ?";

        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, floorId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int roomId = rs.getInt("room_id");
                    String roomNumber = rs.getString("room_number");
                    double rent = rs.getDouble("rent");
                    int electricCounter = rs.getInt("current_electric_counter");
                    int waterCounter = rs.getInt("current_water_counter");
                    boolean isOccupied = rs.getBoolean("is_occupied");

                    // Create the room directly from the query results
                    Room room = new Room(roomNumber, electricCounter, waterCounter);
                    room.setRent(rent);

                    // Load tenant information if room is occupied
                    if (isOccupied) {
                        loadTenantForRoom(room, roomId, conn);
                    }

                    // Load utility usage information
                    loadUtilityUsageForRoom(room, roomId, conn);

                    // Add room to floor
                    floor.addRoom(room);
                }
            }
        }
    }

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
    // ===== Room Update Methods =====

    public void updateRoom(int roomId, double newRent, boolean isOccupied) {
        String query = "UPDATE Rooms SET rent = ?, is_occupied = ? WHERE room_id = ?";

        try (Connection conn = DataBaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setDouble(1, newRent);
            ps.setBoolean(2, isOccupied);
            ps.setInt(3, roomId);

            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Room updated successfully!");
            } else {
                System.out.println("Room update failed. Room ID: " + roomId + " not found.");
            }

        } catch (SQLException e) {
            System.out.println("SQL Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void updateRoomCounters(int roomId, int electricCounter, int waterCounter) {
        String query = "UPDATE Rooms SET current_electric_counter = ?, current_water_counter = ? WHERE room_id = ?";

        try (Connection conn = DataBaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setInt(1, electricCounter);
            ps.setInt(1, electricCounter);
            ps.setInt(2, waterCounter);
            ps.setInt(3, roomId);

            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Room counters updated successfully!");
            } else {
                System.out.println("Room counter update failed. Room ID: " + roomId + " not found.");
            }

        } catch (SQLException e) {
            System.out.println("SQL Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ===== Room Deletion Methods =====

    public void deleteRoom(int roomId) {
        Connection conn = null;

        try {
            conn = DataBaseConnection.getConnection();
            conn.setAutoCommit(false);

            // First, check if room has tenants
            String checkQuery = "SELECT COUNT(*) FROM Tenants WHERE assigned_room_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(checkQuery)) {
                ps.setInt(1, roomId);
                ResultSet rs = ps.executeQuery();
                if (rs.next() && rs.getInt(1) > 0) {
                    System.out.println("Cannot delete room with assigned tenants.");
                    return;
                }
            }

            // Delete utility usage records
            String deleteUtilityQuery = "DELETE FROM UtilityUsage WHERE room_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(deleteUtilityQuery)) {
                ps.setInt(1, roomId);
                ps.executeUpdate();
            }

            // Delete room
            String deleteRoomQuery = "DELETE FROM Rooms WHERE room_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(deleteRoomQuery)) {
                ps.setInt(1, roomId);
                int rowsAffected = ps.executeUpdate();

                if (rowsAffected > 0) {
                    System.out.println("Room deleted successfully!");
                    conn.commit();
                } else {
                    System.out.println("Room deletion failed. Room ID: " + roomId + " not found.");
                    conn.rollback();
                }
            }

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
                if (conn != null) {
                    conn.setAutoCommit(true);
                    conn.close();
                }
            } catch (SQLException e) {
                System.out.println("Error closing connection: " + e.getMessage());
            }
        }
    }

    // Alternative to hard delete - mark room as inactive
    public void markRoomAsInactive(int roomId) {
        String query = "UPDATE Rooms SET is_active = false WHERE room_id = ?";

        try (Connection conn = DataBaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setInt(1, roomId);

            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Room marked as inactive successfully!");
            } else {
                System.out.println("Failed to mark room as inactive. Room ID: " + roomId + " not found.");
            }

        } catch (SQLException e) {
            System.out.println("SQL Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
    public void syncRoomWithTenant(String roomNumber) {
        try (Connection conn = DataBaseConnection.getConnection()) {
            Room room = getRoomByRoomNumber(roomNumber);
            if (room == null) {
//                System.out.println("Room not found: " + roomNumber);
                return;
            }

            int roomId = getRoomIdByRoomNumber(roomNumber);
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

                        room.setTenantDirectly(new Tenant(name, idCard, contact));
                        System.out.println("Room-tenant association synchronized successfully");
                    } else {
                        // No tenant assigned, make sure room is vacant
                        room.removeTenant();
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println("SQL Error syncing room with tenant: " + e.getMessage());
            e.printStackTrace();
        }
    }
    public void displayAllRooms() {
        String query = "SELECT room_id, floor_id, room_number, is_occupied FROM Rooms";

        try (Connection conn = DataBaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            System.out.println("\n=== Available Rooms ===");
            System.out.println("Room ID | Floor ID | Room Number | Occupied");
            System.out.println("----------------------------------------");

            boolean hasRooms = false;
            while (rs.next()) {
                hasRooms = true;
                int roomId = rs.getInt("room_id");
                int floorId = rs.getInt("floor_id");
                String roomNumber = rs.getString("room_number");
                boolean isOccupied = rs.getBoolean("is_occupied");

                System.out.printf("%-7d | %-8d | %-11s | %s%n",
                        roomId, floorId, roomNumber, isOccupied ? "Yes" : "No");
            }

            if (!hasRooms) {
                System.out.println("No rooms found in the database.");
            }
            System.out.println("----------------------------------------");

        } catch (SQLException e) {
            System.out.println("SQL Error listing rooms: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public boolean setUtilityUsageDirectly(String roomNumber, int electricUsage, int waterUsage) {
        try (Connection conn = DataBaseConnection.getConnection()) {
            // First, find the room ID directly
            PreparedStatement findRoom = conn.prepareStatement(
                    "SELECT room_id FROM Rooms WHERE room_number = ?");
            findRoom.setString(1, roomNumber);
            ResultSet rs = findRoom.executeQuery();

            if (!rs.next()) {
                System.out.println("ERROR: Room not found: '" + roomNumber + "'");
                // Run the debug utility to see what's in the database

                return false;
            }

            int roomId = rs.getInt("room_id");
            System.out.println("Found room with ID: " + roomId);

            // Use a simple approach - direct SQL insert/update
            LocalDate today = LocalDate.now();

            // Insert into UtilityUsage table
            PreparedStatement insertUsage = conn.prepareStatement(
                    "INSERT INTO UtilityUsage (room_id, electric_usage, water_usage, usage_date) " +
                            "VALUES (?, ?, ?, ?) " +
                            "ON DUPLICATE KEY UPDATE electric_usage = VALUES(electric_usage), water_usage = VALUES(water_usage)");
            insertUsage.setInt(1, roomId);
            insertUsage.setInt(2, electricUsage);
            insertUsage.setInt(3, waterUsage);
            insertUsage.setDate(4, java.sql.Date.valueOf(today));
            int usageUpdated = insertUsage.executeUpdate();

            System.out.println("Updated utility usage: " + usageUpdated + " rows");

            return true;
        } catch (SQLException e) {
            System.out.println("SQL Error setting utility: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }


}
