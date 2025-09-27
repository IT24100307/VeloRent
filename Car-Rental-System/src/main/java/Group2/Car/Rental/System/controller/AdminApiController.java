package Group2.Car.Rental.System.controller;

import Group2.Car.Rental.System.dto.CustomerDTO;
import Group2.Car.Rental.System.entity.Customer;
import Group2.Car.Rental.System.entity.User;
import Group2.Car.Rental.System.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/admin")
public class AdminApiController {

    private static final Logger logger = LoggerFactory.getLogger(AdminApiController.class);

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/customers")
    public ResponseEntity<List<CustomerDTO>> getAllCustomers() {
        // Get all users with customer information
        List<User> allUsers = userRepository.findAll();
        logger.info("Total users found: {}", allUsers.size());

        List<User> users = allUsers.stream()
                .filter(user -> user.getCustomer() != null)
                .collect(Collectors.toList());

        logger.info("Users with customer data: {}", users.size());

        // Convert to DTOs
        List<CustomerDTO> customerDTOs = users.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        logger.info("CustomerDTOs created: {}", customerDTOs.size());

        return ResponseEntity.ok(customerDTOs);
    }

    @GetMapping("/customers/{id}")
    public ResponseEntity<CustomerDTO> getCustomerById(@PathVariable Long id) {
        logger.info("Fetching customer with ID: {}", id);
        Optional<User> userOptional = userRepository.findById(id);

        if (userOptional.isPresent() && userOptional.get().getCustomer() != null) {
            logger.info("Customer found with ID: {}", id);
            return ResponseEntity.ok(convertToDTO(userOptional.get()));
        }

        logger.warn("Customer not found with ID: {}", id);
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/customers/{id}")
    public ResponseEntity<Void> deleteCustomer(@PathVariable Long id) {
        logger.info("Request to delete customer with ID: {}", id);
        Optional<User> userOptional = userRepository.findById(id);

        if (userOptional.isPresent() && userOptional.get().getCustomer() != null) {
            User user = userOptional.get();
            logger.info("Deleting customer with ID: {}", id);
            userRepository.delete(user);
            return ResponseEntity.ok().build();
        }

        logger.warn("Customer not found with ID: {}", id);
        return ResponseEntity.notFound().build();
    }

    @PutMapping("/customers/{id}")
    public ResponseEntity<?> updateCustomer(@PathVariable Long id, @RequestBody CustomerDTO customerDTO) {
        logger.info("Request to update customer with ID: {}", id);
        try {
            // Find the user
            Optional<User> userOptional = userRepository.findById(id);
            if (!userOptional.isPresent()) {
                logger.warn("Customer not found with ID: {}", id);
                return ResponseEntity.notFound().build();
            }

            User user = userOptional.get();

            // Update user information
            user.setFirstName(customerDTO.getFirstName());
            user.setLastName(customerDTO.getLastName());
            user.setEmail(customerDTO.getEmail());

            // Get or create customer information
            Customer customer = user.getCustomer();
            if (customer == null) {
                customer = new Customer();
                customer.setUserId(user.getId());
                customer.setUser(user);
                user.setCustomer(customer);
            }

            // Update customer details
            customer.setContactNumber(customerDTO.getContactNumber());
            customer.setAddressStreet(customerDTO.getAddressStreet());
            customer.setAddressCity(customerDTO.getAddressCity());
            customer.setAddressPostalCode(customerDTO.getAddressPostalCode());

            // Save updated user (cascade should save customer too)
            User savedUser = userRepository.save(user);

            logger.info("Customer updated successfully: {}", id);
            return ResponseEntity.ok(convertToDTO(savedUser));
        } catch (Exception e) {
            logger.error("Error updating customer with ID: {}", id, e);
            return ResponseEntity.badRequest().body("Failed to update customer: " + e.getMessage());
        }
    }

    private CustomerDTO convertToDTO(User user) {
        Customer customer = user.getCustomer();

        CustomerDTO.CustomerDTOBuilder builder = CustomerDTO.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail());

        // Only add customer details if customer is not null
        if (customer != null) {
            builder.contactNumber(customer.getContactNumber())
                  .addressStreet(customer.getAddressStreet())
                  .addressCity(customer.getAddressCity())
                  .addressPostalCode(customer.getAddressPostalCode());
        }

        return builder.build();
    }
}
