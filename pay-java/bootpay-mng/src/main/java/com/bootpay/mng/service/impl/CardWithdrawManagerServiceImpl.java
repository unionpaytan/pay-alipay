package com.bootpay.mng.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.bootpay.common.constants.CodeConst;
import com.bootpay.common.constants.enums.EnumFlowType;
import com.bootpay.common.excetion.TranException;
import com.bootpay.common.utils.DateUtil;
import com.bootpay.common.utils.HtmlUtil;
import com.bootpay.core.entity.CardChannelFlowInfo;
import com.bootpay.core.entity.CardPayerFlowInfo;
import com.bootpay.core.entity.CardPayerInfo;
import com.bootpay.core.service.*;
import com.bootpay.mng.service.CardChannelManagerService;
import com.bootpay.mng.service.CardWithdrawManagerService;
import org.apache.commons.lang.StringUtils;
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
public class CardWithdrawManagerServiceImpl implements CardWithdrawManagerService {

    private static Logger _log = LoggerFactory.getLogger(CardWithdrawManagerServiceImpl.class);

    @Autowired
    ICardChannelInfoService iCardChannelInfoService;

    @Autowired
    ICardChannelFlowInfoService iCardChannelFlowInfoService;

    @Autowired
    private ICardPayerInfoService iCardPayerInfoService; //持卡人service

    @Autowired
    private ICardPayerFlowInfoService iCardPayerFlowInfoService; //银行卡流水表

    @Override
    @Transactional(rollbackFor = Exception.class) //通道充值回滚
    @SuppressWarnings({"deprecation"})
    public void alterCardPayerBalanceNAddFlowService(CardPayerInfo cardPayerInfo, BigDecimal amt, String flowType, String cardWithdrawNo,String remark) throws TranException {

        //银行卡充值/余额变动 ｜实际入账金额
        BigDecimal alterAmt = flowType.equals(EnumFlowType.WITHDRAW.getName()) ? amt.negate() :amt;

        //修改银行卡余额
        Integer cardPayerBalanceIndex = iCardPayerInfoService.alterCardPayerBalance(cardPayerInfo.getPayerCardNo(), alterAmt, null);
        if (cardPayerBalanceIndex != 1) {
            throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "银行卡余额变动失败");
        }

        //上分 "银行卡充值" + 银行卡变动流水
        Map<String, Object> paramsCardPayer = new HashMap<String, Object>();
        paramsCardPayer.put("payerCardNo", cardPayerInfo.getPayerCardNo());
        CardPayerFlowInfo latestRecord = iCardPayerFlowInfoService.selectLatestCardPayerFlowForUpdate(paramsCardPayer);

