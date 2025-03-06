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
//public class UserDML {
//
//    private int userId;
//    private String name;
//    private String idCard;
//    private String contact;
//    private String role;
//
//    public UserDML(String name, String idCard, String contact, String role) {
//        this.name = name;
//        this.idCard = idCard;
//        this.contact = contact;
//        this.role = role;
//    }
//
//    public int save() throws SQLException {
//        String sql = "INSERT INTO Users (name, IdCard, contact, role) VALUES (?, ?, ?, ?)";
//        try (Connection conn = DataBaseConnection.getConnection();
//             PreparedStatement pstmt = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
//            pstmt.setString(1, name);
//            pstmt.setString(2, idCard);
//            pstmt.setString(3, contact);
//            pstmt.setString(4, role);
//            pstmt.executeUpdate();
//
//            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
//                if (generatedKeys.next()) {
//                    this.userId = generatedKeys.getInt(1);
//                    return userId;
//                } else {
//                    throw new SQLException("Creating user failed, no ID obtained.");
//                }
//            }
//        }
//    }
////
////    public static User getUserById(int userId) throws SQLException {
////        String sql = "SELECT * FROM Users WHERE user_id = ?";
////        try (Connection conn = DataBaseConnection.getConnection();
////             PreparedStatement pstmt = conn.prepareStatement(sql)) {
////            pstmt.setInt(1, userId);
////            try (ResultSet rs = pstmt.executeQuery()) {
////                if (rs.next()) {
////                    return new User(rs.getString("name"), rs.getString("IdCard"),
////                            rs.getString("contact"), rs.getString("role"));
////                }
////            }
////        }
////        return null;
////    }
//}
