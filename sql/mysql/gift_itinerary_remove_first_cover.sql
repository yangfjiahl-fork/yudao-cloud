-- 移除行程对象中已废弃的首图封面字段。
-- 已部署 gift_itinerary 的环境仅执行一次。
ALTER TABLE `gift_itinerary`
    DROP COLUMN `first_cover_url`,
    DROP COLUMN `first_cover_height`,
    DROP COLUMN `first_cover_width`;
