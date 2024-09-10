package com.bootpay.mng.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.bootpay.common.constants.CodeConst;
import com.bootpay.common.constants.MyRedisConst;
import com.bootpay.common.constants.enums.EnumFlowType;
import com.bootpay.common.constants.enums.EnumMerchantStatusType;
import com.bootpay.common.constants.enums.EnumRequestType;
import com.bootpay.common.constants.enums.EnumWithdrawStatus;
import com.bootpay.common.excetion.TranException;
import com.bootpay.common.framework.RedisUtil;
import com.bootpay.common.utils.BigDecimalUtils;
import com.bootpay.common.utils.DateUtil;
import com.bootpay.common.utils.MySeq;
import com.bootpay.core.entity.*;
import com.bootpay.core.service.*;
import com.bootpay.mng.amqueue.AMProducerUtils;
import com.bootpay.mng.service.MerchantAccountManagerService;
import com.bootpay.mng.service.PartnerManagerService;
import org.apache.commons.httpclient.util.DateParseException;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PartnerManagerServiceImpl implements PartnerManagerService {

    private static Logger _log = LoggerFactory.getLogger(PartnerManagerServiceImpl.class);

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private IPayMerchantChannelSiteService iPayMerchantChannelSiteService; //商家通道
    @Autowired
    private IPayAgentChannelSiteService iPayAgentChannelSiteService; //代理通道

    @Autowired
    private IPayPartnerBalanceService iPayPartnerBalanceService;
    @Autowired
    private IPayPlatformIncomeInfoService incomeInfoService;
    @Autowired
    private IPayMerchantFlowInfoService flowInfoService;
    @Autowired
    private IPayMerchantInfoService infoService;
    @Autowired
    private IWithdrawChannelInfoService channelInfoService;
    @Autowired
    private IPayWithdrawInfoService iPayWithdrawInfoService;
    @Autowired
    private IPubCheckBankService checkBankService;
    @Autowired
    private IWithdrawChannelInfoService iWithdrawChannelInfoService;
    @Autowired
    private AMProducerUtils amProducerUtils;

    @Autowired
    private IPayMerchantInfoService iPayMerchantInfoService; //商家表==>PayMerchantInfo

    @Autowired
    private IPayMerchantMoneyChangeService iPayMerchantMoneyChangeService; //商户账变明细

    @Autowired
    private IPayPartnerMoneyChangeService iPayPartnerMoneyChangeService; //外包余额账变明细

    @Autowired
    private IPayPartnerAmtMoneyChangeService iPayPartnerAmtMoneyChangeService;//跑量账变SERVICE

    @Autowired
    private ICardPayerInfoService iCardPayerInfoService; //持卡人service

    @Autowired
    private ICardChannelInfoService iCardChannelInfoService; //卡通道

    @Override
    public void partnerFeeChange(String withdrawNo,BigDecimal amt,String merchantId,String flowType) throws TranException {
        //插入支付表
        String depositSeqNumber = MySeq.getWithdrawNo();
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("merchantId", merchantId); //商家自己
        PayPartnerMoneyChange latestRecord = iPayPartnerMoneyChangeService.selectLatestMoneyChangeForUpdate(params);
        if (latestRecord == null) {

            PayPartnerMoneyChange moneyChange = new PayPartnerMoneyChange();
            moneyChange.setMerchantId(merchantId); //某一个外包
            moneyChange.setWithdrawNo(withdrawNo);
            moneyChange.setFlowType(flowType); //充值类型 EnumFlowType.RECHARGE.getName()
            moneyChange.setAmt(amt); // 提交充值金额
            moneyChange.setAmtBefore(new BigDecimal(0));
            moneyChange.setAmtNow(amt);
            moneyChange.setCreateTime(new Date());
            moneyChange.setRemark("充值");
            boolean isAddMoneyChangeEntity = iPayPartnerMoneyChangeService.save(moneyChange);
            if (!isAddMoneyChangeEntity) {
                throw new TranException(CodeConst.SYSTEM_ERROR_CODE, "充值失败:插入流水表失败");
            }

        } else {

            //设为成功 扣除手续费
            BigDecimal amtNow = new BigDecimal(0);
            String remark = "";
            if (flowType.equals(CodeConst.PartnerFlowType.DEDUCTED)) {
                amtNow = latestRecord.getAmtNow().add(amt.negate());
                remark = "扣减";
            }else if (flowType.equals(CodeConst.PartnerFlowType.RECHARGE)) {
                amtNow = latestRecord.getAmtNow().add(amt);
                remark = "充值";
            }
            //3.修改记录MONEY_CHANGE 下发流水
            PayPartnerMoneyChange moneyChangeEntity = new PayPartnerMoneyChange();
            moneyChangeEntity.setMerchantId(merchantId);
            moneyChangeEntity.setFlowType(flowType); // EnumFlowType.RECHARGE.getName() 充值状态
            moneyChangeEntity.setWithdrawNo(withdrawNo);
            moneyChangeEntity.setAmt(amt); //提交充值金额
            moneyChangeEntity.setAmtBefore(latestRecord.getAmtNow());
            moneyChangeEntity.setAmtNow(amtNow);
            moneyChangeEntity.setCreateTime(new Date());
            moneyChangeEntity.setRemark(remark);
            iPayPartnerMoneyChangeService.save(moneyChangeEntity);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class) //充值回滚
    @SuppressWarnings({"deprecation"})
    public void partnerRecharge(BigDecimal rechargeAmt,
                                String merchantId,
                                String remark,
                                String managerName,
                                String flowType) throws TranException {
        //1.修改商户余额
        Integer index = iPayPartnerBalanceService.alterMerchantBalance(merchantId, rechargeAmt, null);
        if (index != 1) {
            throw new TranException(CodeConst.SYSTEM_ERROR_CODE, "充值失败:外包余额修改失败");
        }

        //2.更新余额账变明细
        String depositSeqNumber = MySeq.getWithdrawNo();
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("merchantId", merchantId); //商家自己
        PayPartnerMoneyChange latestRecord = iPayPartnerMoneyChangeService.selectLatestMoneyChangeForUpdate(params);

        //外包手续费 没有流水 要新插入
        if (latestRecord == null) {

            PayPartnerMoneyChange moneyChange = new PayPartnerMoneyChange();
            moneyChange.setMerchantId(merchantId); //某一个外包
            moneyChange.setWithdrawNo(depositSeqNumber);
            moneyChange.setFlowType(flowType); //充值类型 EnumFlowType.RECHARGE.getName()
            moneyChange.setAmt(rechargeAmt); // 提交充值金额
            moneyChange.setAmtBefore(new BigDecimal(0));
            moneyChange.setAmtNow(rechargeAmt);
            moneyChange.setCreateTime(new Date());
            moneyChange.setRemark(StringUtils.isBlank(remark) ? managerName : remark + "[" + managerName + "]");
            boolean isAddMoneyChangeEntity = iPayPartnerMoneyChangeService.save(moneyChange);
            if (!isAddMoneyChangeEntity) {
                throw new TranException(CodeConst.SYSTEM_ERROR_CODE, "充值失败:插入流水表失败");
            }

        } else {

            _log.info("合作伙伴外包充值流水记录===>>>外包:{},账变前:{},账变后:{}", merchantId, latestRecord.getAmtNow(), latestRecord.getAmtNow().add(rechargeAmt));

            //3.修改记录MONEY_CHANGE 下发流水
            PayPartnerMoneyChange moneyChangeEntity = new PayPartnerMoneyChange();
            moneyChangeEntity.setMerchantId(merchantId);
            moneyChangeEntity.setFlowType(flowType); // EnumFlowType.RECHARGE.getName() 充值状态
            moneyChangeEntity.setWithdrawNo(depositSeqNumber);
            moneyChangeEntity.setAmt(rechargeAmt); //提交充值金额
            moneyChangeEntity.setAmtBefore(latestRecord.getAmtNow());
            moneyChangeEntity.setAmtNow(latestRecord.getAmtNow().add(rechargeAmt));
            moneyChangeEntity.setCreateTime(new Date());
            moneyChangeEntity.setRemark(StringUtils.isBlank(remark) ? managerName : remark + "[" + managerName + "]");

            boolean isSaveMoneyChange = iPayPartnerMoneyChangeService.save(moneyChangeEntity);

            if (!isSaveMoneyChange) {
                throw new TranException(CodeConst.SYSTEM_ERROR_CODE, "充值给外包[" + merchantId + "],金额[" + rechargeAmt + "]失败");
            }

        }

    }


    /**
     * 外包跑量变动流水
     *
     * */

    @Override
    @SuppressWarnings({"deprecation"})
    @Transactional(rollbackFor = Exception.class)
    public Map<String,Object> partnerAmtRecharge(
            String payType,
            int latestWithdrawTotal,
            int latestWithdrawNow,
            int withdrawTotal,
            BigDecimal latestAmtTotal, //数据库中原有总跑量
            BigDecimal latestAmtNow, //最后一笔账变后的金额 为下一笔的账变前数据
            BigDecimal amtTotal,//VUE传过来的总金额
            String withdrawNo,
            String merchantId,
            String remark,
            String managerName,
            String endTime) throws TranException {

        String flowType = "0".equals(payType) ? EnumFlowType.WITHDRAW.getName() : EnumFlowType.RECEIVE.getName();
        //2.更新余额账变明细
        if (BigDecimalUtils.equal(latestAmtTotal, new BigDecimal(0.00))) {

            PayPartnerAmtMoneyChange amtMoneyChange = new PayPartnerAmtMoneyChange();

            amtMoneyChange.setPayType(payType);

            amtMoneyChange.setMerchantId(merchantId); //某一个商家
            amtMoneyChange.setWithdrawNo(withdrawNo);
            amtMoneyChange.setFlowType(flowType); //下发

            amtMoneyChange.setWithdrawTotal(withdrawTotal);
            amtMoneyChange.setWithdraw(withdrawTotal);
            amtMoneyChange.setWithdrawBefore(0);
            amtMoneyChange.setWithdrawNow(withdrawTotal);

            amtMoneyChange.setAmtTotal(amtTotal); //总跑量
            amtMoneyChange.setAmt(amtTotal); // 变动金额
            amtMoneyChange.setAmtBefore(new BigDecimal(0));
            amtMoneyChange.setAmtNow(amtTotal);

            amtMoneyChange.setCreateTime(new Date());
            amtMoneyChange.setRemark(remark);
            boolean isAddMoneyChangeEntity = iPayPartnerAmtMoneyChangeService.save(amtMoneyChange);
            if (!isAddMoneyChangeEntity) {
                _log.error("插入外包:{}流水表失败", merchantId);
                throw new TranException(CodeConst.SYSTEM_ERROR_CODE, "插入外包流水表失败");
            }
            Map<String,Object> amtChangeMap = new HashMap<>();
            amtChangeMap.put("withdrawChangeTotal",withdrawTotal);
            amtChangeMap.put("amtChangeTotal",amtTotal);
            return amtChangeMap;

        } else {

            //条件1.amtTotal > latestAmtTotal 正常更新
            //条件2.amtTotal== latestAmtTotal 没有变化
            //条件3.amtTotal < latestAmtTotal 隔月统计查询;
            BigDecimal amtChange = new BigDecimal(0);
            BigDecimal amtBefore = new BigDecimal(0);
            int withdrawChange = 0; // 交易笔数 变化的
            int withdrawBefore = 0; //交易笔数
            //FIXME:
            withdrawChange = withdrawTotal - latestWithdrawTotal; // 现在总的交易笔数 - 原有的交易笔数
            _log.warn("withdrawChange:{}",withdrawChange);
            amtChange = amtTotal.subtract(latestAmtTotal); //总跑量 - 原有跑量（月初为0）

            if (BigDecimalUtils.lessThan(new BigDecimal(withdrawChange), new BigDecimal(0))) {
                //小于0
                //amtChange = amtTotal;//总跑量 (每月月初的时候，总跑量的值总是很小)
                withdrawChange = 0;

            } else {
                withdrawBefore = latestWithdrawNow;
            }

            if (BigDecimalUtils.lessThan(amtChange, new BigDecimal(0))) {
                //小于0
                //amtChange = amtTotal;//总跑量 (每月月初的时候，总跑量的值总是很小)
                amtChange = new BigDecimal(0.00);

            } else {
                amtBefore = latestAmtNow;
            }


            //3.外包总交易量表
            PayPartnerAmtMoneyChange moneyChangeEntity = new PayPartnerAmtMoneyChange();
            moneyChangeEntity.setPayType(payType);
            moneyChangeEntity.setMerchantId(merchantId);
            moneyChangeEntity.setFlowType(flowType);
            moneyChangeEntity.setWithdrawNo(withdrawNo);

            moneyChangeEntity.setWithdrawTotal(withdrawTotal); //总交易笔数
            moneyChangeEntity.setWithdraw(withdrawChange); //提交变动笔数
            moneyChangeEntity.setWithdrawBefore(withdrawBefore); //笔数变动前
            moneyChangeEntity.setWithdrawNow(withdrawBefore + withdrawChange); //变动后

            moneyChangeEntity.setAmtTotal(amtTotal); //总跑量
            moneyChangeEntity.setAmt(amtChange); //提交变动金额
            moneyChangeEntity.setAmtBefore(amtBefore); //余额账变前
            moneyChangeEntity.setAmtNow(amtBefore.add(amtChange));


            moneyChangeEntity.setCreateTime(new Date());//插入流水表的时间
            moneyChangeEntity.setRemark(remark);
            boolean isSaveMoneyChange = iPayPartnerAmtMoneyChangeService.save(moneyChangeEntity);

            if (!isSaveMoneyChange) {
                throw new TranException(CodeConst.SYSTEM_ERROR_CODE, "外包[" + merchantId + "],变动金额[" + amtChange + "]失败");
            }

            Map<String,Object> amtChangeMap = new HashMap<>();
            amtChangeMap.put("withdrawChangeTotal",withdrawChange);
            amtChangeMap.put("amtChangeTotal",amtChange);

            return amtChangeMap;   //返回 新增的金额 ，如果为负数 则为 0
        }

    }


}
