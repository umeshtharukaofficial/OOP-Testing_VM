package com.sliit.registration.controller;

import com.sliit.registration.dto.UserRegistrationDto;
import com.sliit.registration.model.User;
import com.sliit.registration.service.interfaces.IAdminService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/**
 * UserManagementController - Member 6. Depends on IAdminService interface.
 */
@Slf4j
@Controller
@RequestMapping("/admin/users")
public class UserManagementController {

    private final IAdminService adminService;

    public UserManagementController(IAdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping
    public String usersPage(Model model) {
        model.addAttribute("users", adminService.getAllUsers());
        return "admin-security";
    }

    @PostMapping("/register-moderator")
    public String registerModerator(@Valid @ModelAttribute UserRegistrationDto dto,
                                     BindingResult bindingResult, RedirectAttributes ra) {
        if (bindingResult.hasErrors()) {
            ra.addFlashAttribute("error", "Validation failed: " + bindingResult.getFieldError().getDefaultMessage());
            return "redirect:/admin/users";
        }
        dto.setRole("MODERATOR");
        adminService.registerUser(dto);
        ra.addFlashAttribute("success", "Moderator registered!");
        return "redirect:/admin/users";
    }

    @PostMapping("/register-admin")
    public String registerAdmin(@Valid @ModelAttribute UserRegistrationDto dto,
                                 BindingResult bindingResult, RedirectAttributes ra) {
        if (bindingResult.hasErrors()) {
            ra.addFlashAttribute("error", "Validation failed: " + bindingResult.getFieldError().getDefaultMessage());
            return "redirect:/admin/users";
        }
        dto.setRole("ADMIN");
        adminService.registerUser(dto);
        ra.addFlashAttribute("success", "Admin registered!");
        return "redirect:/admin/users";
    }

    @PostMapping("/update-password")
    public String updatePassword(@RequestParam String userId, @RequestParam String newPassword,
                                  RedirectAttributes ra) {
        boolean updated = adminService.updatePassword(userId, newPassword);
        ra.addFlashAttribute(updated ? "success" : "error",
                updated ? "Password updated!" : "User not found.");
        return "redirect:/admin/users";
    }

    @PostMapping("/delete")
    public String deleteUser(@RequestParam String userId, RedirectAttributes ra) {
        boolean deleted = adminService.deleteUser(userId);
        ra.addFlashAttribute(deleted ? "success" : "error",
                deleted ? "User deleted!" : "User not found.");
        return "redirect:/admin/users";
    }
}
