-- 已部署 gift_itinerary_conversation 的环境仅执行一次。
-- 全新环境直接执行 gift_user_itinerary_structured.sql，无需执行本脚本。

RENAME TABLE `gift_itinerary_conversation` TO `gift_user_itinerary_conversation`;
