package Group2.Car.Rental.System.service;

import Group2.Car.Rental.System.dto.BookingRequest;
import Group2.Car.Rental.System.dto.PackageBookingRequest;
import Group2.Car.Rental.System.dto.BookingResponse;
import Group2.Car.Rental.System.entity.Booking;
import Group2.Car.Rental.System.entity.Customer;
import Group2.Car.Rental.System.entity.Payment;
import Group2.Car.Rental.System.entity.Vehicle;
import Group2.Car.Rental.System.entity.VehiclePackage;
import Group2.Car.Rental.System.repository.BookingRepository;
import Group2.Car.Rental.System.repository.CustomerRepository;
import Group2.Car.Rental.System.repository.PaymentRepository;
import Group2.Car.Rental.System.repository.VehicleRepository;
import Group2.Car.Rental.System.repository.VehiclePackageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
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

    @Autowired
    private VehiclePackageRepository vehiclePackageRepository;

    @Autowired
    private PaymentRepository paymentRepository;

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
     * Create a new package booking
     * @param packageBookingRequest the package booking request DTO
     * @param customerId the customer ID from authentication
     * @return a map containing success status and the created booking or error message
     */
    @Transactional
    public Map<String, Object> createPackageBooking(PackageBookingRequest packageBookingRequest, Integer customerId) {
        Map<String, Object> response = new HashMap<>();

        try {
            // Get the package
            Optional<VehiclePackage> packageOptional = vehiclePackageRepository.findById(packageBookingRequest.getPackageId());
            if (!packageOptional.isPresent()) {
                response.put("success", false);
                response.put("message", "Package not found");
                return response;
            }

            VehiclePackage vehiclePackage = packageOptional.get();

            // Check if package is active
            if (!"Activated".equalsIgnoreCase(vehiclePackage.getStatus())) {
                response.put("success", false);
                response.put("message", "Package is not available for booking");
                return response;
            }

            // Get the customer
            Optional<Customer> customerOptional = customerRepository.findByUserId(customerId.longValue());
            if (!customerOptional.isPresent()) {
                response.put("success", false);
                response.put("message", "Customer not found");
                return response;
            }

            Customer customer = customerOptional.get();

            // Validate dates
            if (packageBookingRequest.getStartDate().isAfter(packageBookingRequest.getEndDate())) {
                response.put("success", false);
                response.put("message", "Start date must be before end date");
                return response;
            }

            // Compare only the date part, not the time, to allow bookings for today
            if (packageBookingRequest.getStartDate().toLocalDate().isBefore(LocalDateTime.now().toLocalDate())) {
                response.put("success", false);
                response.put("message", "Start date cannot be in the past");
                return response;
            }

            // Calculate total cost based on package duration and price
            long daysBetween = ChronoUnit.DAYS.between(
                packageBookingRequest.getStartDate().toLocalDate(),
                packageBookingRequest.getEndDate().toLocalDate()
            );

            // For packages, we can either charge per day or use the fixed package price
            // Here I'm using the package price as base and multiply by days if needed
            BigDecimal totalCost = vehiclePackage.getPrice();
            if (daysBetween > vehiclePackage.getDuration()) {
                // If booking is longer than package duration, calculate additional cost
                BigDecimal dailyRate = vehiclePackage.getPrice().divide(BigDecimal.valueOf(vehiclePackage.getDuration()));
                BigDecimal additionalDays = BigDecimal.valueOf(daysBetween - vehiclePackage.getDuration());
                totalCost = totalCost.add(dailyRate.multiply(additionalDays));
            }

            // Create the booking
            Booking booking = new Booking(
                packageBookingRequest.getStartDate(),
                packageBookingRequest.getEndDate(),
                totalCost,
                customer,
                vehiclePackage
            );

            // Set initial status to "Pending Payment" for package bookings
            booking.setBookingStatus("Pending Payment");

            // Save the booking
            Booking savedBooking = bookingRepository.save(booking);

            // Mark all vehicles in the package as "Rented" to remove them from available vehicles list
            if (vehiclePackage.getVehicles() != null && !vehiclePackage.getVehicles().isEmpty()) {
                for (Vehicle packageVehicle : vehiclePackage.getVehicles()) {
                    packageVehicle.setStatus("Rented");
                    vehicleRepository.save(packageVehicle);
                }
            }

            // Create response
            BookingResponse bookingResponse = new BookingResponse(
                savedBooking.getBookingId(),
                "PACKAGE",
                savedBooking.getStartDate(),
                savedBooking.getEndDate(),
                savedBooking.getTotalCost(),
                savedBooking.getBookingStatus(),
                customer.getUser().getFirstName() + " " + customer.getUser().getLastName(),
                vehiclePackage.getPackageName(),
                savedBooking.getCreatedAt()
            );

            response.put("success", true);
            response.put("message", "Package booking created successfully");
            response.put("booking", bookingResponse);

            return response;

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error creating booking: " + e.getMessage());
            return response;
        }
    }

    /**
     * Create a package booking with payment in a single transaction
     * @param packageBookingRequest the package booking request DTO
     * @param customerId the customer ID from authentication
     * @param paymentMethod the payment method selected
     * @return a map containing success status and the created booking with payment details
     */
    @Transactional
    public Map<String, Object> createPackageBookingWithPayment(PackageBookingRequest packageBookingRequest,
                                                              Integer customerId, String paymentMethod) {
        Map<String, Object> response = new HashMap<>();

        try {
            // Get the package
            Optional<VehiclePackage> packageOptional = vehiclePackageRepository.findById(packageBookingRequest.getPackageId());
            if (!packageOptional.isPresent()) {
                response.put("success", false);
                response.put("message", "Package not found");
                return response;
            }

            VehiclePackage vehiclePackage = packageOptional.get();

            // Check if package is active
            if (!"Activated".equalsIgnoreCase(vehiclePackage.getStatus())) {
                response.put("success", false);
                response.put("message", "Package is not available for booking");
                return response;
            }

            // Get the customer
            Optional<Customer> customerOptional = customerRepository.findByUserId(customerId.longValue());
            if (!customerOptional.isPresent()) {
                response.put("success", false);
                response.put("message", "Customer not found");
                return response;
            }

            Customer customer = customerOptional.get();

            // Validate dates
            if (packageBookingRequest.getStartDate().isAfter(packageBookingRequest.getEndDate())) {
                response.put("success", false);
                response.put("message", "Start date must be before end date");
                return response;
            }

            // Compare only the date part, not the time, to allow bookings for today
            if (packageBookingRequest.getStartDate().toLocalDate().isBefore(LocalDateTime.now().toLocalDate())) {
                response.put("success", false);
                response.put("message", "Start date cannot be in the past");
                return response;
            }

            // Calculate total cost based on package duration and price
            long daysBetween = ChronoUnit.DAYS.between(
                packageBookingRequest.getStartDate().toLocalDate(),
                packageBookingRequest.getEndDate().toLocalDate()
            );

            // For packages, we can either charge per day or use the fixed package price
            BigDecimal totalCost = vehiclePackage.getPrice();
            if (daysBetween > vehiclePackage.getDuration()) {
                // If booking is longer than package duration, calculate additional cost
                BigDecimal dailyRate = vehiclePackage.getPrice().divide(BigDecimal.valueOf(vehiclePackage.getDuration()));
                BigDecimal additionalDays = BigDecimal.valueOf(daysBetween - vehiclePackage.getDuration());
                totalCost = totalCost.add(dailyRate.multiply(additionalDays));
            }

            // Create the booking with final status based on payment method
            Booking booking = new Booking(
                packageBookingRequest.getStartDate(),
                packageBookingRequest.getEndDate(),
                totalCost,
                customer,
                vehiclePackage
            );

            // Set appropriate status based on payment method
            if ("cash".equalsIgnoreCase(paymentMethod)) {
                booking.setBookingStatus("Payment Pending");
            } else {
                booking.setBookingStatus("Confirmed");
            }

            // Save the booking first
            Booking savedBooking = bookingRepository.save(booking);

            // Create and save the payment in the same transaction
            Payment payment = new Payment();
            payment.setPaymentDate(LocalDateTime.now());
            payment.setAmount(totalCost);
            payment.setPaymentMethod(paymentMethod);
            payment.setPaymentStatus("Completed");
            payment.setBooking(savedBooking);

            Payment savedPayment = paymentRepository.save(payment);

            // Mark all vehicles in the package as "Rented" to remove them from available vehicles list
            if (vehiclePackage.getVehicles() != null && !vehiclePackage.getVehicles().isEmpty()) {
                for (Vehicle packageVehicle : vehiclePackage.getVehicles()) {
                    packageVehicle.setStatus("Rented");
                    vehicleRepository.save(packageVehicle);
                }
            }

            // Create response with both booking and payment details
            Map<String, Object> bookingPayload = new HashMap<>();
            bookingPayload.put("bookingId", savedBooking.getBookingId());
            bookingPayload.put("startDate", savedBooking.getStartDate());
            bookingPayload.put("endDate", savedBooking.getEndDate());
            bookingPayload.put("bookingStatus", savedBooking.getBookingStatus());
            bookingPayload.put("totalCost", savedBooking.getTotalCost());
            bookingPayload.put("packageId", vehiclePackage.getPackageId());
            bookingPayload.put("packageName", vehiclePackage.getPackageName());
            bookingPayload.put("customerId", customer.getUserId());

            Map<String, Object> paymentDetails = new HashMap<>();
            paymentDetails.put("paymentId", savedPayment.getPaymentId());
            paymentDetails.put("amount", savedPayment.getAmount());
            paymentDetails.put("paymentMethod", savedPayment.getPaymentMethod());
            paymentDetails.put("paymentDate", savedPayment.getPaymentDate());

            response.put("success", true);
            response.put("message", "Package booking and payment processed successfully");
            response.put("booking", bookingPayload);
            response.put("payment", paymentDetails);

            return response;

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error creating package booking: " + e.getMessage());
            e.printStackTrace();
            return response;
        }
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
     * Get all package bookings for a customer
     * @param customerId the customer ID
     * @return list of package bookings
     */
    public List<Booking> getPackageBookingsByCustomer(Integer customerId) {
        return bookingRepository.findPackageBookingsByCustomer(customerId);
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
     * Get booking by ID
     * @param bookingId the booking ID
     * @return booking if found
     */
    public Optional<Booking> getBookingById(Integer bookingId) {
        return bookingRepository.findById(bookingId);
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
                response.put("message", "Booking already returned");
                return response;
            }

            // Update booking status
            booking.setBookingStatus("Returned");
            bookingRepository.save(booking);

            // Handle both vehicle and package bookings
            if (booking.getVehicle() != null) {
                // Regular vehicle booking - make vehicle available
                Vehicle vehicle = booking.getVehicle();
                vehicle.setStatus("Available");
                vehicleRepository.save(vehicle);
                response.put("message", "Vehicle returned successfully");
            } else if (booking.getVehiclePackage() != null) {
                // Package booking - make all package vehicles available
                VehiclePackage vehiclePackage = booking.getVehiclePackage();
                if (vehiclePackage.getVehicles() != null && !vehiclePackage.getVehicles().isEmpty()) {
                    for (Vehicle packageVehicle : vehiclePackage.getVehicles()) {
                        packageVehicle.setStatus("Available");
                        vehicleRepository.save(packageVehicle);
                    }
                }
                response.put("message", "Package returned successfully - all vehicles are now available");
            }

            response.put("success", true);

            // Lightweight payload
            Map<String, Object> retPayload = new HashMap<>();
            retPayload.put("bookingId", booking.getBookingId());
            retPayload.put("bookingStatus", booking.getBookingStatus());
            retPayload.put("bookingType", booking.getVehicle() != null ? "VEHICLE" : "PACKAGE");
            response.put("booking", retPayload);

            return response;
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error returning booking: " + e.getMessage());
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
            Group2.Car.Rental.System.dto.BookingSummaryDTO dto;

            // Check if this is a package booking or vehicle booking
            if ("PACKAGE".equals(b.getBookingType()) && b.getVehiclePackage() != null) {
                // Handle package booking
                VehiclePackage pkg = b.getVehiclePackage();

                // Create vehicle info list for the package
                java.util.List<Group2.Car.Rental.System.dto.BookingSummaryDTO.VehicleInfo> vehicleInfoList =
                    new java.util.ArrayList<>();

                for (Vehicle v : pkg.getVehicles()) {
                    vehicleInfoList.add(new Group2.Car.Rental.System.dto.BookingSummaryDTO.VehicleInfo(
                        v.getVehicleId(),
                        v.getMake(),
                        v.getModel(),
                        v.getRegistrationNumber(),
                        v.getImageUrl()
                    ));
                }

                // Create package booking DTO
                dto = new Group2.Car.Rental.System.dto.BookingSummaryDTO(
                    b.getBookingId(),
                    b.getStartDate(),
                    b.getEndDate(),
                    b.getBookingStatus(),
                    b.getTotalCost(),
                    pkg.getPackageId(),
                    pkg.getPackageName(),
                    pkg.getImageUrl(),
                    pkg.getDuration(),
                    vehicleInfoList
                );
            } else {
                // Handle individual vehicle booking
                Vehicle v = b.getVehicle();
                dto = new Group2.Car.Rental.System.dto.BookingSummaryDTO(
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
            }

            result.add(dto);
        }
        return result;
    }
}
