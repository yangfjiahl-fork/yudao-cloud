-- 行程领域最终结构（不兼容旧的 gift_trip_* 存储）。
-- 用户会话、会话事件和个人行程统一使用 user_itinerary 命名空间。

DROP TABLE IF EXISTS `gift_user_itinerary_transport_segment`;
DROP TABLE IF EXISTS `gift_user_itinerary_day_item`;
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
  `title` varchar(128) NOT NULL DEFAULT '新旅行计划' COMMENT '会话标题',
  `pinned` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否置顶',
  `province_id` bigint DEFAULT NULL COMMENT '目的地省级区域ID',
  `city_id` bigint DEFAULT NULL COMMENT '目的地城市ID',
  `district_id` bigint DEFAULT NULL COMMENT '目的地区县ID',
  `intake_agent_session_id` varchar(64) DEFAULT NULL COMMENT '信息收集 Agent 会话ID',
  `plan_agent_session_id` varchar(64) DEFAULT NULL COMMENT '行程生成 Agent 会话ID',
  `state_json` text NOT NULL COMMENT '已采集的行程需求状态JSON',
  `missing_required_json` text NOT NULL COMMENT '尚未补齐的必填字段JSON',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '会话状态',
  `creator` varchar(64) NOT NULL DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) NOT NULL DEFAULT '' COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  PRIMARY KEY (`id`),
  KEY `idx_member_update_time` (`member_id`, `update_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户行程 AG-UI 会话';

CREATE TABLE `gift_user_itinerary_conversation_event` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '事件ID，消息事件同时作为 AG-UI messageId',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `conversation_id` bigint NOT NULL COMMENT '用户行程会话ID',
  `run_id` varchar(64) DEFAULT NULL COMMENT 'AG-UI runId',
  `reply_event_id` bigint DEFAULT NULL COMMENT '回复的事件ID',
  `user_itinerary_id` bigint DEFAULT NULL COMMENT '关联的用户行程ID',
  `event_type` varchar(32) NOT NULL COMMENT '事件类型：USER_MESSAGE/USER_ACTION/ASSISTANT_MESSAGE/ITINERARY',
  `role` varchar(16) DEFAULT NULL COMMENT '消息角色',
  `stage` varchar(32) DEFAULT NULL COMMENT '行程处理阶段',
  `content` text DEFAULT NULL COMMENT '事件文本内容',
  `payload_json` longtext DEFAULT NULL COMMENT '事件扩展数据JSON',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '事件状态',
  `creator` varchar(64) NOT NULL DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) NOT NULL DEFAULT '' COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  PRIMARY KEY (`id`),
  KEY `idx_conversation_event` (`conversation_id`, `id`),
  KEY `idx_run_id` (`run_id`),
  KEY `idx_user_itinerary_id` (`user_itinerary_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户行程会话 AG-UI 持久事件';

CREATE TABLE `gift_user_itinerary` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '用户行程ID',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `conversation_id` bigint NOT NULL COMMENT '用户行程会话ID',
  `member_id` bigint NOT NULL COMMENT '会员ID',
  `request_event_id` bigint DEFAULT NULL COMMENT '触发行程生成的请求事件ID',
  `result_event_id` bigint DEFAULT NULL COMMENT '行程生成结果事件ID',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '行程状态',
  `title` varchar(128) NOT NULL COMMENT '行程标题',
  `cover_url` varchar(1024) NOT NULL DEFAULT '' COMMENT '封面图地址',
  `cover_width` int NOT NULL DEFAULT 0 COMMENT '封面图宽度',
  `cover_height` int NOT NULL DEFAULT 0 COMMENT '封面图高度',
  `start_date` date DEFAULT NULL COMMENT '出发日期',
  `end_date` date DEFAULT NULL COMMENT '结束日期',
  `day_cnt` int NOT NULL DEFAULT 0 COMMENT '行程天数',
  `departure` varchar(128) DEFAULT NULL COMMENT '出发地',
  `destination` varchar(128) DEFAULT NULL COMMENT '目的地',
  `traveler_count` int DEFAULT NULL COMMENT '出行人数',
  `budget` int DEFAULT NULL COMMENT '行程总预算，不含往返交通',
  `hotel_budget` int DEFAULT NULL COMMENT '住宿预算',
  `traveler_profile_json` text DEFAULT NULL COMMENT '出行人画像JSON',
  `interests_json` text DEFAULT NULL COMMENT '兴趣偏好JSON',
  `pace` varchar(32) DEFAULT NULL COMMENT '行程节奏',
  `must_visit_json` text DEFAULT NULL COMMENT '必去地点JSON',
  `constraints_json` text DEFAULT NULL COMMENT '行程约束JSON',
  `daily_start_time` time DEFAULT NULL COMMENT '每日开始时间',
  `daily_end_time` time DEFAULT NULL COMMENT '每日结束时间',
  `overview_status` varchar(16) DEFAULT NULL COMMENT '行程总览生成状态',
  `overview_skeleton` varchar(500) DEFAULT NULL COMMENT '行程总览骨架文案',
  `overview_detail` varchar(1000) DEFAULT NULL COMMENT '行程总览详细文案',
  `planner_type` varchar(32) DEFAULT NULL COMMENT '规划器类型',
  `planner_validation` varchar(32) DEFAULT NULL COMMENT '规划结果校验状态',
  `creator` varchar(64) NOT NULL DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) NOT NULL DEFAULT '' COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_conversation_id` (`conversation_id`),
  UNIQUE KEY `uk_result_event_id` (`result_event_id`),
  KEY `idx_member_start_date` (`member_id`, `start_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户生成行程';

