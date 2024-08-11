DROP TABLE IF EXISTS `bot_reported_char_data`;
CREATE TABLE `bot_reported_char_data` (
  `reporterName` varchar(35) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `reporterId` int unsigned NOT NULL DEFAULT '0',
  `botName` varchar(35) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `botId` int unsigned NOT NULL DEFAULT '0',
  `reportTime` text CHARACTER SET utf8 COLLATE utf8_general_ci,
  PRIMARY KEY (`reporterId`,`botId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3;
