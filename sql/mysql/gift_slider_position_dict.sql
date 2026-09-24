-- 轮播位置必填；字典 value 与 SliderPositionEnum.code 保持一致。
ALTER TABLE `gift_slider`
  MODIFY COLUMN `position_code` varchar(32) NOT NULL COMMENT '轮播位置';

INSERT INTO `system_dict_type` (`name`, `type`, `status`, `remark`, `creator`, `updater`, `deleted`)
SELECT '轮播位置', 'gift_slider_position', 0, '轮播投放位置', 'admin', 'admin', b'0'
WHERE NOT EXISTS (
  SELECT 1 FROM `system_dict_type`
  WHERE `type` = 'gift_slider_position' AND `deleted` = b'0'
);

INSERT INTO `system_dict_data`
  (`sort`, `label`, `value`, `dict_type`, `status`, `color_type`, `css_class`, `remark`, `creator`, `updater`, `deleted`)
SELECT 1, '首页顶部', 'HOME_TOP', 'gift_slider_position', 0, 'primary', '', '首页顶部轮播', 'admin', 'admin', b'0'
WHERE NOT EXISTS (
  SELECT 1 FROM `system_dict_data`
  WHERE `dict_type` = 'gift_slider_position' AND `value` = 'HOME_TOP' AND `deleted` = b'0'
);

-- 同步代码生成配置，后续重新生成管理端代码时继续使用该字典。
UPDATE `infra_codegen_column` column_config
JOIN `infra_codegen_table` table_config ON table_config.`id` = column_config.`table_id`
SET column_config.`dict_type` = 'gift_slider_position'
WHERE table_config.`table_name` = 'gift_slider'
  AND column_config.`java_field` = 'positionCode';
