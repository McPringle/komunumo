CREATE TABLE `group` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `profile` VARCHAR(255) NOT NULL,
    `created` DATETIME NOT NULL,
    `updated` DATETIME NOT NULL,
    `name` VARCHAR(255) NOT NULL,
    `description` TEXT NOT NULL,
    `logo` VARCHAR(255) NOT NULL,
    `image` VARCHAR(255) NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_group_profile` (`profile`)
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
    `image` VARCHAR(255) NOT NULL DEFAULT '',
    `visibility` ENUM('public', 'private') NOT NULL DEFAULT 'public',
    `status` ENUM('draft', 'published', 'canceled') NOT NULL DEFAULT 'draft',
    PRIMARY KEY (`id`)
);
