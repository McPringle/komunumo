-- Drop table in opposite order to avoid foreign key constraints.

DROP TABLE IF EXISTS global_page;
DROP TABLE IF EXISTS participant;
DROP TABLE IF EXISTS participation;
DROP TABLE IF EXISTS event;
DROP TABLE IF EXISTS member;
DROP TABLE IF EXISTS community;
DROP TABLE IF EXISTS user;
DROP TABLE IF EXISTS image;
DROP TABLE IF EXISTS mail_template;
DROP TABLE IF EXISTS config;

-- Recreate tables in correct order.

CREATE TABLE config (
    setting VARCHAR(255) NOT NULL,
    language VARCHAR(2) DEFAULT NULL,
    value VARCHAR(255) NOT NULL,
    CONSTRAINT config_unique UNIQUE (setting, language)
);

CREATE TABLE mail_template (
    id VARCHAR(255) NOT NULL,
    language VARCHAR(2) NOT NULL,
    subject VARCHAR(255) NOT NULL,
    markdown TEXT NOT NULL,
    PRIMARY KEY (`id`, `language`)
);

CREATE TABLE image (
    id VARCHAR(36) NOT NULL,
    content_type VARCHAR(255) NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE user (
    id VARCHAR(36) NOT NULL,
    created TIMESTAMP NOT NULL,
    updated TIMESTAMP NOT NULL,
    profile VARCHAR(255) DEFAULT NULL,
    email VARCHAR(255) DEFAULT NULL,
    name VARCHAR(255) NOT NULL,
    bio TEXT NOT NULL,
    image_id VARCHAR(36) DEFAULT NULL,
    role VARCHAR(255) NOT NULL DEFAULT 'USER',
    type VARCHAR(255) NOT NULL DEFAULT 'LOCAL',
    password_hash VARCHAR(255) DEFAULT NULL,
    CHECK (role IN ('ADMIN', 'USER')),
    CHECK (type IN ('LOCAL', 'REMOTE', 'ANONYMOUS')),
    PRIMARY KEY (id),
    UNIQUE uk_user_profile (profile),
    UNIQUE uk_user_email (email),
    CONSTRAINT fk_user_image
        FOREIGN KEY (image_id)
            REFERENCES image (id)
);

CREATE TABLE community (
    id VARCHAR(36) NOT NULL,
    profile VARCHAR(255) NOT NULL,
    created TIMESTAMP NOT NULL,
    updated TIMESTAMP NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT NOT NULL,
    image_id VARCHAR(36) DEFAULT NULL,
    PRIMARY KEY (id),
    UNIQUE uk_community_profile (profile),
    CONSTRAINT fk_community_image
        FOREIGN KEY (image_id)
            REFERENCES image (id)
);

CREATE TABLE member (
    user_id VARCHAR(36) NOT NULL,
    community_id VARCHAR(36) NOT NULL,
    role VARCHAR(255) NOT NULL,
    since TIMESTAMP NOT NULL,
    PRIMARY KEY (user_id, community_id),
    CONSTRAINT fk_member_user
        FOREIGN KEY (user_id)
            REFERENCES user (id),
    CONSTRAINT fk_member_community
        FOREIGN KEY (community_id)
            REFERENCES community (id)
);

CREATE TABLE event (
    id VARCHAR(36) NOT NULL,
    community_id VARCHAR(36) NOT NULL,
    created TIMESTAMP NOT NULL,
    updated TIMESTAMP NOT NULL,
    title VARCHAR(255) NOT NULL,
    description TEXT NOT NULL DEFAULT '',
    location VARCHAR(255) NOT NULL DEFAULT '',
    begin TIMESTAMP DEFAULT NULL,
    end TIMESTAMP DEFAULT NULL,
    image_id VARCHAR(36) DEFAULT NULL,
    visibility VARCHAR(255) NOT NULL DEFAULT 'PUBLIC',
    status VARCHAR(255) NOT NULL DEFAULT 'DRAFT',
    CHECK (visibility IN ('PUBLIC', 'PRIVATE')),
    CHECK (status IN ('DRAFT', 'PUBLISHED', 'CANCELED')),
    PRIMARY KEY (id),
    CONSTRAINT fk_event_community
        FOREIGN KEY (community_id)
        REFERENCES community (id),
    CONSTRAINT fk_event_image
        FOREIGN KEY (image_id)
            REFERENCES image (id)
);

CREATE TABLE participation (
    event_id VARCHAR(36) NOT NULL,
    user_id VARCHAR(36) NOT NULL,
    registered TIMESTAMP NOT NULL,
    PRIMARY KEY (event_id, user_id),
    CONSTRAINT fk_participation_event
        FOREIGN KEY (event_id)
            REFERENCES event (id),
    CONSTRAINT fk_participation_user
        FOREIGN KEY (user_id)
            REFERENCES user (id)
);

CREATE TABLE global_page (
    slot VARCHAR(255) NOT NULL,
    language VARCHAR(2) NOT NULL,
    created TIMESTAMP NOT NULL,
    updated TIMESTAMP NOT NULL,
    title VARCHAR(255) NOT NULL,
    markdown TEXT NOT NULL,
    PRIMARY KEY (slot, language)
);



-- Attention: Markdown line breaks in this file rely on two spaces at the end of a line.
-- Please make sure your editor does not remove trailing spaces when saving!
-- IntelliJ: Settings → Editor → General → On Save → set "Remove trailing spaces" to "None" or use .editorconfig.

-- [jooq ignore start]

INSERT INTO mail_template (id, language, subject, markdown)
VALUES ('CONFIRMATION_PROCESS', 'DE', 'Bitte bestätige deine E-Mail-Adresse', 'Hallo,\n\num fortzufahren, bestätige bitte deine E-Mail-Adresse, indem du auf den folgenden Link klickst:\n\n${confirmationLink}\n\nDieser Link ist 15 Minuten gültig.\n\n${confirmationReason}\n\nDanke!\n${instanceName}'),
       ('CONFIRMATION_PROCESS', 'EN', 'Please confirm your email address', 'Hello,\n\nto continue, please confirm your email address by clicking the link below:\n\n${confirmationLink}\n\nThis link is valid for 15 minutes.\n\n${confirmationReason}\n\nThank you!\n${instanceName}'),
       ('EVENT_REGISTRATION_SUCCESS', 'DE', 'Deine Anmeldung ist bestätigt', 'Hallo,\n\ndeine Anmeldung für das Event "${eventTitle}" wurde erfolgreich bestätigt.\n\nDu bist nun offiziell für das Event angemeldet.\nWir freuen uns, dich dort zu sehen!\n\nVielen Dank und bis bald!'),
       ('EVENT_REGISTRATION_SUCCESS', 'EN', 'Your registration is confirmed', 'Hello,\n\nyour registration for the event "${eventTitle}" has been successfully confirmed.\n\nYou are now officially signed up for the event.\nWe look forward to seeing you there!\n\nThank you very much and see you soon!'),
       ('NEW_PASSWORD','DE','Dein Profil bei Komunumo', 'Dein neues Passwort, um dich bei Komunumo anzumelden:\n${password}'),
       ('NEW_PASSWORD','EN','Your profile at Komunumo', 'Your new password to log in to Komunumo:\n${password}');

-- [jooq ignore stop]
