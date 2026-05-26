CREATE TABLE flashcards (
    id uuid PRIMARY KEY,
    user_id uuid NOT NULL,
    learning_goal_id uuid NOT NULL,
    topic_id uuid NOT NULL,
    question text NOT NULL,
    answer text NOT NULL,
    difficulty varchar(50) NOT NULL DEFAULT 'MEDIUM',
    created_at timestamp with time zone NOT NULL,
    updated_at timestamp with time zone NOT NULL,
    CONSTRAINT fk_flashcards_user
        FOREIGN KEY (user_id)
        REFERENCES users (id),
    CONSTRAINT fk_flashcards_learning_goal
        FOREIGN KEY (learning_goal_id)
        REFERENCES learning_goals (id),
    CONSTRAINT fk_flashcards_topic
        FOREIGN KEY (topic_id)
        REFERENCES topics (id),
    CONSTRAINT chk_flashcards_difficulty
        CHECK (difficulty IN ('EASY', 'MEDIUM', 'HARD'))
);

CREATE INDEX idx_flashcards_user_id ON flashcards (user_id);
CREATE INDEX idx_flashcards_learning_goal_id ON flashcards (learning_goal_id);
CREATE INDEX idx_flashcards_topic_id ON flashcards (topic_id);
CREATE INDEX idx_flashcards_topic_id_difficulty ON flashcards (topic_id, difficulty);

CREATE TABLE flashcard_reviews (
    id uuid PRIMARY KEY,
    user_id uuid NOT NULL,
    flashcard_id uuid NOT NULL,
    result varchar(50) NOT NULL,
    reviewed_at timestamp with time zone NOT NULL,
    CONSTRAINT fk_flashcard_reviews_user
        FOREIGN KEY (user_id)
        REFERENCES users (id),
    CONSTRAINT fk_flashcard_reviews_flashcard
        FOREIGN KEY (flashcard_id)
        REFERENCES flashcards (id),
    CONSTRAINT chk_flashcard_reviews_result
        CHECK (result IN ('KNOW', 'PARTIAL', 'DONT_KNOW'))
);

CREATE INDEX idx_flashcard_reviews_user_id ON flashcard_reviews (user_id);
CREATE INDEX idx_flashcard_reviews_flashcard_id ON flashcard_reviews (flashcard_id);
CREATE INDEX idx_flashcard_reviews_user_id_reviewed_at ON flashcard_reviews (user_id, reviewed_at);
