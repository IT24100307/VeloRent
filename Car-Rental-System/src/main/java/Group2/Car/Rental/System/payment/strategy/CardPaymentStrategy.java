package Group2.Car.Rental.System.payment.strategy;

import Group2.Car.Rental.System.entity.Booking;
import Group2.Car.Rental.System.entity.Payment;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Component
public class CardPaymentStrategy implements PaymentStrategy {
    @Override
    public boolean supports(String method) {
        return method != null && method.equalsIgnoreCase("card");
    }

    @Override
    public PaymentStrategyResult process(Booking booking, BigDecimal amount, String transactionId) {
        // Here you would normally call a gateway. For now we simulate a successful charge.
        Payment payment = new Payment();
        payment.setPaymentDate(LocalDateTime.now());
        payment.setAmount(amount);
        payment.setPaymentMethod("card");
        payment.setPaymentStatus("Completed");
        payment.setTransactionId(transactionId);
        payment.setBooking(booking);
        return new PaymentStrategyResult(payment, "Confirmed");
    }
}
