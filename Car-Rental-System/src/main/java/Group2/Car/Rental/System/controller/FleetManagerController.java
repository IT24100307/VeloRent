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

    @GetMapping("/vehicle-usage-history")
    public String vehicleUsageHistory() {
        return "fleet-manager-vehicle-usage-history";
    }

    @GetMapping("/vehicle-usage-history-test")
    public String vehicleUsageHistoryTest() {
        return "fleet-manager-vehicle-usage-history-test";
    }

    @GetMapping("/vehicle-usage-history-working")
    public String vehicleUsageHistoryWorking() {
        return "fleet-manager-vehicle-usage-history-working";
    }

    @GetMapping("/maintenance")
    public String maintenance() {
        return "fleet-manager-maintenance";
    }
}
