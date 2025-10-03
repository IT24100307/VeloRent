package Group2.Car.Rental.System.service;

import Group2.Car.Rental.System.dto.BookingRequest;
import Group2.Car.Rental.System.entity.Booking;
import Group2.Car.Rental.System.entity.Customer;
import Group2.Car.Rental.System.entity.Vehicle;
import Group2.Car.Rental.System.repository.BookingRepository;
import Group2.Car.Rental.System.repository.CustomerRepository;
import Group2.Car.Rental.System.repository.VehicleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class BookingService {

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private VehicleRepository vehicleRepository;

    @Autowired
    private CustomerRepository customerRepository;

    /**
     * Create a new booking
     * @param bookingRequest the booking request DTO
     * @return a map containing success status and the created booking or error message
     */
    @Transactional
    public Map<String, Object> createBooking(BookingRequest bookingRequest) {
        Map<String, Object> response = new HashMap<>();

        try {
            // Get the vehicle
            Optional<Vehicle> vehicleOptional = vehicleRepository.findById(bookingRequest.getVehicleId());
            if (!vehicleOptional.isPresent()) {
                response.put("success", false);
                response.put("message", "Vehicle not found");
                return response;
            }

            Vehicle vehicle = vehicleOptional.get();

            // Check if vehicle is available
            if (!"Available".equals(vehicle.getStatus())) {
                response.put("success", false);
                response.put("message", "Vehicle is not available for booking");
                return response;
            }

            // Get the customer (convert Integer to Long for customer ID)
            Long customerId = bookingRequest.getCustomerId().longValue();
            Optional<Customer> customerOptional = customerRepository.findByUserId(customerId);
            if (!customerOptional.isPresent()) {
                response.put("success", false);
                response.put("message", "Customer not found");
                return response;
            }

            Customer customer = customerOptional.get();

            // Check if there are overlapping bookings
            LocalDateTime startDate = bookingRequest.getStartDate();
            LocalDateTime endDate = bookingRequest.getEndDate();

            List<Booking> overlappingBookings = bookingRepository.findByVehicleAndStartDateBeforeAndEndDateAfterAndBookingStatusNotIn(
                vehicle, endDate, startDate, java.util.Arrays.asList("Cancelled", "Returned"));

            if (!overlappingBookings.isEmpty()) {
                response.put("success", false);
                response.put("message", "Vehicle is already booked for the selected dates");
                return response;
            }

            // Create new booking
            Booking newBooking = new Booking();
            newBooking.setStartDate(startDate);
            newBooking.setEndDate(endDate);
            newBooking.setTotalCost(bookingRequest.getTotalCost());
            newBooking.setCustomer(customer);
            newBooking.setVehicle(vehicle);
            newBooking.setBookingStatus("Confirmed");

            // Save the booking
            Booking savedBooking = bookingRepository.save(newBooking);

            // Update vehicle status
            vehicle.setStatus("Booked");
            vehicleRepository.save(vehicle);

            response.put("success", true);
            // Return a lightweight booking payload to avoid serialization issues
            Map<String, Object> bookingPayload = new HashMap<>();
            bookingPayload.put("bookingId", savedBooking.getBookingId());
            bookingPayload.put("startDate", savedBooking.getStartDate());
            bookingPayload.put("endDate", savedBooking.getEndDate());
            bookingPayload.put("bookingStatus", savedBooking.getBookingStatus());
            bookingPayload.put("totalCost", savedBooking.getTotalCost());
            bookingPayload.put("vehicleId", vehicle.getVehicleId());
            bookingPayload.put("customerId", customer.getUserId());
            response.put("booking", bookingPayload);
            response.put("message", "Booking successfully created");

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error creating booking: " + e.getMessage());
        }

        return response;
    }

    /**
     * Get all bookings for a specific customer
     * @param customerId the customer's ID
     * @return list of bookings
     */
    public List<Booking> getCustomerBookings(Long customerId) {
        Optional<Customer> customerOptional = customerRepository.findByUserId(customerId);
        if (customerOptional.isPresent()) {
            return bookingRepository.findByCustomer(customerOptional.get());
        }
        return List.of();
    }

    /**
     * Get all bookings for a specific vehicle
     * @param vehicleId the vehicle ID
     * @return list of bookings
     */
    public List<Booking> getVehicleBookings(Integer vehicleId) {
        Optional<Vehicle> vehicleOptional = vehicleRepository.findById(vehicleId);
        if (vehicleOptional.isPresent()) {
            return bookingRepository.findByVehicle(vehicleOptional.get());
        }
        return List.of();
    }

    /**
     * Cancel a booking
     * @param bookingId the booking ID to cancel
     * @return a map containing success status and message
     */
    @Transactional
    public Map<String, Object> cancelBooking(Integer bookingId) {
        Map<String, Object> response = new HashMap<>();

        try {
            Optional<Booking> bookingOptional = bookingRepository.findById(bookingId);
            if (!bookingOptional.isPresent()) {
                response.put("success", false);
                response.put("message", "Booking not found");
                return response;
            }

            Booking booking = bookingOptional.get();
            booking.setBookingStatus("Cancelled");
            bookingRepository.save(booking);

            // Update vehicle status back to available
            Vehicle vehicle = booking.getVehicle();
            vehicle.setStatus("Available");
            vehicleRepository.save(vehicle);

            response.put("success", true);
            response.put("message", "Booking cancelled successfully");

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error cancelling booking: " + e.getMessage());
        }

        return response;
    }

    /**
     * Mark a booking as returned and set vehicle status back to Available
     */
    @Transactional
    public Map<String, Object> returnBooking(Integer bookingId) {
        Map<String, Object> response = new HashMap<>();
        try {
            Optional<Booking> bookingOptional = bookingRepository.findById(bookingId);
            if (!bookingOptional.isPresent()) {
                response.put("success", false);
                response.put("message", "Booking not found");
                return response;
            }

            Booking booking = bookingOptional.get();

            if ("Returned".equalsIgnoreCase(booking.getBookingStatus())) {
                response.put("success", true);
                response.put("message", "Vehicle already returned");
                // return lightweight payload
                Map<String, Object> retPayload = new HashMap<>();
                retPayload.put("bookingId", booking.getBookingId());
                retPayload.put("bookingStatus", booking.getBookingStatus());
                Vehicle v = booking.getVehicle();
                retPayload.put("vehicleId", v != null ? v.getVehicleId() : null);
                response.put("booking", retPayload);
                return response;
            }

            // Update booking status
            booking.setBookingStatus("Returned");
            bookingRepository.save(booking);

            // Update vehicle status to available
            Vehicle vehicle = booking.getVehicle();
            if (vehicle != null) {
                vehicle.setStatus("Available");
                vehicleRepository.save(vehicle);
            }

            response.put("success", true);
            response.put("message", "Vehicle returned successfully");
            // Lightweight payload
            Map<String, Object> retPayload = new HashMap<>();
            retPayload.put("bookingId", booking.getBookingId());
            retPayload.put("bookingStatus", booking.getBookingStatus());
            retPayload.put("vehicleId", vehicle != null ? vehicle.getVehicleId() : null);
            response.put("booking", retPayload);
            return response;
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error returning vehicle: " + e.getMessage());
            return response;
        }
    }

    // Build lightweight booking summaries for safe JSON serialization
    public List<Group2.Car.Rental.System.dto.BookingSummaryDTO> getCustomerBookingSummaries(Long customerId) {
        Optional<Customer> customerOptional = customerRepository.findByUserId(customerId);
        if (customerOptional.isEmpty()) {
            return java.util.Collections.emptyList();
        }
        Customer customer = customerOptional.get();
        List<Booking> bookings = bookingRepository.findByCustomer(customer);
        java.util.ArrayList<Group2.Car.Rental.System.dto.BookingSummaryDTO> result = new java.util.ArrayList<>();
        for (Booking b : bookings) {
            Vehicle v = b.getVehicle();
            Group2.Car.Rental.System.dto.BookingSummaryDTO dto = new Group2.Car.Rental.System.dto.BookingSummaryDTO(
                    b.getBookingId(),
                    b.getStartDate(),
                    b.getEndDate(),
                    b.getBookingStatus(),
                    b.getTotalCost(),
                    v != null ? v.getVehicleId() : null,
                    v != null ? v.getMake() : null,
                    v != null ? v.getModel() : null,
                    v != null ? v.getRegistrationNumber() : null,
                    v != null ? v.getImageUrl() : null
            );
            result.add(dto);
        }
        return result;
    }
}
