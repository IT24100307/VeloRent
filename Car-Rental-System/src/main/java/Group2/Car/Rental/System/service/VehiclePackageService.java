package Group2.Car.Rental.System.service;

import Group2.Car.Rental.System.dto.VehiclePackageRequest;
import Group2.Car.Rental.System.entity.Vehicle;
import Group2.Car.Rental.System.entity.VehiclePackage;
import Group2.Car.Rental.System.repository.VehiclePackageRepository;
import Group2.Car.Rental.System.repository.VehicleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class VehiclePackageService {

    private final VehiclePackageRepository vehiclePackageRepository;
    private final VehicleRepository vehicleRepository;

    public VehiclePackageService(VehiclePackageRepository vehiclePackageRepository,
                                 VehicleRepository vehicleRepository) {
        this.vehiclePackageRepository = vehiclePackageRepository;
        this.vehicleRepository = vehicleRepository;
    }

    public List<VehiclePackage> getAll() {
        return vehiclePackageRepository.findAll();
    }

    public List<VehiclePackage> getActivatedPackages() {
        return vehiclePackageRepository.findByStatus("Activated");
    }

    /**
     * Get all visible packages (both activated and partially reserved)
     */
    public List<VehiclePackage> getVisiblePackages() {
        List<VehiclePackage> activated = vehiclePackageRepository.findByStatus("Activated");
        List<VehiclePackage> partiallyReserved = vehiclePackageRepository.findByStatus("Partially Reserved");
        activated.addAll(partiallyReserved);
        return activated;
    }

    public Optional<VehiclePackage> getById(Integer id) {
        return vehiclePackageRepository.findById(id);
    }

    @Transactional
    public VehiclePackage create(VehiclePackageRequest request) {
        VehiclePackage vp = new VehiclePackage();
        applyToEntity(vp, request);
        return vehiclePackageRepository.save(vp);
    }

    @Transactional
    public Optional<VehiclePackage> update(Integer id, VehiclePackageRequest request) {
        return vehiclePackageRepository.findById(id).map(existing -> {
            applyToEntity(existing, request);
            return vehiclePackageRepository.save(existing);
        });
    }

    @Transactional
    public boolean delete(Integer id) {
        if (!vehiclePackageRepository.existsById(id)) return false;
        vehiclePackageRepository.deleteById(id);
        return true;
    }

    @Transactional
    public Optional<VehiclePackage> setStatus(Integer id, String status) {
        return vehiclePackageRepository.findById(id).map(existing -> {
            existing.setStatus(status);
            return vehiclePackageRepository.save(existing);
        });
    }

    private void applyToEntity(VehiclePackage vp, VehiclePackageRequest request) {
        if (request.getPackageName() != null) vp.setPackageName(request.getPackageName());
        if (request.getPrice() != null) vp.setPrice(request.getPrice());
        if (request.getDuration() != null) vp.setDuration(request.getDuration());
        if (request.getImageUrl() != null) vp.setImageUrl(request.getImageUrl());
        if (request.getStatus() != null) vp.setStatus(request.getStatus());

        if (request.getVehicleIds() != null) {
            Iterable<Vehicle> vehicles = vehicleRepository.findAllById(request.getVehicleIds());
            Set<Vehicle> set = new HashSet<>();
            vehicles.forEach(set::add);
            vp.setVehicles(set);
        }
    }

    /**
     * Mark packages containing this vehicle as partially reserved (but still visible)
     * This is called when a vehicle is rented individually
     */
    @Transactional
    public void markPackagesAsPartiallyReserved(Integer vehicleId) {
        List<VehiclePackage> packages = vehiclePackageRepository.findActivatedPackagesContainingVehicle(vehicleId);
        for (VehiclePackage pkg : packages) {
            pkg.setStatus("Partially Reserved");
            vehiclePackageRepository.save(pkg);
        }
    }

    /**
     * Restore packages to activated status when vehicle is returned
     * This is called when a vehicle is returned from individual rental
     */
    @Transactional
    public void restorePackagesFromPartialReservation(Integer vehicleId) {
        List<VehiclePackage> packages = vehiclePackageRepository.findPackagesContainingVehicle(vehicleId);
        for (VehiclePackage pkg : packages) {
            // Only restore if it was partially reserved
            if ("Partially Reserved".equals(pkg.getStatus())) {
                // Check if all vehicles in the package are now available
                boolean allVehiclesAvailable = pkg.getVehicles().stream()
                    .allMatch(vehicle -> "Available".equals(vehicle.getStatus()));
                
                if (allVehiclesAvailable) {
                    pkg.setStatus("Activated");
                    vehiclePackageRepository.save(pkg);
                }
            }
        }
    }

    /**
     * Check if a package can be booked (not partially reserved)
     */
    public boolean isPackageAvailableForBooking(Integer packageId) {
        Optional<VehiclePackage> packageOpt = vehiclePackageRepository.findById(packageId);
        if (packageOpt.isPresent()) {
            VehiclePackage pkg = packageOpt.get();
            return "Activated".equals(pkg.getStatus());
        }
        return false;
    }

    /**
     * Get package booking availability message
     */
    public String getPackageAvailabilityMessage(Integer packageId) {
        Optional<VehiclePackage> packageOpt = vehiclePackageRepository.findById(packageId);
        if (packageOpt.isPresent()) {
            VehiclePackage pkg = packageOpt.get();
            switch (pkg.getStatus()) {
                case "Activated":
                    return "Available for booking";
                case "Partially Reserved":
                    return "Temporarily unavailable - One or more vehicles are currently rented individually";
                case "Deactivated":
                    return "Package is currently unavailable";
                default:
                    return "Status unknown";
            }
        }
        return "Package not found";
    }

    /**
     * Get packages containing a specific vehicle
     */
    public List<VehiclePackage> getPackagesContainingVehicle(Integer vehicleId) {
        return vehiclePackageRepository.findPackagesContainingVehicle(vehicleId);
    }

    /**
     * Get activated packages containing a specific vehicle
     */
    public List<VehiclePackage> getActivatedPackagesContainingVehicle(Integer vehicleId) {
        return vehiclePackageRepository.findActivatedPackagesContainingVehicle(vehicleId);
    }
}

