package Group2.Car.Rental.System.service;

import Group2.Car.Rental.System.dto.NotificationDTO;
import Group2.Car.Rental.System.entity.Booking;
import Group2.Car.Rental.System.entity.Payment;
import Group2.Car.Rental.System.entity.User;
import Group2.Car.Rental.System.entity.Feedback;
import Group2.Car.Rental.System.repository.BookingRepository;
import Group2.Car.Rental.System.repository.PaymentRepository;
import Group2.Car.Rental.System.repository.UserRepository;
import Group2.Car.Rental.System.repository.FeedbackRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class CustomerNotificationService {

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private UserRepository userRepository;

        @Autowired
        private FeedbackRepository feedbackRepository;

    /**
     * Build recent notifications for the given user email (customer).
     * If the email isn't found or doesn't map to a customer, returns empty list.
     */
    public List<NotificationDTO> getNotificationsForUserEmail(String email) {
        List<NotificationDTO> list = new ArrayList<>();
        if (email == null || email.isBlank() || "anonymousUser".equals(email)) return list;

        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null || user.getCustomer() == null) return list;
        Integer customerId = user.getId() != null ? user.getId().intValue() : null;
        if (customerId == null) return list;

        // Booking confirmed
        for (Booking b : safe(bookingRepository
                .findTop20ByCustomer_UserIdAndBookingStatusOrderByCreatedAtDesc(customerId, "Confirmed"))) {
            String item = b.getVehicle() != null
                    ? (b.getVehicle().getMake() + " " + b.getVehicle().getModel())
                    : (b.getVehiclePackage() != null ? b.getVehiclePackage().getPackageName() : "Your booking");
            NotificationDTO dto = new NotificationDTO(
                    "booking_confirmation",
                    "Booking Confirmed",
                    item + " is confirmed.",
                    "fa-check-circle",
                    "success",
                    b.getCreatedAt() != null ? b.getCreatedAt() : LocalDateTime.now()
            );
            dto.setId("booking_confirmed_" + (b.getBookingId() != null ? b.getBookingId() : (item + "_" + dto.getTimestamp())));
            if (b.getBookingId() != null) dto.setBookingId(b.getBookingId());
            list.add(dto);
        }

        // Booking cancelled
        for (Booking b : safe(bookingRepository
                .findTop20ByCustomer_UserIdAndBookingStatusOrderByCreatedAtDesc(customerId, "Cancelled"))) {
            String item = b.getVehicle() != null
                    ? (b.getVehicle().getMake() + " " + b.getVehicle().getModel())
                    : (b.getVehiclePackage() != null ? b.getVehiclePackage().getPackageName() : "Your booking");
            NotificationDTO dto = new NotificationDTO(
                    "booking_cancel",
                    "Booking Cancelled",
                    item + " booking was cancelled.",
                    "fa-times-circle",
                    "danger",
                    b.getCreatedAt() != null ? b.getCreatedAt() : LocalDateTime.now()
            );
            dto.setId("booking_cancelled_" + (b.getBookingId() != null ? b.getBookingId() : (item + "_" + dto.getTimestamp())));
            if (b.getBookingId() != null) dto.setBookingId(b.getBookingId());
            list.add(dto);
        }

        // Payment completed
        for (Payment p : safe(paymentRepository
                .findTop20ByBooking_Customer_UserIdAndPaymentStatusOrderByPaymentDateDesc(customerId, "Completed"))) {
            Booking b = p.getBooking();
            String item = (b != null && b.getVehicle() != null)
                    ? (b.getVehicle().getMake() + " " + b.getVehicle().getModel())
                    : (b != null && b.getVehiclePackage() != null ? b.getVehiclePackage().getPackageName() : "Your booking");
            NotificationDTO dto = new NotificationDTO(
                    "payment_confirmed",
                    "Payment Confirmed",
                    "Payment received for " + item + ".",
                    "fa-money-check-alt",
                    "success",
                    p.getPaymentDate() != null ? p.getPaymentDate() : LocalDateTime.now()
            );
            dto.setId("payment_completed_" + (p.getPaymentId() != null ? p.getPaymentId() : (item + "_" + dto.getTimestamp())));
            if (b != null && b.getBookingId() != null) dto.setBookingId(b.getBookingId());
            list.add(dto);
        }

                // Admin replied to customer's feedback
                try {
                        Long cid = user.getId();
                        if (cid != null) {
                                for (Feedback f : safe(feedbackRepository.findByCustomerId(cid))) {
                                        if (f == null) continue;
                                        if (f.isDeleted()) continue;
                                        if (f.getReply() == null || f.getReply().isBlank()) continue;
                                        NotificationDTO dto = new NotificationDTO(
                                                        "feedback_reply",
                                                        "Admin Replied to Your Feedback",
                                                        buildReplyMessage(f),
                                                        "fa-reply",
                                                        "info",
                                                        f.getReplyDate() != null ? f.getReplyDate() : (f.getCreatedAt() != null ? f.getCreatedAt() : java.time.LocalDateTime.now())
                                        );
                                        dto.setId("feedback_reply_" + (f.getId() != null ? f.getId() : Math.abs((f.getComments()!=null?f.getComments():"" ).hashCode())));
                                        list.add(dto);
                                }
                        }
                } catch (Exception ignored) {}

        // Sort by timestamp desc and cap
        list.sort(Comparator.comparing(NotificationDTO::getTimestamp, Comparator.nullsLast(Comparator.naturalOrder())).reversed());
        if (list.size() > 30) return new ArrayList<>(list.subList(0, 30));
        return list;
    }

    private static <T> List<T> safe(List<T> l) { return l == null ? java.util.Collections.emptyList() : l; }

        private String buildReplyMessage(Feedback f) {
                String adminName = null;
                try {
                        if (f.getAdmin() != null) {
                                adminName = (f.getAdmin().getFirstName() + " " + f.getAdmin().getLastName()).trim();
                        }
                } catch (Exception ignored) {}
                String reply = f.getReply() != null ? f.getReply().trim() : "";
                String preview = reply.length() > 120 ? reply.substring(0,117) + "..." : reply;
                return (adminName != null && !adminName.isBlank() ? adminName + " replied: " : "Admin replied: ") + preview;
        }
}

