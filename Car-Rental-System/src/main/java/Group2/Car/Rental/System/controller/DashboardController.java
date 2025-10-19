package Group2.Car.Rental.System.controller;

import Group2.Car.Rental.System.dto.DashboardDataDto;
import Group2.Car.Rental.System.service.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/api/owner")
public class DashboardController {
    @Autowired
    private DashboardService dashboardService;

    @GetMapping("/dashboardData")
    @ResponseBody
    public DashboardDataDto getDashboardData() {
        return dashboardService.getDashboardData();
    }
}
