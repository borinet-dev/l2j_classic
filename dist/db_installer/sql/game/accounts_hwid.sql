DROP TABLE IF EXISTS `accounts_hwid`;
CREATE TABLE `accounts_hwid` (
  `account` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `HWID` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  PRIMARY KEY (`HWID`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8mb3;
