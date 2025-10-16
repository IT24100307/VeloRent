package Group2.Car.Rental.System.service;

import Group2.Car.Rental.System.dto.PaymentAdminDTO;
import Group2.Car.Rental.System.entity.Booking;
import Group2.Car.Rental.System.entity.Payment;
import Group2.Car.Rental.System.entity.Vehicle;
import Group2.Car.Rental.System.entity.VehiclePackage;
import Group2.Car.Rental.System.repository.PaymentRepository;
import Group2.Car.Rental.System.repository.VehicleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class PaymentAdminService {

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private Group2.Car.Rental.System.repository.BookingRepository bookingRepository;

    @Autowired
    private VehicleRepository vehicleRepository;

    @Autowired
    private VehiclePackageService vehiclePackageService;

    public List<PaymentAdminDTO> getAllPayments() {
        List<Payment> payments = paymentRepository.findAllWithBookingCustomerUser();
        return payments.stream().map(p -> {
            var b = p.getBooking();
            var c = b.getCustomer();
            var u = c.getUser();
            String name = (u.getFirstName() != null ? u.getFirstName() : "") + " " + (u.getLastName() != null ? u.getLastName() : "");
            return new PaymentAdminDTO(
                p.getPaymentId(),
                b.getBookingId(),
                name.trim(),
                u.getEmail(),
                p.getAmount(),
                p.getPaymentMethod(),
                p.getPaymentStatus(),
                p.getTransactionId(),
                p.getPaymentDate()
            );
        }).collect(Collectors.toList());
    }

    public Map<String, Object> getSummary() {
        Map<String, Object> summary = new HashMap<>();
        BigDecimal totalRevenue = Optional.ofNullable(paymentRepository.sumAllAmounts()).orElse(BigDecimal.ZERO);
        long totalPayments = paymentRepository.count();
        summary.put("totalRevenue", totalRevenue);
        summary.put("totalPayments", totalPayments);

        // By method
        List<Map<String, Object>> byMethod = new ArrayList<>();
        for (Object[] row : paymentRepository.sumAndCountByMethod()) {
            Map<String, Object> m = new HashMap<>();
            m.put("method", String.valueOf(row[0]));
            m.put("amount", row[1]);
            m.put("count", row[2]);
            byMethod.add(m);
        }
        summary.put("byMethod", byMethod);

        // By status
        List<Map<String, Object>> byStatus = new ArrayList<>();
        for (Object[] row : paymentRepository.countByStatus()) {
            Map<String, Object> s = new HashMap<>();
            s.put("status", String.valueOf(row[0]));
            s.put("count", row[1]);
            byStatus.add(s);
        }
        summary.put("byStatus", byStatus);

        // Last payment date
        LocalDateTime lastDate = paymentRepository.findAll().stream()
                .map(Payment::getPaymentDate)
                .filter(Objects::nonNull)
                .max(LocalDateTime::compareTo)
                .orElse(null);
        summary.put("lastPaymentDate", lastDate);

        return summary;
    }

    // New admin actions
    public Map<String, Object> confirmPayment(Integer paymentId) {
        Map<String, Object> res = new HashMap<>();
        Payment p = paymentRepository.findById(paymentId).orElse(null);
        if (p == null) {
            res.put("success", false);
            res.put("message", "Payment not found");
            return res;
        }
        String method = Optional.ofNullable(p.getPaymentMethod()).orElse("").toLowerCase();
        String status = Optional.ofNullable(p.getPaymentStatus()).orElse("").toLowerCase();
        if (!"cash".equals(method)) {
            res.put("success", false);
            res.put("message", "Only cash payments can be confirmed");
            return res;
        }
        if (!status.contains("pend")) {
            res.put("success", false);
            res.put("message", "Only pending cash payments can be confirmed");
            return res;
        }
        p.setPaymentStatus("Completed");
        paymentRepository.save(p);
        var booking = p.getBooking();
        if (booking != null) {
            booking.setBookingStatus("Confirmed");
            bookingRepository.save(booking);
        }
        res.put("success", true);
        res.put("message", "Payment confirmed");
        return res;
    }

    public Map<String, Object> cancelPayment(Integer paymentId) {
        Map<String, Object> res = new HashMap<>();
        Payment p = paymentRepository.findById(paymentId).orElse(null);
        if (p == null) {
            res.put("success", false);
            res.put("message", "Payment not found");
            return res;
        }
        // Always ensure resources are released (idempotent)
        Booking booking = p.getBooking();
        try {
            if (booking != null) {
                // Update booking status if needed
                if (!"Cancelled".equalsIgnoreCase(booking.getBookingStatus())) {
                    booking.setBookingStatus("Cancelled");
                    bookingRepository.save(booking);
                }
                // Vehicle booking: free the vehicle
                Vehicle vehicle = booking.getVehicle();
                if (vehicle != null) {
                    vehicle.setStatus("Available");
                    vehicleRepository.save(vehicle);
                    // Restore any partially reserved packages that include this vehicle
                    try {
                        vehiclePackageService.restorePackagesFromPartialReservation(vehicle.getVehicleId());
                    } catch (Exception ignored) { /* best-effort */ }
                }
                // Package booking: free all vehicles in the package
                VehiclePackage pkg = booking.getVehiclePackage();
                if (pkg != null && pkg.getVehicles() != null) {
                    boolean allAvailable = true;
                    for (Vehicle v : pkg.getVehicles()) {
                        if (!"Available".equals(v.getStatus())) {
                            v.setStatus("Available");
                            vehicleRepository.save(v);
                        }
                        allAvailable &= "Available".equals(v.getStatus());
                    }
                    // Optionally, if all vehicles are available and package was partially reserved, restore status
                    if (allAvailable && "Partially Reserved".equals(pkg.getStatus())) {
                        try { vehiclePackageService.setStatus(pkg.getPackageId(), "Activated"); } catch (Exception ignored) {}
                    }
                }
            }

            // Update payment status last
            if (!"Cancelled".equalsIgnoreCase(p.getPaymentStatus())) {
                p.setPaymentStatus("Cancelled");
                paymentRepository.save(p);
            }

            res.put("success", true);
            res.put("message", "Payment cancelled and resources released");
            return res;
        } catch (Exception e) {
            res.put("success", false);
            res.put("message", "Error cancelling payment: " + e.getMessage());
            return res;
        }
    }

    public Map<String, Object> deletePayment(Integer paymentId) {
        Map<String, Object> res = new HashMap<>();
        Payment p = paymentRepository.findById(paymentId).orElse(null);
        if (p == null) {
            res.put("success", false);
            res.put("message", "Payment not found");
            return res;
        }
        try {
            paymentRepository.delete(p);
            res.put("success", true);
            res.put("message", "Payment deleted");
        } catch (Exception ex) {
            res.put("success", false);
            res.put("message", "Failed to delete payment: " + ex.getMessage());
        }
        return res;
    }
}
