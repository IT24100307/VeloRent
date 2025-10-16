package Group2.Car.Rental.System.repository;

import Group2.Car.Rental.System.entity.Payment;
import Group2.Car.Rental.System.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
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
}
