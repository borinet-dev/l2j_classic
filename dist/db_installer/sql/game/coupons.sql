DROP TABLE IF EXISTS `coupons`;
CREATE TABLE `coupons` (
  `coupon_id` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL DEFAULT '',
  `reward_item` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `startTime` bigint NOT NULL,
  `endTime` bigint NOT NULL DEFAULT '0',
  PRIMARY KEY (`coupon_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3;

-- ----------------------------
-- Records of coupons
-- ----------------------------
INSERT INTO `coupons` VALUES ('TICTVQULJFQLGYFENMMG', '41255,20', '1708293600000', '1711897200000');
