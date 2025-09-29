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
    private Long id;
    private String feedback;
    private String reply;
    private boolean isResolved;
    private LocalDateTime createdAt;
    private int rating;

    @ManyToOne
    private User createdBy;

    private LocalDateTime repliedAt;

    @ManyToOne
    private User repliedBy;

    private boolean isDeleted = false;
}
