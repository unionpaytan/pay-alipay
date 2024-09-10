package com.bootpay.mng.task;

import com.bootpay.common.constants.CodeConst;
import com.bootpay.core.entity.CoinAlipayInfo;
import com.bootpay.core.service.ICoinAlipayInfoService;
import com.bootpay.mng.task.service.QueryAlipayBusinessScheduledService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
/**
 * 定时 支付宝商家/周转码
 * @author Administrator
 */
@Component
public class QueryAlipayBusinessScheduledTask {
    private Logger _log = LoggerFactory.getLogger(QueryAlipayBusinessScheduledTask.class);
    @Autowired
    private ICoinAlipayInfoService iCoinAlipayInfoService; //alipayInfo

    @Autowired
    private QueryAlipayBusinessScheduledService queryAlipayBusinessScheduledService;

    private final Random random = new Random();


    @Scheduled(cron = "0 */1 * * * ?") //1分钟
    //@Transactional(rollbackFor = Exception.class) 不可在CONTROLLER层 应把任务写在SERVICE层
    public void businessScheduled() throws Exception {

        _log.warn("=====>>>>> Java Scheduled 每1分钟 查询 alipayInfo 支付宝商家");

        Map<String, Object> queryParams = new HashMap<String, Object>();
//        queryParams.put("status", CodeConst.PayerCardStatusConst.CARD_STATUS_BELONG); //开启的
        queryParams.put("isBind", CodeConst.AlipayIsBindStatus.YES); //有绑定成功的才查询
        List<CoinAlipayInfo> alipayInfoList = iCoinAlipayInfoService.queryCoinAlipayInfoList(queryParams);
        if (alipayInfoList == null || alipayInfoList.size() == 0) {
            _log.error("暂无商家码登录");
            return;
        }
        int delay = 15 + random.nextInt(35); // 5 + [0, 1, 2, 3, 4, 5]
        Thread.sleep(delay * 1000L);
        for (CoinAlipayInfo info : alipayInfoList) {
            Thread.sleep(1 * 1000L);
            _log.info("实际查询时间 time - {}",new Date());
             queryAlipayBusinessScheduledService.checkBusinessStatus(info);
        }

    }
}
