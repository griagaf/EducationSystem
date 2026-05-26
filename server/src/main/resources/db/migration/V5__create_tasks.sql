CREATE TABLE tasks (
    id uuid PRIMARY KEY,
    user_id uuid NOT NULL,
    learning_goal_id uuid NOT NULL,
    roadmap_step_id uuid,
    topic_id uuid,
    title varchar(255) NOT NULL,
    description text,
    status varchar(50) NOT NULL DEFAULT 'TODO',
    priority varchar(50) NOT NULL DEFAULT 'MEDIUM',
    due_date date,
    created_at timestamp with time zone NOT NULL,
    updated_at timestamp with time zone NOT NULL,
    CONSTRAINT fk_tasks_user
        FOREIGN KEY (user_id)
        REFERENCES users (id),
    CONSTRAINT fk_tasks_learning_goal
        FOREIGN KEY (learning_goal_id)
        REFERENCES learning_goals (id),
    CONSTRAINT fk_tasks_roadmap_step
        FOREIGN KEY (roadmap_step_id)
        REFERENCES roadmap_steps (id),
    CONSTRAINT fk_tasks_topic
        FOREIGN KEY (topic_id)
        REFERENCES topics (id),
    CONSTRAINT chk_tasks_status
        CHECK (status IN ('TODO', 'IN_PROGRESS', 'DONE', 'CANCELLED')),
    CONSTRAINT chk_tasks_priority
        CHECK (priority IN ('LOW', 'MEDIUM', 'HIGH'))
);

CREATE INDEX idx_tasks_user_id ON tasks (user_id);
CREATE INDEX idx_tasks_learning_goal_id ON tasks (learning_goal_id);
CREATE INDEX idx_tasks_roadmap_step_id ON tasks (roadmap_step_id);
CREATE INDEX idx_tasks_topic_id ON tasks (topic_id);
CREATE INDEX idx_tasks_user_id_status ON tasks (user_id, status);
CREATE INDEX idx_tasks_learning_goal_id_status ON tasks (learning_goal_id, status);
CREATE INDEX idx_tasks_user_id_due_date ON tasks (user_id, due_date);
CREATE INDEX idx_tasks_user_id_priority ON tasks (user_id, priority);
