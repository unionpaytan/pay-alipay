package com.bootpay.mng.service;

import com.bootpay.core.entity.PayDepositInfo;
import com.bootpay.core.entity.PayWithdrawInfo;

/**
 * 通知接口
 * @author Administrator
 *
 */
public interface NotifyService {

	/**
	 * 提现状态通知
	 * @实现这个接口的方法
	 * @param withdrawInfo
	 */
	public void notifyMerchant(PayWithdrawInfo withdrawInfo,String withdrawStatus) throws Exception;

    public void notifyMerchantDeposit(PayDepositInfo depositInfo, String withdrawStatus ) throws Exception;
}
