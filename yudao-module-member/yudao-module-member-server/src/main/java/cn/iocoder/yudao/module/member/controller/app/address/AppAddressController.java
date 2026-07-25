package cn.iocoder.yudao.module.member.controller.app.address;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.member.controller.app.address.vo.AppAddressCreateFromWxAddressReqVO;
import cn.iocoder.yudao.module.member.controller.app.address.vo.AppAddressCreateReqVO;
import cn.iocoder.yudao.module.member.controller.app.address.vo.AppAddressRespVO;
import cn.iocoder.yudao.module.member.controller.app.address.vo.AppAddressUpdateReqVO;
import cn.iocoder.yudao.module.member.convert.address.AddressConvert;
import cn.iocoder.yudao.module.member.dal.dataobject.address.MemberAddressDO;
import cn.iocoder.yudao.module.member.service.address.AddressService;
import cn.iocoder.yudao.module.system.api.area.AreaApi;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;
import static cn.iocoder.yudao.module.member.enums.ErrorCodeConstants.ADDRESS_AREA_NOT_EXISTS;

@Tag(name = "用户 APP - 用户收件地址")
@RestController
@RequestMapping("/member/address")
@Validated
public class AppAddressController {

    @Resource
    private AddressService addressService;

    @Resource
    private AreaApi areaApi;

    @PostMapping("/create")
    @Operation(summary = "创建用户收件地址")
    public CommonResult<Long> createAddress(@Valid @RequestBody AppAddressCreateReqVO createReqVO) {
        return success(addressService.createAddress(getLoginUserId(), createReqVO));
    }

    @PostMapping("/create-from-wx-address")
    @Operation(summary = "根据微信地址创建用户收件地址")
    public CommonResult<Long> createAddressFromWxAddress(@Valid @RequestBody AppAddressCreateFromWxAddressReqVO createReqVO) {
        AppAddressCreateReqVO addressCreateReqVO = AddressConvert.INSTANCE.convert(createReqVO);
        addressCreateReqVO.setAreaId(getAreaId(createReqVO));
        return success(addressService.createAddress(getLoginUserId(), addressCreateReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新用户收件地址")
    public CommonResult<Boolean> updateAddress(@Valid @RequestBody AppAddressUpdateReqVO updateReqVO) {
        addressService.updateAddress(getLoginUserId(), updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除用户收件地址")
    @Parameter(name = "id", description = "编号", required = true)
    public CommonResult<Boolean> deleteAddress(@RequestParam("id") Long id) {
        addressService.deleteAddress(getLoginUserId(), id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得用户收件地址")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    public CommonResult<AppAddressRespVO> getAddress(@RequestParam("id") Long id) {
        MemberAddressDO address = addressService.getAddress(getLoginUserId(), id);
        return success(AddressConvert.INSTANCE.convert(address));
    }

    @GetMapping("/get-default")
    @Operation(summary = "获得默认的用户收件地址")
    public CommonResult<AppAddressRespVO> getDefaultUserAddress() {
        MemberAddressDO address = addressService.getDefaultUserAddress(getLoginUserId());
        return success(AddressConvert.INSTANCE.convert(address));
    }

    @GetMapping("/list")
    @Operation(summary = "获得用户收件地址列表")
    public CommonResult<List<AppAddressRespVO>> getAddressList() {
        List<MemberAddressDO> list = addressService.getAddressList(getLoginUserId());
        return success(AddressConvert.INSTANCE.convertList(list));
    }

    private Long getAreaId(AppAddressCreateFromWxAddressReqVO createReqVO) {
        Long areaId = areaApi.getAreaId(createReqVO.getProvinceName(), createReqVO.getCityName(),
                createReqVO.getDistrictName()).getCheckedData();
        if (areaId == null) {
            throw exception(ADDRESS_AREA_NOT_EXISTS);
        }
        return areaId;
    }

}
