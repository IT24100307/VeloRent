package Group2.Car.Rental.System.service;

import Group2.Car.Rental.System.dto.UserProfileDTO;
import Group2.Car.Rental.System.entity.Customer;
import Group2.Car.Rental.System.entity.Staff;
import Group2.Car.Rental.System.entity.User;
import Group2.Car.Rental.System.repository.CustomerRepository;
import Group2.Car.Rental.System.repository.StaffRepository;
import Group2.Car.Rental.System.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.regex.Pattern;

@Service
public class UserProfileService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private StaffRepository staffRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private static final String EMAIL_REGEX = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
    private static final Pattern EMAIL_PATTERN = Pattern.compile(EMAIL_REGEX);

    // Password must be at least 8 characters and contain at least one digit, one lowercase, one uppercase letter, and one special character
    private static final String PASSWORD_REGEX = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[!@#$%^&*()-+=])(?=\\S+$).{8,}$";
    private static final Pattern PASSWORD_PATTERN = Pattern.compile(PASSWORD_REGEX);

    /**
     * Gets the user profile by email
     */
    public UserProfileDTO getUserProfile(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        return createUserProfileDTO(user);
    }

    /**
     * Gets the user profile by ID
     */
    public UserProfileDTO getUserProfileById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        return createUserProfileDTO(user);
    }

    /**
     * Updates the user profile
     */
    @Transactional
    public UserProfileDTO updateUserProfile(UserProfileDTO profileDTO) {
        if (profileDTO == null || profileDTO.getUserId() == null) {
            throw new IllegalArgumentException("Invalid profile data: missing required fields");
        }

        User user = userRepository.findById(profileDTO.getUserId())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        // Update basic user information with validation
        if (profileDTO.getFirstName() == null || profileDTO.getFirstName().trim().isEmpty()) {
            throw new IllegalArgumentException("First name cannot be empty");
        }
        user.setFirstName(profileDTO.getFirstName().trim());

        if (profileDTO.getLastName() == null || profileDTO.getLastName().trim().isEmpty()) {
            throw new IllegalArgumentException("Last name cannot be empty");
        }
        user.setLastName(profileDTO.getLastName().trim());

        // Only update email if it's valid and different
        if (profileDTO.getEmail() != null && !profileDTO.getEmail().trim().isEmpty() &&
            !user.getEmail().equalsIgnoreCase(profileDTO.getEmail().trim())) {
            String newEmail = profileDTO.getEmail().trim();
            validateEmail(newEmail, user.getId());
            user.setEmail(newEmail);
        }

        // Update password if provided and valid
        if (profileDTO.getPassword() != null && !profileDTO.getPassword().isEmpty()) {
            validatePassword(profileDTO.getPassword());
            user.setPassword(passwordEncoder.encode(profileDTO.getPassword()));
        }

        // Update customer information if user is a customer
        if (user.getCustomer() != null) {
            Customer customer = user.getCustomer();
            customer.setContactNumber(profileDTO.getContactNumber());
            customer.setAddressStreet(profileDTO.getAddressStreet());
            customer.setAddressCity(profileDTO.getAddressCity());
            customer.setAddressPostalCode(profileDTO.getAddressPostalCode());
            customerRepository.save(customer);
        }

        // Update staff information if user is a staff member
        if (user.getStaff() != null) {
            Staff staff = user.getStaff();
            staff.setStaffIdCode(profileDTO.getStaffIdCode());
            staff.setDepartment(profileDTO.getDepartment());
            staff.setPosition(profileDTO.getPosition());
            staff.setEmployeeId(profileDTO.getEmployeeId());
            staffRepository.save(staff);
        }

        // Save the user and return updated profile
        User updatedUser = userRepository.save(user);
        return createUserProfileDTO(updatedUser);
    }

    /**
     * Creates a DTO from a User entity
     */
    private UserProfileDTO createUserProfileDTO(User user) {
        UserProfileDTO dto = new UserProfileDTO();

        // Set core user information
        dto.setUserId(user.getId());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setEmail(user.getEmail());
        dto.set2faEnabled(user.is2faEnabled());

        // Set role name
        if (user.getRole() != null) {
            dto.setRoleName(user.getRole().getName());
        }

        // Set customer information if exists
        Customer customer = user.getCustomer();
        if (customer != null) {
            dto.setCustomer(true);
            dto.setContactNumber(customer.getContactNumber());
            dto.setAddressStreet(customer.getAddressStreet());
            dto.setAddressCity(customer.getAddressCity());
            dto.setAddressPostalCode(customer.getAddressPostalCode());
        }

        // Set staff information if exists
        Staff staff = user.getStaff();
        if (staff != null) {
            dto.setStaff(true);
            dto.setStaffIdCode(staff.getStaffIdCode());
            dto.setDepartment(staff.getDepartment());
            dto.setPosition(staff.getPosition());
            dto.setEmployeeId(staff.getEmployeeId());
        }

        return dto;
    }

    /**
     * Validates email format
     */
    private void validateEmail(String email, Long userId) {
        if (email == null || !EMAIL_PATTERN.matcher(email).matches()) {
            throw new IllegalArgumentException("Invalid email format");
        }

        // Check if email is already in use by another user
        userRepository.findByEmail(email).ifPresent(existingUser -> {
            if (!existingUser.getId().equals(userId)) {
                throw new IllegalArgumentException("Email already in use");
            }
        });
    }

    /**
     * Validates password strength
     */
    private void validatePassword(String password) {
        if (password == null || !PASSWORD_PATTERN.matcher(password).matches()) {
            throw new IllegalArgumentException(
                "Password must be at least 8 characters and contain at least one digit, " +
                "one lowercase, one uppercase letter, and one special character"
            );
        }
    }
}
