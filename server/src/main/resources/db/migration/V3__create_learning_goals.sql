CREATE TABLE learning_goals (
    id uuid PRIMARY KEY,
    user_id uuid NOT NULL,
    title varchar(255) NOT NULL,
    description text NOT NULL,
    type varchar(50) NOT NULL,
    status varchar(50) NOT NULL DEFAULT 'ACTIVE',
    target_date date,
    duration_weeks integer,
    estimated_duration varchar(100),
    progress_percent integer NOT NULL DEFAULT 0,
    created_at timestamp with time zone NOT NULL,
    updated_at timestamp with time zone NOT NULL,
    CONSTRAINT fk_learning_goals_user
        FOREIGN KEY (user_id)
        REFERENCES users (id),
    CONSTRAINT chk_learning_goals_type
        CHECK (type IN ('SELF_STUDY', 'EXAM_PREPARATION', 'INTERVIEW_PREPARATION', 'TECHNOLOGY_LEARNING')),
    CONSTRAINT chk_learning_goals_status
        CHECK (status IN ('ACTIVE', 'COMPLETED', 'ARCHIVED')),
    CONSTRAINT chk_learning_goals_duration_weeks
        CHECK (duration_weeks IS NULL OR duration_weeks > 0),
    CONSTRAINT chk_learning_goals_progress_percent
        CHECK (progress_percent >= 0 AND progress_percent <= 100)
);

CREATE INDEX idx_learning_goals_user_id ON learning_goals (user_id);
CREATE INDEX idx_learning_goals_user_id_status ON learning_goals (user_id, status);
CREATE INDEX idx_learning_goals_user_id_type ON learning_goals (user_id, type);
CREATE INDEX idx_learning_goals_user_id_created_at ON learning_goals (user_id, created_at);
CREATE INDEX idx_learning_goals_target_date ON learning_goals (target_date);
