package Group2.Car.Rental.System.repository;

import Group2.Car.Rental.System.entity.Booking;
import Group2.Car.Rental.System.entity.Customer;
import Group2.Car.Rental.System.entity.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

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
}
