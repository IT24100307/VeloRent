package Group2.Car.Rental.System.service;

import Group2.Car.Rental.System.dto.ChangePasswordDto;
import Group2.Car.Rental.System.dto.UserProfileDTO;
import Group2.Car.Rental.System.entity.Customer;
import Group2.Car.Rental.System.entity.User;
import Group2.Car.Rental.System.repository.CustomerRepository;
import Group2.Car.Rental.System.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public User getUserByEmail(String email) {
        // Handle the "anonymousUser" special case used by Spring Security for unauthenticated users
        if ("anonymousUser".equals(email)) {
            return null;
        }

        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));
    }

    /**
     * Check if a user exists by email without throwing an exception
     */
    public boolean existsByEmail(String email) {
        if ("anonymousUser".equals(email)) {
            return false;
        }
        return userRepository.findByEmail(email).isPresent();
    }

    public User saveUser(User user) {
        return userRepository.save(user);
    }

    public void deleteUser(Long userId) {
        userRepository.deleteById(userId);
    }

    /**
     * Get user profile information including Customer details if applicable
     */
    public UserProfileDTO getUserProfile(String email) {
        User user = getUserByEmail(email);
        UserProfileDTO profileDto = new UserProfileDTO();

        // Set basic user information
        profileDto.setFirstName(user.getFirstName());
        profileDto.setLastName(user.getLastName());
        profileDto.setEmail(user.getEmail());

        // Get customer-specific details if the user is a customer
        if (user.getCustomer() != null) {
            Customer customer = user.getCustomer();
            profileDto.setContactNumber(customer.getContactNumber());
            profileDto.setAddressStreet(customer.getAddressStreet());
            profileDto.setAddressCity(customer.getAddressCity());
            profileDto.setAddressPostalCode(customer.getAddressPostalCode());
        }

        return profileDto;
    }

    /**
     * Update user profile information
     */
    @Transactional
    public void updateUserProfile(String email, UserProfileDTO profileDto) {
        try {
            User user = getUserByEmail(email);
            System.out.println("Updating profile for user: " + email);

            // Update basic user information
            user.setFirstName(profileDto.getFirstName());
            user.setLastName(profileDto.getLastName());

            // Save the basic user information first
            userRepository.save(user);
            System.out.println("Saved basic user information for: " + email);

            // Update customer-specific details if applicable
            // Check role in a more flexible way to handle different role formats (CUSTOMER, ROLE_CUSTOMER, etc.)
            if (user.getRole() != null) {
                String roleName = user.getRole().getName();
                System.out.println("User role: " + roleName);

                // Check if the user has a customer role (more flexible check)
                boolean isCustomer = roleName != null &&
                    (roleName.equals("CUSTOMER") || roleName.contains("CUSTOMER") || roleName.equals("ROLE_CUSTOMER"));

                if (isCustomer || user.getCustomer() != null) {
                    Customer customer = user.getCustomer();
                    System.out.println("User has customer role or existing customer record");

                    if (customer == null) {
                        System.out.println("Creating new customer record");
                        customer = new Customer();
                        customer.setUser(user);
                        customer.setUserId(user.getId());
                        user.setCustomer(customer);
                    } else {
                        System.out.println("Updating existing customer record");
                    }

                    customer.setContactNumber(profileDto.getContactNumber());
                    customer.setAddressStreet(profileDto.getAddressStreet());
                    customer.setAddressCity(profileDto.getAddressCity());
                    customer.setAddressPostalCode(profileDto.getAddressPostalCode());

                    customerRepository.save(customer);
                    System.out.println("Saved customer information");
                }
            } else {
                System.out.println("User has no role defined");
            }

            System.out.println("Profile updated successfully for: " + email);
        } catch (Exception e) {
            System.err.println("Error updating profile for " + email + ": " + e.getMessage());
            e.printStackTrace();
            throw e; // Re-throw the exception to be handled by the controller
        }
    }

    /**
     * Change user password with verification of the current password
     */
    public void changePassword(String email, ChangePasswordDto passwordDto) {
        User user = getUserByEmail(email);

        if (user == null) {
            throw new IllegalArgumentException("User not found");
        }

        if (passwordDto == null || !StringUtils.hasText(passwordDto.getCurrentPassword())
                || !StringUtils.hasText(passwordDto.getNewPassword())
                || !StringUtils.hasText(passwordDto.getConfirmPassword())) {
            throw new IllegalArgumentException("All password fields are required");
        }

        // Verify that new password and confirm password match
        if (!passwordDto.getNewPassword().equals(passwordDto.getConfirmPassword())) {
            throw new IllegalArgumentException("New password and confirm password do not match");
        }

        // Optional: basic strength check
        if (passwordDto.getNewPassword().length() < 8) {
            throw new IllegalArgumentException("New password must be at least 8 characters long");
        }

        String stored = user.getPassword();
        boolean currentMatches = false;

        // 1) Try standard encoded match
        if (StringUtils.hasText(stored) && passwordEncoder.matches(passwordDto.getCurrentPassword(), stored)) {
            currentMatches = true;
        } else {
            // 2) Fallback: legacy plain-text password migration
            // If stored password looks not encoded (no bcrypt prefix) and equals the provided current password
            boolean looksPlain = stored != null && !stored.startsWith("$2a$") && !stored.startsWith("$2b$") && !stored.startsWith("$2y$");
            if (looksPlain && stored != null && stored.equals(passwordDto.getCurrentPassword())) {
                currentMatches = true;
                // Immediately migrate to BCrypt using the new password below
            }
        }

        if (!currentMatches) {
            throw new BadCredentialsException("Current password is incorrect");
        }

        // Encode and save new password
        user.setPassword(passwordEncoder.encode(passwordDto.getNewPassword()));
        userRepository.save(user);
    }
}
