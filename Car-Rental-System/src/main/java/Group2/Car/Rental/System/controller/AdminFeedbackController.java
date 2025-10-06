package Group2.Car.Rental.System.controller;

import Group2.Car.Rental.System.service.FeedbackService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/api/admin/feedback")
//@PreAuthorize("hasAnyRole('ROLE_OWNER', 'ROLE_SYSTEM_ADMIN')")
public class AdminFeedbackController {

    @Autowired
    private FeedbackService feedbackService;

    @PostMapping("/reply/{id}")
    public String replyToFeedback(@PathVariable Long id, @RequestParam String reply) {
        feedbackService.replyToFeedback(id, reply);
        return "redirect:/admin/feedback";
    }

    @PostMapping("/delete/{id}")
    public String deleteFeedback(@PathVariable Long id) {
        feedbackService.deleteFeedback(id);
        return "redirect:/admin/feedback";
    }
}