package Group2.Car.Rental.System.controller;

import Group2.Car.Rental.System.dto.CustomerDTO;
import Group2.Car.Rental.System.dto.LoginHistoryDTO;
import Group2.Car.Rental.System.dto.UserDTO;
import Group2.Car.Rental.System.entity.Customer;
import Group2.Car.Rental.System.entity.LoginHistory;
import Group2.Car.Rental.System.entity.Role;
import Group2.Car.Rental.System.entity.User;
import Group2.Car.Rental.System.entity.Booking;
import Group2.Car.Rental.System.entity.Payment;
import Group2.Car.Rental.System.entity.Feedback;
import Group2.Car.Rental.System.repository.LoginHistoryRepository;
import Group2.Car.Rental.System.repository.RoleRepository;
import Group2.Car.Rental.System.repository.UserRepository;
import Group2.Car.Rental.System.repository.CustomerRepository;
import Group2.Car.Rental.System.repository.BookingRepository;
import Group2.Car.Rental.System.repository.PaymentRepository;
import Group2.Car.Rental.System.repository.FeedbackRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private FeedbackRepository feedbackRepository;
    
    @Autowired
    private LoginHistoryRepository loginHistoryRepository;

    @Autowired
    private Group2.Car.Rental.System.service.AuthService authService;

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

    // Create an admin user (Fleet Manager, System Admin, or Owner)
    @PostMapping("/users")
    public ResponseEntity<?> createAdminUser(@RequestBody Group2.Car.Rental.System.dto.CreateAdminUserDTO dto) {
        try {
            // Reuse registration flow but skip registration codes by directly calling service with a tailored RegisterDto
            Group2.Car.Rental.System.dto.RegisterDto reg = new Group2.Car.Rental.System.dto.RegisterDto();
            reg.setFirstName(dto.getFirstName());
            reg.setLastName(dto.getLastName());
            reg.setEmail(dto.getEmail());
            reg.setPassword(dto.getPassword());
            reg.setRoleName(dto.getRoleName());
            // Allow passing optional staffIdCode
            reg.setStaffIdCode(dto.getStaffIdCode());

            // The AuthService.register enforces registration codes for admin roles.
            // For admin-created accounts, set valid codes implicitly by role.
            switch (dto.getRoleName()) {
                case "ROLE_FLEET_MANAGER":
                    reg.setRegistrationCode("FLEET_MGR_SECRET");
                    break;
                case "ROLE_SYSTEM_ADMIN":
                    reg.setRegistrationCode("SYS_ADMIN_SECRET");
                    break;
                case "ROLE_OWNER":
                    reg.setRegistrationCode("OWNER_SECRET");
                    break;
                default:
                    return ResponseEntity.badRequest().body("Invalid role. Use ROLE_FLEET_MANAGER, ROLE_SYSTEM_ADMIN, or ROLE_OWNER");
            }

            authService.register(reg);
            return ResponseEntity.ok().body(Map.of("success", true));
        } catch (Exception ex) {
            logger.error("Failed to create admin user: {}", ex.getMessage(), ex);
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", ex.getMessage()));
        }
    }

    @GetMapping("/users/fleet-managers")
    public ResponseEntity<List<UserDTO>> getAllFleetManagers() {
        logger.info("Fetching all fleet managers");
        Optional<Role> roleOptional = roleRepository.findByName("ROLE_FLEET_MANAGER");

        if (!roleOptional.isPresent()) {
            logger.warn("Fleet Manager role not found");
            return ResponseEntity.ok(List.of());
        }

        List<User> fleetManagers = userRepository.findByRoleId(roleOptional.get().getId());
        logger.info("Found {} fleet managers", fleetManagers.size());

        List<UserDTO> userDTOs = fleetManagers.stream()
                .map(this::convertToUserDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(userDTOs);
    }

    @GetMapping("/users/system-admins")
    public ResponseEntity<List<UserDTO>> getAllSystemAdmins() {
        logger.info("Fetching all system admins");
        Optional<Role> roleOptional = roleRepository.findByName("ROLE_SYSTEM_ADMIN");

        if (!roleOptional.isPresent()) {
            logger.warn("System Admin role not found");
            return ResponseEntity.ok(List.of());
        }

        List<User> systemAdmins = userRepository.findByRoleId(roleOptional.get().getId());
        logger.info("Found {} system admins", systemAdmins.size());

        List<UserDTO> userDTOs = systemAdmins.stream()
                .map(this::convertToUserDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(userDTOs);
    }

    @GetMapping("/users/owners")
    public ResponseEntity<List<UserDTO>> getAllOwners() {
        logger.info("Fetching all owners");
        Optional<Role> roleOptional = roleRepository.findByName("ROLE_OWNER");

        if (!roleOptional.isPresent()) {
            logger.warn("Owner role not found");
            return ResponseEntity.ok(List.of());
        }

        List<User> owners = userRepository.findByRoleId(roleOptional.get().getId());
        logger.info("Found {} owners", owners.size());

        List<UserDTO> userDTOs = owners.stream()
                .map(this::convertToUserDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(userDTOs);
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<UserDTO> getUserById(@PathVariable Long id) {
        logger.info("Fetching user with ID: {}", id);
        Optional<User> userOptional = userRepository.findById(id);

        if (userOptional.isPresent()) {
            logger.info("User found with ID: {}", id);
            return ResponseEntity.ok(convertToUserDTO(userOptional.get()));
        }

        logger.warn("User not found with ID: {}", id);
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/customers/{id}")
    @Transactional
    public ResponseEntity<Void> deleteCustomer(@PathVariable Long id) {
        logger.info("Request to delete customer with ID: {}", id);
        Optional<User> userOptional = userRepository.findById(id);

        if (userOptional.isEmpty()) {
            logger.warn("Customer not found with ID: {}", id);
            return ResponseEntity.notFound().build();
        }

        User user = userOptional.get();
        if (user.getCustomer() == null) {
            logger.warn("User with ID: {} has no customer profile; cannot delete via customer endpoint", id);
            return ResponseEntity.badRequest().build();
        }

        try {
            Customer customer = user.getCustomer();

            // 1) Remove payments for all of this customer's bookings
            logger.info("Fetching bookings for customer {}", id);
            java.util.List<Booking> bookings = bookingRepository.findByCustomer(customer);
            for (Booking b : bookings) {
                Payment payment = paymentRepository.findByBooking(b);
                if (payment != null) {
                    logger.info("Deleting payment {} for booking {}", payment.getPaymentId(), b.getBookingId());
                    paymentRepository.delete(payment);
                }
            }

            // 2) Delete bookings
            if (!bookings.isEmpty()) {
                logger.info("Deleting {} bookings for customer {}", bookings.size(), id);
                bookingRepository.deleteAll(bookings);
            }

            // 3) Delete feedbacks linked to this user (customer_id references users table)
            java.util.List<Feedback> feedbacks = feedbackRepository.findByCustomerId(id);
            if (!feedbacks.isEmpty()) {
                logger.info("Deleting {} feedback entries for customer {}", feedbacks.size(), id);
                feedbackRepository.deleteAll(feedbacks);
            }

            // 4) Delete customer profile explicitly (though cascade exists) before user
            logger.info("Deleting customer profile for user {}", id);
            customerRepository.delete(customer);

            // 5) Finally, delete the user record
            logger.info("Deleting user record {}", id);
            userRepository.delete(user);

            return ResponseEntity.ok().build();
        } catch (Exception ex) {
            logger.error("Failed to delete customer {} due to: {}", id, ex.getMessage(), ex);
            // Transaction will auto-rollback on RuntimeException
            return ResponseEntity.internalServerError().build();
        }
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

    // Method to convert User to UserDTO
    private UserDTO convertToUserDTO(User user) {
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setEmail(user.getEmail());
        dto.setRole(user.getRole().getName());

        // Get staff information if available
        if (user.getStaff() != null) {
            dto.setStaffIdCode(user.getStaff().getStaffIdCode());
            dto.setHireDate(user.getStaff().getHireDate());
        }

        return dto;
    }

    @PutMapping("/users/{id}/role")
    public ResponseEntity<?> updateUserRole(@PathVariable Long id, @RequestBody Map<String, Integer> payload) {
        Integer roleId = payload.get("roleId");
        logger.info("Updating role for user ID: {} to role ID: {}", id, roleId);

        if (roleId == null) {
            return ResponseEntity.badRequest().body("Role ID is required");
        }

        Optional<User> userOptional = userRepository.findById(id);
        Optional<Role> roleOptional = roleRepository.findById(roleId);

        if (userOptional.isPresent() && roleOptional.isPresent()) {
            User user = userOptional.get();
            Role newRole = roleOptional.get();

            // Update user's role
            user.setRole(newRole);
            userRepository.save(user);

            logger.info("Role updated successfully for user ID: {}", id);
            return ResponseEntity.ok(convertToUserDTO(user));
        }

        logger.warn("Failed to update role. User or role not found.");
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/roles")
    public ResponseEntity<List<Map<String, Object>>> getAllRoles() {
        List<Role> roles = roleRepository.findAll();
        List<Map<String, Object>> roleList = roles.stream()
            .filter(role -> !role.getName().equals("ROLE_CUSTOMER")) // Exclude customer role
            .map(role -> {
                Map<String, Object> roleMap = new HashMap<>();
                roleMap.put("id", role.getId());
                roleMap.put("name", role.getName());
                return roleMap;
            })
            .collect(Collectors.toList());

        return ResponseEntity.ok(roleList);
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        logger.info("Request to delete user with ID: {}", id);
        Optional<User> userOptional = userRepository.findById(id);

        if (userOptional.isPresent()) {
            User user = userOptional.get();
            // Check if user is an admin (not a customer)
            if (user.getRole() != null && !user.getRole().getName().equals("ROLE_CUSTOMER")) {
                logger.info("Deleting admin user with ID: {}", id);
                userRepository.delete(user);
                return ResponseEntity.ok().build();
            } else {
                logger.warn("Cannot delete customer user through admin endpoint. Use customer endpoint instead.");
                return ResponseEntity.badRequest().build();
            }
        }

        logger.warn("User not found with ID: {}", id);
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/login-history/test")
    public ResponseEntity<String> testLoginHistoryEndpoint() {
        try {
            logger.info("Testing login history endpoint");
            return ResponseEntity.ok("Login history endpoint is working");
        } catch (Exception e) {
            logger.error("Error in test endpoint: ", e);
            return ResponseEntity.internalServerError().body("Error: " + e.getMessage());
        }
    }

    @GetMapping("/login-history/reset-table")
    public ResponseEntity<String> resetLoginHistoryTable() {
        try {
            logger.info("Resetting login_history table");
            
            // Drop the table first (this will handle the schema change)
            loginHistoryRepository.deleteAll();
            logger.info("Cleared all login history data");
            
            return ResponseEntity.ok("Login history table reset successfully. The table structure will be recreated automatically.");
        } catch (Exception e) {
            logger.error("Error resetting table: ", e);
            return ResponseEntity.internalServerError().body("Error: " + e.getMessage());
        }
    }

    @GetMapping("/login-history/create-test-data")
    public ResponseEntity<String> createTestLoginHistory() {
        try {
            logger.info("Creating test login history data");
            
            // Find the first user to create test data
            List<User> users = userRepository.findAll();
            if (users.isEmpty()) {
                return ResponseEntity.ok("No users found to create test data");
            }
            
            User testUser = users.get(0);
            String accountType = "Test User";
            if (testUser.getRole() != null) {
                switch (testUser.getRole().getName()) {
                    case "ROLE_CUSTOMER":
                        accountType = "Customer";
                        break;
                    case "ROLE_FLEET_MANAGER":
                        accountType = "Fleet Manager";
                        break;
                    case "ROLE_SYSTEM_ADMIN":
                        accountType = "System Admin";
                        break;
                    case "ROLE_OWNER":
                        accountType = "Owner";
                        break;
                }
            }
            
            LoginHistory testLogin = LoginHistory.builder()
                    .user(testUser)
                    .username(testUser.getFirstName() + " " + testUser.getLastName())
                    .accountType(accountType)
                    .build();
            
            loginHistoryRepository.save(testLogin);
            logger.info("Test login history created for user: {}", testUser.getEmail());
            
            return ResponseEntity.ok("Test login history created for user: " + testUser.getEmail());
        } catch (Exception e) {
            logger.error("Error creating test data: ", e);
            return ResponseEntity.internalServerError().body("Error: " + e.getMessage());
        }
    }

    @GetMapping("/login-history")
    public ResponseEntity<List<LoginHistoryDTO>> getLoginHistory(
            @RequestParam(value = "days", defaultValue = "7") int days) {
        try {
            logger.info("=== LOGIN HISTORY API CALLED ===");
            logger.info("Fetching login history for last {} days", days);
            
            // Check if repository exists
            if (loginHistoryRepository == null) {
                logger.error("LoginHistoryRepository is null!");
                return ResponseEntity.internalServerError().body(Collections.emptyList());
            }
            
            logger.info("Repository is available, querying database...");
            LocalDateTime sinceDate = LocalDateTime.now().minusDays(days);
            logger.info("Querying since date: {}", sinceDate);
            
            List<LoginHistory> loginHistories = loginHistoryRepository.findRecentLogins(sinceDate);
            logger.info("Database query completed. Found {} login history records", loginHistories.size());
            
            if (loginHistories.isEmpty()) {
                logger.info("No login history records found, returning empty list");
                return ResponseEntity.ok(Collections.emptyList());
            }
            
            logger.info("Converting {} records to DTOs...", loginHistories.size());
            List<LoginHistoryDTO> loginHistoryDTOs = loginHistories.stream()
                    .map(this::convertToLoginHistoryDTO)
                    .collect(Collectors.toList());
            
            logger.info("Successfully converted to {} DTOs", loginHistoryDTOs.size());
            logger.info("=== API CALL SUCCESSFUL ===");
            return ResponseEntity.ok(loginHistoryDTOs);
        } catch (Exception e) {
            logger.error("=== ERROR IN LOGIN HISTORY API ===");
            logger.error("Error type: {}", e.getClass().getSimpleName());
            logger.error("Error message: {}", e.getMessage());
            logger.error("Full stack trace: ", e);
            logger.error("=== END ERROR LOG ===");
            return ResponseEntity.internalServerError().body(Collections.emptyList());
        }
    }

    private LoginHistoryDTO convertToLoginHistoryDTO(LoginHistory loginHistory) {
        User user = loginHistory.getUser();
        
        return new LoginHistoryDTO(
                loginHistory.getLoginId(),
                user.getId(),
                loginHistory.getUsername(),
                loginHistory.getAccountType(),
                loginHistory.getLoginTime()
        );
    }
}
