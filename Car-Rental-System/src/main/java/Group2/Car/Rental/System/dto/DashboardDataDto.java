package Group2.Car.Rental.System.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardDataDto {
    private long customerCount;
    private long vehicleCount;
    private long inUseVehicleCount;
    private long freeVehicleCount;
    private List<Map<String, Object>> incomeData;
    private List<Map<String, Object>> maintenanceData;
    private List<Map<String, Object>> usageData;
    // Totals (all time)
    private double totalVehicleIncome;
    private double totalPackageIncome;
    private double totalMaintenanceCost;
    // Monthly series for line chart
    private List<Map<String, Object>> monthlyIncome; // keys: month (YYYY-MM), total, vehicleIncome, packageIncome
    private List<Map<String, Object>> monthlyMaintenance; // keys: month (YYYY-MM), total


}
