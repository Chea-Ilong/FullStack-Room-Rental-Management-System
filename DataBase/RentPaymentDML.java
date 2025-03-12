package DataBase;

import Payment.RentPayment;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class RentPaymentDML {

    public List<RentPayment> getPaymentHistoryForTenant(String tenantIdCard) {
        List<RentPayment> paymentHistory = new ArrayList<>();
        String query = "SELECT amount, payment_date, is_rent_payment, is_utility_payment " +
                "FROM Payments " +
                "JOIN Tenants t ON Payments.tenant_id = t.tenant_id " +
                "JOIN Users u ON t.user_id = u.user_id " +
                "WHERE u.IdCard = ? " +
                "ORDER BY payment_date DESC";

        try (Connection conn = DataBaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setString(1, tenantIdCard);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    double amount = rs.getDouble("amount");
                    LocalDate paymentDate = rs.getDate("payment_date").toLocalDate();
                    boolean isRentPayment = rs.getBoolean("is_rent_payment");
                    boolean isUtilityPayment = rs.getBoolean("is_utility_payment");

                    // Create payment record with retrieved data
                    RentPayment payment = new RentPayment(tenantIdCard, amount, isRentPayment, isUtilityPayment);
                    paymentHistory.add(payment);
                }
            }
        } catch (SQLException e) {
            System.out.println("SQL Error retrieving payment history: " + e.getMessage());
            e.printStackTrace();
        }

        return paymentHistory;
    }

    public void savePayment(RentPayment payment) {
        String query = "INSERT INTO Payments (tenant_id, amount, payment_date, is_rent_payment, is_utility_payment) " +
                "SELECT t.tenant_id, ?, ?, ?, ? " +
                "FROM Tenants t " +
                "JOIN Users u ON t.user_id = u.user_id " +
                "WHERE u.IdCard = ?";

        try (Connection conn = DataBaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setDouble(1, payment.getAmount());
            ps.setDate(2, java.sql.Date.valueOf(payment.getPaymentDate()));
            ps.setBoolean(3, payment.isRentPayment());
            ps.setBoolean(4, payment.isUtilitiesPayment());
            ps.setString(5, payment.getTenantID());

            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Payment saved successfully!");
            } else {
                System.out.println("Failed to save payment - tenant not found.");
            }
        } catch (SQLException e) {
            System.out.println("SQL Error saving payment: " + e.getMessage());
            e.printStackTrace();
        }
    }
}