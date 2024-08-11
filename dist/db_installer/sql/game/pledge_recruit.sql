DROP TABLE IF EXISTS `pledge_recruit`;
CREATE TABLE `pledge_recruit` (
  `clan_id` int NOT NULL,
  `karma` tinyint(1) NOT NULL,
  `information` varchar(50) NOT NULL,
  `detailed_information` varchar(255) NOT NULL,
  `application_type` tinyint(1) NOT NULL,
  `recruit_type` tinyint(1) NOT NULL,
  PRIMARY KEY (`clan_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3;