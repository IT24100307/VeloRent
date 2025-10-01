package Group2.Car.Rental.System.repository;

import Group2.Car.Rental.System.entity.Payment;
import Group2.Car.Rental.System.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Integer> {
    Payment findByBooking(Booking booking);
}
