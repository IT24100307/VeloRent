package Group2.Car.Rental.System.config;

import Group2.Car.Rental.System.repository.UserRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final UserRepository userRepository;

    public SecurityConfig(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // This Bean defines the security rules for HTTP requests.
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Disable CSRF protection for API endpoints
                .csrf(csrf -> csrf
                        .ignoringRequestMatchers("/api/auth/**", "/api/test/**", "/api/admin/**",
                                                "/api/vehicles/**", "/api/profile/**", "/profile/api/**",
                                                "/api/payments/**", "/api/bookings/**", "/api/fleet/**")) // added fleet API
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/**").permitAll() // Allow public access to auth endpoints
                        .requestMatchers("/api/test/**").permitAll() // Allow access to test endpoints
                        .requestMatchers("/api/admin/**").permitAll()
                        .requestMatchers("/api/admin/feedback/**").permitAll()// Temporarily allow access to admin endpoints for debugging
                        .requestMatchers("/api/feedback/**").permitAll()// Temporarily allow access to admin endpoints for debugging
                        .requestMatchers("/api/admin/offers/**").permitAll()// Temporarily allow access to admin endpoints for debugging
                        .requestMatchers("/api/vehicles/**").permitAll() // Allow access to vehicle endpoints
                        .requestMatchers("/api/fleet/**").permitAll() // Allow access to fleet manager package APIs

                        .requestMatchers("/api/payments/**").permitAll() // Allow access to payment endpoints
                        .requestMatchers("/api/bookings/**").permitAll() // Allow access to booking endpoints

                        .requestMatchers("/api/profile/**").permitAll()

                        .requestMatchers("/", "/test", "/css/**", "/js/**", "/images/**", "/login",
                                "/register", "/forgot-password", "/reset-password",
                                "/verify-2fa", "/security-settings", "/dashboard", "/admin/dashboard", "/admin/offers",
                                "/admin/system-dashboard", "/admin/owner-dashboard", "/admin/fleet-dashboard",
                                "/fleet-manager/dashboard", "/fleet-manager/packages", "/feedback", "/admin/feedback",
                                "/profile", "/profile/**", "/payment", "/rental-history")
                        .permitAll() // Allow public access to UI pages
                        .anyRequest().authenticated() // Secure all other requests
                )
                // Use session when required so authenticated users remain logged in for API calls
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
                .authenticationProvider(authenticationProvider());

        return http.build();
    }

    // This Bean tells Spring Security how to find a user by their email.
    @Bean
    public UserDetailsService userDetailsService() {
        return username -> userRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    // This Bean is the authentication provider that uses the UserDetailsService and
    // PasswordEncoder.
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider(passwordEncoder());
        authProvider.setUserDetailsService(userDetailsService());
        return authProvider;
    }

    // This Bean is required for the login process in your AuthService.
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    // This Bean is used to hash passwords securely.
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
