CREATE TABLE `grp` (
  `uid` char(32) NOT NULL,
  `code` varchar(5) NOT NULL,
  `name` varchar(50) NOT NULL COMMENT 'group name',
  `status` int(11) NOT NULL DEFAULT '1' COMMENT '0 disable 1 enable',
  `ord` int(11) NOT NULL DEFAULT '0',
  `cr` timestamp NULL DEFAULT NULL,
  `up` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `crUsr` char(32) DEFAULT NULL,
  `upUsr` char(32) DEFAULT NULL,
  `crIP` varchar(21) DEFAULT NULL,
  `upIP` varchar(21) DEFAULT NULL,
  PRIMARY KEY (`uid`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COMMENT='group';