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
        int daysWindow = 30; // last month to today
        LocalDateTime startDate = LocalDateTime.now().minusDays(daysWindow);
        DashboardDataDto dto = new DashboardDataDto();

        dto.setCustomerCount(customerRepository.count());
        dto.setVehicleCount(vehicleRepository.count());
    try {
        // Prefer status-based counts for consistency with fleet dashboard
        long inUse = vehicleRepository.findAll().stream()
            .filter(v -> v != null && v.getStatus() != null)
            .filter(v -> "Booked".equalsIgnoreCase(v.getStatus()) || "Rented".equalsIgnoreCase(v.getStatus()))
            .count();
        long free = vehicleRepository.findAll().stream()
            .filter(v -> v != null && "Available".equalsIgnoreCase(v.getStatus()))
            .count();
        dto.setInUseVehicleCount(inUse);
        dto.setFreeVehicleCount(free);
    } catch (Exception e) {
        // Fallback to repository counters if anything goes wrong
        dto.setInUseVehicleCount(vehicleRepository.countInUse());
        dto.setFreeVehicleCount(vehicleRepository.countAvailable());
    }

        // Income data - includes both vehicle rentals and package bookings (last 7 days only)
        try {
            List<Object[]> incomeRaw = paymentRepository.getTotalIncomeByDay(startDate);
            dto.setIncomeData(convertTotalIncomeMapListForLastNDays(incomeRaw, daysWindow));
        } catch (Exception e) {
            System.err.println("Error fetching total income data: " + e.getMessage());
            e.printStackTrace();
            // Fallback to original method if new query fails
            try {
                List<Object[]> fallbackIncomeRaw = paymentRepository.getIncomeByDay(startDate);
                dto.setIncomeData(convertToMapListForLast7Days(fallbackIncomeRaw));
            } catch (Exception e2) {
                dto.setIncomeData(java.util.Collections.emptyList());
            }
        }

        // Maintenance data (last 7 days only)
        try {
            List<Object[]> maintenanceRaw = maintenanceRecordRepository.getMaintenanceCostByDay(startDate.toLocalDate());
            dto.setMaintenanceData(convertToMapListForLastNDays(maintenanceRaw, daysWindow));
        } catch (Exception e) {
            dto.setMaintenanceData(java.util.Collections.emptyList());
        }

        // Usage data - compute using overlap per day to be accurate
        try {
            java.util.List<java.util.Map<String, Object>> usage = new java.util.ArrayList<>();
            for (int i = 6; i >= 0; i--) {
                java.time.LocalDate day = java.time.LocalDate.now().minusDays(i);
                LocalDateTime start = day.atStartOfDay();
                LocalDateTime end = day.plusDays(1).atStartOfDay().minusSeconds(1);
                Long count = 0L;
                try {
                    count = bookingRepository.countDistinctVehiclesInUseBetween(start, end);
                } catch (Exception ignored) {}
                java.util.Map<String, Object> m = new java.util.HashMap<>();
                m.put("day", day.toString());
                m.put("usage", count != null ? count : 0L);
                usage.add(m);
            }
            dto.setUsageData(usage);
        } catch (Exception e) {
            dto.setUsageData(java.util.Collections.emptyList());
        }
        // Totals across all time
        try {
            dto.setTotalVehicleIncome(sumPaymentsByType("VEHICLE"));
            dto.setTotalPackageIncome(sumPaymentsByType("PACKAGE"));
            dto.setTotalMaintenanceCost(sumAllMaintenanceCost());
        } catch (Exception ignored) {}

        // Monthly series (last 12 months)
        try {
            java.time.LocalDateTime startMonth = java.time.LocalDate.now().minusMonths(11).withDayOfMonth(1).atStartOfDay();
            java.util.List<Object[]> incomeRows = paymentRepository.getMonthlyIncomeSince(startMonth);
            java.util.Map<String, java.util.Map<String, Object>> incomeMap = new java.util.HashMap<>();
            if (incomeRows != null) {
                for (Object[] row : incomeRows) {
                    String ym = String.valueOf(row[0]);
                    double veh = toDouble(row[1]);
                    double pkg = toDouble(row[2]);
                    double tot = toDouble(row[3]);
                    java.util.Map<String,Object> m = new java.util.HashMap<>();
                    m.put("month", ym);
                    m.put("vehicleIncome", veh);
                    m.put("packageIncome", pkg);
                    m.put("total", tot);
                    incomeMap.put(ym, m);
                }
            }
            java.util.List<java.util.Map<String,Object>> monthlyIncome = new java.util.ArrayList<>();
            for (int i = 11; i >= 0; i--) {
                java.time.LocalDate lm = java.time.LocalDate.now().minusMonths(i).withDayOfMonth(1);
                String ym = String.format("%04d-%02d", lm.getYear(), lm.getMonthValue());
                monthlyIncome.add(incomeMap.getOrDefault(ym, new java.util.HashMap<>() {{
                    put("month", ym); put("vehicleIncome", 0.0); put("packageIncome", 0.0); put("total", 0.0);
                }}));
            }
            dto.setMonthlyIncome(monthlyIncome);

            java.util.List<Object[]> maintRows = maintenanceRecordRepository.getMonthlyMaintenanceSince(startMonth.toLocalDate());
            java.util.Map<String, Double> maintMap = new java.util.HashMap<>();
            if (maintRows != null) {
                for (Object[] row : maintRows) {
                    maintMap.put(String.valueOf(row[0]), toDouble(row[1]));
                }
            }
            java.util.List<java.util.Map<String,Object>> monthlyMaint = new java.util.ArrayList<>();
            for (int i = 11; i >= 0; i--) {
                java.time.LocalDate lm = java.time.LocalDate.now().minusMonths(i).withDayOfMonth(1);
                String ym = String.format("%04d-%02d", lm.getYear(), lm.getMonthValue());
                java.util.Map<String,Object> m = new java.util.HashMap<>();
                m.put("month", ym); m.put("total", maintMap.getOrDefault(ym, 0.0));
                monthlyMaint.add(m);
            }
            dto.setMonthlyMaintenance(monthlyMaint);
        } catch (Exception ignored) {}
        return dto;
    }

    private double sumPaymentsByType(String type) {
        try {
            // Native SQL would be more efficient, but reuse existing getTotalIncomeByDay pattern via time-unbounded range
            // We'll instead query all payments grouped by booking type using a lightweight stream across repository
            java.util.List<Group2.Car.Rental.System.entity.Payment> all = paymentRepository.findAll();
            return all.stream()
                    .filter(p -> p.getBooking() != null && p.getBooking().getBookingType() != null)
                    .filter(p -> type.equalsIgnoreCase(p.getBooking().getBookingType()))
                    .map(p -> p.getAmount() != null ? p.getAmount().doubleValue() : 0.0)
                    .reduce(0.0, Double::sum);
        } catch (Exception e) {
            return 0.0;
        }
    }

    private double sumAllMaintenanceCost() {
        try {
            java.util.List<Group2.Car.Rental.System.entity.MaintenanceRecord> all = maintenanceRecordRepository.findAll();
            return all.stream()
                    .map(m -> m.getCost() != null ? m.getCost().doubleValue() : 0.0)
                    .reduce(0.0, Double::sum);
        } catch (Exception e) {
            return 0.0;
        }
    }

    @SuppressWarnings("unused")
    private List<Map<String, Object>> convertToMapList(List<Object[]> rawData) {
        if (rawData == null) return java.util.Collections.emptyList();
        return rawData.stream()
                .filter(row -> row != null && row.length >= 2)
                .map(row -> {
                    Map<String, Object> map = new HashMap<>();
                    Object day = row[0];
                    Object total = row[1];
                    map.put("day", day != null ? day.toString() : "");
                    map.put("total", toDouble(total));
                    return map;
                })
                .collect(Collectors.toList());
    }

    @SuppressWarnings("unused")
    private List<Map<String, Object>> convertToMapList(List<Object[]> rawData, String keyName) {
        if (rawData == null) return java.util.Collections.emptyList();
        return rawData.stream()
                .filter(row -> row != null && row.length >= 2)
                .map(row -> {
                    Map<String, Object> map = new HashMap<>();
                    Object day = row[0];
                    Object val = row[1];
                    map.put("day", day != null ? day.toString() : "");
                    map.put(keyName, toLong(val));
                    return map;
                })
                .collect(Collectors.toList());
    }

    private double toDouble(Object number) {
        if (number instanceof Number) {
            return ((Number) number).doubleValue();
        }
        return 0.0d;
    }

    private long toLong(Object number) {
        if (number instanceof Number) {
            return ((Number) number).longValue();
        }
        return 0L;
    }

    @SuppressWarnings("unused")
    private List<Map<String, Object>> convertTotalIncomeMapList(List<Object[]> rawData) {
        if (rawData == null) return java.util.Collections.emptyList();
        return rawData.stream()
                .filter(row -> row != null && row.length >= 4)
                .map(row -> {
                    Map<String, Object> map = new HashMap<>();
                    Object day = row[0];
                    Object vehicleIncome = row[1];
                    Object packageIncome = row[2];
                    Object totalIncome = row[3];
                    
                    map.put("day", day != null ? day.toString() : "");
                    map.put("total", toDouble(totalIncome)); // Use total income for the chart
                    map.put("vehicleIncome", toDouble(vehicleIncome));
                    map.put("packageIncome", toDouble(packageIncome));
                    return map;
                })
                .collect(Collectors.toList());
    }

    private List<Map<String, Object>> convertTotalIncomeMapListForLast7Days(List<Object[]> rawData) {
        return convertTotalIncomeMapListForLastNDays(rawData, 7);
    }

    private List<Map<String, Object>> convertTotalIncomeMapListForLastNDays(List<Object[]> rawData, int days) {
        // Create a map of day -> data for quick lookup
        Map<String, Map<String, Object>> dataMap = new HashMap<>();
        if (rawData != null) {
            rawData.stream()
                    .filter(row -> row != null && row.length >= 4)
                    .forEach(row -> {
                        Object day = row[0];
                        Object vehicleIncome = row[1];
                        Object packageIncome = row[2];
                        Object totalIncome = row[3];
                        
                        if (day != null) {
                            Map<String, Object> map = new HashMap<>();
                            map.put("day", day.toString());
                            map.put("total", toDouble(totalIncome));
                            map.put("vehicleIncome", toDouble(vehicleIncome));
                            map.put("packageIncome", toDouble(packageIncome));
                            dataMap.put(day.toString(), map);
                        }
                    });
        }
        
        // Generate exactly 7 days of data (today - 6 to today)
        List<Map<String, Object>> result = new java.util.ArrayList<>();
        for (int i = days-1; i >= 0; i--) {
            java.time.LocalDate day = java.time.LocalDate.now().minusDays(i);
            String dayStr = day.toString();
            
            Map<String, Object> dayData = dataMap.getOrDefault(dayStr, new HashMap<>());
            if (dayData.isEmpty()) {
                dayData.put("day", dayStr);
                dayData.put("total", 0.0);
                dayData.put("vehicleIncome", 0.0);
                dayData.put("packageIncome", 0.0);
            }
            result.add(dayData);
        }
        
        return result;
    }

    private List<Map<String, Object>> convertToMapListForLast7Days(List<Object[]> rawData) {
        return convertToMapListForLastNDays(rawData, 7);
    }

    private List<Map<String, Object>> convertToMapListForLastNDays(List<Object[]> rawData, int days) {
        // Create a map of day -> total for quick lookup
        Map<String, Double> dataMap = new HashMap<>();
        if (rawData != null) {
            rawData.stream()
                    .filter(row -> row != null && row.length >= 2)
                    .forEach(row -> {
                        Object day = row[0];
                        Object total = row[1];
                        if (day != null) {
                            dataMap.put(day.toString(), toDouble(total));
                        }
                    });
        }
        
        // Generate exactly 7 days of data
        List<Map<String, Object>> result = new java.util.ArrayList<>();
        for (int i = days-1; i >= 0; i--) {
            java.time.LocalDate day = java.time.LocalDate.now().minusDays(i);
            String dayStr = day.toString();
            
            Map<String, Object> dayData = new HashMap<>();
            dayData.put("day", dayStr);
            dayData.put("total", dataMap.getOrDefault(dayStr, 0.0));
            result.add(dayData);
        }
        
        return result;
    }
}
