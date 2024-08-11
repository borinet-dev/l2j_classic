DROP TABLE IF EXISTS `pledge_applicant`;
CREATE TABLE `pledge_applicant` (
  `charId` int NOT NULL,
  `clanId` int NOT NULL,
  `karma` tinyint(1) NOT NULL,
  `message` varchar(255) NOT NULL,
  PRIMARY KEY (`charId`,`clanId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3;