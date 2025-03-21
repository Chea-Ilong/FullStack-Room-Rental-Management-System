package GUI;

import Payment.Bill;
import Payment.BillRecord;
import Users.Landlord;
import Users.Tenant;
import DataBase.BillDML;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

public class TenantGUI extends JFrame {
    private Tenant tenant;
    private Landlord landlord;
    private JPanel mainPanel;
    private BillRecord billRecord;

    public TenantGUI(Tenant tenant, Landlord landlord) {
        this.tenant = tenant;
        this.landlord = landlord;
        this.billRecord = new BillRecord(); // Initialize the BillRecord

        // Setup frame
        setTitle("Tenant Dashboard - " + tenant.getName());
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Create main panel
        mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Welcome header
        JLabel titleLabel = new JLabel("Welcome, " + tenant.getName() + "!");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        mainPanel.add(titleLabel, BorderLayout.NORTH);

        // Menu options panel using GridBagLayout like LoginGUI
        JPanel optionsPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        // Setup all the menu options
        setupMenuOptions(optionsPanel, gbc);

        // Add options panel to main panel with some spacing
        JPanel wrapperPanel = new JPanel(new GridBagLayout());
        wrapperPanel.add(optionsPanel);
        mainPanel.add(wrapperPanel, BorderLayout.CENTER);

        // Add main panel to frame
        add(mainPanel);
    }

    private void setupMenuOptions(JPanel optionsPanel, GridBagConstraints gbc) {
        // Pay Bill option (commented out)
        // JButton payBillButton = new JButton("Pay Bill");
        // payBillButton.setFont(new Font("Arial", Font.PLAIN, 14));
        // gbc.gridx = 0;
        // gbc.gridy = 0;
        // gbc.gridwidth = 2;
        // optionsPanel.add(payBillButton, gbc);

        // View History option
        JButton viewHistoryButton = new JButton("View Payment ");
        viewHistoryButton.setFont(new Font("Arial", Font.PLAIN, 14));
        gbc.gridx = 0;
        gbc.gridy = 0;  // Moved up to first position since Pay Bill is commented out
        gbc.gridwidth = 2;
        optionsPanel.add(viewHistoryButton, gbc);

        // Logout option
        JButton logoutButton = new JButton("Logout");
        logoutButton.setFont(new Font("Arial", Font.PLAIN, 14));
        gbc.gridx = 0;
        gbc.gridy = 1;  // Adjusted position
        gbc.gridwidth = 2;
        optionsPanel.add(logoutButton, gbc);

        // Add action listeners
        // payBillButton.addActionListener(e -> handleBillPayment());  // Commented out
        viewHistoryButton.addActionListener(e -> showBillHistory());
        logoutButton.addActionListener(e -> handleLogout());
    }

    private void handleBillPayment() {
        try {
            // Create BillDML instance to access bill data
            BillDML billDML = new BillDML();

            // Check for current bill
            Map<String, Object> currentBill = billDML.getCurrentBill(tenant.getIdCard());

            if (currentBill != null && !currentBill.isEmpty()) {
                // There is a current bill to pay
                SwingUtilities.invokeLater(() -> {
                    BillPaymentGUI billPaymentGUI = new BillPaymentGUI(tenant, landlord);
                    billPaymentGUI.setVisible(true);
                });
            } else {
                // No current bill
                JOptionPane.showMessageDialog(this,
                        "You have no outstanding bills to pay at this time.",
                        "No Bills Due",
                        JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Error accessing bill information: " + e.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void showBillHistory() {
        try {
            // Fetch bill history using BillRecord class
            List<Bill> billHistory = billRecord.getBillHistoryForTenant(tenant.getIdCard());

            if (billHistory == null || billHistory.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "No payment history found.",
                        "No History",
                        JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            // Create dialog to display bill history
            JDialog historyDialog = new JDialog(this, "Bill Payment History", true);
            historyDialog.setSize(700, 500);
            historyDialog.setLocationRelativeTo(this);

            JPanel historyPanel = new JPanel(new BorderLayout(10, 10));
            historyPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

            // Add title
            JLabel titleLabel = new JLabel("Your Bill Payment History");
            titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
            titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
            historyPanel.add(titleLabel, BorderLayout.NORTH);

            // Create table columns
            String[] columns = {"Bill ID", "Date", "Building", "Floor", "Room", "Rent (KHR)", "Electric (KHR)", "Water (KHR)", "Total (KHR)", "Status"};

            // Create table model
            DefaultTableModel tableModel = new DefaultTableModel(columns, 0) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false; // Make all cells non-editable
                }
            };

            // Add data from the retrieved bill history
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM yyyy");
            for (Bill bill : billHistory) {
                String billDate = bill.getBillDate().format(formatter);
                String status = bill.isPaid() ? "Paid" : "Unpaid";
                String roomNumber = (bill.getRoom() != null) ? bill.getRoom().getRoomNumber() : "N/A";

                Object[] row = {
                        bill.getBillID(),
                        billDate,
                        bill.getBuildingName(),
                        bill.getFloorNumber(),
                        roomNumber,
                        String.format("%.0f", bill.getRentAmount()),
                        String.format("%.0f", bill.getElectricAmount()),
                        String.format("%.0f", bill.getWaterAmount()),
                        String.format("%.0f", bill.getTotalAmount()),
                        status
                };
                tableModel.addRow(row);
            }

            // Create JTable with the model
            JTable historyTable = new JTable(tableModel);
            historyTable.setFillsViewportHeight(true);
            historyTable.setRowHeight(25);
            historyTable.getTableHeader().setReorderingAllowed(false);

            // Set column widths
            historyTable.getColumnModel().getColumn(0).setPreferredWidth(60); // Bill ID
            historyTable.getColumnModel().getColumn(1).setPreferredWidth(100); // Date
            historyTable.getColumnModel().getColumn(2).setPreferredWidth(120); // Building
            historyTable.getColumnModel().getColumn(3).setPreferredWidth(60); // Floor
            historyTable.getColumnModel().getColumn(4).setPreferredWidth(60); // Room

            // Add scroll pane with the table
            JScrollPane scrollPane = new JScrollPane(historyTable);
            historyPanel.add(scrollPane, BorderLayout.CENTER);

            // Summary information
            JPanel summaryPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            int totalBills = billHistory.size();
            long paidBills = billHistory.stream().filter(Bill::isPaid).count();
            double totalPaid = billHistory.stream()
                    .filter(Bill::isPaid)
                    .mapToDouble(Bill::getTotalAmount)
                    .sum();

            JLabel summaryLabel = new JLabel(String.format(
                    "Total Bills: %d | Paid Bills: %d | Total Paid: %.0f KHR (%.2f USD)",
                    totalBills, paidBills, totalPaid, totalPaid / 4100.0));
            summaryPanel.add(summaryLabel);
            historyPanel.add(summaryPanel, BorderLayout.NORTH);

            // Close button
            JButton closeButton = new JButton("Close");
            closeButton.addActionListener(e -> historyDialog.dispose());

            JPanel buttonPanel = new JPanel();
            buttonPanel.add(closeButton);
            historyPanel.add(buttonPanel, BorderLayout.SOUTH);

            historyDialog.add(historyPanel);
            historyDialog.setVisible(true);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Error retrieving bill history: " + e.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void handleLogout() {
        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to logout?",
                "Confirm Logout",
                JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            dispose(); // Close this window
            SwingUtilities.invokeLater(() -> {
                LoginGUI loginGUI = new LoginGUI();
                loginGUI.setVisible(true);
            });
        }
    }
}