package com.sliit.registration.service;

import com.sliit.registration.dto.CourseModuleDto;
import com.sliit.registration.dto.UserRegistrationDto;
import com.sliit.registration.model.*;
import com.sliit.registration.repository.CourseModuleRepository;
import com.sliit.registration.repository.UserRepository;
import com.sliit.registration.service.interfaces.IAdminService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * AdminServiceImpl — implements IAdminService.
 * Consolidates Member 5 (Module Mgmt) + Member 6 (User Mgmt) logic.
 *
 * OOP: Abstraction (interface), Polymorphism (User subclass creation),
 * Encapsulation (private repos), Information Hiding (UUID strategy hidden).
 * Constructor Injection — no @Autowired fields.
 */
@Slf4j
@Service
public class AdminServiceImpl implements IAdminService {

    private final CourseModuleRepository moduleRepository;
    private final UserRepository userRepository;

    public AdminServiceImpl(CourseModuleRepository moduleRepository, UserRepository userRepository) {
        this.moduleRepository = moduleRepository;
        this.userRepository = userRepository;
        log.info("AdminServiceImpl initialized via constructor injection");
    }

    // ─── MODULE MANAGEMENT (Member 5) ───

    @Override
    public CourseModule addModule(CourseModuleDto dto) {
        log.info("addModule() — DTO: id='{}', name='{}', capacity={}", dto.getModuleId(), dto.getModuleName(), dto.getMaxCapacity());
        CourseModule module = new CourseModule(dto.getModuleId(), dto.getModuleName(), dto.getMaxCapacity(), 0);
        moduleRepository.save(module);
        log.info("addModule() — Module '{}' persisted", dto.getModuleId());
        return module;
    }

    @Override
    public List<CourseModule> getAllModules() {
        return moduleRepository.findAll();
    }

    @Override
    public Optional<CourseModule> getModuleById(String moduleId) {
        return moduleRepository.findById(moduleId);
    }

    @Override
    public boolean updateModuleCapacity(String moduleId, int newCapacity) {
        log.info("updateModuleCapacity() — module: '{}', newCapacity: {}", moduleId, newCapacity);
        if (newCapacity < 1) { log.warn("Capacity must be >= 1"); return false; }
        Optional<CourseModule> opt = moduleRepository.findById(moduleId);
        if (opt.isPresent()) {
            opt.get().setMaxCapacity(newCapacity);
            moduleRepository.save(opt.get());
            log.info("updateModuleCapacity() — Updated");
            return true;
        }
        log.warn("updateModuleCapacity() — Module not found");
        return false;
    }

    @Override
    public boolean removeModule(String moduleId) {
        log.info("removeModule() — Removing: '{}'", moduleId);
        Optional<CourseModule> opt = moduleRepository.findById(moduleId);
        if (opt.isPresent()) { moduleRepository.deleteById(moduleId); return true; }
        log.warn("removeModule() — Not found"); return false;
    }

    // ─── USER MANAGEMENT (Member 6) ───

    @Override
    public User registerUser(UserRegistrationDto dto) {
        log.info("registerUser() — username='{}', role='{}'", dto.getUsername(), dto.getRole());
        User user;
        switch (dto.getRole()) {
            case "MODERATOR":
                String modId = "MOD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
                user = new Moderator(modId, dto.getUsername(), dto.getPassword(), dto.getDepartmentId());
                break;
            case "ADMIN":
                String admId = "ADM-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
                user = new Admin(admId, dto.getUsername(), dto.getPassword(), dto.getClearanceLevel());
                break;
            default:
                throw new IllegalArgumentException("Invalid role: " + dto.getRole());
        }
        userRepository.save(user);
        log.info("registerUser() — User '{}' persisted (role: {})", user.getUserId(), user.getRole());
        return user;
    }

    @Override
    public List<User> getAllUsers() { return userRepository.findAll(); }

    @Override
    public Optional<User> getUserById(String userId) { return userRepository.findById(userId); }

    @Override
    public boolean updatePassword(String userId, String newPassword) {
        log.info("updatePassword() — user: '{}'", userId);
        Optional<User> opt = userRepository.findById(userId);
        if (opt.isPresent()) { opt.get().setPasswordHash(newPassword); userRepository.save(opt.get()); return true; }
        return false;
    }

    @Override
    public boolean deleteUser(String userId) {
        log.info("deleteUser() — user: '{}'", userId);
        Optional<User> opt = userRepository.findById(userId);
        if (opt.isPresent()) { userRepository.delete(userId); return true; }
        return false;
    }
}
