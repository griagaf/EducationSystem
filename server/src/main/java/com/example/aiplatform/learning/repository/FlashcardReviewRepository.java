package com.example.aiplatform.learning.repository;

import com.example.aiplatform.learning.entity.FlashcardReview;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FlashcardReviewRepository extends JpaRepository<FlashcardReview, UUID> {
}
