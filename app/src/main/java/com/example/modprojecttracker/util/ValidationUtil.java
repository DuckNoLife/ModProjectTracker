package com.example.modprojecttracker.util;

import com.example.modprojecttracker.model.Project;

/** Utility methods for project validation. */
public class ValidationUtil {

    /** Returns true when all required project fields are valid. */
    public static boolean isValidProject(Project project) {
        if (project == null) {
            return false;
        }

        // Validate all required fields in one expression.
        return project.projectName != null && !project.projectName.trim().isEmpty() &&
               project.description != null && !project.description.trim().isEmpty() &&
               project.startDate > 0 &&
               project.endDate > 0 &&
               project.startDate < project.endDate &&
               project.manager != null && !project.manager.trim().isEmpty() &&
               project.status != null && !project.status.trim().isEmpty() &&
               project.budget >= Constants.MIN_BUDGET &&
               project.ownerId != null && !project.ownerId.trim().isEmpty();
    }

    /** Validates project name length. */
    public static boolean isValidProjectName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return false;
        }
        String trimmed = name.trim();
        return trimmed.length() >= Constants.MIN_PROJECT_NAME_LENGTH && 
               trimmed.length() <= Constants.MAX_PROJECT_NAME_LENGTH;
    }

    /** Validates description length. */
    public static boolean isValidDescription(String description) {
        if (description == null || description.trim().isEmpty()) {
            return false;
        }
        String trimmed = description.trim();
        return trimmed.length() >= Constants.MIN_DESCRIPTION_LENGTH && 
               trimmed.length() <= Constants.MAX_DESCRIPTION_LENGTH;
    }

    /** Validates budget is greater than or equal to minimum. */
    public static boolean isValidBudget(double budget) {
        return budget >= Constants.MIN_BUDGET;
    }

    /** Validates that start date is before end date. */
    public static boolean isValidDateRange(long startDate, long endDate) {
        return startDate > 0 && endDate > 0 && startDate < endDate;
    }

    /** Validates status against supported values. */
    public static boolean isValidStatus(String status) {
        if (status == null || status.trim().isEmpty()) {
            return false;
        }
        String s = status.trim();
        return s.equals(Constants.PROJECT_STATUS_ACTIVE) || 
               s.equals(Constants.PROJECT_STATUS_COMPLETED) || 
               s.equals(Constants.PROJECT_STATUS_ON_HOLD);
    }

    /** Returns the first validation error message in English, or null when valid. */
    public static String getProjectValidationError(Project project) {
        if (project == null) {
            return "Project cannot be null";
        }

        if (project.projectName == null || project.projectName.trim().isEmpty()) {
            return "Project name cannot be empty";
        }

        if (!isValidProjectName(project.projectName)) {
            return "Project name must be between " + Constants.MIN_PROJECT_NAME_LENGTH + 
                   " and " + Constants.MAX_PROJECT_NAME_LENGTH + " characters";
        }

        if (project.description == null || project.description.trim().isEmpty()) {
            return "Description cannot be empty";
        }

        if (!isValidDescription(project.description)) {
            return "Description must be between " + Constants.MIN_DESCRIPTION_LENGTH + 
                   " and " + Constants.MAX_DESCRIPTION_LENGTH + " characters";
        }

        if (!isValidDateRange(project.startDate, project.endDate)) {
            return "Start date must be before end date";
        }

        if (project.manager == null || project.manager.trim().isEmpty()) {
            return "Project manager cannot be empty";
        }

        if (!isValidStatus(project.status)) {
            return "Status must be: " + Constants.PROJECT_STATUS_ACTIVE + ", " + 
                   Constants.PROJECT_STATUS_COMPLETED + " or " + Constants.PROJECT_STATUS_ON_HOLD;
        }

        if (!isValidBudget(project.budget)) {
            return "Budget must be at least " + Constants.MIN_BUDGET;
        }

        if (project.ownerId == null || project.ownerId.trim().isEmpty()) {
            return "Owner ID cannot be empty";
        }

        return null;
    }
}
