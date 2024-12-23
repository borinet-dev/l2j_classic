SET FOREIGN_KEY_CHECKS=0;
-- ----------------------------
-- Table structure for `items_reward_mail`
-- ----------------------------
DROP TABLE IF EXISTS `items_reward_mail`;
CREATE TABLE `items_reward_mail` (
  `id` int NOT NULL AUTO_INCREMENT,
  `messageId` int DEFAULT NULL,
  `char_name` varchar(35) NOT NULL DEFAULT '',
  `charId` int NOT NULL,
  `itemId` int NOT NULL,
  `itemCount` int NOT NULL,
  `itemId2` int NOT NULL,
  `itemCount2` int NOT NULL,
  `delivered` int DEFAULT '0',
  `received_time` text,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=0 DEFAULT CHARSET=utf8mb3;
