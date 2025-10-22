package Group2.Car.Rental.System.controller;

import Group2.Car.Rental.System.entity.Customer;
import Group2.Car.Rental.System.repository.CustomerRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/customers")
public class CustomerController {

    private final CustomerRepository customerRepository;

    public CustomerController(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    /**
     * Return all customers with minimal fields for selection UIs.
     * Each item: { customerId, name, email }
     */
    @GetMapping
    @Transactional(readOnly = true)
    public ResponseEntity<List<Map<String, Object>>> listAllCustomers() {
        List<Map<String, Object>> out = new ArrayList<>();
        try {
            List<Customer> customers = customerRepository.findAllWithUser();
            for (Customer c : customers) {
                Map<String, Object> row = new HashMap<>();
                row.put("customerId", c.getUserId() != null ? c.getUserId().intValue() : null);
                String first = c.getUser() != null ? c.getUser().getFirstName() : null;
                String last = c.getUser() != null ? c.getUser().getLastName() : null;
                String email = c.getUser() != null ? c.getUser().getEmail() : null;
                String name = ((first != null ? first : "") + " " + (last != null ? last : "")).trim();
                row.put("name", name);
                row.put("email", email);
                out.add(row);
            }
        } catch (Exception ignored) {}
        return ResponseEntity.ok(out);
    }
}
