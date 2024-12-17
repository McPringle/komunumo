CREATE TABLE `group` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `created` DATETIME NOT NULL,
    `updated` DATETIME NOT NULL,
    `name` VARCHAR(255) NOT NULL,
    PRIMARY KEY (`id`)
);

CREATE INDEX `group_name` ON `group` (`name`);
