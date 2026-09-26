package cn.iocoder.yudao.module.gift.dal.mysql.memberlocationhistory;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.gift.dal.dataobject.memberlocationhistory.MemberLocationHistoryDO;
import org.apache.ibatis.annotations.Mapper;

/** 会员位置变更历史 Mapper。 */
@Mapper
public interface MemberLocationHistoryMapper extends BaseMapperX<MemberLocationHistoryDO> {

    default MemberLocationHistoryDO selectLatestByMemberId(Long memberId) {
        return selectOne(new LambdaQueryWrapperX<MemberLocationHistoryDO>()
                .eq(MemberLocationHistoryDO::getMemberId, memberId)
                .orderByDesc(MemberLocationHistoryDO::getId)
                .last("LIMIT 1"));
    }

}
