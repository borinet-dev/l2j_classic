DROP TABLE IF EXISTS `character_hwid`;
CREATE TABLE `character_hwid` (
  `account` varchar(45) NOT NULL,
  `char_name` varchar(35) NOT NULL DEFAULT '',
  `charId` int NOT NULL DEFAULT '0',
  `hwid` varchar(255) NOT NULL DEFAULT '0',
  PRIMARY KEY (`charId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3;