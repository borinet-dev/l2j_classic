SET FOREIGN_KEY_CHECKS=0;
-- ----------------------------
-- Table structure for `character_heavenly_rift`
-- ----------------------------
DROP TABLE IF EXISTS `character_heavenly_rift`;
CREATE TABLE `character_heavenly_rift` (
  `charId` int NOT NULL DEFAULT '0',
  `val` int NOT NULL,
  PRIMARY KEY (`charId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3;
