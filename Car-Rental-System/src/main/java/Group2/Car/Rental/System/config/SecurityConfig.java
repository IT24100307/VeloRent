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

import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.http.HttpMethod;

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
                        "/api/payments/**", "/api/bookings/**", "/api/fleet/**",
                        "/api/upload/**", "/api/feedback/**")) // allow feedback form posts without CSRF token
        .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/**").permitAll() // Allow public access to auth endpoints
                        .requestMatchers("/api/test/**").permitAll() // Allow access to test endpoints
                        .requestMatchers("/api/admin/**").permitAll()
                        .requestMatchers("/api/admin/feedback/**").permitAll()// Temporarily allow access to admin endpoints for debugging
                        .requestMatchers("/api/feedback/**").permitAll()// Temporarily allow access to admin endpoints for debugging
                        .requestMatchers("/api/admin/offers/**").permitAll()// Temporarily allow access to admin endpoints for debugging
                        .requestMatchers("/api/vehicles/**").permitAll() // Allow access to vehicle endpoints
                        .requestMatchers("/api/fleet/**").permitAll() // Allow access to fleet manager package APIs
            // Allow public GET access to images so <img> tags can load without Authorization header
            .requestMatchers(HttpMethod.GET, "/api/upload/images/**").permitAll()
            // All other upload operations (POST/PUT/DELETE) require authentication
            .requestMatchers(HttpMethod.POST, "/api/upload/**").authenticated()
            .requestMatchers(HttpMethod.PUT, "/api/upload/**").authenticated()
            .requestMatchers(HttpMethod.DELETE, "/api/upload/**").authenticated()
                        .requestMatchers("/fleet-manager/**").permitAll() // Allow access to all fleet manager pages
                        .requestMatchers("/api/public/**").permitAll() // Allow access to public endpoints

                        .requestMatchers("/api/payments/**").permitAll() // Allow access to payment endpoints
                        .requestMatchers("/api/bookings/**").authenticated() // Require authentication for booking endpoints

                        .requestMatchers("/api/profile/**").permitAll()

                        .requestMatchers("/", "/test", "/css/**", "/js/**", "/images/**", "/uploads/**", "/login",
                                "/register", "/forgot-password", "/reset-password",
                                "/verify-2fa", "/security-settings", "/dashboard", "/admin/dashboard", "/admin/offers",
                                "/admin/system-dashboard", "/admin/owner-dashboard", "/admin/fleet-dashboard",
                                "/fleet-manager/dashboard", "/fleet-manager/packages", "/fleet-manager/vehicle-usage-history", 
                                "/feedback", "/admin/feedback", "/profile", "/profile/**", "/payment", "/rental-history")
                        .permitAll() // Allow public access to UI pages
                        .anyRequest().authenticated() // Secure all other requests
                )
                // Use stateless session for JWT
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);

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
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setPasswordEncoder(passwordEncoder());
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

    // Create JWT Authentication Filter bean
    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter(jwtService(), userDetailsService());
    }

    // Create JWT Service bean
    @Bean
    public Group2.Car.Rental.System.service.JwtService jwtService() {
        return new Group2.Car.Rental.System.service.JwtService();
    }
}
