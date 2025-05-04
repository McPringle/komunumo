CREATE TABLE `config` (
    `key` VARCHAR(255) NOT NULL,
    `value` VARCHAR(255) NOT NULL,
    PRIMARY KEY `pk_config` (`key`)
);

CREATE TABLE `image` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `type` ENUM('image/gif', 'image/jpeg', 'image/png', 'image/svg+xml') NOT NULL,
    `filename` VARCHAR(255) NOT NULL,
    PRIMARY KEY `pk_image` (`id`)
);

CREATE TABLE `group` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `profile` VARCHAR(255) NOT NULL,
    `created` DATETIME NOT NULL,
    `updated` DATETIME NOT NULL,
    `name` VARCHAR(255) NOT NULL,
    `description` TEXT NOT NULL,
    `image_id` BIGINT DEFAULT NULL,
    PRIMARY KEY `pk_group` (`id`),
    UNIQUE KEY `uk_group_profile` (`profile`),
    CONSTRAINT `fk_group_image`
        FOREIGN KEY (`image_id`)
            REFERENCES `image` (`id`)
);

CREATE TABLE `event` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `group_id` BIGINT NOT NULL,
    `created` DATETIME NOT NULL,
    `updated` DATETIME NOT NULL,
    `title` VARCHAR(255) NOT NULL,
    `description` TEXT NOT NULL DEFAULT '',
    `location` VARCHAR(255) NOT NULL DEFAULT '',
    `begin` DATETIME DEFAULT NULL,
    `end` DATETIME DEFAULT NULL,
    `image_id` BIGINT DEFAULT NULL,
    `visibility` ENUM('public', 'private') NOT NULL DEFAULT 'public',
    `status` ENUM('draft', 'published', 'canceled') NOT NULL DEFAULT 'draft',
    PRIMARY KEY `pk_event` (`id`),
    CONSTRAINT `fk_event_group`
        FOREIGN KEY (`group_id`)
        REFERENCES `group` (`id`),
    CONSTRAINT `fk_event_image`
        FOREIGN KEY (`image_id`)
            REFERENCES `image` (`id`)
);
