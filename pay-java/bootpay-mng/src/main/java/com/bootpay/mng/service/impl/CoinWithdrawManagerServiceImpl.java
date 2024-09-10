package com.bootpay.mng.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.bootpay.common.constants.CodeConst;
import com.bootpay.common.constants.enums.EnumAccountType;
import com.bootpay.common.constants.enums.EnumFlowType;
import com.bootpay.common.excetion.TranException;
import com.bootpay.common.utils.DateUtil;
import com.bootpay.common.utils.HtmlUtil;
import com.bootpay.common.utils.ReturnMsg;
import com.bootpay.core.entity.*;
import com.bootpay.core.service.*;
import com.bootpay.mng.service.CardWithdrawManagerService;
import com.bootpay.mng.service.CoinWithdrawManagerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class CoinWithdrawManagerServiceImpl implements CoinWithdrawManagerService {

    private static Logger _log = LoggerFactory.getLogger(CoinWithdrawManagerServiceImpl.class);

    @Autowired
    ICardChannelInfoService iCardChannelInfoService;

    @Autowired
    ICardChannelFlowInfoService iCardChannelFlowInfoService;

    @Autowired
    private ICardPayerInfoService iCardPayerInfoService; //持卡人service

    @Autowired
    private ICoinPayerInfoService iCoinPayerInfoService; //持卡人service


    @Autowired
    private ICardPayerFlowInfoService iCardPayerFlowInfoService; //银行卡流水表

    @Autowired
    private  ICoinPayerWithdrawFlowService iCoinPayerWithdrawFlowService;

    @Autowired
    private IPayMerchantInfoService iPayMerchantInfoService;

    @Autowired
    private ICoinHolderInfoService iCoinHolderInfoService;

    @Autowired
    private ICoinHolderDepositFlowService iCoinHolderDepositFlowService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    @SuppressWarnings({"deprecation"})
    public void alterCoinPayerBalanceNAddFlowService(CoinPayerInfo payerInfo, BigDecimal rechargeAmt, String flowType, String withdrawNo,String channelWithdrawNo, String remark) throws TranException {

        //银行卡充值/余额变动 ｜实际入账金额
        BigDecimal alterAmt = flowType.equals(EnumFlowType.WITHDRAW.getName()) ? rechargeAmt.negate() :rechargeAmt;
        //1.钱包充值修改钱包余额
        Integer balanceIndex = iCoinPayerInfoService.alterCoinPayerBalance(payerInfo.getPayerAddr(), alterAmt, null);
        if (balanceIndex != 1) {
            throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "钱包余额变动失败");
        }

        //2.钱包变动流水
        Map<String, Object> paramsCardPayer = new HashMap<String, Object>();
        paramsCardPayer.put("payerAddr", payerInfo.getPayerAddr());

        CoinPayerWithdrawFlow  latestRecord = iCoinPayerWithdrawFlowService.selectLatestCardPayerFlowForUpdate(paramsCardPayer);

        //没有流水 要新插入
        if (latestRecord == null) {

            //记录充值银行流水
            CoinPayerWithdrawFlow flowInfo = new CoinPayerWithdrawFlow();

            flowInfo.setWithdrawNo(withdrawNo);
            flowInfo.setChannelWithdrawNo(channelWithdrawNo);
            flowInfo.setFlowType(flowType); //自动上分
            flowInfo.setAmt(rechargeAmt);  //银行卡交易总额
            flowInfo.setCreditAmt(alterAmt); //银行卡交易总额 下发时银行卡金额与交易金额一致
            flowInfo.setAmtBefore(new BigDecimal(0));
            flowInfo.setAmtNow(alterAmt);

            flowInfo.setCoinType(payerInfo.getCoinType());
            flowInfo.setMerchantId(payerInfo.getMerchantId());

            flowInfo.setPayerAddr(payerInfo.getPayerAddr());
            flowInfo.setPayerName(payerInfo.getPayerName());

            //flowInfo.setCreateTime(new Date());
            flowInfo.setDate(DateUtil.getDate());
            flowInfo.setRemark(remark);

            flowInfo.setChannelCode(payerInfo.getChannelCode());
            flowInfo.setChannelName(payerInfo.getChannelName());
            flowInfo.setManagerName("");

            boolean isFlowChange = iCoinPayerWithdrawFlowService.save(flowInfo);
            if (!isFlowChange) {
                _log.error("=====>>>>>代付钱包地址:{},{}流水写入失败,回滚", payerInfo.getPayerAddr(), alterAmt);
                throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "代付钱包:订单号[" + channelWithdrawNo + "],USDT金额[" + alterAmt + "]流水写入失败");
            }

        } else {

            //从流水表内选取
            CoinPayerWithdrawFlow flowEntity = new CoinPayerWithdrawFlow();
            flowEntity.setPayerAddr(payerInfo.getPayerAddr()); //哪个钱包
            flowEntity.setAmt(alterAmt); //增加/减少余额

            //1.插入流水表
            Integer latestRecordId = iCoinPayerWithdrawFlowService.selectIdByInsertLatestCardPayerFlow(flowEntity);
            if (latestRecordId != 1) {
                throw new TranException(CodeConst.SYSTEM_ERROR_CODE, "失败:插入钱包流水失败");
            }
            //2.获取刚插入的卡自增ID
            // CardPayerFlowInfo latestRecordCardPayerFlowInfo = iCardPayerFlowInfoService.getById(flowEntity.getId());

            //3.记录CardPayerFlowInfo 银行卡流水
            //如果有冲正流水了 不再重复插入
            CoinPayerWithdrawFlow flowInfoBack = iCoinPayerWithdrawFlowService.getOne(new QueryWrapper<CoinPayerWithdrawFlow>().lambda()
                    .eq(CoinPayerWithdrawFlow::getChannelWithdrawNo,channelWithdrawNo)
                    .eq(CoinPayerWithdrawFlow::getFlowType,EnumFlowType.RECEIVE.getName()));
            if (flowInfoBack != null){
                throw new TranException(CodeConst.SYSTEM_ERROR_CODE, "失败:已有冲正流水,插入银行卡流水失败");
            }

            CoinPayerWithdrawFlow flowInfo = new CoinPayerWithdrawFlow();

            flowInfo.setWithdrawNo(withdrawNo);
            flowInfo.setChannelWithdrawNo(channelWithdrawNo);
            flowInfo.setFlowType(flowType); //自动上分
            flowInfo.setAmt(rechargeAmt);  //银行卡交易总额

            //flowInfo.setAmtBefore(new BigDecimal(0)); //已用selectIdByInsertLatestCardPayerFlow插入
            //flowInfo.setAmtNow(new BigDecimal(amt));

            flowInfo.setCoinType(payerInfo.getCoinType());
            flowInfo.setMerchantId(payerInfo.getMerchantId());

            flowInfo.setPayerAddr(payerInfo.getPayerAddr());
            flowInfo.setPayerName(payerInfo.getPayerName());

            flowInfo.setCreateTime(new Date());
            flowInfo.setDate(DateUtil.getDate());
            flowInfo.setRemark(remark);

            flowInfo.setChannelCode(payerInfo.getChannelCode());
            flowInfo.setChannelName(payerInfo.getChannelName());

            flowInfo.setManagerName("");

            boolean isFlowChange = iCoinPayerWithdrawFlowService.update(flowInfo, new QueryWrapper<CoinPayerWithdrawFlow>()
                    .lambda().eq(CoinPayerWithdrawFlow::getId, flowEntity.getId()));
            if (!isFlowChange) {
                _log.error("=====>>>>>卡号:{},{}流水写入失败,回滚", payerInfo.getPayerAddr(), alterAmt);
                throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "钱包订单[" + withdrawNo + "],PHP金额[" + alterAmt + "]流水写入失败");
            }

        }
    }




    @Override
    @SuppressWarnings({"deprecation"})
    public void alterCoinHolderBalanceNAddFlowService(CoinHolderInfo holderInfo,CoinHolderDepositInfo holderDepositInfo,BigDecimal amt, BigDecimal amtPay, String flowType, String withdrawNo,String channelWithdrawNo, String remark,String refNo) throws TranException {

        //银行卡充值/余额变动 ｜实际入账金额
        BigDecimal alterAmt = flowType.equals(EnumFlowType.WITHDRAW.getName()) ? amtPay.negate() :amtPay;

        //1.钱包充值修改钱包余额
        Integer balanceIndex = iCoinHolderInfoService.alterCoinHolderBalance(holderInfo.getPayerAddr(), alterAmt, null);
        if (balanceIndex != 1) {
            _log.error("钱包余额变动失败:{}",holderInfo.getPayerAddr());
            throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "钱包余额变动失败");
        }

        //2.钱包变动流水
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("payerAddr", holderInfo.getPayerAddr());
        CoinHolderDepositFlow  latestRecord = iCoinHolderDepositFlowService.selectLatestCardPayerFlowForUpdate(params);

        //没有流水 要新插入
        if (latestRecord == null) {

            //记录充值银行流水
            CoinHolderDepositFlow flowInfo = new CoinHolderDepositFlow();

            flowInfo.setWithdrawNo(withdrawNo);
            flowInfo.setRefNo(refNo);//上游GCASH的充值流水号
            flowInfo.setChannelWithdrawNo(channelWithdrawNo);//哪个商家分配的通道
            flowInfo.setFlowType(flowType); //自动上分
            flowInfo.setAmt(amt);  //银行卡交易总额
            flowInfo.setAmtPay(amtPay);
            flowInfo.setAmtCredit(alterAmt); //收款钱包入账金额
            flowInfo.setAmtBefore(new BigDecimal(0));
            flowInfo.setAmtNow(alterAmt);
            if (holderDepositInfo!=null) {
                flowInfo.setMerchantId(holderDepositInfo.getMerchantId());//哪个商家的ID
            }
            flowInfo.setPayerMerchantId(holderInfo.getPayerMerchantId()); //哪个码商
            flowInfo.setPayerIdentity(holderInfo.getPayerIdentity());   //哪个收款人
            flowInfo.setPayerAddr(holderInfo.getPayerAddr());
            flowInfo.setPayerName(holderInfo.getPayerName());

            //flowInfo.setCreateTime(new Date());
            flowInfo.setDate(DateUtil.getDate());
            flowInfo.setRemark(remark);

            flowInfo.setChannelCode(holderInfo.getChannelCode());
            flowInfo.setChannelName(holderInfo.getChannelName());
            flowInfo.setManagerName("");

            boolean isFlowChange = iCoinHolderDepositFlowService.save(flowInfo);
            if (!isFlowChange) {
                _log.error("=====>>>>>代收钱包地址:{},{}流水写入失败,回滚", holderInfo.getPayerAddr(), alterAmt);
                throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "代收钱包:订单号[" + withdrawNo + "],金额[" + alterAmt + "]流水写入失败");
            }

        } else {

            //从流水表内选取
            CoinHolderDepositFlow flowEntity = new CoinHolderDepositFlow();
            flowEntity.setPayerAddr(holderInfo.getPayerAddr()); //哪个钱包
            flowEntity.setAmt(alterAmt); //增加|减少 余额

            //1.插入流水表
            Integer latestRecordId = iCoinHolderDepositFlowService.selectIdByInsertLatestCardPayerFlow(flowEntity);
            if (latestRecordId != 1) {
                throw new TranException(CodeConst.SYSTEM_ERROR_CODE, "失败:插入钱包流水失败");
            }
            //2.获取刚插入的卡自增ID
             //CardPayerFlowInfo latestRecordCardPayerFlowInfo = iCardPayerFlowInfoService.getById(flowEntity.getId());

            //3.记录 CoinHolderDepositFlow 流水
            //如果有冲正流水了 不再重复插入
//            CoinHolderDepositFlow flowInfoBack = iCoinHolderDepositFlowService.getOne(new QueryWrapper<CoinHolderDepositFlow>().lambda()
//                    .eq(CoinHolderDepositFlow::getWithdrawNo,withdrawNo)
//                    .eq(CoinHolderDepositFlow::getFlowType,EnumFlowType.RECEIVE.getName()));
//            if (flowInfoBack != null){
//                throw new TranException(CodeConst.SYSTEM_ERROR_CODE, "失败:已有充值流水,插入银行卡流水失败");
//            }

            CoinHolderDepositFlow flowInfo = new CoinHolderDepositFlow();


            flowInfo.setWithdrawNo(withdrawNo);
            flowInfo.setChannelWithdrawNo(channelWithdrawNo);
            flowInfo.setRefNo(refNo);
            flowInfo.setFlowType(flowType); //自动上分
            flowInfo.setAmt(amt);  //银行卡交易总额
            flowInfo.setAmtPay(amtPay);
            flowInfo.setAmtCredit(alterAmt); //收款钱包入账金额
            //flowInfo.setAmtBefore(new BigDecimal(0)); //已用selectIdByInsertLatestCardPayerFlow插入
            //flowInfo.setAmtNow(new BigDecimal(amt));

            //flowInfo.setMerchantId(holderInfo.getMerchantId());
            if (holderDepositInfo!=null) {
                flowInfo.setMerchantId(holderDepositInfo.getMerchantId());//哪个商家的ID
            }
            flowInfo.setPayerMerchantId(holderInfo.getPayerMerchantId()); //哪个码商
            flowInfo.setPayerIdentity(holderInfo.getPayerIdentity());   //哪个收款人

            assert holderDepositInfo != null;
            flowInfo.setPayerAddr(holderInfo.getPayerAddr());
            flowInfo.setPayerName(holderInfo.getPayerName());

            flowInfo.setCreateTime(new Date());
            flowInfo.setDate(DateUtil.getDate());
            flowInfo.setRemark(remark);

            flowInfo.setChannelCode(holderInfo.getChannelCode());
            flowInfo.setChannelName(holderInfo.getChannelName());

            flowInfo.setManagerName("");

            boolean isFlowChange = iCoinHolderDepositFlowService.update(flowInfo, new QueryWrapper<CoinHolderDepositFlow>()
                    .lambda().eq(CoinHolderDepositFlow::getId, flowEntity.getId()));
            if (!isFlowChange) {
                _log.error("=====>>>>>代收钱包:{},{}流水写入失败,回滚", holderInfo.getPayerAddr(), alterAmt);
                throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "代收钱包单[" + channelWithdrawNo + "],USDT金额[" + alterAmt + "]流水写入失败");
            }else {
                /**
                 * @流水插入成功
                 * @通知充值的商户
                 * */
            }

        }
    }


}
