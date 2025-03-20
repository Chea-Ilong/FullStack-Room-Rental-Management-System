package DataBase;

import Properties.Building;
import Properties.Floor;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BuildingDML {
    // ====================================================================================================
    // Building CRUD Operations
    // ====================================================================================================
    public void saveBuilding(Building building) {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet generatedKeys = null;

        try {
            conn = DataBaseConnection.getConnection();

            String query = "INSERT INTO Buildings (building_name, address) VALUES (?, ?)";
            stmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);

            stmt.setString(1, building.getBuildingName());
            stmt.setString(2, building.getAddress());

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected <= 0) {
                throw new SQLException("Failed to create building record");
            }

            generatedKeys = stmt.getGeneratedKeys();
            if (generatedKeys.next()) {
                int buildingId = generatedKeys.getInt(1);
                System.out.println("Building saved successfully! Building ID: " + buildingId);

                FloorDML floorDML = new FloorDML();
                building.getFloors().forEach(floor -> floorDML.saveFloor(floor, buildingId));
            } else {
                throw new SQLException("Failed to get building_id");
            }

        } catch (SQLException e) {
            System.out.println("SQL Error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                if (generatedKeys != null) generatedKeys.close();
                if (stmt != null) stmt.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                System.out.println("Error closing resources: " + e.getMessage());
            }
        }
    }

    public void updateBuilding(int buildingId, Building building) {
        String query = "UPDATE Buildings SET building_name = ?, address = ? WHERE building_id = ?";

        try (Connection conn = DataBaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setString(1, building.getBuildingName());
            ps.setString(2, building.getAddress());
            ps.setInt(3, buildingId);

            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Building updated successfully!");
            } else {
                System.out.println("Building not found or not updated.");
            }

        } catch (SQLException e) {
            System.out.println("SQL Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void deleteBuilding(int buildingId) {
        String query = "DELETE FROM Buildings WHERE building_id = ?";

        try (Connection conn = DataBaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setInt(1, buildingId);

            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Building deleted successfully!");
            } else {
                System.out.println("Building not found or not deleted.");
            }

        } catch (SQLException e) {
            System.out.println("SQL Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ====================================================================================================
    // Building Retrieval Methods
    // ====================================================================================================
    public Building getBuildingById(int buildingId) {
        String query = "SELECT building_name, address FROM Buildings WHERE building_id = ?";

        try (Connection conn = DataBaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setInt(1, buildingId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String buildingName = rs.getString("building_name");
                    String address = rs.getString("address");

                    Building building = new Building(buildingName, address);
                    loadFloorsForBuilding(building, buildingId, conn);

                    return building;
                }
            }
        } catch (SQLException e) {
            System.out.println("SQL Error: " + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }

    public Building getBuildingByName(String buildingName) {
        String query = "SELECT building_id, building_name, address FROM Buildings WHERE building_name = ?";

        try (Connection conn = DataBaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setString(1, buildingName);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int buildingId = rs.getInt("building_id");
                    String address = rs.getString("address");

                    Building building = new Building(buildingName, address);
                    loadFloorsForBuilding(building, buildingId, conn);

                    return building;
                }
            }
        } catch (SQLException e) {
            System.out.println("SQL Error: " + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }

    public List<Building> getAllBuildings() {
        List<Building> buildings = new ArrayList<>();
        String query = "SELECT building_id, building_name, address FROM Buildings";

        try (Connection conn = DataBaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                int buildingId = rs.getInt("building_id");
                String buildingName = rs.getString("building_name");
                String address = rs.getString("address");

                Building building = new Building(buildingName, address);
                loadFloorsForBuilding(building, buildingId, conn);
                buildings.add(building);
            }
        } catch (SQLException e) {
            System.out.println("SQL Error: " + e.getMessage());
            e.printStackTrace();
        }

        return buildings;
    }

    public List<Map<String, Object>> getAllBuildingsWithIds() {
        List<Map<String, Object>> buildingsWithIds = new ArrayList<>();
        String query = "SELECT building_id, building_name, address FROM Buildings";

        try (Connection conn = DataBaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Map<String, Object> buildingData = new HashMap<>();
                buildingData.put("id", rs.getInt("building_id"));
                buildingData.put("name", rs.getString("building_name"));
                buildingData.put("address", rs.getString("address"));
                buildingsWithIds.add(buildingData);
            }
        } catch (SQLException e) {
            System.out.println("SQL Error: " + e.getMessage());
            e.printStackTrace();
        }

        return buildingsWithIds;
    }

    public List<String> getAllBuildingNames() {
        List<String> buildingNames = new ArrayList<>();
        try (Connection conn = DataBaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT building_name FROM Buildings")) {
            while (rs.next()) {
                buildingNames.add(rs.getString("building_name"));
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving building names: " + e.getMessage());
        }
        return buildingNames;
    }

    // ====================================================================================================
    // Floor Management Methods
    // ====================================================================================================
    public void addFloorToBuilding(String buildingName, Floor floor) {
        String query = "SELECT building_id FROM Buildings WHERE building_name = ?";

        try (Connection conn = DataBaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setString(1, buildingName);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int buildingId = rs.getInt("building_id");
                    FloorDML floorDML = new FloorDML();
                    floorDML.saveFloor(floor, buildingId);
                    System.out.println("Floor added successfully to building: " + buildingName);
                } else {
                    System.out.println("Building not found: " + buildingName);
                }
            }
        } catch (SQLException e) {
            System.out.println("SQL Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ====================================================================================================
    // Helper Methods
    // ====================================================================================================
    private void loadFloorsForBuilding(Building building, int buildingId, Connection conn) throws SQLException {
        String query = "SELECT floor_id, floor_number FROM Floors WHERE building_id = ?";

        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, buildingId);

            try (ResultSet rs = ps.executeQuery()) {
                FloorDML floorDML = new FloorDML();
                while (rs.next()) {
                    int floorId = rs.getInt("floor_id");
                    building.addFloor(floorDML.getFloorById(floorId));
                }
            }
        }
    }

    public int getBuildingIdByName(String buildingName) {
        String query = "SELECT building_id FROM Buildings WHERE building_name = ?";

        try (Connection conn = DataBaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setString(1, buildingName);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("building_id");
                }
            }
        } catch (SQLException e) {
            System.out.println("SQL Error: " + e.getMessage());
            e.printStackTrace();
        }

        return -1;
    }

    public boolean buildingExistsByName(String name) {
        String query = "SELECT COUNT(*) FROM Buildings WHERE building_name = ?";
        try (Connection conn = DataBaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, name);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            System.out.println("SQL Error checking building name: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    public boolean buildingExistsByNameAndAddress(String name, String address, int excludeId) {
        String query = "SELECT COUNT(*) FROM Buildings WHERE building_name = ? AND address = ? AND building_id != ?";
        try (Connection conn = DataBaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, name);
            stmt.setString(2, address);
            stmt.setInt(3, excludeId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            System.out.println("SQL Error checking building name and address: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }
}