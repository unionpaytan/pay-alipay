package com.bootpay.mng.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;

import com.bootpay.channel.constants.DepositChannelCode;
import com.bootpay.common.constants.CodeConst;
import com.bootpay.common.constants.MyRedisConst;
import com.bootpay.common.constants.enums.EnumChannelStatus;
import com.bootpay.common.constants.enums.EnumMerchantStatusType;
import com.bootpay.common.constants.enums.EnumRequestType;
import com.bootpay.common.constants.enums.EnumWithdrawStatus;
import com.bootpay.common.excetion.TranException;
import com.bootpay.common.framework.RedisUtil;
import com.bootpay.common.utils.*;
import com.bootpay.core.entity.*;
import com.bootpay.core.service.*;
import com.bootpay.mng.service.AuthenticatorService;
import com.bootpay.mng.service.MerchantAccountManagerService;
import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotBlank;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;


/**
 * 商户API接口代收
 *
 * @author Administrator
 */
@RestController
@RequestMapping("/deposit")
public class DepositAPIContoller {

    public Logger _log = LoggerFactory.getLogger(DepositAPIContoller.class);

    @Autowired
    private IPayWithdrawInfoService iPayWithdrawInfoService;
    @Autowired
    private MerchantAccountManagerService accountManagerService;
    @Autowired
    private IWithdrawChannelInfoService channelInfoService;
    @Autowired
    private IPayMerchantInfoService infoService;
    @Autowired
    private IPayMerchantChannelSiteService channelSiteService;
    @Autowired
    private IPayMerchantBalanceService balanceService;
    @Autowired
    private AuthenticatorService authenticatorService;

    @Autowired
    private IPayAgentBalanceService iPayAgentBalanceService;


    @Autowired
    private ICardPayerInfoService iCardPayerInfoService; //持卡人service

    @Autowired
    private ICardChannelInfoService iCardChannelInfoService; //卡通道


    @Autowired
    private IPayMerchantBalanceService iPayMerchantBalanceService;

    @Autowired
    private IPayDepositInfoService iPayDepositInfoService;

    @Autowired
    private IPayMerchantInfoService iPayMerchantInfoService;

    @Autowired
    private IPayMerchantChannelSiteService iPayMerchantChannelSiteService; //商户通道成本服务

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private ICoinChannelDepositInfoService iCoinChannelDepositInfoService; //收

    @Autowired
    private ICoinHolderInfoService iCoinHolderInfoService;

    @Autowired
    private IWithdrawChannelInfoService iWithdrawChannelInfoService;

    @Autowired
    private ICoinHolderDepositInfoService iCoinHolderDepositInfoService;

    @Autowired
    private ICoinHolderDepositFlowService iCoinHolderDepositFlowService;

    @Value("${app.walletPrivateKeySalt}") //application.yml文件读取
    private String walletPrivateKeySalt;

    @Autowired
    private IIpBlockListService iIpBlockListService;

    @Autowired
    private IIpListService iIpListService;


