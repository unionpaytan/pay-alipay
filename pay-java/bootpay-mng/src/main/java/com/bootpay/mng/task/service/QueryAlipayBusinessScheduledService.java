package com.bootpay.mng.task.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.bootpay.common.constants.CodeConst;
import com.bootpay.common.framework.RedisUtil;
import com.bootpay.common.utils.HttpClient4Utils;
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
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class QueryAlipayBusinessScheduledService {

    private Logger _log = LoggerFactory.getLogger(QueryAlipayBusinessScheduledService.class);

    @Value("${app.nodeTokenDomain}") //application.yml文件读取
    public  String nodeTokenDomain;


    @Autowired
    private ICoinHolderDepositFlowService iCoinHolderDepositFlowService; //COIN FLOW

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
    private ICoinHolderInfoService iCoinHolderInfoService;

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
    public void checkBusinessStatus(CoinAlipayInfo info) throws Exception {


        Calendar nowCalendar = Calendar.getInstance();
        nowCalendar.add(Calendar.SECOND, 0);
        String nowTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(nowCalendar.getTime());


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
        String businessType = CodeConst.BusinessType.BUSINESS;
        String token = info.getAlipayCtoken();
        String uid = info.getAlipayUid();
        String alipayCookie = info.getAlipayCookie();
        //登录绑定成功的支付宝商家 若已禁用，则不再进行收款查询
         String serverResult =  queryAlipayOrderList(businessType,alipayUrl,alipayCookie,token,uid,startTime,endTime);
           //
            _log.info("支付宝商家:{},返回 alipay server res - {}",info.getAlipayName(),serverResult);
            if (serverResult != null) {
                JSONObject json = JSONObject.parseObject(serverResult);
                String status = json.getString("status");
                String msg = json.getString("msg");
                if ("deny".equalsIgnoreCase(status)) {
                     _log.error("登录绑定失败");
                     CoinAlipayInfo updInfo = new CoinAlipayInfo();
//                     updInfo.setStatus(CodeConst.AlipayStatus.OFFLINE);//不要自动关闭 否则就接不了单了
                     updInfo.setIsBind(CodeConst.AlipayIsBindStatus.NO);//关闭自动绑定 ,下线功能
                     //updInfo.setAlipayUid("");//清空
                     updInfo.setAlipayCtoken("");
                     updInfo.setAlipayCookie("");
                     updInfo.setUpdateTime(new Date());
                    boolean isUpdAlipay = iCoinAlipayInfoService.update(updInfo,new UpdateWrapper<CoinAlipayInfo>().lambda()
                            .eq(CoinAlipayInfo::getId, info.getId()));
                    if (!isUpdAlipay) {
                        _log.error("更新登录状态失败");
                    }else {
                        _log.info("更新登录状态成功");
                    }
                     //更新 coinHolderInfo的 isBind
                    CoinHolderInfo updHolderInfo = new CoinHolderInfo();
//                     updInfo.setStatus(CodeConst.AlipayStatus.OFFLINE);//不要自动关闭 否则就接不了单了
                    updHolderInfo.setIsBind(CodeConst.AlipayIsBindStatus.NO);//关闭自动绑定 ,下线功能
                   boolean isUpdCoinHolder =  iCoinHolderInfoService.update(updHolderInfo,new UpdateWrapper<CoinHolderInfo>().lambda()
                            .eq(CoinHolderInfo::getAlipayInfoId, info.getId()));
                    if (!isUpdCoinHolder) {
                        _log.error("更新商家码状态失败");
                    }else {
                        _log.info("更新商家码状态成功");
                    }

                }else if ("succeed".equalsIgnoreCase(status)) {
                    _log.info("登录绑定成功 - {}", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date().getTime()));
                    _log.info("Alipay 在线商家:{},UID:{},时间:{}",info.getAlipayName(),info.getAlipayUid(),new Date().toString());
                    if (!info.getStatus().equals(CodeConst.PayerCardStatusConst.CARD_STATUS_BELONG)) {
                        _log.info("商家状态为:{},不参与自动回调;", info.getStatus());
                    }else {

                        CoinAlipayInfo updInfo = new CoinAlipayInfo();
                        updInfo.setUpdateTime(new Date());//更新绑定时间
                        iCoinAlipayInfoService.update(updInfo,new UpdateWrapper<CoinAlipayInfo>().lambda()
                                .eq(CoinAlipayInfo::getId, info.getId()));

                        CoinHolderInfo updHolderInfo = new CoinHolderInfo();
                        updHolderInfo.setPayerName(info.getAlipayName());
                        updHolderInfo.setIsBind(CodeConst.AlipayIsBindStatus.YES);
                        iCoinHolderInfoService.update(updHolderInfo,new UpdateWrapper<CoinHolderInfo>().lambda()
                                .eq(CoinHolderInfo::getAlipayInfoId, info.getId()));

                        JSONObject result = json.getJSONObject("result");
                        JSONArray orderList = result.getJSONArray("detail");

                        if (!orderList.isEmpty()) {
                            // jsonArray
                            for (int i = 0; i < orderList.size(); i++) {
                                JSONObject orderObj = (JSONObject) orderList.get(i); //将array中的数据进行逐条转换
                                String outTradeNo = orderObj.getString("outTradeNo");
                                String totalPayedAmount = orderObj.getString("totalPayedAmount");
                                String tradeTransAmount = orderObj.getString("tradeTransAmount");
                                String tradeNo = orderObj.getString("tradeNo");
                                String tradeType = orderObj.getString("tradeType"); //担保交易
                                String tradeStatus = orderObj.getString("tradeStatus");//周转码 待确认收货
                                String direction = orderObj.getString("direction");
                                String gmtCreate = orderObj.getString("gmtCreate");//2024-06-02 19:24:49,订单得 10分钟内有效 才自动设为成功
                                String cannotRefundReason = orderObj.getString("cannotRefundReason");

                                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                                Date date1 = sdf.parse(gmtCreate); //周转码的这个时间和个人码不一样 ，个人码是实时创建，周转码是先创建再收款
                                Date nowDatetime = sdf.parse(nowTime);
                                Date date2 = sdf.parse(minsAgoTime); //10分钟前

                                //商家码不用时间判断 因为每个商家码只能付款一次
                                if (cannotRefundReason.equals("该订单类型不支持发起退款") && tradeType.equalsIgnoreCase("担保交易") && (tradeStatus.equalsIgnoreCase("待确认收货") || tradeStatus.equalsIgnoreCase("待发货"))&& direction.equalsIgnoreCase("卖出")) {
                                    _log.info("支付宝商家名称:{},UID:{},商家交易号:{},支付宝平台订单号:{},支付金额:{},订单状态:{}",info.getAlipayName(),info.getAlipayUid(),outTradeNo.substring(4),tradeNo,totalPayedAmount,tradeStatus);
                                    //查询数所库订单 订单若匹配 则更新二维码的状态
                                    //新增若收款码已有成功，就不要再重新收款，以防止误启用，造成重复收款
                                    CoinHolderDepositInfo holderDepositInfoAlreadySuccess = iCoinHolderDepositInfoService.getOne(new QueryWrapper<CoinHolderDepositInfo>().lambda()
                                                    .eq(CoinHolderDepositInfo::getPayerAddr, outTradeNo.substring(5))
                                                    .eq(CoinHolderDepositInfo::getStatus,CodeConst.WithdrawStatusConst.WITHDRAW_SUCCESS)
                                    );
                                    if (holderDepositInfoAlreadySuccess == null) {

                                        //周转码是唯一的 订单号 由支付宝生成
                                        CoinHolderDepositInfo holderDepositInfo = iCoinHolderDepositInfoService.getOne(new QueryWrapper<CoinHolderDepositInfo>().lambda()
                                                .eq(CoinHolderDepositInfo::getPayerAddr, outTradeNo.substring(5))
                                                .eq(CoinHolderDepositInfo::getAmt,new BigDecimal(totalPayedAmount))
                                                .ge(CoinHolderDepositInfo::getCreateTime,date2)
                                        );
                                        //订单[排队中]才可修正 + 查询的任务也不可设为失败 || 以防止前端设为成功后刚好时间到期
                                        if (holderDepositInfo != null) {
                                            if (holderDepositInfo.getStatus().equals(CodeConst.WithdrawStatusConst.WITHDRAW_SUCCESS) || holderDepositInfo.getStatus().equals(CodeConst.WithdrawStatusConst.WITHDRAW_FAIL)) {
                                                //                                 throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "订单状态[非待支付],不可更改状态");
                                                _log.info("订单:{}状态为:{},不可更改状态",holderDepositInfo.getWithdrawNo(),holderDepositInfo.getStatus());
                                            }else {
                                                //@service 服务 减少重复 code
                                                barcodeManagerService.barcodeSuccess(null,holderDepositInfo.getWithdrawNo(),outTradeNo,totalPayedAmount,tradeNo,tradeStatus,holderDepositInfo,info.getAlipayUid(),CodeConst.BusinessType.BUSINESS,"1");//1-自动
                                            }
                                        }else {
                                            _log.info("系统不包含支付宝商家订单号:{},支付宝商家名称:{},UID:{}",outTradeNo,info.getAlipayName(),info.getAlipayUid());
                                        }

                                    }else {
                                        _log.info("订单:{}状态为:{},已成功,不能再重复接收新订单",holderDepositInfoAlreadySuccess.getWithdrawNo(),holderDepositInfoAlreadySuccess.getStatus());
                                    }


                                }

                            }
                        }else if ("failed".equalsIgnoreCase(status)){
                            _log.error("支付宝商家名称:{},UID:{},请求有误:{}",info.getAlipayName(),info.getAlipayUid(),msg);
                        }

                    }

                }
            }
//        }

    }



    public  String queryAlipayOrderList(String businessType, String alipayUrl, String alipayCookie, String token, String uid, String startTime, String endTime){

        //String targetUrl = "http://localhost:3000" + "/queryAlipayOrderList";
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
