CREATE TABLE `gift_article_like` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
    `article_id` bigint NOT NULL COMMENT '文章编号',
    `member_id` bigint NOT NULL COMMENT '点赞会员编号',
    `creator` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '' COMMENT '创建人编号',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updater` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '' COMMENT '更新人编号',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_tenant_article_user` (`article_id`, `member_id`),
    KEY `idx_tenant_user` (`member_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文章点赞记录';
