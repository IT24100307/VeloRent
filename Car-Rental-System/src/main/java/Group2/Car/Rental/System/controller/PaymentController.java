package Group2.Car.Rental.System.controller;

import Group2.Car.Rental.System.dto.PaymentRequest;
import Group2.Car.Rental.System.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    @PostMapping
    public ResponseEntity<Map<String, Object>> processPayment(@RequestBody PaymentRequest paymentRequest) {
        Map<String, Object> result = paymentService.processPayment(paymentRequest);
        boolean success = (boolean) result.get("success");

        if (success) {
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.badRequest().body(result);
        }
    }
}
