package DataBase;

import Payment.UtilityUsage;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class UtilityUsageDML {

    // ====================================================================================================
    // Save Utility Usage
    // ====================================================================================================
    public void saveUtilityUsage(UtilityUsage usage, int roomId) {
        String checkQuery = "SELECT COUNT(*) FROM UtilityUsage WHERE room_id = ? AND usage_date = ?";
        String insertQuery = "INSERT INTO UtilityUsage (room_id, electric_usage, water_usage, usage_date) VALUES (?, ?, ?, ?)";
        String roomCheckQuery = "SELECT COUNT(*) FROM Rooms WHERE room_id = ?";

        try (Connection conn = DataBaseConnection.getConnection();
             PreparedStatement checkPs = conn.prepareStatement(roomCheckQuery)) {

            checkPs.setInt(1, roomId);
            try (ResultSet rs = checkPs.executeQuery()) {
                if (rs.next() && rs.getInt(1) == 0) {
                    System.out.println("Error: Room with ID " + roomId + " does not exist in database");
                    return;
                }
            }

            // If we get here, no duplicate exists, so proceed with insert
            try (PreparedStatement ps = conn.prepareStatement(insertQuery)) {
                ps.setInt(1, roomId);
                ps.setInt(2, usage.getElectricUsage());
                ps.setInt(3, usage.getWaterUsage());
                ps.setDate(4, java.sql.Date.valueOf(usage.getDate()));

                int rowsAffected = ps.executeUpdate();
                if (rowsAffected > 0) {
                    System.out.println("Utility usage saved successfully!");
                } else {
                    System.out.println("Failed to save utility usage.");
                }
            }
        } catch (SQLException e) {
            System.out.println("SQL Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ====================================================================================================
    // Get All Utility Usage Records for a Room
    // ====================================================================================================
    public List<UtilityUsage> getUtilityUsageByRoomId(int roomId) {
        List<UtilityUsage> usageList = new ArrayList<>();
        String query = "SELECT electric_usage, water_usage, usage_date FROM UtilityUsage WHERE room_id = ? ORDER BY usage_date DESC";

        try (Connection conn = DataBaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setInt(1, roomId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int electricUsage = rs.getInt("electric_usage");
                    int waterUsage = rs.getInt("water_usage");
                    LocalDate date = rs.getDate("usage_date").toLocalDate();

                    UtilityUsage usage = new UtilityUsage(electricUsage, waterUsage, date);
                    usageList.add(usage);
                }
            }

        } catch (SQLException e) {
            System.out.println("SQL Error: " + e.getMessage());
            e.printStackTrace();
        }

        return usageList;
    }

    // ====================================================================================================
    // Get Utility Usage for a Specific Month
    // ====================================================================================================

//    public UtilityUsage getUtilityUsageByMonth(int roomId, int year, int month) {
//        String query = "SELECT electric_usage, water_usage, usage_date FROM UtilityUsage " +
//                "WHERE room_id = ? AND YEAR(usage_date) = ? AND MONTH(usage_date) = ? " +
//                "ORDER BY usage_date DESC LIMIT 1";
//
//        try (Connection conn = DataBaseConnection.getConnection();
//             PreparedStatement ps = conn.prepareStatement(query)) {
//
//            ps.setInt(1, roomId);
//            ps.setInt(2, year);
//            ps.setInt(3, month);
//
//            try (ResultSet rs = ps.executeQuery()) {
//                if (rs.next()) {
//                    int electricUsage = rs.getInt("electric_usage");
//                    int waterUsage = rs.getInt("water_usage");
//                    LocalDate date = rs.getDate("usage_date").toLocalDate();
//
//                    return new UtilityUsage(electricUsage, waterUsage, date);
//                }
//            }
//
//        } catch (SQLException e) {
//            System.out.println("SQL Error: " + e.getMessage());
//            e.printStackTrace();
//        }
//
//        return null; // No usage found for that month
//    }

    // ====================================================================================================
    // Delete Utility Usage Record
    // ====================================================================================================

//    public void deleteUtilityUsage(int usageId) {
//        String query = "DELETE FROM UtilityUsage WHERE usage_id = ?";
//
//        try (Connection conn = DataBaseConnection.getConnection();
//             PreparedStatement ps = conn.prepareStatement(query)) {
//
//            ps.setInt(1, usageId);
//
//            int rowsAffected = ps.executeUpdate();
//            if (rowsAffected > 0) {
//                System.out.println("Utility usage record deleted successfully!");
//            } else {
//                System.out.println("Record not found or not deleted.");
//            }
//
//        } catch (SQLException e) {
//            System.out.println("SQL Error: " + e.getMessage());
//            e.printStackTrace();
//        }
//    }
}
