package Payment;

import DataBase.BillDML;
import Properties.Building;
import Properties.Floor;
import Properties.Room;
import Users.Tenant;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BillRecord {
    // Map structure to store bills by building, floor, room, and billing period
    private Map<String, Map<String, Map<String, Map<YearMonth, Bill>>>> billDatabase;

    // Constructor
    public BillRecord() {
        this.billDatabase = new HashMap<>();
    }

    /**
     * Store a newly created bill in the database
     *
     * @param bill The bill to store
     * @param buildingName Name of the building
     * @param floorNumber Floor number
     */
    public void storeBill(Bill bill, String buildingName, String floorNumber) {
        String roomNumber = bill.getRoom().getRoomNumber();
        YearMonth billingPeriod = YearMonth.from(bill.getBillDate());

        // Create the necessary structure if it doesn't exist
        billDatabase.putIfAbsent(buildingName, new HashMap<>());
        billDatabase.get(buildingName).putIfAbsent(floorNumber, new HashMap<>());
        billDatabase.get(buildingName).get(floorNumber).putIfAbsent(roomNumber, new HashMap<>());

        // Store the bill
        billDatabase.get(buildingName).get(floorNumber).get(roomNumber).put(billingPeriod, bill);

        System.out.println("Bill stored successfully for Room " + roomNumber +
                " in " + buildingName + ", Floor " + floorNumber +
                " for period " + billingPeriod.getMonth() + " " + billingPeriod.getYear());
    }

    /**
     * Create and distribute bills to selected rooms
     *
     * @param rooms List of rooms to create bills for
     * @param buildingName Name of the building
     * @param floorNumber Floor number
     * @param rentAmount Base rent amount input by landlord
     * @return List of generated bills
     */
    public List<Bill> distributeBills(List<Room> rooms, String buildingName, String floorNumber,
                                      double rentAmount, Map<String, Integer> electricUsageMap,
                                      Map<String, Integer> waterUsageMap) {
        List<Bill> generatedBills = new ArrayList<>();
        BillDML billDML = new BillDML();

        // Ensure the bills table exists
        billDML.createBillsTableIfNotExists();

        for (Room room : rooms) {
            if (!room.isOccupied()) {
                continue;  // Skip unoccupied rooms
            }

            String roomNumber = room.getRoomNumber();

            // Get usage data for this room
            int electricUsage = electricUsageMap.getOrDefault(roomNumber, 0);
            int waterUsage = waterUsageMap.getOrDefault(roomNumber, 0);

            // Create the bill
            Bill bill = new Bill(room, buildingName, floorNumber, rentAmount, electricUsage, waterUsage);

            // Save to database
            boolean saved = billDML.saveBill(bill);

            if (saved) {
                System.out.println("Bill saved successfully for room " + roomNumber);
                generatedBills.add(bill);
            } else {
                System.out.println("Failed to save bill for room " + roomNumber);
            }
        }

        return generatedBills;
    }

    /**
     * Get a specific bill by building, floor, room, and billing period
     */
    public Bill getBill(String buildingName, String floorNumber, String roomNumber, YearMonth billingPeriod) {
        if (!billDatabase.containsKey(buildingName) ||
                !billDatabase.get(buildingName).containsKey(floorNumber) ||
                !billDatabase.get(buildingName).get(floorNumber).containsKey(roomNumber) ||
                !billDatabase.get(buildingName).get(floorNumber).get(roomNumber).containsKey(billingPeriod)) {
            return null;
        }

        return billDatabase.get(buildingName).get(floorNumber).get(roomNumber).get(billingPeriod);
    }
    public void markAndStorePaidBill(Bill bill, double paymentAmount) {
        bill.markAsPaid(paymentAmount);  // Mark bill as paid
        storeBill(bill, bill.getBuildingName(), bill.getFloorNumber());  // Persist updated bill
    }

    /**
     * Update an existing bill in the record
     * @param bill The updated bill
     */
    public void updateBill(Bill bill) {
        String buildingName = bill.getBuildingName();
        String floorNumber = bill.getFloorNumber();
        String roomNumber = bill.getRoom().getRoomNumber();
        YearMonth billingPeriod = YearMonth.from(bill.getBillDate());

        // Check if the bill exists in our structure
        if (billDatabase.containsKey(buildingName) &&
                billDatabase.get(buildingName).containsKey(floorNumber) &&
                billDatabase.get(buildingName).get(floorNumber).containsKey(roomNumber) &&
                billDatabase.get(buildingName).get(floorNumber).get(roomNumber).containsKey(billingPeriod)) {

            // Update the bill
            billDatabase.get(buildingName).get(floorNumber).get(roomNumber).put(billingPeriod, bill);
            System.out.println("Bill updated successfully in BillRecord for Room " + roomNumber +
                    " in " + buildingName + ", Floor " + floorNumber +
                    " for period " + billingPeriod.getMonth() + " " + billingPeriod.getYear());
        } else {
            // If the bill doesn't exist in our structure, store it
            storeBill(bill, buildingName, floorNumber);
        }
    }
    /**
     * Get all bills for a specific room
     */
    public List<Bill> getBillHistoryForRoom(String buildingName, String floorNumber, String roomNumber) {
        if (!billDatabase.containsKey(buildingName) ||
                !billDatabase.get(buildingName).containsKey(floorNumber) ||
                !billDatabase.get(buildingName).get(floorNumber).containsKey(roomNumber)) {
            return new ArrayList<>();
        }

        return new ArrayList<>(billDatabase.get(buildingName).get(floorNumber).get(roomNumber).values());
    }

    /**
     * Get all bills for a specific tenant
     */
    public List<Bill> getBillHistoryForTenant(String tenantId) {
        // Create a BillDML instance
        BillDML billDML = new BillDML();

        // Get bills from the database
        List<Bill> tenantBills = billDML.getBillsByTenantId(tenantId);

        // Store each bill in the billDatabase structure
        for (Bill bill : tenantBills) {
            String buildingName = bill.getBuildingName();
            String floorNumber = bill.getFloorNumber();

            // Only store if we have the necessary information
            if (buildingName != null && floorNumber != null && bill.getRoom() != null) {
                updateBill(bill);
            }
        }

        return tenantBills;
    }

    /**
     * Get all unpaid bills
     */
    public List<Bill> getUnpaidBills() {
        List<Bill> unpaidBills = new ArrayList<>();

        // Iterate through all bills to find unpaid ones
        for (Map<String, Map<String, Map<YearMonth, Bill>>> buildingMap : billDatabase.values()) {
            for (Map<String, Map<YearMonth, Bill>> floorMap : buildingMap.values()) {
                for (Map<YearMonth, Bill> roomMap : floorMap.values()) {
                    for (Bill bill : roomMap.values()) {
                        if (!bill.isPaid()) {
                            unpaidBills.add(bill);
                        }
                    }
                }
            }
        }

        return unpaidBills;
    }

    /**
     * Get all bills for a specific month
     */
    public List<Bill> getBillsForMonth(YearMonth month) {
        List<Bill> monthlyBills = new ArrayList<>();

        System.out.println("DEBUG: Searching for bills for month: " + month);

        // Iterate through all bills to find those for the specified month
        for (String buildingName : billDatabase.keySet()) {
            Map<String, Map<String, Map<YearMonth, Bill>>> buildingMap = billDatabase.get(buildingName);
            System.out.println("DEBUG: Building: " + buildingName);

            for (String floorNumber : buildingMap.keySet()) {
                Map<String, Map<YearMonth, Bill>> floorMap = buildingMap.get(floorNumber);
                System.out.println("DEBUG: Floor: " + floorNumber);

                for (String roomNumber : floorMap.keySet()) {
                    Map<YearMonth, Bill> roomMap = floorMap.get(roomNumber);
                    System.out.println("DEBUG: Room: " + roomNumber + ", Key set: " + roomMap.keySet());

                    if (roomMap.containsKey(month)) {
                        Bill bill = roomMap.get(month);
                        System.out.println("DEBUG: Found bill ID: " + bill.getBillID() + ", Paid: " + bill.isPaid());
                        monthlyBills.add(bill);
                    }
                }
            }
        }

        return monthlyBills;
    }

    /**
     * Generate a summary report for a specific month
     */
    public String generateMonthlyReport(YearMonth month) {
        List<Bill> monthlyBills = getBillsForMonth(month);
        int totalBills = monthlyBills.size();
        int paidBills = (int) monthlyBills.stream().filter(Bill::isPaid).count();
        int unpaidBills = totalBills - paidBills;

        // Calculate total amounts
        double totalRevenue = 0.0;
        double totalExpectedRevenue = 0.0;
        double totalElectricAmount = 0.0;
        double totalWaterAmount = 0.0;
        double totalRentAmount = 0.0;

        for (Bill bill : monthlyBills) {
            totalExpectedRevenue += bill.getTotalAmount();
            totalElectricAmount += bill.getElectricAmount();
            totalWaterAmount += bill.getWaterAmount();
            totalRentAmount += bill.getRentAmount();

            if (bill.isPaid()) {
                totalRevenue += bill.getTotalAmount();
            }
        }

        double collectionRate = totalBills > 0 ? (double) paidBills / totalBills * 100 : 0;

        // Build the report
        StringBuilder report = new StringBuilder();
        report.append("===============================================\n");
        report.append("             MONTHLY BILLING REPORT            \n");
        report.append("===============================================\n");
        report.append("Month: ").append(month.getMonth()).append(" ").append(month.getYear()).append("\n");
        report.append("-----------------------------------------------\n");
        report.append("Total Bills: ").append(totalBills).append("\n");
        report.append("Paid Bills: ").append(paidBills).append("\n");
        report.append("Unpaid Bills: ").append(unpaidBills).append("\n");
        report.append("Collection Rate: ").append(String.format("%.2f", collectionRate)).append("%\n");
        report.append("-----------------------------------------------\n");
        report.append("Rent Revenue: ").append(String.format("%.0f KHR (%.2f USD)",
                totalRentAmount, totalRentAmount / 4100.0)).append("\n");
        report.append("Electric Revenue: ").append(String.format("%.0f KHR (%.2f USD)",
                totalElectricAmount, totalElectricAmount / 4100.0)).append("\n");
        report.append("Water Revenue: ").append(String.format("%.0f KHR (%.2f USD)",
                totalWaterAmount, totalWaterAmount / 4100.0)).append("\n");
        report.append("-----------------------------------------------\n");
        report.append("Collected Revenue: ").append(String.format("%.0f KHR (%.2f USD)",
                totalRevenue, totalRevenue / 4100.0)).append("\n");
        report.append("Expected Revenue: ").append(String.format("%.0f KHR (%.2f USD)",
                totalExpectedRevenue, totalExpectedRevenue / 4100.0)).append("\n");
        report.append("Outstanding Amount: ").append(String.format("%.0f KHR (%.2f USD)",
                totalExpectedRevenue - totalRevenue, (totalExpectedRevenue - totalRevenue) / 4100.0)).append("\n");
        report.append("===============================================\n");

        return report.toString();
    }


}