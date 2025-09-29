package Group2.Car.Rental.System.service;

import Group2.Car.Rental.System.dto.UserProfileDto;
import Group2.Car.Rental.System.entity.Customer;
import Group2.Car.Rental.System.entity.User;
import Group2.Car.Rental.System.repository.CustomerRepository;
import Group2.Car.Rental.System.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProfileService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CustomerRepository customerRepository;

    /**
     * Get a user by email
     */
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));
    }

    /**
     * Get user profile information
     */
    public UserProfileDto getUserProfile(String email) {
        User user = getUserByEmail(email);
        UserProfileDto profileDto = new UserProfileDto();

        // Set basic user information
        profileDto.setFirstName(user.getFirstName());
        profileDto.setLastName(user.getLastName());
        profileDto.setEmail(user.getEmail());

        // Get customer-specific details if available
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
     * Update user profile information with more reliable handling
     */
    @Transactional
    public void updateProfile(String email, UserProfileDto profileDto) {
        try {
            System.out.println("ProfileService: Starting profile update for email: " + email);
            System.out.println("ProfileService: Profile data: " + profileDto);

            User user = getUserByEmail(email);
            System.out.println("ProfileService: Retrieved user: " + user.getId() + ", " + user.getEmail());

            // 1. Update and save the basic user information
            user.setFirstName(profileDto.getFirstName());
            user.setLastName(profileDto.getLastName());
            System.out.println("ProfileService: Updated name to: " + profileDto.getFirstName() + " " + profileDto.getLastName());

            try {
                user = userRepository.save(user);
                System.out.println("ProfileService: Successfully saved basic user info");
            } catch (Exception e) {
                System.err.println("ProfileService: Error saving user: " + e.getMessage());
                e.printStackTrace();
                throw new RuntimeException("Failed to save user information: " + e.getMessage(), e);
            }

            // 2. Handle customer information if it exists
            Customer customer = user.getCustomer();
            System.out.println("ProfileService: Customer record exists: " + (customer != null));

            // If this is a customer (has customer details in the DTO)
            if (profileDto.getContactNumber() != null ||
                profileDto.getAddressStreet() != null ||
                profileDto.getAddressCity() != null ||
                profileDto.getAddressPostalCode() != null) {

                System.out.println("ProfileService: Customer details provided, processing customer data");

                // Create customer record if it doesn't exist
                if (customer == null) {
                    System.out.println("ProfileService: Creating new customer record");
                    customer = new Customer();
                    customer.setUserId(user.getId());
                    customer.setUser(user);
                } else {
                    System.out.println("ProfileService: Updating existing customer with ID: " + customer.getUserId());
                }

                // Update customer fields
                customer.setContactNumber(profileDto.getContactNumber());
                customer.setAddressStreet(profileDto.getAddressStreet());
                customer.setAddressCity(profileDto.getAddressCity());
                customer.setAddressPostalCode(profileDto.getAddressPostalCode());

                try {
                    // Save customer record
                    customerRepository.save(customer);
                    System.out.println("ProfileService: Successfully saved customer information");
                } catch (Exception e) {
                    System.err.println("ProfileService: Error saving customer: " + e.getMessage());
                    e.printStackTrace();
                    throw new RuntimeException("Failed to save customer information: " + e.getMessage(), e);
                }
            } else {
                System.out.println("ProfileService: No customer-specific details to update");
            }

            System.out.println("ProfileService: Profile update completed successfully for: " + email);
        } catch (Exception e) {
            System.err.println("ProfileService: Unhandled exception in updateProfile: " + e.getMessage());
            e.printStackTrace();
            throw e; // Re-throw to be handled by the controller
        }
    }
}
