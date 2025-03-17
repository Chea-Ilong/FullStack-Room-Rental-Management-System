package DataBase;

import java.sql.*;
import java.time.LocalDate;

public class BillRecordDML {
    private Connection connection;
    private BillDML billDML;

    // Constructor
    public BillRecordDML() {
        this.connection = connection;
        this.billDML = new BillDML();
    }

    /**
     * Record a payment for a bill
     *
     * @param billId ID of the bill being paid
     * @param tenantId ID of the tenant making the payment
     * @param paymentAmount Amount being paid
     * @param paymentMethod Method of payment (Cash, Bank Transfer, etc.)
     * @param referenceNumber Reference number for the payment (optional)
     * @param landlordId ID of the landlord who received the payment
     * @param notes Additional notes about the payment
     * @return ID of the newly created payment record
     */
    /**
     * Record a payment for a bill
     *
     * @param billId     ID of the bill being paid
     * @param tenantId   ID of the tenant making the payment
     * @param landlordId ID of the landlord receiving the payment
     * @param amount     Amount being paid
     *                   //     * @param paymentMethod Method of payment
     *                   //     * @param referenceNumber Reference number for the payment
     *                   //     * @param notes Additional notes
     * @return ID of the created payment record, or -1 if recording failed
     */
    public int recordPayment(int billId, int tenantId, int landlordId, double amount) {
        String sql = "INSERT INTO BillRecords (bill_id, tenant_id, landlord_id, payment_amount) " +
                "VALUES (?, ?, ?, ?)";

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet generatedKeys = null;

        try {
            conn = DataBaseConnection.getConnection();
            conn.setAutoCommit(false); // Start transaction

            // Insert payment record
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

                // Now update the bill status to paid
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


    /**
     * Update the tenant's balance due after a payment
     *
     * @param tenantId ID of the tenant
     */
    private void updateTenantBalanceAfterPayment(int tenantId) throws SQLException {
        // Recalculate the tenant's balance based on unpaid bills
        double balance = billDML.getTenantBalanceDue(String.valueOf(tenantId));

        // Update the tenant's balance in the database
        String sql = "UPDATE Tenants SET balance_due = ? WHERE tenant_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setDouble(1, balance);
            stmt.setInt(2, tenantId);
            stmt.executeUpdate();
        }
    }

    /**
     * Get payment records for a specific bill
     *
     * @param billId ID of the bill
     * @return ResultSet containing payment records
     */
    public ResultSet getPaymentRecordsByBillId(int billId) {
        String sql = "SELECT br.*, u.name as landlord_name, t.user_id as tenant_user_id " +
                "FROM BillRecords br " +
                "LEFT JOIN Landlords l ON br.landlord_id = l.landlord_id " +
                "LEFT JOIN Users u ON l.user_id = u.user_id " +
                "LEFT JOIN Tenants t ON br.tenant_id = t.tenant_id " +
                "WHERE br.bill_id = ? " +
                "ORDER BY br.payment_date DESC";

        try {
            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.setInt(1, billId);
            return stmt.executeQuery();
        } catch (SQLException e) {
            System.err.println("Error getting payment records: " + e.getMessage());
            return null;
        }
    }


    /**
     * Get payment records for a specific tenant
     *
     * @param tenantId ID of the tenant
     * @return ResultSet containing payment records
     */
    public ResultSet getPaymentRecordsByTenantId(int tenantId) {
        String sql = "SELECT br.*, " +
                "r.room_number, f.floor_number, bg.building_name, " +
                "u.name as landlord_name " +
                "FROM BillRecords br " +
                "JOIN Bills b ON br.bill_id = b.bill_id " +
                "JOIN Rooms r ON b.room_id = r.room_id " +
                "JOIN Floors f ON r.floor_id = f.floor_id " +
                "JOIN Buildings bg ON f.building_id = bg.building_id " +
                "LEFT JOIN Landlords l ON br.landlord_id = l.landlord_id " +
                "LEFT JOIN Users u ON l.user_id = u.user_id " +
                "WHERE br.tenant_id = ? " +
                "ORDER BY br.payment_date DESC";

        try {
            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.setInt(1, tenantId);
            return stmt.executeQuery();
        } catch (SQLException e) {
            System.err.println("Error getting tenant payment records: " + e.getMessage());
            return null;
        }
    }


    /**
     * Get payment records by date range
     *
     * @param startDate Start date of the range
     * @param endDate End date of the range
     * @return ResultSet containing payment records in the specified date range
     */
    public ResultSet getPaymentRecordsByDateRange(LocalDate startDate, LocalDate endDate) {
        String sql = "SELECT br.*, " +
                "r.room_number, f.floor_number, bg.building_name, " +
                "u.name as landlord_name, u2.name as tenant_name " +
                "FROM BillRecords br " +
                "JOIN Bills b ON br.bill_id = b.bill_id " +
                "JOIN Rooms r ON b.room_id = r.room_id " +
                "JOIN Floors f ON r.floor_id = f.floor_id " +
                "JOIN Buildings bg ON f.building_id = bg.building_id " +
                "LEFT JOIN Landlords l ON br.landlord_id = l.landlord_id " +
                "LEFT JOIN Users u ON l.user_id = u.user_id " +
                "LEFT JOIN Tenants t ON br.tenant_id = t.tenant_id " +
                "LEFT JOIN Users u2 ON t.user_id = u2.user_id " +
                "WHERE DATE(br.payment_date) BETWEEN ? AND ? " +
                "ORDER BY br.payment_date DESC";

        try {
            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.setDate(1, Date.valueOf(startDate));
            stmt.setDate(2, Date.valueOf(endDate));
            return stmt.executeQuery();
        } catch (SQLException e) {
            System.err.println("Error getting payment records by date range: " + e.getMessage());
            return null;
        }
    }


    /**
     * Get payment records by landlord
     *
     * @param landlordId ID of the landlord
     * @return ResultSet containing payment records received by the specified landlord
     */
    public ResultSet getPaymentRecordsByLandlordId(int landlordId) {
        String sql = "SELECT br.*, " +
                "r.room_number, f.floor_number, bg.building_name, " +
                "u2.name as tenant_name " +
                "FROM BillRecords br " +
                "JOIN Bills b ON br.bill_id = b.bill_id " +
                "JOIN Rooms r ON b.room_id = r.room_id " +
                "JOIN Floors f ON r.floor_id = f.floor_id " +
                "JOIN Buildings bg ON f.building_id = bg.building_id " +
                "LEFT JOIN Tenants t ON br.tenant_id = t.tenant_id " +
                "LEFT JOIN Users u2 ON t.user_id = u2.user_id " +
                "WHERE br.landlord_id = ? " +
                "ORDER BY br.payment_date DESC";

        try {
            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.setInt(1, landlordId);
            return stmt.executeQuery();
        } catch (SQLException e) {
            System.err.println("Error getting landlord payment records: " + e.getMessage());
            return null;
        }
    }


    /**
     * Update a payment record
     *
     * @param recordId ID of the record to update
     * @param paymentAmount New payment amount
     * @param paymentMethod New payment method
     * @param referenceNumber New reference number
     * @param notes New notes
     * @return true if successful, false otherwise
//     */
//    public boolean updatePaymentRecord(int recordId, double paymentAmount,
//                                       String paymentMethod, String referenceNumber, String notes) {
//        String sql = "UPDATE BillRecords SET payment_amount = ?, payment_method = ?, " +
//                "reference_number = ?, notes = ? WHERE record_id = ?";
//
//        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
//            stmt.setDouble(1, paymentAmount);
//            stmt.setString(2, paymentMethod);
//            stmt.setString(3, referenceNumber);
//            stmt.setString(4, notes);
//            stmt.setInt(5, recordId);
//            int rowsAffected = stmt.executeUpdate();
//            return rowsAffected > 0;
//        } catch (SQLException e) {
//            System.err.println("Error updating payment record: " + e.getMessage());
//            return false;
//        }
//    }
//
//    /**
//     * Delete a payment record
//     *
//     * @param recordId ID of the record to delete
//     * @param billId ID of the associated bill
//     * @param tenantId ID of the associated tenant
//     * @return true if successful, false otherwise
//     */
//    public boolean deletePaymentRecord(int recordId, int billId, int tenantId) {
//        String sql = "DELETE FROM BillRecords WHERE record_id = ?";
//
//        try {
//            connection.setAutoCommit(false);
//
//            // First delete the payment record
//            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
//                stmt.setInt(1, recordId);
//                int rowsAffected = stmt.executeUpdate();
//
//                if (rowsAffected > 0) {
//                    // Check if there are any remaining payments for this bill
//                    String checkSql = "SELECT COUNT(*) as count FROM BillRecords WHERE bill_id = ?";
//                    try (PreparedStatement checkStmt = connection.prepareStatement(checkSql)) {
//                        checkStmt.setInt(1, billId);
//                        try (ResultSet rs = checkStmt.executeQuery()) {
//                            if (rs.next() && rs.getInt("count") == 0) {
//                                // If this was the only payment, mark the bill as unpaid
//                                String updateSql = "UPDATE Bills SET is_paid = FALSE WHERE bill_id = ?";
//                                try (PreparedStatement updateStmt = connection.prepareStatement(updateSql)) {
//                                    updateStmt.setInt(1, billId);
//                                    updateStmt.executeUpdate();
//                                }
//                            }
//                        }
//                    }
//
//                    // Update tenant balance
//                    updateTenantBalanceAfterPayment(tenantId);
//                    connection.commit();
//                    return true;
//                } else {
//                    connection.rollback();
//                    return false;
//                }
//            }
//        } catch (SQLException e) {
//            try {
//                connection.rollback();
//            } catch (SQLException ex) {
//                System.err.println("Error rolling back transaction: " + ex.getMessage());
//            }
//            System.err.println("Error deleting payment record: " + e.getMessage());
//            return false;
//        } finally {
//            try {
//                connection.setAutoCommit(true);
//            } catch (SQLException e) {
//                System.err.println("Error resetting auto-commit: " + e.getMessage());
//            }
//        }
//    }
//
//    /**
//     * Generate a monthly income report
//     *
//     * @param year The year for the report
//     * @param month The month for the report (1-12)
//     * @return Map containing income statistics for the specified month
//     */
//    public Map<String, Object> generateMonthlyIncomeReport(int year, int month) {
//        Map<String, Object> report = new HashMap<>();
//
//        // Create date range for the specified month
//        LocalDate startDate = LocalDate.of(year, month, 1);
//        LocalDate endDate = YearMonth.of(year, month).atEndOfMonth();
//
//        String sql = "SELECT SUM(payment_amount) as total_income, " +
//                "COUNT(*) as payment_count, " +
//                "MIN(payment_amount) as min_payment, " +
//                "MAX(payment_amount) as max_payment, " +
//                "AVG(payment_amount) as avg_payment, " +
//                "COUNT(DISTINCT tenant_id) as unique_tenants, " +
//                "COUNT(DISTINCT landlord_id) as unique_landlords " +
//                "FROM BillRecords " +
//                "WHERE DATE(payment_date) BETWEEN ? AND ?";
//
//        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
//            stmt.setDate(1, Date.valueOf(startDate));
//            stmt.setDate(2, Date.valueOf(endDate));
//
//            try (ResultSet rs = stmt.executeQuery()) {
//                if (rs.next()) {
//                    report.put("year", year);
//                    report.put("month", month);
//                    report.put("totalIncome", rs.getDouble("total_income"));
//                    report.put("paymentCount", rs.getInt("payment_count"));
//                    report.put("minPayment", rs.getDouble("min_payment"));
//                    report.put("maxPayment", rs.getDouble("max_payment"));
//                    report.put("avgPayment", rs.getDouble("avg_payment"));
//                    report.put("uniqueTenants", rs.getInt("unique_tenants"));
//                    report.put("uniqueLandlords", rs.getInt("unique_landlords"));
//                }
//            }
//
//            // Add payment method breakdown
//            report.put("paymentMethodBreakdown", getPaymentMethodBreakdown(startDate, endDate));
//
//            // Add building income breakdown
//            report.put("buildingIncomeBreakdown", getBuildingIncomeBreakdown(startDate, endDate));
//
//            // Add daily income data
//            report.put("dailyIncomeData", getDailyIncomeData(startDate, endDate));
//        } catch (SQLException e) {
//            System.err.println("Error generating monthly income report: " + e.getMessage());
//        }
//
//        return report;
//    }
//
//    /**
//     * Get breakdown of income by payment method for a date range
//     *
//     * @param startDate Start date of the range
//     * @param endDate End date of the range
//     * @return List of maps containing payment method and amount
//     */
//    private List<Map<String, Object>> getPaymentMethodBreakdown(LocalDate startDate, LocalDate endDate) {
//        List<Map<String, Object>> breakdown = new ArrayList<>();
//
//        String sql = "SELECT payment_method, SUM(payment_amount) as total, COUNT(*) as count " +
//                "FROM BillRecords " +
//                "WHERE DATE(payment_date) BETWEEN ? AND ? " +
//                "GROUP BY payment_method " +
//                "ORDER BY total DESC";
//
//        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
//            stmt.setDate(1, Date.valueOf(startDate));
//            stmt.setDate(2, Date.valueOf(endDate));
//
//            try (ResultSet rs = stmt.executeQuery()) {
//                while (rs.next()) {
//                    Map<String, Object> methodData = new HashMap<>();
//                    methodData.put("method", rs.getString("payment_method"));
//                    methodData.put("amount", rs.getDouble("total"));
//                    methodData.put("count", rs.getInt("count"));
//                    breakdown.add(methodData);
//                }
//            }
//        } catch (SQLException e) {
//            System.err.println("Error getting payment method breakdown: " + e.getMessage());
//        }
//
//        return breakdown;
//    }
//
//    /**
//     * Get breakdown of income by building for a date range
//     *
//     * @param startDate Start date of the range
//     * @param endDate End date of the range
//     * @return List of maps containing building info and income
//     */
//    private List<Map<String, Object>> getBuildingIncomeBreakdown(LocalDate startDate, LocalDate endDate) {
//        List<Map<String, Object>> breakdown = new ArrayList<>();
//
//        String sql = "SELECT bg.building_id, bg.building_name, " +
//                "SUM(br.payment_amount) as total_income, COUNT(br.record_id) as payment_count " +
//                "FROM BillRecords br " +
//                "JOIN Bills b ON br.bill_id = b.bill_id " +
//                "JOIN Rooms r ON b.room_id = r.room_id " +
//                "JOIN Floors f ON r.floor_id = f.floor_id " +
//                "JOIN Buildings bg ON f.building_id = bg.building_id " +
//                "WHERE DATE(br.payment_date) BETWEEN ? AND ? " +
//                "GROUP BY bg.building_id, bg.building_name " +
//                "ORDER BY total_income DESC";
//
//        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
//            stmt.setDate(1, Date.valueOf(startDate));
//            stmt.setDate(2, Date.valueOf(endDate));
//
//            try (ResultSet rs = stmt.executeQuery()) {
//                while (rs.next()) {
//                    Map<String, Object> buildingData = new HashMap<>();
//                    buildingData.put("buildingId", rs.getInt("building_id"));
//                    buildingData.put("buildingName", rs.getString("building_name"));
//                    buildingData.put("income", rs.getDouble("total_income"));
//                    buildingData.put("paymentCount", rs.getInt("payment_count"));
//                    breakdown.add(buildingData);
//                }
//            }
//        } catch (SQLException e) {
//            System.err.println("Error getting building income breakdown: " + e.getMessage());
//        }
//
//        return breakdown;
//    }
//
//    /**
//     * Get daily income data for a date range
//     *
//     * @param startDate Start date of the range
//     * @param endDate End date of the range
//     * @return List of maps containing daily income data
//     */
//    private List<Map<String, Object>> getDailyIncomeData(LocalDate startDate, LocalDate endDate) {
//        List<Map<String, Object>> dailyData = new ArrayList<>();
//
//        String sql = "SELECT DATE(payment_date) as payment_day, " +
//                "SUM(payment_amount) as daily_total, COUNT(*) as payment_count " +
//                "FROM BillRecords " +
//                "WHERE DATE(payment_date) BETWEEN ? AND ? " +
//                "GROUP BY payment_day " +
//                "ORDER BY payment_day";
//
//        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
//            stmt.setDate(1, Date.valueOf(startDate));
//            stmt.setDate(2, Date.valueOf(endDate));
//
//            try (ResultSet rs = stmt.executeQuery()) {
//                while (rs.next()) {
//                    Map<String, Object> dayData = new HashMap<>();
//                    dayData.put("date", rs.getDate("payment_day").toLocalDate());
//                    dayData.put("amount", rs.getDouble("daily_total"));
//                    dayData.put("count", rs.getInt("payment_count"));
//                    dailyData.add(dayData);
//                }
//            }
//        } catch (SQLException e) {
//            System.err.println("Error getting daily income data: " + e.getMessage());
//        }
//
//        return dailyData;
//    }
//
//    /**
//     * Generate a tenant payment history report
//     *
//     * @param tenantId ID of the tenant
//     * @param startDate Start date for the report
//     * @param endDate End date for the report
//     * @return Map containing tenant payment history statistics
//     */
//    public Map<String, Object> generateTenantPaymentHistoryReport(int tenantId, LocalDate startDate, LocalDate endDate) {
//        Map<String, Object> report = new HashMap<>();
//
//        // Get tenant information
//        String tenantSql = "SELECT t.*, u.name, u.email, u.phone " +
//                "FROM Tenants t " +
//                "JOIN Users u ON t.user_id = u.user_id " +
//                "WHERE t.tenant_id = ?";
//
//        try (PreparedStatement stmt = connection.prepareStatement(tenantSql)) {
//            stmt.setInt(1, tenantId);
//
//            try (ResultSet rs = stmt.executeQuery()) {
//                if (rs.next()) {
//                    Map<String, Object> tenantInfo = new HashMap<>();
//                    tenantInfo.put("tenantId", rs.getInt("tenant_id"));
//                    tenantInfo.put("name", rs.getString("name"));
//                    tenantInfo.put("email", rs.getString("email"));
//                    tenantInfo.put("phone", rs.getString("phone"));
//                    tenantInfo.put("currentBalance", rs.getDouble("balance_due"));
//                    report.put("tenantInfo", tenantInfo);
//                }
//            }
//
//            // Get payment summary
//            String paymentSql = "SELECT COUNT(*) as payment_count, " +
//                    "SUM(payment_amount) as total_paid, " +
//                    "AVG(payment_amount) as average_payment, " +
//                    "MAX(payment_amount) as largest_payment, " +
//                    "MIN(payment_date) as first_payment, " +
//                    "MAX(payment_date) as last_payment " +
//                    "FROM BillRecords " +
//                    "WHERE tenant_id = ? AND DATE(payment_date) BETWEEN ? AND ?";
//
//            try (PreparedStatement paymentStmt = connection.prepareStatement(paymentSql)) {
//                paymentStmt.setInt(1, tenantId);
//                paymentStmt.setDate(2, Date.valueOf(startDate));
//                paymentStmt.setDate(3, Date.valueOf(endDate));
//
//                try (ResultSet rs = paymentStmt.executeQuery()) {
//                    if (rs.next()) {
//                        Map<String, Object> paymentSummary = new HashMap<>();
//                        paymentSummary.put("paymentCount", rs.getInt("payment_count"));
//                        paymentSummary.put("totalPaid", rs.getDouble("total_paid"));
//                        paymentSummary.put("averagePayment", rs.getDouble("average_payment"));
//                        paymentSummary.put("largestPayment", rs.getDouble("largest_payment"));
//                        paymentSummary.put("firstPayment", rs.getTimestamp("first_payment"));
//                        paymentSummary.put("lastPayment", rs.getTimestamp("last_payment"));
//                        report.put("paymentSummary", paymentSummary);
//                    }
//                }
//            }
//
//            // Get payment method breakdown
//            String methodSql = "SELECT payment_method, COUNT(*) as count, SUM(payment_amount) as total " +
//                    "FROM BillRecords " +
//                    "WHERE tenant_id = ? AND DATE(payment_date) BETWEEN ? AND ? " +
//                    "GROUP BY payment_method " +
//                    "ORDER BY total DESC";
//
//            List<Map<String, Object>> methodBreakdown = new ArrayList<>();
//            try (PreparedStatement methodStmt = connection.prepareStatement(methodSql)) {
//                methodStmt.setInt(1, tenantId);
//                methodStmt.setDate(2, Date.valueOf(startDate));
//                methodStmt.setDate(3, Date.valueOf(endDate));
//
//                try (ResultSet rs = methodStmt.executeQuery()) {
//                    while (rs.next()) {
//                        Map<String, Object> methodData = new HashMap<>();
//                        methodData.put("method", rs.getString("payment_method"));
//                        methodData.put("count", rs.getInt("count"));
//                        methodData.put("total", rs.getDouble("total"));
//                        methodBreakdown.add(methodData);
//                    }
//                }
//            }
//            report.put("paymentMethodBreakdown", methodBreakdown);
//
//            // Get payment history
//            report.put("paymentHistory", getFormattedPaymentHistoryForTenant(tenantId, startDate, endDate));
//
//        } catch (SQLException e) {
//            System.err.println("Error generating tenant payment history report: " + e.getMessage());
//        }
//
//        return report;
//    }
//
//    /**
//     * Get formatted payment history for a tenant
//     *
//     * @param tenantId ID of the tenant
//     * @param startDate Start date for the history
//     * @param endDate End date for the history
//     * @return List of maps containing payment history details
//     */
//    private List<Map<String, Object>> getFormattedPaymentHistoryForTenant(int tenantId, LocalDate startDate, LocalDate endDate) {
//        List<Map<String, Object>> paymentHistory = new ArrayList<>();
//
//        String sql = "SELECT br.*, " +
//                "b.amount as bill_amount, " +
//                "b.due_date, b.issue_date, b.description, " +
//                "r.room_number, f.floor_number, bg.building_name, " +
//                "u.name as landlord_name " +
//                "FROM BillRecords br " +
//                "JOIN Bills b ON br.bill_id = b.bill_id " +
//                "JOIN Rooms r ON b.room_id = r.room_id " +
//                "JOIN Floors f ON r.floor_id = f.floor_id " +
//                "JOIN Buildings bg ON f.building_id = bg.building_id " +
//                "LEFT JOIN Landlords l ON br.landlord_id = l.landlord_id " +
//                "LEFT JOIN Users u ON l.user_id = u.user_id " +
//                "WHERE br.tenant_id = ? AND DATE(br.payment_date) BETWEEN ? AND ? " +
//                "ORDER BY br.payment_date DESC";
//
//        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
//            stmt.setInt(1, tenantId);
//            stmt.setDate(2, Date.valueOf(startDate));
//            stmt.setDate(3, Date.valueOf(endDate));
//
//            try (ResultSet rs = stmt.executeQuery()) {
//                while (rs.next()) {
//                    Map<String, Object> payment = new HashMap<>();
//                    payment.put("recordId", rs.getInt("record_id"));
//                    payment.put("billId", rs.getInt("bill_id"));
//                    payment.put("paymentDate", rs.getTimestamp("payment_date"));
//                    payment.put("amount", rs.getDouble("payment_amount"));
//                    payment.put("method", rs.getString("payment_method"));
//                    payment.put("reference", rs.getString("reference_number"));
//                    payment.put("notes", rs.getString("notes"));
//
//                    payment.put("billAmount", rs.getDouble("bill_amount"));
//                    payment.put("dueDate", rs.getDate("due_date"));
//                    payment.put("issueDate", rs.getDate("issue_date"));
//                    payment.put("description", rs.getString("description"));
//
//                    payment.put("roomNumber", rs.getString("room_number"));
//                    payment.put("floorNumber", rs.getString("floor_number"));
//                    payment.put("buildingName", rs.getString("building_name"));
//                    payment.put("landlordName", rs.getString("landlord_name"));
//
//                    paymentHistory.add(payment);
//                }
//            }
//        } catch (SQLException e) {
//            System.err.println("Error getting tenant payment history: " + e.getMessage());
//        }
//
//        return paymentHistory;
//    }
//
//    /**
//     * Generate a payment receipt
//     *
//     * @param recordId ID of the payment record
//     * @return Map containing receipt information or null if record not found
//     */
//    public Map<String, Object> generatePaymentReceipt(int recordId) {
//        Map<String, Object> receipt = new HashMap<>();
//
//        String sql = "SELECT br.*, " +
//                "b.amount as bill_amount, " +
//                "b.due_date, b.issue_date, b.description, " +
//                "r.room_number, f.floor_number, bg.building_name, " +
//                "u.name as landlord_name, u.email as landlord_email, " +
//                "u2.name as tenant_name, u2.email as tenant_email " +
//                "FROM BillRecords br " +
//                "JOIN Bills b ON br.bill_id = b.bill_id " +
//                "JOIN Rooms r ON b.room_id = r.room_id " +
//                "JOIN Floors f ON r.floor_id = f.floor_id " +
//                "JOIN Buildings bg ON f.building_id = bg.building_id " +
//                "LEFT JOIN Landlords l ON br.landlord_id = l.landlord_id " +
//                "LEFT JOIN Users u ON l.user_id = u.user_id " +
//                "LEFT JOIN Tenants t ON br.tenant_id = t.tenant_id " +
//                "LEFT JOIN Users u2 ON t.user_id = u2.user_id " +
//                "WHERE br.record_id = ?";
//
//        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
//            stmt.setInt(1, recordId);
//
//            try (ResultSet rs = stmt.executeQuery()) {
//                if (rs.next()) {
//                    // Payment details
//                    receipt.put("receiptNumber", "R-" + rs.getInt("record_id"));
//                    receipt.put("paymentDate", rs.getTimestamp("payment_date"));
//                    receipt.put("paymentAmount", rs.getDouble("payment_amount"));
//                    receipt.put("paymentMethod", rs.getString("payment_method"));
//                    receipt.put("referenceNumber", rs.getString("reference_number"));
//
//                    // Bill details
//                    receipt.put("billId", rs.getInt("bill_id"));
//                    receipt.put("billAmount", rs.getDouble("bill_amount"));
//                    receipt.put("dueDate", rs.getDate("due_date"));
//                    receipt.put("issueDate", rs.getDate("issue_date"));
//                    receipt.put("description", rs.getString("description"));
//
//                    // Property details
//                    receipt.put("roomNumber", rs.getString("room_number"));
//                    receipt.put("floorNumber", rs.getString("floor_number"));
//                    receipt.put("buildingName", rs.getString("building_name"));
//
//                    // Tenant details
//                    receipt.put("tenantId", rs.getInt("tenant_id"));
//                    receipt.put("tenantName", rs.getString("tenant_name"));
//                    receipt.put("tenantEmail", rs.getString("tenant_email"));
//
//                    // Landlord details
//                    receipt.put("landlordId", rs.getInt("landlord_id"));
//                    receipt.put("landlordName", rs.getString("landlord_name"));
//                    receipt.put("landlordEmail", rs.getString("landlord_email"));
//
//                    // Notes
//                    receipt.put("notes", rs.getString("notes"));
//
//                    // Generate receipt timestamp
//                    receipt.put("generatedAt", LocalDateTime.now());
//
//                    // Format a receipt number with timestamp for uniqueness
//                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
//                    String timestamp = LocalDateTime.now().format(formatter);
//                    receipt.put("uniqueReceiptId", "RCPT-" + rs.getInt("record_id") + "-" + timestamp);
//                } else {
//                    return null; // Record not found
//                }
//            }
//        } catch (SQLException e) {
//            System.err.println("Error generating payment receipt: " + e.getMessage());
//            return null;
//        }
//
//        return receipt;
//    }
//
//    /**
//     * Generate a report of late or missed payments
//     *
//     * @param days Number of days overdue to consider
//     * @return List of maps containing late payment information
//     */
//    public List<Map<String, Object>> generateLatePaymentsReport(int days) {
//        List<Map<String, Object>> latePayments = new ArrayList<>();
//
//        // Get current date
//        LocalDate currentDate = LocalDate.now();
//        // Calculate the cutoff date for late payments
//        LocalDate cutoffDate = currentDate.minusDays(days);
//
//        String sql = "SELECT b.bill_id, b.amount, b.due_date, " +
//                "DATEDIFF(CURRENT_DATE, b.due_date) as days_overdue, " +
//                "t.tenant_id, u.name as tenant_name, u.email as tenant_email, u.phone as tenant_phone, " +
//                "r.room_number, f.floor_number, bg.building_name " +
//                "FROM Bills b " +
//                "JOIN Tenants t ON b.tenant_id = t.tenant_id " +
//                "JOIN Users u ON t.user_id = u.user_id " +
//                "JOIN Rooms r ON b.room_id = r.room_id " +
//                "JOIN Floors f ON r.floor_id = f.floor_id " +
//                "JOIN Buildings bg ON f.building_id = bg.building_id " +
//                "WHERE b.is_paid = FALSE AND b.due_date <= ? " +
//                "ORDER BY days_overdue DESC";
//
//        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
//            stmt.setDate(1, Date.valueOf(cutoffDate));
//
//            try (ResultSet rs = stmt.executeQuery()) {
//                while (rs.next()) {
//                    Map<String, Object> latePayment = new HashMap<>();
//
//                    // Bill details
//                    latePayment.put("billId", rs.getInt("bill_id"));
//                    latePayment.put("amount", rs.getDouble("amount"));
//                    latePayment.put("dueDate", rs.getDate("due_date"));
//                    latePayment.put("daysOverdue", rs.getInt("days_overdue"));
//
//                    // Tenant details
//                    latePayment.put("tenantId", rs.getInt("tenant_id"));
//                    latePayment.put("tenantName", rs.getString("tenant_name"));
//                    latePayment.put("tenantEmail", rs.getString("tenant_email"));
//                    latePayment.put("tenantPhone", rs.getString("tenant_phone"));
//
//                    // Property details
//                    latePayment.put("roomNumber", rs.getString("room_number"));
//                    latePayment.put("floorNumber", rs.getString("floor_number"));
//                    latePayment.put("buildingName", rs.getString("building_name"));
//
//                    latePayments.add(latePayment);
//                }
//            }
//        } catch (SQLException e) {
//            System.err.println("Error generating late payments report: " + e.getMessage());
//        }
//
//        return latePayments;
//    }
//
//    /**
//     * Search payment records by various criteria
//     *
//     * @param criteria Map of search criteria (key: field name, value: search value)
//     * @return ResultSet containing matched payment records
//     */
//    public ResultSet searchPaymentRecords(Map<String, Object> criteria) {
//        StringBuilder sqlBuilder = new StringBuilder();
//        List<Object> params = new ArrayList<>();
//
//        sqlBuilder.append("SELECT br.*, " +
//                "r.room_number, f.floor_number, bg.building_name, " +
//                "u.name as landlord_name, u2.name as tenant_name ");
//        sqlBuilder.append("FROM BillRecords br ");
//        sqlBuilder.append("JOIN Bills b ON br.bill_id = b.bill_id ");
//        sqlBuilder.append("JOIN Rooms r ON b.room_id = r.room_id ");
//        sqlBuilder.append("JOIN Floors f ON r.floor_id = f.floor_id ");
//        sqlBuilder.append("JOIN Buildings bg ON f.building_id = bg.building_id ");
//        sqlBuilder.append("LEFT JOIN Landlords l ON br.landlord_id = l.landlord_id ");
//        sqlBuilder.append("LEFT JOIN Users u ON l.user_id = u.user_id ");
//        sqlBuilder.append("LEFT JOIN Tenants t ON br.tenant_id = t.tenant_id ");
//        sqlBuilder.append("LEFT JOIN Users u2 ON t.user_id = u2.user_id ");
//
//        // Start with WHERE clause if there are criteria
//        if (!criteria.isEmpty()) {
//            sqlBuilder.append("WHERE 1=1 ");
//
//            // Add specific criteria
//            if (criteria.containsKey("tenantId")) {
//                sqlBuilder.append("AND br.tenant_id = ? ");
//                params.add(criteria.get("tenantId"));
//            }
//
//            if (criteria.containsKey("landlordId")) {
//                sqlBuilder.append("AND br.landlord_id = ? ");
//                params.add(criteria.get("landlordId"));
//            }
//
//            if (criteria.containsKey("billId")) {
//                sqlBuilder.append("AND br.bill_id = ? ");
//                params.add(criteria.get("billId"));
//            }
//
//            if (criteria.containsKey("paymentMethod")) {
//                sqlBuilder.append("AND br.payment_method = ? ");
//                params.add(criteria.get("paymentMethod"));
//            }
//
//            if (criteria.containsKey("startDate") && criteria.containsKey("endDate")) {
//                sqlBuilder.append("AND DATE(br.payment_date) BETWEEN ? AND ? ");
//                params.add(criteria.get("startDate"));
//                params.add(criteria.get("endDate"));
//            } else if (criteria.containsKey("startDate")) {
//                sqlBuilder.append("AND DATE(br.payment_date) >= ? ");
//                params.add(criteria.get("startDate"));
//            } else if (criteria.containsKey("endDate")) {
//                sqlBuilder.append("AND DATE(br.payment_date) <= ? ");
//                params.add(criteria.get("endDate"));
//            }
//
//            if (criteria.containsKey("referenceNumber")) {
//                sqlBuilder.append("AND br.reference_number LIKE ? ");
//                params.add("%" + criteria.get("referenceNumber") + "%");
//            }
//
//            if (criteria.containsKey("minAmount")) {
//                sqlBuilder.append("AND br.payment_amount >= ? ");
//                params.add(criteria.get("minAmount"));
//            }
//
//            if (criteria.containsKey("maxAmount")) {
//                sqlBuilder.append("AND br.payment_amount <= ? ");
//                params.add(criteria.get("maxAmount"));
//            }
//
//            if (criteria.containsKey("buildingId")) {
//                sqlBuilder.append("AND bg.building_id = ? ");
//                params.add(criteria.get("buildingId"));
//            }
//
//            if (criteria.containsKey("tenantName")) {
//                sqlBuilder.append("AND u2.name LIKE ? ");
//                params.add("%" + criteria.get("tenantName") + "%");
//            }
//        }
//
//        // Add default ordering
//        sqlBuilder.append("ORDER BY br.payment_date DESC");
//
//        try {
//            PreparedStatement stmt = connection.prepareStatement(sqlBuilder.toString());
//
//            // Set parameters
//            for (int i = 0; i < params.size(); i++) {
//                Object param = params.get(i);
//                if (param instanceof Integer) {
//                    stmt.setInt(i + 1, (Integer) param);
//                } else if (param instanceof Double) {
//                    stmt.setDouble(i + 1, (Double) param);
//                } else if (param instanceof LocalDate) {
//                    stmt.setDate(i + 1, Date.valueOf((LocalDate) param));
//                } else {
//                    stmt.setString(i + 1, param.toString());
//                }
//            }
//
//            return stmt.executeQuery();
//        } catch (SQLException e) {
//            System.err.println("Error searching payment records: " + e.getMessage());
//            return null;
//        }
//    }
//
//    /**
//     * Check if a bill has any payment records
//     *
//     * @param billId ID of the bill to check
//     * @return true if the bill has payment records, false otherwise
//     */
//    public boolean hasBillPaymentRecords(int billId) {
//        String sql = "SELECT COUNT(*) as count FROM BillRecords WHERE bill_id = ?";
//
//        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
//            stmt.setInt(1, billId);
//
//            try (ResultSet rs = stmt.executeQuery()) {
//                if (rs.next()) {
//                    return rs.getInt("count") > 0;
//                }
//            }
//        } catch (SQLException e) {
//            System.err.println("Error checking bill payment records: " + e.getMessage());
//        }
//
//        return false;
//    }
//
//    /**
//     * Get total payments received in a date range
//     *
//     * @param startDate Start date of the range
//     * @param endDate End date of the range
//     * @return Total payment amount received
//     */
//    public double getTotalPaymentsInDateRange(LocalDate startDate, LocalDate endDate) {
//        String sql = "SELECT SUM(payment_amount) as total FROM BillRecords " +
//                "WHERE DATE(payment_date) BETWEEN ? AND ?";
//
//        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
//            stmt.setDate(1, Date.valueOf(startDate));
//            stmt.setDate(2, Date.valueOf(endDate));
//
//            try (ResultSet rs = stmt.executeQuery()) {
//                if (rs.next()) {
//                    return rs.getDouble("total");
//                }
//            }
//        } catch (SQLException e) {
//            System.err.println("Error getting total payments: " + e.getMessage());
//        }
//
//        return 0.0;
//    }
//
//    /**
//     * Get count of payment records for a specific tenant
//     *
//     * @param tenantId ID of the tenant
//     * @return Count of payment records
//     */
//    public int getPaymentCountForTenant(int tenantId) {
//        String sql = "SELECT COUNT(*) as count FROM BillRecords WHERE tenant_id = ?";
//
//        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
//            stmt.setInt(1, tenantId);
//
//            try (ResultSet rs = stmt.executeQuery()) {
//                if (rs.next()) {
//                    return rs.getInt("count");
//                }
//            }
//        } catch (SQLException e) {
//            System.err.println("Error getting tenant payment count: " + e.getMessage());
//        }
//
//        return 0;
//    }
//
//    /**
//     * Get the most recent payment date for a tenant
//     *
//     * @param tenantId ID of the tenant
//     * @return LocalDateTime representing the most recent payment date, or null if no payments
//     */
//    public LocalDateTime getLastPaymentDateForTenant(int tenantId) {
//        String sql = "SELECT MAX(payment_date) as last_payment FROM BillRecords WHERE tenant_id = ?";
//
//        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
//            stmt.setInt(1, tenantId);
//
//            try (ResultSet rs = stmt.executeQuery()) {
//                if (rs.next() && rs.getTimestamp("last_payment") != null) {
//                    return rs.getTimestamp("last_payment").toLocalDateTime();
//                }
//            }
//        } catch (SQLException e) {
//            System.err.println("Error getting last payment date: " + e.getMessage());
//        }
//
//        return null;
//    }
//
//    /**
//     * Generate a payment summary for a specific period
//     *
//     * @param period Either "daily", "weekly", "monthly", or "yearly"
//     * @param numberOfPeriods Number of periods to look back
//     * @return List of maps containing period and payment summary
//     */
//    public List<Map<String, Object>> generatePaymentTrendReport(String period, int numberOfPeriods) {
//        List<Map<String, Object>> trends = new ArrayList<>();
//
//        // Build the SQL based on the period
//        StringBuilder sqlBuilder = new StringBuilder();
//        sqlBuilder.append("SELECT ");
//
//        switch (period.toLowerCase()) {
//            case "daily":
//                sqlBuilder.append("DATE(payment_date) as period_date, ");
//                break;
//            case "weekly":
//                sqlBuilder.append("YEARWEEK(payment_date, 1) as period_date, ");
//                sqlBuilder.append("CONCAT(YEAR(payment_date), '-W', WEEK(payment_date, 1)) as period_label, ");
//                break;
//            case "monthly":
//                sqlBuilder.append("DATE_FORMAT(payment_date, '%Y-%m') as period_date, ");
//                break;
//            case "yearly":
//                sqlBuilder.append("YEAR(payment_date) as period_date, ");
//                break;
//            default:
//                sqlBuilder.append("DATE(payment_date) as period_date, "); // Default to daily
//        }
//
//        sqlBuilder.append("COUNT(*) as payment_count, ");
//        sqlBuilder.append("SUM(payment_amount) as total_amount, ");
//        sqlBuilder.append("AVG(payment_amount) as average_amount, ");
//        sqlBuilder.append("MAX(payment_amount) as max_amount, ");
//        sqlBuilder.append("MIN(payment_amount) as min_amount ");
//        sqlBuilder.append("FROM BillRecords ");
//
//        // Add appropriate date filter based on period
//        switch (period.toLowerCase()) {
//            case "daily":
//                sqlBuilder.append("WHERE payment_date >= DATE_SUB(CURRENT_DATE, INTERVAL ? DAY) ");
//                break;
//            case "weekly":
//                sqlBuilder.append("WHERE payment_date >= DATE_SUB(CURRENT_DATE, INTERVAL ? WEEK) ");
//                break;
//            case "monthly":
//                sqlBuilder.append("WHERE payment_date >= DATE_SUB(CURRENT_DATE, INTERVAL ? MONTH) ");
//                break;
//            case "yearly":
//                sqlBuilder.append("WHERE payment_date >= DATE_SUB(CURRENT_DATE, INTERVAL ? YEAR) ");
//                break;
//            default:
//                sqlBuilder.append("WHERE payment_date >= DATE_SUB(CURRENT_DATE, INTERVAL ? DAY) ");
//        }
//
//        sqlBuilder.append("GROUP BY period_date ");
//        sqlBuilder.append("ORDER BY period_date");
//
//        try (PreparedStatement stmt = connection.prepareStatement(sqlBuilder.toString())) {
//            stmt.setInt(1, numberOfPeriods);
//
//            try (ResultSet rs = stmt.executeQuery()) {
//                while (rs.next()) {
//                    Map<String, Object> periodData = new HashMap<>();
//
//                    String periodKey = "period";
//                    if (period.equalsIgnoreCase("weekly") && rs.getString("period_label") != null) {
//                        periodData.put(periodKey, rs.getString("period_label"));
//                    } else {
//                        periodData.put(periodKey, rs.getString("period_date"));
//                    }
//
//                    periodData.put("count", rs.getInt("payment_count"));
//                    periodData.put("total", rs.getDouble("total_amount"));
//                    periodData.put("average", rs.getDouble("average_amount"));
//                    periodData.put("maximum", rs.getDouble("max_amount"));
//                    periodData.put("minimum", rs.getDouble("min_amount"));
//
//                    trends.add(periodData);
//                }
//            }
//        } catch (SQLException e) {
//            System.err.println("Error generating payment trend report: " + e.getMessage());
//        }
//
//        return trends;
//    }
//
//    /**
//     * Get payment records with a specific reference number
//     *
//     * @param referenceNumber Reference number to search for
//     * @return ResultSet containing matching payment records
//     */
//    public ResultSet getPaymentRecordsByReferenceNumber(String referenceNumber) {
//        String sql = "SELECT br.*, " +
//                "r.room_number, f.floor_number, bg.building_name, " +
//                "u.name as landlord_name, u2.name as tenant_name " +
//                "FROM BillRecords br " +
//                "JOIN Bills b ON br.bill_id = b.bill_id " +
//                "JOIN Rooms r ON b.room_id = r.room_id " +
//                "JOIN Floors f ON r.floor_id = f.floor_id " +
//                "JOIN Buildings bg ON f.building_id = bg.building_id " +
//                "LEFT JOIN Landlords l ON br.landlord_id = l.landlord_id " +
//                "LEFT JOIN Users u ON l.user_id = u.user_id " +
//                "LEFT JOIN Tenants t ON br.tenant_id = t.tenant_id " +
//                "LEFT JOIN Users u2 ON t.user_id = u2.user_id " +
//                "WHERE br.reference_number = ? " +
//                "ORDER BY br.payment_date DESC";
//
//        try {
//            PreparedStatement stmt = connection.prepareStatement(sql);
//            stmt.setString(1, referenceNumber);
//            return stmt.executeQuery();
//        } catch (SQLException e) {
//            System.err.println("Error getting payment records by reference number: " + e.getMessage());
//            return null;
//        }
//    }
//
}
