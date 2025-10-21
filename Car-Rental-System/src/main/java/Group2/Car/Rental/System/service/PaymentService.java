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
import Group2.Car.Rental.System.payment.strategy.PaymentStrategyFactory;
import Group2.Car.Rental.System.payment.strategy.PaymentStrategy;
import Group2.Car.Rental.System.payment.strategy.PaymentStrategyResult;

@Service
public class PaymentService {

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private PaymentStrategyFactory paymentStrategyFactory;

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

            // Check if payment already exists to prevent duplicate payments
            Payment existingPayment = paymentRepository.findByBooking(booking);
            if (existingPayment != null) {
                response.put("success", false);
                response.put("message", "Payment already processed for this booking");
                return response;
            }

            // Validate booking status - should be "Pending Payment" or "Confirmed" for new bookings
            if (!"Pending Payment".equals(booking.getBookingStatus()) &&
                !"Confirmed".equals(booking.getBookingStatus())) {
                response.put("success", false);
                response.put("message", "Booking is not in a valid state for payment processing");
                return response;
            }

            // Resolve strategy and process payment
            PaymentStrategy strategy = paymentStrategyFactory.resolve(paymentRequest.getPaymentMethod());
            PaymentStrategyResult result = strategy.process(
                    booking,
                    paymentRequest.getAmount() != null ? paymentRequest.getAmount() : booking.getTotalCost(),
                    null // no transactionId in PaymentRequest
            );

            Payment payment = result.getPayment();
            booking.setBookingStatus(result.getBookingStatus());

            // Save payment first, then update booking
            Payment savedPayment = paymentRepository.save(payment);
            Booking updatedBooking = bookingRepository.save(booking);

            response.put("success", true);
            response.put("message", "Payment processed successfully");
            response.put("bookingStatus", updatedBooking.getBookingStatus());
            response.put("paymentId", savedPayment.getPaymentId());

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error processing payment: " + e.getMessage());
            // Log the full exception for debugging
            e.printStackTrace();
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

            // Resolve strategy and process payment for package
            PaymentStrategy strategy = paymentStrategyFactory.resolve(paymentMethod);
            PaymentStrategyResult result = strategy.process(
                    booking,
                    booking.getTotalCost(),
                    transactionId
            );

            Payment payment = result.getPayment();

            // Save the payment
            Payment savedPayment = paymentRepository.save(payment);

            // Update booking status depending on strategy
            booking.setBookingStatus(result.getBookingStatus());
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
