DROP TABLE IF EXISTS `event_name`;
CREATE TABLE `event_name` (
  `id` INT NOT NULL DEFAULT 1,
  `name` VARCHAR(255) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;