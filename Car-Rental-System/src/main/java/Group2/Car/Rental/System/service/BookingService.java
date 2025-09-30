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

            List<Booking> overlappingBookings = bookingRepository.findByVehicleAndStartDateBeforeAndEndDateAfterAndBookingStatusNot(
                vehicle, endDate, startDate, "Cancelled");

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
            response.put("booking", savedBooking);
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
}
