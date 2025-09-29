package Group2.Car.Rental.System.controller;


import Group2.Car.Rental.System.dto.FeedbackDTO;
import Group2.Car.Rental.System.service.FeedbackService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/api/admin/feedback")
//@PreAuthorize("hasAnyRole('ROLE_OWNER', 'ROLE_SYSTEM_ADMIN')")
public class AdminFeedbackController {

    @Autowired
    private FeedbackService feedbackService;


    @PostMapping("/reply/{id}")
    public String addOrEditReply(@PathVariable Long id, @RequestParam String reply) {
        feedbackService.addOrEditReply(id, reply);
        return "redirect:/admin/feedback";
    }

    @PostMapping("/delete/{id}")
    public String deleteFeedback(@PathVariable Long id) {
        feedbackService.deleteFeedback(id);
        return "redirect:/admin/feedback";
    }
}