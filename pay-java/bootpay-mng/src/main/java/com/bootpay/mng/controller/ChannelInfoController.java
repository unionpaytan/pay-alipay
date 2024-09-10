package com.bootpay.mng.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bootpay.common.authenticator.GoogleAuthenticator;
import com.bootpay.common.constants.CodeConst;
import com.bootpay.common.constants.enums.EnumAccountType;
import com.bootpay.common.constants.enums.EnumChannelStatus;
import com.bootpay.common.excetion.TranException;
import com.bootpay.common.utils.DateUtil;
import com.bootpay.common.utils.EncryptUtil;
import com.bootpay.common.utils.ReturnMsg;
import com.bootpay.core.entity.*;
import com.bootpay.core.service.*;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author
 * @since 2019-03-17
 */
@RestController
@RequestMapping("/channelManager")
public class ChannelInfoController {
	
	@Autowired
	private IWithdrawChannelInfoService channelInfoService;

	@Autowired
	private IWithdrawChannelInfoService iWithdrawChannelInfoService;

	@Autowired
	private IPayPlatformIncomeInfoService incomeInfoService;

	@Autowired
	private IPayMerchantInfoService iPayMerchantInfoService;

	@Autowired
	private ICardChannelInfoService iCardChannelInfoService; //接入银行卡三方通道

