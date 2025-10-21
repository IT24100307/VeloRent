package Group2.Car.Rental.System.util;

import Group2.Car.Rental.System.entity.Vehicle;
import Group2.Car.Rental.System.entity.VehiclePackage;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * Creational Pattern: Singleton
 * Central pricing engine used across services to compute totals consistently.
 */
public final class PricingEngine {
    private static volatile PricingEngine INSTANCE;

    private PricingEngine() {}

    public static PricingEngine getInstance() {
        if (INSTANCE == null) {
            synchronized (PricingEngine.class) {
                if (INSTANCE == null) {
                    INSTANCE = new PricingEngine();
                }
            }
        }
        return INSTANCE;
    }

    /**
     * Calculate total cost for a package booking.
     * - Uses the package base price for its configured duration.
     * - If the booking exceeds the duration, charges pro-rated daily rate for extra days.
     * - Rounds to 2 decimal places.
     */

     //line 405
    
    public BigDecimal calculatePackageTotalCost(VehiclePackage vehiclePackage,
                                                LocalDateTime start,
                                                LocalDateTime end) {
        if (vehiclePackage == null || start == null || end == null) {
            return BigDecimal.ZERO;
        }

        long daysBetween = ChronoUnit.DAYS.between(start.toLocalDate(), end.toLocalDate());
        if (daysBetween <= 0) {
            daysBetween = 1; // minimum 1 day
        }

        BigDecimal base = safe(vehiclePackage.getPrice());
        int duration = safeInt(vehiclePackage.getDuration(), 1);
        if (duration <= 0) duration = 1;

        if (daysBetween > duration) {
            BigDecimal dailyRate = base.divide(BigDecimal.valueOf(duration), 2, RoundingMode.HALF_UP);
            BigDecimal additionalDays = BigDecimal.valueOf(daysBetween - duration);
            return base.add(dailyRate.multiply(additionalDays)).setScale(2, RoundingMode.HALF_UP);
        }
        return base.setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Calculate total cost for an individual vehicle booking.
     * - Uses discountedRatePerDay if present; otherwise rentalRatePerDay.
     * - Multiplies by number of days (minimum 1).
     * - Rounds to 2 decimals.
     */
    //line 191
    public BigDecimal calculateVehicleTotalCost(Vehicle vehicle,
                                                LocalDateTime start,
                                                LocalDateTime end) {
        if (vehicle == null || start == null || end == null) {
            return BigDecimal.ZERO;
        }
        long days = ChronoUnit.DAYS.between(start.toLocalDate(), end.toLocalDate());
        if (days <= 0) days = 1;

        BigDecimal rate = vehicle.getDiscountedRatePerDay();
        if (rate == null || rate.compareTo(BigDecimal.ZERO) <= 0) {
            rate = vehicle.getRentalRatePerDay();
        }
        rate = safe(rate);

        return rate.multiply(BigDecimal.valueOf(days)).setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal safe(BigDecimal v) {
        return v == null ? BigDecimal.ZERO : v;
    }

    private int safeInt(Integer v, int def) {
        return v == null ? def : v;
    }
}
