-- [jooq ignore start]
UPDATE mail_template
SET markdown = 'Hallo,\n\n${participantName} hat sich soeben für "${eventTitle}" angemeldet.\n\nAktuelle Anzahl Anmeldungen: ${participantCount}\n\nViele Grüße\n${instanceName}'
WHERE id = 'EVENT_REGISTRATION_NOTIFY_MANAGERS' AND language = 'DE';

UPDATE mail_template
SET markdown = 'Hello,\n\n${participantName} just registered for "${eventTitle}".\n\nCurrent number of registrations: ${participantCount}\n\nBest regards\n${instanceName}'
WHERE id = 'EVENT_REGISTRATION_NOTIFY_MANAGERS' AND language = 'EN';
-- [jooq ignore stop]
