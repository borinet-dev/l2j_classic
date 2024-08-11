DROP TABLE IF EXISTS `ban_ip`;
CREATE TABLE `ban_ip` (
  `ip_adress` varchar(15) NOT NULL DEFAULT '',
  `access_level` int NOT NULL DEFAULT '0',
  PRIMARY KEY (`ip_adress`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3;

-- ----------------------------
-- Records of ban_ip
-- ----------------------------
INSERT INTO `ban_ip` VALUES ('112.126.216.49', '-100');
INSERT INTO `ban_ip` VALUES ('115.126.216.49', '-100');
INSERT INTO `ban_ip` VALUES ('115.21.2.242', '-100');
INSERT INTO `ban_ip` VALUES ('116.122.84.148', '-100');
INSERT INTO `ban_ip` VALUES ('118.39.108.229', '-100');
INSERT INTO `ban_ip` VALUES ('119.200.94.10', '-100');
INSERT INTO `ban_ip` VALUES ('121.128.237.211', '-100');
INSERT INTO `ban_ip` VALUES ('123.200.106.193', '-100');
INSERT INTO `ban_ip` VALUES ('125.133.60.191', '-100');
INSERT INTO `ban_ip` VALUES ('211.54.194.204', '-100');
INSERT INTO `ban_ip` VALUES ('221.165.148.116', '-100');
INSERT INTO `ban_ip` VALUES ('58.235.39.60', '-100');
INSERT INTO `ban_ip` VALUES ('61.253.195.158', '-100');
