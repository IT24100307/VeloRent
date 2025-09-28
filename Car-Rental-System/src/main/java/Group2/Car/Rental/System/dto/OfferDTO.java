package Group2.Car.Rental.System.dto;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class OfferDTO {
    private Long id;
    private int vehicleId;
    private String registrationNumber;
    private Double discount;
    private LocalDate startDate;
    private LocalDate endDate;
    private Boolean isActive;
    private Boolean isDeleted;
    private LocalDateTime createdAt;
    private String formatedCreatedAt;
    private Long createdBy;
    private String createdByUsername;
    private LocalDateTime editedAt;
    private String formatedEditedAt;
    private Long editedBy;
    private String editedByUsername;
}
