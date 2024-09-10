package com.bootpay.mng.service;

import com.bootpay.common.excetion.TranException;
import com.bootpay.core.entity.PayMerchantChannelSite;
import com.bootpay.core.entity.PayMerchantInfo;
import com.bootpay.core.entity.PayWithdrawInfo;
import com.bootpay.core.entity.WithdrawChannelInfo;
import org.apache.commons.httpclient.util.DateParseException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 外包账户管理
 *
 * @author Administrator
 */
public interface PartnerManagerService {

    public void partnerFeeChange(String withdrawNo,
                                   BigDecimal amt,
                                   String merchantId,
                                   String flowType) throws TranException;

    /**
     * 外包手续费充值
     *
     * @param rechargeAmt 充值金额
     * @param merchantId  商户ID
     * @throws TranException
     */
    public void partnerRecharge(BigDecimal rechargeAmt,
                                String merchantId,
                                String remark,
                                String managerName,
                                String flowType) throws TranException;


    /**
     * 外包总跑量充值
     *
     * @param amtTotal   总成功跑量
     *                   *@param withdrawTotal 总成功交易笔数
     * @param merchantId 外包ID
     * @throws TranException
     */
    public Map<String, Object> partnerAmtRecharge(
            String payType,
            int latestWithdrawTotal,
            int latestWithdrawNow,
            int withdrawTotal,
            BigDecimal latestAmtTotal,
            BigDecimal latestAmtNow,
            BigDecimal amtTotal,
            String withdrawNo,
            String merchantId,
            String remark,
            String managerName,
            String endTime) throws TranException, DateParseException;


}
