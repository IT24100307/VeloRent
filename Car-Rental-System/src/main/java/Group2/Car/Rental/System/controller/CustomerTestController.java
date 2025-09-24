package Group2.Car.Rental.System.controller;

import Group2.Car.Rental.System.entity.Customer;
import Group2.Car.Rental.System.entity.Role;
import Group2.Car.Rental.System.entity.User;
import Group2.Car.Rental.System.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/test")
public class CustomerTestController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping("/create-test-customer")
    public ResponseEntity<Map<String, String>> createTestCustomer() {
        // Create a test user with customer details
        try {
            Role customerRole = new Role();
            customerRole.setId(2); // Using Integer instead of Long (2L)

            User user = User.builder()
                    .firstName("Test")
                    .lastName("Customer")
                    .email("testcustomer@example.com")
                    .password(passwordEncoder.encode("password"))
                    .role(customerRole)
                    .is2faEnabled(false)
                    .build();

            // Save user first to get ID
            User savedUser = userRepository.save(user);

            // Create customer with additional details
            Customer customer = Customer.builder()
                    .userId(savedUser.getId())
                    .contactNumber("123-456-7890")
                    .addressStreet("123 Test St")
                    .addressCity("Test City")
                    .addressPostalCode("12345")
                    .user(savedUser)
                    .build();

            // Set customer to user
            savedUser.setCustomer(customer);

            // Save user again to update relationship
            userRepository.save(savedUser);

            Map<String, String> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Test customer created with ID: " + savedUser.getId());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", "Failed to create test customer: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    @GetMapping("/customer-count")
    public ResponseEntity<Map<String, Object>> getCustomerCount() {
        Map<String, Object> response = new HashMap<>();

        try {
            long totalUsers = userRepository.count();
            long customersCount = userRepository.findAll().stream()
                    .filter(user -> user.getCustomer() != null)
                    .count();

            response.put("status", "success");
            response.put("totalUsers", totalUsers);
            response.put("customersCount", customersCount);

            if (customersCount > 0) {
                User firstCustomerUser = userRepository.findAll().stream()
                        .filter(user -> user.getCustomer() != null)
                        .findFirst().get();

                Map<String, Object> customerDetails = new HashMap<>();
                customerDetails.put("id", firstCustomerUser.getId());
                customerDetails.put("firstName", firstCustomerUser.getFirstName());
                customerDetails.put("lastName", firstCustomerUser.getLastName());
                customerDetails.put("email", firstCustomerUser.getEmail());

                Customer customer = firstCustomerUser.getCustomer();
                if (customer != null) {
                    customerDetails.put("contactNumber", customer.getContactNumber());
                    customerDetails.put("addressStreet", customer.getAddressStreet());
                }

                response.put("sampleCustomer", customerDetails);
            }

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Error counting customers: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
}
