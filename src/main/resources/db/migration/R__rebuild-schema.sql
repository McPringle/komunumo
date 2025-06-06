-- Drop table in opposite order to avoid foreign key constraints.

DROP TABLE IF EXISTS global_page;
DROP TABLE IF EXISTS participant;
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
    value VARCHAR(255) NOT NULL,
    PRIMARY KEY (setting)
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
    filename VARCHAR(255) NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE user (
    id VARCHAR(36) NOT NULL,
    created TIMESTAMP NOT NULL,
    updated TIMESTAMP NOT NULL,
    profile VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    name VARCHAR(255) NOT NULL,
    bio TEXT NOT NULL,
    image_id VARCHAR(36) DEFAULT NULL,
    role VARCHAR(255) NOT NULL DEFAULT 'USER',
    password_hash VARCHAR(255) DEFAULT NULL,
    CHECK (role IN ('ADMIN', 'USER')),
    PRIMARY KEY (id),
    UNIQUE uk_user_profile (profile),
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

CREATE TABLE participant (
    event_id VARCHAR(36) NOT NULL,
    user_id VARCHAR(36) NOT NULL,
    registered TIMESTAMP NOT NULL,
    PRIMARY KEY (event_id, user_id),
    CONSTRAINT fk_participant_event
        FOREIGN KEY (event_id)
            REFERENCES event (id),
    CONSTRAINT fk_participant_user
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
VALUES ('NEW_PASSWORD','DE','Dein Profil bei Komunumo', 'Dein neues Passwort, um dich bei Komunumo anzumelden:\n${password}'),
       ('NEW_PASSWORD','EN','Your profile at Komunumo', 'Your new password to log in to Komunumo:\n${password}');

INSERT INTO global_page (slot, language, created, updated, title, markdown)
VALUES
-- English version
('about', 'EN', NOW(), NOW(), 'About',
 '## About
 
 Organization  
 Firstname Lastname  
 Street and No.  
 ZIP City  
 Country
 
 E-Mail: [email@example.eu](mailto:email@example.eu)
 
 Platform of the EU Commission for online dispute resolution:  
 [https://ec.europa.eu/consumers/odr](https://ec.europa.eu/consumers/odr)
 
 ---
 
 For more information about the software used on this website, *Komunumo*, please visit the website: [https://komunumo.app](https://komunumo.app)'),

-- German version
('about', 'DE', NOW(), NOW(), 'Impressum',
 '## Impressum
 
 **Angaben gemäß § 5 TMG:**
 
 Organisation  
 Vorname Nachname  
 Strasse und Nr.  
 PLZ Ort  
 Land
 
 E-Mail: [email@example.eu](mailto:email@example.eu)
 
 **Verantwortlich für den Inhalt nach § 18 Abs. 2 MStV:**  
 Vorname Nachname (Anschrift wie oben)
 
 Keine Umsatzsteuer-Identifikationsnummer vorhanden.  
 Nicht eingetragen im Handelsregister.
 
 Plattform der EU-Kommission zur Online-Streitbeilegung:  
 [https://ec.europa.eu/consumers/odr](https://ec.europa.eu/consumers/odr)
 
 ---
 
 Für mehr Informationen über die auf dieser Webseite verwendete Software, *Komunumo*, besuche bitte die Website: [https://komunumo.app](https://komunumo.app)');

-- [jooq ignore stop]
