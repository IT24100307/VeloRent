package Group2.Car.Rental.System.service;

import Group2.Car.Rental.System.entity.Offer;
import Group2.Car.Rental.System.repository.OfferRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class OfferService {
    private final OfferRepository offerRepository;

    @Autowired
    public OfferService(OfferRepository offerRepository) {
        this.offerRepository = offerRepository;
    }

    // Get all offers
    public List<Offer> getAllOffers() {
        return offerRepository.findAll();
    }

    // Get active offers
    public List<Offer> getActiveOffers() {
        LocalDate today = LocalDate.now();
        return offerRepository.findAllActive().stream()
                .filter(offer -> !offer.getStartDate().isAfter(today)
                        && !offer.getEndDate().isBefore(today))
                .collect(Collectors.toList());
    }

    // Get offer by id
    public Optional<Offer> getOfferById(Long id) {
        return offerRepository.findById(id);
    }

    // Create new offer
    public Offer createOffer(Offer offer) {
        return offerRepository.save(offer);
    }

    // Update offer
    public Optional<Offer> updateOffer(Long id, Offer updatedOffer) {
        Optional<Offer> offerOpt = offerRepository.findById(id);
        if (offerOpt.isPresent()) {
            Offer offer = offerOpt.get();
            offer.setName(updatedOffer.getName());
            offer.setDiscount(updatedOffer.getDiscount());
            offer.setStartDate(updatedOffer.getStartDate());
            offer.setEndDate(updatedOffer.getEndDate());
            return Optional.of(offerRepository.save(offer));
        }
        return Optional.empty();
    }

    // Delete offer
    public boolean deleteOffer(Long id) {
        if (offerRepository.existsById(id)) {
            offerRepository.deleteById(id);
            return true;
        }
        return false;
    }

    // Toggle active status
    public Optional<Offer> toggleActiveStatus(Long id, boolean isActive) {
        Optional<Offer> offerOpt = offerRepository.findById(id);
        if (offerOpt.isPresent()) {
            Offer offer = offerOpt.get();
            offer.setIsActive(isActive);
            return Optional.of(offerRepository.save(offer));
        }
        return Optional.empty();
    }

    // Calculate discount for a price based on active offers
    public BigDecimal applyDiscount(BigDecimal originalPrice) {
        BigDecimal maxDiscount = getMaxActiveDiscountPercentage();

        // Apply percentage discount (e.g., 10% = multiply by 0.9)
        BigDecimal multiplier = BigDecimal.ONE.subtract(maxDiscount.divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP));
        return originalPrice.multiply(multiplier).setScale(2, RoundingMode.HALF_UP);
    }

    // Get the maximum discount percentage from all active offers
    public BigDecimal getMaxActiveDiscountPercentage() {
        return getActiveOffers().stream()
                .map(Offer::getDiscount)
                .max(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);
    }
}
