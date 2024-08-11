DROP TABLE IF EXISTS `announcements`;
CREATE TABLE `announcements` (
  `id` int unsigned NOT NULL AUTO_INCREMENT,
  `type` int NOT NULL,
  `initial` bigint NOT NULL DEFAULT '0',
  `delay` bigint NOT NULL DEFAULT '0',
  `repeat` int NOT NULL DEFAULT '0',
  `author` text NOT NULL,
  `content` text NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb3;

-- ----------------------------
-- Records of announcements
-- ----------------------------
INSERT INTO `announcements` VALUES ('1', '0', '0', '0', '0', '운영자', '========================================');
INSERT INTO `announcements` VALUES ('2', '0', '0', '0', '0', '운영자', '바이블에 오신것을 환영합니다.');
INSERT INTO `announcements` VALUES ('4', '0', '0', '0', '0', '운영자', '매주 월요일 [아침 7시] 서버가 자동 재시작 됩니다.');
INSERT INTO `announcements` VALUES ('5', '0', '0', '0', '0', '운영자', '외치기로 욕설/조롱 금지. 적발시 1일간 수감됩니다.');
INSERT INTO `announcements` VALUES ('6', '0', '0', '0', '0', '운영자', '스페셜 레이드는 10분이상 공지 후 진행바랍니다.');
INSERT INTO `announcements` VALUES ('7', '0', '0', '0', '0', '운영자', '[매크로 사냥] 금지입니다. 적발시 2일간 수감됩니다.');
INSERT INTO `announcements` VALUES ('8', '0', '0', '0', '0', '운영자', '========================================');
