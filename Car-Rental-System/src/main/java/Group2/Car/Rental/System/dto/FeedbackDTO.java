package Group2.Car.Rental.System.dto;

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
    private int rating;
    private String comments;
    private String feedbackDate;
    private String customerName;
    private Long customerId;
    private boolean isResolved;
    private String reply;
    private String replyDate;
    private String adminName;
    private Long adminId;
}