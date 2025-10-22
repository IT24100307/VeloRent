package Group2.Car.Rental.System.repository;

import Group2.Car.Rental.System.entity.Booking;
import Group2.Car.Rental.System.entity.Customer;
import Group2.Car.Rental.System.entity.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Integer> {

    // Find all bookings for a specific customer
    List<Booking> findByCustomer(Customer customer);

    // Find all bookings for a specific vehicle
    List<Booking> findByVehicle(Vehicle vehicle);

    // Find bookings by customer ID
    List<Booking> findByCustomer_UserId(Integer customerId);

    // Find bookings by status
    List<Booking> findByBookingStatus(String status);

    // Find bookings by booking type
    List<Booking> findByBookingType(String bookingType);

    // Find bookings between specific dates
    List<Booking> findByStartDateBetweenOrEndDateBetween(
        LocalDateTime startDate1, LocalDateTime endDate1,
        LocalDateTime startDate2, LocalDateTime endDate2);

    // Find active bookings for a vehicle (to check availability)
    List<Booking> findByVehicleAndStartDateBeforeAndEndDateAfterAndBookingStatusNot(
        Vehicle vehicle,
        LocalDateTime endCheck,
        LocalDateTime startCheck,
        String status);

    // Overlap check excluding multiple non-blocking statuses (e.g., Cancelled, Returned)
    List<Booking> findByVehicleAndStartDateBeforeAndEndDateAfterAndBookingStatusNotIn(
        Vehicle vehicle,
        LocalDateTime endCheck,
        LocalDateTime startCheck,
        List<String> statuses);

    // Find package bookings by customer
    @Query("SELECT b FROM Booking b WHERE b.customer.userId = :customerId AND b.bookingType = 'PACKAGE'")
    List<Booking> findPackageBookingsByCustomer(@Param("customerId") Integer customerId);

    // Find vehicle bookings by customer
    @Query("SELECT b FROM Booking b WHERE b.customer.userId = :customerId AND b.bookingType = 'VEHICLE'")
    List<Booking> findVehicleBookingsByCustomer(@Param("customerId") Integer customerId);

    // Count total bookings
    long count();

    // Count bookings by type
    long countByBookingType(String bookingType);

    // Vehicle usage history queries
    @Query("SELECT COUNT(b) FROM Booking b WHERE b.vehicle.vehicleId = :vehicleId")
    Long countBookingsByVehicle(@Param("vehicleId") Integer vehicleId);

    @Query("SELECT COUNT(b) FROM Booking b WHERE b.vehicle.vehicleId = :vehicleId AND b.bookingStatus = :status")
    Long countBookingsByVehicleAndStatus(@Param("vehicleId") Integer vehicleId, @Param("status") String status);

    @Query("SELECT SUM(b.totalCost) FROM Booking b WHERE b.vehicle.vehicleId = :vehicleId AND b.bookingStatus IN ('Confirmed', 'Returned')")
    BigDecimal getTotalRevenueByVehicle(@Param("vehicleId") Integer vehicleId);

    @Query("SELECT MAX(b.startDate) FROM Booking b WHERE b.vehicle.vehicleId = :vehicleId")
    LocalDateTime getLastBookingDateByVehicle(@Param("vehicleId") Integer vehicleId);

    @Query(value = "SELECT AVG(CAST(DATEDIFF(day, b.start_date, b.end_date) AS FLOAT)) FROM bookings b WHERE b.vehicle_id = :vehicleId AND b.booking_status IN ('Confirmed', 'Returned')", nativeQuery = true)
    Double getAverageBookingDurationByVehicle(@Param("vehicleId") Integer vehicleId);

    @Query(value = "SELECT TOP 1 CONCAT(u.first_name, ' ', u.last_name) FROM bookings b " +
           "JOIN customers c ON b.customer_id = c.customer_id " +
           "JOIN users u ON c.user_id = u.id " +
           "WHERE b.vehicle_id = :vehicleId " +
           "GROUP BY u.id, u.first_name, u.last_name " +
           "ORDER BY COUNT(b.booking_id) DESC", nativeQuery = true)
    String getMostFrequentCustomerByVehicle(@Param("vehicleId") Integer vehicleId);


    // Eagerly fetch associations for admin/fleet listing and order by created date desc then id desc
    @EntityGraph(attributePaths = {"customer", "customer.user", "vehicle", "vehiclePackage"})
    List<Booking> findAllByOrderByCreatedAtDescBookingIdDesc();


    @Query(value = "SELECT CAST(b.start_date AS DATE) as day, COUNT(DISTINCT b.vehicle_id) as usage " +
        "FROM bookings b WHERE b.start_date >= :startDate AND b.booking_status IN ('Confirmed','Rented','Booked') " +
        "GROUP BY CAST(b.start_date AS DATE) ORDER BY day", nativeQuery = true)
    List<Object[]> getVehicleUsageByDay(@Param("startDate") LocalDateTime startDate);

    // Count distinct vehicles that are in use (booked or rented) overlapping a time window
    @Query("SELECT COUNT(DISTINCT b.vehicle.vehicleId) FROM Booking b " +
        "WHERE b.vehicle IS NOT NULL AND b.startDate <= :end AND b.endDate >= :start " +
        "AND b.bookingStatus IN ('Confirmed','Rented','Booked')")
    Long countDistinctVehiclesInUseBetween(@Param("start") LocalDateTime start,
                         @Param("end") LocalDateTime end);

    // Convenience queries for notifications
    List<Booking> findTop20ByBookingStatusOrderByCreatedAtDesc(String status);

    List<Booking> findTop20ByBookingStatusOrderByBookingIdDesc(String status);

    // Upcoming confirmed bookings starting within a window (for countdown notifications)
    List<Booking> findTop20ByBookingStatusAndStartDateBetweenOrderByStartDateAsc(String status, LocalDateTime from, LocalDateTime to);

    // NEW: Customer-scoped convenience queries
    List<Booking> findTop20ByCustomer_UserIdAndBookingStatusOrderByCreatedAtDesc(Integer customerId, String status);

    // Customers ordered by total rentals (vehicle or package bookings) - returns rows: userId, name, email, rentals
    @Query(value = "SELECT u.id AS user_id, CONCAT(COALESCE(u.first_name,''),' ',COALESCE(u.last_name,'')) AS full_name, u.email, COUNT(b.booking_id) AS rentals\n" +
        "FROM bookings b\n" +
        "JOIN customers c ON b.customer_id = c.customer_id\n" +
        "JOIN users u ON c.user_id = u.id\n" +
        "GROUP BY u.id, u.first_name, u.last_name, u.email\n" +
        "HAVING COUNT(b.booking_id) >= :minRentals\n" +
        "ORDER BY rentals DESC, full_name ASC", nativeQuery = true)
    List<Object[]> findCustomersOrderedByRentalCount(@Param("minRentals") int minRentals);
}
