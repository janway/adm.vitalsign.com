CREATE TABLE `usrprg` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `prg` int(11) NOT NULL,
  `usr` char(32) DEFAULT NULL COMMENT 'usr.uuid',
  `status` int(11) DEFAULT '1',
  `cr` datetime DEFAULT NULL,
  `up` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `IX_prg_usr` (`usr`)
) ENGINE=InnoDB AUTO_INCREMENT=831 DEFAULT CHARSET=utf8;
