-- Insert a default admin user (password: admin123)
INSERT INTO users (username, email, password_hash, status) VALUES 
('admin', 'admin@hrms.com', '$argon2id$v=19$m=65536,t=3,p=4$c29tZXNhbHQ$RdescudvJCsgt3ub+bxdWRhJT527JNZYNWM4kWeu+Wc', 'ACTIVE');

-- Assign admin role to admin user
INSERT INTO user_roles (user_id, role_id) 
SELECT u.id, r.id 
FROM users u, roles r 
WHERE u.username = 'admin' AND r.name = 'ROLE_ADMIN';