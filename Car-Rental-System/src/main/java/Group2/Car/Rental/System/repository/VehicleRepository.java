package Group2.Car.Rental.System.repository;

import Group2.Car.Rental.System.entity.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface VehicleRepository extends JpaRepository<Vehicle, Integer> {
    long count();
    @Query("SELECT COUNT(v) FROM Vehicle v WHERE v.status != 'Available'")
    long countInUse();
    @Query("SELECT COUNT(v) FROM Vehicle v WHERE v.status = 'Available'")
    long countAvailable();
}
