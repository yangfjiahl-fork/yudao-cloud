ALTER TABLE `gift_itinerary_category`
  ADD COLUMN `status` tinyint NOT NULL DEFAULT 0 COMMENT '状态（0开启 1关闭）' AFTER `sort`;