	@Value("${app.walletPrivateKeySalt}") //application.yml文件读取
	private String walletPrivateKeySalt;
	/**
	 * 获取代付通道列表 分页
	 * @param
	 * @param rows 条数
	 * @param page 页码
	 * @return
	 */
	@RequestMapping(value="/queryChannelInfoPage",method = {RequestMethod.POST, RequestMethod.GET})
	@ResponseBody
	public ReturnMsg queryChannelInfoList(
			String payType,
			String channelCode,String channelStatus,String channelName,Integer rows,Integer page){
        Page<WithdrawChannelInfo> pages = new Page<>(page,rows);
        QueryWrapper<WithdrawChannelInfo> queryWrapper = new QueryWrapper<>();
        Map<String,Object> params = new HashMap<String,Object>();
		params.put("PAY_TYPE", payType);
        params.put("CHANNEL_CODE", channelCode);
        params.put("CHANNEL_STATUS", channelStatus);
        queryWrapper = queryWrapper.allEq(params, false); //忽略value为null 值为null空 则忽略
        queryWrapper.orderByDesc("CHANNEL_CODE");
        queryWrapper.lambda().like(StringUtils.isNotBlank(channelName),WithdrawChannelInfo :: getChannelName, channelName);
        IPage<WithdrawChannelInfo>  pageMap = channelInfoService.page(pages, queryWrapper);
        return new ReturnMsg(pageMap);	
	}
	
	
	/**
	 * 获取可用通道
	 * @return
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping(value="/queryChannelInfo",method = {RequestMethod.POST, RequestMethod.GET})
	@ResponseBody
	public ReturnMsg queryChannelInfo(String payType){
   
        QueryWrapper<WithdrawChannelInfo> queryWrapper = new QueryWrapper<WithdrawChannelInfo>();
		Map<String,Object> params = new HashMap<String,Object>();
		params.put("PAY_TYPE", payType);
		queryWrapper = queryWrapper.allEq(params, false);
		queryWrapper.orderByAsc("PAY_TYPE").orderByAsc("CHANNEL_NAME");

//        queryWrapper.select("PAY_TYPE as payType","CHANNEL_CODE as channelCode","CHANNEL_NAME as channelName");
//        queryWrapper.lambda()
//        .eq(WithdrawChannelInfo :: getChannelStatus, EnumChannelStatus.ENABLE.getName())
//				.eq(WithdrawChannelInfo::getPayType,payType)
//				.orderByAsc(WithdrawChannelInfo :: getPayType)
//				.orderByAsc(WithdrawChannelInfo::getChannelName);
        List<Map<String,Object>> list = channelInfoService.listMaps(queryWrapper);
        return new ReturnMsg(list);	
	}

	/**
	 * 获取可用通道
	 * @return
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping(value="/queryDepositChannelInfo",method = {RequestMethod.POST, RequestMethod.GET})
	@ResponseBody
	public ReturnMsg queryDepositChannelInfo(){

		QueryWrapper<WithdrawChannelInfo> queryWrapper = new QueryWrapper<WithdrawChannelInfo>();
		queryWrapper.select("CHANNEL_CODE as channelCode","CHANNEL_NAME as channelName");
		queryWrapper.lambda()
				.eq(WithdrawChannelInfo :: getChannelStatus, EnumChannelStatus.ENABLE.getName())
				.eq(WithdrawChannelInfo::getPayType,CodeConst.PayType.RECEIVE_IN)
				.orderByAsc(WithdrawChannelInfo :: getChannelName);
		List<Map<String,Object>> list = iWithdrawChannelInfoService.listMaps(queryWrapper);
		return new ReturnMsg(list);
	}

	/**
	 * 获取可用通道
	 * @return
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping(value="/queryMerchantChannelInfo",method = {RequestMethod.POST, RequestMethod.GET})
	@ResponseBody
	public ReturnMsg queryMerchantChannelInfo(
			String productCode,
			String channelCode,
			String merchantId,
			String payType){

		if (merchantId == null || "".equals(merchantId)){
			return new ReturnMsg("商户编号不能为空");
		}
		Map<String,Object> params = new HashMap<String,Object>();
		params.put("productCode", productCode);//支付产品
		params.put("channelCode", channelCode);//支付产品的其中一个通道
		params.put("payType", payType);
		params.put("merchantId", merchantId);
		params.put("channelStatus", EnumChannelStatus.ENABLE.getName()); //0-启用
		List<Map<String,Object>> list = iWithdrawChannelInfoService.queryMerchantChannelInfo(params);
		return new ReturnMsg(list);
	}


	/**
	 * 获取三方可用通道
	 * @return
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping(value="/queryCardChannelInfo",method = {RequestMethod.POST, RequestMethod.GET})
	@ResponseBody
	public ReturnMsg queryCardChannelInfo(){
		QueryWrapper<CardChannelInfo> queryWrapper = new QueryWrapper<CardChannelInfo>();
		queryWrapper.select("CARD_CHANNEL_CODE as cardChannelCode","CHANNEL_NAME as channelName");
		queryWrapper.lambda()
				.eq(CardChannelInfo :: getChlStatus, EnumChannelStatus.DISABLE.getName()).orderByAsc(CardChannelInfo :: getChlName);
		List<Map<String,Object>> list = iCardChannelInfoService.listMaps(queryWrapper);
		return new ReturnMsg(list);
	}


	/**
	 * 修改通道信息
	 * @@Size(min=4,max=4,message="通道编号固定长度4位") String channelCode,
	 * @param
	 * @return
	 */
	@RequestMapping(value="/modifyChannelInfo",method = {RequestMethod.POST, RequestMethod.GET},name = "admin")
	@ResponseBody
	public ReturnMsg modifyChannelInfo(@NotBlank(message="id不能为空")String id,
			String channelName,
		    String channelCode,
			String channelStatus,
			String channelFeeType,
			String rechargeCost,
			String singleCost,
			String singleMinimum,
			String singleHighest,
			String singleRechargeCost,
			String dayQuota,
			String beginWithdrawTime,
			String endWithdrawTime) throws TranException{
		if(!DateUtil.isValidDate(beginWithdrawTime, DateUtil.FORMAT_HHMMSS) || !DateUtil.isValidDate(endWithdrawTime, DateUtil.FORMAT_HHMMSS)){
			throw new TranException(CodeConst.BUSINESS_ERROR_CODE,"代付时间格式不正确");
		}
		WithdrawChannelInfo channelInfo = new WithdrawChannelInfo();
		channelInfo.setChannelName(channelName);
		channelInfo.setChannelCode(channelCode);
		channelInfo.setChannelFeeType(channelFeeType); //手续费
		channelInfo.setChannelStatus(channelStatus);
		channelInfo.setRechargeCost(rechargeCost);
		channelInfo.setSingleCost(new BigDecimal(singleCost));
		channelInfo.setSingleMinimum(new BigDecimal(singleMinimum));
		channelInfo.setSingleHighest(new BigDecimal(singleHighest));
		channelInfo.setBeginWithdrawTime(beginWithdrawTime);
		channelInfo.setEndWithdrawTime(endWithdrawTime);
		channelInfo.setSingleRechargeCost(new BigDecimal(singleRechargeCost));
		channelInfo.setDayQuota(new BigDecimal(dayQuota));
        channelInfoService.update(channelInfo, new UpdateWrapper<WithdrawChannelInfo>().lambda().eq(WithdrawChannelInfo :: getId, id));
        return new ReturnMsg();	
	}
	
	
	
