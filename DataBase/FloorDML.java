package DataBase;

import Properties.Floor;
import java.sql.*;

public class FloorDML {

    // Save a new floor to the database
    public void saveFloor(Floor floor, int buildingId) {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet generatedKeys = null;

        try {
            conn = DataBaseConnection.getConnection();

            // Insert into Floors table
            String query = "INSERT INTO Floors (building_id, floor_number) VALUES (?, ?)";
            stmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
            stmt.setInt(1, buildingId);
            stmt.setString(2, floor.getFloorNumber()); // This should probably be setInt if floor_number is INT

            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected <= 0) {
                throw new SQLException("Failed to create floor record");
            }

            // Get the generated floor_id
            generatedKeys = stmt.getGeneratedKeys();
            if (generatedKeys.next()) {
                int floorId = generatedKeys.getInt(1);

                // Now save all rooms for this floor
                RoomDML roomDML = new RoomDML();
                floor.getRooms().forEach(room -> roomDML.saveRoom(room, floorId));
            } else {
                throw new SQLException("Failed to get floor_id");
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




    // Get floor by ID
    public Floor getFloorById(int floorId) {
        String query = "SELECT floor_number FROM Floors WHERE floor_id = ?";

        try (Connection conn = DataBaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setInt(1, floorId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String floorNumber = rs.getString("floor_number");

                    // Create and return the floor object
                    Floor floor = new Floor(floorNumber);

                    // Load rooms for this floor
                    loadRoomsForFloor(floor, floorId, conn);

                    return floor;
                }
            }
        } catch (SQLException e) {
            System.out.println("SQL Error: " + e.getMessage());
            e.printStackTrace();
        }

        return null; // Floor not found
    }

    // Helper method to load rooms for a floor
    private void loadRoomsForFloor(Floor floor, int floorId, Connection conn) throws SQLException {
        String query = "SELECT room_id, room_number, rent, current_electric_counter, current_water_counter, is_occupied FROM Rooms WHERE floor_id = ?";

        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, floorId);

            try (ResultSet rs = ps.executeQuery()) {
                RoomDML roomDML = new RoomDML();
                while (rs.next()) {
                    int roomId = rs.getInt("room_id");

                    // Add room to floor
                    floor.addRoom(roomDML.getRoomById(roomId));
                }
            }
        }
    }

    // Update floor information
    public void updateFloor(int floorId, Floor floor) {
        String query = "UPDATE Floors SET floor_number = ? WHERE floor_id = ?";

        try (Connection conn = DataBaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setString(1, floor.getFloorNumber());
            ps.setInt(2, floorId);

            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Floor updated successfully!");
            } else {
                System.out.println("Floor not found or not updated.");
            }

        } catch (SQLException e) {
            System.out.println("SQL Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Delete floor
    public void deleteFloor(int floorId) {
        // Note: This will only work if there are no rooms associated with this floor
        // due to foreign key constraints
        String query = "DELETE FROM Floors WHERE floor_id = ?";

        try (Connection conn = DataBaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setInt(1, floorId);

            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Floor deleted successfully!");
            } else {
                System.out.println("Floor not found or not deleted.");
            }

        } catch (SQLException e) {
            System.out.println("SQL Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}