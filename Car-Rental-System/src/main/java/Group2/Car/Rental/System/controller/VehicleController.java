package Group2.Car.Rental.System.controller;

import Group2.Car.Rental.System.entity.Vehicle;
import Group2.Car.Rental.System.service.VehicleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import Group2.Car.Rental.System.service.MaintenanceService;
import java.time.LocalDate;
import Group2.Car.Rental.System.repository.BookingRepository;
import Group2.Car.Rental.System.repository.MaintenanceRecordRepository;
import Group2.Car.Rental.System.repository.VehiclePackageRepository;
import org.springframework.dao.DataIntegrityViolationException;
import java.util.HashMap;
import java.util.Map;
import java.util.Arrays;
import Group2.Car.Rental.System.entity.VehiclePackage;

@RestController
@RequestMapping("/api/vehicles")
public class VehicleController {

    private final VehicleService vehicleService;
    @Autowired
    private MaintenanceService maintenanceService;

    // New: repositories to validate references before deletion
    private final BookingRepository bookingRepository;
    private final MaintenanceRecordRepository maintenanceRecordRepository;
    private final VehiclePackageRepository vehiclePackageRepository;

    @Autowired
    public VehicleController(VehicleService vehicleService,
                             BookingRepository bookingRepository,
                             MaintenanceRecordRepository maintenanceRecordRepository,
                             VehiclePackageRepository vehiclePackageRepository) {
        this.vehicleService = vehicleService;
        this.bookingRepository = bookingRepository;
        this.maintenanceRecordRepository = maintenanceRecordRepository;
        this.vehiclePackageRepository = vehiclePackageRepository;
    }

    @GetMapping
    public ResponseEntity<List<Vehicle>> getAllVehicles() {
        List<Vehicle> vehicles = vehicleService.getAllVehicles();
        return new ResponseEntity<>(vehicles, HttpStatus.OK);
    }

    @GetMapping("/available")
    public ResponseEntity<List<Vehicle>> getAvailableVehicles() {
        List<Vehicle> availableVehicles = vehicleService.getAvailableVehicles();
        return new ResponseEntity<>(availableVehicles, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Vehicle> getVehicleById(@PathVariable Integer id) {
        return vehicleService.getVehicleById(id)
                .map(vehicle -> new ResponseEntity<>(vehicle, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @PostMapping
    public ResponseEntity<Vehicle> addVehicle(@RequestBody Vehicle vehicle) {
        Vehicle newVehicle = vehicleService.saveVehicle(vehicle);
        try {
            if (newVehicle != null && newVehicle.getVehicleId() != null) {
                LocalDate scheduledDate = LocalDate.now().plusDays(30);
                String description = "Initial inspection & service";
                maintenanceService.scheduleMaintenance(newVehicle.getVehicleId(), description, scheduledDate);
            }
        } catch (Exception ignored) { /* do not fail vehicle creation on scheduling issues */ }
        return new ResponseEntity<>(newVehicle, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Vehicle> updateVehicle(@PathVariable Integer id, @RequestBody Vehicle vehicle) {
        return vehicleService.getVehicleById(id)
                .map(existingVehicle -> {
                    vehicle.setVehicleId(id);
                    Vehicle updatedVehicle = vehicleService.updateVehicle(vehicle);
                    return new ResponseEntity<>(updatedVehicle, HttpStatus.OK);
                })
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteVehicle(@PathVariable Integer id) {
        return vehicleService.getVehicleById(id)
                .map(vehicle -> {
                    // Only allow deleting vehicles that are currently Available
                    if (vehicle.getStatus() == null || !"Available".equalsIgnoreCase(vehicle.getStatus())) {
                        Map<String, Object> body = new HashMap<>();
                        body.put("success", false);
                        body.put("message", "Only vehicles with status 'Available' can be deleted.");
                        return new ResponseEntity<>(body, HttpStatus.CONFLICT);
                    }

                    // Block deletion if there are active/pending bookings; ignore past/returned/cancelled
                    long activeBookingRefs = 0L;
                    for (String status : Arrays.asList("Confirmed", "Rented", "Booked", "ACTIVE")) {
                        try {
                            Long c = bookingRepository.countBookingsByVehicleAndStatus(id, status);
                            activeBookingRefs += (c == null ? 0L : c);
                        } catch (Exception ignored) { }
                    }

                    if (activeBookingRefs > 0) {
                        Map<String, Object> body = new HashMap<>();
                        body.put("success", false);
                        body.put("message", "Cannot delete vehicle while it has active or pending bookings.");
                        return new ResponseEntity<>(body, HttpStatus.CONFLICT);
                    }

                    try {
                        // Detach from any packages to clear join table references
                        try {
                            for (VehiclePackage pkg : vehiclePackageRepository.findPackagesContainingVehicle(id)) {
                                pkg.getVehicles().removeIf(v -> v.getVehicleId().equals(id));
                                vehiclePackageRepository.save(pkg);
                            }
                        } catch (Exception ignored) { }

                        // Delete maintenance records for this vehicle (safe cleanup)
                        try {
                            maintenanceRecordRepository.deleteByVehicleId(id);
                        } catch (Exception ignored) { }

                        // Finally delete the vehicle
                        vehicleService.deleteVehicle(id);
                        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
                    } catch (DataIntegrityViolationException dive) {
                        Map<String, Object> body = new HashMap<>();
                        body.put("success", false);
                        body.put("message", "Cannot delete vehicle due to existing references. Remove related bookings first.");
                        return new ResponseEntity<>(body, HttpStatus.CONFLICT);
                    } catch (Exception e) {
                        Map<String, Object> body = new HashMap<>();
                        body.put("success", false);
                        body.put("message", "Failed to delete vehicle due to server error.");
                        return new ResponseEntity<>(body, HttpStatus.INTERNAL_SERVER_ERROR);
                    }
                })
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }
}
