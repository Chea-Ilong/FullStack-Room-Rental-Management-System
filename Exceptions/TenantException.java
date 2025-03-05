//package Exceptions;
//
//import Users.Tenant;
//
//public class TenantException extends Exception {
//    public TenantException(String message) {
//        super(message);
//    }
//
//    public TenantException(String message, Throwable cause) {
//        super(message, cause);
//    }
//
//    // Validate if the tenant is assigned to a room
//    public static void validateTenantHasRoom(Tenant tenant) throws TenantException {
//        if (tenant.getAssignedRoom() == null) {
//            throw new TenantException("Error: Tenant is not assigned to any room.");
//        }
//    }
//
//    // Validate if the payment amount is greater than zero
//    public static void validatePaymentAmount(double amount) throws TenantException {
//        if (amount <= 0) {
//            throw new TenantException("Invalid payment amount: $" + amount + ". Amount must be greater than zero.");
//        }
//    }
//
//    // Validate if the payment amount does not exceed the balance due
//    public static void validatePaymentAmountAgainstBalance(double amount, double balanceDue) throws TenantException {
//        if (amount > balanceDue) {
//            throw new TenantException("Error: Payment amount cannot be greater than the balance due.");
//        }
//    }
//}
