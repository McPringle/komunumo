-- [jooq ignore start]
INSERT INTO mail_template (id, language, subject, markdown)
VALUES ('LOGIN_CONFIRMATION_MAIL', 'DE', 'Bitte bestätige deine Anmeldung',
        'Hallo ${userName},\n\nbitte bestätige deine Anmeldung, indem du auf den folgenden Link klickst:\n\n${confirmationLink}\n\nDieser Link ist ${timeoutMinutes} Minuten gültig.\n\nViele Grüße\n${instanceName}'),
       ('LOGIN_CONFIRMATION_MAIL', 'EN', 'Please confirm your login',
        'Hello ${userName},\n\nplease confirm your login by clicking the following link:\n\n${confirmationLink}\n\nThis link is valid for ${timeoutMinutes} minutes.\n\nBest regards\n${instanceName}');
-- [jooq ignore stop]
