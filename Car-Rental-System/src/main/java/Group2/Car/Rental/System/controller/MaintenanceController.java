package Group2.Car.Rental.System.controller;

import Group2.Car.Rental.System.dto.MaintenanceRequestDto;
import Group2.Car.Rental.System.dto.MaintenanceResponseDto;
import Group2.Car.Rental.System.entity.MaintenanceRecord;
import Group2.Car.Rental.System.entity.Vehicle;
import Group2.Car.Rental.System.service.MaintenanceService;
import Group2.Car.Rental.System.service.VehicleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/fleet/maintenance")
// Allow both Fleet Managers and Owners to access maintenance APIs
// Note: hasRole/hasAnyRole expect role names WITHOUT the ROLE_ prefix
@PreAuthorize("hasAnyRole('FLEET_MANAGER','OWNER')")
public class MaintenanceController {
    
    @Autowired
    private MaintenanceService maintenanceService;
    
    @Autowired
    private VehicleService vehicleService;
    
    // Get all maintenance records
    @GetMapping
    public ResponseEntity<List<MaintenanceResponseDto>> getAllMaintenance() {
        try {
            List<MaintenanceRecord> records = maintenanceService.getAllMaintenance();
            List<MaintenanceResponseDto> dtos = records.stream()
                    .map(this::convertToDto)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(dtos);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    // Get maintenance records for a specific vehicle
    @GetMapping("/vehicle/{vehicleId}")
    public ResponseEntity<List<MaintenanceResponseDto>> getMaintenanceByVehicle(@PathVariable Integer vehicleId) {
        try {
            List<MaintenanceRecord> records = maintenanceService.getMaintenanceByVehicle(vehicleId);
            List<MaintenanceResponseDto> dtos = records.stream()
                    .map(this::convertToDto)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(dtos);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    // Create new maintenance record (log or schedule)
    @PostMapping
    public ResponseEntity<MaintenanceResponseDto> createMaintenance(@RequestBody MaintenanceRequestDto request) {
        try {
            MaintenanceRecord record;
            if (request.getCost() != null) {
                // Log completed maintenance
                record = maintenanceService.logMaintenance(
                    request.getVehicleId(), 
                    request.getDescription(), 
                    request.getCost(), 
                    request.getMaintenanceDate()
                );
            } else {
                // Schedule future maintenance
                record = maintenanceService.scheduleMaintenance(
                    request.getVehicleId(), 
                    request.getDescription(), 
                    request.getMaintenanceDate()
                );
            }
            
            MaintenanceResponseDto dto = convertToDto(record);
            return ResponseEntity.status(HttpStatus.CREATED).body(dto);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }
    
    // Update maintenance record
    @PutMapping("/{id}")
    public ResponseEntity<MaintenanceResponseDto> updateMaintenance(
            @PathVariable Integer id, 
            @RequestBody MaintenanceRequestDto request) {
        try {
            Optional<MaintenanceRecord> existingRecord = maintenanceService.getMaintenanceById(id);
            if (existingRecord.isPresent()) {
                MaintenanceRecord record = existingRecord.get();
                record.setVehicleId(request.getVehicleId());
                record.setMaintenanceDate(request.getMaintenanceDate());
                record.setDescription(request.getDescription());
                record.setCost(request.getCost());
                
                MaintenanceRecord updatedRecord = maintenanceService.updateMaintenance(record);
                MaintenanceResponseDto dto = convertToDto(updatedRecord);
                return ResponseEntity.ok(dto);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }
    
    // Complete scheduled maintenance (add cost)
    @PutMapping("/{id}/complete")
    public ResponseEntity<MaintenanceResponseDto> completeMaintenance(
            @PathVariable Integer id, 
            @RequestBody Map<String, BigDecimal> costRequest) {
        try {
            BigDecimal cost = costRequest.get("cost");
            Optional<MaintenanceRecord> updatedRecord = maintenanceService.completeMaintenance(id, cost);
            
            if (updatedRecord.isPresent()) {
                MaintenanceResponseDto dto = convertToDto(updatedRecord.get());
                return ResponseEntity.ok(dto);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }
    
    // Delete maintenance record
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMaintenance(@PathVariable Integer id) {
        try {
            if (maintenanceService.getMaintenanceById(id).isPresent()) {
                maintenanceService.deleteMaintenance(id);
                return ResponseEntity.noContent().build();
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    // Get recent maintenance (last 30 days)
    @GetMapping("/recent")
    public ResponseEntity<List<MaintenanceResponseDto>> getRecentMaintenance() {
        try {
            List<MaintenanceRecord> records = maintenanceService.getRecentMaintenance();
            List<MaintenanceResponseDto> dtos = records.stream()
                    .map(this::convertToDto)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(dtos);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    // Get upcoming maintenance
    @GetMapping("/upcoming")
    public ResponseEntity<List<MaintenanceResponseDto>> getUpcomingMaintenance() {
        try {
            List<MaintenanceRecord> records = maintenanceService.getUpcomingMaintenance();
            List<MaintenanceResponseDto> dtos = records.stream()
                    .map(this::convertToDto)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(dtos);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    // Get overdue maintenance
    @GetMapping("/overdue")
    public ResponseEntity<List<MaintenanceResponseDto>> getOverdueMaintenance() {
        try {
            List<MaintenanceRecord> records = maintenanceService.getOverdueMaintenance();
            List<MaintenanceResponseDto> dtos = records.stream()
                    .map(this::convertToDto)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(dtos);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    // Get maintenance statistics
    @GetMapping("/stats")
    public ResponseEntity<MaintenanceService.MaintenanceStats> getMaintenanceStats() {
        try {
            MaintenanceService.MaintenanceStats stats = maintenanceService.getMaintenanceStats();
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    // Search maintenance by description
    @GetMapping("/search")
    public ResponseEntity<List<MaintenanceResponseDto>> searchMaintenance(@RequestParam String keyword) {
        try {
            List<MaintenanceRecord> records = maintenanceService.searchMaintenanceByDescription(keyword);
            List<MaintenanceResponseDto> dtos = records.stream()
                    .map(this::convertToDto)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(dtos);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    // Get maintenance records within date range
    @GetMapping("/date-range")
    public ResponseEntity<List<MaintenanceResponseDto>> getMaintenanceByDateRange(
            @RequestParam String startDate, 
            @RequestParam String endDate) {
        try {
            LocalDate start = LocalDate.parse(startDate);
            LocalDate end = LocalDate.parse(endDate);
            
            List<MaintenanceRecord> records = maintenanceService.getMaintenanceByDateRange(start, end);
            List<MaintenanceResponseDto> dtos = records.stream()
                    .map(this::convertToDto)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(dtos);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }
    
    // Helper method to convert entity to DTO
    private MaintenanceResponseDto convertToDto(MaintenanceRecord record) {
        MaintenanceResponseDto dto = new MaintenanceResponseDto();
        dto.setMaintenanceId(record.getMaintenanceId());
        dto.setVehicleId(record.getVehicleId());
        dto.setMaintenanceDate(record.getMaintenanceDate());
        dto.setDescription(record.getDescription());
        dto.setCost(record.getCost());
        
        // Get vehicle information
        Optional<Vehicle> vehicle = vehicleService.getVehicleById(record.getVehicleId());
        if (vehicle.isPresent()) {
            Vehicle v = vehicle.get();
            dto.setVehicleMake(v.getMake());
            dto.setVehicleModel(v.getModel());
            dto.setVehicleYear(v.getYear());
            dto.setVehicleRegistrationNumber(v.getRegistrationNumber());
        }
        
        return dto;
    }
}