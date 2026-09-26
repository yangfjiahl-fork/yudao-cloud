-- 删除用户行程会话后保留已生成行程，因此解除行程与已删除会话的关联。
ALTER TABLE `gift_user_itinerary`
  MODIFY COLUMN `conversation_id` bigint DEFAULT NULL COMMENT '用户行程会话ID';
