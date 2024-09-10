package com.bootpay.mng.service;

import com.bootpay.common.excetion.TranException;

import java.math.BigDecimal;

/**
 * 三方卡通道管理 服务
 * @author
 * @02/25/2020
 *
 */
public interface CoinChannelManagerService {
	public void  alterCoinChannelBalanceService(
			                         String merchantId,
			                         BigDecimal rechargeAmt,
									 String flowType,
									 String withdrawNo,
									 String channelWithdrawNo,
									 String channelCode,
									 String managerName,
									 String channelName,
									 String remark)throws TranException;

}
