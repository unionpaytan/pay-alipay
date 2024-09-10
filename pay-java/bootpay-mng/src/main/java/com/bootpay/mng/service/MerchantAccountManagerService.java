package com.bootpay.mng.service;

import com.bootpay.common.excetion.TranException;
import com.bootpay.common.utils.ReturnMsg;
import com.bootpay.core.entity.PayMerchantChannelSite;
import com.bootpay.core.entity.PayMerchantInfo;
import com.bootpay.core.entity.PayWithdrawInfo;
import com.bootpay.core.entity.WithdrawChannelInfo;
import reactor.util.annotation.Nullable;

import javax.jms.JMSException;
import java.math.BigDecimal;
import java.util.List;

/**
 * 商户账户管理
 * @author Administrator
 *
 */
public interface MerchantAccountManagerService {
	
	
	/**
	 * 商户充值
	 * @param amt 交易金额
	 * @param amtCredit 入账金额 = 交易金额 - 随机立减 - 手续费率 - 单笔手续费
	 * @param merchantId 商户ID
	 * @throws TranException
	 */
	public void  merchantRecharge(BigDecimal amt,BigDecimal amtCredit,String merchantId,String merchantName,String remark,String managerName,String label,String channelCode, String merchantWithdrawNo,String withdrawNo,String flowType)throws TranException;

	/**
	 * 商户手续费充值
	 * @param rechargeAmt 充值金额
	 * @param merchantId 商户ID
	 * @throws TranException
	 */
	public void  merchantFeeRecharge(BigDecimal rechargeAmt,String withdrawNo,String merchantId,String merchantName,String remark,String managerName)throws TranException;


	/**
	 * 商户批量代付
	 * @param
	 * @param
	 * @param
	 * @param
	 * @param  API 接口
	 * @throws TranException
	 */
	public void batchWithdraw(PayMerchantInfo merchantInfo,PayMerchantChannelSite payMerchantChannelSite,WithdrawChannelInfo channelInfo,List<PayWithdrawInfo> list,String Type)throws TranException, InterruptedException;
	


	public ReturnMsg singleDeposit(PayMerchantChannelSite selectedPayMerchantChannelInfo,
								   WithdrawChannelInfo channelInfo,
								   PayMerchantInfo merchantInfo,
								   String selectedChannelCode,
								   String merchantId,
								   String merchantWithdrawNo,
								   BigDecimal amt, //提交的订单充值金额 元
								   String clientIp,
								   String extraParam,
								   String sysNotifyUrl,
								   String merchantChannelCode,
								   String coinType) throws Exception;



}
