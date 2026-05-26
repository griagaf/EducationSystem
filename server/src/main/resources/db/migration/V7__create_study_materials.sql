CREATE TABLE study_materials (
    id uuid PRIMARY KEY,
    user_id uuid NOT NULL,
    learning_goal_id uuid NOT NULL,
    file_name varchar(255) NOT NULL,
    content_type varchar(100) NOT NULL,
    file_size bigint NOT NULL,
    storage_key varchar(500) NOT NULL,
    extracted_text text,
    processing_status varchar(50) NOT NULL DEFAULT 'UPLOADED',
    created_at timestamp with time zone NOT NULL,
    updated_at timestamp with time zone NOT NULL,
    CONSTRAINT fk_study_materials_user
        FOREIGN KEY (user_id)
        REFERENCES users (id),
    CONSTRAINT fk_study_materials_learning_goal
        FOREIGN KEY (learning_goal_id)
        REFERENCES learning_goals (id),
    CONSTRAINT chk_study_materials_file_size
        CHECK (file_size > 0),
    CONSTRAINT chk_study_materials_processing_status
        CHECK (processing_status IN ('UPLOADED', 'TEXT_EXTRACTED', 'TOPICS_EXTRACTED', 'FAILED'))
);

CREATE INDEX idx_study_materials_user_id ON study_materials (user_id);
CREATE INDEX idx_study_materials_learning_goal_id ON study_materials (learning_goal_id);
CREATE INDEX idx_study_materials_user_id_created_at ON study_materials (user_id, created_at);
CREATE INDEX idx_study_materials_processing_status ON study_materials (processing_status);
