CREATE DATABASE  IF NOT EXISTS `parentdb` /*!40100 DEFAULT CHARACTER SET utf8 */;
USE `parentdb`;

DROP TABLE IF EXISTS `groups`;
CREATE TABLE `groups` (
  `groupId` int(11) NOT NULL AUTO_INCREMENT,
  `masterId` int(11) NOT NULL,
  PRIMARY KEY (`groupId`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8;

-- Dump completed on 2014-02-27 21:38:35
