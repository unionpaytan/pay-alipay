package com.bootpay.mng.controller;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.support.Property;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bootpay.channel.constants.DepositChannelCode;
import com.bootpay.channel.vo.ChannelResultVo;
import com.bootpay.common.authenticator.GoogleAuthenticator;
import com.bootpay.common.constants.CodeConst;
import com.bootpay.common.constants.CodeConst.GoogleConst;
import com.bootpay.common.constants.MyRedisConst;
import com.bootpay.common.constants.enums.*;
import com.bootpay.common.excetion.TranException;
import com.bootpay.common.framework.AvoidDuplicateFormToken;
import com.bootpay.common.framework.RedisUtil;
import com.bootpay.common.utils.*;
import com.bootpay.core.entity.*;
import com.bootpay.core.service.*;
import com.bootpay.mng.amqueue.AMProducerUtils;
import com.bootpay.mng.service.AuthenticatorService;
import com.bootpay.mng.service.MerchantAccountManagerService;
import com.bootpay.mng.service.NotifyService;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * <p>
 * 查询充值代收控制器
 * </p>
 *
 * @author 
 * @since 08/10/2021
 */
@RestController
@RequestMapping("/deposit")
public class DepositManagerController {

    private Logger _log = LoggerFactory.getLogger(DepositManagerController.class);

    @Value("${app.MERCHANT_ID}") //application.yml文件读取
    private String MERCHANT_ID;

    @Value("${app.MERCHANT_API_KEY}") //application.yml文件读取
    private String MERCHANT_API_KEY;

    @Autowired
    private IPayDepositInfoService iPayDepositInfoService;

    @Autowired
    private MerchantAccountManagerService accountManagerService;
    @Autowired
    private IWithdrawChannelInfoService channelInfoService;
    @Autowired
    private IPayMerchantInfoService iPayMerchantInfoService;
    
    @Autowired
    private IPayPlatformIncomeInfoService incomeInfoService;
    @Autowired
    private NotifyService notifyService;
    

    @Autowired
    private AuthenticatorService authenticatorService;

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

    @Value("${app.BBBTRE_NOTIFY_URL}")
    private String bbbTreNotifyUrl;

    @Autowired
    private IIpBlockListService iIpBlockListService;

    @Autowired
    private ICoinAlipayInfoService iCoinAlipayInfoService;

