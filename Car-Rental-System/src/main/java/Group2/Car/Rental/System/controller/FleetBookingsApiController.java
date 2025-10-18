package Group2.Car.Rental.System.controller;

import Group2.Car.Rental.System.dto.BookingAdminSummaryDTO;
import Group2.Car.Rental.System.service.BookingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;

@RestController
@RequestMapping("/api/fleet/bookings")
@PreAuthorize("hasRole('FLEET_MANAGER')")
public class FleetBookingsApiController {

    @Autowired
    private BookingService bookingService;

    private static final Logger log = LoggerFactory.getLogger(FleetBookingsApiController.class);

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<BookingAdminSummaryDTO>> listAll() {
        List<BookingAdminSummaryDTO> list = bookingService.getAllBookingsAdmin();
        log.debug("Fleet bookings requested: {} records", list != null ? list.size() : 0);
        return ResponseEntity.ok(list);
    }

    @DeleteMapping("/{bookingId}")
    public ResponseEntity<Map<String, Object>> delete(@PathVariable Integer bookingId) {
        Map<String, Object> res = bookingService.deleteBookingIfNoPayments(bookingId);
        if (Boolean.TRUE.equals(res.get("success"))) return ResponseEntity.ok(res);
        return ResponseEntity.badRequest().body(res);
    }
}
