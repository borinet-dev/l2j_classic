SET FOREIGN_KEY_CHECKS=0;
-- ----------------------------
-- Table structure for `character_minigame_score`
-- ----------------------------
DROP TABLE IF EXISTS `character_minigame_score`;
CREATE TABLE `character_minigame_score` (
  `object_id` int NOT NULL,
  `score` int NOT NULL,
  PRIMARY KEY (`object_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3;
