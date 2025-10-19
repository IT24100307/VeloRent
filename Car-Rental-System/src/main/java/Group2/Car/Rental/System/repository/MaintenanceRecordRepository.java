package Group2.Car.Rental.System.repository;

import Group2.Car.Rental.System.entity.MaintenanceRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface MaintenanceRecordRepository extends JpaRepository<MaintenanceRecord, Integer> {
    
    // Find all maintenance records for a specific vehicle
    List<MaintenanceRecord> findByVehicleIdOrderByMaintenanceDateDesc(Integer vehicleId);
    
    // Find maintenance records within a date range
    List<MaintenanceRecord> findByMaintenanceDateBetween(LocalDate startDate, LocalDate endDate);
    
    // Find recent maintenance (last 30 days)
    @Query("SELECT m FROM MaintenanceRecord m WHERE m.maintenanceDate >= :thirtyDaysAgo ORDER BY m.maintenanceDate DESC")
    List<MaintenanceRecord> findRecentMaintenance(@Param("thirtyDaysAgo") LocalDate thirtyDaysAgo);
    
    // Calculate total maintenance cost for a vehicle
    @Query("SELECT COALESCE(SUM(m.cost), 0) FROM MaintenanceRecord m WHERE m.vehicleId = :vehicleId")
    Double getTotalMaintenanceCostByVehicle(@Param("vehicleId") Integer vehicleId);
    
    // Find maintenance records by description containing keyword
    List<MaintenanceRecord> findByDescriptionContainingIgnoreCaseOrderByMaintenanceDateDesc(String keyword);
    
    // Count maintenance records for a vehicle
    long countByVehicleId(Integer vehicleId);

    // Delete all maintenance records for a specific vehicle (used when deleting vehicle)
    @Transactional
    void deleteByVehicleId(Integer vehicleId);
    
    // Find all maintenance records ordered by date
    List<MaintenanceRecord> findAllByOrderByMaintenanceDateDesc();

//    @Query("SELECT DATE(m.maintenanceDate) as day, SUM(m.cost) as total FROM MaintenanceRecord m WHERE m.maintenanceDate >= :startDate GROUP BY DATE(m.maintenanceDate) ORDER BY m.maintenanceDate")
    @Query(value = "SELECT CAST(m.maintenance_date AS DATE) as day, SUM(m.cost) as total " +
        "FROM maintenance_records m WHERE m.maintenance_date >= :startDate " +
        "GROUP BY CAST(m.maintenance_date AS DATE) ORDER BY day", nativeQuery = true)
    List<Object[]> getMaintenanceCostByDay(@Param("startDate") LocalDate startDate);

    @Query(value = "SELECT CONCAT(YEAR(m.maintenance_date), '-', RIGHT('0' + CAST(MONTH(m.maintenance_date) AS VARCHAR(2)), 2)) AS ym, " +
            "SUM(m.cost) AS total_cost " +
            "FROM maintenance_records m " +
            "WHERE m.maintenance_date >= :startDate " +
            "GROUP BY YEAR(m.maintenance_date), MONTH(m.maintenance_date) " +
            "ORDER BY YEAR(m.maintenance_date), MONTH(m.maintenance_date)", nativeQuery = true)
    List<Object[]> getMonthlyMaintenanceSince(@Param("startDate") LocalDate startDate);
}