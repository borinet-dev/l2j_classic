DROP TABLE IF EXISTS `patch`;
CREATE TABLE `patch` (
  `no` bigint NOT NULL AUTO_INCREMENT,
  `title` varchar(250) DEFAULT NULL,
  `body` longtext NOT NULL,
  PRIMARY KEY (`no`)
) ENGINE=InnoDB AUTO_INCREMENT=41 DEFAULT CHARSET=utf8mb3;