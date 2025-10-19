package Group2.Car.Rental.System.service;

import Group2.Car.Rental.System.dto.DashboardDataDto;
import Group2.Car.Rental.System.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class DashboardService {
    @Autowired
    private CustomerRepository customerRepository;
    @Autowired
    private VehicleRepository vehicleRepository;
    @Autowired
    private PaymentRepository paymentRepository;
    @Autowired
    private MaintenanceRecordRepository maintenanceRecordRepository;
    @Autowired
    private BookingRepository bookingRepository;

    public DashboardDataDto getDashboardData() {
        LocalDateTime startDate = LocalDateTime.now().minusDays(7);
        DashboardDataDto dto = new DashboardDataDto();

        dto.setCustomerCount(customerRepository.count());
        dto.setVehicleCount(vehicleRepository.count());
        dto.setInUseVehicleCount(vehicleRepository.countInUse());
        dto.setFreeVehicleCount(vehicleRepository.countAvailable());

        // Income data
        List<Object[]> incomeRaw = paymentRepository.getIncomeByDay(startDate);
        dto.setIncomeData(convertToMapList(incomeRaw));

        // Maintenance data
        List<Object[]> maintenanceRaw = maintenanceRecordRepository.getMaintenanceCostByDay(startDate.toLocalDate());
        dto.setMaintenanceData(convertToMapList(maintenanceRaw));

        // Usage data
        List<Object[]> usageRaw = bookingRepository.getVehicleUsageByDay(startDate);
        dto.setUsageData(convertToMapList(usageRaw, "usage"));

        return dto;
    }

    private List<Map<String, Object>> convertToMapList(List<Object[]> rawData) {
        return rawData.stream()
                .map(row -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("day", row[0].toString()); // Date as string
                    map.put("total", ((Number) row[1]).doubleValue());
                    return map;
                })
                .collect(Collectors.toList());
    }

    private List<Map<String, Object>> convertToMapList(List<Object[]> rawData, String keyName) {
        return rawData.stream()
                .map(row -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("day", row[0].toString());
                    map.put(keyName, ((Number) row[1]).longValue());
                    return map;
                })
                .collect(Collectors.toList());
    }
}
