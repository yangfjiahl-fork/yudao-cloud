package cn.iocoder.yudao.module.trade.framework.delivery.core.client;

import cn.hutool.crypto.digest.DigestUtil;

import java.util.Locale;
import java.util.Objects;

/**
 * 快递轨迹缓存 Key 工具类
 */
public class ExpressTrackCacheKeyUtils {

    private ExpressTrackCacheKeyUtils() {
    }

    public static String build(String expressCode, String logisticsNo, String phone) {
        String source = String.join("-", Objects.toString(expressCode, "").toLowerCase(Locale.ROOT),
                Objects.toString(logisticsNo, ""), Objects.toString(phone, ""));
        return DigestUtil.sha256Hex(source);
    }

}
