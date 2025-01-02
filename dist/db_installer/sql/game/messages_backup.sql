DROP TABLE IF EXISTS `messages_backup`;
CREATE TABLE `messages_backup` (
  `messageId` int(11) NOT NULL DEFAULT 0,
  `senderId` int(11) NOT NULL DEFAULT 0,
  `receiverId` int(11) NOT NULL DEFAULT 0,
  `subject` tinytext DEFAULT NULL,
  `content` text DEFAULT NULL,
  `expiration` bigint(20) unsigned NOT NULL DEFAULT 0,
  `reqAdena` bigint(20) NOT NULL DEFAULT 0,
  `hasAttachments` enum('true','false') NOT NULL DEFAULT 'false',
  `isUnread` enum('true','false') NOT NULL DEFAULT 'true',
  `isDeletedBySender` enum('true','false') NOT NULL DEFAULT 'false',
  `isDeletedByReceiver` enum('true','false') NOT NULL DEFAULT 'false',
  `isLocked` enum('true','false') NOT NULL DEFAULT 'false',
  `sendBySystem` tinyint(1) NOT NULL DEFAULT 0,
  `isReturned` enum('true','false') NOT NULL DEFAULT 'false',
  `itemId` int(11) NOT NULL DEFAULT 0,
  `enchantLvl` int(11) NOT NULL DEFAULT 0,
  `elementals` varchar(25) DEFAULT NULL,
  PRIMARY KEY (`messageId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_general_ci;
