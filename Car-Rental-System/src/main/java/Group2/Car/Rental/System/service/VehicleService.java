package Group2.Car.Rental.System.service;

import Group2.Car.Rental.System.entity.Vehicle;
import Group2.Car.Rental.System.repository.VehicleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class VehicleService {

    private final VehicleRepository vehicleRepository;
    private final OfferService offerService;

    @Autowired
    public VehicleService(VehicleRepository vehicleRepository, OfferService offerService) {
        this.vehicleRepository = vehicleRepository;
        this.offerService = offerService;
    }

    public List<Vehicle> getAllVehicles() {
        List<Vehicle> vehicles = vehicleRepository.findAll();
        return applyActiveDiscountsToVehicles(vehicles);
    }

    public List<Vehicle> getAvailableVehicles() {
        List<Vehicle> availableVehicles = vehicleRepository.findAll().stream()
                .filter(v -> "Available".equalsIgnoreCase(v.getStatus()))
                .collect(Collectors.toList());
        return applyActiveDiscountsToVehicles(availableVehicles);
    }

    public Optional<Vehicle> getVehicleById(Integer id) {
        Optional<Vehicle> vehicleOpt = vehicleRepository.findById(id);
        if (vehicleOpt.isPresent()) {
            Vehicle vehicle = vehicleOpt.get();
            applyActiveDiscountToVehicle(vehicle);
            return Optional.of(vehicle);
        }
        return vehicleOpt;
    }

    public Vehicle saveVehicle(Vehicle vehicle) {
        return vehicleRepository.save(vehicle);
    }

    public Vehicle updateVehicle(Vehicle vehicle) {
        return vehicleRepository.save(vehicle);
    }

    public void deleteVehicle(Integer id) {
        vehicleRepository.deleteById(id);
    }

    // Apply active offer discounts to a list of vehicles
    private List<Vehicle> applyActiveDiscountsToVehicles(List<Vehicle> vehicles) {
        BigDecimal maxDiscountPercentage = offerService.getMaxActiveDiscountPercentage();

        if (maxDiscountPercentage.compareTo(BigDecimal.ZERO) > 0) {
            vehicles.forEach(vehicle -> applyDiscountToVehicle(vehicle, maxDiscountPercentage));
        } else {
            // If no active discount, ensure all vehicles show original price
            vehicles.forEach(vehicle -> vehicle.applyDiscount(BigDecimal.ZERO));
        }

        return vehicles;
    }

    // Apply active offer discount to a single vehicle
    private void applyActiveDiscountToVehicle(Vehicle vehicle) {
        BigDecimal maxDiscountPercentage = offerService.getMaxActiveDiscountPercentage();
        applyDiscountToVehicle(vehicle, maxDiscountPercentage);
    }

    // Apply a specific discount percentage to a vehicle
    private void applyDiscountToVehicle(Vehicle vehicle, BigDecimal discountPercentage) {
        // Only apply discounts to available vehicles
        if ("Available".equalsIgnoreCase(vehicle.getStatus())) {
            vehicle.applyDiscount(discountPercentage);
        } else {
            vehicle.applyDiscount(BigDecimal.ZERO);
        }
    }
}
