package com.bootpay.mng.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.bootpay.common.constants.CodeConst;
import com.bootpay.common.constants.MyRedisConst;
import com.bootpay.common.constants.enums.EnumFlowType;
import com.bootpay.common.excetion.TranException;
import com.bootpay.common.framework.RedisUtil;
import com.bootpay.common.utils.*;
import com.bootpay.core.entity.*;
import com.bootpay.core.service.*;
import com.bootpay.mng.service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;

@Service
public class BarcodeManagerServiceImpl implements BarcodeManagerService {

    private static Logger _log = LoggerFactory.getLogger(BarcodeManagerServiceImpl.class);

    @Value("${app.merchantPlatformId}") //外包ID
    private String merchantPlatformId;

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
    private IPayAgentBalanceService iPayAgentBalanceService; //商户流水明细

    @Autowired
    private IPayAgentBalanceFlowService iPayAgentBalanceFlowService;

    @Autowired
    private IAgentProfitBalanceService iAgentProfitBalanceService; //码商分润


    @Autowired
    private IAgentProfitFlowService iAgentProfitFlowService; //码商分润明细表

    @Autowired
    private ICoinAlipayInfoService iCoinAlipayInfoService; //币址

    @Autowired
    private IPayAgentChannelSiteService iPayAgentChannelSiteService; //代理通道

    @Autowired
    private MerchantAccountManagerService merchantAccountManagerService;

    @Autowired
    private NotifyService notifyService; //通知商家服务

    @Autowired
    private IPayMerchantMoneyChangeService iPayMerchantMoneyChangeService; //商户流水明细

    @Autowired
    private CoinChannelManagerService coinChannelManagerService; //币址

    @Autowired
    private IPayMerchantChannelSiteService iPayMerchantChannelSiteService; //通知商家服务

    @Autowired
    private BarcodeManagerService barcodeManagerService;

    @Autowired
    private IPayPartnerBalanceService iPayPartnerBalanceService;

    @Autowired
    private PartnerManagerService partnerManagerService;


