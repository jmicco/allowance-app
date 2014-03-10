DROP SCHEMA IF EXISTS parentdb;
CREATE SCHEMA IF NOT EXISTS parentdb;
DROP TABLE IF EXISTS `parentdb`.`groups`;
CREATE TABLE `parentdb`.`groups` (
  `groupId` int NOT NULL AUTO_INCREMENT,
  `masterId` varchar(36) NOT NULL,
  PRIMARY KEY (`groupId`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `parentdb`.`transaction_journal`;
CREATE TABLE `parentdb`.`transaction_journal` (
  `journalId` INT NOT NULL,
  `deviceId` VARCHAR(36) NOT NULL,
  `transactionType` INT NOT NULL,
  `timestamp` DATETIME NOT NULL,
  `transactionId` INT NOT NULL,
  `childId` INT NOT NULL,
  `description` VARCHAR(255) NULL,
  `date` DATE NOT NULL,
  `amount` DECIMAL(2) NOT NULL,
  PRIMARY KEY (`journalId`, `deviceId`)) ENGINE=InnoDB  DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `parentdb`.`child_journal`;
CREATE TABLE `parentdb`.`child_journal` (
  `journalId` INT NOT NULL,
  `deviceId` VARCHAR(36) NOT NULL,
  `transactionType` INT NOT NULL,
  `timestamp` DATETIME NOT NULL,
  `childId` INT NOT NULL,
  `name` VARCHAR(255) NOT NULL,
PRIMARY KEY (`journalId`, `deviceID`)) ENGINE=InnoDB  DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `parentdb`.`device_history`;
CREATE TABLE `parentdb`.`device_history` (
  `deviceId` varchar(36) NOT NULL,
  `groupId` INT NOT NULL,
  `hwmChildPush` INT NOT NULL,
  `hwmChildMasterPush` INT NOT NULL,
  `hwmTransPush` INT NOT NULL,
  `hwmTransMasterPush` INT NOT NULL,
  `hwmChildPull` INT NOT NULL,
  `hwmTransPull` INT NOT NULL,
  `email` VARCHAR(255) NOT NULL,
  PRIMARY KEY (`deviceId`)) ENGINE=InnoDB  DEFAULT CHARSET=utf8;

CREATE TABLE `parentdb`.`child` (
  `childId` INT NOT NULL AUTO_INCREMENT,
  `deviceId` VARCHAR(36) NOT NULL,
  `name` VARCHAR(255) NULL,
  PRIMARY KEY (`childId`, `deviceId`));

CREATE TABLE `parentdb`.`transactions` (
  `transactionId` INT NOT NULL AUTO_INCREMENT,
  `deviceId` VARCHAR(36) NOT NULL,
  `childId` INT NOT NULL,
  `date` DATE NOT NULL,
  `description` VARCHAR(255) NULL,
  `amount` DECIMAL(2,0) NULL,
  PRIMARY KEY (`transactionId`, `deviceId`));

CREATE TABLE `parentdb`.`sequence` (
  `sequence` INT NOT NULL AUTO_INCREMENT,
  `notUsed` INT,
  PRIMARY KEY (`sequence`));  

ALTER TABLE `parentdb`.`device_history` 
ADD INDEX `groupId_idx` (`groupId` ASC);
ALTER TABLE `parentdb`.`device_history` 
ADD INDEX `email_idx` (`email` ASC);
ALTER TABLE `parentdb`.`device_history` 
ADD CONSTRAINT `groupId`
  FOREIGN KEY (`groupId`)
  REFERENCES `parentdb`.`groups` (`groupId`)
  ON DELETE NO ACTION
  ON UPDATE NO ACTION;
