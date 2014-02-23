SET FOREIGN_KEY_CHECKS=0;
# Exporting metadata from parentdb
DROP DATABASE IF EXISTS `parentdb`;
CREATE DATABASE `parentdb`;
USE `parentdb`;
# TABLE: parentdb.child
CREATE TABLE `child` (
  `_id` int(11) NOT NULL,
  `name` varchar(80) NOT NULL,
  `balance` double NOT NULL,
  PRIMARY KEY (`_id`),
  UNIQUE KEY `name_UNIQUE` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
# TABLE: paretndb.transactions
CREATE TABLE `transactions` (
  `_id` int(11) NOT NULL,
  `child_id` int(11) NOT NULL,
  `date` date NOT NULL,
  `description` text,
  `amount` double NOT NULL,
  PRIMARY KEY (`_id`),
  UNIQUE KEY `_id_UNIQUE` (`_id`),
  CONSTRAINT `child_id` FOREIGN KEY (`_id`) REFERENCES `child` (`_id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
#...done.
SET FOREIGN_KEY_CHECKS=1;
