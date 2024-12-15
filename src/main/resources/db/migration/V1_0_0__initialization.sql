CREATE TABLE `client` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `created` DATETIME NOT NULL,
    `updated` DATETIME NOT NULL,
    `name` VARCHAR(255) NOT NULL,
    PRIMARY KEY (`id`)
);

CREATE INDEX `client_name` ON `client` (`name`);
