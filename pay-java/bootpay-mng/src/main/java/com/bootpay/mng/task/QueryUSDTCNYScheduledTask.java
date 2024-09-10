package com.bootpay.mng.task;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.bootpay.common.constants.CodeConst;
import com.bootpay.common.constants.enums.EnumAccountType;
import com.bootpay.common.constants.enums.EnumAgentRate;
import com.bootpay.common.excetion.TranException;
import com.bootpay.common.framework.RedisUtil;
import com.bootpay.common.utils.DateUtil;
import com.bootpay.common.utils.HttpClient4Utils;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 定时查询代付订单状态
 *
 * @author Administrator
 */
@Component
public class QueryUSDTCNYScheduledTask {

    private Logger _log = LoggerFactory.getLogger(QueryUSDTCNYScheduledTask.class);

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


    @Scheduled(cron = "0/55 * * * * ?") //自动设为超时 而不是失败
    public void scheduled() {

    }
    public static void main(String[] args) throws Exception {
       //queryOkexUSDTCNYRate();
        queryFastMoss();
    }
    private static void queryOkexUSDTCNYRate(){
        String time = "" + System.currentTimeMillis();
       // String QUERY_OKEX_URL = "https://www.okx.com/v3/c2c/tradingOrders/books?quoteCurrency=CNY&baseCurrency=USDT&side=sell&paymentMethod=aliPay&userType=all&showTrade=false&showFollow=false&showAlreadyTraded=false&isAbleFilter=false&receivingAds=true&t=" + time;
        String QUERY_OKEX_URL = "https://www.okx.com/v3/c2c/tradingOrders/books";
        Map<String,Object> params = new HashMap<String,Object>();
        params.put("quoteCurrency", "CNY");
        params.put("baseCurrency", "USDT");
        params.put("side", "sell");
        params.put("paymentMethod", "aliPay");
        params.put("userType", "all");
        params.put("showTrade", false);
        params.put("showFollow", false);
        params.put("showAlreadyTraded", false);
        params.put("isAbleFilter", false);
        params.put("receivingAds", false);
        params.put("t", System.currentTimeMillis());
        System.out.println("请求URL - " + QUERY_OKEX_URL);
        String result = HttpClient4Utils.getJSON(QUERY_OKEX_URL,params,"GET");
        System.out.println(result);

      //_log.info("代付查询返回参数=====>>>>>{}", result);
    }

    private static void queryFastMoss(){
        String time = "" + System.currentTimeMillis();
        // String QUERY_OKEX_URL = "https://www.fastmoss.com/api/goods/saleRank?page=1&date_type=1&region=PH&order=1%2C2&pagesize=5";
        String QUERY_FASTMOSS_URL = "https://www.fastmoss.com/api/goods/saleRank";
        Map<String,Object> params = new HashMap<String,Object>();
        params.put("page", "1");
        params.put("date_type", "1");
        params.put("region", "PH");
        params.put("order", "1%2C2");
        params.put("pagesize", "5");
        System.out.println("请求URL - " + QUERY_FASTMOSS_URL);
        String result = HttpClient4Utils.getJSON(QUERY_FASTMOSS_URL,params,"GET");
        System.out.println(result);

        //_log.info("代付查询返回参数=====>>>>>{}", result);
    }
}
