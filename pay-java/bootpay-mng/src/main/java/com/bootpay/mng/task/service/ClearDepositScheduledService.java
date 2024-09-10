package com.bootpay.mng.task.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.bootpay.common.constants.CodeConst;
import com.bootpay.common.constants.MyRedisConst;
import com.bootpay.common.constants.enums.EnumFlowType;
import com.bootpay.common.constants.enums.EnumMerchantStatusType;
import com.bootpay.common.excetion.TranException;
import com.bootpay.common.framework.RedisUtil;
import com.bootpay.common.utils.DateUtil;
import com.bootpay.core.entity.*;
import com.bootpay.core.service.*;
import com.bootpay.mng.service.CoinChannelManagerService;
import com.bootpay.mng.service.CoinWithdrawManagerService;
import com.bootpay.mng.service.MerchantAccountManagerService;
import com.bootpay.mng.service.NotifyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class ClearDepositScheduledService {

    private Logger _log = LoggerFactory.getLogger(ClearDepositScheduledService.class);

    @Autowired
    private ICoinHolderDepositFlowService iCoinHolderDepositFlowService; //COIN FLOW

    @Autowired
    private ICoinHolderInfoService iCoinHolderInfoService;

    @Autowired
    private ICoinPayerInfoService iCoinPayerInfoService;

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private CoinWithdrawManagerService coinWithdrawManagerService;

    @Autowired
    private ICoinChannelDepositInfoService iCoinChannelDepositInfoService;//

    @Autowired
    private ICoinHolderDepositInfoService iCoinHolderDepositInfoService;//收款订单表

    @Autowired
    private ICoinPayerWithdrawFlowService iCoinPayerWithdrawFlowService;

    @Autowired
    private IPayDepositInfoService iPayDepositInfoService;

    @Autowired
    private IPayMerchantInfoService iPayMerchantInfoService;

    @Autowired
    private IPayMerchantBalanceService iPayMerchantBalanceService; //商户余额服务

    @Autowired
    private IPayMerchantMoneyChangeService iPayMerchantMoneyChangeService; //商户流水明细

    @Autowired
    private MerchantAccountManagerService merchantAccountManagerService;

    @Autowired
    private CoinChannelManagerService coinChannelManagerService; //币址

    @Autowired
    private IPayAgentChannelSiteService iPayAgentChannelSiteService; //代理通道

    @Autowired
    private NotifyService notifyService; //通知商家服务

    /**
     * 收款卡
     */
    @Transactional(rollbackFor = Exception.class)
    @SuppressWarnings("deprecation")
    public void clearDeposit(CoinHolderDepositInfo info) throws Exception {

        Date endTime = new Date();
        //先判断订单是否处于未支付状态
        CoinHolderDepositInfo holderDepositInfo = iCoinHolderDepositInfoService.getOne(new QueryWrapper<CoinHolderDepositInfo>().lambda()
                .eq(CoinHolderDepositInfo::getWithdrawNo, info.getWithdrawNo()));
        //订单[排队中]才可修正 + 查询的任务也不可设为失败 || 以防止前端设为成功后刚好时间到期
        if (!holderDepositInfo.getStatus().equals(CodeConst.WithdrawStatusConst.WITHDRAW_UNKNOWN)) {
            throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "订单状态[非待支付],不可更改状态");
        }
        //1.修改订单状态为 失败
        CoinHolderDepositInfo updEntity = new CoinHolderDepositInfo();
        updEntity.setStatus(CodeConst.WithdrawStatusConst.WITHDRAW_NO_CLEAR); //更新为超时
        //updEntity.setChannelWithdrawNo("");//支付超时无上游订单号
        updEntity.setRemark("支付超时");
        updEntity.setTaskEndTime(endTime);
        iCoinHolderDepositInfoService.update(updEntity, new UpdateWrapper<CoinHolderDepositInfo>().lambda()
                .eq(CoinHolderDepositInfo::getWithdrawNo, info.getWithdrawNo()));

        //2.商户订单列表
        PayDepositInfo depositInfoUpd = new PayDepositInfo();
        depositInfoUpd.setWithdrawStatus(CodeConst.WithdrawStatusConst.WITHDRAW_NO_CLEAR); //更新为失败
        depositInfoUpd.setEndTime(endTime);
        depositInfoUpd.setRemark("支付超时");
        iPayDepositInfoService.update(depositInfoUpd, new UpdateWrapper<PayDepositInfo>().lambda()
                .eq(PayDepositInfo::getWithdrawNo, info.getWithdrawNo()));

        //3.更改CoinHolderInfo 数量 不再更改数量
        //iCoinHolderInfoService.alterCoinHolderCodeTimes(info.getPayerAddr(),-1); //--1负负+1

        //4.清空缓存 redis (同个码相同金额订单 可再接码)
        String redis_payerAddr_amt = info.getPayerAddr() + MyRedisConst.SPLICE + info.getAmtPay();
        if (redisUtil.get(redis_payerAddr_amt) != null) {
            redisUtil.del(redis_payerAddr_amt);
        }
        //get one
        PayDepositInfo depositInfo = iPayDepositInfoService.getOne(new QueryWrapper<PayDepositInfo>().lambda().eq(PayDepositInfo::getWithdrawNo, info.getWithdrawNo()));
        //3.通知商户 若商户有提交异步回调地址(可设为必须条件)
        notifyService.notifyMerchantDeposit(depositInfo, depositInfo.getWithdrawStatus());

    }



}
