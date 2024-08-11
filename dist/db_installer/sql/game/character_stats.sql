DROP TABLE IF EXISTS `character_stats`;
CREATE TABLE `character_stats` (
  `char_name` varchar(35) NOT NULL DEFAULT '',
  `charId` int NOT NULL DEFAULT '0',
  `point` int DEFAULT '0',
  `str` int DEFAULT '0',
  `dex` int DEFAULT '0',
  `con` int DEFAULT '0',
  `mpw` int DEFAULT '0',
  `wit` int DEFAULT '0',
  `men` int DEFAULT '0',
  PRIMARY KEY (`charId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3;