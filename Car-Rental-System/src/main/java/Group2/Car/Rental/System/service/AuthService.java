package Group2.Car.Rental.System.service;

import Group2.Car.Rental.System.dto.LoginDto;
import Group2.Car.Rental.System.dto.RegisterDto;
import Group2.Car.Rental.System.dto.ResetPasswordDto;
import Group2.Car.Rental.System.entity.Role;
import Group2.Car.Rental.System.entity.User;
import Group2.Car.Rental.System.repository.RoleRepository;
import Group2.Car.Rental.System.repository.UserRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final TwoFactorAuthService twoFactorAuthService;

    private final Map<String, String> otpStore = new HashMap<>();

    public AuthService(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder,
            AuthenticationManager authenticationManager, JwtService jwtService,
            TwoFactorAuthService twoFactorAuthService) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.twoFactorAuthService = twoFactorAuthService;
    }

    public void register(RegisterDto registerDto) {
        if (userRepository.findByEmail(registerDto.getEmail()).isPresent()) {
            throw new RuntimeException("Error: Email is already in use!");
        }

        User user = new User();
        user.setFirstName(registerDto.getFirstName());
        user.setLastName(registerDto.getLastName());
        user.setEmail(registerDto.getEmail());
        user.setPassword(passwordEncoder.encode(registerDto.getPassword()));

        // Handle role assignment based on roleName with registration code validation
        String roleName = registerDto.getRoleName();

        // Validate registration code for admin roles
        if (!"ROLE_CUSTOMER".equals(roleName)) {
            String registrationCode = registerDto.getRegistrationCode();

            // Validate registration code based on the selected role
            switch (roleName) {
                case "ROLE_FLEET_MANAGER":
                    if (!"FLEET_MGR_SECRET".equals(registrationCode)) {
                        throw new RuntimeException("Invalid registration code for Fleet Manager role");
                    }
                    break;
                case "ROLE_SYSTEM_ADMIN":
                    if (!"SYS_ADMIN_SECRET".equals(registrationCode)) {
                        throw new RuntimeException("Invalid registration code for System Admin role");
                    }
                    break;
                case "ROLE_OWNER":
                    if (!"OWNER_SECRET".equals(registrationCode)) {
                        throw new RuntimeException("Invalid registration code for Owner role");
                    }
                    break;
                default:
                    throw new RuntimeException("Invalid role selected");
            }
        }

        // Find the selected role in the database
        Role selectedRole = roleRepository.findByName(roleName)
                .orElseThrow(() -> new RuntimeException("Error: Selected role not found."));
        user.setRole(selectedRole);

        userRepository.save(user);
    }

    public Map<String, String> login(LoginDto loginDto) {
        authenticationManager
                .authenticate(new UsernamePasswordAuthenticationToken(loginDto.getEmail(), loginDto.getPassword()));
        User user = userRepository.findByEmail(loginDto.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Map<String, String> response = new HashMap<>();

        if (user.is2faEnabled()) {
            response.put("status", "2FA required");
            return response;
        }

        String token = jwtService.generateToken(user);
        response.put("token", token);

        // Check user role and add it to the response
        String roleName = user.getRole().getName();
        response.put("role", roleName);

        // Determine the redirect URL based on role
        if (roleName.equals("ROLE_CUSTOMER")) {
            response.put("redirect", "/dashboard");
        } else {
            // For admin roles (ROLE_FLEET_MANAGER, ROLE_OWNER, ROLE_SYSTEM_ADMIN)
            response.put("redirect", "/admin/dashboard");
        }

        return response;
    }

    public String forgotPassword(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with this email."));
        if (!user.is2faEnabled()) {
            throw new RuntimeException(
                    "Two-factor authentication is not enabled for this account. Please contact support.");
        }
        return "Please enter the verification code from your authenticator app to reset your password.";
    }

    public String resetPassword(ResetPasswordDto resetPasswordDto) {
        User user = userRepository.findByEmail(resetPasswordDto.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found with this email."));

        // Verify the 2FA code instead of using email OTP
        if (!user.is2faEnabled()) {
            throw new RuntimeException(
                    "Two-factor authentication is not enabled for this account. Please contact support.");
        }

        if (!twoFactorAuthService.isOtpValid(user.getSecret(), resetPasswordDto.getOtp())) {
            throw new RuntimeException("Invalid verification code. Please try again.");
        }

        user.setPassword(passwordEncoder.encode(resetPasswordDto.getNewPassword()));
        userRepository.save(user);
        return "Password has been reset successfully.";
    }

    public String setup2FA(String email) {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));
        String secret = twoFactorAuthService.generateNewSecret();
        user.setSecret(secret);
        userRepository.save(user);
        return twoFactorAuthService.generateQrCodeImageUri(secret);
    }

    public void enable2FA(String email, String code) {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));
        if (!twoFactorAuthService.isOtpValid(user.getSecret(), code)) {
            throw new RuntimeException("Invalid OTP code.");
        }
        user.set2faEnabled(true);
        userRepository.save(user);
    }

    public String verify2FA(String email, String code) {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));
        if (!twoFactorAuthService.isOtpValid(user.getSecret(), code)) {
            throw new RuntimeException("Invalid OTP code.");
        }
        return jwtService.generateToken(user);
    }

    public String extractEmailFromToken(String token) {
        return jwtService.extractEmailFromToken(token);
    }
}