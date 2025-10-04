package Group2.Car.Rental.System.service;

import Group2.Car.Rental.System.dto.PaymentRequest;
import Group2.Car.Rental.System.entity.Booking;
import Group2.Car.Rental.System.entity.Payment;
import Group2.Car.Rental.System.repository.BookingRepository;
import Group2.Car.Rental.System.repository.PaymentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class PaymentService {

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private BookingRepository bookingRepository;

    /**
     * Process a payment for a booking
     * @param paymentRequest the payment information
     * @return a map containing status and message
     */
    @Transactional
    public Map<String, Object> processPayment(PaymentRequest paymentRequest) {
        Map<String, Object> response = new HashMap<>();

        try {
            // Find the booking
            Booking booking = bookingRepository.findById(paymentRequest.getBookingId())
                    .orElse(null);

            if (booking == null) {
                response.put("success", false);
                response.put("message", "Booking not found");
                return response;
            }

            // Create the payment
            Payment payment = new Payment();
            payment.setPaymentDate(LocalDateTime.now());
            payment.setAmount(paymentRequest.getAmount());
            payment.setPaymentMethod(paymentRequest.getPaymentMethod());
            payment.setBooking(booking);

            // Update booking status based on payment method
            if ("cash".equalsIgnoreCase(paymentRequest.getPaymentMethod())) {
                booking.setBookingStatus("Payment Pending");
            } else {
                booking.setBookingStatus("Confirmed");
            }

            // Save payment and updated booking
            bookingRepository.save(booking);
            paymentRepository.save(payment);

            response.put("success", true);
            response.put("message", "Payment processed successfully");
            response.put("bookingStatus", booking.getBookingStatus());
            response.put("paymentId", payment.getPaymentId());

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error processing payment: " + e.getMessage());
        }

        return response;
    }

    /**
     * Process payment for a package booking
     * @param bookingId the booking ID
     * @param paymentMethod the payment method
     * @param transactionId optional transaction ID for tracking
     * @return a map containing payment status and details
     */
    @Transactional
    public Map<String, Object> processPackagePayment(Integer bookingId, String paymentMethod, String transactionId) {
        Map<String, Object> response = new HashMap<>();

        try {
            // Find the booking
            Booking booking = bookingRepository.findById(bookingId).orElse(null);

            if (booking == null) {
                response.put("success", false);
                response.put("message", "Booking not found");
                return response;
            }

            // Check if payment already exists
            Payment existingPayment = paymentRepository.findByBooking(booking);
            if (existingPayment != null) {
                response.put("success", false);
                response.put("message", "Payment already processed for this booking");
                return response;
            }

            // Create the payment
            Payment payment = new Payment();
            payment.setPaymentDate(LocalDateTime.now());
            payment.setAmount(booking.getTotalCost());
            payment.setPaymentMethod(paymentMethod);
            payment.setTransactionId(transactionId);
            payment.setPaymentStatus("Completed");
            payment.setBooking(booking);

            // Save the payment
            Payment savedPayment = paymentRepository.save(payment);

            // Update booking status to confirmed with payment
            booking.setBookingStatus("Confirmed");
            bookingRepository.save(booking);

            response.put("success", true);
            response.put("message", "Payment processed successfully");

            // Return payment details
            Map<String, Object> paymentDetails = new HashMap<>();
            paymentDetails.put("paymentId", savedPayment.getPaymentId());
            paymentDetails.put("amount", savedPayment.getAmount());
            paymentDetails.put("paymentMethod", savedPayment.getPaymentMethod());
            paymentDetails.put("transactionId", savedPayment.getTransactionId());
            paymentDetails.put("paymentDate", savedPayment.getPaymentDate());
            paymentDetails.put("bookingId", booking.getBookingId());

            response.put("payment", paymentDetails);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error processing payment: " + e.getMessage());
        }

        return response;
    }

    /**
     * Get payment by booking ID
     * @param bookingId the booking ID
     * @return payment if found
     */
    public Payment getPaymentByBookingId(Integer bookingId) {
        return bookingRepository.findById(bookingId)
            .map(booking -> paymentRepository.findByBooking(booking))
            .orElse(null);
    }
}
