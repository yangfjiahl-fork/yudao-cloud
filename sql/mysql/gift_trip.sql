-- 旧旅行规划表已由 gift_user_itinerary_conversation、gift_user_itinerary_conversation_event
-- 和 gift_user_itinerary_* 取代。按依赖顺序删除，不迁移历史旧结构数据。

DROP TABLE IF EXISTS `gift_trip_itinerary_slot`;
DROP TABLE IF EXISTS `gift_trip_itinerary`;
DROP TABLE IF EXISTS `gift_trip_fact`;
DROP TABLE IF EXISTS `gift_trip_source`;
DROP TABLE IF EXISTS `gift_trip_run`;
DROP TABLE IF EXISTS `gift_trip_plan`;
