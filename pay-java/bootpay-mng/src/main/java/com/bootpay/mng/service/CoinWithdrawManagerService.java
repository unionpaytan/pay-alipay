package com.bootpay.mng.service;

import com.bootpay.common.excetion.TranException;
import com.bootpay.core.entity.CardPayerInfo;
import com.bootpay.core.entity.CoinHolderDepositInfo;
import com.bootpay.core.entity.CoinHolderInfo;
import com.bootpay.core.entity.CoinPayerInfo;

import java.math.BigDecimal;

/**
 * 修改钱包余额 + 新增流水服务
 * @author 
 * @07/31/2021
 *
 */
public interface CoinWithdrawManagerService {

	/**
	 * @CardPayerInfo
	 * 金额
	 * 流水类型
	 * 银行卡流水订单号
	 * 备注
	 * */
	public void  alterCoinPayerBalanceNAddFlowService(
			CoinPayerInfo coinPayerInfo,
			BigDecimal amt,
			String flowType,
			String withdrawNo,
			String channelWithdrawNo,
			String remark)throws TranException;

	/**
	 * @CardHolderInfo
	 * 金额
	 * 流水类型
	 * 银行卡流水订单号
	 * 备注
	 * */
	public void  alterCoinHolderBalanceNAddFlowService(
			CoinHolderInfo coinHolderInfo,
			CoinHolderDepositInfo holderDepositInfo,
			BigDecimal amtRmb,
			BigDecimal amt,
			String flowType,
			String withdrawNo,
			String channelWithdrawNo,
			String remark,String refNo)throws TranException;


}
