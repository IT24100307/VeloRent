package Group2.Car.Rental.System.service;

import Group2.Car.Rental.System.dto.NotificationDTO;
import Group2.Car.Rental.System.entity.Booking;
import Group2.Car.Rental.System.entity.MaintenanceRecord;
import Group2.Car.Rental.System.entity.Vehicle;
import Group2.Car.Rental.System.repository.BookingRepository;
import Group2.Car.Rental.System.repository.MaintenanceRecordRepository;
import Group2.Car.Rental.System.repository.VehicleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class NotificationService {

    private final BookingRepository bookingRepository;
    private final MaintenanceRecordRepository maintenanceRecordRepository;
    private final VehicleRepository vehicleRepository;

    @Autowired
    public NotificationService(BookingRepository bookingRepository,
                               MaintenanceRecordRepository maintenanceRecordRepository,
                               VehicleRepository vehicleRepository) {
        this.bookingRepository = bookingRepository;
        this.maintenanceRecordRepository = maintenanceRecordRepository;
        this.vehicleRepository = vehicleRepository;
    }

    public List<NotificationDTO> getRecentNotifications() {
        List<NotificationDTO> notifications = new ArrayList<>();

        // Recent booking confirmations
        List<Booking> confirmed = bookingRepository.findTop20ByBookingStatusOrderByCreatedAtDesc("Confirmed");
        if (confirmed == null || confirmed.isEmpty()) {
            confirmed = bookingRepository.findTop20ByBookingStatusOrderByBookingIdDesc("Confirmed");
        }
        for (Booking b : safeList(confirmed)) {
            String title = "Booking Confirmed";
            String item = b.getVehicle() != null ? formatVehicle(b.getVehicle()) :
                    (b.getVehiclePackage() != null ? b.getVehiclePackage().getPackageName() : "Package");
            String msg = "A booking was confirmed for " + item + ".";
            notifications.add(new NotificationDTO(
                    "booking_confirmation",
                    title,
                    msg,
                    "fa-check-circle",
                    "success",
                    b.getCreatedAt() != null ? b.getCreatedAt() : LocalDateTime.now()
            ));
        }

        // Recent booking cancellations
        List<Booking> cancelled = bookingRepository.findTop20ByBookingStatusOrderByCreatedAtDesc("Cancelled");
        if (cancelled == null || cancelled.isEmpty()) {
            cancelled = bookingRepository.findTop20ByBookingStatusOrderByBookingIdDesc("Cancelled");
        }
        for (Booking b : safeList(cancelled)) {
            String title = "Booking Cancelled";
            String item = b.getVehicle() != null ? formatVehicle(b.getVehicle()) :
                    (b.getVehiclePackage() != null ? b.getVehiclePackage().getPackageName() : "Package");
            String msg = "A booking was cancelled for " + item + ".";
            notifications.add(new NotificationDTO(
                    "booking_cancel",
                    title,
                    msg,
                    "fa-times-circle",
                    "danger",
                    b.getCreatedAt() != null ? b.getCreatedAt() : LocalDateTime.now()
            ));
        }

        // Upcoming bookings starting within next 24 hours (countdown)
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime nextDay = now.plusHours(24);
        List<Booking> startingSoon = bookingRepository
                .findTop20ByBookingStatusAndStartDateBetweenOrderByStartDateAsc("Confirmed", now, nextDay);
        for (Booking b : safeList(startingSoon)) {
            String item = b.getVehicle() != null ? formatVehicle(b.getVehicle()) :
                    (b.getVehiclePackage() != null ? b.getVehiclePackage().getPackageName() : "Package");
            String title = "Booking Starts Soon";
            String msg = item + " starts at " + (b.getStartDate() != null ? b.getStartDate() : "(unknown)") + ".";
            NotificationDTO dto = new NotificationDTO(
                    "booking_countdown",
                    title,
                    msg,
                    "fa-hourglass-half",
                    "warning",
                    // Use creation time if present; otherwise sort near start time
                    b.getCreatedAt() != null ? b.getCreatedAt() : b.getStartDate()
            );
            // Stable id and target time for countdown rendering on client
            dto.setId("booking_start_" + (b.getBookingId() != null ? b.getBookingId() : (item + "_" + b.getStartDate())));
            dto.setTargetTime(b.getStartDate());
            // Include context so UI can open details
            try {
                if (b.getVehicle() != null) dto.setVehicleId(b.getVehicle().getVehicleId());
                if (b.getBookingId() != null) dto.setBookingId(b.getBookingId());
            } catch (Exception ignored) {}
            notifications.add(dto);
        }

        // Upcoming maintenance within next 30 days (countdown until due)
        LocalDate today = LocalDate.now();
        LocalDate inThirtyDays = today.plusDays(30);
        List<MaintenanceRecord> upcoming = maintenanceRecordRepository.findByMaintenanceDateBetween(today, inThirtyDays);
        for (MaintenanceRecord m : safeList(upcoming)) {
            // Only show countdown for scheduled-but-not-completed maintenance (no cost yet)
            if (m.getCost() != null) continue;
            String vehicleName = "Vehicle #" + m.getVehicleId();
            try {
                Vehicle v = vehicleRepository.findById(m.getVehicleId()).orElse(null);
                if (v != null) vehicleName = v.getMake() + " " + v.getModel() + " (" + v.getRegistrationNumber() + ")";
            } catch (Exception ignored) {}
            String title = "Maintenance Due Soon";
            String msg = vehicleName + " scheduled maintenance on " + m.getMaintenanceDate() + ".";
            NotificationDTO dto = new NotificationDTO(
                    "booking_countdown",
                    title,
                    msg,
                    "fa-tools",
                    "warning",
                    m.getMaintenanceDate().atStartOfDay()
            );
            dto.setId("maint_" + m.getMaintenanceId());
            dto.setTargetTime(m.getMaintenanceDate().atStartOfDay());
            try { dto.setVehicleId(m.getVehicleId()); } catch (Exception ignored) {}
            notifications.add(dto);
        }

        // New vehicles added recently
        try {
            List<Vehicle> recentVehicles = vehicleRepository.findTop10ByOrderByCreatedAtDesc();
            if (recentVehicles == null || recentVehicles.isEmpty()) {
                recentVehicles = vehicleRepository.findTop10ByOrderByVehicleIdDesc();
            }
            for (Vehicle v : safeList(recentVehicles)) {
                String title = "New Vehicle Added";
                String msg = formatVehicle(v) + " added to fleet.";
                LocalDateTime ts = v.getCreatedAt() != null ? v.getCreatedAt() : LocalDateTime.now();
                NotificationDTO dto = new NotificationDTO(
                        "vehicle_added",
                        title,
                        msg,
                        "fa-car",
                        "info",
                        ts
                );
                try { dto.setVehicleId(v.getVehicleId()); } catch (Exception ignored) {}
                // Use a stable id to avoid duplicates client-side
                dto.setId("vehicle_added_" + (v.getVehicleId() != null ? v.getVehicleId() : msg.hashCode()));
                notifications.add(dto);
            }
        } catch (Exception ignored) {}

        // Sort all notifications by timestamp desc and limit to 30
        notifications.sort(Comparator.comparing(NotificationDTO::getTimestamp, Comparator.nullsLast(Comparator.naturalOrder())).reversed());
        if (notifications.size() > 30) {
            return new ArrayList<>(notifications.subList(0, 30));
        }
        return notifications;
    }

    private static <T> List<T> safeList(List<T> list) {
        return list == null ? java.util.Collections.emptyList() : list;
    }

    private String formatVehicle(Vehicle v) {
        String reg = v.getRegistrationNumber() != null ? (" [" + v.getRegistrationNumber() + "]") : "";
        return v.getMake() + " " + v.getModel() + reg;
    }
}
