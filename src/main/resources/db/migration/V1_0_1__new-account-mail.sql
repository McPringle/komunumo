-- [jooq ignore start]
UPDATE mail_template SET markdown = 'Hallo ${name},\n\nherzlich willkommen bei ${instanceName}! Du hast erfolgreich ein neues lokales Konto bei uns erstellt.\n\nVielen Dank für dein Vertrauen!\n${instanceName}' WHERE id = 'ACCOUNT_REGISTRATION_SUCCESS' AND language = 'DE';
UPDATE mail_template SET markdown = 'Hello ${name},\n\nWelcome to ${instanceName}! You have successfully created a new local account with us.\n\nThank you for trusting us!\n${instanceName}' WHERE id = 'ACCOUNT_REGISTRATION_SUCCESS' AND language = 'EN';
-- [jooq ignore stop]
