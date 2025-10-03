package Group2.Car.Rental.System.repository;

import Group2.Car.Rental.System.entity.Booking;
import Group2.Car.Rental.System.entity.Customer;
import Group2.Car.Rental.System.entity.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Integer> {

    // Find all bookings for a specific customer
    List<Booking> findByCustomer(Customer customer);

    // Find all bookings for a specific vehicle
    List<Booking> findByVehicle(Vehicle vehicle);

    // Find bookings by status
    List<Booking> findByBookingStatus(String status);

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

    // New: overlap check excluding multiple non-blocking statuses (e.g., Cancelled, Returned)
    List<Booking> findByVehicleAndStartDateBeforeAndEndDateAfterAndBookingStatusNotIn(
        Vehicle vehicle,
        LocalDateTime endCheck,
        LocalDateTime startCheck,
        List<String> statuses);
}
