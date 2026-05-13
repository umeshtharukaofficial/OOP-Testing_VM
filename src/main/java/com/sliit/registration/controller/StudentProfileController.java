package com.sliit.registration.controller;

import com.sliit.registration.model.StudentProfile;
import com.sliit.registration.service.interfaces.IStudentService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

/**
 * StudentProfileController - Member 2. Depends on IStudentService interface.
 */
@Controller
@RequestMapping("/student/profile")
public class StudentProfileController {

    private final IStudentService studentService;

    public StudentProfileController(IStudentService studentService) {
        this.studentService = studentService;
    }

    @GetMapping
    public String profilePage(HttpSession session, Model model) {
        String studentId = (String) session.getAttribute("userId");
        if (studentId == null) return "redirect:/login";
        Optional<StudentProfile> profile = studentService.getProfile(studentId);
        model.addAttribute("profile", profile.orElse(null));
        model.addAttribute("studentId", studentId);
        return "student-profile";
    }

    @PostMapping("/create")
    public String createProfile(@RequestParam String fullName, @RequestParam String email,
                                 @RequestParam String phone, @RequestParam String program,
                                 HttpSession session, RedirectAttributes ra) {
        String studentId = (String) session.getAttribute("userId");
        if (studentId == null) return "redirect:/login";
        studentService.createProfile(studentId, fullName, email, phone, program);
        ra.addFlashAttribute("success", "Profile created!");
        return "redirect:/student/profile";
    }

    @PostMapping("/update")
    public String updateContact(@RequestParam String email, @RequestParam String phone,
                                 HttpSession session, RedirectAttributes ra) {
        String studentId = (String) session.getAttribute("userId");
        if (studentId == null) return "redirect:/login";
        boolean updated = studentService.updateContactInfo(studentId, email, phone);
        ra.addFlashAttribute(updated ? "success" : "error",
                updated ? "Contact info updated!" : "Profile not found.");
        return "redirect:/student/profile";
    }

    @PostMapping("/deactivate")
    public String deactivateProfile(HttpSession session, RedirectAttributes ra) {
        String studentId = (String) session.getAttribute("userId");
        if (studentId == null) return "redirect:/login";
        studentService.deactivateProfile(studentId);
        ra.addFlashAttribute("success", "Profile deactivated.");
        return "redirect:/login";
    }
}
