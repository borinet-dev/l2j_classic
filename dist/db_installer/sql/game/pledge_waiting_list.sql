DROP TABLE IF EXISTS `pledge_waiting_list`;
CREATE TABLE `pledge_waiting_list` (
  `char_id` int NOT NULL,
  `karma` tinyint(1) NOT NULL,
  PRIMARY KEY (`char_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3;