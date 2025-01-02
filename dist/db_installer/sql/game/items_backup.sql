DROP TABLE IF EXISTS `items_backup`;
CREATE TABLE `items_backup` (
  `owner_id` int(11) DEFAULT NULL,
  `object_id` int(11) NOT NULL DEFAULT 0,
  `item_id` int(11) DEFAULT NULL,
  `count` bigint(20) unsigned NOT NULL DEFAULT 0,
  `enchant_level` int(11) DEFAULT NULL,
  `loc` varchar(10) DEFAULT NULL,
  `loc_data` int(11) DEFAULT NULL,
  `time_of_use` int(11) DEFAULT NULL,
  `custom_type1` int(11) DEFAULT 0,
  `custom_type2` int(11) DEFAULT 0,
  `mana_left` decimal(5,0) NOT NULL DEFAULT -1,
  `time` decimal(13,0) NOT NULL DEFAULT 0,
  PRIMARY KEY (`object_id`),
  KEY `owner_id` (`owner_id`),
  KEY `item_id` (`item_id`),
  KEY `loc` (`loc`),
  KEY `time_of_use` (`time_of_use`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_general_ci;