    /**
     * 商户API代收
     *
     * @param
     * @param
     * @param
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/apiDeposit", method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    public ReturnMsg apiDeposit(
            @NotBlank(message = "商户编号不能为空") String merchantId,
            @NotBlank(message = "订单号不能为空") String merchantWithdrawNo,
            String amt, //提交的订单充值金额
            String coinType,
            String merchantNotifyUrl,
            String merchantChannelCode,
            String clientIp,
            String extraParam,
            @NotBlank(message = "签名不能为空") String sign
    ) throws Exception {


        if (merchantId == null) {
            throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "无此商户号");
        }
        if (merchantWithdrawNo == null) {
            throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "商户订单号为空");
        }

        // 金额守护
        if (!BigDecimalUtils.isRmbNumeric(amt)) {
            throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "非正确的金额格式:" + amt);
        }

        if (BigDecimalUtils.lessThan(new BigDecimal(amt), new BigDecimal("1.00"))) {
            throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "单笔订单金额不能小于" + new BigDecimal("10.00") + "元");
        }

        //交易金额：精度不足则补小数点
        BigDecimal  amtFormat = new BigDecimal(amt).setScale(2,BigDecimal.ROUND_HALF_UP);

        if (sign == null || "".equals(sign)) {
            throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "签名为空");
        }

        //TODO:前端商户提交通道号 channelCode 则一个商户可以分配有有多条通道 每条通道按不同的汇率进行结算 针对平台有不同的上游
        //1.获取商户信息
        PayMerchantInfo merchantInfo = iPayMerchantInfoService.getOne(new QueryWrapper<PayMerchantInfo>().lambda().eq(PayMerchantInfo::getMerchantId, merchantId));
        if (merchantInfo.getStatus().equals(EnumMerchantStatusType.FAIL.getName())) {
            throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "商户禁用状态,请联系客服");
        }
        /**
         * 验证签名
         * */
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("merchantId", merchantId);// 商户号
        params.put("merchantWithdrawNo", merchantWithdrawNo);// 订单号W888666001 System.currentTimeMillis()+
        if (coinType != null && !"".equals(coinType)){
            params.put("coinType",coinType);
        }
        params.put("amt", amt);// 单位为PHP 二位小数精度
        if (merchantChannelCode != null && !"".equals(merchantChannelCode)){
            params.put("merchantChannelCode",merchantChannelCode.trim()); //商户通道编号 有传则验签
        }
        if (merchantNotifyUrl != null && !"".equals(merchantNotifyUrl)){
            params.put("merchantNotifyUrl",merchantNotifyUrl.trim()); //有传则验签
        }
        if (clientIp != null && !"".equals(clientIp)){
            params.put("clientIp",clientIp.trim()); //客户端IP
        }
        if (extraParam != null && !"".equals(extraParam)){
            params.put("extraParam",extraParam.trim()); //客户端拓展参数
        }
        if ("".equalsIgnoreCase(merchantInfo.getApiKey())){
            throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "商户未配置API密钥");
        }

        String apiKeyDecrypt = EncryptUtil.aesDecrypt(merchantInfo.getApiKey(),walletPrivateKeySalt); //商户的API KEY要解密

        if (!sign.equals(Signature.getSign(params, apiKeyDecrypt))) {
            throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "签名有误");
        }

        //2.查询商户代收通道的配置
        //商户单个支付产品内有多个通道
        //商户若没有传通道号
        if (coinType == null || "".equals(coinType)) {
            coinType = "1";//支付宝扫码 默认通道
        }
        if (merchantChannelCode == null || "".equals(merchantChannelCode)) {
            merchantChannelCode = "8001";//支付宝扫码 默认通道
        }
        String productCode = merchantChannelCode;//支付产品通道号 8001
        /**
         * TODO:此处增加产品轮询
         * @一个产品号可以多条通道
         * */
        Map<String, Object> queryParams = new HashMap<String, Object>();
        queryParams.put("merchantId",merchantId);
        queryParams.put("productCode",productCode);
        queryParams.put("payType",CodeConst.PayType.RECEIVE_IN);
        queryParams.put("channelStatus",EnumChannelStatus.ENABLE.getName()); //只有通道启用订单才会进
        List<PayMerchantChannelSite> payMerchantChannelSiteList = iPayMerchantChannelSiteService.queryPayMerchantChannelSiteList(queryParams);
        if (payMerchantChannelSiteList.isEmpty()) {
            throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "商户未配置代收通道或通道已关闭");
        }
        List<String> channelCodes = new ArrayList<>();
        List<Integer> channelWeights = new ArrayList<>();

        _log.info("产品：{},商户所有通道:{}",productCode,payMerchantChannelSiteList);
        for (PayMerchantChannelSite item :payMerchantChannelSiteList) {
            //获取三方上游代收通道信息
            WithdrawChannelInfo channelInfo = channelInfoService.getOne(new QueryWrapper<WithdrawChannelInfo>().lambda()
                    .eq(WithdrawChannelInfo::getChannelCode, item.getChannelCode()));
            if (channelInfo == null) {
                throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "代收通道不存在");
            }
            if (channelInfo.getChannelStatus().equals(EnumChannelStatus.DISABLE.getName())) {
                throw new TranException(CodeConst.BUSINESS_ERROR_CODE, channelInfo.getChannelName() + "代收通道已关闭");
            }
            if (!DateUtil.isEffectiveDate(new Date(), channelInfo.getBeginWithdrawTime(), channelInfo.getEndWithdrawTime())) {
                throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "当前时间不允许代收");
            }

            //通道单笔最低与最高金额
            if (BigDecimalUtils.greaterThan(amtFormat, channelInfo.getSingleHighest()) || BigDecimalUtils.lessThan(amtFormat, channelInfo.getSingleMinimum())) {
                throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "通道限额:" + channelInfo.getSingleMinimum() +" ~ " + channelInfo.getSingleHighest());
            }

            channelCodes.add(item.getChannelCode());
            channelWeights.add(Integer.valueOf(item.getChannelWeight()));
        }
        WeightedRoundUtil weightedRoundUtil = new WeightedRoundUtil(channelCodes,channelWeights);
        String selectedChannelCode = weightedRoundUtil.getNextChannel();

        //3.获取三方上游代收通道信息
        WithdrawChannelInfo selectedChannelInfo = channelInfoService.getOne(new QueryWrapper<WithdrawChannelInfo>().lambda()
                .eq(WithdrawChannelInfo::getChannelCode,selectedChannelCode));

        PayMerchantChannelSite selectedPayMerchantChannelInfo = iPayMerchantChannelSiteService.getOne(new QueryWrapper<PayMerchantChannelSite>()
                .lambda()
                .eq(PayMerchantChannelSite::getMerchantId, merchantId)
                .eq(PayMerchantChannelSite::getProductCode,productCode)
                .eq(PayMerchantChannelSite::getPayType, CodeConst.PayType.RECEIVE_IN)
                .eq(PayMerchantChannelSite::getChannelCode,selectedChannelCode)
        );
        _log.info("支付产品:{},选中的通道 - {} {}",productCode,selectedChannelCode,selectedChannelInfo.getChannelName());

