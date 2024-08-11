DROP TABLE IF EXISTS `character_use_item`;
CREATE TABLE `character_use_item` (
  `charId` int NOT NULL DEFAULT '0',
  `itemId` int NOT NULL DEFAULT '0',
  PRIMARY KEY (`charId`,`itemId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3;