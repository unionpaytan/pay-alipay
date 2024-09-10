package com.bootpay.mng.service;

import com.bootpay.common.excetion.TranException;
import com.bootpay.common.utils.ReturnMsg;
import com.bootpay.core.entity.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * 二维码收款成功
 * @params payType - 商家周转码 1 || 个人码 0
 * @author Administrator
 *
 */
public interface BarcodeManagerService {

	public ReturnMsg barcodeSuccess(
			                 PayMerchantInfo merchantInfo,
							 String withdrawNo,
							 String outTradeNo,
							 String totalPayedAmount,
							 String tradeNo,
							 String tradeStatus,
							 CoinHolderDepositInfo holderDepositInfo,
							 String alipayUid,
							 String payType,
							 String autoType) throws Exception;
}
