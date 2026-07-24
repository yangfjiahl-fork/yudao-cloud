/*
 * Copyright (C) 2019 ~ 2026 MeiTuan. All Rights Reserved.
 *
 */
package cn.iocoder.yudao.module.gift.convert;

import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

/**
 *
 * @author YangFeng(calvin)
 * @version v1.0
 * @date 2026/7/25 00:02
 */
public class AliyunAuthUtil {

    /**
     * 生成带 auth_key 的阿里云鉴权 URL (A型鉴权)
     *
     * @param rawUrl        原始文件或封面 URL
     * @param privateKey    阿里云点播/CDN 控制台中设置的鉴权 Key (PrivateKey)
     * @param expireSeconds 有效时长（单位：秒），例如 3600 (1小时)
     * @return 拼接后的鉴权 URL
     */
    public static String generateAuthUrl(String rawUrl, String privateKey, long expireSeconds) {
        try {
            // 1. 提取 URL 中的纯路径 Path（如 /vod-6072c5/xxx/sd.mp4）
            URL url = new URL(rawUrl);
            String path = url.getPath();

            // 2. 计算 Unix 过期时间戳（秒）
            long timestamp = (System.currentTimeMillis() / 1000) + expireSeconds;

            // 3. 随机数 rand 与用户 ID uid，通常默认填 "0"
            String rand = "0";
            String uid = "0";

            // 4. 构造待签名字符串: Path-timestamp-rand-uid-PrivateKey
            String stringToSign = String.format("%s-%d-%s-%s-%s", path, timestamp, rand, uid, privateKey);

            // 5. 计算 MD5 哈希值（小写 32 位）
            String md5Hash = md5(stringToSign);

            // 6. 组合 auth_key 参数值
            String authKey = String.format("%d-%s-%s-%s", timestamp, rand, uid, md5Hash);

            // 7. 拼接回原始 URL（如果原 URL 已带 ? 参数，用 & 连接，否则用 ? 连接）
            String separator = rawUrl.contains("?") ? "&" : "?";
            return rawUrl + separator + "auth_key=" + authKey;

        } catch (Exception e) {
            throw new RuntimeException("生成阿里云鉴权URL失败", e);
        }
    }

    /**
     * MD5 加密辅助工具方法
     */
    private static String md5(String input) throws Exception {
        MessageDigest md = MessageDigest.getInstance("MD5");
        byte[] digest = md.digest(input.getBytes(StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder();
        for (byte b : digest) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    // 测试运行
    public static void main(String[] args) {
        String privateKey = "your_aliyun_private_key"; // 在阿里云控制台设置的主 KEY
        String rawFileUrl = "http://xiaoyanggaoprod.oss-cn-shanghai.aliyuncs.com/vod-6072c5/004faa88876771f180f44531949c0102/24ab4aaa2adf4910afed88ec02805945-ad0e47fc72f41128c3f61b423c195a5b-sd.mp4";

        // 生成有效时长 2 小时 (7200秒) 的签名地址
        String authUrl = generateAuthUrl(rawFileUrl, privateKey, 7200);

        System.out.println("签名后的访问地址：");
        System.out.println(authUrl);
    }
}
