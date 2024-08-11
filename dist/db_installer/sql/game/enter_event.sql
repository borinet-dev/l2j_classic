DROP TABLE IF EXISTS `enter_event`;
CREATE TABLE `enter_event` (
  `account` varchar(32) NOT NULL,
  `1hour` int DEFAULT '0',
  `2hour` int DEFAULT '0',
  `3hour` int DEFAULT '0',
  `5hour` int DEFAULT '0',
  `inTimes` int DEFAULT '0',
  PRIMARY KEY (`account`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3;