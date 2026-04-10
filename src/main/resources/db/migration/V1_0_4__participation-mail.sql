-- [jooq ignore start]
INSERT INTO mail_template (id, language, subject, markdown)
VALUES ('EVENT_REGISTRATION_NOTIFY_MANAGERS', 'DE', 'Neue Anmeldung für "${eventTitle}"',
        'Hallo,\n\n${participantName} hat sich soeben für "${eventTitle}" angemeldet.\n\nViele Grüße\n${instanceName}'),
       ('EVENT_REGISTRATION_NOTIFY_MANAGERS', 'EN', 'New registration for "${eventTitle}"',
        'Hello,\n\n${participantName} just registered for "${eventTitle}".\n\nBest regards\n${instanceName}');
-- [jooq ignore stop]
