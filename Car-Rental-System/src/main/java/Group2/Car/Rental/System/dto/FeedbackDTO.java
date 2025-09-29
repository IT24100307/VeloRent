package Group2.Car.Rental.System.dto;

import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FeedbackDTO {
    private Long id;
    private String feedback;
    private String reply;
    private int rating;
    private boolean isResolved;
    private String createdAt;
    private String createdByName;
    private String repliedAt;
    private String repliedByName;
}