//        return new ReturnMsg();
        //TODO:升级为ACTIVEMQ 以防止同一秒接收到相同的订单 01/30/2024
        return accountManagerService.singleDeposit(
                selectedPayMerchantChannelInfo,
                selectedChannelInfo,
                merchantInfo,
                selectedChannelCode,
                merchantId,
                merchantWithdrawNo,
                amtFormat,
                clientIp,
                extraParam,
                merchantNotifyUrl,
                merchantChannelCode,
                coinType);

    }


    /**
     * 商户批量提现查询
     *
     * @param merchantId         商户号
     * @param merchantWithdrawNo 提现订单号
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/apiQueryDeposit", method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    public ReturnMsg apiQueryDeposit(
            @NotBlank(message = "商户编号不能为空") String merchantId,
            @NotBlank(message = "提现订单号不能为空") String merchantWithdrawNo,
            @NotBlank(message = "签名不能为空") String sign) throws Exception {

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("merchantId", merchantId);
        params.put("merchantWithdrawNo", merchantWithdrawNo);
        //params.put("sign", sign);

        try {
            // 获取商户信息
            PayMerchantInfo merchantInfo = infoService
                    .getOne(new QueryWrapper<PayMerchantInfo>().lambda().eq(PayMerchantInfo::getMerchantId, merchantId));
            if (merchantInfo.getStatus().equals(EnumMerchantStatusType.FAIL.getName())) {
                throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "商户禁用状态,请联系客服");
            }

            //验证签名
            if ("".equalsIgnoreCase(merchantInfo.getApiKey())){
                throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "商户未配置API密钥");
            }
            String apiKeyDecrypt = EncryptUtil.aesDecrypt(merchantInfo.getApiKey(),walletPrivateKeySalt); //商户的API KEY要解密
           // _log.info("apiKeyDecrypt- {}",apiKeyDecrypt);
            if (!sign.equals(Signature.getSign(params, apiKeyDecrypt))) {
                throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "签名有误~");
            }
            //查询订单状态
            PayDepositInfo withdrawInfo = iPayDepositInfoService.getOne(new QueryWrapper<PayDepositInfo>().lambda().eq(PayDepositInfo::getMerchantId, merchantId).eq(PayDepositInfo::getMerchantWithdrawNo, merchantWithdrawNo));
            if (withdrawInfo == null) {
                throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "订单不存在");
            }

            Map<String, Object> resultMap = new HashMap<String, Object>();
            resultMap.put("merchantId", merchantId);
            resultMap.put("merchantWithdrawNo", merchantWithdrawNo);
            resultMap.put("withdrawNo", withdrawInfo.getWithdrawNo());
            resultMap.put("withdrawStatus", withdrawInfo.getWithdrawStatus());
            resultMap.put("sign", Signature.getSign(resultMap, apiKeyDecrypt));
            return new ReturnMsg(resultMap);

        } catch (Exception e) {
            return new ReturnMsg(e.getMessage());
        }
    }


    /**
     * 查询余额
     * @param
     * @param
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/apiQueryBalance", method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    public ReturnMsg apiQueryBalance(
            @NotBlank(message = "商户编号不能为空") String merchantId,
            @NotBlank(message = "签名不能为空") String sign) throws Exception {

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("merchantId", merchantId);
        params.put("sign", sign);

        // 获取商户信息
        PayMerchantInfo merchantInfo = infoService.getOne(new QueryWrapper<PayMerchantInfo>().lambda().eq(PayMerchantInfo::getMerchantId, merchantId));

        if (merchantInfo == null) {
            throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "商户不存在");
        }
        if (merchantInfo.getStatus().equals(EnumMerchantStatusType.FAIL.getName())) {
            throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "商户禁用状态,请联系客服");
        }

        //验证签名
        if ("".equalsIgnoreCase(merchantInfo.getApiKey())){
            throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "商户未配置API密钥");
        }
        String apiKeyDecrypt = EncryptUtil.aesDecrypt(merchantInfo.getApiKey(),walletPrivateKeySalt); //商户的API KEY要解密
        if (!sign.equals(Signature.getSign(params, apiKeyDecrypt))) {
            throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "签名有误");
        }

        PayMerchantBalance balance = balanceService.getOne(new QueryWrapper<PayMerchantBalance>().lambda().eq(PayMerchantBalance::getMerchantId, merchantId));
        if (balance == null) {
            throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "商户未初始化余额");
        }
        Map<String, Object> resultMap = new HashMap<String, Object>();
       // resultMap.put("accountFee", balance.getAccountFee());
        resultMap.put("accountBalance", balance.getAccountBalance());
        resultMap.put("merchantId", balance.getMerchantId());
        resultMap.put("merchantName", merchantInfo.getMerchantName());
        resultMap.put("sign", Signature.getSign(resultMap, apiKeyDecrypt));
        return new ReturnMsg(resultMap);
    }

    //vue:payUrl -> java:getPayUrl
    @RequestMapping(value = "/verifyPayUrl", method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    public ReturnMsg getPayUrl(
            @NotBlank(message = "链接无效") String url,HttpServletRequest request) throws Exception {

        //url https://e-usdt.com/#/payUrl?id=VMDyQXDHHBotvRX7cArIMw%3D%3D
        if (url == null || "".equals(url)){
            throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "链接无效");
        }
        Object redisPayUrl = redisUtil.get(url);
        //已过期或者不存在
        if (redisPayUrl == null){
            throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "支付链接已过期");
        }

        String clientIp = IPUtility.getClientIp(request);//用户IP地址

        //1.此处更新用户的IP
        String merchantWithdrawNo = redisPayUrl.toString().split(":")[1];
        PayDepositInfo depositEntity = new PayDepositInfo();
        depositEntity.setClientIP(clientIp);
        iPayDepositInfoService.update(depositEntity,new UpdateWrapper<PayDepositInfo>().lambda()
                .eq(PayDepositInfo::getMerchantWithdrawNo, merchantWithdrawNo)
        );

        //2.IP地址是否在IpBlockList黑名单内
        IpBlockList ipBlockList =  iIpBlockListService.getOne(new QueryWrapper<IpBlockList>().lambda()
                .eq(IpBlockList::getClientIp, clientIp));

        if (ipBlockList != null) {
            throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "无权访问");
        }

        //3.新增用户IP
        IpList ipInfo =  iIpListService.getOne(new QueryWrapper<IpList>().lambda()
                .eq(IpList::getMerchantWithdrawNo, merchantWithdrawNo));

        if (ipInfo == null){
            IpList ipEntity = new IpList();
            ipEntity.setClientIp(clientIp);
            ipEntity.setMerchantWithdrawNo(merchantWithdrawNo);
            ipEntity.setCreateTime(new Date());
            iIpListService.save(ipEntity);
        }


        //收款钱包地址:商户单号:平台订单号:订单金额:优惠立减:实付金额:timestamp
        String payInfo = String.valueOf(redisPayUrl); //key redisPayUrl value timestamp
        Map<String,String> map = new HashMap<>();
        map.put("payInfo",payInfo);
        return new ReturnMsg(map);
      }

    @RequestMapping(value = "/payerConfirmPaid", method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    public ReturnMsg payerConfirmPaid(
            @NotBlank(message = "单号不能为空") String merchantWithdrawNo) throws Exception {
        if (merchantWithdrawNo == null || "".equals(merchantWithdrawNo)){
            throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "单号不能为空");
        }


        PayDepositInfo payDepositInfo = iPayDepositInfoService.getOne(new QueryWrapper<PayDepositInfo>().lambda()
                .eq(PayDepositInfo::getMerchantWithdrawNo, merchantWithdrawNo));

        if (payDepositInfo == null) {
            throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "订单不存在");
        }
        if (!payDepositInfo.getWithdrawStatus().equals(CodeConst.WithdrawStatusConst.WITHDRAW_UNKNOWN)){
            throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "订单非待确认状态");
        }
        CoinHolderDepositInfo holderDepositInfo = iCoinHolderDepositInfoService.getOne(new QueryWrapper<CoinHolderDepositInfo>().lambda()
                .eq(CoinHolderDepositInfo::getWithdrawNo, payDepositInfo.getWithdrawNo()));
        if (holderDepositInfo == null) {
            throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "订单不存在");
        }

        if (holderDepositInfo.getStatus().equals(CodeConst.WithdrawStatusConst.WITHDRAW_SUCCESS)){
            throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "订单已支付成功");
        }

        if (holderDepositInfo.getStatus().equals(CodeConst.WithdrawStatusConst.WITHDRAW_FAIL)){
            throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "订单已失败");
        }

        CoinHolderDepositInfo holderEntity = new CoinHolderDepositInfo();
        holderEntity.setRemark("付款人点击已完成付款按钮");
        holderEntity.setPreStatus(CodeConst.PreStatusConst.WITHDRAW_CONFIRM);//由收款人收到款项
        boolean isUpdHolder = iCoinHolderDepositInfoService.update(holderEntity,new UpdateWrapper<CoinHolderDepositInfo>().lambda()
                .eq(CoinHolderDepositInfo::getWithdrawNo, payDepositInfo.getWithdrawNo())); //保存更改
        if (!isUpdHolder){
            throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "三方订单更新失败");
        }


        Map<String,String> map = new HashMap<>();
        map.put("withdrawNo",holderEntity.getWithdrawNo());
        return new ReturnMsg(map);
    }

    @RequestMapping(value = "/payerConfirmFromAddr", method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    public ReturnMsg payerConfirmFromAddr(
            @NotBlank(message = "单号不能为空") String merchantWithdrawNo,String fromAddr) throws Exception {
        if (merchantWithdrawNo == null || "".equals(merchantWithdrawNo)){
            throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "单号不能为空");
        }


        PayDepositInfo payDepositInfo = iPayDepositInfoService.getOne(new QueryWrapper<PayDepositInfo>().lambda()
                .eq(PayDepositInfo::getMerchantWithdrawNo, merchantWithdrawNo));

        if (payDepositInfo == null) {
            throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "订单不存在");
        }

        CoinHolderDepositInfo holderEntity = new CoinHolderDepositInfo();
        holderEntity.setFromAddr(fromAddr);
        iCoinHolderDepositInfoService.update(holderEntity,new UpdateWrapper<CoinHolderDepositInfo>().lambda()
                .eq(CoinHolderDepositInfo::getWithdrawNo, payDepositInfo.getWithdrawNo())); //保存更改



        Map<String,String> map = new HashMap<>();
        map.put("withdrawNo",holderEntity.getWithdrawNo());
        return new ReturnMsg(map);
    }


    @RequestMapping(value = "/getServerTime", method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    public ReturnMsg getServerTime(HttpServletRequest request) throws Exception {
        String token = request.getHeader("authorization");
//        if (!authenticatorService.checkToken(token)) {
//            throw new TranException(CodeConst.PERMISSION_CODE, "无权访问");
//        }
        Map<String,String> map = new HashMap<>();
        map.put("serverTime",System.currentTimeMillis()+"");
        return new ReturnMsg(map);
    }

}
