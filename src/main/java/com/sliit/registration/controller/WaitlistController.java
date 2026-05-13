package com.sliit.registration.controller;

import com.sliit.registration.model.WaitlistEntry;
import com.sliit.registration.service.interfaces.IModeratorService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/**
 * WaitlistController - Member 3. Depends on IModeratorService interface.
 */
@Controller
@RequestMapping("/moderator/waitlist")
public class WaitlistController {

    private final IModeratorService moderatorService;

    public WaitlistController(IModeratorService moderatorService) {
        this.moderatorService = moderatorService;
    }

    @GetMapping
    public String waitlistPage(Model model) {
        model.addAttribute("entries", moderatorService.getAllWaitlistEntries());
        model.addAttribute("modules", moderatorService.getAllModules());
        return "moderator-waitlist";
    }

    @PostMapping("/add")
    public String addToWaitlist(@RequestParam String studentId, @RequestParam String moduleCode,
                                 RedirectAttributes ra) {
        moderatorService.addToWaitlist(studentId, moduleCode);
        ra.addFlashAttribute("success", "Student added to waitlist!");
        return "redirect:/moderator/waitlist";
    }

    @PostMapping("/update")
    public String updatePosition(@RequestParam String waitlistId, @RequestParam int newPosition,
                                  RedirectAttributes ra) {
        boolean updated = moderatorService.updateWaitlistPosition(waitlistId, newPosition);
        ra.addFlashAttribute(updated ? "success" : "error",
                updated ? "Queue position updated!" : "Entry not found.");
        return "redirect:/moderator/waitlist";
    }

    @PostMapping("/remove")
    public String removeFromWaitlist(@RequestParam String waitlistId, RedirectAttributes ra) {
        boolean removed = moderatorService.removeFromWaitlist(waitlistId);
        ra.addFlashAttribute(removed ? "success" : "error",
                removed ? "Student removed from waitlist!" : "Entry not found.");
        return "redirect:/moderator/waitlist";
    }
}
