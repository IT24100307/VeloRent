package Group2.Car.Rental.System.controller;

import Group2.Car.Rental.System.dto.BookingRequest;
import Group2.Car.Rental.System.entity.Booking;
import Group2.Car.Rental.System.service.BookingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    @Autowired
    private BookingService bookingService;

    /**
     * Create a new booking
     * @param bookingRequest the booking details
     * @return response with booking details or error message
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> createBooking(@RequestBody BookingRequest bookingRequest) {
        Map<String, Object> response = bookingService.createBooking(bookingRequest);
        if ((Boolean) response.get("success")) {
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
        if ((Boolean) response.get("success")) {
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
        if ((Boolean) response.get("success")) {
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
}
