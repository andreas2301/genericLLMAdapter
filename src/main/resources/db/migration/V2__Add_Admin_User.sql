-- Migration to add admin user with credentials admin:admin
-- Password hash generated for "admin" using BCrypt
INSERT INTO user_data.users (email, password_hash, role, enabled)
VALUES ('admin', '$2a$10$8.UnVuG9HHgffUDAlk8q2OuVGkqBKkjJRqdE7HeTiZOTDWfBfW5T6', 'SUPER_USER', TRUE)
ON CONFLICT (email) DO NOTHING;
