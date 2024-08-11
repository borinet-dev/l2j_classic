DROP TABLE IF EXISTS `web_connect`;
CREATE TABLE `web_connect` (
  `date` text CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `newdate` text CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `fishing` text CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `offline` text CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `used_mem` text NOT NULL,
  `total_mem` text NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3;