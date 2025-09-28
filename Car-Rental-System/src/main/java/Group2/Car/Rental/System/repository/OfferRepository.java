package Group2.Car.Rental.System.repository;

import Group2.Car.Rental.System.entity.Offer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface OfferRepository extends JpaRepository<Offer, Long> {
    @Query("SELECT o FROM Offer o WHERE o.isDeleted = false")
    List<Offer> findAllNotDeleted();

    @Query("SELECT o FROM Offer o WHERE o.isDeleted = false and o.isActive=true")
    List<Offer> findAllActive();

    Optional<Offer> findById(Long id);
}