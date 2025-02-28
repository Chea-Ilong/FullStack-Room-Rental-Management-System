package Payment;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class UtilityUsage {
    private int electricUsage;
    private int waterUsage;
    private LocalDate date; // Exact date when the utility usage was recorded

    // Constructor
    public UtilityUsage(int electricUsage, int waterUsage, LocalDate date) {
        this.electricUsage = electricUsage;
        this.waterUsage = waterUsage;
        this.date = date;
    }

    // Getters
    public int getElectricUsage() {
        return electricUsage;
    }

    public int getWaterUsage() {
        return waterUsage;
    }

    public LocalDate getDate() {
        return date;
    }

    // Get formatted month (like "January 2025")
    public String getFormattedMonth() {
        return date.format(DateTimeFormatter.ofPattern("MMMM yyyy"));
    }

    @Override
    public String toString() {
        return "Electric Usage: " + electricUsage + " kWh, Water Usage: " + waterUsage +
                " m³, Date: " + date.format(DateTimeFormatter.ISO_LOCAL_DATE);
    }
}