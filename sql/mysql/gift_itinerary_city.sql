-- 行程目的地城市，用于目的地搜索和推荐展示。

CREATE TABLE IF NOT EXISTS `gift_itinerary_city` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '目的地城市ID',
  `tenant_id` bigint NOT NULL DEFAULT 1 COMMENT '租户ID',
  `province_id` int NOT NULL COMMENT '省级区域ID',
  `city_id` int NOT NULL COMMENT '城市区域ID',
  `city_name` varchar(64) NOT NULL COMMENT '城市名称',
  `city_url` varchar(1024) NOT NULL DEFAULT '' COMMENT '城市图片地址',
  `title` varchar(128) NOT NULL DEFAULT '' COMMENT '目的地展示标题',
  `itinerary_count` int NOT NULL DEFAULT 0 COMMENT '行程数量',
  `hot` int NOT NULL DEFAULT 0 COMMENT '热门指数',
  `sort` int NOT NULL DEFAULT 0 COMMENT '排序值',
  `creator` varchar(64) NOT NULL DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) NOT NULL DEFAULT '' COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_city_id` (`city_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='行程目的地城市';