    /**
     * 获取提现订单数据
     *
     * @param merchantId
     * @param withdrawNo
     * @param
     * @param
     * @param withdrawStatus
     * @param merchantWithdrawNo
     * @param beginTime
     * @param endTime
     * @param channelCode
     * @param rows
     * @param page
     * @return
     */
    @RequestMapping(value = "/queryDepositPage", method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    public ReturnMsg queryDepositPage(
            String merchantId,
            String merchantName,
            String withdrawNo,
            String payerAddr,
            String payerName,
            String withdrawStatus,
            String merchantWithdrawNo,
            String beginTime,
            String endTime,
            String channelCode,
            String requestType,
            String minAmt,
            String maxAmt,
            @NotBlank(message = "行数不能为空") Integer rows,
            @NotBlank(message = "页码不能为空") Integer page,
            HttpServletRequest request) throws TranException {

        //管理员查询 商户不可查询全部订单
        if (merchantId == null || "".equals(merchantId)) {
            String token = request.getHeader("authorization");
            if (!authenticatorService.checkPermission(token).equals("admin")) {
                throw new TranException(CodeConst.PERMISSION_CODE, "无权访问");
            }
        }

        Page<PayDepositInfo> pages = new Page<>(page, rows);
        Map<Property<PayDepositInfo, ?>, Object> params = new HashMap<Property<PayDepositInfo, ?>, Object>();
        params.put(PayDepositInfo::getMerchantId, merchantId);
        params.put(PayDepositInfo::getMerchantName, merchantName);
        params.put(PayDepositInfo::getWithdrawNo, withdrawNo);
        params.put(PayDepositInfo::getPayerAddr, payerAddr);
        params.put(PayDepositInfo::getWithdrawStatus, withdrawStatus);
        params.put(PayDepositInfo::getMerchantWithdrawNo, merchantWithdrawNo);
        params.put(PayDepositInfo::getChannelCode, channelCode);
        params.put(PayDepositInfo::getRequestType, requestType);
        params.put(PayDepositInfo::getPayerName, payerName);
        LambdaQueryWrapper<PayDepositInfo> query = new QueryWrapper<PayDepositInfo>().orderByDesc("ID")
                .lambda()
                .eq(StringUtils.isBlank(beginTime) || StringUtils.isBlank(endTime), PayDepositInfo::getDate, DateUtil.getDate())
                .ge(StringUtils.isNotBlank(beginTime), PayDepositInfo::getDate, beginTime)
                .le(StringUtils.isNotBlank(endTime), PayDepositInfo::getDate, endTime)
                .ge(StringUtils.isNotBlank(minAmt), PayDepositInfo::getAmt, minAmt)
                .le(StringUtils.isNotBlank(maxAmt), PayDepositInfo::getAmt, maxAmt)
                .allEq(params, false);
        _log.info("收款订单列表查询条件{}", query.getSqlSegment());
        //查询订单
        IPage<PayDepositInfo> pageMap = iPayDepositInfoService.page(pages, query);
        //统计总交易金额
        Map<String, Object> sunCount = iPayDepositInfoService.selectCountSumAmt(query);
        //统计成功订单数
        query.eq(StringUtils.isBlank(withdrawStatus), PayDepositInfo::getWithdrawStatus, EnumWithdrawStatus.SUCCESS.getName());
        Map<String, Object> sumSuccessAmt = iPayDepositInfoService.selectSumAmt(query);

        if (sumSuccessAmt == null) {
            sumSuccessAmt = new HashMap<String, Object>();
            sumSuccessAmt.put("countSuccessAmt", "0.00"); //成功交易金额
            sumSuccessAmt.put("countSuccessNum", "0");
            sumSuccessAmt.put("countFee", "0.00");  //商户代付手续费
            sumSuccessAmt.put("countSuccessAgentProfit", "0.00"); //代理分成

        }
        if (sunCount == null) {
            sunCount = new HashMap<String, Object>();
            sunCount.put("countAmt", "0.00");
        }
        Map<String, Object> result = new HashMap<String, Object>();
        sumSuccessAmt.putAll(sunCount);
        result.put("pages", pageMap);
        result.put("count", sumSuccessAmt);
        return new ReturnMsg(result);
    }


    /**
     * 获取代理商的下游提现订单数据
     *
     * @param merchantId
     * @param withdrawNo
     * @param
     * @param
     * @param withdrawStatus
     * @param merchantWithdrawNo
     * @param beginTime
     * @param endTime
     * @param channelCode
     * @param rows
     * @param page
     * @return
     */
    @RequestMapping(value = "/queryAgentDepositPage", method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    public ReturnMsg queryAgentDepositPage(
            String parentId,//代理ID
            String merchantId,//代理名下的商户ID
            String withdrawNo,
            String payerAddr,
            String payerName,
            String withdrawStatus,
            String merchantWithdrawNo,
            String beginTime,
            String endTime,
            String channelCode,
            String requestType,
            String minAmt,
            String maxAmt,
            @NotBlank(message = "行数不能为空") Integer rows,
            @NotBlank(message = "页码不能为空") Integer page) throws Exception {

        _log.info("代理商下级商户提现代付订单查询{},{}", beginTime, endTime);

        //根据ParentId找到下级商户
        List<PayMerchantInfo> merchantInfoList = iPayMerchantInfoService.list(new QueryWrapper<PayMerchantInfo>().lambda().eq(PayMerchantInfo::getParentId, parentId));
        if (merchantInfoList.isEmpty()) {
            throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "代理名下还无下级商户");
        }

        List<String> list = new ArrayList<>();
        for (PayMerchantInfo info : merchantInfoList) {
            list.add(info.getMerchantId());
            List<PayMerchantInfo> agentList = iPayMerchantInfoService.list(new QueryWrapper<PayMerchantInfo>().lambda().eq(PayMerchantInfo::getParentId, info.getMerchantId()));
            if (agentList != null) {
                for (PayMerchantInfo info2 : agentList) {
                    list.add(info2.getMerchantId());
                }
            }
        }
        //M1574940921721550912
        _log.info("===>>>代付订单：代理:{},下级商户列表:{}", parentId, list);
        Page<PayDepositInfo> pages = new Page<>(page, rows);
        Map<Property<PayDepositInfo, ?>, Object> params = new HashMap<Property<PayDepositInfo, ?>, Object>();

        //params.put(PayDepositInfo :: getMerchantId, merchantId);
        //params.put(PayDepositInfo :: getMerchantId, merchantId);
        params.put(PayDepositInfo::getWithdrawNo, withdrawNo);
        params.put(PayDepositInfo::getPayerAddr, payerAddr);

        params.put(PayDepositInfo::getWithdrawStatus, withdrawStatus);
        params.put(PayDepositInfo::getMerchantWithdrawNo, merchantWithdrawNo);
        params.put(PayDepositInfo::getChannelCode, channelCode);
        params.put(PayDepositInfo::getRequestType, requestType);
        params.put(PayDepositInfo::getPayerName, payerName);
        LambdaQueryWrapper<PayDepositInfo> query = new QueryWrapper<PayDepositInfo>().orderByDesc("ID")
                .lambda()
                .in(PayDepositInfo::getMerchantId, list)
                .eq(StringUtils.isNotBlank(merchantId), PayDepositInfo::getMerchantId, merchantId)
                .eq(StringUtils.isBlank(beginTime) || StringUtils.isBlank(endTime), PayDepositInfo::getDate, DateUtil.getDate())
                .ge(StringUtils.isNotBlank(beginTime), PayDepositInfo::getDate, beginTime)
                .le(StringUtils.isNotBlank(endTime), PayDepositInfo::getDate, endTime).ge(StringUtils.isNotBlank(minAmt), PayDepositInfo::getAmt, minAmt).le(StringUtils.isNotBlank(maxAmt), PayDepositInfo::getAmt, maxAmt)
                .allEq(params, false);
        _log.info("提现订单列表查询条件{}", query.getSqlSegment());
        //查询代付订单
        IPage<PayDepositInfo> pageMap = iPayDepositInfoService.page(pages, query);
        //统计代量总交易金额
        Map<String, Object> sumCount = iPayDepositInfoService.selectCountAgentSumAmt(query);
        //统计代理成功订单数
        query.eq(StringUtils.isBlank(withdrawStatus), PayDepositInfo::getWithdrawStatus, EnumWithdrawStatus.SUCCESS.getName());
        Map<String, Object> sumSuccessAmt = iPayDepositInfoService.selectAgentSumAmt(query);

        if (sumSuccessAmt == null) {
            sumSuccessAmt = new HashMap<String, Object>();
            sumSuccessAmt.put("countSuccessAmt", "0.00");
            sumSuccessAmt.put("countSuccessNum", "0");
            sumSuccessAmt.put("countFee", "0.00");
            sumSuccessAmt.put("countAgentProfit", "0.00"); //代理分成
        }
        if (sumCount == null) {
            sumCount = new HashMap<String, Object>();
            sumCount.put("countAmt", "0.00");
        }
        Map<String, Object> result = new HashMap<String, Object>();
        sumSuccessAmt.putAll(sumCount);
        result.put("pages", pageMap);
        result.put("count", sumSuccessAmt);
        return new ReturnMsg(result);
    }


    /**
     * 补充通知
     *
     * @param withdrawNo
     * @return
     * @throws Exception
     * @todo: 新增定时器 补充5次通知
     */
    @RequestMapping(value = "/noticeDeposit", method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    public ReturnMsg noticeDeposit(@NotBlank(message = "平台订单号") String withdrawNo) throws Exception {
        PayDepositInfo depositInfo = iPayDepositInfoService.getOne(new QueryWrapper<PayDepositInfo>().lambda().eq(PayDepositInfo::getWithdrawNo, withdrawNo));
        if (StringUtils.isBlank(depositInfo.getMerchantNotifyUrl())) {
            throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "商户回调地址为空");
        }
        notifyService.notifyMerchantDeposit(depositInfo, depositInfo.getWithdrawStatus());
        return new ReturnMsg();
    }

    /**
     * IP拉黑
     * */
    @RequestMapping(value = "/blockIp", method = {RequestMethod.POST, RequestMethod.GET},name = "admin")
    @ResponseBody
    public ReturnMsg blockIp(String merchantWithdrawNo,String clientIp) throws Exception {

        IpBlockList ipBlockList =  iIpBlockListService.getOne(new QueryWrapper<IpBlockList>().lambda()
                .eq(IpBlockList::getClientIp, clientIp));
        if (ipBlockList != null) {
            throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "IP已在黑名单");
        }

        IpBlockList ipEntity = new IpBlockList();
        ipEntity.setMerchantWithdrawNo(merchantWithdrawNo);
        ipEntity.setClientIp(clientIp);
        ipEntity.setCreateTime(new Date());
        iIpBlockListService.save(ipEntity);

        return new ReturnMsg();
    }

