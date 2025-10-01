package Group2.Car.Rental.System.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PaymentViewController {

    @GetMapping("/payment")
    public String showPaymentPage() {
        return "payment";
    }
}
