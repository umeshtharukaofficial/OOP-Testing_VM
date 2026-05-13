package com.sliit.registration.controller;

import com.sliit.registration.model.EnrollmentRequest;
import com.sliit.registration.service.interfaces.IAdminService;
import com.sliit.registration.service.interfaces.IStudentService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

/**
 * DashboardController — depends on IStudentService and IAdminService interfaces.
 */
@Controller
public class DashboardController {

    private final IStudentService studentService;
    private final IAdminService adminService;

    public DashboardController(IStudentService studentService, IAdminService adminService) {
        this.studentService = studentService;
        this.adminService = adminService;
    }

    @GetMapping("/student-home")
    public String studentDashboard(HttpSession session, Model model) {
        String userId = (String) session.getAttribute("userId");
        if (userId == null) return "redirect:/login";
        model.addAttribute("username", session.getAttribute("username"));
        List<EnrollmentRequest> requests = studentService.getStudentRequests(userId);
        model.addAttribute("requests", requests);
        return "student-home";
    }

    @GetMapping("/moderator-home")
    public String moderatorDashboard(HttpSession session, Model model) {
        String userId = (String) session.getAttribute("userId");
        if (userId == null) return "redirect:/login";
        model.addAttribute("username", session.getAttribute("username"));
        return "moderator-home";
    }

    @GetMapping("/admin-home")
    public String adminDashboard(HttpSession session, Model model) {
        String userId = (String) session.getAttribute("userId");
        if (userId == null) return "redirect:/login";
        model.addAttribute("username", session.getAttribute("username"));
        model.addAttribute("totalModules", adminService.getAllModules().size());
        model.addAttribute("totalUsers", adminService.getAllUsers().size());
        return "admin-home";
    }
}
