package Group2.Car.Rental.System.payment.strategy;

import Group2.Car.Rental.System.entity.Booking;
import Group2.Car.Rental.System.entity.Payment;

import java.math.BigDecimal;
public interface PaymentStrategy {
    boolean supports(String method);
    PaymentStrategyResult process(Booking booking, BigDecimal amount, String transactionId);
}
