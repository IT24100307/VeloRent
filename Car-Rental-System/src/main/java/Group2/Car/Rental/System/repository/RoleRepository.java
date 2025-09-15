package Group2.Car.Rental.System.repository;

import Group2.Car.Rental.System.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Integer> {
    // This method allows you to find a specific role by its name,
    // for example, to assign the 'ROLE_CUSTOMER' to a new user.
    Optional<Role> findByName(String name);
}