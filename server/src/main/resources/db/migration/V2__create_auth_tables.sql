CREATE TABLE users (
    id uuid PRIMARY KEY,
    email varchar(255) NOT NULL,
    password_hash varchar(255) NOT NULL,
    role varchar(50) NOT NULL DEFAULT 'USER',
    is_enabled boolean NOT NULL DEFAULT true,
    created_at timestamp with time zone NOT NULL,
    updated_at timestamp with time zone NOT NULL,
    CONSTRAINT uk_users_email UNIQUE (email)
);

CREATE INDEX idx_users_is_enabled ON users (is_enabled);

CREATE TABLE user_profiles (
    id uuid PRIMARY KEY,
    user_id uuid NOT NULL,
    display_name varchar(150) NOT NULL,
    bio text,
    timezone varchar(100),
    created_at timestamp with time zone NOT NULL,
    updated_at timestamp with time zone NOT NULL,
    CONSTRAINT uk_user_profiles_user_id UNIQUE (user_id),
    CONSTRAINT fk_user_profiles_user
        FOREIGN KEY (user_id)
        REFERENCES users (id)
);
