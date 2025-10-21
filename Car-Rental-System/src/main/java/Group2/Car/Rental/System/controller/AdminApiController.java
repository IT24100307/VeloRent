package Group2.Car.Rental.System.controller;

import Group2.Car.Rental.System.dto.CustomerDTO;
import Group2.Car.Rental.System.dto.UserDTO;
import Group2.Car.Rental.System.entity.Customer;
import Group2.Car.Rental.System.entity.Role;
import Group2.Car.Rental.System.entity.User;
import Group2.Car.Rental.System.repository.RoleRepository;
import Group2.Car.Rental.System.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
}
