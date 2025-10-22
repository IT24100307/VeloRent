package Group2.Car.Rental.System.repository;

import Group2.Car.Rental.System.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {
    Optional<Customer> findByUserId(Long userId);

    // Fetch all customers with their linked User eagerly to avoid LazyInitialization
    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = {"user"})
    @org.springframework.data.jpa.repository.Query("select c from Customer c")
    java.util.List<Customer> findAllWithUser();
}
