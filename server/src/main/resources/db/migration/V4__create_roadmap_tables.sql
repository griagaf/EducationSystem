CREATE TABLE topics (
    id uuid PRIMARY KEY,
    user_id uuid NOT NULL,
    learning_goal_id uuid NOT NULL,
    title varchar(255) NOT NULL,
    description text,
    mastery_score integer NOT NULL DEFAULT 0,
    difficulty_level varchar(50) NOT NULL DEFAULT 'MEDIUM',
    created_at timestamp with time zone NOT NULL,
    updated_at timestamp with time zone NOT NULL,
    CONSTRAINT fk_topics_user
        FOREIGN KEY (user_id)
        REFERENCES users (id),
    CONSTRAINT fk_topics_learning_goal
        FOREIGN KEY (learning_goal_id)
        REFERENCES learning_goals (id),
    CONSTRAINT chk_topics_mastery_score
        CHECK (mastery_score >= 0 AND mastery_score <= 100),
    CONSTRAINT chk_topics_difficulty_level
        CHECK (difficulty_level IN ('EASY', 'MEDIUM', 'HARD'))
);

CREATE INDEX idx_topics_user_id ON topics (user_id);
CREATE INDEX idx_topics_learning_goal_id ON topics (learning_goal_id);
CREATE INDEX idx_topics_learning_goal_id_mastery_score ON topics (learning_goal_id, mastery_score);
CREATE INDEX idx_topics_learning_goal_id_difficulty_level ON topics (learning_goal_id, difficulty_level);

CREATE TABLE roadmaps (
    id uuid PRIMARY KEY,
    learning_goal_id uuid NOT NULL,
    title varchar(255) NOT NULL,
    description text NOT NULL,
    created_at timestamp with time zone NOT NULL,
    updated_at timestamp with time zone NOT NULL,
    CONSTRAINT uk_roadmaps_learning_goal_id UNIQUE (learning_goal_id),
    CONSTRAINT fk_roadmaps_learning_goal
        FOREIGN KEY (learning_goal_id)
        REFERENCES learning_goals (id)
);

CREATE INDEX idx_roadmaps_created_at ON roadmaps (created_at);

CREATE TABLE roadmap_steps (
    id uuid PRIMARY KEY,
    roadmap_id uuid NOT NULL,
    topic_id uuid,
    title varchar(255) NOT NULL,
    description text NOT NULL,
    order_index integer NOT NULL,
    estimated_days integer NOT NULL,
    status varchar(50) NOT NULL DEFAULT 'NOT_STARTED',
    created_at timestamp with time zone NOT NULL,
    updated_at timestamp with time zone NOT NULL,
    CONSTRAINT fk_roadmap_steps_roadmap
        FOREIGN KEY (roadmap_id)
        REFERENCES roadmaps (id),
    CONSTRAINT fk_roadmap_steps_topic
        FOREIGN KEY (topic_id)
        REFERENCES topics (id),
    CONSTRAINT uk_roadmap_steps_roadmap_id_order_index UNIQUE (roadmap_id, order_index),
    CONSTRAINT chk_roadmap_steps_order_index
        CHECK (order_index > 0),
    CONSTRAINT chk_roadmap_steps_estimated_days
        CHECK (estimated_days > 0),
    CONSTRAINT chk_roadmap_steps_status
        CHECK (status IN ('NOT_STARTED', 'IN_PROGRESS', 'COMPLETED'))
);

CREATE INDEX idx_roadmap_steps_roadmap_id ON roadmap_steps (roadmap_id);
CREATE INDEX idx_roadmap_steps_topic_id ON roadmap_steps (topic_id);
CREATE INDEX idx_roadmap_steps_roadmap_id_status ON roadmap_steps (roadmap_id, status);
CREATE INDEX idx_roadmap_steps_roadmap_id_order_index ON roadmap_steps (roadmap_id, order_index);
