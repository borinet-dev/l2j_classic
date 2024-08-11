DROP TABLE IF EXISTS `monster_race_bets`;
CREATE TABLE `monster_race_bets` (
  `lane_id` int NOT NULL DEFAULT '0',
  `last_betting` int DEFAULT '0',
  PRIMARY KEY (`lane_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3;

-- ----------------------------
-- Records of monster_race_bets
-- ----------------------------
INSERT INTO `monster_race_bets` VALUES ('1', '0');
INSERT INTO `monster_race_bets` VALUES ('2', '0');
INSERT INTO `monster_race_bets` VALUES ('3', '0');
INSERT INTO `monster_race_bets` VALUES ('4', '0');
INSERT INTO `monster_race_bets` VALUES ('5', '0');
INSERT INTO `monster_race_bets` VALUES ('6', '0');
INSERT INTO `monster_race_bets` VALUES ('7', '0');
INSERT INTO `monster_race_bets` VALUES ('8', '0');
