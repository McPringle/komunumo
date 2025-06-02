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
    password VARCHAR(255) DEFAULT NULL,
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
