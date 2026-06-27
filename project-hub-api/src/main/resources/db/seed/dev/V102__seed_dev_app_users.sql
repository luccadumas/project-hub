-- Dev-only credentials: admin/admin123 and user/user123
INSERT INTO app_users (username, password_hash, role, enabled) VALUES
    ('admin', '$2a$10$PIuMJiA1KeSeoe3IA6f/Y.F1xjugjZgDCJaI5ToQ0cWXjNWSpZ2MO', 'ADMIN', TRUE),
    ('user', '$2a$10$iSlOnhSOd2B7H3Sialrh2OqYgLVi3g7bnDeXfwdhLTZ1q802l2kyG', 'USER', TRUE);
