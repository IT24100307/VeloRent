package Group2.Car.Rental.System.repository;


import Group2.Car.Rental.System.entity.Feedback;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface FeedbackRepository extends JpaRepository<Feedback, Long> {
    @Query("SELECT f FROM Feedback f WHERE f.customer.id = ?1")
    List<Feedback> findByCustomerId(Long customerId);
}
