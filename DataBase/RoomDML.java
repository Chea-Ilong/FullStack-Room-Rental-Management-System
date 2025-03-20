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

    public void saveRoom(Room room, int floorId) {
        Connection conn = null;
        PreparedStatement roomStmt = null;
        ResultSet generatedKeys = null;

        try {
            conn = DataBaseConnection.getConnection();
            conn.setAutoCommit(false); // Start transaction


            // Save room to Rooms table
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
//
//    private boolean roomExists(Connection conn, String roomNumber, int floorId) throws SQLException {
//        String query = "SELECT COUNT(*) FROM Rooms WHERE room_number = ? AND floor_id = ?";
//        try (PreparedStatement ps = conn.prepareStatement(query)) {
//            ps.setString(1, roomNumber);
//            ps.setInt(2, floorId);
//            try (ResultSet rs = ps.executeQuery()) {
//                if (rs.next()) {
//                    return rs.getInt(1) > 0;
//                }
//            }
//        }
//        return false;
//    }

    public void createInitialBill(Connection conn, int roomId, double amount) {
        String billQuery = "INSERT INTO Bills (room_id, bill_type, amount, due_date, is_paid) VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement billStmt = conn.prepareStatement(billQuery)) {
            billStmt.setInt(1, roomId);
            billStmt.setString(2, "Rent and Utilities");
            billStmt.setDouble(3, amount);

            // Set due date to the end of the current month
            LocalDate dueDate = LocalDate.now().withDayOfMonth(LocalDate.now().lengthOfMonth());
            billStmt.setDate(4, Date.valueOf(dueDate));
            billStmt.setBoolean(5, false);

            int rowsAffected = billStmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Bill created successfully for room ID: " + roomId);
            } else {
                System.err.println("Failed to create bill for room ID: " + roomId);
            }
        } catch (SQLException e) {
            System.err.println("SQL Error creating bill: " + e.getMessage());
            e.printStackTrace();
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


            this.saveRoom(newRoom, floorId);
            System.out.println("Room " + roomNumber + " added successfully to floor " + floorNumber);

        } catch (Exception e) {
            System.out.println("Error adding room: " + e.getMessage());
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

    // Updated to use new schema - removed rent field references


//    // New method to load rent from Bills table
//    private void loadRentForRoom(Room room, int roomId, Connection conn) throws SQLException {
//        String query = "SELECT amount FROM Bills WHERE room_id = ? AND bill_type = 'Rent and Utilities' ORDER BY due_date DESC LIMIT 1";
//
//        try (PreparedStatement ps = conn.prepareStatement(query)) {
//            ps.setInt(1, roomId);
//
//            try (ResultSet rs = ps.executeQuery()) {
//                if (rs.next()) {
//                    double rent = rs.getDouble("amount");
//
//                }
//            }
//        }
//    }

    // Updated to use new schema - removed rent field references
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
    public void displayRoomDetails(String buildingName, String floorNumber, String roomNumber) {
        try {
            // Get building, floor, and room IDs
            BuildingDML buildingDML = new BuildingDML();
            int buildingId = buildingDML.getBuildingIdByName(buildingName);

            if (buildingId == -1) {
                System.out.println("Building not found: " + buildingName);
                return;
            }

            FloorDML floorDML = new FloorDML();
            int floorId = floorDML.getFloorIdByBuildingAndNumber(buildingId, floorNumber);

            if (floorId == -1) {
                System.out.println("Floor not found: " + floorNumber + " in building: " + buildingName);
                return;
            }

            int roomId = getRoomIdByFloorAndNumber(floorId, roomNumber);

            if (roomId == -1) {
                System.out.println("Room not found: " + roomNumber + " on floor: " + floorNumber);
                return;
            }

            // Get room details
            Room room = getRoomById(roomId);

            if (room == null) {
                System.out.println("Failed to retrieve room details for room: " + roomNumber);
                return;
            }

            // Display room details
            System.out.println("\n  ======= Room Details =======");
            System.out.println("  Building: " + buildingName);
            System.out.println("  Floor: " + floorNumber);
            System.out.println("  Room Number: " + room.getRoomNumber());
            System.out.println("  Status: " + (room.isOccupied() ? "Occupied" : "Vacant"));
            System.out.println("  Electric Counter: " + room.getCurrentElectricCounter());
            System.out.println("  Water Counter: " + room.getCurrentWaterCounter());

            // Display tenant information if room is occupied
            if (room.isOccupied() && room.getTenant() != null) {
                System.out.println("  Tenant Information:");
                System.out.println("    Name: " + room.getTenant().getName());
                System.out.println("    ID Card: " + room.getTenant().getIdCard());
                System.out.println("    Contact: " + room.getTenant().getContact());
            }

            System.out.println("  ============================");

        } catch (Exception e) {
            System.out.println("An error occurred while displaying room details: " + e.getMessage());
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

    // Updated to use new schema - removed rent field references
    private void loadRoomsForFloor(Floor floor, int floorId, Connection conn) throws SQLException {
        String query = "SELECT room_id, room_number, current_electric_counter, current_water_counter, is_occupied FROM Rooms WHERE floor_id = ?";

        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, floorId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int roomId = rs.getInt("room_id");
                    String roomNumber = rs.getString("room_number");
                    int electricCounter = rs.getInt("current_electric_counter");
                    int waterCounter = rs.getInt("current_water_counter");
                    boolean isOccupied = rs.getBoolean("is_occupied");

                    // Create the room directly from the query results
                    Room room = new Room(roomNumber, electricCounter, waterCounter);


                    // Load tenant information if room is occupied
                    if (isOccupied) {
                        loadTenantForRoom(room, roomId, conn);
                    }
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

    public void assignTenantToRoom(String roomNumber, Tenant tenant) {
        Connection conn = null;

        try {
            conn = DataBaseConnection.getConnection();
            conn.setAutoCommit(false); // Start transaction

            // Get room ID
            int roomId = getRoomIdByRoomNumber(roomNumber);

            if (roomId == -1) {
                System.out.println("Room not found: " + roomNumber);
                return;
            }

            // Check if room is already occupied
            String checkQuery = "SELECT is_occupied FROM Rooms WHERE room_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(checkQuery)) {
                ps.setInt(1, roomId);

                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next() && rs.getBoolean("is_occupied")) {
                        System.out.println("Room " + roomNumber + " is already occupied");
                        return;
                    }
                }
            }

            // Check if tenant is already assigned to another room
            // Check if tenant is already assigned to another room
            String checkTenantQuery = "SELECT r.room_number FROM Tenants t " +
                    "JOIN Users u ON t.user_id = u.user_id " +
                    "JOIN Rooms r ON t.assigned_room_id = r.room_id " +
                    "WHERE u.IdCard = ? AND t.assigned_room_id IS NOT NULL";

            try (PreparedStatement ps = conn.prepareStatement(checkTenantQuery)) {
                ps.setString(1, tenant.getIdCard());

                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        String existingRoom = rs.getString("room_number");
                        System.out.println("Tenant " + tenant.getName() + " is already assigned to room " + existingRoom);
                        System.out.println("Do you want to reassign this tenant? (y/n)");
                        // In a real application, you'd handle user input here
                        // For now, we'll just return
                        return;
                    }
                }
            }

            // Update room status to occupied
            String updateRoomQuery = "UPDATE Rooms SET is_occupied = TRUE WHERE room_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(updateRoomQuery)) {
                ps.setInt(1, roomId);
                ps.executeUpdate();
            }

            // Save tenant if not exists and update tenant's assigned room
            saveTenantIfNotExists(tenant, conn);
            updateTenantAssignedRoom(tenant, roomId, conn);

            conn.commit();
            System.out.println("Tenant " + tenant.getName() + " assigned to room " + roomNumber + " successfully");

        } catch (SQLException e) {
            try {
                if (conn != null) {
                    conn.rollback();
                }
            } catch (SQLException ex) {
                System.out.println("Failed to rollback: " + ex.getMessage());
            }

            System.out.println("Error assigning tenant to room: " + e.getMessage());
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

    private void saveTenantIfNotExists(Tenant tenant, Connection conn) throws SQLException {
        // Check if user exists
        String userQuery = "SELECT user_id FROM Users WHERE IdCard = ?";
        int userId = -1;

        try (PreparedStatement ps = conn.prepareStatement(userQuery)) {
            ps.setString(1, tenant.getIdCard());

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    userId = rs.getInt("user_id");
                }
            }
        }

        // If user doesn't exist, create new user
        if (userId == -1) {
            String insertUserQuery = "INSERT INTO Users (name, IdCard, contact, role) VALUES (?, ?, ?, 'Tenant')";

            try (PreparedStatement ps = conn.prepareStatement(insertUserQuery, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, tenant.getName());
                ps.setString(2, tenant.getIdCard());
                ps.setString(3, tenant.getContact());

                ps.executeUpdate();

                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        userId = rs.getInt(1);
                    } else {
                        throw new SQLException("Failed to get user_id for new tenant");
                    }
                }
            }
        }

        // Check if tenant record exists
        String checkTenantQuery = "SELECT tenant_id FROM Tenants WHERE user_id = ?";
        boolean tenantExists = false;

        try (PreparedStatement ps = conn.prepareStatement(checkTenantQuery)) {
            ps.setInt(1, userId);

            try (ResultSet rs = ps.executeQuery()) {
                tenantExists = rs.next();
            }
        }

        // If tenant record doesn't exist, create it
        if (!tenantExists) {
            String insertTenantQuery = "INSERT INTO Tenants (user_id) VALUES (?)";

            try (PreparedStatement ps = conn.prepareStatement(insertTenantQuery)) {
                ps.setInt(1, userId);
                ps.executeUpdate();
            }
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

                    // Create tenant and assign to room properly
                    Tenant tenant = new Tenant(name, idCard, contact);
                    try {
                        // Use a special method that bypasses the normal occupancy check
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
    public List<Room> getAllRooms() {
        List<Room> rooms = new ArrayList<>();
        String query = "SELECT r.room_id, r.room_number, r.current_electric_counter, " +
                "r.current_water_counter, r.is_occupied FROM Rooms r " +
                "ORDER BY r.room_number";

        try (Connection conn = DataBaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                int roomId = rs.getInt("room_id");
                String roomNumber = rs.getString("room_number");
                int electricCounter = rs.getInt("current_electric_counter");
                int waterCounter = rs.getInt("current_water_counter");
                boolean isOccupied = rs.getBoolean("is_occupied");

                // Create room
                Room room = new Room(roomNumber, electricCounter, waterCounter);

                // Set occupancy and tenant if occupied
                if (isOccupied) {
                    try {
                        room.markAsOccupied();
                        // Load tenant for this room
                        loadTenantForRoom(room, roomId, conn);
                    } catch (RoomException e) {
                        System.out.println("Error marking room as occupied: " + e.getMessage());
                    }
                }

                rooms.add(room);
            }
        } catch (SQLException e) {
            System.out.println("SQL Error retrieving all rooms: " + e.getMessage());
            e.printStackTrace();
        }

        return rooms;
    }
    // Add these methods to your existing RoomDML class

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

    public void updateRoomOccupancy(int roomId, boolean isOccupied) {
        String query = "UPDATE Rooms SET is_occupied = ? WHERE room_id = ?";

        try (Connection conn = DataBaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setBoolean(1, isOccupied);
            ps.setInt(2, roomId);

            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Room occupancy status updated successfully in database.");
            } else {
                System.out.println("No room was updated. Room ID may not exist.");
            }
        } catch (SQLException e) {
            System.out.println("SQL Error updating room occupancy: " + e.getMessage());
            e.printStackTrace();
        }
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

        return -1; // Room not found
    }
    public void loadRoomsForFloor(Floor floor, int floorId) {
        String query = "SELECT room_id, room_number, current_electric_counter, current_water_counter, is_occupied FROM Rooms WHERE floor_id = ?";

        try (Connection conn = DataBaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setInt(1, floorId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int roomId = rs.getInt("room_id");
                    String roomNumber = rs.getString("room_number");
                    int electricCounter = rs.getInt("current_electric_counter");
                    int waterCounter = rs.getInt("current_water_counter");
                    boolean isOccupied = rs.getBoolean("is_occupied");

                    // Create the room directly from the query results
                    Room room = new Room(roomNumber, electricCounter, waterCounter);

                    // Load tenant information if room is occupied
                    if (isOccupied) {
                        loadTenantForRoom(room, roomId, conn);
                    }
                    // Add room to floor
                    floor.addRoom(room);
                }
            }
        } catch (SQLException e) {
            System.out.println("SQL Error loading rooms for floor: " + e.getMessage());
            e.printStackTrace();
        }
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

    // Helper class to hold room details with building and floor info
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
    public Room getRoomByRoomNumber(String roomNumber) {
        String query = "SELECT r.room_id, r.room_number, r.current_electric_counter, " +
                "r.current_water_counter, r.is_occupied FROM Rooms r WHERE r.room_number = ?";

        try (Connection conn = DataBaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setString(1, roomNumber);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int roomId = rs.getInt("room_id");
                    int electricCounter = rs.getInt("current_electric_counter");
                    int waterCounter = rs.getInt("current_water_counter");
                    boolean isOccupied = rs.getBoolean("is_occupied");

                    // Create room object
                    Room room = new Room(roomNumber, electricCounter, waterCounter);

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
                } else {
                    System.out.println("No room found with number: " + roomNumber);
                    return null;
                }
            }
        } catch (SQLException e) {
            System.out.println("SQL Error retrieving room: " + e.getMessage());
            e.printStackTrace();
            return null;
        } catch (Exception e) {
            System.out.println("Unexpected error retrieving room: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
}
