-- [jooq ignore start]

INSERT INTO `mail_template` (`id`, `language`, `subject`, `markdown`)
VALUES ('USER_LOGIN_CONFIRMATION','DE','Deine Anmeldung bei Komunumo', 'Bitte klicke auf den folgenden Link, um dich bei Komunumo anzumelden:\n${login_link}'),
       ('USER_LOGIN_CONFIRMATION','EN','Your Login Request at Komunumo', 'Please click on the following link to log in to Komunumo:\n${login_link}');

-- [jooq ignore stop]
