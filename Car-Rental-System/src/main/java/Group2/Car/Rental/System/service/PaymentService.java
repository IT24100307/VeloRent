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
}
