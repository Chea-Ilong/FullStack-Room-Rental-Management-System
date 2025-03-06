//package Users;
//
//import DataBase.DataBaseConnection;
//import Users.User;
//
//import java.sql.Connection;
//import java.sql.PreparedStatement;
//import java.sql.ResultSet;
//import java.sql.SQLException;
//
//public class LandlordDML extends User {
//
//    private int landlordId;
//    private int landlordPin;
//
//    public LandlordDML(String name, String idCard, String contact, int landlordPin) {
//        super(name, idCard, contact, "Landlord");
//        this.landlordPin = landlordPin;
//    }
//
//    public void saveLandlord() throws SQLException {
//        String sql = "INSERT INTO Landlords (user_id, landlord_pin) VALUES (?, ?)";
//        try (Connection conn = DataBaseConnection.getConnection();
//             PreparedStatement pstmt = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
//            pstmt.setString(1, getUserId());
//            pstmt.setInt(2, landlordPin);
//            pstmt.executeUpdate();
//
//            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
//                if (generatedKeys.next()) {
//                    this.landlordId = generatedKeys.getInt(1);
//                } else {
//                    throw new SQLException("Creating landlord failed, no ID obtained.");
//                }
//            }
//        }
//    }
//
//    public String getUserId() {
//        return super.IdCard;
//    }
//}
