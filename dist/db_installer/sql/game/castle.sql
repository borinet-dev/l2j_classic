DROP TABLE IF EXISTS `castle`;
CREATE TABLE `castle` (
  `id` int NOT NULL DEFAULT '0',
  `name` varchar(25) NOT NULL,
  `side` enum('NEUTRAL','LIGHT','DARK') NOT NULL DEFAULT 'NEUTRAL',
  `treasury` bigint NOT NULL DEFAULT '0',
  `siegeDate` bigint unsigned NOT NULL DEFAULT '0',
  `regTimeOver` enum('true','false') NOT NULL DEFAULT 'true',
  `regTimeEnd` bigint unsigned NOT NULL DEFAULT '0',
  `showNpcCrest` enum('true','false') NOT NULL DEFAULT 'false',
  `ticketBuyCount` smallint NOT NULL DEFAULT '0',
  PRIMARY KEY (`name`),
  KEY `id` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3;

-- ----------------------------
-- Records of castle
-- ----------------------------
INSERT INTO `castle` VALUES ('1', '글루디오성', 'LIGHT', '0', '1666522800103', 'false', '1666004400020', 'false', '0');
INSERT INTO `castle` VALUES ('3', '기란성', 'LIGHT', '3338776', '1666522800106', 'false', '1666004400022', 'false', '0');
INSERT INTO `castle` VALUES ('2', '디온성', 'NEUTRAL', '0', '0', 'false', '1646031414712', 'false', '0');
INSERT INTO `castle` VALUES ('5', '아덴성', 'NEUTRAL', '0', '0', 'false', '1646031414707', 'false', '0');
INSERT INTO `castle` VALUES ('4', '오렌성', 'NEUTRAL', '0', '0', 'false', '1646031414707', 'false', '0');