    /**
     * 移除黑名单
     * */
    @RequestMapping(value = "/removeBlockIp", method = {RequestMethod.POST, RequestMethod.GET},name = "admin")
    @ResponseBody
    public ReturnMsg removeBlockIp(String merchantWithdrawNo,String clientIp) throws Exception {

        IpBlockList ipBlockList =  iIpBlockListService.getOne(new QueryWrapper<IpBlockList>().lambda()
                .eq(IpBlockList::getClientIp, clientIp));
        if (ipBlockList == null) {
            throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "IP不存在");
        }

        iIpBlockListService.remove(new QueryWrapper<IpBlockList>().lambda().eq(IpBlockList::getClientIp, clientIp));

        return new ReturnMsg();
    }

    @RequestMapping(value = "/queryIpBlockList", method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    public ReturnMsg queryIpBlockList(
            String clientIp,
            String merchantWithdrawNo,
            String beginTime,
            String endTime,
            @NotBlank(message = "行数不能为空") Integer rows,
            @NotBlank(message = "页码不能为空") Integer page) throws Exception {

        Page<IpBlockList> pages = new Page<>(page, rows);
        Map<Property<IpBlockList, ?>, Object> params = new HashMap<Property<IpBlockList, ?>, Object>();
        params.put(IpBlockList::getClientIp, clientIp);
        params.put(IpBlockList::getMerchantWithdrawNo, merchantWithdrawNo);

        LambdaQueryWrapper<IpBlockList> query = new QueryWrapper<IpBlockList>().orderByDesc("ID")
                .lambda()
                .eq(StringUtils.isNotBlank(clientIp), IpBlockList::getClientIp, clientIp)
                .eq(StringUtils.isNotBlank(merchantWithdrawNo), IpBlockList::getMerchantWithdrawNo, merchantWithdrawNo)
                .ge(StringUtils.isNotBlank(beginTime), IpBlockList::getCreateTime, beginTime + " 00:00:00")
                .le(StringUtils.isNotBlank(endTime), IpBlockList::getCreateTime, endTime + "23:59:59")
                .allEq(params, false);
        //查询订单
        IPage<IpBlockList> pageMap = iIpBlockListService.page(pages, query);
        Map<String, Object> result = new HashMap<String, Object>();
        result.put("pages", pageMap);
        return new ReturnMsg(result);

    }


    /**
     * 商户收款 web代收
     *
     * @param
     * @param
     * @param
     * @return
     * @throws Exception
     * let batchList = {
     *         amtRmb: this.amtRmb,
     *         usdtLive: this.usdtLive,
     *         amtUsdt: this.amtUsdt,
     *         amtBonus: this.amtBouns,
     *         amt: (this.amtUsdt - this.amtBouns).toFixed(4),
     *       };
     */
    @RequestMapping(value = "/merchantDeposit", method = {RequestMethod.POST})
    @ResponseBody
    @AvoidDuplicateFormToken
    @Transactional(rollbackFor = Exception.class)
    public ReturnMsg merchantDeposit(
            @NotBlank(message = "商户编号不能为空") String merchantId,
            String coinType,
            String merchantChannelCode,
            String merchantWithdrawNo,
            String merchantNotifyUrl,
            String merchantReturnUrl,
            @NotBlank(message = "代收数据不能为空") String batchList) throws Exception {
//        SimpleDateFormat dateFormatLocal = new SimpleDateFormat("yyyy-MMM-dd HH:mm:ss");
        SimpleDateFormat dateFormatGmt = new SimpleDateFormat("yyyy-MMM-dd HH:mm:ss");
        dateFormatGmt.setTimeZone(TimeZone.getTimeZone("GMT"));

        if (merchantId == null){
            throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "无此商户号");
        }

        if (merchantWithdrawNo == null){
            throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "商户订单号为空");
        }
        if (merchantChannelCode == null){
            throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "商户通道号为空");
        }

        if (batchList == null) {
            throw new TranException(CodeConst.SYSTEM_ERROR_CODE, "代收数据不允许为空");
        }
        //前端VUE传送过来的了batchList===>>>>>>
        String HTTP_URL = "https://30pay.info/api";
        PayDepositInfo payDepositInfo = (PayDepositInfo) JSON.parseObject(batchList,PayDepositInfo.class);
        _log.warn("商户:{},订单信息:{}", payDepositInfo.getMerchantId(), payDepositInfo);


        String DEPOSIT_URL = HTTP_URL + "/deposit/apiDeposit";

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("merchantId",  MERCHANT_ID);// 商户号
        params.put("merchantWithdrawNo", merchantWithdrawNo);// 订单号W888666001 System.currentTimeMillis()+
        params.put("coinType","1");// "0"-微信 “1”-支付宝,"2"-聚合码
        params.put("amt",payDepositInfo.getAmt());//精度为二位小数数字 若整数请填写 10.00
        params.put("merchantChannelCode",merchantChannelCode);
        params.put("merchantNotifyUrl", HTTP_URL + "/notify/localNotifyUrl"); //URL不为空则参与验签
        params.put("extraParam","收银台");
        params.put("sign", Signature.getSign(params, MERCHANT_API_KEY));

        String result = HttpClient4Utils.sendHttpRequest(DEPOSIT_URL, params, "utf-8", true);

        System.out.println("代收提交---》" + params);
        System.out.println("代收返回参数---》" + result);

        JSONObject json = JSONObject.parseObject(result);

        if (!"0000".equals(json.getString("code"))) {
            System.out.println("代收返回参数---》" + json.getString("message"));
            return new ReturnMsg().setFail(json.getString("message"));
        } else {
            JSONObject dataJson = json.getJSONObject("data");
            if (!Signature.checkIsSignValidFromMap(dataJson, MERCHANT_API_KEY)) {
                System.out.println("签名验证失败");
                return new ReturnMsg().setFail("签名验证失败");
            } else {
                System.out.println("签名验证成功");
                String payerUrl = dataJson.getString("payerUrl");
                Map<String, Object> resultMap = new HashMap<String, Object>();
                resultMap.put("payerUrl",payerUrl);
                return new ReturnMsg(resultMap);

            }
        }

    }

    //此功能用于前台界面显示用  5分钟内不断刷新监控 ，若支付成功，则页面上显示支付成功
    @RequestMapping(value = "/monitorSuccess", method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    public ReturnMsg monitorSuccess(@NotBlank(message = "平台订单号") String withdrawNo) throws Exception {
//        PayDepositInfo depositInfo = iPayDepositInfoService.getOne(new QueryWrapper<PayDepositInfo>().lambda().eq(PayDepositInfo::getWithdrawNo, withdrawNo));
//        if (depositInfo == null) {
//            throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "平台订单号地址为空");
//        }
        if (withdrawNo == null){
            throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "订单号为空");
        }
        String redis_success = withdrawNo + MyRedisConst.SPLICE + "success";
        Object redisObj = redisUtil.get(redis_success);
        //已过期或者不存在
        if (redisObj == null){
            throw new TranException(CodeConst.BUSINESS_ERROR_CODE, withdrawNo + " 订单未成功或者不存在");
        }
        Map<String, Object> resultMap = new HashMap<String, Object>();
        resultMap.put("withdrawStatus",CodeConst.WithdrawStatusConst.WITHDRAW_SUCCESS);//订单成功支付状态
        return new ReturnMsg(resultMap);
    }

}
