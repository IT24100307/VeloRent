package Group2.Car.Rental.System.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/fleet-manager")
public class FleetManagerController {

    @GetMapping("/dashboard")
    public String fleetManagerDashboard() {
        return "fleet-manager-dashboard";
    }

    @GetMapping("/packages")
    public String fleetManagerPackages() {
        return "fleet-manager-packages";
    }
}
