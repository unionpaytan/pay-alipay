package com.bootpay.mng.task.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.bootpay.common.constants.CodeConst;
import com.bootpay.common.framework.RedisUtil;
import com.bootpay.common.utils.DateUtil;
import com.bootpay.common.utils.HttpClient4Utils;
import com.bootpay.core.entity.CoinAlipayInfo;
import com.bootpay.core.entity.CoinHolderDepositInfo;
import com.bootpay.core.entity.CoinHolderInfo;
import com.bootpay.core.service.*;
import com.bootpay.mng.service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class QueryAlipayPersonScheduledService {

    private Logger _log = LoggerFactory.getLogger(QueryAlipayPersonScheduledService.class);

    @Value("${app.nodeTokenDomain}") //application.yml文件读取
    public  String nodeTokenDomain;

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

    /**
     * 商家码 收款卡
     */
    @Transactional(rollbackFor = Exception.class)
    @SuppressWarnings("deprecation")
    public void checkPersonStatus(CoinHolderInfo info) throws Exception {

        Calendar minsCalendar = Calendar.getInstance();
        minsCalendar.add(Calendar.MINUTE, -10);
        String minsAgoTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(minsCalendar.getTime());
        _log.warn("多少分钟前的时间:{}", minsAgoTime);

        Calendar calendarStart = Calendar.getInstance();
        calendarStart.add(Calendar.DATE, 0);//实际为 0 == 当天的时间
        String startTime = new SimpleDateFormat("yyyy-MM-dd").format(calendarStart.getTime());

        Calendar calendarEnd = Calendar.getInstance();
        calendarEnd.add(Calendar.DATE, 1);
        String endTime = new SimpleDateFormat("yyyy-MM-dd").format(calendarEnd.getTime());

        String alipayUrl = "https://mbillexprod.alipay.com/enterprise/tradeListQuery.json";
        String businessType = CodeConst.BusinessType.PERSON;
        String token = info.getAlipayCtoken();
        String uid = info.getAlipayUid();
        String alipayCookie = info.getAlipayCookie();
        //登录绑定成功的支付宝个码
          String serverResult =  queryAlipayOrderList(businessType,alipayUrl,alipayCookie,token,uid,startTime,endTime);
            _log.info("支付宝个码:{},返回 alipay server res - {}",info.getPayerName(),serverResult);
            if (serverResult != null) {
                JSONObject json = JSONObject.parseObject(serverResult);
                String status = json.getString("status");
                if ("deny".equalsIgnoreCase(status)) {
                     _log.info("登录绑定失败:个码 - {}",info.getPayerAddr());
                     CoinHolderInfo holderInfo = new CoinHolderInfo();
//                     updInfo.setStatus(CodeConst.AlipayStatus.OFFLINE);//不要自动关闭 否则就接不了单了
                     holderInfo.setIsBind(CodeConst.AlipayIsBindStatus.NO);//下线
                     holderInfo.setAlipayCtoken("");
                     holderInfo.setAlipayCookie("");
                     iCoinHolderInfoService.update(holderInfo,new UpdateWrapper<CoinHolderInfo>().lambda()
                            .eq(CoinHolderInfo::getId, info.getId()));
                }else if ("succeed".equalsIgnoreCase(status)) {
                    _log.info("登录绑定成功:个码 - {},{}",info.getPayerAddr(), new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date().getTime()));
                    _log.info("Alipay 个人码:{},UID:{},时间:{}",info.getPayerName(),info.getAlipayUid(),new Date().toString());
                    if (!info.getStatus().equals(CodeConst.PayerCardStatusConst.CARD_STATUS_BELONG)) {
                        _log.info("个码状态为:{},不参与自动回调;",info.getStatus());
                    }else {
                        CoinHolderInfo holderInfo = new CoinHolderInfo();
                        holderInfo.setIsBind(CodeConst.AlipayIsBindStatus.YES);//上线
                        iCoinHolderInfoService.update(holderInfo,new UpdateWrapper<CoinHolderInfo>().lambda()
                                .eq(CoinHolderInfo::getId, info.getId()));

                        JSONObject result = json.getJSONObject("result");
                        JSONArray orderList = result.getJSONArray("detail");
                        // outTradeNo : "T200P2142514632903819675" //商家订单号 去掉 T200P // 2142514632903819675为二维码的订单号
                        // totalPayedAmount: "300.00"
                        // tradeNo : "2024050722001165751439145610" //支付宝交易号(系统：上游通道号)
                        // tradeStatus:待确认收货
                        if (!orderList.isEmpty()) {
                            // jsonArray
                            for (int i = 0; i < orderList.size(); i++) {
                                JSONObject orderObj = (JSONObject) orderList.get(i); //将array中的数据进行逐条转换
                                String tradeStatus = orderObj.getString("tradeStatus");//成功
                                String totalPayedAmount = orderObj.getString("totalPayedAmount");
                                String outTradeNo = orderObj.getString("outTradeNo");
                                String tradeNo = orderObj.getString("tradeNo");
                                String tradeType = orderObj.getString("tradeType");//即时到帐
                                String direction = orderObj.getString("direction");//卖出
                                String goodsTitle = orderObj.getString("goodsTitle");//收钱码收款
                                String gmtCreate = orderObj.getString("gmtCreate");//2024-06-02 19:24:49,订单得 10分钟内有效 才自动设为成功

                                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                                Date date1 = sdf.parse(gmtCreate);
                                Date date2 = sdf.parse(minsAgoTime);

                                if (date1.compareTo(date2) > 0 && "成功".equals(tradeStatus) && "即时到帐".equals(tradeType) && "卖出".equals(direction) && "收钱码收款".equals(goodsTitle)) {
                                    _log.info("条件相符:支付宝个人名称:{},UID:{},商家交易号:{},支付宝平台订单号:{},支付金额:{},订单状态:{}",info.getPayerName(),info.getAlipayUid(),outTradeNo,tradeNo,totalPayedAmount,tradeStatus);
                                    //个码：10分钟内相同金额的只有一单
                                    //查询数所库订单 订单若匹配 则更新二维码的状态
                                    CoinHolderDepositInfo holderDepositInfo = iCoinHolderDepositInfoService.getOne(new QueryWrapper<CoinHolderDepositInfo>().lambda()
                                            .eq(CoinHolderDepositInfo::getAlipayUid,info.getAlipayUid())
                                            .eq(CoinHolderDepositInfo::getAmtPay, new BigDecimal(totalPayedAmount))
                                            .ge(CoinHolderDepositInfo::getCreateTime,minsAgoTime)
                                    );
                                    //订单[排队中]才可修正 + 查询的任务也不可设为失败 || 以防止前端设为成功后刚好时间到期
                                    if (holderDepositInfo != null) {
                                        if (holderDepositInfo.getStatus().equals(CodeConst.WithdrawStatusConst.WITHDRAW_SUCCESS) || holderDepositInfo.getStatus().equals(CodeConst.WithdrawStatusConst.WITHDRAW_FAIL)) {
                                            //                                 throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "订单状态[非待支付],不可更改状态");
                                            _log.info("订单:{}状态为:{},不可更改状态",holderDepositInfo.getWithdrawNo(),holderDepositInfo.getStatus());
                                        }else {
                                            //@service 服务 减少重复 code
                                            barcodeManagerService.barcodeSuccess(null,holderDepositInfo.getWithdrawNo(),outTradeNo,totalPayedAmount,tradeNo,tradeStatus,holderDepositInfo,holderDepositInfo.getAlipayUid(),CodeConst.BusinessType.PERSON,"1");//1-自动
                                        }
                                    }else {
                                        _log.info("系统不包含支付宝订单号:{},支付宝个人名称:{},UID:{}",outTradeNo,info.getPayerName(),info.getAlipayUid());
                                    }
                                }else {
                                    _log.info("条件不符:支付宝订单创建时间:{},当前系统时间:{},支付宝个人名称,{},UID:{},商家交易号:{},支付宝平台订单号:{},支付金额:{},订单状态:{}",gmtCreate,sdf.toString(),info.getPayerName(),info.getAlipayUid(),outTradeNo,tradeNo,totalPayedAmount,tradeStatus);
                                }

                            }
                        }

                    }

                }
            }
//        }

    }

    public  String queryAlipayOrderList(String businessType, String alipayUrl, String alipayCookie, String token, String uid, String startTime, String endTime){
        String targetUrl = nodeTokenDomain + "/queryAlipayOrderList";
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("businessType", businessType);//1-商家码
        params.put("alipayUrl", alipayUrl);
        params.put("cookie", alipayCookie);
        params.put("token", token);
        params.put("uid", uid);
        params.put("startTime", startTime +  " 00:00:00");
        params.put("endTime", endTime +  " 00:00:00") ;

        String jsoinString = JSON.toJSONString(params);
        return HttpClient4Utils.postJSON(targetUrl,jsoinString);
    }

}
