package DataBase;

import java.sql.*;
import java.time.LocalDate;

public class BillRecordDML {
    // ====================================================================================================
    // Fields and Constructor
    // ====================================================================================================
    private BillDML billDML;

    public BillRecordDML() {
        this.billDML = new BillDML();
    }

    // ====================================================================================================
    // Payment Recording Methods
    // ====================================================================================================
    public int recordPayment(int billId, int tenantId, int landlordId, double amount) {
        String sql = "INSERT INTO BillRecords (bill_id, tenant_id, landlord_id, payment_amount) " +
                "VALUES (?, ?, ?, ?)";

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet generatedKeys = null;

        try {
            conn = DataBaseConnection.getConnection();
            conn.setAutoCommit(false);

            stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            stmt.setInt(1, billId);
            stmt.setInt(2, tenantId);
            stmt.setInt(3, landlordId);
            stmt.setDouble(4, amount);

            int affectedRows = stmt.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("Creating payment record failed, no rows affected.");
            }

            generatedKeys = stmt.getGeneratedKeys();
            int recordId = -1;

            if (generatedKeys.next()) {
                recordId = generatedKeys.getInt(1);

                String updateBillSql = "UPDATE Bills SET is_paid = TRUE WHERE bill_id = ?";
                try (PreparedStatement updateStmt = conn.prepareStatement(updateBillSql)) {
                    updateStmt.setInt(1, billId);
                    updateStmt.executeUpdate();
                }

                conn.commit();
                return recordId;
            } else {
                conn.rollback();
                throw new SQLException("Creating payment record failed, no ID obtained.");
            }
        } catch (SQLException e) {
            try {
                if (conn != null) conn.rollback();
            } catch (SQLException ex) {
                System.err.println("Error rolling back transaction: " + ex.getMessage());
            }
            System.err.println("Error recording payment: " + e.getMessage());
            return -1;
        } finally {
            try {
                if (generatedKeys != null) generatedKeys.close();
                if (stmt != null) stmt.close();
                if (conn != null) {
                    conn.setAutoCommit(true);
                    conn.close();
                }
            } catch (SQLException e) {
                System.err.println("Error closing resources: " + e.getMessage());
            }
        }
    }

}