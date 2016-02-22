CREATE TABLE `prgmod` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `prg` int(11) NOT NULL,
  `name` varchar(45) DEFAULT NULL,
  `desc` varchar(45) NOT NULL,
  `cr` datetime DEFAULT NULL,
  `up` datetime DEFAULT NULL,
  `status` int(11) DEFAULT '1',
  `ord` int(11) DEFAULT '0',
  `crUsr` char(32) DEFAULT NULL,
  `upUsr` char(32) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3002 DEFAULT CHARSET=utf8;
