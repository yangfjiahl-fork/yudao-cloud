package cn.iocoder.yudao.module.member.api.config;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.member.api.config.dto.MemberConfigRespDTO;
import cn.iocoder.yudao.module.member.api.config.dto.PriceTransDTO;
import cn.iocoder.yudao.module.member.convert.config.MemberConfigConvert;
import cn.iocoder.yudao.module.member.dal.dataobject.config.MemberConfigDO;
import cn.iocoder.yudao.module.member.service.config.MemberConfigService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RestController;

import jakarta.annotation.Resource;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户配置 API 实现类
 *
 * @author owen
 */
@RestController // 提供 RESTful API 接口，给 Feign 调用
@Validated
public class MemberConfigApiImpl implements MemberConfigApi {

    @Resource
    private MemberConfigService memberConfigService;

    @Override
    public CommonResult<MemberConfigRespDTO> getConfig() {
        return success(MemberConfigConvert.INSTANCE.convert01(memberConfigService.getConfig()));
    }

    @Override
    public List<PriceTransDTO> batchGet(List<Integer> ids) {
        MemberConfigDO configDO = memberConfigService.getConfig();
        return ids.stream().map(fen -> PriceTransDTO.builder().price(fen)
                .woolPrice(new BigDecimal(fen).divide(configDO.getPointTradeDeductUnitPrice(),
                        0, RoundingMode.HALF_DOWN).intValue()).build())
                .toList();
    }
}
