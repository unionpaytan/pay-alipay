package com.bootpay.mng.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.bootpay.common.constants.CodeConst;
import com.bootpay.common.constants.enums.EnumFlowType;
import com.bootpay.common.constants.enums.EnumMerchantStatusType;
import com.bootpay.common.constants.enums.EnumRequestType;
import com.bootpay.common.constants.enums.EnumWithdrawStatus;
import com.bootpay.common.excetion.TranException;
import com.bootpay.common.utils.BigDecimalUtils;
import com.bootpay.common.utils.DateUtil;
import com.bootpay.common.utils.HtmlUtil;
import com.bootpay.common.utils.MySeq;
import com.bootpay.core.entity.*;
import com.bootpay.core.service.*;
import com.bootpay.mng.amqueue.AMProducerUtils;
import com.bootpay.mng.service.CardChannelManagerService;
import com.bootpay.mng.service.CoinChannelManagerService;
import com.bootpay.mng.service.MerchantAccountManagerService;
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
import java.util.List;
import java.util.Map;

@Service
public class CardChannelManagerServiceImpl implements CardChannelManagerService {

    private static Logger _log = LoggerFactory.getLogger(CardChannelManagerServiceImpl.class);

    @Autowired
    ICoinChannelWithdrawInfoService iCoinChannelWithdrawInfoService;

    @Autowired
    ICardChannelInfoService iCardChannelInfoService;

    @Autowired
    ICardChannelFlowInfoService iCardChannelFlowInfoService;

    @Override
    @Transactional(rollbackFor = Exception.class) //通道充值回滚
    @SuppressWarnings({"deprecation"})
    public void alterCardChannelBalanceService(
                                    BigDecimal amt,
                                    String flowType,
                                    String rechargeType,
                                    String cardWithdrawNo,
                                    String channelWithdrawNo,
                                    String cardChannelCode,
                                    String managerName,
                                    String channelName,
                                    String remark) throws TranException {

        // 修改通道余额｜手续费余额
        // 取款 扣除手续费
        // 充值 增加手续费
        // 退回 增加手续费
        BigDecimal rechargeAmt = flowType.equals(CodeConst.CardFlowTypeConst.WITHDRAW)? amt.negate() : amt;

        Integer index = iCardChannelInfoService.alterCardChannelBalance(cardChannelCode, amt, rechargeType, null);

        String returnTxt = rechargeType.equals(CodeConst.CardRechargeTypeConst.CHANNEL) ? "通道" : "手续费";

        if (index != 1) {
            throw new TranException(CodeConst.SYSTEM_ERROR_CODE, "修改"+returnTxt + "余额失败");
         }

        // 记录通道充值流水 | 流水用绝对值
        CardChannelFlowInfo flowInfo = new CardChannelFlowInfo();

        //手动后台充值是没有四方流水号的
        flowInfo.setCardWithdrawNo(cardWithdrawNo);
        flowInfo.setChannelWithdrawNo(channelWithdrawNo);
        flowInfo.setAmt(rechargeAmt);    //充值金额
        flowInfo.setFlowType(flowType); //有流水类型 ,充值金额 无负数 除非输入的时候 -1
        flowInfo.setRechargeType(rechargeType);

        //flowInfo.setCreateTime(new Date());
        flowInfo.setRemark(StringUtils.isBlank(remark) ? managerName : HtmlUtil.delHTMLTag(remark));

        flowInfo.setCardChannelCode(cardChannelCode);

        flowInfo.setManagerName(managerName);
        flowInfo.setChlName(channelName);
        boolean isFlowChange = iCardChannelFlowInfoService.save(flowInfo);

        if (!isFlowChange) {
            _log.error("=====>>>>>通道:{},{}流水写入失败,回滚", cardChannelCode,rechargeAmt);
            throw new TranException(CodeConst.SYSTEM_ERROR_CODE, "修改"+returnTxt+"[" + cardChannelCode + "],金额[" + rechargeAmt + "]失败");
        }

    }


}
