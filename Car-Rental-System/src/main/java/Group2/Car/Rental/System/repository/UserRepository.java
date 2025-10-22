package Group2.Car.Rental.System.repository;

import Group2.Car.Rental.System.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    // This method allows you to find a user by their email address,
    // which is essential for login and checking for duplicate accounts.
    Optional<User> findByEmail(String email);

    // This method allows you to find users by their role ID
    List<User> findByRoleId(Integer roleId);

    // Recent users by id (proxy for registration time when createdAt not available)
    List<User> findTop20ByOrderByIdDesc();
}