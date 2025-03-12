package Payment;

import java.time.LocalDate;

public class RentPayment {

    // ====================================================================================================
    // Fields
    // ====================================================================================================
    private String tenantID;
    private double amount;
    private LocalDate paymentDate;
    private boolean rentPayment;
    private boolean utilitiesPayment;

    // ====================================================================================================
    // Constructor
    // ====================================================================================================
    public RentPayment(String tenantID, double amount, boolean rentPayment, boolean utilitiesPayment) {
        if (tenantID == null || tenantID.isEmpty()) {
            throw new IllegalArgumentException("Tenant ID cannot be null or empty.");
        }
        if (amount < 0) {
            throw new IllegalArgumentException("Amount cannot be negative.");
        }
        this.tenantID = tenantID;
        this.amount = amount;
        this.paymentDate = LocalDate.now();
        this.rentPayment = rentPayment;
        this.utilitiesPayment = utilitiesPayment;
    }

    // ====================================================================================================
    // Getters
    // ====================================================================================================
    public String getTenantID() {
        return tenantID;
    }

    public double getAmount() {
        return amount;
    }

    public LocalDate getPaymentDate() {
        return paymentDate;
    }

    public boolean isRentPayment() {
        return rentPayment;
    }

    public boolean isUtilitiesPayment() {
        return utilitiesPayment;
    }

    // ====================================================================================================
    // toString Method
    // ====================================================================================================
    @Override
    public String toString() {
        return "Payment [Tenant ID: " + tenantID + ", Amount: " + amount +
                ", Date: " + paymentDate + ", Rent: " + rentPayment + ", Utilities: " + utilitiesPayment + "]";
    }
}
