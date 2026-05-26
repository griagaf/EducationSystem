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
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "flashcard_reviews")
public class FlashcardReview {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "flashcard_id", nullable = false)
    private Flashcard flashcard;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private FlashcardReviewResult result;

    @Column(name = "reviewed_at", nullable = false)
    private Instant reviewedAt;

    protected FlashcardReview() {
    }

    public FlashcardReview(User user, Flashcard flashcard, FlashcardReviewResult result) {
        this.user = user;
        this.flashcard = flashcard;
        this.result = result;
    }

    @PrePersist
    void prePersist() {
        if (id == null) {
            id = UUID.randomUUID();
        }
        if (reviewedAt == null) {
            reviewedAt = Instant.now();
        }
    }

    public UUID getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public Flashcard getFlashcard() {
        return flashcard;
    }

    public FlashcardReviewResult getResult() {
        return result;
    }

    public Instant getReviewedAt() {
        return reviewedAt;
    }
}
