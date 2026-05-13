package com.sliit.registration.controller;

import com.sliit.registration.dto.CourseModuleDto;
import com.sliit.registration.model.CourseModule;
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
 * ModuleController - Member 5. Depends on IAdminService interface.
 */
@Slf4j
@Controller
@RequestMapping("/admin/modules")
public class ModuleController {

    private final IAdminService adminService;

    public ModuleController(IAdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping
    public String modulesPage(Model model) {
        model.addAttribute("modules", adminService.getAllModules());
        return "admin-curriculum";
    }

    @PostMapping("/add")
    public String addModule(@Valid @ModelAttribute CourseModuleDto dto,
                             BindingResult bindingResult, RedirectAttributes ra) {
        if (bindingResult.hasErrors()) {
            log.warn("addModule() — Validation failed: {}", bindingResult.getAllErrors());
            ra.addFlashAttribute("error", "Validation failed: " + bindingResult.getFieldError().getDefaultMessage());
            return "redirect:/admin/modules";
        }
        adminService.addModule(dto);
        ra.addFlashAttribute("success", "Module " + dto.getModuleId() + " added!");
        return "redirect:/admin/modules";
    }

    @PostMapping("/update")
    public String updateCapacity(@RequestParam String moduleId, @RequestParam int newCapacity,
                                  RedirectAttributes ra) {
        boolean updated = adminService.updateModuleCapacity(moduleId, newCapacity);
        ra.addFlashAttribute(updated ? "success" : "error",
                updated ? "Capacity updated for " + moduleId : "Module not found.");
        return "redirect:/admin/modules";
    }

    @PostMapping("/delete")
    public String deleteModule(@RequestParam String moduleId, RedirectAttributes ra) {
        boolean deleted = adminService.removeModule(moduleId);
        ra.addFlashAttribute(deleted ? "success" : "error",
                deleted ? "Module " + moduleId + " removed." : "Module not found.");
        return "redirect:/admin/modules";
    }
}
