package Group2.Car.Rental.System.controller;


import Group2.Car.Rental.System.dto.FeedbackDTO;
import Group2.Car.Rental.System.service.FeedbackService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/api/feedback")
//@PreAuthorize("isAuthenticated()")
public class FeedbackController {

    @Autowired
    private FeedbackService feedbackService;

    @PostMapping("/add")
    public String addFeedback(@RequestParam String comments, @RequestParam int rating) {
        feedbackService.createFeedback(comments, rating);
        return "redirect:/feedback";
    }

    @PostMapping("/update/{id}")
    public String updateFeedback(@PathVariable Long id, @RequestParam String comments, @RequestParam int rating) {
        feedbackService.updateFeedback(id, comments, rating);
        return "redirect:/feedback";
    }

    @PostMapping("/delete/{id}")
    public String deleteFeedback(@PathVariable Long id) {
        feedbackService.deleteFeedback(id);
        return "redirect:/feedback";
    }
}
