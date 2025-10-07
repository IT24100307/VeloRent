package Group2.Car.Rental.System.service;

import Group2.Car.Rental.System.entity.MaintenanceRecord;
import Group2.Car.Rental.System.repository.MaintenanceRecordRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class MaintenanceService {
    
    @Autowired
    private MaintenanceRecordRepository maintenanceRepository;
    
    // Log new maintenance record
    public MaintenanceRecord logMaintenance(Integer vehicleId, String description, BigDecimal cost, LocalDate date) {
        MaintenanceRecord record = new MaintenanceRecord(date, description, cost, vehicleId);
        return maintenanceRepository.save(record);
    }
    
    // Schedule future maintenance (with cost as null initially)
    public MaintenanceRecord scheduleMaintenance(Integer vehicleId, String description, LocalDate scheduledDate) {
        MaintenanceRecord record = new MaintenanceRecord(scheduledDate, description, null, vehicleId);
        return maintenanceRepository.save(record);
    }
    
    // Get all maintenance records for a vehicle
    public List<MaintenanceRecord> getMaintenanceByVehicle(Integer vehicleId) {
        return maintenanceRepository.findByVehicleIdOrderByMaintenanceDateDesc(vehicleId);
    }
    
    // Get recent maintenance (last 30 days)
    public List<MaintenanceRecord> getRecentMaintenance() {
        LocalDate thirtyDaysAgo = LocalDate.now().minusDays(30);
        return maintenanceRepository.findRecentMaintenance(thirtyDaysAgo);
    }
    
    // Get upcoming maintenance (scheduled for future dates)
    public List<MaintenanceRecord> getUpcomingMaintenance() {
        LocalDate today = LocalDate.now();
        return maintenanceRepository.findByMaintenanceDateBetween(today, today.plusDays(30))
                .stream()
                .filter(m -> m.getCost() == null) // Scheduled but not completed
                .toList();
    }
    
    // Get overdue maintenance (scheduled for past dates but no cost recorded)
    public List<MaintenanceRecord> getOverdueMaintenance() {
        LocalDate today = LocalDate.now();
        return maintenanceRepository.findByMaintenanceDateBetween(today.minusDays(365), today.minusDays(1))
                .stream()
                .filter(m -> m.getCost() == null) // Scheduled but not completed
                .toList();
    }
    
    // Get maintenance records within date range
    public List<MaintenanceRecord> getMaintenanceByDateRange(LocalDate startDate, LocalDate endDate) {
        return maintenanceRepository.findByMaintenanceDateBetween(startDate, endDate);
    }
    
    // Get total maintenance cost for a vehicle
    public Double getTotalMaintenanceCost(Integer vehicleId) {
        return maintenanceRepository.getTotalMaintenanceCostByVehicle(vehicleId);
    }
    
    // Get all maintenance records
    public List<MaintenanceRecord> getAllMaintenance() {
        return maintenanceRepository.findAllByOrderByMaintenanceDateDesc();
    }
    
    // Get maintenance record by ID
    public Optional<MaintenanceRecord> getMaintenanceById(Integer id) {
        return maintenanceRepository.findById(id);
    }
    
    // Update maintenance record
    public MaintenanceRecord updateMaintenance(MaintenanceRecord record) {
        return maintenanceRepository.save(record);
    }
    
    // Complete scheduled maintenance (add cost)
    public Optional<MaintenanceRecord> completeMaintenance(Integer id, BigDecimal cost) {
        return maintenanceRepository.findById(id)
                .map(record -> {
                    record.setCost(cost);
                    return maintenanceRepository.save(record);
                });
    }
    
    // Delete maintenance record
    public void deleteMaintenance(Integer id) {
        maintenanceRepository.deleteById(id);
    }
    
    // Search maintenance records by description
    public List<MaintenanceRecord> searchMaintenanceByDescription(String keyword) {
        return maintenanceRepository.findByDescriptionContainingIgnoreCaseOrderByMaintenanceDateDesc(keyword);
    }
    
    // Get maintenance count for a vehicle
    public long getMaintenanceCountByVehicle(Integer vehicleId) {
        return maintenanceRepository.countByVehicleId(vehicleId);
    }
    
    // Get maintenance statistics
    public MaintenanceStats getMaintenanceStats() {
        List<MaintenanceRecord> allRecords = maintenanceRepository.findAll();
        
        long totalRecords = allRecords.size();
        long completedRecords = allRecords.stream()
                .mapToLong(record -> record.getCost() != null ? 1 : 0)
                .sum();
        long scheduledRecords = totalRecords - completedRecords;
        
        BigDecimal totalCost = allRecords.stream()
                .filter(record -> record.getCost() != null)
                .map(MaintenanceRecord::getCost)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        return new MaintenanceStats(totalRecords, completedRecords, scheduledRecords, totalCost);
    }
    
    // Inner class for maintenance statistics
    public static class MaintenanceStats {
        private final long totalRecords;
        private final long completedRecords;
        private final long scheduledRecords;
        private final BigDecimal totalCost;
        
        public MaintenanceStats(long totalRecords, long completedRecords, long scheduledRecords, BigDecimal totalCost) {
            this.totalRecords = totalRecords;
            this.completedRecords = completedRecords;
            this.scheduledRecords = scheduledRecords;
            this.totalCost = totalCost;
        }
        
        public long getTotalRecords() { return totalRecords; }
        public long getCompletedRecords() { return completedRecords; }
        public long getScheduledRecords() { return scheduledRecords; }
        public BigDecimal getTotalCost() { return totalCost; }
    }
}