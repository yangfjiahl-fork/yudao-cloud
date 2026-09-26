-- 通用行程节点已使用 longitude、latitude 分别存储高德坐标，删除重复字段 gd_position。
-- 脚本可重复执行：字段不存在时仅返回提示，不重复执行 ALTER TABLE。

SET @gift_drop_gd_position_sql = (
  SELECT IF(
    COUNT(*) > 0,
    'ALTER TABLE `gift_itinerary_day_item` DROP COLUMN `gd_position`',
    'SELECT ''gift_itinerary_day_item.gd_position already absent'''
  )
  FROM `information_schema`.`COLUMNS`
  WHERE `TABLE_SCHEMA` = DATABASE()
    AND `TABLE_NAME` = 'gift_itinerary_day_item'
    AND `COLUMN_NAME` = 'gd_position'
);

PREPARE gift_drop_gd_position_stmt FROM @gift_drop_gd_position_sql;
EXECUTE gift_drop_gd_position_stmt;
DEALLOCATE PREPARE gift_drop_gd_position_stmt;
