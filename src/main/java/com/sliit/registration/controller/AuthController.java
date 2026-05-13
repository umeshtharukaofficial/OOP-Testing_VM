package com.sliit.registration.controller;

import com.sliit.registration.dto.LoginDto;
import com.sliit.registration.model.User;
import com.sliit.registration.service.interfaces.IAuthService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.Optional;

/**
 * AuthController — depends on IAuthService interface (not concrete class).
 * Demonstrates Polymorphism: Spring injects AuthServiceImpl at runtime.
 */
@Slf4j
@Controller
public class AuthController {

    private final IAuthService authService;

    public AuthController(IAuthService authService) {
        this.authService = authService;
    }

    @GetMapping("/")
    public String home() { return "redirect:/login"; }

    @GetMapping("/login")
    public String showLoginForm(Model model) {
        model.addAttribute("loginDto", new LoginDto());
        return "login";
    }

    @PostMapping("/login")
    public String loginUser(@Valid @ModelAttribute("loginDto") LoginDto loginDto,
                            BindingResult bindingResult, HttpSession session, Model model) {
        if (bindingResult.hasErrors()) {
            log.warn("loginUser() — Validation failed: {}", bindingResult.getAllErrors());
            return "login";
        }
        Optional<User> authenticatedUser = authService.authenticate(loginDto);
        if (authenticatedUser.isPresent()) {
            User user = authenticatedUser.get();
            session.setAttribute("userId", user.getUserId());
            session.setAttribute("username", user.getUsername());
            session.setAttribute("role", user.getRole());
            return "redirect:" + user.getDashboardUrl();
        } else {
            model.addAttribute("error", "Invalid username or password");
            return "login";
        }
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }
}
