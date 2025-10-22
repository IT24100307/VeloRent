package Group2.Car.Rental.System.repository;

import Group2.Car.Rental.System.entity.LoginHistory;
import Group2.Car.Rental.System.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface LoginHistoryRepository extends JpaRepository<LoginHistory, Long> {
    
    // Find recent login history ordered by login time (most recent first)
    @Query("SELECT lh FROM LoginHistory lh ORDER BY lh.loginTime DESC")
    List<LoginHistory> findAllOrderByLoginTimeDesc();
    
    // Find login history for a specific user
    List<LoginHistory> findByUserOrderByLoginTimeDesc(User user);
    
    // Find login history within a date range
    @Query("SELECT lh FROM LoginHistory lh WHERE lh.loginTime BETWEEN :startDate AND :endDate ORDER BY lh.loginTime DESC")
    List<LoginHistory> findByLoginTimeBetweenOrderByLoginTimeDesc(
            @Param("startDate") LocalDateTime startDate, 
            @Param("endDate") LocalDateTime endDate);
    
    // Find recent login history (last N days)
    @Query("SELECT lh FROM LoginHistory lh WHERE lh.loginTime >= :sinceDate ORDER BY lh.loginTime DESC")
    List<LoginHistory> findRecentLogins(@Param("sinceDate") LocalDateTime sinceDate);
}