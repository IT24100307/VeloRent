package Group2.Car.Rental.System.service;

import Group2.Car.Rental.System.entity.User;
import Group2.Car.Rental.System.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

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
}
