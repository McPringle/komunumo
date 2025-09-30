-- regular user
INSERT INTO user (id, created, updated, profile, email, name, bio, image_id, role, type)
VALUES (
           '11111111-1111-1111-1111-111111111111',
           NOW(),
           NOW(),
           '@testuser@example.com',
           'user@example.com',
           'Test User',
           'Test user for integration tests',
           NULL,
           'USER',
           'LOCAL'
       ), (
           '22222222-2222-2222-2222-222222222222',
           NOW(),
           NOW(),
           '@testadmin@example.com',
           'admin@example.com',
           'Admin User',
           'Admin user for integration tests',
           NULL,
           'ADMIN',
           'LOCAL'
       );
