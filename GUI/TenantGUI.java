//package GUI;
//
//import DataBase.BillDML;
//import DataBase.TenantDML;
//import Exceptions.RoomException;
//import Exceptions.TenantException;
//import Payment.Bill;
//import Users.Tenant;
//
//import javax.swing.*;
//import javax.swing.border.EmptyBorder;
//import javax.swing.table.DefaultTableModel;
//
//import java.awt.*;
//import java.awt.event.ActionEvent;
//import java.awt.event.ActionListener;
//import java.time.LocalDate;
//import java.time.YearMonth;
//import java.time.format.DateTimeFormatter;
//import java.util.List;
//
//public class TenantGUI extends JFrame {
//
//    private Tenant tenant;
//    private JPanel contentPane;
//    private JTable billsTable;
//    private DefaultTableModel billTableModel;
//    private JPanel profilePanel;
//    private JLabel lblRoomStatus;
//    private BillDML billDML = new BillDML();
//    private TenantDML tenantDML = new TenantDML();
//
//    /**
//     * Create the frame for Tenant GUI.
//     */
//    public TenantGUI(Tenant tenant) {
//        this.tenant = tenant;
//        initializeUI();
//        loadTenantData();
//    }
//
//    private void initializeUI() {
//        setTitle("Tenant Management Panel - " + tenant.getName());
//        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//        setBounds(100, 100, 800, 600);
//
//        contentPane = new JPanel();
//        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
//        contentPane.setLayout(new BorderLayout(0, 0));
//        setContentPane(contentPane);
//
//        // Create menu bar
//        JMenuBar menuBar = new JMenuBar();
//        setJMenuBar(menuBar);
//
//        JMenu mnProfile = new JMenu("Profile");
//        menuBar.add(mnProfile);
//
//        JMenuItem mntmViewProfile = new JMenuItem("View My Profile");
//        mntmViewProfile.addActionListener(new ActionListener() {
//            public void actionPerformed(ActionEvent e) {
//                showProfilePanel();
//            }
//        });
//        mnProfile.add(mntmViewProfile);
//
//        JMenu mnBills = new JMenu("Bills");
//        menuBar.add(mnBills);
//
//        JMenuItem mntmViewBills = new JMenuItem("View My Bills");
//        mntmViewBills.addActionListener(new ActionListener() {
//            public void actionPerformed(ActionEvent e) {
//                showBillsPanel();
//            }
//        });
//        mnBills.add(mntmViewBills);
//
//        JMenuItem mntmPayBill = new JMenuItem("Pay Bill");
//        mntmPayBill.addActionListener(new ActionListener() {
//            public void actionPerformed(ActionEvent e) {
//                payBill();
//            }
//        });
//        mnBills.add(mntmPayBill);
//
//        JMenu mnRoom = new JMenu("Room");
//        menuBar.add(mnRoom);
//
//        JMenuItem mntmVacateRoom = new JMenuItem("Request to Vacate Room");
//        mntmVacateRoom.addActionListener(new ActionListener() {
//            public void actionPerformed(ActionEvent e) {
//                requestVacateRoom();
//            }
//        });
//        mnRoom.add(mntmVacateRoom);
//
//        JMenu mnHelp = new JMenu("Help");
//        menuBar.add(mnHelp);
//
//        JMenuItem mntmContactLandlord = new JMenuItem("Contact Landlord");
//        mntmContactLandlord.addActionListener(new ActionListener() {
//            public void actionPerformed(ActionEvent e) {
//                showContactLandlord();
//            }
//        });
//        mnHelp.add(mntmContactLandlord);
//
//        JMenuItem mntmLogout = new JMenuItem("Logout");
//        mntmLogout.addActionListener(new ActionListener() {
//            public void actionPerformed(ActionEvent e) {
//                logout();
//            }
//        });
//        menuBar.add(mntmLogout);
//
//        // Create welcome panel (initial panel)
//        JPanel welcomePanel = new JPanel();
//        welcomePanel.setLayout(new BorderLayout());
//
//        JLabel lblWelcome = new JLabel("Welcome, " + tenant.getName() + "!");
//        lblWelcome.setFont(new Font("Tahoma", Font.BOLD, 20));
//        lblWelcome.setHorizontalAlignment(SwingConstants.CENTER);
//        welcomePanel.add(lblWelcome, BorderLayout.NORTH);
//
//        JPanel quickInfoPanel = new JPanel();
//        quickInfoPanel.setLayout(new GridLayout(3, 1, 10, 10));
//
//        // Room status info
//        JPanel roomPanel = new JPanel();
//        roomPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
//        roomPanel.setBorder(BorderFactory.createTitledBorder("Room Information"));
//
//        lblRoomStatus = new JLabel("Loading room information...");
//        roomPanel.add(lblRoomStatus);
//        quickInfoPanel.add(roomPanel);
//
//        // Bill status info
//        JPanel billPanel = new JPanel();
//        billPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
//        billPanel.setBorder(BorderFactory.createTitledBorder("Recent Bill Information"));
//
//        JLabel lblRecentBill = new JLabel("Loading recent bill information...");
//        billPanel.add(lblRecentBill);
//        quickInfoPanel.add(billPanel);
//
//        welcomePanel.add(quickInfoPanel, BorderLayout.CENTER);
//
//        // Add welcome panel to content pane
//        contentPane.add(welcomePanel, BorderLayout.CENTER);
//
//        // Initialize other panels but don't show them yet
//        initProfilePanel();
//        initBillsPanel();
//    }
//
//    private void initProfilePanel() {
//        profilePanel = new JPanel();
//        profilePanel.setLayout(new BorderLayout());
//
//        JPanel infoPanel = new JPanel();
//        infoPanel.setLayout(new GridLayout(5, 2, 10, 10));
//        infoPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
//
//        infoPanel.add(new JLabel("Name:"));
//        infoPanel.add(new JLabel(tenant.getName()));
//
//        infoPanel.add(new JLabel("ID Card:"));
//        infoPanel.add(new JLabel(tenant.getIdCard()));
//
//        infoPanel.add(new JLabel("Contact:"));
//        infoPanel.add(new JLabel(tenant.getContact()));
//
//        infoPanel.add(new JLabel("Role:"));
//        infoPanel.add(new JLabel("Tenant"));
//
//        infoPanel.add(new JLabel("Room Assigned:"));
//        if (tenant.getAssignedRoom() != null) {
//            infoPanel.add(new JLabel(tenant.getAssignedRoom().getRoomNumber()));
//        } else {
//            infoPanel.add(new JLabel("Not assigned"));
//        }
//
//        profilePanel.add(infoPanel, BorderLayout.NORTH);
//
//        JButton btnBack = new JButton("Back to Dashboard");
//        btnBack.addActionListener(new ActionListener() {
//            public void actionPerformed(ActionEvent e) {
//                showWelcomePanel();
//            }
//        });
//
//        JPanel buttonPanel = new JPanel();
//        buttonPanel.add(btnBack);
//        profilePanel.add(buttonPanel, BorderLayout.SOUTH);
//    }
//
//    private void initBillsPanel() {
//        JPanel billsPanel = new JPanel();
//        billsPanel.setLayout(new BorderLayout());
//
//        // Create table model for bills
//        billTableModel = new DefaultTableModel(
//                new Object[][] {},
//                new String[] {"Bill ID", "Date", "Rent", "Electric", "Water", "Total", "Status"}
//        ) {
//            @Override
//            public boolean isCellEditable(int row, int column) {
//                return false;
//            }
//        };
//
//        billsTable = new JTable(billTableModel);
//        JScrollPane scrollPane = new JScrollPane(billsTable);
//        billsPanel.add(scrollPane, BorderLayout.CENTER);
//
//        JPanel buttonPanel = new JPanel();
//        JButton btnPayBill = new JButton("Pay Selected Bill");
//        btnPayBill.addActionListener(new ActionListener() {
//            public void actionPerformed(ActionEvent e) {
//                paySelectedBill();
//            }
//        });
//        buttonPanel.add(btnPayBill);
//
//        JButton btnBack = new JButton("Back to Dashboard");
//        btnBack.addActionListener(new ActionListener() {
//            public void actionPerformed(ActionEvent e) {
//                showWelcomePanel();
//            }
//        });
//        buttonPanel.add(btnBack);
//
//        billsPanel.add(buttonPanel, BorderLayout.SOUTH);
//    }
//
//    private void loadTenantData() {
//        // Update room status label
//        if (tenant.getAssignedRoom() != null) {
//            StringBuilder roomInfo = new StringBuilder();
//            roomInfo.append("Room Number: ").append(tenant.getAssignedRoom().getRoomNumber());
//
//            // Add building and floor information if available
//            if (tenant.getAssignedRoom().getFloor() != null) {
//                roomInfo.append(" | Floor: ").append(tenant.getAssignedRoom().getFloor().getFloorNumber());
//
//                if (tenant.getAssignedRoom().getFloor().getBuilding() != null) {
//                    roomInfo.append(" | Building: ").append(tenant.getAssignedRoom().getFloor().getBuilding().getBuildingName());
//                }
//            }
//
//            lblRoomStatus.setText(roomInfo.toString());
//        } else {
//            lblRoomStatus.setText("No room currently assigned");
//        }
//    }
//
//    private void showWelcomePanel() {
//        contentPane.removeAll();
//
//        JPanel welcomePanel = new JPanel();
//        welcomePanel.setLayout(new BorderLayout());
//
//        JLabel lblWelcome = new JLabel("Welcome, " + tenant.getName() + "!");
//        lblWelcome.setFont(new Font("Tahoma", Font.BOLD, 20));
//        lblWelcome.setHorizontalAlignment(SwingConstants.CENTER);
//        welcomePanel.add(lblWelcome, BorderLayout.NORTH);
//
//        JPanel quickInfoPanel = new JPanel();
//        quickInfoPanel.setLayout(new GridLayout(3, 1, 10, 10));
//
//        // Room status info
//        JPanel roomPanel = new JPanel();
//        roomPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
//        roomPanel.setBorder(BorderFactory.createTitledBorder("Room Information"));
//        roomPanel.add(lblRoomStatus);
//        quickInfoPanel.add(roomPanel);
//
//        // Bill status info
//        JPanel billPanel = new JPanel();
//        billPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
//        billPanel.setBorder(BorderFactory.createTitledBorder("Recent Bill Information"));
//
//        // Get latest bill info
//        List<Bill> bills = billDML.getBillsByTenantId(tenant.getIdCard());
//        JLabel lblRecentBill;
//        if (!bills.isEmpty()) {
//            // Sort bills by date, most recent first
//            bills.sort((b1, b2) -> b2.getBillDate().compareTo(b1.getBillDate()));
//            Bill recentBill = bills.get(0);
//
//            String status = recentBill.isPaid() ? "PAID" : "UNPAID";
//            String date = recentBill.getBillDate().format(DateTimeFormatter.ofPattern("MMMM yyyy"));
//
//            lblRecentBill = new JLabel("Latest bill (" + date + "): $" +
//                    String.format("%.2f", recentBill.getTotalAmount()) + " - " + status);
//        } else {
//            lblRecentBill = new JLabel("No bills found");
//        }
//
//        billPanel.add(lblRecentBill);
//        quickInfoPanel.add(billPanel);
//
//        welcomePanel.add(quickInfoPanel, BorderLayout.CENTER);
//
//        contentPane.add(welcomePanel, BorderLayout.CENTER);
//        contentPane.revalidate();
//        contentPane.repaint();
//    }
//
//    private void showProfilePanel() {
//        contentPane.removeAll();
//        contentPane.add(profilePanel, BorderLayout.CENTER);
//        contentPane.revalidate();
//        contentPane.repaint();
//    }
//
//    private void showBillsPanel() {
//        // Clear existing data
//        while (billTableModel.getRowCount() > 0) {
//            billTableModel.removeRow(0);
//        }
//
//        // Load bills from database
//        List<Bill> bills = billDML.getBillsByTenantId(tenant.getIdCard());
//        for (Bill bill : bills) {
//            billTableModel.addRow(new Object[] {
//                    bill.getBillId(),
//                    bill.getBillDate().format(DateTimeFormatter.ofPattern("MMMM yyyy")),
//                    String.format("$%.2f", bill.getRentAmount()),
//                    String.format("$%.2f", bill.getElectricityAmount()),
//                    String.format("$%.2f", bill.getWaterAmount()),
//                    String.format("$%.2f", bill.getTotalAmount()),
//                    bill.isPaid() ? "PAID" : "UNPAID"
//            });
//        }
//
//        JPanel billsPanel = new JPanel(new BorderLayout());
//        billsPanel.add(new JScrollPane(billsTable), BorderLayout.CENTER);
//
//        JPanel buttonPanel = new JPanel();
//        JButton btnPayBill = new JButton("Pay Selected Bill");
//        btnPayBill.addActionListener(new ActionListener() {
//            public void actionPerformed(ActionEvent e) {
//                paySelectedBill();
//            }
//        });
//        buttonPanel.add(btnPayBill);
//
//        JButton btnBack = new JButton("Back to Dashboard");
//        btnBack.addActionListener(new ActionListener() {
//            public void actionPerformed(ActionEvent e) {
//                showWelcomePanel();
//            }
//        });
//        buttonPanel.add(btnBack);
//
//        billsPanel.add(buttonPanel, BorderLayout.SOUTH);
//
//        contentPane.removeAll();
//        contentPane.add(billsPanel, BorderLayout.CENTER);
//        contentPane.revalidate();
//        contentPane.repaint();
//    }
//
//    private void paySelectedBill() {
//        int selectedRow = billsTable.getSelectedRow();
//        if (selectedRow == -1) {
//            JOptionPane.showMessageDialog(this, "Please select a bill to pay", "No Selection", JOptionPane.WARNING_MESSAGE);
//            return;
//        }
//
//        String status = (String) billTableModel.getValueAt(selectedRow, 6);
//        if (status.equals("PAID")) {
//            JOptionPane.showMessageDialog(this, "This bill has already been paid", "Already Paid", JOptionPane.INFORMATION_MESSAGE);
//            return;
//        }
//
//        int billId = (int) billTableModel.getValueAt(selectedRow, 0);
//        String amount = (String) billTableModel.getValueAt(selectedRow, 5);
//
//        int confirm = JOptionPane.showConfirmDialog(this,
//                "Are you sure you want to pay this bill?\nAmount: " + amount,
//                "Confirm Payment", JOptionPane.YES_NO_OPTION);
//
//        if (confirm == JOptionPane.YES_OPTION) {
//            // Process payment - In real implementation, this would connect to a payment gateway
//            // For now, we'll just mark it as paid
//            if (billDML.markBillAsPaid(billId)) {
//                JOptionPane.showMessageDialog(this, "Payment successful!", "Success", JOptionPane.INFORMATION_MESSAGE);
//
//                // Update the table
//                billTableModel.setValueAt("PAID", selectedRow, 6);
//
//                // If tenant object has local payment tracking, update that too
//                String date = (String) billTableModel.getValueAt(selectedRow, 1);
//                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM yyyy");
//                YearMonth yearMonth = YearMonth.parse(date, formatter);
//                tenant.markBillAsPaid(LocalDate.of(yearMonth.getYear(), yearMonth.getMonth(), 1));
//            } else {
//                JOptionPane.showMessageDialog(this, "Payment failed. Please try again later.", "Error", JOptionPane.ERROR_MESSAGE);
//            }
//        }
//    }
//
//    private void payBill() {
//        // Show dialog to select month and year
//        JPanel panel = new JPanel(new GridLayout(3, 2));
//
//        String[] months = {"January", "February", "March", "April", "May", "June",
//                "July", "August", "September", "October", "November", "December"};
//        JComboBox<String> monthComboBox = new JComboBox<>(months);
//
//        int currentYear = LocalDate.now().getYear();
//        String[] years = new String[3]; // Current year and 2 previous years
//        for (int i = 0; i < 3; i++) {
//            years[i] = String.valueOf(currentYear - i);
//        }
//        JComboBox<String> yearComboBox = new JComboBox<>(years);
//
//        panel.add(new JLabel("Select Month:"));
//        panel.add(monthComboBox);
//        panel.add(new JLabel("Select Year:"));
//        panel.add(yearComboBox);
//
//        int result = JOptionPane.showConfirmDialog(
//                this, panel, "Select Bill Period",
//                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
//
//        if (result == JOptionPane.OK_OPTION) {
//            int month = monthComboBox.getSelectedIndex() + 1; // Months are 1-based
//            int year = Integer.parseInt((String) yearComboBox.getSelectedItem());
//
//            // Check if there's a bill for this period
//            List<Bill> bills = billDML.getBillsByTenantId(tenant.getIdCard());
//            for (Bill bill : bills) {
//                if (bill.getBillDate().getYear() == year && bill.getBillDate().getMonthValue() == month) {
//                    if (bill.isPaid()) {
//                        JOptionPane.showMessageDialog(this,
//                                "The bill for " + months[month-1] + " " + year + " has already been paid.",
//                                "Already Paid", JOptionPane.INFORMATION_MESSAGE);
//                    } else {
//                        int confirm = JOptionPane.showConfirmDialog(this,
//                                "Bill for " + months[month-1] + " " + year + "\n" +
//                                        "Rent: $" + String.format("%.2f", bill.getRentAmount()) + "\n" +
//                                        "Electricity: $" + String.format("%.2f", bill.getElectricityAmount()) + "\n" +
//                                        "Water: $" + String.format("%.2f", bill.getWaterAmount()) + "\n" +
//                                        "Total: $" + String.format("%.2f", bill.getTotalAmount()) + "\n\n" +
//                                        "Do you want to pay this bill now?",
//                                "Confirm Payment", JOptionPane.YES_NO_OPTION);
//
//                        if (confirm == JOptionPane.YES_OPTION) {
//                            if (billDML.markBillAsPaid(bill.getBillId())) {
//                                JOptionPane.showMessageDialog(this, "Payment successful!", "Success", JOptionPane.INFORMATION_MESSAGE);
//
//                                // Update tenant's local bill payment status
//                                tenant.markBillAsPaid(bill.getBillDate());
//                            } else {
//                                JOptionPane.showMessageDialog(this, "Payment failed. Please try again later.", "Error", JOptionPane.ERROR_MESSAGE);
//                            }
//                        }
//                    }
//                    return;
//                }
//            }
//
//            JOptionPane.showMessageDialog(this,
//                    "No bill found for " + months[month-1] + " " + year,
//                    "No Bill", JOptionPane.INFORMATION_MESSAGE);
//        }
//    }
//
//    private void requestVacateRoom() {
//        if (tenant.getAssignedRoom() == null) {
//            JOptionPane.showMessageDialog(this, "You are not currently assigned to any room.",
//                    "No Room Assigned", JOptionPane.INFORMATION_MESSAGE);
//            return;
//        }
//
//        int confirm = JOptionPane.showConfirmDialog(this,
//                "Are you sure you want to request to vacate your current room?\n" +
//                        "Room: " + tenant.getAssignedRoom().getRoomNumber(),
//                "Confirm Vacate Request", JOptionPane.YES_NO_OPTION);
//
//        if (confirm == JOptionPane.YES_OPTION) {
//            try {
//                // First check if there are any unpaid bills
//                List<Bill> unpaidBills = billDML.getUnpaidBillsByTenantId(tenant.getIdCard());
//                if (!unpaidBills.isEmpty()) {
//                    int payBills = JOptionPane.showConfirmDialog(this,
//                            "You have " + unpaidBills.size() + " unpaid bills. You must pay all bills before vacating.\n" +
//                                    "Would you like to view your bills now?",
//                            "Unpaid Bills", JOptionPane.YES_NO_OPTION);
//
//                    if (payBills == JOptionPane.YES_OPTION) {
//                        showBillsPanel();
//                    }
//                    return;
//                }
//
//                // Proceed with vacating room
//                tenant.vacateRoom();
//                JOptionPane.showMessageDialog(this,
//                        "Your request to vacate has been submitted successfully.\n" +
//                                "Please contact the landlord for final inspection.",
//                        "Request Submitted", JOptionPane.INFORMATION_MESSAGE);
//
//                // Update room status label
//                lblRoomStatus.setText("No room currently assigned");
//
//            } catch (TenantException | RoomException e) {
//                JOptionPane.showMessageDialog(this,
//                        "Error: " + e.getMessage(),
//                        "Error", JOptionPane.ERROR_MESSAGE);
//            }
//        }
//    }
//
//    private void showContactLandlord() {
//        JPanel panel = new JPanel(new BorderLayout());
//
//        JPanel infoPanel = new JPanel(new GridLayout(0, 1));
//        infoPanel.add(new JLabel("Contact your landlord for any assistance:"));
//        infoPanel.add(new JLabel(" "));
//        infoPanel.add(new JLabel("Phone: [Landlord Phone]"));
//        infoPanel.add(new JLabel("Email: [Landlord Email]"));
//        infoPanel.add(new JLabel(" "));
//        infoPanel.add(new JLabel("Office Hours:"));
//        infoPanel.add(new JLabel("Monday - Friday: 9:00 AM - 5:00 PM"));
//        infoPanel.add(new JLabel(" "));
//
//        panel.add(infoPanel, BorderLayout.CENTER);
//
//        JTextArea messageArea = new JTextArea(5, 30);
//        messageArea.setLineWrap(true);
//        messageArea.setWrapStyleWord(true);
//        panel.add(new JScrollPane(messageArea), BorderLayout.SOUTH);
//
//        int result = JOptionPane.showConfirmDialog(
//                this, panel, "Contact Landlord",
//                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
//
//        if (result == JOptionPane.OK_OPTION && !messageArea.getText().trim().isEmpty()) {
//            JOptionPane.showMessageDialog(this,
//                    "Your message has been sent to the landlord.",
//                    "Message Sent", JOptionPane.INFORMATION_MESSAGE);
//        }
//    }
//
//    private void logout() {
//        int confirm = JOptionPane.showConfirmDialog(this,
//                "Are you sure you want to logout?",
//                "Confirm Logout", JOptionPane.YES_NO_OPTION);
//
//        if (confirm == JOptionPane.YES_OPTION) {
//            this.dispose();
//            // In a real application, you'd show the login screen or similar
//            JOptionPane.showMessageDialog(null, "You have been logged out successfully.",
//                    "Logout Successful", JOptionPane.INFORMATION_MESSAGE);
//
//            // Show login screen again
//            LoginGUI loginGUI = new LoginGUI();
//            loginGUI.setVisible(true);
//        }
//    }
//
//    /**
//     * Launch the GUI for testing
//     */
//
//}