package Group2.Car.Rental.System.payment.strategy;

import Group2.Car.Rental.System.entity.Booking;
import Group2.Car.Rental.System.entity.Payment;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Component
public class CashPaymentStrategy implements PaymentStrategy {
    @Override
    public boolean supports(String method) {
        return method != null && method.equalsIgnoreCase("cash");
    }

    @Override
    public PaymentStrategyResult process(Booking booking, BigDecimal amount, String transactionId) {
        Payment payment = new Payment();
        payment.setPaymentDate(LocalDateTime.now());
        payment.setAmount(amount);
        payment.setPaymentMethod("cash");
        payment.setPaymentStatus("Pending"); // to be confirmed by staff later
        payment.setBooking(booking);
        // no transaction id for cash
        return new PaymentStrategyResult(payment, "Payment Pending");
    }
}
