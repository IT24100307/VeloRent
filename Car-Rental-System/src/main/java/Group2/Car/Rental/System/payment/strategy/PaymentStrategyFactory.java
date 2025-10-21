package Group2.Car.Rental.System.payment.strategy;

import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PaymentStrategyFactory {
    private final List<PaymentStrategy> strategies;

    public PaymentStrategyFactory(List<PaymentStrategy> strategies) {
        this.strategies = strategies;
    }

    public PaymentStrategy resolve(String method) {
        return strategies.stream()
                .filter(s -> s.supports(method))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unsupported payment method: " + method));
    }
}
