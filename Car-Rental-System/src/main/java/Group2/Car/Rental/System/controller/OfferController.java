package Group2.Car.Rental.System.controller;

import Group2.Car.Rental.System.dto.OfferDTO;
import Group2.Car.Rental.System.entity.Vehicle;
import Group2.Car.Rental.System.service.OfferService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/offer")
public class OfferController {

    @Autowired
    private OfferService offerService;

    @GetMapping
    public List<OfferDTO> getAllOffers() {
        return offerService.getAllOffers();
    }

    @GetMapping("/vehicles")
    //@PreAuthorize("hasAnyRole('ROLE_SYSTEM_ADMIN', 'ROLE_OWNER')")
    public List<Vehicle> getAllVehicles() {
        return offerService.getAllVehicles();
    }

    @PostMapping
    //@PreAuthorize("hasAnyRole('ROLE_SYSTEM_ADMIN', 'ROLE_OWNER')")
    public OfferDTO createOffer(@RequestBody OfferDTO offerDTO) {
        return offerService.createOffer(offerDTO);
    }

    @GetMapping("/{id}")
    //@PreAuthorize("hasAnyRole('ROLE_SYSTEM_ADMIN', 'ROLE_OWNER')")
    public ResponseEntity<OfferDTO> updateOffer(@PathVariable int id) {
        try {
            return ResponseEntity.ok(offerService.getOffer(id));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{id}")
    //@PreAuthorize("hasAnyRole('ROLE_SYSTEM_ADMIN', 'ROLE_OWNER')")
    public ResponseEntity<OfferDTO> updateOffer(@PathVariable Long id, @RequestBody OfferDTO offerDTO) {
        try {
            return ResponseEntity.ok(offerService.updateOffer(id, offerDTO));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PatchMapping("/{id}/active")
    //@PreAuthorize("hasAnyRole('ROLE_SYSTEM_ADMIN', 'ROLE_OWNER')")
    public ResponseEntity<OfferDTO> toggleActive(@PathVariable Long id, @RequestBody Boolean isActive) {
        try {
            return ResponseEntity.ok(offerService.toggleActive(id, isActive));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    //@PreAuthorize("hasAnyRole('ROLE_SYSTEM_ADMIN', 'ROLE_OWNER')")
    public ResponseEntity<Void> deleteOffer(@PathVariable Long id) {
        try {
            offerService.deleteOffer(id);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}