CREATE TABLE `gift_user_itinerary_day` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '用户行程日程ID',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `user_itinerary_id` bigint NOT NULL COMMENT '用户行程ID',
  `day` int NOT NULL COMMENT '行程第几天，从1开始',
  `date` date DEFAULT NULL COMMENT '行程日期',
  `province_id` int DEFAULT NULL COMMENT '当日省级区域ID',
  `city_id` int DEFAULT NULL COMMENT '当日城市ID',
  `district_id` int DEFAULT NULL COMMENT '当日区县ID',
  `city` varchar(128) DEFAULT NULL COMMENT '当日城市名称',
  `area` varchar(128) DEFAULT NULL COMMENT '当日游玩区域',
  `theme` varchar(256) DEFAULT NULL COMMENT '当日行程主题',
  `anchor_poi_names_json` text DEFAULT NULL COMMENT '当日锚点POI名称JSON',
  `sort` int NOT NULL DEFAULT 0 COMMENT '排序值',
  `overview_status` varchar(16) DEFAULT NULL COMMENT '每日总览生成状态',
  `overview_skeleton` varchar(500) DEFAULT NULL COMMENT '每日总览骨架文案',
  `overview_detail` varchar(1000) DEFAULT NULL COMMENT '每日总览详细文案',
  `planner` varchar(32) DEFAULT NULL COMMENT '当日规划器',
  `planning_status` varchar(16) DEFAULT NULL COMMENT '当日排程状态',
  `macro_source` varchar(32) DEFAULT NULL COMMENT '宏观路线来源',
  `selection_status` varchar(16) DEFAULT NULL COMMENT '景点选择状态',
  `budget_status` varchar(16) DEFAULT NULL COMMENT '预算校验状态',
  `requested_scenic_count` int DEFAULT NULL COMMENT '期望景点数量',
  `selected_scenic_count` int DEFAULT NULL COMMENT '实际选择景点数量',
  `day_start_time` time DEFAULT NULL COMMENT '当日开始时间',
  `day_end_time` time DEFAULT NULL COMMENT '当日结束时间',
  `dropped_node_ids_json` text DEFAULT NULL COMMENT '排程丢弃的节点ID JSON',
  `candidate_counts_json` text DEFAULT NULL COMMENT '各类候选数量JSON',
  `creator` varchar(64) NOT NULL DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) NOT NULL DEFAULT '' COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  PRIMARY KEY (`id`), UNIQUE KEY `uk_user_itinerary_day` (`user_itinerary_id`, `day`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户行程每日安排';

CREATE TABLE `gift_user_itinerary_day_item` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '用户行程节点记录ID',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `user_itinerary_id` bigint NOT NULL COMMENT '用户行程ID',
  `user_itinerary_day_id` bigint NOT NULL COMMENT '用户行程日程ID',
  `item_id` varchar(64) NOT NULL COMMENT '行程节点业务ID',
  `day` int NOT NULL COMMENT '所属行程天数',
  `type` varchar(16) NOT NULL COMMENT '节点类型',
  `slot` varchar(32) NOT NULL COMMENT '节点时段',
  `label` varchar(32) DEFAULT NULL COMMENT '节点展示标签',
  `sort` int NOT NULL DEFAULT 0 COMMENT '当日节点排序值',
  `start_time` time DEFAULT NULL COMMENT '计划开始时间',
  `end_time` time DEFAULT NULL COMMENT '计划结束时间',
  `duration_minutes` int DEFAULT NULL COMMENT '建议停留分钟数',
  `poi_id` varchar(64) DEFAULT NULL COMMENT 'POI供应商地点ID',
  `poi_name` varchar(256) DEFAULT NULL COMMENT 'POI名称',
  `province_id` int DEFAULT NULL COMMENT 'POI省级区域ID',
  `city_id` int DEFAULT NULL COMMENT 'POI城市ID',
  `district_id` int DEFAULT NULL COMMENT 'POI区县ID',
  `city` varchar(128) DEFAULT NULL COMMENT 'POI城市名称',
  `area` varchar(128) DEFAULT NULL COMMENT 'POI所在区域',
  `address_detail` varchar(512) DEFAULT NULL COMMENT 'POI详细地址',
  `longitude` decimal(10,7) DEFAULT NULL COMMENT 'POI经度',
  `latitude` decimal(10,7) DEFAULT NULL COMMENT 'POI纬度',
  `coordinate_system` varchar(16) DEFAULT NULL COMMENT '坐标系',
  `business_hours` varchar(256) DEFAULT NULL COMMENT '营业时间',
  `phone_no` varchar(64) DEFAULT NULL COMMENT '联系电话',
  `cover_url` varchar(1024) DEFAULT NULL COMMENT '封面图地址',
  `rating` decimal(4,2) DEFAULT NULL COMMENT '评分',
  `cost` decimal(12,2) DEFAULT NULL COMMENT '预计花费',
  `tags_json` text DEFAULT NULL COMMENT '标签JSON',
  `skeleton` varchar(500) DEFAULT NULL COMMENT '节点骨架文案',
  `detail` varchar(1000) DEFAULT NULL COMMENT '节点详细文案',
  `status` varchar(16) NOT NULL DEFAULT 'PENDING' COMMENT '节点内容状态',
  `resolve_status` tinyint NOT NULL DEFAULT 0 COMMENT '节点解析状态',
  `planning_status` varchar(16) DEFAULT NULL COMMENT '节点排程状态',
  `poi_verification_status` varchar(16) DEFAULT NULL COMMENT 'POI校验状态',
  `must_visit` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否必去',
  `locked` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否锁定',
  `source` varchar(32) DEFAULT NULL COMMENT '节点来源',
  `provider` varchar(32) DEFAULT NULL COMMENT 'POI数据供应商',
  `poi_snapshot_json` text DEFAULT NULL COMMENT 'POI快照JSON',
  `candidates_json` text DEFAULT NULL COMMENT '候选POI JSON',
  `citation_ids_json` text DEFAULT NULL COMMENT '引用来源ID JSON',
  `creator` varchar(64) NOT NULL DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) NOT NULL DEFAULT '' COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_itinerary_day_item` (`user_itinerary_id`, `item_id`),
  KEY `idx_day_sort` (`user_itinerary_day_id`, `sort`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户行程节点';

-- 通用行程按天拆分；当前通用行程表无数据，可直接重建节点表。
DROP TABLE IF EXISTS `gift_itinerary_day_item`;
DROP TABLE IF EXISTS `gift_itinerary_day`;

CREATE TABLE `gift_itinerary_day` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '通用行程日程ID',
  `tenant_id` bigint NOT NULL DEFAULT 1 COMMENT '租户ID',
  `itinerary_id` bigint NOT NULL COMMENT '通用行程ID',
  `day` int NOT NULL COMMENT '行程第几天，从1开始',
  `city_id` int DEFAULT NULL COMMENT '当日城市ID',
  `district_id` int DEFAULT NULL COMMENT '当日区县ID',
  `title` varchar(128) DEFAULT NULL COMMENT '当日标题',
  `description` varchar(1000) DEFAULT NULL COMMENT '当日描述',
  `sort` int NOT NULL DEFAULT 0 COMMENT '排序值',
  `creator` varchar(64) NOT NULL DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) NOT NULL DEFAULT '' COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  PRIMARY KEY (`id`), UNIQUE KEY `uk_itinerary_day` (`itinerary_id`, `day`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='通用行程每日安排';

CREATE TABLE `gift_itinerary_day_item` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '通用行程节点ID',
  `tenant_id` bigint NOT NULL DEFAULT 1 COMMENT '租户ID',
  `itinerary_id` bigint NOT NULL COMMENT '通用行程ID',
  `itinerary_day_id` bigint NOT NULL COMMENT '通用行程日程ID',
  `type` varchar(16) NOT NULL DEFAULT 'ACTIVITY' COMMENT '节点类型',
  `slot` varchar(32) DEFAULT NULL COMMENT '节点时段',
  `title` varchar(128) NOT NULL COMMENT '节点标题',
  `sub_title` varchar(128) DEFAULT NULL COMMENT '节点副标题',
  `description` varchar(1024) NOT NULL DEFAULT '' COMMENT '节点描述',
  `sort` int NOT NULL DEFAULT 0 COMMENT '当日节点排序值',
  `start_time` time DEFAULT NULL COMMENT '计划开始时间',
  `duration_minutes` int DEFAULT NULL COMMENT '建议停留分钟数',
  `poi_id` varchar(64) DEFAULT NULL COMMENT 'POI供应商地点ID',
  `province_id` int DEFAULT NULL COMMENT 'POI省级区域ID',
  `city_id` int DEFAULT NULL COMMENT 'POI城市ID',
  `district_id` int DEFAULT NULL COMMENT 'POI区县ID',
  `longitude` decimal(10,7) DEFAULT NULL COMMENT 'POI经度',
  `latitude` decimal(10,7) DEFAULT NULL COMMENT 'POI纬度',
  `cover_url` varchar(1024) DEFAULT NULL COMMENT '封面图地址',
  `cover_width` int DEFAULT NULL COMMENT '封面图宽度',
  `cover_height` int DEFAULT NULL COMMENT '封面图高度',
  `pic_urls` text DEFAULT NULL COMMENT '图片地址集合',
  `pic_sizes` text DEFAULT NULL COMMENT '图片尺寸集合',
  `tags` text DEFAULT NULL COMMENT '标签集合',
  `business_time` varchar(128) DEFAULT NULL COMMENT '营业时间',
  `address_detail` varchar(512) DEFAULT NULL COMMENT '详细地址',
  `phone_no` varchar(64) DEFAULT NULL COMMENT '联系电话',
  `creator` varchar(64) NOT NULL DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) NOT NULL DEFAULT '' COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  PRIMARY KEY (`id`), KEY `idx_itinerary_day_sort` (`itinerary_day_id`, `sort`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='通用行程节点';

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
