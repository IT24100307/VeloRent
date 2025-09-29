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
        return feedbackRepository.findAll(pageable).map(this::convertToDTO);
    }

    public List<FeedbackDTO> getAllFeedbacksForAdmin() {
        return feedbackRepository.findAll().stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    public FeedbackDTO getFeedbackById(Long id) {
        Optional<Feedback> feedback = feedbackRepository.findById(id);
        if (feedback.isPresent()) {
            return convertToDTO(feedback.get());
        }
        return null;
    }

    public FeedbackDTO createFeedback(String comments, int rating) {
        User currentUser = getCurrentUser();
        Feedback feedback = new Feedback();
        feedback.setComments(comments);
        feedback.setFeedbackDate(LocalDateTime.now());
        feedback.setRating(rating);
        feedback.setCustomer(currentUser);
        feedbackRepository.save(feedback);
        return convertToDTO(feedback);
    }

    public FeedbackDTO updateFeedback(Long id, String comments, int rating) {
        User currentUser = getCurrentUser();
        Optional<Feedback> optionalFeedback = feedbackRepository.findById(id);
        if (optionalFeedback.isPresent()) {
            Feedback feedback = optionalFeedback.get();
            // Check if user owns the feedback AND there's no admin reply yet
            if (feedback.getCustomer().getId().equals(currentUser.getId()) && feedback.getReply() == null) {
                feedback.setComments(comments);
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
            if (feedback.getCustomer().getId().equals(currentUser.getId()) || isAdmin(currentUser)) {
                feedbackRepository.delete(feedback);
            }
        }
    }

    public FeedbackDTO replyToFeedback(Long id, String reply) {
        User currentUser = getCurrentUser();
        if (!isAdmin(currentUser)) {
            return null; // Only admins can reply to feedback
        }

        Optional<Feedback> optionalFeedback = feedbackRepository.findById(id);
        if (optionalFeedback.isPresent()) {
            Feedback feedback = optionalFeedback.get();
            feedback.setReply(reply);
            feedback.setReplyDate(LocalDateTime.now());
            feedback.setAdmin(currentUser);
            feedback.setResolved(true);
            feedbackRepository.save(feedback);
            return convertToDTO(feedback);
        }
        return null;
    }

    private FeedbackDTO convertToDTO(Feedback feedback) {
        FeedbackDTO dto = new FeedbackDTO();
        dto.setId(feedback.getId());
        dto.setRating(feedback.getRating());
        dto.setComments(feedback.getComments());
        dto.setFeedbackDate(LocalDateToString(feedback.getFeedbackDate()));
        dto.setCustomerId(feedback.getCustomer().getId());
        dto.setCustomerName(feedback.getCustomer().getFirstName() + " " + feedback.getCustomer().getLastName());
        dto.setResolved(feedback.isResolved());

        // Add reply-related data if available
        if (feedback.getReply() != null) {
            dto.setReply(feedback.getReply());
            dto.setReplyDate(LocalDateToString(feedback.getReplyDate()));

            if (feedback.getAdmin() != null) {
                dto.setAdminId(feedback.getAdmin().getId());
                dto.setAdminName(feedback.getAdmin().getFirstName() + " " + feedback.getAdmin().getLastName());
            }
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
        // Get the current authentication from the security context
        var authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.isAuthenticated()) {
            String email = authentication.getName();

            // Skip if it's the anonymous user
            if ("anonymousUser".equals(email)) {
                return userRepository.findById(1L)
                    .orElseThrow(() -> new UsernameNotFoundException("Default user not found"));
            }

            // Try to find the user by email
            var user = userRepository.findByEmail(email);
            if (user.isPresent()) {
                System.out.println("Found authenticated user: " + user.get().getFirstName() + " " + user.get().getLastName());
                return user.get();
            }
        }

        System.out.println("Falling back to default user");
        // Fall back to default user if no authenticated user found
        return userRepository.findById(1L)
            .orElseThrow(() -> new UsernameNotFoundException("Default user not found"));
    }

    private boolean isAdmin(User user) {
        //TODO: check corect roll name
        return true;
//        String roleName = user.getRole().getName();
//        return "ROLE_OWNER".equals(roleName) || "ROLE_SYSTEM_ADMIN".equals(roleName);
    }
}
