package com.example.aiplatform.learning.repository;

import com.example.aiplatform.learning.entity.Flashcard;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FlashcardRepository extends JpaRepository<Flashcard, UUID> {

    List<Flashcard> findAllByTopicIdAndUserIdOrderByCreatedAtAsc(UUID topicId, UUID userId);

    Optional<Flashcard> findByIdAndUserId(UUID id, UUID userId);
}
