package GUI;

import DataBase.BillDML;
import DataBase.BillRecordDML;
import DataBase.DataBaseConnection;
import Payment.Bill;
import Users.Landlord;
import Users.Tenant;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

public class BillPaymentGUI extends JFrame {
    private Tenant tenant;
    private Landlord landlord;
    private List<Bill> unpaidBills;
    private JTable billTable;
    private DefaultTableModel tableModel;

    public BillPaymentGUI(Tenant tenant, Landlord landlord) {
        this.tenant = tenant;
        this.landlord = landlord;

        // Setup frame
        setTitle("Pay Bills - " + tenant.getName());
        setSize(700, 500);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        // Create main panel
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Header
        JLabel headerLabel = new JLabel("Your Unpaid Bills");
        headerLabel.setFont(new Font("Arial", Font.BOLD, 18));
        headerLabel.setHorizontalAlignment(SwingConstants.CENTER);
        mainPanel.add(headerLabel, BorderLayout.NORTH);

        // Load unpaid bills
        loadUnpaidBills();

        // Create table model with columns
        String[] columns = {"Bill ID", "Date", "Amount (KHR)", "Amount (USD)", "Details"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Make table non-editable
            }
        };

        // Fill table with data
        for (Bill bill : unpaidBills) {
            Object[] row = {
                    bill.getBillID(),
                    bill.getBillDate(),
                    String.format("%.0f", bill.getTotalAmount()),
                    String.format("%.2f", bill.getTotalAmount() / 4100.00),
                    "View Details"
            };
            tableModel.addRow(row);
        }

