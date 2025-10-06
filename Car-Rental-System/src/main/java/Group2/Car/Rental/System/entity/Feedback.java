package Group2.Car.Rental.System.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Feedback {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "feedback_id")
    private Long id;

    @Column(name = "rating")
    private int rating;

    @Column(name = "comments")
    private String comments;

    @Column(name = "customer_name")
    private String customerName;

    @Column(name = "feedback_date")
    private LocalDateTime feedbackDate;

    @ManyToOne
    @JoinColumn(name = "customer_id", nullable = true)
    private User customer;

    @Column(name = "is_deleted", nullable = false)
    private boolean isDeleted = false;

    @Column(name = "is_resolved", nullable = false)
    private boolean isResolved = false;

    @Column(name = "reply")
    private String reply;

    @Column(name = "reply_date")
    private LocalDateTime replyDate;

    @ManyToOne
    @JoinColumn(name = "admin_id")
    private User admin;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (feedbackDate == null) {
            feedbackDate = LocalDateTime.now();
        }
    }

    // Lombok generates setters automatically, no need for custom setId method
}
