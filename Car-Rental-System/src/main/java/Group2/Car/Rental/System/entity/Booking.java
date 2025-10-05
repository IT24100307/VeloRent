package Group2.Car.Rental.System.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "bookings")
@Data
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "booking_id")
    private Integer bookingId;

    @Column(name = "start_date", nullable = false)
    private LocalDateTime startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDateTime endDate;

    @Column(name = "total_cost", nullable = false)
    private BigDecimal totalCost;

    @Column(name = "booking_status")
    private String bookingStatus = "Confirmed";

    @Column(name = "booking_type")
    private String bookingType = "VEHICLE"; // VEHICLE or PACKAGE

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @ManyToOne
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @ManyToOne
    @JoinColumn(name = "vehicle_id", nullable = true)
    private Vehicle vehicle;

    @ManyToOne
    @JoinColumn(name = "package_id", nullable = true)
    private VehiclePackage vehiclePackage;

    @ManyToOne
    @JoinColumn(name = "managed_by_staff_id", nullable = true)
    private Staff managedByStaff;

    // Default constructor
    public Booking() {
    }

    // Constructor for vehicle booking
    public Booking(LocalDateTime startDate, LocalDateTime endDate, BigDecimal totalCost,
                  Customer customer, Vehicle vehicle) {
        this.startDate = startDate;
        this.endDate = endDate;
        this.totalCost = totalCost;
        this.customer = customer;
        this.vehicle = vehicle;
        this.bookingType = "VEHICLE";
        this.createdAt = LocalDateTime.now();
    }

    // Constructor for package booking
    public Booking(LocalDateTime startDate, LocalDateTime endDate, BigDecimal totalCost,
                  Customer customer, VehiclePackage vehiclePackage) {
        this.startDate = startDate;
        this.endDate = endDate;
        this.totalCost = totalCost;
        this.customer = customer;
        this.vehiclePackage = vehiclePackage;
        this.bookingType = "PACKAGE";
        this.createdAt = LocalDateTime.now();
    }

    // Getters and setters
    public Integer getBookingId() {
        return bookingId;
    }

    public void setBookingId(Integer bookingId) {
        this.bookingId = bookingId;
    }

    public LocalDateTime getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDateTime startDate) {
        this.startDate = startDate;
    }

    public LocalDateTime getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDateTime endDate) {
        this.endDate = endDate;
    }

    public BigDecimal getTotalCost() {
        return totalCost;
    }

    public void setTotalCost(BigDecimal totalCost) {
        this.totalCost = totalCost;
    }

    public String getBookingStatus() {
        return bookingStatus;
    }

    public void setBookingStatus(String bookingStatus) {
        this.bookingStatus = bookingStatus;
    }

    public String getBookingType() {
        return bookingType;
    }

    public void setBookingType(String bookingType) {
        this.bookingType = bookingType;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public Vehicle getVehicle() {
        return vehicle;
    }

    public void setVehicle(Vehicle vehicle) {
        this.vehicle = vehicle;
    }

    public VehiclePackage getVehiclePackage() {
        return vehiclePackage;
    }

    public void setVehiclePackage(VehiclePackage vehiclePackage) {
        this.vehiclePackage = vehiclePackage;
    }

    public Staff getManagedByStaff() {
        return managedByStaff;
    }

    public void setManagedByStaff(Staff managedByStaff) {
        this.managedByStaff = managedByStaff;
    }
}
