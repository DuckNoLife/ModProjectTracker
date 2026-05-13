package com.example.modprojecttracker.model;

/** Project entity used by local DB and sync. */
public class Project {

    public String id;

    /** Project name. */
    public String projectName;

    /** Project description. */
    public String description;

    /** Start date in milliseconds. */
    public long startDate;

    /** End date in milliseconds. */
    public long endDate;

    /** Project manager. */
    public String manager;

    /** Project status (Active, Completed, On Hold). */
    public String status;

    /** Project budget. */
    public double budget;


    /** Optional project image URL. */
    public String imageUrl;

    /** Owner admin UID. */
    public String ownerId;

    /** Local favorite flag. */
    public boolean isFavorite;

    /** Last update timestamp for sync conflict resolution. */
    public long lastModified;

    /**
     * Soft-delete flag for sync-safe deletion.
     */
    public boolean isDeleted;

    /**
     * Deletion timestamp (milliseconds), 0 when active.
     */
    public long deletedAt;

    public Project() {
    }

    public Project(String id, String projectName, String description, long startDate,
                   long endDate, String manager, String status, double budget,
                   String ownerId) {
        this.id = id;
        this.projectName = projectName;
        this.description = description;
        this.startDate = startDate;
        this.endDate = endDate;
        this.manager = manager;
        this.status = status;
        this.budget = budget;
        this.ownerId = ownerId;
        this.isFavorite = false;
        this.lastModified = System.currentTimeMillis();
        this.isDeleted = false;
        this.deletedAt = 0L;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public long getStartDate() {
        return startDate;
    }

    public void setStartDate(long startDate) {
        this.startDate = startDate;
    }

    public long getEndDate() {
        return endDate;
    }

    public void setEndDate(long endDate) {
        this.endDate = endDate;
    }

    public String getManager() {
        return manager;
    }

    public void setManager(String manager) {
        this.manager = manager;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public double getBudget() {
        return budget;
    }

    public void setBudget(double budget) {
        this.budget = budget;
    }


    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    public boolean isFavorite() {
        return isFavorite;
    }

    public void setFavorite(boolean favorite) {
        isFavorite = favorite;
    }

    public long getLastModified() {
        return lastModified;
    }

    public void setLastModified(long lastModified) {
        this.lastModified = lastModified;
    }

    public boolean isDeleted() {
        return isDeleted;
    }

    public void setDeleted(boolean deleted) {
        isDeleted = deleted;
    }

    public long getDeletedAt() {
        return deletedAt;
    }

    public void setDeletedAt(long deletedAt) {
        this.deletedAt = deletedAt;
    }
}

