package Group2.Car.Rental.System.controller;

import Group2.Car.Rental.System.dto.UserProfileDTO;
import Group2.Car.Rental.System.entity.Customer;
import Group2.Car.Rental.System.entity.User;
import Group2.Car.Rental.System.repository.CustomerRepository;
import Group2.Car.Rental.System.repository.UserRepository;
import Group2.Car.Rental.System.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/account")
public class AccountController {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @GetMapping("/profile")
    public ResponseEntity<UserProfileDTO> getUserProfile() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();

        try {
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            Customer customer = customerRepository.findByUserId(user.getId()).orElse(null);

            UserProfileDTO profileDTO = new UserProfileDTO();
            profileDTO.setFirstName(user.getFirstName());
            profileDTO.setLastName(user.getLastName());
            profileDTO.setEmail(user.getEmail());

            if (customer != null) {
                profileDTO.setPhone(customer.getContactNumber());
                profileDTO.setAddressStreet(customer.getAddressStreet());
                profileDTO.setAddressCity(customer.getAddressCity());
                profileDTO.setAddressPostalCode(customer.getAddressPostalCode());
            }

            return ResponseEntity.ok(profileDTO);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @PutMapping("/profile")
    public ResponseEntity<UserProfileDTO> updateUserProfile(@RequestBody UserProfileDTO profileDTO) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();

        try {
            // Get the current user by email
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // Update user basic information
            user.setFirstName(profileDTO.getFirstName());
            user.setLastName(profileDTO.getLastName());
            // Keep the existing email for now to avoid authentication issues
            // user.setEmail(profileDTO.getEmail());

            // Save updated user info
            userRepository.save(user);

            // Update or create customer information
            Customer customer = customerRepository.findByUserId(user.getId()).orElse(new Customer());
            if (customer.getUserId() == null) {
                customer.setUserId(user.getId());
                customer.setUser(user);
            }

            // Update customer details
            customer.setContactNumber(profileDTO.getPhone());
            customer.setAddressStreet(profileDTO.getAddressStreet());
            customer.setAddressCity(profileDTO.getAddressCity());
            customer.setAddressPostalCode(profileDTO.getAddressPostalCode());

            // Save updated customer info
            customerRepository.save(customer);

            // Return updated profile info
            UserProfileDTO updatedProfile = new UserProfileDTO();
            updatedProfile.setFirstName(user.getFirstName());
            updatedProfile.setLastName(user.getLastName());
            updatedProfile.setEmail(user.getEmail());
            updatedProfile.setPhone(customer.getContactNumber());
            updatedProfile.setAddressStreet(customer.getAddressStreet());
            updatedProfile.setAddressCity(customer.getAddressCity());
            updatedProfile.setAddressPostalCode(customer.getAddressPostalCode());

            return ResponseEntity.ok(updatedProfile);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
}
