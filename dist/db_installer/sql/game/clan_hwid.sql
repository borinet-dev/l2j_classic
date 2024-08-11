DROP TABLE IF EXISTS `clan_hwid`;
CREATE TABLE `clan_hwid` (
  `account_name` varchar(45) NOT NULL,
  `clanid` int NOT NULL,
  `HWID` varchar(255) NOT NULL,
  PRIMARY KEY (`account_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3;
