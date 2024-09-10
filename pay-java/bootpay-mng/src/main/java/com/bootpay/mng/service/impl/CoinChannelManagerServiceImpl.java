package com.bootpay.mng.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.bootpay.common.constants.CodeConst;
import com.bootpay.common.excetion.TranException;
import com.bootpay.common.utils.HtmlUtil;
import com.bootpay.core.entity.CardChannelFlowInfo;
import com.bootpay.core.entity.CoinChannelWithdrawFlow;
import com.bootpay.core.entity.CoinChannelWithdrawInfo;
import com.bootpay.core.entity.WithdrawChannelInfo;
import com.bootpay.core.service.*;
import com.bootpay.mng.service.CardChannelManagerService;
import com.bootpay.mng.service.CoinChannelManagerService;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Date;

@Service
public class CoinChannelManagerServiceImpl implements CoinChannelManagerService {

    private static Logger _log = LoggerFactory.getLogger(CoinChannelManagerServiceImpl.class);

    @Autowired
    ICardChannelInfoService iCardChannelInfoService;

    @Autowired
    ICoinChannelWithdrawInfoService iCoinChannelWithdrawInfoService;

    @Autowired
    ICoinChannelWithdrawFlowService iCoinChannelWithdrawFlowService;
    @Autowired
    ICardChannelFlowInfoService iCardChannelFlowInfoService;

    @Autowired
    private IPayMerchantBalanceService balanceService;

    @Autowired
    private IWithdrawChannelInfoService iWithdrawChannelInfoService;

    @Override
    @Transactional(rollbackFor = Exception.class) //通道充值回滚
    @SuppressWarnings({"deprecation"})
    public void alterCoinChannelBalanceService(
                                    String merchantId,
                                    BigDecimal rechargeAmt,
                                    String flowType,
                                    String withdrawNo,
                                    String channelWithdrawNo,
                                    String channelCode,
                                    String managerName,
                                    String channelName,
                                    String remark) throws TranException {

        //2.修改商户手续费余额 代付- ，冲正+，代收-
        Integer index = balanceService.alterMerchantFee(merchantId, rechargeAmt, null);
        if (index != 1) {
            _log.error("充值失败:商户手续费修改失败 - {}",merchantId);
            throw new TranException(CodeConst.SYSTEM_ERROR_CODE, "充值失败:商户手续费修改失败");
        }

        // 记录通道充值流水 | 流水用绝对值
        CoinChannelWithdrawFlow flowInfo = new CoinChannelWithdrawFlow();
        flowInfo.setMerchantId(merchantId); //充值商户ID
        //手动后台充值是没有四方流水号的
        flowInfo.setWithdrawNo(withdrawNo);
        flowInfo.setChannelWithdrawNo(channelWithdrawNo);
        flowInfo.setFlowType(flowType); //有流水类型 ,充值金额 无负数 除非输入的时候 -1
        flowInfo.setAmt(rechargeAmt);    //充值金额

        flowInfo.setChannelName(channelName);
        flowInfo.setChannelCode(channelCode);
        flowInfo.setManagerName(managerName);
        flowInfo.setRemark(StringUtils.isBlank(remark) ? managerName : HtmlUtil.delHTMLTag(remark));
        //flowInfo.setCreateTime(new Date());

        boolean isFlowChange = iCoinChannelWithdrawFlowService.save(flowInfo);

        if (!isFlowChange) {
            _log.error("=====>>>>>通道:{},{}流水写入失败,回滚", channelCode,rechargeAmt);
            throw new TranException(CodeConst.SYSTEM_ERROR_CODE, "修改" + channelCode + "],金额[" + rechargeAmt + "]失败");
        }

    }


}
