package com.sliit.registration.repository;

import com.sliit.registration.exception.DataAccessException;
import com.sliit.registration.model.CourseModule;
import com.sliit.registration.util.FileStorageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * CourseModuleRepository — File-based persistence for modules.txt
 * Member 5 (Jayathilaka W.M.U.S) — Curriculum Capacity.
 *
 * OOP: Encapsulation (private file path), Abstraction (clean CRUD API).
 * File Handling: Delegates to thread-safe FileStorageService.
 */
@Slf4j
@Repository
public class CourseModuleRepository {

    private static final String FILE_PATH = "src/main/resources/database/modules.txt";
    private final FileStorageService fileStorageService;

    @Autowired
    public CourseModuleRepository(FileStorageService fileStorageService) {
        this.fileStorageService = fileStorageService;
        log.info("CourseModuleRepository initialized — data file: {}", FILE_PATH);
    }

    /** READ: Return all modules */
    public List<CourseModule> findAll() {
        log.info("findAll() — Loading modules from: {}", FILE_PATH);
        try {
            List<String> lines = fileStorageService.readAllLines(FILE_PATH);
            List<CourseModule> modules = new ArrayList<>();
            for (String line : lines) {
                CourseModule module = new CourseModule();
                module.fromFileString(line);
                modules.add(module);
            }
            log.info("findAll() — Loaded {} modules", modules.size());
            return modules;
        } catch (DataAccessException e) {
            log.error("findAll() FAILED — {}", e.getMessage());
            throw e;
        }
    }

    /** READ: Find module by ID */
    public Optional<CourseModule> findById(String moduleId) {
        log.info("findById() — Searching for module: {}", moduleId);
        return findAll().stream().filter(m -> m.getModuleId().equals(moduleId)).findFirst();
    }

    /** CREATE or UPDATE: Save a module */
    public void save(CourseModule module) {
        log.info("save() — Saving module: {}", module.getModuleId());
        try {
            List<CourseModule> modules = findAll();
            boolean updated = false;
            for (int i = 0; i < modules.size(); i++) {
                if (modules.get(i).getModuleId().equals(module.getModuleId())) {
                    modules.set(i, module);
                    updated = true;
                    break;
                }
            }
            if (!updated) modules.add(module);

            List<String> lines = modules.stream().map(CourseModule::toFileString).collect(Collectors.toList());
            fileStorageService.writeAllLines(FILE_PATH, lines);
            log.info("save() — Module {} persisted successfully", module.getModuleId());
        } catch (DataAccessException e) {
            log.error("save() FAILED for module {} — {}", module.getModuleId(), e.getMessage());
            throw e;
        }
    }

    /** DELETE: Remove module by ID */
    public void deleteById(String moduleId) {
        log.info("deleteById() — Deleting module: {}", moduleId);
        try {
            List<CourseModule> modules = findAll();
            modules.removeIf(m -> m.getModuleId().equals(moduleId));
            List<String> lines = modules.stream().map(CourseModule::toFileString).collect(Collectors.toList());
            fileStorageService.writeAllLines(FILE_PATH, lines);
            log.info("deleteById() — Module {} removed", moduleId);
        } catch (DataAccessException e) {
            log.error("deleteById() FAILED for module {} — {}", moduleId, e.getMessage());
            throw e;
        }
    }
}