    @Override
    public ReturnMsg barcodeSuccess(PayMerchantInfo merchantInfo,String withdrawNo,String outTradeNo, String totalPayedAmount, String tradeNo, String tradeStatus, CoinHolderDepositInfo holderDepositInfo, String alipayUid, String businessType,String autoSuccessType) throws Exception {

        Date taskEndTime = new Date();
        //1.修改订单状态为 失败
        CoinHolderDepositInfo depositUpdEntity = new CoinHolderDepositInfo();
        depositUpdEntity.setPreStatus(CodeConst.PreStatusConst.WITHDRAW_SUCCESS);//收到款项
        depositUpdEntity.setStatus(CodeConst.WithdrawStatusConst.WITHDRAW_SUCCESS); //更新为成功
//        updEntity.setOutTradeNo(outTradeNo);//代付订单编号
        depositUpdEntity.setChannelWithdrawNo(tradeNo);//支付宝交易号
        depositUpdEntity.setTaskEndTime(taskEndTime);
        if (CodeConst.AutoSuccessType.AUTO.equals(autoSuccessType)) {
            depositUpdEntity.setRemark(CodeConst.BusinessType.BUSINESS.equals(businessType) ? "商家码自动收款成功" : "个人码自动收款成功");
           boolean isDepositUpdEntity =  iCoinHolderDepositInfoService.update(depositUpdEntity, new UpdateWrapper<CoinHolderDepositInfo>().lambda()
                    .eq(CoinHolderDepositInfo::getWithdrawNo,withdrawNo)
            );
            if (!isDepositUpdEntity){
                _log.error("代收订单:{}更新失败",holderDepositInfo.getWithdrawNo());
                throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "订单更新失败");
            }
        }else {
            //手动设为成功
            depositUpdEntity.setRemark("["+merchantInfo.getMerchantName()+"]设为成功");
            boolean isDepositUpdEntity =   iCoinHolderDepositInfoService.update(depositUpdEntity,new UpdateWrapper<CoinHolderDepositInfo>().lambda()
                    .eq(CoinHolderDepositInfo::getWithdrawNo, withdrawNo)); //保存更改
            if (!isDepositUpdEntity){
                _log.error("代收订单:{}更新失败",withdrawNo);
                throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "订单更新失败");
            }
        }

        if (holderDepositInfo.getPayerMerchantId() != null) {
            //2.订单成功 》 扣除码商的收款额度
            iPayAgentBalanceService.alterAgentBalance(holderDepositInfo.getPayerMerchantId(), holderDepositInfo.getAmt().negate(), null);
            Map<String, Object> params = new HashMap<String, Object>();
            params.put("merchantId", holderDepositInfo.getPayerMerchantId());
            PayAgentBalanceFlow latestRecord = iPayAgentBalanceFlowService.selectLatestBalanceChangeForUpdate(params);
            //3.更新码商流水 没有流水 要新插入
            if (latestRecord == null) {
                BigDecimal beforeAmt = new BigDecimal(0);
                PayAgentBalance payAgentBalanceInfo = iPayAgentBalanceService.getOne(new QueryWrapper<PayAgentBalance>().lambda().eq(PayAgentBalance::getMerchantId, holderDepositInfo.getPayerMerchantId()));
                if (payAgentBalanceInfo != null){
                    beforeAmt = payAgentBalanceInfo.getAccountBalance();
                }

                PayAgentBalanceFlow addMoneyChangeEntity = new PayAgentBalanceFlow();
                addMoneyChangeEntity.setWithdrawNo(holderDepositInfo.getWithdrawNo());
                addMoneyChangeEntity.setMerchantId(holderDepositInfo.getPayerMerchantId()); //某一个代理
                addMoneyChangeEntity.setAmt(holderDepositInfo.getAmt()); // 提交代付金额
                addMoneyChangeEntity.setAmtBefore(beforeAmt);
                addMoneyChangeEntity.setAmtNow(beforeAmt.add(holderDepositInfo.getAmt().negate()));
                addMoneyChangeEntity.setRemark(holderDepositInfo.getWithdrawNo() + "扣除上码额度");
                addMoneyChangeEntity.setCreateTime(new Date());
                iPayAgentBalanceFlowService.save(addMoneyChangeEntity);
            }else {

                PayAgentBalanceFlow moneyChangeEntity = new PayAgentBalanceFlow();
                moneyChangeEntity.setMerchantId(holderDepositInfo.getPayerMerchantId());
                moneyChangeEntity.setWithdrawNo(holderDepositInfo.getWithdrawNo());
                moneyChangeEntity.setAmt(holderDepositInfo.getAmt()); //资金变动
                moneyChangeEntity.setAmtBefore(latestRecord.getAmtNow());
                moneyChangeEntity.setAmtNow(latestRecord.getAmtNow().add(holderDepositInfo.getAmt().negate()));
                moneyChangeEntity.setCreateTime(new Date());
                moneyChangeEntity.setRemark(holderDepositInfo.getWithdrawNo() + "扣除上码额度");
                iPayAgentBalanceFlowService.save(moneyChangeEntity);
            }

            //3.订单成功 》 码商分润 + 码商分润余额新增
            PayMerchantInfo payMerchantInfo = iPayMerchantInfoService.getOne(new QueryWrapper<PayMerchantInfo>().lambda().eq(PayMerchantInfo::getMerchantId, holderDepositInfo.getPayerMerchantId()));
            BigDecimal profitAmt = holderDepositInfo.getAmt().divide(new BigDecimal(100)).multiply(payMerchantInfo.getAgentProfitRate()).setScale(2, BigDecimal.ROUND_HALF_UP);
            iAgentProfitBalanceService.alterAgentProfitBalance(holderDepositInfo.getPayerMerchantId(),profitAmt , null);

            Map<String, Object> paramsFlow = new HashMap<String, Object>();
            paramsFlow.put("merchantId", holderDepositInfo.getPayerMerchantId());
            AgentProfitFlow latestFlowRecord = iAgentProfitFlowService.selectLatestProfitChangeForUpdate(paramsFlow);

            AgentProfitFlow profitFlow = new AgentProfitFlow();
            profitFlow.setWithdrawNo(holderDepositInfo.getWithdrawNo());
            profitFlow.setMerchantId(holderDepositInfo.getMerchantId()); //某一个代理
            profitFlow.setAmt(profitAmt); // 提交代付金额
            profitFlow.setAmtBefore(latestFlowRecord.getAmtNow());
            profitFlow.setAmtNow(latestFlowRecord.getAmtNow().add(profitAmt));
            profitFlow.setRemark("金额:" + holderDepositInfo.getAmt() + "费率:"+ payMerchantInfo.getAgentProfitRate() + "%");
            profitFlow.setCreateTime(new Date());
            iAgentProfitFlowService.save(profitFlow);
        }



        //4.更新代收订单列表 PayDepositInfo
        PayDepositInfo payDepositEntity = new PayDepositInfo();
        payDepositEntity.setWithdrawStatus(CodeConst.WithdrawStatusConst.WITHDRAW_SUCCESS);
        payDepositEntity.setEndTime(taskEndTime);
        boolean isUpdDeposit = iPayDepositInfoService.update(payDepositEntity,new UpdateWrapper<PayDepositInfo>().lambda()
                .eq(PayDepositInfo::getWithdrawNo, holderDepositInfo.getWithdrawNo()));
        if (!isUpdDeposit){
            _log.error("代收订单:{}更新失败",holderDepositInfo.getWithdrawNo());
            throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "订单更新失败");

        }

        //3.钱包更改余额+流水表
        //String channelWithdrawNo = "C" + DateUtil.format(new Date(), DateUtil.FULL_TIME_FORMAT_EN) + "0" + System.currentTimeMillis();
        String remark = "收款码[" + holderDepositInfo.getPayerAddr() + "]收款成功,所属通道[" + holderDepositInfo.getChannelCode() + "]";

        CoinHolderInfo lastCoinHolderInfo = iCoinHolderInfoService.getOne(new QueryWrapper<CoinHolderInfo>().lambda().eq(CoinHolderInfo::getPayerAddr,holderDepositInfo.getPayerAddr()));
        if (lastCoinHolderInfo != null) {
            String flowType = EnumFlowType.RECEIVE.getName(); //自动收款 则为代收的订单 非补单
            coinWithdrawManagerService.alterCoinHolderBalanceNAddFlowService(lastCoinHolderInfo,holderDepositInfo, holderDepositInfo.getAmt(),holderDepositInfo.getAmtPay(), flowType, holderDepositInfo.getWithdrawNo(),tradeNo,remark,"");
            //新增码自动下架功能 （个人固码就不需要下架）
            if (lastCoinHolderInfo.getBusinessType().equals(CodeConst.BusinessType.BUSINESS)) {
                CoinHolderInfo holderEntity = new CoinHolderInfo();
                holderEntity.setStatus(CodeConst.PayerCardStatusConst.CARD_STATUS_DISABLE);//下架码 ||
                holderEntity.setPayerCodeTimes(0);//设为成功次数为0
                boolean isUpdateCoinHolder = iCoinHolderInfoService.update(holderEntity, new QueryWrapper<CoinHolderInfo>().lambda().eq(CoinHolderInfo::getPayerAddr, holderDepositInfo.getPayerAddr()));
                if (!isUpdateCoinHolder){
                    _log.error("收款码:{}自动下架更新失败",holderDepositInfo.getPayerAddr());
                    throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "收款码自动下架更新失败");
                }
            }else {
                /**
                 * TODO:加上风控功能、码商可以设置该码收款几次，收款时间，该码总收款上限金额
                 * 收款区间总后台可以单独设置分配该码收款时间、收款金额区间、根据码商押金额设上限超上限不分类订单
                 * */
                //扣除成功收款次数
                Integer successCodeTimes =  iCoinHolderInfoService.alterCoinHolderCodeTimes(lastCoinHolderInfo.getPayerAddr(),1);
                if (successCodeTimes != 1) {
                    throw new TranException(CodeConst.BUSINESS_ERROR_CODE,"扣除成功收款次数更新有误");
                }
            }
        }

        //4.商家余额+流水表 || TODO:一条通道有多个商家 不再适用用通道查询对应的商户 否则会上错分
        PayMerchantInfo curMerchantInfo = iPayMerchantInfoService.getOne(new QueryWrapper<PayMerchantInfo>().lambda().eq(PayMerchantInfo::getMerchantId,holderDepositInfo.getMerchantId()));
        if (curMerchantInfo != null) {
            String merchantRemark = "[" + curMerchantInfo.getMerchantName() + "]收款[" + holderDepositInfo.getAmtCredit() + "]";
            merchantAccountManagerService.merchantRecharge(
                    holderDepositInfo.getAmt(),
                    holderDepositInfo.getAmtCredit(),
                    curMerchantInfo.getMerchantId(),
                    curMerchantInfo.getMerchantName(),
                    merchantRemark,
                    curMerchantInfo.getMerchantName(),
                    curMerchantInfo.getLabel(),
                    holderDepositInfo.getChannelCode(),
                    holderDepositInfo.getWithdrawNo(),
                    holderDepositInfo.getWithdrawNo(),
                    EnumFlowType.RECEIVE.getName());
        }

        //5.代理分润+流水表 || 找出当前商家信息的上级代理 （包话 代理，总代）
        PayMerchantInfo currentMerchantInfo = iPayMerchantInfoService.getOne(new QueryWrapper<PayMerchantInfo>().lambda().eq(PayMerchantInfo::getMerchantId, holderDepositInfo.getMerchantId()));
        BigDecimal agentSingleProfit = new BigDecimal(0);  //初始代理单笔分成 为 0元
        BigDecimal agentRateProfit = new BigDecimal(0);    //初始代理下发分成 为 0元

        //递归新增代理的单笔总分成
        alterAgentSingleBalance(agentSingleProfit, currentMerchantInfo, holderDepositInfo);
        //递归新增单笔费率总分成
        alterAgentRateBalance(agentRateProfit, currentMerchantInfo, holderDepositInfo);

        //6.记录代收商家手续费及手续费流水
        coinChannelManagerService.alterCoinChannelBalanceService(
                currentMerchantInfo.getMerchantId(),
                holderDepositInfo.getPayerRateFee().add(holderDepositInfo.getPayerSingleFee()).negate(), //代收 扣除手续费
                CodeConst.CardFlowTypeConst.DEPOSIT,
                holderDepositInfo.getWithdrawNo(),
                tradeNo,
                holderDepositInfo.getChannelCode(),
                holderDepositInfo.getMerchantId(),//此处自动收款 为商家的编号ID
                holderDepositInfo.getChannelName(),
                remark
        );

        //7.记录订单成功的缓存 （用于前台APP监控用）
        int timeTTL = 60 * 10;  //10*60; 订单 trx 10分钟失效 | eth 20分钟
        //平台订号号:钱包地址:金额
        String redis_success = holderDepositInfo.getWithdrawNo() + MyRedisConst.SPLICE + "success";
        redisUtil.set(redis_success,redis_success,timeTTL);

        //8.清空收款码的缓存
        String redis_payerAddr_amt = holderDepositInfo.getPayerAddr() + MyRedisConst.SPLICE + holderDepositInfo.getAmtPay();
        if (redisUtil.get(redis_payerAddr_amt) != null) {
            redisUtil.del(redis_payerAddr_amt);
        }

        //9.扣除手续费(人民币)
        PayPartnerBalance payPartnerBalance = iPayPartnerBalanceService.getOne(new QueryWrapper<PayPartnerBalance>().lambda().eq(PayPartnerBalance::getMerchantId, merchantPlatformId));
        if(payPartnerBalance != null) {
            BigDecimal amtRate = payPartnerBalance.getAccountRate();
            BigDecimal amtFee =  holderDepositInfo.getAmt().multiply(amtRate);
            iPayPartnerBalanceService.alterAccountFee(merchantPlatformId, amtFee.negate(), null);

            partnerManagerService.partnerFeeChange(holderDepositInfo.getWithdrawNo(),amtFee,merchantPlatformId,CodeConst.PartnerFlowType.DEDUCTED);
        }

        //10.通知下游商家订单已支付成功 (查询最新订单状态)
        PayDepositInfo payDepositInfo = iPayDepositInfoService.getOne(new QueryWrapper<PayDepositInfo>().lambda().eq(PayDepositInfo::getWithdrawNo, holderDepositInfo.getWithdrawNo()));
        notifyService.notifyMerchantDeposit(payDepositInfo, payDepositInfo.getWithdrawStatus());

        return new ReturnMsg();
    }

    /**
     * 修改代理商的余额
     * 记录代理商的流水
     *  商户 1元 代理 0.7 总代 0.6 == 单笔
     */
    @SuppressWarnings({"deprecation"})
    @Transactional(rollbackFor = Exception.class)
    public BigDecimal alterAgentSingleBalance(BigDecimal agentsProfit, PayMerchantInfo curMerchantInfo, CoinHolderDepositInfo holderDepositInfo) throws TranException {

        //当前商户汇率
        PayMerchantChannelSite curMerchantChannelSite = iPayMerchantChannelSiteService.getOne(
                new QueryWrapper<PayMerchantChannelSite>().lambda()
                        .eq(PayMerchantChannelSite::getMerchantId, curMerchantInfo.getMerchantId())
                        .eq(PayMerchantChannelSite::getChannelCode, holderDepositInfo.getChannelCode())
                        .eq(PayMerchantChannelSite::getPayType,CodeConst.PayType.RECEIVE_IN)
        );
        //上级商户
        PayMerchantInfo parentMerchantInfo = iPayMerchantInfoService.getOne(new QueryWrapper<PayMerchantInfo>().lambda().eq(PayMerchantInfo::getMerchantId, curMerchantInfo.getParentId()));
        //找到上级代理
        if (parentMerchantInfo != null) {
            //上级代理通道信息
            PayMerchantChannelSite parentChannelSite = iPayMerchantChannelSiteService.getOne(new QueryWrapper<PayMerchantChannelSite>().lambda()
                    .eq(PayMerchantChannelSite::getMerchantId, parentMerchantInfo.getMerchantId())
                    .eq(PayMerchantChannelSite::getChannelCode,holderDepositInfo.getChannelCode())
                    .eq(PayMerchantChannelSite::getPayType,CodeConst.PayType.RECEIVE_IN)
            );
            //上级代理商未分配代收通道 以防出错 直接返回 0
            if (parentChannelSite == null){ return new BigDecimal(0); }
            //代理手续费 = 商户成本 - 代理成本
            _log.info("商户单笔代收手续费:{},代理成本:{}", curMerchantChannelSite.getSinglePoundage(), parentChannelSite.getSinglePoundage());
            BigDecimal parentPayProfit = curMerchantChannelSite.getSinglePoundage().subtract(parentChannelSite.getSinglePoundage());

            Integer parentIndex = iPayMerchantBalanceService.alterMerchantBalance(parentMerchantInfo.getMerchantId(), parentPayProfit, null);
            if (parentIndex != 1) throw new TranException(CodeConst.SYSTEM_ERROR_CODE, "分配代理商利润失败");
            _log.info("===>>>代理:{},单笔分成:{}", parentMerchantInfo.getMerchantId(), parentPayProfit);

            //继续查询是否有代理的上级代理 即总代
            //FIXME:充值bug修复 若money_change变中没有上级代理,则查找上级代理的余额为 null,会导致 插入代理商分润流水失败
            //以防死锁 selectLatestMoneyChange
            Map<String, Object> params = new HashMap<String, Object>();
            params.put("merchantId", parentMerchantInfo.getMerchantId()); //某一个代理
            PayMerchantMoneyChange latestRecord = iPayMerchantMoneyChangeService.selectLatestMoneyChangeForUpdate(params);

            //没有流水 要新插入
            if (latestRecord == null) {

                PayMerchantMoneyChange addMoneyChangeEntity = new PayMerchantMoneyChange();
                addMoneyChangeEntity.setMerchantId(parentMerchantInfo.getMerchantId()); //某一个代理
                addMoneyChangeEntity.setPayType(CodeConst.PayType.RECEIVE_IN);
                addMoneyChangeEntity.setFlowType(EnumFlowType.RECEIVE.getName()); //代收类型 不只是充值
                addMoneyChangeEntity.setMerchantWithdrawNo("");//设为空先
                addMoneyChangeEntity.setWithdrawNo(holderDepositInfo.getWithdrawNo());
                addMoneyChangeEntity.setAmt(parentPayProfit); // 提交代付金额
                addMoneyChangeEntity.setCreditAmt(parentPayProfit); //实际入账金额
                addMoneyChangeEntity.setAmtBefore(new BigDecimal(0));
                addMoneyChangeEntity.setAmtNow(parentPayProfit);

                //addMoneyChangeEntity.setCreateTime(new Date());
                addMoneyChangeEntity.setRemark("代理[" + parentMerchantInfo.getMerchantId() + "]单笔代收分成["+parentPayProfit+"]");
                addMoneyChangeEntity.setChannelName(parentChannelSite.getChannelName());
                addMoneyChangeEntity.setChannelCode(parentChannelSite.getChannelCode());

                boolean isAddMoneyChangeEntity = iPayMerchantMoneyChangeService.save(addMoneyChangeEntity);

                if (!isAddMoneyChangeEntity) {
                    _log.error("代收失败:插入代理商:{}分润流水失败",parentMerchantInfo.getMerchantId());
                    throw new TranException(CodeConst.SYSTEM_ERROR_CODE, "代收失败:插入代理商分润流水失败");
                }
            } else {

                //更新时间
                _log.info("代理代收单笔流水记录===>>>商家:{},账变前:{},账变后:{}", parentMerchantInfo.getMerchantId(), latestRecord.getAmtNow(), latestRecord.getAmtNow().add(parentPayProfit));

                //3.记录MONEY_CHANGE 充值流水
                PayMerchantMoneyChange moneyChangeEntity = new PayMerchantMoneyChange();
                moneyChangeEntity.setMerchantId(parentMerchantInfo.getMerchantId());
                moneyChangeEntity.setPayType(CodeConst.PayType.RECEIVE_IN);
                moneyChangeEntity.setFlowType(EnumFlowType.RECEIVE.getName()); //充值状态
                moneyChangeEntity.setMerchantWithdrawNo("");
                moneyChangeEntity.setWithdrawNo(holderDepositInfo.getWithdrawNo());
                moneyChangeEntity.setAmt(parentPayProfit); //资金变动
                moneyChangeEntity.setCreditAmt(parentPayProfit); //实际入账金额
                moneyChangeEntity.setAmtBefore(latestRecord.getAmtNow());
                moneyChangeEntity.setAmtNow(latestRecord.getAmtNow().add(parentPayProfit));
                //moneyChangeEntity.setCreateTime(new Date());
                moneyChangeEntity.setRemark("代理[" + parentMerchantInfo.getMerchantId() + "]单笔代收分成[" + parentPayProfit + "]");
                moneyChangeEntity.setChannelName(parentChannelSite.getChannelName());
                moneyChangeEntity.setChannelCode(holderDepositInfo.getChannelCode());
                boolean isSaveMoneyChange = iPayMerchantMoneyChangeService.save(moneyChangeEntity);
                if (!isSaveMoneyChange) {
                    _log.info("=====>>>>>代理商充值/代收余额变动明细表:{},代收流水表:{}失败", parentMerchantInfo.getMerchantId(), parentPayProfit);
                    throw new TranException(CodeConst.SYSTEM_ERROR_CODE, "代收分成给代理商[" + parentMerchantInfo.getMerchantId() + "],金额[" + parentPayProfit + "]失败");
                }
            }
            agentsProfit = agentsProfit.add(parentPayProfit);
            return alterAgentSingleBalance(agentsProfit, parentMerchantInfo, holderDepositInfo); //递归
        }
        return agentsProfit;
    }


    @SuppressWarnings({"deprecation"})
    @Transactional(rollbackFor = Exception.class)
    public BigDecimal alterAgentRateBalance(BigDecimal agentsProfit, PayMerchantInfo curMerchantInfo, CoinHolderDepositInfo holderDepositInfo) throws TranException {

        //当前商户汇率
        PayMerchantChannelSite curMerchantChannelSite = iPayMerchantChannelSiteService.getOne(
                new QueryWrapper<PayMerchantChannelSite>().lambda()
                        .eq(PayMerchantChannelSite::getMerchantId, curMerchantInfo.getMerchantId())
                        .eq(PayMerchantChannelSite::getChannelCode, holderDepositInfo.getChannelCode())
                        .eq(PayMerchantChannelSite::getPayType,CodeConst.PayType.RECEIVE_IN)
        );
        //上级商户
        PayMerchantInfo parentMerchantInfo = iPayMerchantInfoService.getOne(new QueryWrapper<PayMerchantInfo>().lambda().eq(PayMerchantInfo::getMerchantId, curMerchantInfo.getParentId()));
        //找到上级代理
        if (parentMerchantInfo != null) {

            //上级代理通道信息
            PayMerchantChannelSite parentChannelSite = iPayMerchantChannelSiteService.getOne(new QueryWrapper<PayMerchantChannelSite>().lambda()
                    .eq(PayMerchantChannelSite::getMerchantId, parentMerchantInfo.getMerchantId())
                    .eq(PayMerchantChannelSite::getChannelCode, holderDepositInfo.getChannelCode())
                    .eq(PayMerchantChannelSite::getPayType,CodeConst.PayType.RECEIVE_IN));

            //上级代理商未分配代收通道 以防出错 直接返回 0
            if (parentChannelSite == null){ return new BigDecimal(0); }

            //代理手续费 = （商户成本 - 代理成本） * 下发金额
            _log.info("商户代收手续费率 * 代收金额:{},代理成本:{}", curMerchantChannelSite.getMerchantRate(), parentChannelSite.getMerchantRate());
            BigDecimal parentPayProfitRate = new BigDecimal(curMerchantChannelSite.getMerchantRate()).subtract(new BigDecimal(parentChannelSite.getMerchantRate()));

            BigDecimal parentPayProfit = parentPayProfitRate.divide(new BigDecimal("100")).multiply(holderDepositInfo.getAmt()).setScale(4, BigDecimal.ROUND_HALF_UP);

            Integer parentIndex = iPayMerchantBalanceService.alterMerchantBalance(parentMerchantInfo.getMerchantId(), parentPayProfit, null);
            if (parentIndex != 1) throw new TranException(CodeConst.SYSTEM_ERROR_CODE, "分配代理商利润失败");
            _log.info("===>>>代理:{},单笔下发手续费率*下发金额分成:{}", parentMerchantInfo.getMerchantId(), parentPayProfit);


            Map<String, Object> params = new HashMap<String, Object>();
            params.put("merchantId", parentMerchantInfo.getMerchantId());
            PayMerchantMoneyChange latestRecord = iPayMerchantMoneyChangeService.selectLatestMoneyChangeForUpdate(params);
            //没有流水 要新插入
            if (latestRecord == null) {

                PayMerchantMoneyChange addMoneyChangeEntity = new PayMerchantMoneyChange();

                addMoneyChangeEntity.setMerchantId(parentMerchantInfo.getMerchantId()); //某一个代理
                addMoneyChangeEntity.setFlowType(EnumFlowType.RECEIVE.getName()); //充值类型
                addMoneyChangeEntity.setPayType(CodeConst.PayType.RECEIVE_IN);
                addMoneyChangeEntity.setMerchantWithdrawNo("");
                addMoneyChangeEntity.setWithdrawNo(holderDepositInfo.getWithdrawNo());
                addMoneyChangeEntity.setAmt(parentPayProfit); // 提交代付金额
                addMoneyChangeEntity.setCreditAmt(parentPayProfit); //实际入账金额
                addMoneyChangeEntity.setAmtBefore(new BigDecimal(0));
                addMoneyChangeEntity.setAmtNow(parentPayProfit);
                //addMoneyChangeEntity.setCreateTime(new Date());
                addMoneyChangeEntity.setRemark("代理[" + parentMerchantInfo.getMerchantId() + "]代收分成");
                addMoneyChangeEntity.setChannelName(parentChannelSite.getChannelName());
                addMoneyChangeEntity.setChannelCode(parentChannelSite.getChannelCode());

                boolean isAddMoneyChangeEntity = iPayMerchantMoneyChangeService.save(addMoneyChangeEntity);

                if (!isAddMoneyChangeEntity) {
                    _log.error("代收失败:插入代理商:{}分润流水失败",parentMerchantInfo.getMerchantId());
                    throw new TranException(CodeConst.SYSTEM_ERROR_CODE, "代收失败:插入代理商分润流水失败");
                }
            } else {

                //更新时间
                _log.info("代理代收流水记录===>>>商家:{},账变前:{},账变后:{}", parentMerchantInfo.getMerchantId(), latestRecord.getAmtNow(), latestRecord.getAmtNow().add(parentPayProfit));

                //3.记录MONEY_CHANGE 退款流水
                PayMerchantMoneyChange moneyChangeEntity = new PayMerchantMoneyChange();
                moneyChangeEntity.setMerchantId(parentMerchantInfo.getMerchantId());
                moneyChangeEntity.setPayType(CodeConst.PayType.RECEIVE_IN);
                moneyChangeEntity.setFlowType(EnumFlowType.RECEIVE.getName()); //代收
                moneyChangeEntity.setMerchantWithdrawNo("");
                moneyChangeEntity.setWithdrawNo(holderDepositInfo.getWithdrawNo());
                moneyChangeEntity.setAmt(parentPayProfit); //资金变动
                moneyChangeEntity.setCreditAmt(parentPayProfit); //实际入账金额
                moneyChangeEntity.setAmtBefore(latestRecord.getAmtNow());
                moneyChangeEntity.setAmtNow(latestRecord.getAmtNow().add(parentPayProfit));

                //moneyChangeEntity.setCreateTime(new Date());
                moneyChangeEntity.setRemark("代理[" + parentMerchantInfo.getMerchantId() + "]代收分成[" + parentPayProfit + "]");
                moneyChangeEntity.setChannelName(holderDepositInfo.getChannelName());
                moneyChangeEntity.setChannelCode(holderDepositInfo.getChannelCode());
                boolean isSaveMoneyChange = iPayMerchantMoneyChangeService.save(moneyChangeEntity);

                // boolean isUpdateMoneyChange = iPayMerchantMoneyChangeService.update(moneyChangeEntity, new QueryWrapper<PayMerchantMoneyChange>().lambda().in(PayMerchantMoneyChange::getId, moneyLatestChangeEntity.getId()));

                if (!isSaveMoneyChange) {
                    _log.info("=====>>>>>代理商充值/代收余额变动明细表:{},代收流水表:{}失败", parentMerchantInfo.getMerchantId(), parentPayProfit);
                    throw new TranException(CodeConst.SYSTEM_ERROR_CODE, "代收分成给代理商[" + parentMerchantInfo.getMerchantId() + "],金额[" + parentPayProfit + "]失败");
                }
            }
            agentsProfit = agentsProfit.add(parentPayProfit);
            return alterAgentRateBalance(agentsProfit, parentMerchantInfo, holderDepositInfo);
        }
        return agentsProfit;
    }


}
