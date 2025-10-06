package Group2.Car.Rental.System.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;


@Entity
@Table(name = "vehicles")
@Data
public class Vehicle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "vehicle_id")
    private Integer vehicleId;

    @Column(name = "make", nullable = false)
    private String make;

    @Column(name = "model", nullable = false)
    private String model;

    @Column(name = "year", nullable = false)
    private Integer year;

    @Column(name = "registration_number", nullable = false, unique = true)
    private String registrationNumber;

    @Column(name = "rental_rate_per_day", nullable = false)
    private BigDecimal rentalRatePerDay;

    @Transient
    private BigDecimal discountedRatePerDay;

    @Transient
    private BigDecimal discountPercentage;

    @Column(name = "status")
    private String status = "Available";

    @Column(name = "image_url")
    private String imageUrl;

    // Default constructor
    public Vehicle() {
    }

    // Constructor with parameters
    public Vehicle(String make, String model, Integer year, String registrationNumber, BigDecimal rentalRatePerDay) {
        this.make = make;
        this.model = model;
        this.year = year;
        this.registrationNumber = registrationNumber;
        this.rentalRatePerDay = rentalRatePerDay;
    }

    // Getters and setters
    public Integer getVehicleId() {
        return vehicleId;
    }

    public void setVehicleId(Integer vehicleId) {
        this.vehicleId = vehicleId;
    }

    public String getMake() {
        return make;
    }

    public void setMake(String make) {
        this.make = make;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public String getRegistrationNumber() {
        return registrationNumber;
    }

    public void setRegistrationNumber(String registrationNumber) {
        this.registrationNumber = registrationNumber;
    }

    public BigDecimal getRentalRatePerDay() {
        return rentalRatePerDay;
    }

    public void setRentalRatePerDay(BigDecimal rentalRatePerDay) {
        this.rentalRatePerDay = rentalRatePerDay;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    @Override
    public String toString() {
        return "Vehicle{" +
                "vehicleId=" + vehicleId +
                ", make='" + make + '\'' +
                ", model='" + model + '\'' +
                ", year=" + year +
                ", registrationNumber='" + registrationNumber + '\'' +
                ", rentalRatePerDay=" + rentalRatePerDay +
                ", status='" + status + '\'' +
                ", imageUrl='" + imageUrl + '\'' +
                '}';
    }

    // Apply discount to this vehicle
    public void applyDiscount(BigDecimal discountPercentage) {
        if (discountPercentage == null || discountPercentage.compareTo(BigDecimal.ZERO) <= 0) {
            this.discountedRatePerDay = this.rentalRatePerDay;
            this.discountPercentage = BigDecimal.ZERO;
            return;
        }

        this.discountPercentage = discountPercentage;

        // Calculate discounted rate: original rate * (1 - discount/100)
        BigDecimal discountFactor = BigDecimal.ONE.subtract(
            discountPercentage.divide(new BigDecimal("100"), 4, java.math.RoundingMode.HALF_UP)
        );

        this.discountedRatePerDay = this.rentalRatePerDay.multiply(discountFactor)
            .setScale(2, java.math.RoundingMode.HALF_UP);
    }

    // Check if this vehicle has an active discount
    @JsonProperty("has_discount")
    public boolean hasDiscount() {
        return discountPercentage != null && discountPercentage.compareTo(BigDecimal.ZERO) > 0;
    }
}
