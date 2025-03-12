package Payment;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class UtilityUsage {

    // ====================================================================================================
    // Fields
    // ====================================================================================================
    private int electricUsage;
    private int waterUsage;
    private LocalDate date;

    // ====================================================================================================
    // Constructor
    // ====================================================================================================
    public UtilityUsage(int electricUsage, int waterUsage, LocalDate date) {
        if (electricUsage < 0 || waterUsage < 0) {
            throw new IllegalArgumentException("Usage values cannot be negative.");
        }
        this.electricUsage = electricUsage;
        this.waterUsage = waterUsage;
        this.date = date;
    }

    // ====================================================================================================
    // Getters
    // ====================================================================================================
    public int getElectricUsage() {
        return electricUsage;
    }

    public int getWaterUsage() {
        return waterUsage;
    }

    public LocalDate getDate() {
        return date;
    }

    // ====================================================================================================
    // toString Method
    // ====================================================================================================
    @Override
    public String toString() {
        return "Electric Usage: " + electricUsage + " kWh, Water Usage: " + waterUsage +
                " m³, Date: " + date.format(DateTimeFormatter.ISO_LOCAL_DATE);
    }
}
