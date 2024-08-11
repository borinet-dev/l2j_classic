DROP TABLE IF EXISTS `grandboss_data`;
CREATE TABLE `grandboss_data` (
  `boss_id` smallint unsigned NOT NULL,
  `loc_x` mediumint NOT NULL,
  `loc_y` mediumint NOT NULL,
  `loc_z` mediumint NOT NULL,
  `heading` mediumint NOT NULL DEFAULT '0',
  `respawn_time` bigint unsigned NOT NULL DEFAULT '0',
  `currentHP` decimal(30,15) NOT NULL,
  `currentMP` decimal(30,15) NOT NULL,
  `status` tinyint unsigned NOT NULL DEFAULT '0',
  PRIMARY KEY (`boss_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3;

-- ----------------------------
-- Records of grandboss_data
-- ----------------------------
INSERT INTO `grandboss_data` VALUES ('25283', '185059', '-9610', '-5488', '11873', '1666152627583', '127237156.000000000000000', '69182.000000000000000', '0');
INSERT INTO `grandboss_data` VALUES ('25286', '-6675', '18505', '-5488', '41740', '1666152627583', '127237156.000000000000000', '69182.000000000000000', '0');
INSERT INTO `grandboss_data` VALUES ('29001', '-21610', '181594', '-5770', '10737', '1666152627583', '1701279.000000000000000', '3348.000000000000000', '0');
INSERT INTO `grandboss_data` VALUES ('29006', '17736', '108910', '-6530', '0', '1666152627583', '2120014.000000000000000', '6434.000000000000000', '0');
INSERT INTO `grandboss_data` VALUES ('29014', '55496', '17032', '-5570', '8191', '1666152627583', '1977076.000000000000000', '6434.000000000000000', '0');
INSERT INTO `grandboss_data` VALUES ('29020', '115374', '16858', '10071', '0', '1666152627583', '211769793.000000000000000', '67803.000000000000000', '0');
INSERT INTO `grandboss_data` VALUES ('29022', '52207', '217230', '-3344', '0', '1666152627583', '195221137.000000000000000', '46107.000000000000000', '0');
INSERT INTO `grandboss_data` VALUES ('29068', '185708', '114298', '-8224', '0', '1666152627583', '1117237331.000000000000000', '16940116.000000000000000', '0');
