package DataBase;

import Properties.Floor;
import Properties.Building;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class FloorDML {
    // Save a new floor to the database
    public boolean saveFloor(Floor floor, int buildingId) {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet generatedKeys = null;

        try {
            conn = DataBaseConnection.getConnection();

            // Check if floor already exists
            if (getFloorIdByBuildingAndNumber(buildingId, floor.getFloorNumber()) != -1) {
                System.out.println("Floor " + floor.getFloorNumber() + " already exists in this building.");
                return false;
            }

            // Insert into Floors table
            String query = "INSERT INTO Floors (building_id, floor_number) VALUES (?, ?)";
            stmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
            stmt.setInt(1, buildingId);
            stmt.setString(2, floor.getFloorNumber());

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
                if (floor.getRooms() != null) {
                    floor.getRooms().forEach(room -> roomDML.saveRoom(room, floorId));
                }
                System.out.println("Floor " + floor.getFloorNumber() + " added successfully.");
                return true;
            } else {
                throw new SQLException("Failed to get floor_id");
            }

        } catch (SQLException e) {
            System.out.println("SQL Error: " + e.getMessage());
            e.printStackTrace();
            return false;
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

    // Get floor ID by building ID and floor number
    public int getFloorIdByBuildingAndNumber(int buildingId, String floorNumber) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = DataBaseConnection.getConnection();
            String query = "SELECT floor_id FROM Floors WHERE building_id = ? AND floor_number = ?";
            ps = conn.prepareStatement(query);
            ps.setInt(1, buildingId);
            ps.setString(2, floorNumber);

            rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt("floor_id");
            }
        } catch (SQLException e) {
            System.out.println("SQL Error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                if (rs != null) rs.close();
                if (ps != null) ps.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                System.out.println("Error closing resources: " + e.getMessage());
            }
        }

        return -1; // Return -1 if floor not found
    }

    // Get all floors for a building
    public List<Floor> getFloorsByBuildingId(int buildingId) {
        List<Floor> floors = new ArrayList<>();

        try (Connection conn = DataBaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT floor_id, floor_number FROM Floors WHERE building_id = ? ORDER BY floor_number")) {

            ps.setInt(1, buildingId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int floorId = rs.getInt("floor_id");
                    String floorNumber = rs.getString("floor_number");

                    // Create floor object
                    Floor floor = new Floor(floorNumber);

                    // Load rooms for this floor
                    loadRoomsForFloor(floor, floorId, conn);

                    floors.add(floor);
                }
            }
        } catch (SQLException e) {
            System.out.println("SQL Error: " + e.getMessage());
            e.printStackTrace();
        }

        return floors;
    }

    // Get floor by ID
    public Floor getFloorById(int floorId) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = DataBaseConnection.getConnection();
            String query = "SELECT floor_number FROM Floors WHERE floor_id = ?";
            ps = conn.prepareStatement(query);
            ps.setInt(1, floorId);

            rs = ps.executeQuery();
            if (rs.next()) {
                String floorNumber = rs.getString("floor_number");

                // Create and return the floor object
                Floor floor = new Floor(floorNumber);

                // Load rooms for this floor
                loadRoomsForFloor(floor, floorId, conn);

                return floor;
            }
        } catch (SQLException e) {
            System.out.println("SQL Error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                if (rs != null) rs.close();
                if (ps != null) ps.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                System.out.println("Error closing resources: " + e.getMessage());
            }
        }

        return null; // Floor not found
    }

    // Helper method to load rooms for a floor
    private void loadRoomsForFloor(Floor floor, int floorId, Connection conn) throws SQLException {
        String query = "SELECT room_id FROM Rooms WHERE floor_id = ?";

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
    public boolean updateFloor(int floorId, Floor floor) {
        try (Connection conn = DataBaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement("UPDATE Floors SET floor_number = ? WHERE floor_id = ?")) {

            ps.setString(1, floor.getFloorNumber());
            ps.setInt(2, floorId);

            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Floor updated successfully!");
                return true;
            } else {
                System.out.println("Floor not found or not updated.");
                return false;
            }
        } catch (SQLException e) {
            System.out.println("SQL Error: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // Delete floor
    public boolean deleteFloor(int floorId) {
        Connection conn = null;
        PreparedStatement ps = null;

        try {
            conn = DataBaseConnection.getConnection();
            conn.setAutoCommit(false);  // Start transaction

            // First, check if there are rooms on this floor
            try (PreparedStatement checkRooms = conn.prepareStatement("SELECT COUNT(*) FROM Rooms WHERE floor_id = ?")) {
                checkRooms.setInt(1, floorId);
                ResultSet rs = checkRooms.executeQuery();
                if (rs.next() && rs.getInt(1) > 0) {
                    // There are rooms on this floor, delete them first
                    try (PreparedStatement deleteRooms = conn.prepareStatement("DELETE FROM Rooms WHERE floor_id = ?")) {
                        deleteRooms.setInt(1, floorId);
                        deleteRooms.executeUpdate();
                    }
                }
            }

            // Now delete the floor
            String query = "DELETE FROM Floors WHERE floor_id = ?";
            ps = conn.prepareStatement(query);
            ps.setInt(1, floorId);

            int rowsAffected = ps.executeUpdate();

            conn.commit();  // Commit transaction

            if (rowsAffected > 0) {
                System.out.println("Floor deleted successfully!");
                return true;
            } else {
                System.out.println("Floor not found or not deleted.");
                return false;
            }
        } catch (SQLException e) {
            System.out.println("SQL Error: " + e.getMessage());
            e.printStackTrace();

            // Roll back transaction on error
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    System.out.println("Error rolling back transaction: " + ex.getMessage());
                }
            }
            return false;
        } finally {
            try {
                if (ps != null) ps.close();
                if (conn != null) {
                    conn.setAutoCommit(true);  // Reset auto-commit
                    conn.close();
                }
            } catch (SQLException e) {
                System.out.println("Error closing resources: " + e.getMessage());
            }
        }
    }

    // Delete floor by building ID and floor number
    public boolean deleteFloorByBuildingAndNumber(int buildingId, String floorNumber) {
        int floorId = getFloorIdByBuildingAndNumber(buildingId, floorNumber);
        if (floorId == -1) {
            System.out.println("Floor " + floorNumber + " not found in this building.");
            return false;
        }
        return deleteFloor(floorId);
    }
}
