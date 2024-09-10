package com.bootpay.mng.task;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.bootpay.common.constants.CodeConst;
import com.bootpay.common.constants.enums.EnumAccountType;
import com.bootpay.common.constants.enums.EnumAgentRate;
import com.bootpay.common.excetion.TranException;
import com.bootpay.common.framework.RedisUtil;
import com.bootpay.common.utils.DateUtil;
import com.bootpay.core.entity.OkexTradeInfo;
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

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;

/**
 * 定时查询代付订单状态
 *
 * @author Administrator
 */
@Component
public class QueryOKEXUSDTCNYScheduledTask {

    private Logger _log = LoggerFactory.getLogger(QueryOKEXUSDTCNYScheduledTask.class);


    @Autowired
    private IPayMerchantMoneyBalanceService iPayMerchantMoneyBalanceService; //商户每日统计

    @Autowired
    private static IOkexTradeInfoService iOkexTradeInfoService;


    //@Scheduled(cron = "0 0/30 * * * ?")
    public void scheduled() {
        _log.info("=====>>>>> Java Scheduled 每天凌晨00:00:05执行一次");
        try {
            String queryUrl = "https://www.okx.com/v3/c2c/tradingOrders/books";
            long t = System.currentTimeMillis();
            // 构建参数
            Map<String, String> params = new HashMap<>();
            params.put("quoteCurrency", "CNY");
            params.put("baseCurrency", "USDT");
            params.put("side", "sell");
            params.put("paymentMethod", "aliPay");
            params.put("userType", "all");
            params.put("showTrade", "false");
            params.put("showFollow", "false");
            params.put("showAlreadyTraded", "false");
            params.put("isAbleFilter", "false");
            params.put("receivingAds", "true");
            params.put("t", String.valueOf(t));
            String serverResult =  queryOKEX(queryUrl,params);

            JSONObject json = JSONObject.parseObject(serverResult);
            if ("0".equals(json.getString("code"))) {
                JSONObject jsonData = JSONObject.parseObject(json.getString("data"));
                JSONArray sellList = jsonData.getJSONArray("sell");

                System.out.println("Total:" + sellList.size());

                for (int i = 0; i < sellList.size(); i++) {
                    JSONObject sellItem = sellList.getJSONObject(i);
                    //System.out.println("Sell Item " + (i + 1) + ": " + sellItem.toString());
                    String quoteCurrency = sellItem.getString("quoteCurrency"); //CNY
                    String baseCurrency = sellItem.getString("baseCurrency"); //USDT
                    String price = sellItem.getString("price");
                    String nickName = sellItem.getString("nickName");
                    String side = sellItem.getString("side");
                    //System.out.println(nickName + " "+side+" "+baseCurrency+",可兑币种:"+quoteCurrency+",价格:"+price);
                    OkexTradeInfo okexTradeInfo = new OkexTradeInfo();
                    okexTradeInfo.setQuoteCurrency(quoteCurrency);
                    okexTradeInfo.setBaseCurrency(baseCurrency);
                    okexTradeInfo.setPrice(price);
                    okexTradeInfo.setNickName(nickName);
                    okexTradeInfo.setSide(side);
                    okexTradeInfo.setCreateTime(new Date());
                    iOkexTradeInfoService.save(okexTradeInfo);


                }

            }


        } catch (Exception e) {
            e.printStackTrace();
            _log.error("=====>>>>>Java ScheduledTask 抓取 OKEX 处理异常,{}", e.getMessage());
        }
    }
    public static void main(String[] args) {
        String queryUrl = "https://www.okx.com/v3/c2c/tradingOrders/books";
        long t = System.currentTimeMillis();
        // 构建参数
        Map<String, String> params = new HashMap<>();
        params.put("quoteCurrency", "CNY");
        params.put("baseCurrency", "USDT");
        params.put("side", "sell");
        params.put("paymentMethod", "aliPay");
        params.put("userType", "blockTrade");
        params.put("showTrade", "false");
        params.put("showFollow", "false");
        params.put("showAlreadyTraded", "false");
        params.put("isAbleFilter", "false");
        params.put("receivingAds", "true");
        params.put("t", String.valueOf(t));
        String serverResult =  queryOKEX(queryUrl,params);

        JSONObject json = JSONObject.parseObject(serverResult);
        if ("0".equals(json.getString("code"))) {
            JSONObject jsonData = JSONObject.parseObject(json.getString("data"));
            JSONArray sellList = jsonData.getJSONArray("sell");

            System.out.println("Total:" + sellList.size());

            for (int i = 0; i < sellList.size(); i++) {
                JSONObject sellItem = sellList.getJSONObject(i);
                //System.out.println("Sell Item " + (i + 1) + ": " + sellItem.toString());
                String quoteCurrency = sellItem.getString("quoteCurrency"); //CNY
                String baseCurrency = sellItem.getString("baseCurrency"); //USDT
                String price = sellItem.getString("price");
                String nickName = sellItem.getString("nickName");
                String side = sellItem.getString("side");
                System.out.println(nickName + " "+side+" "+baseCurrency+",可兑币种:"+quoteCurrency+",价格:"+price);

//                OkexTradeInfo okexTradeInfo = new OkexTradeInfo();
//                okexTradeInfo.setQuoteCurrency(quoteCurrency);
//                okexTradeInfo.setBaseCurrency(baseCurrency);
//                okexTradeInfo.setPrice(price);
//                okexTradeInfo.setNickName(nickName);
//                okexTradeInfo.setSide(side);
//                okexTradeInfo.setCreateTime(new Date());
//                iOkexTradeInfoService.save(okexTradeInfo);
            }

        }
    }

    private static String queryOKEX(String queryUrl,Map<String, String> params) {
        try {

            String randomIP = getRandomIP();
            // 构建URL
            String url = buildUrl(queryUrl, params);

            // 打开连接
            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();

            // 设置请求头
            connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.110 Safari/537.3");
            connection.setRequestProperty("Accept", "application/json");
            connection.setRequestProperty("X-Forwarded-For", randomIP);

            // 发送GET请求
            connection.setRequestMethod("GET");

            // 获取响应
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String line;
            StringBuilder response = new StringBuilder();

            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();
            // 输出响应
//            System.out.println(response.toString());
            // 关闭连接
            connection.disconnect();
            return response.toString();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }


    // 生成合法的IPv4地址的函数
    private static String getRandomIP() {
        Random random = new Random();
        StringBuilder ipBuilder = new StringBuilder();

        for (int i = 0; i < 4; i++) {
            ipBuilder.append(random.nextInt(256));
            if (i < 3) {
                ipBuilder.append('.');
            }
        }

        return ipBuilder.toString();
    }

    // 构建带参数的URL
    private static String buildUrl(String baseUrl, Map<String, String> params) {
        StringBuilder urlBuilder = new StringBuilder(baseUrl);

        if (!params.isEmpty()) {
            urlBuilder.append("?");
            for (Map.Entry<String, String> entry : params.entrySet()) {
                urlBuilder.append(entry.getKey()).append("=").append(entry.getValue()).append("&");
            }
            urlBuilder.deleteCharAt(urlBuilder.length() - 1); // 移除最后一个多余的"&"
        }

        return urlBuilder.toString();
    }

}
