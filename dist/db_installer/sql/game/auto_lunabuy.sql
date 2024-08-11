DROP TABLE IF EXISTS `auto_lunabuy`;
CREATE TABLE `auto_lunabuy` (
  `id` int NOT NULL AUTO_INCREMENT,
  `charId` int NOT NULL,
  `char_name` text NOT NULL,
  `buyer` text NOT NULL,
  `price` text NOT NULL,
  `luna` int NOT NULL,
  `checked` int NOT NULL DEFAULT '0',
  `send_time` text,
  `reward_time` text,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 ROW_FORMAT=DYNAMIC;
