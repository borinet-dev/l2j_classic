DROP TABLE IF EXISTS `daily_luna`;
CREATE TABLE `daily_luna` (
  `account` varchar(45) NOT NULL,
  `ip` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL DEFAULT '',
  `hwid` varchar(255) NOT NULL DEFAULT '0',
  `e_mail` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3;
