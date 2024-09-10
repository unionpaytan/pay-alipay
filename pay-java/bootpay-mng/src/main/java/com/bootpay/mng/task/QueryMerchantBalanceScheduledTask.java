package com.bootpay.mng.task;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.bootpay.common.constants.CodeConst;
import com.bootpay.common.constants.enums.EnumAccountType;
import com.bootpay.common.constants.enums.EnumAgentRate;
import com.bootpay.common.excetion.TranException;
import com.bootpay.common.framework.RedisUtil;
import com.bootpay.common.utils.DateUtil;
import com.bootpay.core.entity.PayMerchantBalance;
import com.bootpay.core.entity.PayMerchantInfo;
import com.bootpay.core.entity.PayMerchantMoneyBalance;
import com.bootpay.core.service.*;
import com.bootpay.mng.amqueue.AMProducerUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

/**
 * 定时查询代付订单状态
 *
 * @author Administrator
 */
@Component
public class QueryMerchantBalanceScheduledTask {

    private Logger _log = LoggerFactory.getLogger(QueryMerchantBalanceScheduledTask.class);

    @Autowired
    private RedisUtil redisUtil;
    @Autowired
    private IPayWithdrawInfoService infoService;
    @Autowired
    private IPayPlatformIncomeInfoService incomeInfoService;
    @Autowired
    private AMProducerUtils amProducerUtils;

    @Autowired
    private IPayMerchantInfoService iPayMerchantInfoService; //商家表服务==>PayMerchantInfo

    @Autowired
    private IPayMerchantChannelSiteService iPayMerchantChannelSiteService; //商户通道成本服务

    @Autowired
    private IPayMerchantBalanceService iPayMerchantBalanceService; //商户余额服务

    @Autowired
    private IPayMerchantMoneyBalanceService iPayMerchantMoneyBalanceService; //商户每日统计


    @Scheduled(cron = "5 0 0 * * ?")
    public void scheduled() {
        _log.info("=====>>>>> Java Scheduled 每天凌晨00:00:05执行一次");
        try {
            //找到商户
            List<PayMerchantBalance> merchantInfoList = iPayMerchantBalanceService.list(new QueryWrapper<>());

            for (PayMerchantBalance merchantBalance : merchantInfoList) {

                // 记录商家的每日余额
                PayMerchantInfo merchantInfo = iPayMerchantInfoService.getOne(new QueryWrapper<PayMerchantInfo>().lambda().eq(PayMerchantInfo::getMerchantId, merchantBalance.getMerchantId()));

                if (merchantInfo != null && merchantInfo.getAccountType().equals(EnumAccountType.MERCHANT.getName())) {

                    if (merchantInfo.getAgentRate().equals(EnumAgentRate.MERCHANT.getName()) || merchantInfo.getAgentRate().equals(EnumAgentRate.AGENT_RATE1.getName()) || merchantInfo.getAgentRate().equals(EnumAgentRate.AGENT_RATE2.getName())){
                        PayMerchantMoneyBalance balanceEveydayInfo = new PayMerchantMoneyBalance();
                        balanceEveydayInfo.setMerchantId(merchantBalance.getMerchantId());
                        // balanceInfo.setMerchantName(merchantBalance.g);
                        balanceEveydayInfo.setAmt(merchantBalance.getAccountBalance()); //商户总额
                        //balanceEveydayInfo.setCreateTime(new Date());
                        balanceEveydayInfo.setRemark("记录[" + merchantBalance.getMerchantId() + "][" + DateUtil.format(new Date(), DateUtil.FORMAT_DATE) + "]余额[" + merchantBalance.getAccountBalance() + "]");

                        boolean isBalanceEverydayChange = iPayMerchantMoneyBalanceService.save(balanceEveydayInfo);

                        if (!isBalanceEverydayChange) {
                            throw new TranException(CodeConst.SYSTEM_ERROR_CODE, "记录每日[" + new Date() + "]商家[" + merchantBalance.getMerchantId() + "],余额[" + merchantBalance.getAccountBalance() + "]失败");
                        }else {
                            //16:36:45.668 16:37:11.966 16:37:46.085
                            _log.info("成功记录每日[" + new Date() + "]商家[" + merchantBalance.getMerchantId() + "],余额[" + merchantBalance.getAccountBalance() + "]");
                        }
                    }

                }


            }

        } catch (Exception e) {
            e.printStackTrace();
            _log.error("=====>>>>>Java ScheduledTask 处理异常,{}", e.getMessage());
        }
    }
}
