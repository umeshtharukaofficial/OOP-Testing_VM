package com.sliit.registration.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CourseModule implements FilePersistable {
    private String moduleId;
    private String moduleName;
    private int maxCapacity;
    private int currentEnrollment;

    @Override
    public String toFileString() {
        return moduleId + "|" + moduleName + "|" + maxCapacity + "|" + currentEnrollment;
    }

    @Override
    public void fromFileString(String line) {
        String[] parts = line.split("\\|");
        if (parts.length >= 4) {
            this.moduleId = parts[0];
            this.moduleName = parts[1];
            this.maxCapacity = Integer.parseInt(parts[2]);
            this.currentEnrollment = Integer.parseInt(parts[3]);
        }
    }
}
