DROP TABLE IF EXISTS `name_history`;
CREATE TABLE `name_history` (
  `id` int NOT NULL AUTO_INCREMENT,
  `charId` int NOT NULL,
  `old_name` text CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `new_name` text CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `date` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 ROW_FORMAT=DYNAMIC;
