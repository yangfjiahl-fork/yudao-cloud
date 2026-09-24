-- 已部署旧行程事件表的环境仅执行一次。
-- 全新环境直接执行 gift_user_itinerary_structured.sql，无需执行本脚本。

DROP TABLE IF EXISTS `gift_user_itinerary_transport_segment`;

RENAME TABLE `gift_itinerary_event` TO `gift_user_itinerary_conversation_event`;

ALTER TABLE `gift_user_itinerary_conversation`
  DROP COLUMN `current_user_itinerary_id`,
  DROP INDEX `idx_member_update_time`,
  ADD KEY `idx_member_update_time` (`member_id`, `update_time`);

ALTER TABLE `gift_user_itinerary_conversation_event`
  DROP INDEX `idx_conversation_event`,
  DROP INDEX `idx_run_id`,
  DROP INDEX `idx_user_itinerary_id`,
  ADD KEY `idx_conversation_event` (`conversation_id`, `id`),
  ADD KEY `idx_run_id` (`run_id`),
  ADD KEY `idx_user_itinerary_id` (`user_itinerary_id`);

ALTER TABLE `gift_user_itinerary`
  DROP INDEX `uk_conversation_version`,
  DROP INDEX `uk_result_event_id`,
  DROP INDEX `idx_member_start_date`,
  DROP COLUMN `version`,
  ADD UNIQUE KEY `uk_conversation_id` (`conversation_id`),
  ADD UNIQUE KEY `uk_result_event_id` (`result_event_id`),
  ADD KEY `idx_member_start_date` (`member_id`, `start_date`);

ALTER TABLE `gift_user_itinerary_day`
  DROP INDEX `uk_user_itinerary_day`,
  DROP COLUMN `route_data_status`,
  ADD UNIQUE KEY `uk_user_itinerary_day` (`user_itinerary_id`, `day`);

ALTER TABLE `gift_user_itinerary_day_item`
  DROP INDEX `uk_user_itinerary_day_item`,
  DROP INDEX `idx_day_sort`,
  DROP COLUMN `previous_item_id`,
  DROP COLUMN `travel_mode_from_previous`,
  DROP COLUMN `travel_distance_meters_from_previous`,
  DROP COLUMN `travel_duration_minutes_from_previous`,
  DROP COLUMN `travel_provider_from_previous`,
  DROP COLUMN `travel_status_from_previous`,
  DROP COLUMN `travel_route_points_json`,
  ADD UNIQUE KEY `uk_user_itinerary_day_item` (`user_itinerary_id`, `item_id`),
  ADD KEY `idx_day_sort` (`user_itinerary_day_id`, `sort`);

ALTER TABLE `gift_itinerary_day`
  DROP INDEX `uk_itinerary_day`,
  ADD UNIQUE KEY `uk_itinerary_day` (`itinerary_id`, `day`);

ALTER TABLE `gift_itinerary_day_item`
  DROP INDEX `idx_itinerary_day_sort`,
  DROP COLUMN `previous_item_id`,
  DROP COLUMN `travel_mode_from_previous`,
  DROP COLUMN `travel_distance_meters_from_previous`,
  DROP COLUMN `travel_duration_minutes_from_previous`,
  ADD KEY `idx_itinerary_day_sort` (`itinerary_day_id`, `sort`);
