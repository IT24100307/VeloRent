package Group2.Car.Rental.System.controller;

import Group2.Car.Rental.System.dto.BookingRequest;
import Group2.Car.Rental.System.dto.PackageBookingRequest;
import Group2.Car.Rental.System.entity.Booking;
import Group2.Car.Rental.System.entity.User;
import Group2.Car.Rental.System.repository.UserRepository;
import Group2.Car.Rental.System.service.BookingService;
import Group2.Car.Rental.System.service.PaymentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    private static final String SUCCESS_KEY = "success";
    private final BookingService bookingService;
    private final PaymentService paymentService;
    private final UserRepository userRepository;

    // Constructor injection instead of field injection
    public BookingController(BookingService bookingService, PaymentService paymentService, UserRepository userRepository) {
        this.bookingService = bookingService;
        this.paymentService = paymentService;
        this.userRepository = userRepository;
    }

    /**
     * Create a new booking
     * @param bookingRequest the booking details
     * @return response with booking details or error message
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> createBooking(@RequestBody BookingRequest bookingRequest) {
        Map<String, Object> response = bookingService.createBooking(bookingRequest);
        if (Boolean.TRUE.equals(response.get(SUCCESS_KEY))) {
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    /**
     * Get all bookings for a customer
     * @param customerId the customer's ID
     * @return list of bookings
     */
    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<Booking>> getCustomerBookings(@PathVariable Long customerId) {
        List<Booking> bookings = bookingService.getCustomerBookings(customerId);
        return ResponseEntity.ok(bookings);
    }

    /**
     * Get all bookings for a vehicle
     * @param vehicleId the vehicle's ID
     * @return list of bookings
     */
    @GetMapping("/vehicle/{vehicleId}")
    public ResponseEntity<List<Booking>> getVehicleBookings(@PathVariable Integer vehicleId) {
        List<Booking> bookings = bookingService.getVehicleBookings(vehicleId);
        return ResponseEntity.ok(bookings);
    }

    /**
     * Cancel a booking
     * @param bookingId the booking ID to cancel
     * @return response with success or error message
     */
    @PostMapping("/{bookingId}/cancel")
    public ResponseEntity<Map<String, Object>> cancelBooking(@PathVariable Integer bookingId) {
        Map<String, Object> response = bookingService.cancelBooking(bookingId);
        if (Boolean.TRUE.equals(response.get(SUCCESS_KEY))) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    /**
     * Return a booking (mark returned and free the vehicle)
     */
    @PostMapping("/{bookingId}/return")
    public ResponseEntity<Map<String, Object>> returnBooking(@PathVariable Integer bookingId) {
        Map<String, Object> response = bookingService.returnBooking(bookingId);
        if (Boolean.TRUE.equals(response.get(SUCCESS_KEY))) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    /**
     * Get lightweight booking summaries for a customer (safe for JSON)
     */
    @GetMapping("/customer/{customerId}/summary")
    public ResponseEntity<List<Group2.Car.Rental.System.dto.BookingSummaryDTO>> getCustomerBookingSummaries(@PathVariable Long customerId) {
        List<Group2.Car.Rental.System.dto.BookingSummaryDTO> bookings = bookingService.getCustomerBookingSummaries(customerId);
        return ResponseEntity.ok(bookings);
    }

    /**
     * Create a new package booking
     * @param packageBookingRequest the package booking details
     * @return response with booking details or error message
     */
    @PostMapping("/package")
    public ResponseEntity<Map<String, Object>> createPackageBooking(@RequestBody PackageBookingRequest packageBookingRequest) {
        try {
            // Get customer ID from authentication context
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();

            // Extract customer ID from the authenticated user
            Integer customerId = extractCustomerIdFromAuth(auth);

            if (customerId == null) {
                Map<String, Object> errorResponse = Map.of(
                    SUCCESS_KEY, false,
                    "message", "Customer ID not found. Please ensure you are properly logged in."
                );
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
            }

            Map<String, Object> response = bookingService.createPackageBooking(packageBookingRequest, customerId);
            if (Boolean.TRUE.equals(response.get(SUCCESS_KEY))) {
                return ResponseEntity.status(HttpStatus.CREATED).body(response);
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
        } catch (Exception e) {
            Map<String, Object> errorResponse = Map.of(
                SUCCESS_KEY, false,
                "message", "Error creating package booking: " + e.getMessage()
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Create a package booking with payment in a single transaction
     * @param request the combined booking and payment request
     * @return response with booking and payment details or error message
     */
    @PostMapping("/package/with-payment")
    public ResponseEntity<Map<String, Object>> createPackageBookingWithPayment(@RequestBody Map<String, Object> request) {
        try {
            // Get customer ID from authentication context
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            Integer customerId = extractCustomerIdFromAuth(auth);

            if (customerId == null) {
                Map<String, Object> errorResponse = Map.of(
                    SUCCESS_KEY, false,
                    "message", "Customer ID not found. Please ensure you are properly logged in."
                );
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
            }

            // Extract package booking data
            PackageBookingRequest packageBookingRequest = new PackageBookingRequest();
            packageBookingRequest.setPackageId(Integer.valueOf(request.get("packageId").toString()));
            packageBookingRequest.setStartDate(LocalDateTime.parse(request.get("startDate").toString()));
            packageBookingRequest.setEndDate(LocalDateTime.parse(request.get("endDate").toString()));

            // Extract payment method
            String paymentMethod = request.get("paymentMethod").toString();

            Map<String, Object> response = bookingService.createPackageBookingWithPayment(
                packageBookingRequest, customerId, paymentMethod);

            if (Boolean.TRUE.equals(response.get(SUCCESS_KEY))) {
                return ResponseEntity.status(HttpStatus.CREATED).body(response);
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
        } catch (Exception e) {
            Map<String, Object> errorResponse = Map.of(
                SUCCESS_KEY, false,
                "message", "Error creating package booking: " + e.getMessage()
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Extract customer ID from authentication context
     * This method looks up the user by email from the JWT token and returns their ID
     */
    private Integer extractCustomerIdFromAuth(Authentication auth) {
        try {
            String email = auth.getName(); // This is the user's email from JWT token
            
            if (email == null || email.isEmpty()) {
                throw new RuntimeException("No email found in authentication context");
            }

            // Look up the user by email to get their actual user ID
            Optional<User> userOptional = userRepository.findByEmail(email);
            
            if (userOptional.isEmpty()) {
                throw new RuntimeException("User not found with email: " + email);
            }
            
            User user = userOptional.get();
            return user.getId().intValue();

        } catch (Exception e) {
            throw new RuntimeException("Failed to extract customer ID from authentication: " + e.getMessage(), e);
        }
    }

    /**
     * Get package bookings for a customer
     * @param customerId the customer's ID
     * @return list of package bookings
     */
    @GetMapping("/package/customer/{customerId}")
    public ResponseEntity<List<Booking>> getCustomerPackageBookings(@PathVariable Integer customerId) {
        try {
            List<Booking> bookings = bookingService.getPackageBookingsByCustomer(customerId);
            return ResponseEntity.ok(bookings);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(List.of());
        }
    }

    /**
     * Process payment for a package booking
     * @param bookingId the booking ID
     * @param paymentRequest the payment details
     * @return payment processing result
     */
    @PostMapping("/package/{bookingId}/payment")
    public ResponseEntity<Map<String, Object>> processPackagePayment(
            @PathVariable Integer bookingId,
            @RequestBody Map<String, String> paymentRequest) {
        try {
            String paymentMethod = paymentRequest.get("paymentMethod");
            String transactionId = paymentRequest.get("transactionId");

            Map<String, Object> response = paymentService.processPackagePayment(bookingId, paymentMethod, transactionId);

            if (Boolean.TRUE.equals(response.get(SUCCESS_KEY))) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
        } catch (Exception e) {
            Map<String, Object> errorResponse = Map.of(
                SUCCESS_KEY, false,
                "message", "Error processing payment: " + e.getMessage()
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
}
