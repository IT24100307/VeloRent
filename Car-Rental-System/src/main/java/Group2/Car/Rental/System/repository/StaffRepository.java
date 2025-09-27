package Group2.Car.Rental.System.repository;

import Group2.Car.Rental.System.entity.Staff;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StaffRepository extends JpaRepository<Staff, Long> {
    // Check if a staff member exists with the given staffIdCode
    boolean existsByStaffIdCode(String staffIdCode);
}
