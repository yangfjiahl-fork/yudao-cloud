-- 轮播可不限定城市；未指定城市时 city_id 保存为 NULL。
ALTER TABLE `gift_slider`
  MODIFY COLUMN `city_id` bigint DEFAULT NULL COMMENT '城市ID（可选）';
