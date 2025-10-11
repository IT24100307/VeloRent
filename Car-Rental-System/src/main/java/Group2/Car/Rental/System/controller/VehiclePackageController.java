package Group2.Car.Rental.System.controller;

import Group2.Car.Rental.System.dto.VehiclePackageRequest;
import Group2.Car.Rental.System.entity.VehiclePackage;
import Group2.Car.Rental.System.service.VehiclePackageService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/fleet/packages")
public class VehiclePackageController {

    private final VehiclePackageService service;

    public VehiclePackageController(VehiclePackageService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<VehiclePackage>> getAll() {
        return ResponseEntity.ok(service.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<VehiclePackage> getById(@PathVariable Integer id) {
        Optional<VehiclePackage> vp = service.getById(id);
        return vp.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<VehiclePackage> create(@RequestBody VehiclePackageRequest request) {
        VehiclePackage created = service.create(request);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<VehiclePackage> update(@PathVariable Integer id, @RequestBody VehiclePackageRequest request) {
        Optional<VehiclePackage> updated = service.update(id, request);
        return updated.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        boolean deleted = service.delete(id);
        return deleted ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<VehiclePackage> setStatus(@PathVariable Integer id, @RequestBody Map<String, String> payload) {
        String status = payload.getOrDefault("status", "Activated");
        Optional<VehiclePackage> result = service.setStatus(id, status);
        return result.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/analytics")
    public ResponseEntity<Map<String, Object>> getPackageAnalytics() {
        try {
            Map<String, Object> analytics = service.getPackageAnalytics();
            return ResponseEntity.ok(analytics);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to fetch package analytics: " + e.getMessage()));
        }
    }

    @GetMapping("/{id}/analytics")
    public ResponseEntity<Map<String, Object>> getPackageAnalyticsById(@PathVariable Integer id) {
        try {
            Map<String, Object> analytics = service.getPackageAnalyticsById(id);
            return ResponseEntity.ok(analytics);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to fetch package analytics: " + e.getMessage()));
        }
    }
}

