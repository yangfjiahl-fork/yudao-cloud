-- 旅行规划 Agent（gift C 端）
-- 由 gift 模块持久化业务状态；ai_chat_* 只保存会话与对话转录。

CREATE TABLE `gift_trip_plan` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint NOT NULL,
  `conversation_id` bigint NOT NULL,
  `member_id` bigint NOT NULL,
  `state_json` text NOT NULL,
  `missing_required_json` text NOT NULL,
  `status` tinyint NOT NULL DEFAULT 1,
  `creator` varchar(64) DEFAULT '',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updater` varchar(64) DEFAULT '',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` bit NOT NULL DEFAULT b'0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_conversation_member` (`tenant_id`, `conversation_id`, `member_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='旅行规划状态';

CREATE TABLE `gift_trip_fact` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint NOT NULL,
  `trip_id` bigint NOT NULL,
  `entity_id` bigint DEFAULT NULL COMMENT '关联的旅行实体编号',
  `fact_key` varchar(128) NOT NULL,
  `value_json` text NOT NULL,
  `source_id` bigint DEFAULT NULL,
  `retrieved_at` datetime DEFAULT NULL,
  `expires_at` datetime DEFAULT NULL,
  `confidence` decimal(4,3) DEFAULT NULL,
  `status` tinyint NOT NULL DEFAULT 0 COMMENT '0待确认 1已验证 2冲突 3过期',
  `creator` varchar(64) DEFAULT '',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updater` varchar(64) DEFAULT '',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` bit NOT NULL DEFAULT b'0',
  PRIMARY KEY (`id`),
  KEY `idx_trip_status_expire` (`tenant_id`, `trip_id`, `status`, `expires_at`),
  KEY `idx_entity_status_expire` (`entity_id`, `status`, `expires_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='旅行事实与来源';

CREATE TABLE `gift_trip_entity` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint NOT NULL,
  `trip_id` bigint NOT NULL,
  `entity_type` varchar(32) NOT NULL COMMENT 'CITY/POI/HOTEL/RESTAURANT/TRANSPORT_HUB',
  `name` varchar(256) NOT NULL,
  `provider` varchar(64) NOT NULL COMMENT 'USER_INPUT/gaode/aliyun/mock_hotel',
  `external_id` varchar(256) DEFAULT NULL,
  `parent_entity_id` bigint DEFAULT NULL,
  `longitude` decimal(11,8) DEFAULT NULL,
  `latitude` decimal(10,8) DEFAULT NULL,
  `metadata_json` text DEFAULT NULL,
  `status` tinyint NOT NULL DEFAULT 0 COMMENT '0待解析 1已解析 2不可用',
  `creator` varchar(64) DEFAULT '',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updater` varchar(64) DEFAULT '',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` bit NOT NULL DEFAULT b'0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_trip_type_name` (`trip_id`, `entity_type`, `name`),
  KEY `idx_trip_type` (`trip_id`, `entity_type`),
  KEY `idx_provider_external` (`provider`, `external_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='旅行关键实体';

CREATE TABLE `gift_trip_source` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint NOT NULL,
  `trip_id` bigint NOT NULL,
  `source_type` varchar(32) NOT NULL COMMENT 'official/provider/web_search',
  `source_name` varchar(128) NOT NULL,
  `source_url` varchar(1024) NOT NULL,
  `retrieved_at` datetime NOT NULL,
  `expires_at` datetime DEFAULT NULL,
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '0待核验 1有效 2冲突 3过期',
  `creator` varchar(64) DEFAULT '',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updater` varchar(64) DEFAULT '',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` bit NOT NULL DEFAULT b'0',
  PRIMARY KEY (`id`),
  KEY `idx_trip_status` (`tenant_id`, `trip_id`, `status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='旅行事实来源';

CREATE TABLE `gift_trip_itinerary` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint NOT NULL,
  `trip_id` bigint NOT NULL,
  `message_id` bigint NOT NULL COMMENT '关联 ai_chat_message.id',
  `content_json` longtext NOT NULL,
  `citation_ids_json` text NOT NULL,
  `status` tinyint NOT NULL DEFAULT 1,
  `creator` varchar(64) DEFAULT '',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updater` varchar(64) DEFAULT '',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` bit NOT NULL DEFAULT b'0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_message_id` (`message_id`),
  KEY `idx_trip` (`tenant_id`, `trip_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='旅行行程';

CREATE TABLE `gift_trip_run` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint NOT NULL,
  `trip_id` bigint NOT NULL,
  `stage` varchar(32) NOT NULL,
  `model` varchar(128) DEFAULT NULL,
  `prompt_tokens` bigint NOT NULL DEFAULT 0,
  `completion_tokens` bigint NOT NULL DEFAULT 0,
  `total_tokens` bigint NOT NULL DEFAULT 0,
  `duration_ms` bigint DEFAULT NULL,
  `status` tinyint NOT NULL DEFAULT 0 COMMENT '0运行中 1成功 2失败',
  `error_message` varchar(500) DEFAULT NULL,
  `input_json` longtext DEFAULT NULL COMMENT '节点输入与状态快照',
  `output_json` longtext DEFAULT NULL COMMENT '节点结构化输出快照',
  `creator` varchar(64) DEFAULT '',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updater` varchar(64) DEFAULT '',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` bit NOT NULL DEFAULT b'0',
  PRIMARY KEY (`id`),
  KEY `idx_trip_stage` (`tenant_id`, `trip_id`, `stage`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='旅行 Agent 编排运行记录';

-- 旅行会话的三级行政区划（一次性执行；依赖 ai_chat_conversation 表已存在）
ALTER TABLE `ai_chat_conversation`
  ADD COLUMN `province_id` bigint DEFAULT NULL COMMENT '省级地区编号' AFTER `user_type`,
  ADD COLUMN `city_id` bigint DEFAULT NULL COMMENT '市级地区编号' AFTER `province_id`,
  ADD COLUMN `district_id` bigint DEFAULT NULL COMMENT '区县级地区编号' AFTER `city_id`;

-- 已部署旅行表的流式节点快照扩展（仅执行一次）
ALTER TABLE `gift_trip_run`
  ADD COLUMN `input_json` longtext DEFAULT NULL COMMENT '节点输入与状态快照' AFTER `error_message`,
  ADD COLUMN `output_json` longtext DEFAULT NULL COMMENT '节点结构化输出快照' AFTER `input_json`;

-- 已部署旅行表的消息关联扩展：历史存量允许为空，新生成行程必须写入 message_id
ALTER TABLE `gift_trip_itinerary`
  ADD COLUMN `message_id` bigint DEFAULT NULL COMMENT '关联 ai_chat_message.id' AFTER `trip_id`,
  ADD UNIQUE KEY `uk_message_id` (`message_id`);

-- 已部署旅行事实表的实体关联扩展（仅执行一次）
ALTER TABLE `gift_trip_fact`
  ADD COLUMN `entity_id` bigint DEFAULT NULL COMMENT '关联的旅行实体编号' AFTER `trip_id`,
  ADD KEY `idx_entity_status_expire` (`entity_id`, `status`, `expires_at`);
