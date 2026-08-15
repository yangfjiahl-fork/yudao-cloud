package cn.iocoder.yudao.module.gift.service.article;

import java.util.*;

import cn.iocoder.yudao.module.gift.controller.admin.article.vo.ArticleSuffixPageReqVO;
import cn.iocoder.yudao.module.gift.controller.admin.article.vo.ArticleSuffixSaveReqVO;
import cn.iocoder.yudao.module.gift.dal.dataobject.article.ArticleSuffixDO;
import jakarta.validation.*;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;

/**
 * 文章后缀 Service 接口
 *
 * @author 羔享科技
 */
public interface ArticleSuffixService {

    /**
     * 创建文章后缀
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createArticleSuffix(@Valid ArticleSuffixSaveReqVO createReqVO);

    /**
     * 更新文章后缀
     *
     * @param updateReqVO 更新信息
     */
    void updateArticleSuffix(@Valid ArticleSuffixSaveReqVO updateReqVO);

    /**
     * 删除文章后缀
     *
     * @param id 编号
     */
    void deleteArticleSuffix(Long id);

    /**
    * 批量删除文章后缀
    *
    * @param ids 编号
    */
    void deleteArticleSuffixListByIds(List<Long> ids);

    /**
     * 获得文章后缀
     *
     * @param id 编号
     * @return 文章后缀
     */
    ArticleSuffixDO getArticleSuffix(Long id);

    /**
     * 获得文章后缀分页
     *
     * @param pageReqVO 分页查询
     * @return 文章后缀分页
     */
    PageResult<ArticleSuffixDO> getArticleSuffixPage(ArticleSuffixPageReqVO pageReqVO);

}
