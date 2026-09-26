CREATE TABLE IF NOT EXISTS `gift_member_location_history` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '历史ID',
  `member_id` bigint NOT NULL COMMENT '会员ID',
  `source_type` varchar(16) NOT NULL COMMENT '定位来源：COORDINATE、IP',
  `longitude` decimal(10,6) DEFAULT NULL COMMENT '经度',
  `latitude` decimal(9,6) DEFAULT NULL COMMENT '纬度',
  `ip` varchar(64) DEFAULT NULL COMMENT '客户端IP',
  `city_id` bigint NOT NULL COMMENT '城市ID',
  `city_name` varchar(64) NOT NULL COMMENT '城市名称',
  `creator` varchar(64) NOT NULL DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) NOT NULL DEFAULT '' COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  PRIMARY KEY (`id`),
  KEY `idx_member_history_id` (`member_id`, `id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='会员位置变更历史';
