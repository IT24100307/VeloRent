package Group2.Car.Rental.System.service;

import Group2.Car.Rental.System.dto.BookingRequest;
import Group2.Car.Rental.System.dto.PackageBookingRequest;
import Group2.Car.Rental.System.dto.BookingResponse;
import Group2.Car.Rental.System.entity.Booking;
import Group2.Car.Rental.System.entity.Customer;
import Group2.Car.Rental.System.entity.Payment;
import Group2.Car.Rental.System.entity.User;
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
import java.util.stream.Collectors;

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

    @Autowired
    private VehiclePackageService vehiclePackageService;

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

            // Mark packages containing this vehicle as partially reserved (still visible but not bookable)
            vehiclePackageService.markPackagesAsPartiallyReserved(vehicle.getVehicleId());

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

            // Check if package is available for booking (not partially reserved)
            if (!vehiclePackageService.isPackageAvailableForBooking(packageBookingRequest.getPackageId())) {
                response.put("success", false);
                response.put("message", vehiclePackageService.getPackageAvailabilityMessage(packageBookingRequest.getPackageId()));
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

            // Validate all vehicles in package are available
            if (vehiclePackage.getVehicles() != null) {
                for (Vehicle vehicle : vehiclePackage.getVehicles()) {
                    if (!"Available".equals(vehicle.getStatus())) {
                        response.put("success", false);
                        response.put("message", "One or more vehicles in the package are currently unavailable");
                        return response;
                    }
                }
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

            // Check if package is available for booking (not partially reserved)
            if (!vehiclePackageService.isPackageAvailableForBooking(packageBookingRequest.getPackageId())) {
                response.put("success", false);
                response.put("message", vehiclePackageService.getPackageAvailabilityMessage(packageBookingRequest.getPackageId()));
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
            
            // Restore packages containing this vehicle from partial reservation
            vehiclePackageService.restorePackagesFromPartialReservation(vehicle.getVehicleId());

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
                
                // Restore packages containing this vehicle from partial reservation
                vehiclePackageService.restorePackagesFromPartialReservation(vehicle.getVehicleId());
                
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

    /**
     * Get vehicle usage history for fleet manager dashboard
     * @return list of vehicle usage history DTOs
     */
    public List<Group2.Car.Rental.System.dto.VehicleUsageHistoryDTO> getVehicleUsageHistory() {
        List<Group2.Car.Rental.System.dto.VehicleUsageHistoryDTO> usageHistory = new java.util.ArrayList<>();
        
        try {
            List<Vehicle> allVehicles = vehicleRepository.findAll();
            System.out.println("Found " + allVehicles.size() + " vehicles");

            for (Vehicle vehicle : allVehicles) {
                try {
                    Integer vehicleId = vehicle.getVehicleId();
                    String vehicleName = vehicle.getMake() + " " + vehicle.getModel();
                    String vehicleImage = vehicle.getImageUrl();
                    String registrationNumber = vehicle.getRegistrationNumber();

                    // Get booking statistics with error handling
                    Long totalBookings = 0L;
                    Long completedBookings = 0L;
                    Long activeBookings = 0L;
                    Long cancelledBookings = 0L;
                    
                    try {
                        totalBookings = bookingRepository.countBookingsByVehicle(vehicleId);
                        completedBookings = bookingRepository.countBookingsByVehicleAndStatus(vehicleId, "Returned");
                        activeBookings = bookingRepository.countBookingsByVehicleAndStatus(vehicleId, "Confirmed");
                        cancelledBookings = bookingRepository.countBookingsByVehicleAndStatus(vehicleId, "Cancelled");
                    } catch (Exception e) {
                        System.err.println("Error getting booking stats for vehicle " + vehicleId + ": " + e.getMessage());
                    }

                    // Get total revenue
                    BigDecimal totalRevenue = BigDecimal.ZERO;
                    try {
                        BigDecimal revenue = bookingRepository.getTotalRevenueByVehicle(vehicleId);
                        if (revenue != null) {
                            totalRevenue = revenue;
                        }
                    } catch (Exception e) {
                        System.err.println("Error getting revenue for vehicle " + vehicleId + ": " + e.getMessage());
                    }

                    // Get last booking date
                    LocalDateTime lastBookingDate = null;
                    try {
                        lastBookingDate = bookingRepository.getLastBookingDateByVehicle(vehicleId);
                    } catch (Exception e) {
                        System.err.println("Error getting last booking date for vehicle " + vehicleId + ": " + e.getMessage());
                    }

                    // Get most frequent customer
                    String mostFrequentCustomer = "N/A";
                    try {
                        String customer = bookingRepository.getMostFrequentCustomerByVehicle(vehicleId);
                        if (customer != null && !customer.trim().isEmpty()) {
                            mostFrequentCustomer = customer;
                        }
                    } catch (Exception e) {
                        System.err.println("Error getting frequent customer for vehicle " + vehicleId + ": " + e.getMessage());
                    }

                    // Get average booking duration
                    Double averageBookingDuration = 0.0;
                    try {
                        Double duration = bookingRepository.getAverageBookingDurationByVehicle(vehicleId);
                        if (duration != null) {
                            averageBookingDuration = duration;
                        }
                    } catch (Exception e) {
                        System.err.println("Error getting average duration for vehicle " + vehicleId + ": " + e.getMessage());
                    }

                    // Get current vehicle status and customer info if rented
                    String vehicleStatus = vehicle.getStatus(); // Available, Rented, Maintenance, Out of Service
                    String currentCustomerName = null;
                    String currentCustomerEmail = null;
                    String currentCustomerPhone = null;
                    Integer currentBookingId = null;

                    // If vehicle is currently rented or booked, get current customer details
                    if ("Rented".equals(vehicleStatus) || "Booked".equals(vehicleStatus)) {
                        try {
                            // Find active or future booking for this vehicle
                            LocalDateTime now = LocalDateTime.now();
                            List<Booking> currentBookings;
                            
                            if ("Booked".equals(vehicleStatus)) {
                                // For booked vehicles, find future bookings (confirmed but not started yet)
                                currentBookings = bookingRepository.findByVehicle(vehicle).stream()
                                    .filter(booking -> !"Cancelled".equals(booking.getBookingStatus()) && 
                                                     !"Returned".equals(booking.getBookingStatus()) &&
                                                     booking.getEndDate().isAfter(now))
                                    .collect(java.util.stream.Collectors.toList());
                            } else {
                                // For rented vehicles, find currently active bookings
                                currentBookings = bookingRepository.findByVehicleAndStartDateBeforeAndEndDateAfterAndBookingStatusNot(
                                    vehicle, now, now, "Cancelled"
                                );
                            }
                            
                            // If no bookings found with specific logic, try to find any non-cancelled booking
                            if (currentBookings.isEmpty()) {
                                currentBookings = bookingRepository.findByVehicle(vehicle).stream()
                                    .filter(booking -> !"Cancelled".equals(booking.getBookingStatus()) && 
                                                     !"Returned".equals(booking.getBookingStatus()))
                                    .sorted((b1, b2) -> b2.getCreatedAt().compareTo(b1.getCreatedAt())) // Most recent first
                                    .collect(Collectors.toList());
                            }
                            
                            if (!currentBookings.isEmpty()) {
                                Booking activeBooking = currentBookings.get(0); // Get first active booking
                                currentBookingId = activeBooking.getBookingId();
                                
                                if (activeBooking.getCustomer() != null && activeBooking.getCustomer().getUser() != null) {
                                    User user = activeBooking.getCustomer().getUser();
                                    currentCustomerName = user.getFirstName() + " " + user.getLastName();
                                    currentCustomerEmail = user.getEmail();
                                    currentCustomerPhone = activeBooking.getCustomer().getContactNumber();
                                }
                            }
                        } catch (Exception e) {
                            System.err.println("Error getting current customer for vehicle " + vehicleId + ": " + e.getMessage());
                        }
                    }

                    // Create DTO
                    Group2.Car.Rental.System.dto.VehicleUsageHistoryDTO dto = new Group2.Car.Rental.System.dto.VehicleUsageHistoryDTO(
                        vehicleId,
                        vehicleName,
                        vehicleImage,
                        registrationNumber,
                        totalBookings != null ? totalBookings.intValue() : 0,
                        completedBookings != null ? completedBookings.intValue() : 0,
                        activeBookings != null ? activeBookings.intValue() : 0,
                        cancelledBookings != null ? cancelledBookings.intValue() : 0,
                        totalRevenue,
                        lastBookingDate,
                        mostFrequentCustomer,
                        averageBookingDuration,
                        vehicleStatus,
                        currentCustomerName,
                        currentCustomerEmail,
                        currentCustomerPhone,
                        currentBookingId
                    );

                    usageHistory.add(dto);
                } catch (Exception e) {
                    System.err.println("Error processing vehicle " + vehicle.getVehicleId() + ": " + e.getMessage());
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            System.err.println("Error getting vehicle usage history: " + e.getMessage());
            e.printStackTrace();
        }

        System.out.println("Returning " + usageHistory.size() + " vehicle usage records");
        
        // If no data, create sample data for testing
        if (usageHistory.isEmpty()) {
            System.out.println("No vehicles found, creating sample data for testing");
            // Create sample data with different vehicle statuses
            usageHistory.add(createSampleVehicleUsageHistoryWithStatus(1, "Toyota Camry", "TY001", 15, 12, 2, 1, new BigDecimal("5400.00"), "Rented", "Alice Johnson", "alice.johnson@example.com", "+1-555-0101", 1001));
            usageHistory.add(createSampleVehicleUsageHistoryWithStatus(2, "Honda Civic", "HC002", 8, 7, 1, 0, new BigDecimal("2800.00"), "Available", null, null, null, null));
            usageHistory.add(createSampleVehicleUsageHistoryWithStatus(3, "BMW X5", "BX003", 22, 18, 3, 1, new BigDecimal("8900.00"), "Booked", "Michael Brown", "michael.brown@example.com", "+1-555-0103", 1003));
            usageHistory.add(createSampleVehicleUsageHistoryWithStatus(4, "Mercedes E-Class", "ME004", 5, 4, 1, 0, new BigDecimal("2200.00"), "Maintenance", null, null, null, null));
        }
        
        return usageHistory;
    }
    
    private Group2.Car.Rental.System.dto.VehicleUsageHistoryDTO createSampleVehicleUsageHistoryWithStatus(
            Integer id, String name, String reg, int total, int completed, int active, int cancelled, 
            BigDecimal revenue, String vehicleStatus, String currentCustomerName, 
            String currentCustomerEmail, String currentCustomerPhone, Integer currentBookingId) {
        
        return new Group2.Car.Rental.System.dto.VehicleUsageHistoryDTO(
            id, name, "/images/default-car.jpg", reg, total, completed, active, cancelled,
            revenue, LocalDateTime.now().minusDays(5), "Sarah Wilson", 3.5,
            vehicleStatus, currentCustomerName, currentCustomerEmail, currentCustomerPhone, currentBookingId
        );
    }

    /**
     * Get current customer details for a rented vehicle
     * @param vehicleId the vehicle ID
     * @return map containing customer details if vehicle is rented
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getCurrentCustomerForVehicle(Integer vehicleId) {
        Map<String, Object> customerDetails = new HashMap<>();
        
        try {
            System.out.println("DEBUG: Getting customer details for vehicle ID: " + vehicleId);
            
            Optional<Vehicle> vehicleOpt = vehicleRepository.findById(vehicleId);
            if (vehicleOpt.isEmpty()) {
                System.out.println("DEBUG: Vehicle not found with ID: " + vehicleId);
                customerDetails.put("error", "Vehicle not found");
                return customerDetails;
            }
            
            Vehicle vehicle = vehicleOpt.get();
            System.out.println("DEBUG: Vehicle found - ID: " + vehicle.getVehicleId() + ", Status: " + vehicle.getStatus());
            
            if (!"Rented".equals(vehicle.getStatus()) && !"Booked".equals(vehicle.getStatus())) {
                System.out.println("DEBUG: Vehicle is not rented or booked. Status: " + vehicle.getStatus());
                customerDetails.put("error", "Vehicle is not currently rented or booked");
                customerDetails.put("vehicleStatus", vehicle.getStatus());
                return customerDetails;
            }
            
            // Find active booking for this vehicle
            LocalDateTime now = LocalDateTime.now();
            System.out.println("DEBUG: Looking for active bookings at time: " + now);
            
            List<Booking> activeBookings = bookingRepository.findByVehicleAndStartDateBeforeAndEndDateAfterAndBookingStatusNot(
                vehicle, now, now, "Cancelled"
            );
            System.out.println("DEBUG: Found " + activeBookings.size() + " active bookings with date range check");
            
            // If no active bookings found with date range, try to find any non-cancelled booking
            if (activeBookings.isEmpty()) {
                System.out.println("DEBUG: No active bookings found, trying fallback approach");
                List<Booking> allBookings = bookingRepository.findByVehicle(vehicle);
                System.out.println("DEBUG: Total bookings for vehicle: " + allBookings.size());
                
                activeBookings = allBookings.stream()
                    .filter(booking -> !"Cancelled".equals(booking.getBookingStatus()) && 
                                     !"Returned".equals(booking.getBookingStatus()))
                    .sorted((b1, b2) -> b2.getCreatedAt().compareTo(b1.getCreatedAt())) // Most recent first
                    .collect(Collectors.toList());
                System.out.println("DEBUG: Found " + activeBookings.size() + " non-cancelled bookings");
                
                // Print booking details for debugging
                for (Booking booking : activeBookings) {
                    System.out.println("DEBUG: Booking ID: " + booking.getBookingId() + 
                                     ", Status: " + booking.getBookingStatus() + 
                                     ", Customer: " + (booking.getCustomer() != null ? booking.getCustomer().getUserId() : "null"));
                }
            }
            
            if (activeBookings.isEmpty()) {
                System.out.println("DEBUG: No active bookings found for vehicle");
                customerDetails.put("error", "No active booking found for this vehicle");
                return customerDetails;
            }
            
            Booking activeBooking = activeBookings.get(0);
            System.out.println("DEBUG: Using booking ID: " + activeBooking.getBookingId());
            
            customerDetails.put("bookingId", activeBooking.getBookingId());
            customerDetails.put("startDate", activeBooking.getStartDate());
            customerDetails.put("endDate", activeBooking.getEndDate());
            customerDetails.put("totalCost", activeBooking.getTotalCost());
            customerDetails.put("bookingStatus", activeBooking.getBookingStatus());
            
            if (activeBooking.getCustomer() != null && activeBooking.getCustomer().getUser() != null) {
                User user = activeBooking.getCustomer().getUser();
                Customer customer = activeBooking.getCustomer();
                
                System.out.println("DEBUG: Customer found - Name: " + user.getFirstName() + " " + user.getLastName() + 
                                 ", Email: " + user.getEmail());
                
                customerDetails.put("customerName", user.getFirstName() + " " + user.getLastName());
                customerDetails.put("customerEmail", user.getEmail());
                customerDetails.put("customerPhone", customer.getContactNumber());
                customerDetails.put("customerAddress", customer.getAddressStreet());
                customerDetails.put("customerCity", customer.getAddressCity());
                customerDetails.put("customerPostalCode", customer.getAddressPostalCode());
            } else {
                System.out.println("DEBUG: No customer or user found for booking");
            }
            
        } catch (Exception e) {
            System.err.println("Error getting current customer for vehicle " + vehicleId + ": " + e.getMessage());
            customerDetails.put("error", "Unable to retrieve customer details");
        }
        
        return customerDetails;
    }

    /**
     * Get dashboard statistics including currently rented vehicles count
     * @return map containing dashboard statistics
     */
    public Map<String, Object> getDashboardStats() {
        Map<String, Object> stats = new HashMap<>();
        
        try {
            // Get all vehicles
            List<Vehicle> allVehicles = vehicleRepository.findAll();
            int totalVehicles = allVehicles.size();
            
            // Count vehicles by status from vehicle table
            long availableFromVehicleStatus = allVehicles.stream()
                    .filter(v -> "Available".equals(v.getStatus()))
                    .count();
            long maintenanceFromVehicleStatus = allVehicles.stream()
                    .filter(v -> "Maintenance".equals(v.getStatus()) || "Out of Service".equals(v.getStatus()))
                    .count();
            
            // Get currently rented vehicles from active bookings
            long currentlyRentedFromBookings = 0;
            try {
                // Count unique vehicles with active (Confirmed) bookings
                currentlyRentedFromBookings = bookingRepository.findByBookingStatus("Confirmed").stream()
                        .filter(booking -> booking.getVehicle() != null)
                        .filter(booking -> {
                            LocalDateTime now = LocalDateTime.now();
                            return booking.getStartDate().isBefore(now) && booking.getEndDate().isAfter(now);
                        })
                        .map(booking -> booking.getVehicle().getVehicleId())
                        .distinct()
                        .count();
            } catch (Exception e) {
                System.err.println("Error counting rented vehicles from bookings: " + e.getMessage());
            }
            
            stats.put("totalVehicles", totalVehicles);
            stats.put("availableVehicles", (int) availableFromVehicleStatus);
            stats.put("currentlyRentedVehicles", (int) currentlyRentedFromBookings);
            stats.put("maintenanceVehicles", (int) maintenanceFromVehicleStatus);
            
            System.out.println("Dashboard Stats - Total: " + totalVehicles + 
                             ", Available: " + availableFromVehicleStatus + 
                             ", Currently Rented: " + currentlyRentedFromBookings + 
                             ", Maintenance: " + maintenanceFromVehicleStatus);
                             
        } catch (Exception e) {
            System.err.println("Error calculating dashboard stats: " + e.getMessage());
            // Return default values
            stats.put("totalVehicles", 0);
            stats.put("availableVehicles", 0);
            stats.put("currentlyRentedVehicles", 0);
            stats.put("maintenanceVehicles", 0);
        }
        
        return stats;
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
