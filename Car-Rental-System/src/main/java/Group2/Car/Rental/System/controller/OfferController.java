package Group2.Car.Rental.System.controller;

import Group2.Car.Rental.System.entity.Offer;
import Group2.Car.Rental.System.service.OfferService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/admin/offer")
public class OfferController {

    private final OfferService offerService;

    @Autowired
    public OfferController(OfferService offerService) {
        this.offerService = offerService;
    }

    @GetMapping
    public ResponseEntity<List<Offer>> getAllOffers() {
        return ResponseEntity.ok(offerService.getAllOffers());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Offer> getOfferById(@PathVariable Long id) {
        Optional<Offer> offer = offerService.getOfferById(id);
        return offer.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Offer> createOffer(@RequestBody Map<String, Object> payload) {
        Offer offer = new Offer();
        offer.setName((String) payload.get("name"));
        offer.setDiscount(new BigDecimal(payload.get("discount").toString()));
        offer.setStartDate(LocalDate.parse(payload.get("startDate").toString()));
        offer.setEndDate(LocalDate.parse(payload.get("endDate").toString()));
        offer.setIsActive(true);

        Offer createdOffer = offerService.createOffer(offer);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdOffer);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Offer> updateOffer(@PathVariable Long id, @RequestBody Map<String, Object> payload) {
        Offer offer = new Offer();
        offer.setName((String) payload.get("name"));
        offer.setDiscount(new BigDecimal(payload.get("discount").toString()));
        offer.setStartDate(LocalDate.parse(payload.get("startDate").toString()));
        offer.setEndDate(LocalDate.parse(payload.get("endDate").toString()));

        Optional<Offer> updatedOffer = offerService.updateOffer(id, offer);
        return updatedOffer.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOffer(@PathVariable Long id) {
        boolean deleted = offerService.deleteOffer(id);
        return deleted ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }

    @PatchMapping("/{id}/active")
    public ResponseEntity<Offer> toggleActiveStatus(@PathVariable Long id, @RequestBody Boolean isActive) {
        Optional<Offer> updatedOffer = offerService.toggleActiveStatus(id, isActive);
        return updatedOffer.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
