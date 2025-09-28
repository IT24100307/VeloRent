package Group2.Car.Rental.System.service;

import Group2.Car.Rental.System.dto.OfferDTO;
import Group2.Car.Rental.System.entity.Offer;
import Group2.Car.Rental.System.entity.User;
import Group2.Car.Rental.System.entity.Vehicle;
import Group2.Car.Rental.System.repository.OfferRepository;
import Group2.Car.Rental.System.repository.UserRepository;
import Group2.Car.Rental.System.repository.VehicleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OfferService {

    @Autowired
    private OfferRepository offerRepository;

    @Autowired
    private VehicleRepository vehicleRepository;

    @Autowired
    private UserRepository userRepository;

    public List<OfferDTO> getAllOffers() {
        return offerRepository.findAllNotDeleted().stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    public OfferDTO createOffer(OfferDTO offerDTO) {
        Offer offer = new Offer();
        Vehicle vehicle = vehicleRepository.findById(offerDTO.getVehicleId())
                .orElseThrow(() -> new RuntimeException("Vehicle not found"));

        User currentUser = getCurrentUser();
        offer.setVehicle(vehicle);
        offer.setDiscount(offerDTO.getDiscount());
        offer.setStartDate(offerDTO.getStartDate());
        offer.setEndDate(offerDTO.getEndDate());
        offer.setIsActive(true);
        offer.setIsDeleted(false);
        offer.setCreatedAt(LocalDateTime.now());
        offer.setCreatedBy(currentUser);
        offer = offerRepository.save(offer);
        return convertToDTO(offer);
    }

    public OfferDTO updateOffer(Long id, OfferDTO offerDTO) {
        Offer offer = offerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Offer not found"));
        User currentUser = getCurrentUser();
        offer.setDiscount(offerDTO.getDiscount());
        offer.setStartDate(offerDTO.getStartDate());
        offer.setEndDate(offerDTO.getEndDate());
        offer.setEditedAt(LocalDateTime.now());
        offer.setEditedBy(currentUser);
        offer = offerRepository.save(offer);
        return convertToDTO(offer);
    }

    public OfferDTO toggleActive(Long id, Boolean isActive) {
        Offer offer = offerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Offer not found"));
        User currentUser = getCurrentUser();
        offer.setIsActive(isActive);
        offer.setEditedAt(LocalDateTime.now());
        offer.setEditedBy(currentUser);
        offer = offerRepository.save(offer);
        return convertToDTO(offer);
    }

    public void deleteOffer(Long id) {
        Offer offer = offerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Offer not found"));
        User currentUser = getCurrentUser();
        offer.setIsDeleted(true);
        offer.setEditedAt(LocalDateTime.now());
        offer.setEditedBy(currentUser);
        offerRepository.save(offer);
    }

    public List<Vehicle> getAllVehicles() {
        return vehicleRepository.findAll();
    }
    public OfferDTO getOffer(int id) {
        Offer offer = offerRepository.findById(Long.valueOf(String.valueOf(id)))
                .orElseThrow(() -> new RuntimeException("Offer not found"));
        return convertToDTO(offer);
    }

    private User getCurrentUser() {
        //TODO: bypass for now
        return userRepository.findById(Long.parseLong("1"))
                .orElseThrow(() -> new RuntimeException("User not found"));

//        String username = SecurityContextHolder.getContext().getAuthentication().getName();
//        return userRepository.findById(Long.parseLong(username))
//                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    private OfferDTO convertToDTO(Offer offer) {
        OfferDTO dto = new OfferDTO();
        dto.setId(offer.getId());
        dto.setVehicleId(offer.getVehicle().getVehicleId());
        dto.setRegistrationNumber(offer.getVehicle().getRegistrationNumber());
        dto.setDiscount(offer.getDiscount());
        dto.setStartDate(offer.getStartDate());
        dto.setEndDate(offer.getEndDate());
        dto.setIsActive(offer.getIsActive());
        dto.setIsDeleted(offer.getIsDeleted());
        dto.setCreatedAt(offer.getCreatedAt());
        dto.setFormatedCreatedAt(LocalDateToString(offer.getCreatedAt()));
        dto.setCreatedBy(offer.getCreatedBy().getId());
        dto.setCreatedByUsername(offer.getCreatedBy().getUsername());
        dto.setEditedAt(offer.getEditedAt());
        dto.setFormatedEditedAt(LocalDateToString(offer.getEditedAt()));
        dto.setEditedBy(offer.getEditedBy() != null ? offer.getEditedBy().getId() : null);
        dto.setEditedByUsername(offer.getEditedBy() != null ? offer.getEditedBy().getUsername() : null);
        return dto;
    }

    private String LocalDateToString(LocalDateTime date) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        try{
            return date.format(formatter);
        }catch (Exception e){
            return "";
        }
    }
}