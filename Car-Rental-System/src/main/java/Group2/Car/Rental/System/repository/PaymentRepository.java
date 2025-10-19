package Group2.Car.Rental.System.repository;

import Group2.Car.Rental.System.entity.Payment;
import Group2.Car.Rental.System.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Integer> {
    Payment findByBooking(Booking booking);

    // Eagerly fetch related booking > customer > user for admin listing
    @Query("SELECT p FROM Payment p JOIN FETCH p.booking b JOIN FETCH b.customer c JOIN FETCH c.user u ORDER BY p.paymentDate DESC")
    List<Payment> findAllWithBookingCustomerUser();

    // Aggregations
    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p")
    BigDecimal sumAllAmounts();

    @Query("SELECT p.paymentMethod, COALESCE(SUM(p.amount),0), COUNT(p) FROM Payment p GROUP BY p.paymentMethod")
    List<Object[]> sumAndCountByMethod();

    @Query("SELECT p.paymentStatus, COUNT(p) FROM Payment p GROUP BY p.paymentStatus")
    List<Object[]> countByStatus();

    //@Query("SELECT DATE(p.paymentDate) as day, SUM(p.amount) as total FROM Payment p WHERE p.paymentDate >= :startDate GROUP BY DATE(p.paymentDate) ORDER BY p.paymentDate")
    @Query(value = "SELECT CAST(p.payment_date AS DATE) as day, SUM(p.amount) as total " +
            "FROM payments p WHERE p.payment_date >= :startDate " +
            "GROUP BY CAST(p.payment_date AS DATE) ORDER BY day", nativeQuery = true)
    List<Object[]> getIncomeByDay(@Param("startDate") LocalDateTime startDate);

    // Get total income including both vehicle and package bookings
    @Query(value = "SELECT CAST(p.payment_date AS DATE) as day, " +
            "SUM(CASE WHEN b.booking_type = 'VEHICLE' THEN p.amount ELSE 0 END) as vehicle_income, " +
            "SUM(CASE WHEN b.booking_type = 'PACKAGE' THEN p.amount ELSE 0 END) as package_income, " +
            "SUM(p.amount) as total_income " +
            "FROM payments p " +
            "JOIN bookings b ON p.booking_id = b.booking_id " +
            "WHERE p.payment_date >= :startDate " +
            "GROUP BY CAST(p.payment_date AS DATE) ORDER BY day", nativeQuery = true)
    List<Object[]> getTotalIncomeByDay(@Param("startDate") LocalDateTime startDate);

    // Monthly totals since a start date (YYYY-MM label with SQL Server)
    @Query(value = "SELECT CONCAT(YEAR(p.payment_date), '-', RIGHT('0' + CAST(MONTH(p.payment_date) AS VARCHAR(2)), 2)) AS ym, " +
            "SUM(CASE WHEN b.booking_type = 'VEHICLE' THEN p.amount ELSE 0 END) AS vehicle_income, " +
            "SUM(CASE WHEN b.booking_type = 'PACKAGE' THEN p.amount ELSE 0 END) AS package_income, " +
            "SUM(p.amount) AS total_income " +
            "FROM payments p " +
            "JOIN bookings b ON p.booking_id = b.booking_id " +
            "WHERE p.payment_date >= :startDate " +
            "GROUP BY YEAR(p.payment_date), MONTH(p.payment_date) " +
            "ORDER BY YEAR(p.payment_date), MONTH(p.payment_date)", nativeQuery = true)
    List<Object[]> getMonthlyIncomeSince(@Param("startDate") LocalDateTime startDate);
}
