package Group2.Car.Rental.System.controller;

import Group2.Car.Rental.System.dto.FeedbackDTO;
import Group2.Car.Rental.System.service.FeedbackService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/api/feedback")
//@PreAuthorize("isAuthenticated()")
public class FeedbackController {

    @Autowired
    private FeedbackService feedbackService;

    @PostMapping("/add")
    public String addFeedback(@RequestParam String comments, 
                            @RequestParam int rating, 
                            @RequestParam String customerName,
                            RedirectAttributes redirectAttributes) {
        try {
            // Validate input
            if (comments == null || comments.trim().isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Feedback comment cannot be empty.");
                return "redirect:/feedback";
            }
            
            if (customerName == null || customerName.trim().isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Customer name cannot be empty.");
                return "redirect:/feedback";
            }
            
            if (rating < 1 || rating > 5) {
                redirectAttributes.addFlashAttribute("error", "Rating must be between 1 and 5.");
                return "redirect:/feedback";
            }

            FeedbackDTO feedback = feedbackService.createFeedback(comments.trim(), rating, customerName.trim());
            
            if (feedback != null) {
                redirectAttributes.addFlashAttribute("message", 
                    "Thank you for your feedback! Your review has been submitted successfully.");
            } else {
                redirectAttributes.addFlashAttribute("error", "Failed to submit feedback. Please try again.");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", 
                "An error occurred while submitting your feedback: " + e.getMessage());
        }
        
        return "redirect:/feedback";
    }

    @PostMapping("/update/{id}")
    public String updateFeedback(@PathVariable Long id, 
                               @RequestParam String feedback, 
                               @RequestParam int rating, 
                               @RequestParam String customerName,
                               RedirectAttributes redirectAttributes) {
        try {
            FeedbackDTO updatedFeedback = feedbackService.updateFeedback(id, feedback, rating, customerName);
            
            if (updatedFeedback != null) {
                redirectAttributes.addFlashAttribute("message", "Feedback updated successfully.");
            } else {
                redirectAttributes.addFlashAttribute("error", "Failed to update feedback.");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "An error occurred while updating feedback: " + e.getMessage());
        }
        
        return "redirect:/feedback";
    }

    @PostMapping("/delete/{id}")
    public String deleteFeedback(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            feedbackService.deleteFeedback(id);
            redirectAttributes.addFlashAttribute("message", "Feedback deleted successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "An error occurred while deleting feedback: " + e.getMessage());
        }
        
        return "redirect:/feedback";
    }
}
