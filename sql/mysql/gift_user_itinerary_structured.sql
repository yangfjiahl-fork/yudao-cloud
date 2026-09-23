-- 行程领域最终结构（不兼容旧的 gift_trip_* 存储）。
-- 用户会话、会话事件和个人行程统一使用 user_itinerary 命名空间。

DROP TABLE IF EXISTS `gift_user_itinerary_transport_segment`;
DROP TABLE IF EXISTS `gift_user_itinerary_item`;
DROP TABLE IF EXISTS `gift_user_itinerary_day`;
DROP TABLE IF EXISTS `gift_user_itinerary`;
DROP TABLE IF EXISTS `gift_user_itinerary_conversation_event`;
DROP TABLE IF EXISTS `gift_itinerary_event`;
DROP TABLE IF EXISTS `gift_user_itinerary_conversation`;
DROP TABLE IF EXISTS `gift_itinerary_conversation`;

CREATE TABLE `gift_user_itinerary_conversation` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '会话ID，同时作为 AG-UI threadId',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `member_id` bigint NOT NULL COMMENT '会员ID',
  `title` varchar(128) NOT NULL DEFAULT '新旅行计划',
  `pinned` bit(1) NOT NULL DEFAULT b'0',
  `province_id` bigint DEFAULT NULL,
  `city_id` bigint DEFAULT NULL,
  `district_id` bigint DEFAULT NULL,
  `intake_agent_session_id` varchar(64) DEFAULT NULL,
  `plan_agent_session_id` varchar(64) DEFAULT NULL,
  `state_json` text NOT NULL,
  `missing_required_json` text NOT NULL,
  `status` tinyint NOT NULL DEFAULT 1,
  `creator` varchar(64) NOT NULL DEFAULT '', `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updater` varchar(64) NOT NULL DEFAULT '', `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` bit(1) NOT NULL DEFAULT b'0',
  PRIMARY KEY (`id`),
  KEY `idx_member_update_time` (`member_id`, `update_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户行程 AG-UI 会话';

CREATE TABLE `gift_user_itinerary_conversation_event` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '事件ID，消息事件同时作为 AG-UI messageId',
  `tenant_id` bigint NOT NULL,
  `conversation_id` bigint NOT NULL,
  `run_id` varchar(64) DEFAULT NULL COMMENT 'AG-UI runId',
  `reply_event_id` bigint DEFAULT NULL,
  `user_itinerary_id` bigint DEFAULT NULL,
  `event_type` varchar(32) NOT NULL COMMENT 'USER_MESSAGE/USER_ACTION/ASSISTANT_MESSAGE/ITINERARY',
  `role` varchar(16) DEFAULT NULL,
  `stage` varchar(32) DEFAULT NULL,
  `content` text DEFAULT NULL,
  `payload_json` longtext DEFAULT NULL,
  `status` tinyint NOT NULL DEFAULT 1,
  `creator` varchar(64) NOT NULL DEFAULT '', `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updater` varchar(64) NOT NULL DEFAULT '', `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` bit(1) NOT NULL DEFAULT b'0',
  PRIMARY KEY (`id`),
  KEY `idx_conversation_event` (`conversation_id`, `id`),
  KEY `idx_run_id` (`run_id`),
  KEY `idx_user_itinerary_id` (`user_itinerary_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户行程会话 AG-UI 持久事件';

CREATE TABLE `gift_user_itinerary` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint NOT NULL,
  `conversation_id` bigint NOT NULL,
  `member_id` bigint NOT NULL,
  `request_event_id` bigint DEFAULT NULL,
  `result_event_id` bigint DEFAULT NULL,
  `status` tinyint NOT NULL DEFAULT 1,
  `title` varchar(128) NOT NULL,
  `cover_url` varchar(1024) NOT NULL DEFAULT '',
  `cover_width` int NOT NULL DEFAULT 0,
  `cover_height` int NOT NULL DEFAULT 0,
  `start_date` date DEFAULT NULL,
  `end_date` date DEFAULT NULL,
  `day_cnt` int NOT NULL DEFAULT 0,
  `departure` varchar(128) DEFAULT NULL,
  `destination` varchar(128) DEFAULT NULL,
  `traveler_count` int DEFAULT NULL,
  `budget` int DEFAULT NULL COMMENT '不含往返交通',
  `hotel_budget` int DEFAULT NULL,
  `traveler_profile_json` text DEFAULT NULL,
  `interests_json` text DEFAULT NULL,
  `pace` varchar(32) DEFAULT NULL,
  `must_visit_json` text DEFAULT NULL,
  `constraints_json` text DEFAULT NULL,
  `daily_start_time` time DEFAULT NULL,
  `daily_end_time` time DEFAULT NULL,
  `overview_status` varchar(16) DEFAULT NULL,
  `overview_skeleton` varchar(500) DEFAULT NULL,
  `overview_detail` varchar(1000) DEFAULT NULL,
  `planner_type` varchar(32) DEFAULT NULL,
  `planner_validation` varchar(32) DEFAULT NULL,
  `creator` varchar(64) NOT NULL DEFAULT '', `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updater` varchar(64) NOT NULL DEFAULT '', `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` bit(1) NOT NULL DEFAULT b'0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_conversation_id` (`conversation_id`),
  UNIQUE KEY `uk_result_event_id` (`result_event_id`),
  KEY `idx_member_start_date` (`member_id`, `start_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户生成行程';

CREATE TABLE `gift_user_itinerary_day` (
  `id` bigint NOT NULL AUTO_INCREMENT, `tenant_id` bigint NOT NULL,
  `user_itinerary_id` bigint NOT NULL, `day` int NOT NULL, `date` date DEFAULT NULL,
  `province_id` int DEFAULT NULL, `city_id` int DEFAULT NULL, `district_id` int DEFAULT NULL,
  `city` varchar(128) DEFAULT NULL, `area` varchar(128) DEFAULT NULL, `theme` varchar(256) DEFAULT NULL,
  `anchor_poi_names_json` text DEFAULT NULL,
  `sort` int NOT NULL DEFAULT 0,
  `overview_status` varchar(16) DEFAULT NULL, `overview_skeleton` varchar(500) DEFAULT NULL,
  `overview_detail` varchar(1000) DEFAULT NULL, `planner` varchar(32) DEFAULT NULL,
  `planning_status` varchar(16) DEFAULT NULL, `macro_source` varchar(32) DEFAULT NULL,
  `selection_status` varchar(16) DEFAULT NULL,
  `route_data_status` varchar(16) DEFAULT NULL, `budget_status` varchar(16) DEFAULT NULL,
  `requested_scenic_count` int DEFAULT NULL, `selected_scenic_count` int DEFAULT NULL,
  `day_start_time` time DEFAULT NULL, `day_end_time` time DEFAULT NULL,
  `dropped_node_ids_json` text DEFAULT NULL, `candidate_counts_json` text DEFAULT NULL,
  `creator` varchar(64) NOT NULL DEFAULT '', `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updater` varchar(64) NOT NULL DEFAULT '', `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` bit(1) NOT NULL DEFAULT b'0',
  PRIMARY KEY (`id`), UNIQUE KEY `uk_user_itinerary_day` (`user_itinerary_id`, `day`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户行程每日安排';

CREATE TABLE `gift_user_itinerary_item` (
  `id` bigint NOT NULL AUTO_INCREMENT, `tenant_id` bigint NOT NULL,
  `user_itinerary_id` bigint NOT NULL, `user_itinerary_day_id` bigint NOT NULL,
  `item_id` varchar(64) NOT NULL, `day` int NOT NULL,
  `type` varchar(16) NOT NULL, `slot` varchar(32) NOT NULL, `label` varchar(32) DEFAULT NULL,
  `sort` int NOT NULL DEFAULT 0, `start_time` time DEFAULT NULL, `end_time` time DEFAULT NULL,
  `duration_minutes` int DEFAULT NULL,
  `poi_id` varchar(64) DEFAULT NULL, `poi_name` varchar(256) DEFAULT NULL,
  `province_id` int DEFAULT NULL, `city_id` int DEFAULT NULL, `district_id` int DEFAULT NULL,
  `city` varchar(128) DEFAULT NULL, `area` varchar(128) DEFAULT NULL,
  `address_detail` varchar(512) DEFAULT NULL, `longitude` decimal(10,7) DEFAULT NULL,
  `latitude` decimal(10,7) DEFAULT NULL, `coordinate_system` varchar(16) DEFAULT NULL,
  `business_hours` varchar(256) DEFAULT NULL, `phone_no` varchar(64) DEFAULT NULL,
  `cover_url` varchar(1024) DEFAULT NULL, `rating` decimal(4,2) DEFAULT NULL,
  `cost` decimal(12,2) DEFAULT NULL, `tags_json` text DEFAULT NULL,
  `skeleton` varchar(500) DEFAULT NULL, `detail` varchar(1000) DEFAULT NULL,
  `status` varchar(16) NOT NULL DEFAULT 'PENDING', `resolve_status` tinyint NOT NULL DEFAULT 0,
  `planning_status` varchar(16) DEFAULT NULL, `poi_verification_status` varchar(16) DEFAULT NULL,
  `must_visit` bit(1) NOT NULL DEFAULT b'0', `locked` bit(1) NOT NULL DEFAULT b'0',
  `source` varchar(32) DEFAULT NULL, `provider` varchar(32) DEFAULT NULL,
  `previous_item_id` varchar(64) DEFAULT NULL,
  `travel_mode_from_previous` varchar(16) DEFAULT NULL,
  `travel_distance_meters_from_previous` bigint DEFAULT NULL,
  `travel_duration_minutes_from_previous` int DEFAULT NULL,
  `travel_provider_from_previous` varchar(32) DEFAULT NULL,
  `travel_status_from_previous` varchar(16) DEFAULT NULL,
  `travel_route_points_json` longtext DEFAULT NULL,
  `poi_snapshot_json` text DEFAULT NULL, `candidates_json` text DEFAULT NULL,
  `citation_ids_json` text DEFAULT NULL,
  `creator` varchar(64) NOT NULL DEFAULT '', `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updater` varchar(64) NOT NULL DEFAULT '', `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` bit(1) NOT NULL DEFAULT b'0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_itinerary_item` (`user_itinerary_id`, `item_id`),
  KEY `idx_day_sort` (`user_itinerary_day_id`, `sort`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户行程节点（含入站交通）';

-- 通用行程按天拆分；当前通用行程表无数据，可直接重建节点表。
DROP TABLE IF EXISTS `gift_itinerary_item`;
DROP TABLE IF EXISTS `gift_itinerary_day`;

CREATE TABLE `gift_itinerary_day` (
  `id` bigint NOT NULL AUTO_INCREMENT, `tenant_id` bigint NOT NULL DEFAULT 1,
  `itinerary_id` bigint NOT NULL, `day` int NOT NULL, `city_id` int DEFAULT NULL,
  `district_id` int DEFAULT NULL, `title` varchar(128) DEFAULT NULL,
  `description` varchar(1000) DEFAULT NULL, `sort` int NOT NULL DEFAULT 0,
  `creator` varchar(64) NOT NULL DEFAULT '', `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updater` varchar(64) NOT NULL DEFAULT '', `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` bit(1) NOT NULL DEFAULT b'0',
  PRIMARY KEY (`id`), UNIQUE KEY `uk_itinerary_day` (`itinerary_id`, `day`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='通用行程每日安排';

CREATE TABLE `gift_itinerary_item` (
  `id` bigint NOT NULL AUTO_INCREMENT, `tenant_id` bigint NOT NULL DEFAULT 1,
  `itinerary_id` bigint NOT NULL, `itinerary_day_id` bigint NOT NULL,
  `type` varchar(16) NOT NULL DEFAULT 'ACTIVITY', `slot` varchar(32) DEFAULT NULL,
  `title` varchar(128) NOT NULL, `sub_title` varchar(128) DEFAULT NULL,
  `description` varchar(1024) NOT NULL DEFAULT '', `sort` int NOT NULL DEFAULT 0,
  `start_time` time DEFAULT NULL, `duration_minutes` int DEFAULT NULL,
  `poi_id` varchar(64) DEFAULT NULL, `province_id` int DEFAULT NULL, `city_id` int DEFAULT NULL,
  `district_id` int DEFAULT NULL, `longitude` decimal(10,7) DEFAULT NULL,
  `latitude` decimal(10,7) DEFAULT NULL, `cover_url` varchar(1024) DEFAULT NULL,
  `cover_width` int DEFAULT NULL, `cover_height` int DEFAULT NULL,
  `pic_urls` text DEFAULT NULL, `pic_sizes` text DEFAULT NULL, `tags` text DEFAULT NULL,
  `gd_position` varchar(128) DEFAULT NULL,
  `business_time` varchar(128) DEFAULT NULL, `address_detail` varchar(512) DEFAULT NULL,
  `phone_no` varchar(64) DEFAULT NULL, `previous_item_id` bigint DEFAULT NULL,
  `travel_mode_from_previous` varchar(16) DEFAULT NULL,
  `travel_distance_meters_from_previous` bigint DEFAULT NULL,
  `travel_duration_minutes_from_previous` int DEFAULT NULL,
  `creator` varchar(64) NOT NULL DEFAULT '', `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updater` varchar(64) NOT NULL DEFAULT '', `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` bit(1) NOT NULL DEFAULT b'0',
  PRIMARY KEY (`id`), KEY `idx_itinerary_day_sort` (`itinerary_day_id`, `sort`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='通用行程节点（含入站交通）';

-- 高德旅行 POI 类型（value 与 AmapPoiTypeEnum.category 保持一致）
INSERT INTO `system_dict_type` (`name`, `type`, `status`, `remark`, `creator`, `updater`, `deleted`)
SELECT '高德 POI 类型', 'gift_amap_poi_type', 0, '旅行规划和探索页使用的高德 POI 大类', 'admin', 'admin', b'0'
WHERE NOT EXISTS (SELECT 1 FROM `system_dict_type` WHERE `type` = 'gift_amap_poi_type' AND `deleted` = b'0');

INSERT INTO `system_dict_data` (`sort`, `label`, `value`, `dict_type`, `status`, `color_type`, `css_class`, `remark`, `creator`, `updater`, `deleted`)
SELECT 1, '景点', 'sightseeing', 'gift_amap_poi_type', 0, 'primary', '', '高德类型 110000', 'admin', 'admin', b'0'
WHERE NOT EXISTS (SELECT 1 FROM `system_dict_data` WHERE `dict_type` = 'gift_amap_poi_type' AND `value` = 'sightseeing' AND `deleted` = b'0');
INSERT INTO `system_dict_data` (`sort`, `label`, `value`, `dict_type`, `status`, `color_type`, `css_class`, `remark`, `creator`, `updater`, `deleted`)
SELECT 2, '酒店', 'hotel', 'gift_amap_poi_type', 0, 'success', '', '高德类型 100000', 'admin', 'admin', b'0'
WHERE NOT EXISTS (SELECT 1 FROM `system_dict_data` WHERE `dict_type` = 'gift_amap_poi_type' AND `value` = 'hotel' AND `deleted` = b'0');
INSERT INTO `system_dict_data` (`sort`, `label`, `value`, `dict_type`, `status`, `color_type`, `css_class`, `remark`, `creator`, `updater`, `deleted`)
SELECT 3, '美食', 'food', 'gift_amap_poi_type', 0, 'warning', '', '高德类型 050000', 'admin', 'admin', b'0'
WHERE NOT EXISTS (SELECT 1 FROM `system_dict_data` WHERE `dict_type` = 'gift_amap_poi_type' AND `value` = 'food' AND `deleted` = b'0');
INSERT INTO `system_dict_data` (`sort`, `label`, `value`, `dict_type`, `status`, `color_type`, `css_class`, `remark`, `creator`, `updater`, `deleted`)
SELECT 4, '购物', 'shopping', 'gift_amap_poi_type', 0, 'info', '', '高德类型 060000', 'admin', 'admin', b'0'
WHERE NOT EXISTS (SELECT 1 FROM `system_dict_data` WHERE `dict_type` = 'gift_amap_poi_type' AND `value` = 'shopping' AND `deleted` = b'0');
