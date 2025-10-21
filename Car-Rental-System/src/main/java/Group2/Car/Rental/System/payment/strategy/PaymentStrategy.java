package Group2.Car.Rental.System.payment.strategy;

import Group2.Car.Rental.System.entity.Booking;
import Group2.Car.Rental.System.entity.Payment;

import java.math.BigDecimal;

/**
 * Behavioral Pattern: Strategy
 * Encapsulates payment-method-specific processing logic.
 */
public interface PaymentStrategy {
    /**
     * @param method canonical payment method string (e.g., "cash", "card")
     * @return true if this strategy supports the method
     */
    boolean supports(String method);

    /**
     * Process the payment for a booking and return a result containing the Payment entity and
     * the booking status to set upon success.
     *
     * @param booking the booking being paid for
     * @param amount the amount to charge/record
     * @param transactionId optional external transaction/reference id (may be null)
     * @return result containing built Payment and desired booking status
     */
    PaymentStrategyResult process(Booking booking, BigDecimal amount, String transactionId);
}
