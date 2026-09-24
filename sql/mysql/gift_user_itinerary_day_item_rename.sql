-- 已部署 gift_user_itinerary_item 的环境仅执行一次。

RENAME TABLE `gift_user_itinerary_item` TO `gift_user_itinerary_day_item`;

ALTER TABLE `gift_user_itinerary_day_item`
  RENAME INDEX `uk_user_itinerary_item` TO `uk_user_itinerary_day_item`;