        //没有流水 要新插入
        if (latestRecord == null) {

            //记录充值银行流水
            CardPayerFlowInfo flowInfo = new CardPayerFlowInfo();

            flowInfo.setWithdrawNo(cardWithdrawNo);
            flowInfo.setCardWithdrawNo(cardWithdrawNo);
            flowInfo.setFlowType(flowType); //自动上分
            flowInfo.setAmt(amt);  //银行卡交易总额
            flowInfo.setCreditAmt(amt); //银行卡交易总额 下发时银行卡金额与交易金额一致
            flowInfo.setAmtBefore(new BigDecimal(0));
            flowInfo.setAmtNow(alterAmt);

            flowInfo.setPayerBankType(cardPayerInfo.getPayerBankType());
            flowInfo.setPayerMerchantId(cardPayerInfo.getPayerMerchantId());

            flowInfo.setBankName(cardPayerInfo.getBankName());
            flowInfo.setBankCode(cardPayerInfo.getBankCode());
            flowInfo.setPayerCardNo(cardPayerInfo.getPayerCardNo());
            flowInfo.setPayerName(cardPayerInfo.getPayerName());

            //flowInfo.setCreateTime(new Date());
            flowInfo.setDate(DateUtil.getDate());
            flowInfo.setRemark(remark);

            flowInfo.setCardChannelCode(cardPayerInfo.getCardChannelCode());
            flowInfo.setCardChannelName(cardPayerInfo.getCardChannelName());
            flowInfo.setManagerName("");

            boolean isFlowChange = iCardPayerFlowInfoService.save(flowInfo);
            if (!isFlowChange) {
                _log.error("=====>>>>>卡号:{},{}流水写入失败,回滚", cardPayerInfo.getPayerCardNo(), amt);
                throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "银行卡订单[" + cardWithdrawNo + "],金额[" + amt + "]流水写入失败");
            }

        } else {

            //从流水表内选取
            CardPayerFlowInfo flowEntity = new CardPayerFlowInfo();
            flowEntity.setPayerCardNo(cardPayerInfo.getPayerCardNo()); //哪一张卡
            flowEntity.setAmt(alterAmt); //增加/减少余额

            //1.插入流水表
            Integer latestRecordId = iCardPayerFlowInfoService.selectIdByInsertLatestCardPayerFlow(flowEntity);
            if (latestRecordId != 1) {
                throw new TranException(CodeConst.SYSTEM_ERROR_CODE, "失败:插入银行卡流水失败");
            }
            //2.获取刚插入的卡自增ID
            // CardPayerFlowInfo latestRecordCardPayerFlowInfo = iCardPayerFlowInfoService.getById(flowEntity.getId());

            //3.记录CardPayerFlowInfo 银行卡流水
            //如果有冲正流水了 不再重复插入
            CardPayerFlowInfo cardPayerFlowInfoBack = iCardPayerFlowInfoService.getOne(new QueryWrapper<CardPayerFlowInfo>().lambda()
                    .eq(CardPayerFlowInfo::getCardWithdrawNo,cardWithdrawNo)
                    .eq(CardPayerFlowInfo::getFlowType,EnumFlowType.RECEIVE.getName()));
            if (cardPayerFlowInfoBack != null){
                throw new TranException(CodeConst.SYSTEM_ERROR_CODE, "失败:已有冲正流水,插入银行卡流水失败");
            }
            
            CardPayerFlowInfo flowInfo = new CardPayerFlowInfo();

            flowInfo.setWithdrawNo(cardWithdrawNo);
            flowInfo.setCardWithdrawNo(cardWithdrawNo);
            flowInfo.setFlowType(flowType);
            flowInfo.setAmt(amt);  //银行卡交易总额
            flowInfo.setCreditAmt(amt); //银行卡交易总额 下发时银行卡金额与交易金额一致
            //flowInfo.setAmtBefore(new BigDecimal(0)); //已用selectIdByInsertLatestCardPayerFlow插入
            //flowInfo.setAmtNow(new BigDecimal(amt));

            flowInfo.setPayerBankType(cardPayerInfo.getPayerBankType());
            flowInfo.setPayerMerchantId(cardPayerInfo.getPayerMerchantId());

            flowInfo.setBankName(cardPayerInfo.getBankName());
            flowInfo.setBankCode(cardPayerInfo.getBankCode());
            flowInfo.setPayerCardNo(cardPayerInfo.getPayerCardNo());
            flowInfo.setPayerName(cardPayerInfo.getPayerName());

            flowInfo.setCreateTime(new Date());
            flowInfo.setDate(DateUtil.getDate());
            flowInfo.setRemark(remark);

            flowInfo.setCardChannelCode(cardPayerInfo.getCardChannelCode());
            flowInfo.setCardChannelName(cardPayerInfo.getCardChannelName());
            flowInfo.setManagerName("");

            boolean isFlowChange = iCardPayerFlowInfoService.update(flowInfo, new QueryWrapper<CardPayerFlowInfo>()
                    .lambda().eq(CardPayerFlowInfo::getId, flowEntity.getId()));
            if (!isFlowChange) {
                _log.error("=====>>>>>卡号:{},{}流水写入失败,回滚", cardPayerInfo.getPayerCardNo(), amt);
                throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "银行卡订单[" + cardWithdrawNo + "],金额[" + amt + "]流水写入失败");
            }else {
                /**
                 * @流水插入成功
                 * @通知充值的商户
                 * */
            }

        }
    }

}