        // Create table and scroll pane
        billTable = new JTable(tableModel);
        billTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        billTable.getColumnModel().getColumn(4).setCellRenderer(new ButtonRenderer());
        billTable.addMouseListener(new ButtonClickListener(billTable) {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                int column = billTable.getColumnModel().getColumnIndexAtX(e.getX());
                int row = e.getY() / billTable.getRowHeight();

                if (row < billTable.getRowCount() && row >= 0 && column == 4) {
                    showBillDetails(row);
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(billTable);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        // Buttons panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        // JButton payButton = new JButton("Pay Selected Bill");  // Commented out
        JButton closeButton = new JButton("Close");

        // payButton.addActionListener(e -> paySelectedBill());  // Commented out
        closeButton.addActionListener(e -> dispose());

        // buttonPanel.add(payButton);  // Commented out
        buttonPanel.add(closeButton);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        // Status panel
        JPanel statusPanel = new JPanel(new BorderLayout());
        JLabel statusLabel = new JLabel("Select a bill and click 'Pay' to process payment");
        statusPanel.add(statusLabel, BorderLayout.WEST);
        mainPanel.add(statusPanel, BorderLayout.NORTH);

        // Add main panel to frame
        add(mainPanel);
    }

    private void loadUnpaidBills() {
        // Check if tenant has room assignment
        if (tenant.getAssignedRoom() == null) {
            unpaidBills = List.of(); // Empty list
            JOptionPane.showMessageDialog(this,
                    "You are not assigned to any room.",
                    "No Room Assignment",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Check if tenant already paid for current month
        LocalDate today = LocalDate.now();
        if (tenant.isBillPaid(today)) {
            unpaidBills = List.of(); // Empty list
            JOptionPane.showMessageDialog(this,
                    "You have already paid your bill for this month.",
                    "Bill Paid",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        // Find bills for this tenant
        try {
            List<Bill> tenantBills = landlord.getBillRecord().getBillHistoryForTenant(tenant.getIdCard());

            // Filter unpaid bills
            unpaidBills = tenantBills.stream()
                    .filter(bill -> !bill.isPaid())
                    .collect(Collectors.toList());

            if (unpaidBills.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "You have no pending bills to pay.",
                        "No Pending Bills",
                        JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Error loading bills: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            unpaidBills = List.of(); // Empty list
        }
    }

    private void showBillDetails(int row) {
        if (row >= 0 && row < unpaidBills.size()) {
            Bill selectedBill = unpaidBills.get(row);

            // Create a dialog to show bill details
            JDialog detailsDialog = new JDialog(this, "Bill Details", true);
            detailsDialog.setSize(500, 400);
            detailsDialog.setLocationRelativeTo(this);

            JPanel panel = new JPanel(new BorderLayout(10, 10));
            panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

            // Create text area for details
            JTextArea detailsArea = new JTextArea(selectedBill.toString());
            detailsArea.setEditable(false);
            detailsArea.setFont(new Font("Monospaced", Font.PLAIN, 12));

            panel.add(new JScrollPane(detailsArea), BorderLayout.CENTER);

            // Button to close the dialog
            JButton closeButton = new JButton("Close");
            closeButton.addActionListener(e -> detailsDialog.dispose());

            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            buttonPanel.add(closeButton);
            panel.add(buttonPanel, BorderLayout.SOUTH);

            detailsDialog.add(panel);
            detailsDialog.setVisible(true);
        }
    }

    private void paySelectedBill() {
        int selectedRow = billTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                    "Please select a bill to pay",
                    "No Bill Selected",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        Bill selectedBill = unpaidBills.get(selectedRow);

        // Show confirmation dialog
        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to pay bill #" + selectedBill.getBillID() +
                        " dated " + selectedBill.getBillDate() +
                        " for " + String.format("%.0f KHR (%.2f USD)",
                        selectedBill.getTotalAmount(),
                        selectedBill.getTotalAmount() / 4100.00) + "?",
                "Confirm Payment",
                JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                // Mark bill as paid in memory
                selectedBill.markAsPaid(selectedBill.getTotalAmount());
                tenant.markBillAsPaid(selectedBill.getBillDate());

                // Get tenant and landlord database IDs
                int tenantDbId = 0;
                int landlordDbId = 0;

                try (Connection conn = DataBaseConnection.getConnection()) {
                    // Get tenant ID from database using IdCard
                    String tenantQuery = "SELECT tenant_id FROM Tenants t JOIN Users u ON t.user_id = u.user_id WHERE u.IdCard = ?";
                    try (PreparedStatement tenantStmt = conn.prepareStatement(tenantQuery)) {
                        tenantStmt.setString(1, tenant.getIdCard());
                        try (ResultSet tenantRs = tenantStmt.executeQuery()) {
                            if (tenantRs.next()) {
                                tenantDbId = tenantRs.getInt("tenant_id");
                            } else {
                                throw new SQLException("Tenant not found in database");
                            }
                        }
                    }

                    // Get landlord ID from database
                    String landlordQuery = "SELECT landlord_id FROM Landlords WHERE user_id = ?";
                    try (PreparedStatement landlordStmt = conn.prepareStatement(landlordQuery)) {
                        landlordStmt.setInt(1, 1); // Replace with actual landlord user ID or get dynamically
                        try (ResultSet landlordRs = landlordStmt.executeQuery()) {
                            if (landlordRs.next()) {
                                landlordDbId = landlordRs.getInt("landlord_id");
                            } else {
                                throw new SQLException("Landlord not found in database");
                            }
                        }
                    }

                    // Record payment and update bill status in database
                    BillRecordDML billRecordDML = new BillRecordDML();
                    BillDML billDML = new BillDML();

                    // Record payment in BillRecordDML
                    int recordId = billRecordDML.recordPayment(
                            selectedBill.getBillID(),
                            tenantDbId,
                            landlordDbId,
                            selectedBill.getTotalAmount()
                    );

                    if (recordId > 0) {
                        // Update bill status in Bills table
                        boolean updated = billDML.markBillAsPaid(selectedBill.getBillID());
                        if (updated) {
                            // Update in-memory BillRecord
                            landlord.getBillRecord().updateBill(selectedBill);

                            JOptionPane.showMessageDialog(this,
                                    "Payment successful! Thank you.",
                                    "Payment Completed",
                                    JOptionPane.INFORMATION_MESSAGE);

                            // Refresh the bills list
                            tableModel.removeRow(selectedRow);
                            unpaidBills.remove(selectedRow);

                            if (unpaidBills.isEmpty()) {
                                dispose(); // Close the window if no more bills
                            }
                        } else {
                            JOptionPane.showMessageDialog(this,
                                    "Failed to update bill status in database.",
                                    "Database Error",
                                    JOptionPane.ERROR_MESSAGE);
                        }
                    } else {
                        JOptionPane.showMessageDialog(this,
                                "Payment recording failed. Please try again later.",
                                "Payment Error",
                                JOptionPane.ERROR_MESSAGE);
                    }
                }
            } catch (SQLException | IllegalArgumentException | IllegalStateException e) {
                JOptionPane.showMessageDialog(this,
                        "Payment error: " + e.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            }
        }
    }

    // Custom button renderer for the "View Details" column
    class ButtonRenderer extends JButton implements javax.swing.table.TableCellRenderer {
        public ButtonRenderer() {
            setOpaque(true);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            setText((value == null) ? "" : value.toString());
            return this;
        }
    }

    // Custom mouse listener for handling button clicks
    abstract class ButtonClickListener extends java.awt.event.MouseAdapter {
        private JTable table;

        public ButtonClickListener(JTable table) {
            this.table = table;
        }
    }
}