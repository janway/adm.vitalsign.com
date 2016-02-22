CREATE TABLE `sys` (
  `id` int(11) NOT NULL,
  `name` varchar(45) NOT NULL,
  `cr` datetime DEFAULT NULL,
  `up` datetime DEFAULT NULL,
  `status` int(11) DEFAULT '1',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;