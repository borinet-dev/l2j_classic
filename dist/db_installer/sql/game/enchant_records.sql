DROP TABLE IF EXISTS `enchant_records`;
CREATE TABLE `enchant_records` (
  `itemObjId` int NOT NULL,
  `itemId` int NOT NULL,
  `char_Id` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `item_name` varchar(255) DEFAULT NULL,
  `enchant_level` int DEFAULT NULL,
  `enchant_date` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `blessed` tinyint unsigned NOT NULL DEFAULT '0',
  PRIMARY KEY (`itemObjId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3;