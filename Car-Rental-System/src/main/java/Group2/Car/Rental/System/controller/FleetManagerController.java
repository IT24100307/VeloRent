package Group2.Car.Rental.System.controller;

import Group2.Car.Rental.System.dto.BookingAdminSummaryDTO;
import Group2.Car.Rental.System.service.BookingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/fleet-manager")
public class FleetManagerController {

    @Autowired
    private BookingService bookingService;

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

    @GetMapping("/bookings")
    public String bookings(Model model) {
        try {
            // Get all bookings for fleet manager view
            List<BookingAdminSummaryDTO> bookings = bookingService.getAllBookingsAdmin();
            model.addAttribute("bookings", bookings);
            model.addAttribute("bookingsCount", bookings.size());

            // Compute status counts server-side for accurate initial render
            long confirmed = bookings.stream().filter(b -> "Confirmed".equalsIgnoreCase(b.getBookingStatus())).count();
            long pending = bookings.stream().filter(b -> {
                String s = b.getBookingStatus() == null ? "" : b.getBookingStatus();
                return "Pending Payment".equalsIgnoreCase(s) || "Payment Pending".equalsIgnoreCase(s);
            }).count();
            long returned = bookings.stream().filter(b -> "Returned".equalsIgnoreCase(b.getBookingStatus())).count();
            long cancelled = bookings.stream().filter(b -> "Cancelled".equalsIgnoreCase(b.getBookingStatus())).count();

            model.addAttribute("confirmedCount", confirmed);
            model.addAttribute("pendingCount", pending);
            model.addAttribute("returnedCount", returned);
            model.addAttribute("cancelledCount", cancelled);
        } catch (Exception e) {
            model.addAttribute("error", "Failed to load bookings: " + e.getMessage());
            model.addAttribute("bookings", java.util.Collections.emptyList());
            model.addAttribute("bookingsCount", 0);
        }
        return "fleet-manager-bookings";
    }

    @PostMapping("/bookings/delete/{bookingId}")
    public String deleteBooking(@PathVariable Integer bookingId, RedirectAttributes redirectAttributes) {
        try {
            Map<String, Object> result = bookingService.deleteBookingIfNoPayments(bookingId);
            if (Boolean.TRUE.equals(result.get("success"))) {
                redirectAttributes.addFlashAttribute("successMessage", result.get("message"));
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", result.get("message"));
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to delete booking: " + e.getMessage());
        }
        return "redirect:/fleet-manager/bookings";
    }
}
