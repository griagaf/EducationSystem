package com.example.aiplatform.learning.entity;

import com.example.aiplatform.user.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "learning_goals")
public class LearningGoal {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(nullable = false, columnDefinition = "text")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private LearningGoalType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private LearningGoalStatus status;

    @Column(name = "target_date")
    private LocalDate targetDate;

    @Column(name = "duration_weeks")
    private Integer estimatedDurationWeeks;

    @Column(name = "progress_percent", nullable = false)
    private int progressPercent;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected LearningGoal() {
    }

    public LearningGoal(
            User user,
            String title,
            String description,
            LearningGoalType type,
            LocalDate targetDate,
            Integer estimatedDurationWeeks
    ) {
        this.user = user;
        this.title = title;
        this.description = description;
        this.type = type;
        this.targetDate = targetDate;
        this.estimatedDurationWeeks = estimatedDurationWeeks;
        this.status = LearningGoalStatus.ACTIVE;
        this.progressPercent = 0;
    }

    @PrePersist
    void prePersist() {
        if (id == null) {
            id = UUID.randomUUID();
        }
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
        if (status == null) {
            status = LearningGoalStatus.ACTIVE;
        }
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = Instant.now();
    }

    public void update(
            String title,
            String description,
            LearningGoalType type,
            LearningGoalStatus status,
            LocalDate targetDate,
            Integer estimatedDurationWeeks
    ) {
        this.title = title;
        this.description = description;
        this.type = type;
        this.status = status;
        this.targetDate = targetDate;
        this.estimatedDurationWeeks = estimatedDurationWeeks;
    }

    public void archive() {
        this.status = LearningGoalStatus.ARCHIVED;
    }

    public UUID getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public LearningGoalType getType() {
        return type;
    }

    public LearningGoalStatus getStatus() {
        return status;
    }

    public LocalDate getTargetDate() {
        return targetDate;
    }

    public Integer getEstimatedDurationWeeks() {
        return estimatedDurationWeeks;
    }

    public int getProgressPercent() {
        return progressPercent;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
