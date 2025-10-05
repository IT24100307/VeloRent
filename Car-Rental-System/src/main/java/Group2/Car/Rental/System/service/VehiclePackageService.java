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
}

