-- momentdb_dev.T_COMMENT definition

CREATE TABLE `T_COMMENT` (
                             `ID` bigint(18) NOT NULL AUTO_INCREMENT COMMENT '主键',
                             `COMMENT_ID` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
                             `FEED_ID` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
                             `USER_ID` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
                             `COMMENT_CONTENT` varchar(1000) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
                             `REPLY_TO` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
                             `CREATE_DT` datetime(3) NOT NULL,
                             PRIMARY KEY (`ID`),
                             UNIQUE KEY `COMMENT_ID` (`COMMENT_ID`),
                             KEY `IDX_COMMENT_FID_UID` (`FEED_ID`,`USER_ID`)
) ENGINE=InnoDB AUTO_INCREMENT=3345 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- momentdb_dev.T_FEED definition

CREATE TABLE `T_FEED` (
                          `ID` int(11) NOT NULL AUTO_INCREMENT COMMENT '自增Id',
                          `FEED_ID` varchar(64) NOT NULL COMMENT '动态 Id',
                          `USER_ID` varchar(64) NOT NULL COMMENT '发布者 Id',
                          `FEED_TYPE` int(2) NOT NULL COMMENT '动态类型',
                          `FEED_CONTENT` text NOT NULL COMMENT '动态内容',
                          `FEED_POI` varchar(255) DEFAULT NULL COMMENT '动态的地理位置信息',
                          `FEED_STATUS` int(2) NOT NULL DEFAULT '0' COMMENT '0 正常，1 被删除',
                          `CREATE_DT` datetime(3) NOT NULL,
                          `UPDATE_DT` datetime(3) NOT NULL,
                          PRIMARY KEY (`ID`),
                          UNIQUE KEY `UNION_KEY_FEED_ID` (`FEED_ID`) USING BTREE,
                          KEY `KEY_USER_ID` (`USER_ID`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=1474 DEFAULT CHARSET=utf8mb4;


-- momentdb_dev.T_LIKE definition

CREATE TABLE `T_LIKE` (
                          `ID` bigint(18) NOT NULL AUTO_INCREMENT COMMENT '主键',
                          `LIKE_ID` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
                          `FEED_ID` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
                          `USER_ID` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
                          `CREATE_DT` datetime(3) NOT NULL,
                          `LIKE_STATUS` int(2) NOT NULL,
                          PRIMARY KEY (`ID`),
                          UNIQUE KEY `UNIDX_LIKE_FID_UID` (`FEED_ID`,`USER_ID`),
                          UNIQUE KEY `UNIDX_LIKE_ID` (`LIKE_ID`)
) ENGINE=InnoDB AUTO_INCREMENT=1052 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- momentdb_dev.T_MESSAGE definition

CREATE TABLE `T_MESSAGE` (
                             `ID` int(11) NOT NULL AUTO_INCREMENT,
                             `MESSAGE_ID` varchar(64) NOT NULL,
                             `FEED_ID` varchar(64) NOT NULL,
                             `USER_ID` varchar(64) NOT NULL,
                             `MESSAGE_TYPE` int(2) NOT NULL,
                             `STATUS` int(2) NOT NULL,
                             `CREATE_DT` datetime(3) NOT NULL,
                             `PUBLISH_USER_ID` varchar(64) NOT NULL,
                             PRIMARY KEY (`ID`),
                             KEY `KEY_USER_ID` (`USER_ID`) USING BTREE,
                             KEY `KEY_MESSAGE_ID` (`MESSAGE_ID`)
) ENGINE=InnoDB AUTO_INCREMENT=8676 DEFAULT CHARSET=utf8mb4;


-- momentdb_dev.T_TIMELINE definition

CREATE TABLE `T_TIMELINE` (
                              `ID` int(11) NOT NULL AUTO_INCREMENT,
                              `FEED_ID` varchar(32) NOT NULL,
                              `ORG_ID` varchar(32) NOT NULL,
                              `CREATE_DT` datetime(3) NOT NULL,
                              `FEED_STATUS` int(2) NOT NULL,
                              PRIMARY KEY (`ID`),
                              KEY `KEY_ORG_ID` (`ORG_ID`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=1454 DEFAULT CHARSET=utf8mb4;


-- momentdb_dev.T_USER_SETTING definition

CREATE TABLE `T_USER_SETTING` (
                                  `USER_ID` varchar(100) NOT NULL,
                                  `COVER` varchar(1024) DEFAULT NULL,
                                  `UPDATE_DT` datetime(3) NOT NULL,
                                  PRIMARY KEY (`USER_ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;