package com.bootpay.mng.service;

import com.bootpay.common.excetion.TranException;
import com.bootpay.core.entity.PayMerchantChannelSite;
import com.bootpay.core.entity.PayMerchantInfo;
import com.bootpay.core.entity.PayWithdrawInfo;
import com.bootpay.core.entity.WithdrawChannelInfo;

import java.math.BigDecimal;
import java.util.List;

/**
 * 三方卡通道管理 服务
 * @author 
 * @02/25/2020
 *
 */
public interface CardChannelManagerService {

	/**
	 * @三方卡通道充值
	 * @通道手续费充值 ==>
	 * @rechargeAmt(充值)金额 ,
	 * @flowType 流水类型,
	 * @rechargeType 充值类别  通道 | 手续费
	 * @cardWithdrawNo 下挂卡流水号
	 * @channelWithdrawNo 通道流水号
	 * @通道编号 cardChannelCode
	 * @managerName 管理员名称 备注
	 * @channelName 通道名称
	 * @throws TranException
	 */
	public void  alterCardChannelBalanceService(BigDecimal rechargeAmt,
									 String flowType,
									 String rechargeType,
									 String cardWithdrawNo,
									 String channelWithdrawNo,
									 String cardChannelCode,
									 String managerName,String channelName,String remark)throws TranException;

}
