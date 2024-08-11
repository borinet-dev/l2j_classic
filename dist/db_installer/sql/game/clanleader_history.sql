SET FOREIGN_KEY_CHECKS=0;
-- ----------------------------
-- Table structure for `clanleader_history`
-- ----------------------------
DROP TABLE IF EXISTS `clanleader_history`;
CREATE TABLE `clanleader_history` (
  `id` int NOT NULL AUTO_INCREMENT,
  `clanId` int NOT NULL,
  `clan_name` text CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `ex_objId` int NOT NULL,
  `ex_name` text CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `new_objId` int NOT NULL,
  `new_name` text CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `date` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb3 ROW_FORMAT=DYNAMIC;
