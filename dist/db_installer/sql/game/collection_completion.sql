DROP TABLE IF EXISTS `collection_completion`;
CREATE TABLE `collection_completion` (
  `accounts` varchar(45) NOT NULL,
  `collection_id` int NOT NULL,
  `completed` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`accounts`,`collection_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3;