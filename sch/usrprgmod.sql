CREATE TABLE `usrprgmod` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `mod` int(11) NOT NULL,
  `usr` char(32) NOT NULL,
  `status` int(11) DEFAULT '1',
  `cr` datetime DEFAULT NULL,
  `up` datetime DEFAULT NULL,
  `access` int(11) DEFAULT '0' COMMENT '0:forbidden,1:readonly,2:read/write',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=115 DEFAULT CHARSET=utf8;
