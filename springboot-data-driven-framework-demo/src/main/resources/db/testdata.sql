/*
SQLyog Ultimate v11.33 (64 bit)
MySQL - 5.7.19-log : Database - autotest
*********************************************************************
*/

/*!40101 SET NAMES utf8 */;

/*!40101 SET SQL_MODE=''*/;

/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;
CREATE DATABASE /*!32312 IF NOT EXISTS*/`autotest` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_vietnamese_ci */;

USE `autotest`;

/*Table structure for table `api_testdata_demo` */

DROP TABLE IF EXISTS `api_testdata_demo`;

CREATE TABLE `api_testdata_demo` (
  `id` bigint(11) NOT NULL AUTO_INCREMENT COMMENT '测试ID',
  `Protocol` enum('Http','RPC','jdbc') DEFAULT NULL COMMENT '协议',
  `Category` enum('Webapi','db') DEFAULT NULL COMMENT '接口类别',
  `Method` varchar(128) DEFAULT NULL COMMENT '接口名称',
  `Parameters` varchar(1000) DEFAULT NULL COMMENT '参数',
  `expected` varchar(128) DEFAULT NULL COMMENT '检查点',
  `description` varchar(1000) DEFAULT NULL COMMENT '描述',
  `isRun` enum('1','0') DEFAULT NULL COMMENT '运行状态，1:运行，0:未运行',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8;

/*Data for the table `api_testdata_demo` */

insert  into `api_testdata_demo`(`id`,`Protocol`,`Category`,`Method`,`Parameters`,`expected`,`description`,`isRun`) values (1,'jdbc','db','demo','{\"Parameters\":\"latte\"}','CNY 25.00','测试demo','1');

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;
