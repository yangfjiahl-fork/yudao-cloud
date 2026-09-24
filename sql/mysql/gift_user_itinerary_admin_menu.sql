-- 用户行程管理后台菜单及按钮权限。
-- 按路由定位父菜单，避免依赖不同环境中的自增 ID；所有插入均可重复执行。
SET @user_itinerary_parent_id := (
  SELECT `id`
  FROM `system_menu`
  WHERE `path` = 'member-itinerary'
    AND `type` = 1
    AND `deleted` = b'0'
  ORDER BY `id`
  LIMIT 1
);

INSERT INTO `system_menu`
  (`name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `status`, `component_name`)
SELECT menu_definition.`name`, '', 2, menu_definition.`sort`, @user_itinerary_parent_id,
       menu_definition.`path`, '', menu_definition.`component`, 0, menu_definition.`component_name`
FROM (
  SELECT '用户行程每日安排管理' AS `name`, 1 AS `sort`,
         'user-itinerary-day' AS `path`, 'gift/useritineraryday/index' AS `component`,
         'UserItineraryDay' AS `component_name`
  UNION ALL
  SELECT '用户行程节点管理', 2,
         'user-itinerary-day-item', 'gift/useritinerarydayitem/index', 'UserItineraryDayItem'
  UNION ALL
  SELECT '收藏行程管理', 3,
         'user-itinerary-liked', 'gift/useritineraryliked/index', 'UserItineraryLiked'
) menu_definition
WHERE @user_itinerary_parent_id IS NOT NULL
  AND NOT EXISTS (
    SELECT 1
    FROM `system_menu` existing_menu
    WHERE existing_menu.`parent_id` = @user_itinerary_parent_id
      AND existing_menu.`path` = menu_definition.`path`
      AND existing_menu.`deleted` = b'0'
  );

INSERT INTO `system_menu`
  (`name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `status`)
SELECT permission_definition.`name`, permission_definition.`permission`, 3,
       permission_definition.`sort`, parent_menu.`id`, '', '', '', 0
FROM (
  SELECT 'user-itinerary-day' AS `parent_path`, '用户行程每日安排查询' AS `name`,
         'gift:user-itinerary-day:query' AS `permission`, 1 AS `sort`
  UNION ALL SELECT 'user-itinerary-day', '用户行程每日安排创建', 'gift:user-itinerary-day:create', 2
  UNION ALL SELECT 'user-itinerary-day', '用户行程每日安排更新', 'gift:user-itinerary-day:update', 3
  UNION ALL SELECT 'user-itinerary-day', '用户行程每日安排删除', 'gift:user-itinerary-day:delete', 4
  UNION ALL SELECT 'user-itinerary-day', '用户行程每日安排导出', 'gift:user-itinerary-day:export', 5
  UNION ALL SELECT 'user-itinerary-day-item', '用户行程节点查询', 'gift:user-itinerary-day-item:query', 1
  UNION ALL SELECT 'user-itinerary-day-item', '用户行程节点创建', 'gift:user-itinerary-day-item:create', 2
  UNION ALL SELECT 'user-itinerary-day-item', '用户行程节点更新', 'gift:user-itinerary-day-item:update', 3
  UNION ALL SELECT 'user-itinerary-day-item', '用户行程节点删除', 'gift:user-itinerary-day-item:delete', 4
  UNION ALL SELECT 'user-itinerary-day-item', '用户行程节点导出', 'gift:user-itinerary-day-item:export', 5
  UNION ALL SELECT 'user-itinerary-liked', '收藏行程查询', 'gift:user-itinerary-liked:query', 1
  UNION ALL SELECT 'user-itinerary-liked', '收藏行程创建', 'gift:user-itinerary-liked:create', 2
  UNION ALL SELECT 'user-itinerary-liked', '收藏行程更新', 'gift:user-itinerary-liked:update', 3
  UNION ALL SELECT 'user-itinerary-liked', '收藏行程删除', 'gift:user-itinerary-liked:delete', 4
  UNION ALL SELECT 'user-itinerary-liked', '收藏行程导出', 'gift:user-itinerary-liked:export', 5
) permission_definition
JOIN `system_menu` parent_menu
  ON parent_menu.`parent_id` = @user_itinerary_parent_id
 AND parent_menu.`path` = permission_definition.`parent_path`
 AND parent_menu.`type` = 2
 AND parent_menu.`deleted` = b'0'
WHERE NOT EXISTS (
  SELECT 1
  FROM `system_menu` existing_permission
  WHERE existing_permission.`permission` = permission_definition.`permission`
    AND existing_permission.`deleted` = b'0'
);
