package Group2.Car.Rental.System.dto;

import java.time.LocalDateTime;

public class NotificationDTO {
    private String type;       // booking_confirmation | booking_cancel | maintenance_upcoming | booking_countdown
    private String title;      // Short title for display
    private String message;    // Detailed message
    private String icon;       // FontAwesome icon class
    private String severity;   // info | success | warning | danger
    private LocalDateTime timestamp; // When this event occurred

    // Optional: stable identifier for read/unread state on client
    private String id;
    // Optional: target time for countdown-style notifications (e.g., booking start)
    private LocalDateTime targetTime;

    // Optional: context to allow UI deep-linking/details
    private Integer vehicleId;
    private Integer bookingId;

    public NotificationDTO() {}

    public NotificationDTO(String type, String title, String message, String icon, String severity, LocalDateTime timestamp) {
        this.type = type;
        this.title = title;
        this.message = message;
        this.icon = icon;
        this.severity = severity;
        this.timestamp = timestamp;
    }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getIcon() { return icon; }
    public void setIcon(String icon) { this.icon = icon; }

    public String getSeverity() { return severity; }
    public void setSeverity(String severity) { this.severity = severity; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public LocalDateTime getTargetTime() { return targetTime; }
    public void setTargetTime(LocalDateTime targetTime) { this.targetTime = targetTime; }

    public Integer getVehicleId() { return vehicleId; }
    public void setVehicleId(Integer vehicleId) { this.vehicleId = vehicleId; }

    public Integer getBookingId() { return bookingId; }
    public void setBookingId(Integer bookingId) { this.bookingId = bookingId; }
}
