-- 旅行规划 Agent（gift C 端）
-- 由 gift 模块持久化业务状态；ai_chat_* 只保存会话与对话转录。

CREATE TABLE `gift_trip_plan` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint NOT NULL,
  `conversation_id` bigint NOT NULL,
  `member_id` bigint NOT NULL,
  `current_itinerary_id` bigint DEFAULT NULL COMMENT '当前生效的旅行行程编号',
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
  KEY `idx_trip_status_expire` (`tenant_id`, `trip_id`, `status`, `expires_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='旅行事实与来源';

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
  `version` int NOT NULL COMMENT '同一旅行计划内从1开始递增的版本号',
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
  KEY `idx_trip_version` (`tenant_id`, `trip_id`, `version`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='旅行行程';

CREATE TABLE IF NOT EXISTS `gift_trip_itinerary_slot` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint NOT NULL,
  `itinerary_id` bigint NOT NULL,
  `day` int NOT NULL COMMENT '行程天数；交通节点固定为 0',
  `slot` varchar(32) NOT NULL,
  `skeleton` varchar(500) NOT NULL,
  `status` varchar(16) NOT NULL DEFAULT 'PENDING' COMMENT '节点展示状态：PENDING/RESOLVED',
  `resolve_status` tinyint NOT NULL DEFAULT 0 COMMENT '补充状态：0待处理 1处理中 2已完成 3失败',
  `detail` varchar(500) DEFAULT NULL,
  `candidates_json` text DEFAULT NULL,
  `citation_ids_json` text DEFAULT NULL,
  `creator` varchar(64) DEFAULT '',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updater` varchar(64) DEFAULT '',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` bit NOT NULL DEFAULT b'0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_itinerary_day_slot` (`tenant_id`, `itinerary_id`, `day`, `slot`),
  KEY `idx_itinerary_resolve_status` (`tenant_id`, `itinerary_id`, `resolve_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='旅行行程节点补充结果';

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

-- 已部署旅行行程的版本扩展（仅执行一次；历史存量使用版本 0）
ALTER TABLE `gift_trip_itinerary`
  ADD COLUMN `version` int NOT NULL DEFAULT 0 COMMENT '同一旅行计划内从1开始递增的版本号' AFTER `trip_id`,
  ADD KEY `idx_trip_version` (`tenant_id`, `trip_id`, `version`);

-- 已部署旅行状态表的当前行程指针扩展（仅执行一次）
ALTER TABLE `gift_trip_plan`
  ADD COLUMN `current_itinerary_id` bigint DEFAULT NULL COMMENT '当前生效的旅行行程编号' AFTER `member_id`;

-- 已部署旅行行程的独立节点补充表（历史骨架会在首次节点请求时按需初始化）
CREATE TABLE IF NOT EXISTS `gift_trip_itinerary_slot` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint NOT NULL,
  `itinerary_id` bigint NOT NULL,
  `day` int NOT NULL COMMENT '行程天数；交通节点固定为 0',
  `slot` varchar(32) NOT NULL,
  `skeleton` varchar(500) NOT NULL,
  `status` varchar(16) NOT NULL DEFAULT 'PENDING' COMMENT '节点展示状态：PENDING/RESOLVED',
  `resolve_status` tinyint NOT NULL DEFAULT 0 COMMENT '补充状态：0待处理 1处理中 2已完成 3失败',
  `detail` varchar(500) DEFAULT NULL,
  `candidates_json` text DEFAULT NULL,
  `citation_ids_json` text DEFAULT NULL,
  `creator` varchar(64) DEFAULT '',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updater` varchar(64) DEFAULT '',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` bit NOT NULL DEFAULT b'0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_itinerary_day_slot` (`tenant_id`, `itinerary_id`, `day`, `slot`),
  KEY `idx_itinerary_resolve_status` (`tenant_id`, `itinerary_id`, `resolve_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='旅行行程节点补充结果';

-- 修复初版独立节点表因 INSERT IGNORE 写入的租户编号 0
UPDATE `gift_trip_itinerary_slot` slot
INNER JOIN `gift_trip_itinerary` itinerary ON itinerary.id = slot.itinerary_id
SET slot.tenant_id = itinerary.tenant_id
WHERE slot.tenant_id = 0 AND itinerary.tenant_id <> 0;
