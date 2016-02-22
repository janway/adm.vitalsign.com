CREATE TABLE `prg` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(45) DEFAULT NULL,
  `desc` varchar(45) DEFAULT NULL,
  `sys` int(11) DEFAULT '0' COMMENT '0:misc,1 : GO, 2:wow88',
  `up` datetime DEFAULT NULL,
  `cr` datetime DEFAULT NULL,
  `crUsr` char(32) DEFAULT NULL,
  `upUsr` char(32) DEFAULT NULL,
  `status` int(11) DEFAULT '1',
  `ord` int(11) DEFAULT '0',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=416 DEFAULT CHARSET=utf8;