	/**
	 * 创建代付通道
	 * @param channelInfo
	 * @return
	 */
	@RequestMapping(value="/addChannelInfo",method = {RequestMethod.POST, RequestMethod.GET},name = "admin")
	@ResponseBody
	public ReturnMsg addChannelInfo(WithdrawChannelInfo channelInfo) throws TranException {
		if(!DateUtil.isValidDate(channelInfo.getBeginWithdrawTime(), DateUtil.FORMAT_HHMMSS) || !DateUtil.isValidDate(channelInfo.getEndWithdrawTime(), DateUtil.FORMAT_HHMMSS)){
			throw new TranException(CodeConst.BUSINESS_ERROR_CODE,"代付时间格式不正确");
		}
		WithdrawChannelInfo query = channelInfoService.getOne(new QueryWrapper<WithdrawChannelInfo>().lambda().eq(WithdrawChannelInfo :: getChannelCode, channelInfo.getChannelCode()));
		if(query !=null){
			throw new TranException(CodeConst.BUSINESS_ERROR_CODE,"通道编号重复");
		}
		channelInfo.setPayType(channelInfo.getPayType());//0-付 1-收
		channelInfo.setChannelFeeType(channelInfo.getChannelFeeType()); //手续费结算方式
		channelInfo.setChannelStatus(EnumChannelStatus.ENABLE.getName());
		channelInfo.setCreateTime(new Date());
		channelInfoService.save(channelInfo);
		
		//初始化代付通道余额
		PayPlatformIncomeInfo incomeInfo = new PayPlatformIncomeInfo();
		incomeInfo.setChannelType(CodeConst.ChannelTypeConst.WITHDRAW);//平台
		incomeInfo.setChannelFeeType(channelInfo.getChannelFeeType());
		incomeInfo.setChannelCode(channelInfo.getChannelCode());
		incomeInfo.setChannelName(channelInfo.getChannelName());
		incomeInfo.setPlatformAccountBalance(new BigDecimal("0.0000"));
		incomeInfoService.save(incomeInfo);
        return new ReturnMsg();	
	}
	
	
	/**
	 * 修改通道状态
	 * @param
	 * @return
	 */
	@RequestMapping(value="/modifyChannelStatus",method = {RequestMethod.POST, RequestMethod.GET},name = "admin")
	@ResponseBody
	public ReturnMsg modifyChannelStatus(@NotBlank(message="id不能为空")String id,@NotBlank(message="通道状态不能为空")String channelStatus) throws TranException {
		WithdrawChannelInfo entity = new WithdrawChannelInfo();
		entity.setChannelStatus(channelStatus);
		channelInfoService.update(entity, new UpdateWrapper<WithdrawChannelInfo>().lambda().eq(WithdrawChannelInfo :: getId, id));
        return new ReturnMsg();	
	}
	
	
	
	
	/**
	 * 删除代付通道
	 * @param
	 * @return
	 */
	@RequestMapping(value="/delChannelInfo",method = {RequestMethod.POST, RequestMethod.GET},name = "admin")
	@ResponseBody
	public ReturnMsg delChannelInfo(@NotBlank(message="通道id不能为空")String channelCode,String tableMerchantId,String googleCode) throws Exception {

		PayMerchantInfo merchantInfo = iPayMerchantInfoService.getOne(new QueryWrapper<PayMerchantInfo>().lambda().eq(PayMerchantInfo :: getMerchantId, tableMerchantId));

//		if(merchantInfo.getAccountType().equals(EnumAccountType.MERCHANT.getName())){
//			throw new TranException(CodeConst.BUSINESS_ERROR_CODE,"权限不足");
//		}
		if(googleCode == null || "".equals(googleCode)){
			throw new TranException(CodeConst.BUSINESS_ERROR_CODE,"google验证码错误");
		}

		String googleKeyDecode =  EncryptUtil.aesDecrypt(merchantInfo.getGoogleKey(),walletPrivateKeySalt);
		if(!GoogleAuthenticator.check_code(googleKeyDecode, googleCode, System.currentTimeMillis())){
			throw new TranException(CodeConst.BUSINESS_ERROR_CODE,"google验证码错误");
		}

		channelInfoService.remove(new QueryWrapper<WithdrawChannelInfo>().lambda().eq(WithdrawChannelInfo :: getChannelCode, channelCode));
        return new ReturnMsg();	
	}
	
	

	
}
