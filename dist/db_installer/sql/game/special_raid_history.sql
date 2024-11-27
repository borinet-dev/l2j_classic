DROP TABLE IF EXISTS `special_raid_history`;
CREATE TABLE `special_raid_history` (
  `enter_time` text CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `raid_name` varchar(35) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `leader_name` varchar(35) NOT NULL,
  `members` text CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3;
