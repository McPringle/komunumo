-- regular user
INSERT INTO user (id, created, updated, profile, email, name, bio, image_id, role, type)
VALUES (
           '11111111-1111-1111-1111-111111111111',
           NOW(),
           NOW(),
           '@user@example.com',
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
           '@admin@example.com',
           'admin@example.com',
           'Admin User',
           'Admin user for integration tests',
           NULL,
           'ADMIN',
           'LOCAL'
       ), (
           '33333333-3333-3333-3333-333333333333',
           NOW(),
           NOW(),
           '@remote@example.com',
           'remote@example.com',
           'Remote User',
           'Remote user for integration tests',
           NULL,
           'USER',
           'REMOTE'
       ), (
           '44444444-4444-4444-4444-444444444444',
           NOW(),
           NOW(),
           '@anonymous@example.com',
           'anonymous@example.com',
           'Anonymous User',
           'Anonymous user for integration tests',
           NULL,
           'USER',
           'ANONYMOUS'
       );
