package Group2.Car.Rental.System.service;


import Group2.Car.Rental.System.dto.FeedbackDTO;
import Group2.Car.Rental.System.entity.Feedback;
import Group2.Car.Rental.System.entity.User;
import Group2.Car.Rental.System.repository.FeedbackRepository;
import Group2.Car.Rental.System.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class FeedbackService {

    @Autowired
    private FeedbackRepository feedbackRepository;

    @Autowired
    private UserRepository userRepository;

    public Page<FeedbackDTO> getAllFeedbacks(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return feedbackRepository.findByIsDeletedFalse(pageable).map(this::convertToDTO);
    }

    public List<FeedbackDTO> getAllFeedbacksForAdmin() {
        return feedbackRepository.findByIsDeletedFalse().stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    public FeedbackDTO getFeedbackById(Long id) {
        Optional<Feedback> feedback = feedbackRepository.findById(id);
        if (feedback.isPresent() && !feedback.get().isDeleted()) {
            return convertToDTO(feedback.get());
        }
        return null;
    }

    public FeedbackDTO createFeedback(String feedbackText,int rating) {
        User currentUser = getCurrentUser();
        Feedback feedback = new Feedback();
        feedback.setFeedback(feedbackText);
        feedback.setCreatedAt(LocalDateTime.now());
        feedback.setRating(rating);
        feedback.setCreatedBy(currentUser);
        feedback.setResolved(false);
        feedbackRepository.save(feedback);
        return convertToDTO(feedback);
    }

    public FeedbackDTO updateFeedback(Long id, String feedbackText,int rating) {
        User currentUser = getCurrentUser();
        Optional<Feedback> optionalFeedback = feedbackRepository.findById(id);
        if (optionalFeedback.isPresent()) {
            Feedback feedback = optionalFeedback.get();
            if (feedback.getCreatedBy().getId().equals(currentUser.getId()) && !feedback.isDeleted()) {
                feedback.setFeedback(feedbackText);
                feedback.setRating(rating);
                feedbackRepository.save(feedback);
                return convertToDTO(feedback);
            }
        }
        return null;
    }

    public void deleteFeedback(Long id) {
        User currentUser = getCurrentUser();
        Optional<Feedback> optionalFeedback = feedbackRepository.findById(id);
        if (optionalFeedback.isPresent()) {
            Feedback feedback = optionalFeedback.get();
            if (feedback.getCreatedBy().getId().equals(currentUser.getId()) || isAdmin(currentUser)) {
                feedback.setDeleted(true);
                feedbackRepository.save(feedback);
            }
        }
    }

    public FeedbackDTO addOrEditReply(Long id, String replyText) {
        User currentUser = getCurrentUser();
        if (!isAdmin(currentUser)) {
            return null;
        }
        Optional<Feedback> optionalFeedback = feedbackRepository.findById(id);
        if (optionalFeedback.isPresent()) {
            Feedback feedback = optionalFeedback.get();
            if (!feedback.isDeleted()) {
                feedback.setReply(replyText);
                feedback.setRepliedAt(LocalDateTime.now());
                feedback.setRepliedBy(currentUser);
                feedback.setResolved(true);
                feedbackRepository.save(feedback);
                return convertToDTO(feedback);
            }
        }
        return null;
    }

    private FeedbackDTO convertToDTO(Feedback feedback) {
        FeedbackDTO dto = new FeedbackDTO();
        dto.setId(feedback.getId());
        dto.setFeedback(feedback.getFeedback());
        dto.setReply(feedback.getReply());
        dto.setRating(feedback.getRating());
        dto.setResolved(feedback.isResolved());
        dto.setCreatedAt(LocalDateToString(feedback.getCreatedAt()));
        dto.setCreatedByName(feedback.getCreatedBy().getFirstName() + " " + feedback.getCreatedBy().getLastName());
        if (feedback.getRepliedBy() != null) {
            dto.setRepliedAt(LocalDateToString(feedback.getRepliedAt()));
            dto.setRepliedByName(feedback.getRepliedBy().getFirstName() + " " + feedback.getRepliedBy().getLastName());
        }
        return dto;
    }

    private String LocalDateToString(LocalDateTime date) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        try {
            return date.format(formatter);
        } catch (Exception e) {
            return "";
        }
    }

    private User getCurrentUser() {
        //TODO: change this code to get curent user
        return userRepository.findById(Long.valueOf("1")).orElseThrow(() -> new UsernameNotFoundException("User not found"));
//        String email = SecurityContextHolder.getContext().getAuthentication().getName();
//        return userRepository.findByEmail(email).orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    private boolean isAdmin(User user) {
        //TODO: check corect roll name
        return true;
//        String roleName = user.getRole().getName();
//        return "ROLE_OWNER".equals(roleName) || "ROLE_SYSTEM_ADMIN".equals(roleName);
    }
}
