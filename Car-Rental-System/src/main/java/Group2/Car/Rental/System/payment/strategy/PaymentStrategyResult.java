package Group2.Car.Rental.System.payment.strategy;

import Group2.Car.Rental.System.entity.Payment;

public class PaymentStrategyResult {
    private final Payment payment;
    private final String bookingStatus;

    public PaymentStrategyResult(Payment payment, String bookingStatus) {
        this.payment = payment;
        this.bookingStatus = bookingStatus;
    }

    public Payment getPayment() {
        return payment;
    }

    public String getBookingStatus() {
        return bookingStatus;
    }
}
