package com.bootpay.mng.task;

import com.bootpay.channel.constants.DepositChannelCode;
import com.bootpay.common.constants.CodeConst;
import com.bootpay.core.entity.CoinAlipayInfo;
import com.bootpay.core.entity.CoinHolderInfo;
import com.bootpay.core.service.ICoinAlipayInfoService;
import com.bootpay.core.service.ICoinHolderInfoService;
import com.bootpay.mng.task.service.QueryAlipayBusinessScheduledService;
import com.bootpay.mng.task.service.QueryAlipayPersonScheduledService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * 定时 支付宝商家/周转码
 * @author Administrator
 */
@Component
public class QueryAlipayPersonScheduledTask {
    private Logger _log = LoggerFactory.getLogger(QueryAlipayPersonScheduledTask.class);

    @Autowired
    private ICoinHolderInfoService iCoinHolderInfoService;

    @Autowired
    private QueryAlipayPersonScheduledService queryAlipayPersonScheduledService;
    private final Random random = new Random();

    @Scheduled(cron = "0 */1 * * * ?") //2分钟
    //@Transactional(rollbackFor = Exception.class) 不可在CONTROLLER层 应把任务写在SERVICE层
    public void personScheduled() throws Exception {

        _log.warn("=====>>>>> Java Scheduled 每1分钟 查询 person 支付宝个人码");

        Map<String, Object> paramsHolder = new HashMap<String, Object>();
        paramsHolder.put("channelCode", DepositChannelCode.CHANNEL_CODE_ALIPAY_POOL);//个人码
        paramsHolder.put("coinType","1");// 0-微 1-支 2-聚
        paramsHolder.put("businessType",CodeConst.BusinessType.PERSON);// 0-个人码
//        paramsHolder.put("status", CodeConst.PayerCardStatusConst.CARD_STATUS_BELONG);
        paramsHolder.put("isBind",CodeConst.AlipayIsBindStatus.YES);//只有绑定才能查询;

        List<CoinHolderInfo> coinHolderList = iCoinHolderInfoService.queryCoinHolderListAllowDeposit(paramsHolder);
        if (coinHolderList == null || coinHolderList.size() == 0) {
            _log.error("暂无个人码登录");
            return;
        }
        int delay = 15 + random.nextInt(35);
        Thread.sleep(delay * 1000L);
        for (CoinHolderInfo info : coinHolderList) {
            Thread.sleep(300L);
            queryAlipayPersonScheduledService.checkPersonStatus(info);
        }

    }
}
