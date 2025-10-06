package Group2.Car.Rental.System.repository;

import Group2.Car.Rental.System.entity.Offer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface OfferRepository extends JpaRepository<Offer, Long> {
    @Query("SELECT o FROM Offer o WHERE o.isActive = true")
    List<Offer> findAllActive();
}