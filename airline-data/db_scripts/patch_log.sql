ALTER TABLE `log` DROP INDEX `PRIMARY`;
ALTER TABLE `log` ADD `id` INT PRIMARY KEY NOT NULL AUTO_INCREMENT FIRST;
