package com.bootpay.mng.service;

import com.bootpay.common.excetion.TranException;
import com.bootpay.core.entity.CardPayerInfo;

import java.math.BigDecimal;

/**
 * 修改银行卡余额 + 流水服务
 * @author 
 * @03/18/2020
 *
 */
public interface CardWithdrawManagerService {

	/**
	 * @CardPayerInfo
	 * 金额
	 * 流水类型
	 * 银行卡流水订单号
	 * 备注
	 * */
	public void  alterCardPayerBalanceNAddFlowService(
			CardPayerInfo cardPayerInfo,
			BigDecimal amt,
			String flowType,
			String cardWithdrawNo,
			String remark)throws TranException;

}
