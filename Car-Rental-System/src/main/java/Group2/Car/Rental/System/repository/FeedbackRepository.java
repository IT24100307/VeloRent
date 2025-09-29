package Group2.Car.Rental.System.repository;


import Group2.Car.Rental.System.entity.Feedback;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface FeedbackRepository extends JpaRepository<Feedback, Long> {
    Page<Feedback> findByIsDeletedFalse(Pageable pageable);

    List<Feedback> findByIsDeletedFalse();

    @Query("SELECT f FROM Feedback f WHERE f.createdBy.id = ?1 AND f.isDeleted = false")
    List<Feedback> findByCreatedByIdAndIsDeletedFalse(Long userId);
}
