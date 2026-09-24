-- 轮播图跳转页面字典；字典 value 与 SliderItemJumpPageEnum.code 保持一致。
INSERT INTO `system_dict_type` (`name`, `type`, `status`, `remark`, `creator`, `updater`, `deleted`)
SELECT '轮播图跳转页面', 'gift_slider_item_jump_page', 0, '轮播图可跳转的页面', 'admin', 'admin', b'0'
WHERE NOT EXISTS (
  SELECT 1 FROM `system_dict_type`
  WHERE `type` = 'gift_slider_item_jump_page' AND `deleted` = b'0'
);

INSERT INTO `system_dict_data`
  (`sort`, `label`, `value`, `dict_type`, `status`, `color_type`, `css_class`, `remark`, `creator`, `updater`, `deleted`)
SELECT 1, '分享页', 'SHARE', 'gift_slider_item_jump_page', 0, 'primary', '', '跳转到分享页', 'admin', 'admin', b'0'
WHERE NOT EXISTS (
  SELECT 1 FROM `system_dict_data`
  WHERE `dict_type` = 'gift_slider_item_jump_page' AND `value` = 'SHARE' AND `deleted` = b'0'
);

-- 同步代码生成配置，后续重新生成管理端代码时继续使用该字典。
UPDATE `infra_codegen_column` column_config
JOIN `infra_codegen_table` table_config ON table_config.`id` = column_config.`table_id`
SET column_config.`dict_type` = 'gift_slider_item_jump_page'
WHERE table_config.`table_name` = 'gift_slider_item'
  AND column_config.`java_field` = 'jumpPage';
