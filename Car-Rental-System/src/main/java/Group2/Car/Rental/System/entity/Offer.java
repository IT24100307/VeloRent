package Group2.Car.Rental.System.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "offers")
@Data
public class Offer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "offer_id")
    private Long offerId;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "discount", nullable = false, precision = 5, scale = 2)
    private BigDecimal discount; // Percentage discount

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "is_active", nullable = false, columnDefinition = "BIT DEFAULT 1")
    private Boolean isActive;

    // Constructor with fields
    public Offer(String name, BigDecimal discount, LocalDate startDate, LocalDate endDate) {
        this.name = name;
        this.discount = discount;
        this.startDate = startDate;
        this.endDate = endDate;
        this.isActive = true;
    }

    // Default constructor
    public Offer() {
        this.isActive = true;
    }
}
