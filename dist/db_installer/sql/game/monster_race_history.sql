DROP TABLE IF EXISTS `monster_race_history`;
CREATE TABLE `monster_race_history` (
  `race_id` mediumint NOT NULL DEFAULT '0',
  `first` int DEFAULT '0',
  `second` int DEFAULT '0',
  `odd_rate` double(10,2) DEFAULT '0.00',
  `npc_id` int NOT NULL DEFAULT '0',
  `win` int NOT NULL DEFAULT '0',
  PRIMARY KEY (`race_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3;