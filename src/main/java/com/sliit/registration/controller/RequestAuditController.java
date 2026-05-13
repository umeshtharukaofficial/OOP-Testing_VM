package com.sliit.registration.controller;

import com.sliit.registration.model.EnrollmentRequest;
import com.sliit.registration.service.interfaces.IModeratorService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/**
 * RequestAuditController - Member 4. Depends on IModeratorService interface.
 */
@Controller
@RequestMapping("/moderator/audit")
public class RequestAuditController {

    private final IModeratorService moderatorService;

    public RequestAuditController(IModeratorService moderatorService) {
        this.moderatorService = moderatorService;
    }

    @GetMapping
    public String auditPage(Model model) {
        model.addAttribute("allRequests", moderatorService.getAllRequestsSorted());
        model.addAttribute("pendingRequests", moderatorService.generateBatchList());
        long fortyEightHoursAgo = System.currentTimeMillis() - (48L * 60L * 60L * 1000L);
        model.addAttribute("fortyEightHoursAgo", fortyEightHoursAgo);
        return "moderator-auditing";
    }

    @PostMapping("/approve")
    public String approveRequest(@RequestParam String requestId, RedirectAttributes ra) {
        boolean approved = moderatorService.approveRequest(requestId);
        ra.addFlashAttribute(approved ? "success" : "error",
                approved ? "Request " + requestId + " approved!" : "Cannot approve — module may be full.");
        return "redirect:/moderator/audit";
    }

    @PostMapping("/reject")
    public String rejectRequest(@RequestParam String requestId, RedirectAttributes ra) {
        boolean rejected = moderatorService.rejectRequest(requestId);
        ra.addFlashAttribute(rejected ? "success" : "error",
                rejected ? "Request " + requestId + " rejected." : "Request not found.");
        return "redirect:/moderator/audit";
    }

    @PostMapping("/approve-batch")
    public String approveBatch(@RequestParam(required = false) List<String> requestIds, RedirectAttributes ra) {
        if (requestIds == null || requestIds.isEmpty()) {
            ra.addFlashAttribute("error", "No requests selected.");
            return "redirect:/moderator/audit";
        }
        int count = 0;
        for (String id : requestIds) {
            if (moderatorService.approveRequest(id)) count++;
        }
        ra.addFlashAttribute("success", "Batch approved " + count + " requests out of " + requestIds.size());
        return "redirect:/moderator/audit";
    }

    @PostMapping("/reject-batch")
    public String rejectBatch(@RequestParam(required = false) List<String> requestIds, RedirectAttributes ra) {
        if (requestIds == null || requestIds.isEmpty()) {
            ra.addFlashAttribute("error", "No requests selected.");
            return "redirect:/moderator/audit";
        }
        int count = 0;
        for (String id : requestIds) {
            if (moderatorService.rejectRequest(id)) count++;
        }
        ra.addFlashAttribute("success", "Batch rejected " + count + " requests.");
        return "redirect:/moderator/audit";
    }

    @PostMapping("/clear")
    public String clearProcessed(RedirectAttributes ra) {
        moderatorService.clearProcessedRequests();
        ra.addFlashAttribute("success", "All processed requests cleared!");
        return "redirect:/moderator/audit";
    }
}
