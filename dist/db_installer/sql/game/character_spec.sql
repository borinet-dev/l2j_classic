DROP TABLE IF EXISTS `character_spec`;
CREATE TABLE `character_spec` (
  `char_name` varchar(35) NOT NULL DEFAULT '',
  `charId` int NOT NULL DEFAULT '0',
  `point` int DEFAULT '0',
  `spec1` int DEFAULT '0',
  `spec2` int DEFAULT '0',
  `spec3` int DEFAULT '0',
  `spec4` int DEFAULT '0',
  `spec5` int DEFAULT '0',
  `spec6` int DEFAULT '0',
  `spec7` int DEFAULT '0',
  `spec8` int DEFAULT '0',
  PRIMARY KEY (`charId`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8mb3;