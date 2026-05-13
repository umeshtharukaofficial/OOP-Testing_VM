package com.sliit.registration.controller;

import com.sliit.registration.model.CourseModule;
import com.sliit.registration.model.EnrollmentRequest;
import com.sliit.registration.service.interfaces.IStudentService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/**
 * EnrollmentController - Member 1. Depends on IStudentService interface.
 */
@Controller
@RequestMapping("/student/enrollment")
public class EnrollmentController {

    private final IStudentService studentService;

    public EnrollmentController(IStudentService studentService) {
        this.studentService = studentService;
    }

    @GetMapping
    public String enrollmentPage(HttpSession session, Model model) {
        String studentId = (String) session.getAttribute("userId");
        if (studentId == null) return "redirect:/login";
        model.addAttribute("requests", studentService.getStudentRequests(studentId));
        model.addAttribute("modules", studentService.getAvailableModules());
        return "student-enrollment";
    }

    @PostMapping("/submit")
    public String submitRequest(@RequestParam String moduleCode, HttpSession session,
                                 RedirectAttributes ra) {
        String studentId = (String) session.getAttribute("userId");
        if (studentId == null) return "redirect:/login";
        studentService.submitEnrollmentRequest(studentId, moduleCode);
        ra.addFlashAttribute("success", "Enrollment request submitted!");
        return "redirect:/student/enrollment";
    }

    @PostMapping("/swap")
    public String swapModule(@RequestParam String requestId, @RequestParam String newModuleCode,
                              RedirectAttributes ra) {
        boolean swapped = studentService.swapModule(requestId, newModuleCode);
        ra.addFlashAttribute(swapped ? "success" : "error",
                swapped ? "Module swapped!" : "Cannot swap — request is not pending.");
        return "redirect:/student/enrollment";
    }

    @PostMapping("/withdraw")
    public String withdrawRequest(@RequestParam String requestId, RedirectAttributes ra) {
        boolean withdrawn = studentService.withdrawRequest(requestId);
        ra.addFlashAttribute(withdrawn ? "success" : "error",
                withdrawn ? "Request withdrawn!" : "Cannot withdraw — not pending.");
        return "redirect:/student/enrollment";
    }
}
