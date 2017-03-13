CREATE TABLE `filter_test` (
  `filter_id` varchar(45) DEFAULT NULL,
  `revision` int(11) DEFAULT NULL,
  `create_time` datetime DEFAULT NULL,
  `is_active` tinyint(4) DEFAULT NULL,
  `is_canary` tinyint(4) DEFAULT NULL,
  `filter_code` longtext,
  `filter_type` varchar(45) DEFAULT NULL,
  `filter_name` varchar(45) DEFAULT NULL,
  `disable_property_name` varchar(45) DEFAULT NULL,
  `filter_order` varchar(45) DEFAULT NULL,
  `application_name` varchar(45) DEFAULT NULL,
  UNIQUE KEY `new_tablecol_UNIQUE` (`filter_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
