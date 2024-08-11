DROP TABLE IF EXISTS `boat_schedule`;
CREATE TABLE `boat_schedule` (
  `name` varchar(255) NOT NULL,
  `status` varchar(255) NOT NULL,
  `destination` varchar(255) NOT NULL,
  `arrivalTime` bigint DEFAULT '0',
  PRIMARY KEY (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3;

INSERT INTO `boat_schedule` VALUES ('바이칼 호', '정박 중', '기란 항구', '0');
INSERT INTO `boat_schedule` VALUES ('보리넷 호', '정박 중', '말하는 섬 항구', '0');
