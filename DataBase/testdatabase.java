package DataBase;

import java.sql.Connection;
import java.sql.SQLException;

public class testdatabase {
    public static void main(String[] args) {
        try (Connection connection = DataBaseConnection.getConnection()) {
            if (connection != null) {
                System.out.println("Database connection successful!");
            } else {
                System.out.println("Failed to make connection!");
            }
        } catch (SQLException e) {
            System.err.println("Connection error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
