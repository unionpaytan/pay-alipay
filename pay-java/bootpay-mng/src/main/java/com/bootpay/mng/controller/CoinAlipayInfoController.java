package com.bootpay.mng.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bootpay.common.authenticator.GoogleAuthenticator;
import com.bootpay.common.constants.CodeConst;
import com.bootpay.common.constants.enums.EnumAccountType;
import com.bootpay.common.constants.enums.EnumAgentRate;
import com.bootpay.common.excetion.TranException;
import com.bootpay.common.framework.RedisUtil;
import com.bootpay.common.utils.*;
import com.bootpay.core.entity.*;
import com.bootpay.core.service.*;
import com.bootpay.mng.service.AuthenticatorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

/**
 * <p>
 * 卡主账户管理
 * </p>
 *
 * @author 
 * @since 2021-06-03
 */
@Validated
@RestController
@RequestMapping("/AlipayAccountManage")
public class CoinAlipayInfoController {

    private static Logger _log = LoggerFactory.getLogger(CoinAlipayInfoController.class);

    @Value("${app.walletPrivateKeySalt}") //application.yml文件读取
    private String walletPrivateKeySalt;

    @Value("${app.nodeTokenDomain}") //application.yml文件读取
    private String nodeTokenDomain;


    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private AuthenticatorService authenticatorService;

    @Autowired
    private IPayMerchantInfoService iPayMerchantInfoService;

    @Autowired
    private IPayAgentBalanceService iPayAgentBalanceService;

    @Autowired
    private IPayAgentBalanceFlowService iPayAgentBalanceFlowService;

    @Autowired
    private ICoinHolderInfoService iCoinHolderInfoService;
    @Autowired
    private ICoinAlipayInfoService iCoinAlipayInfoService;



    @RequestMapping(value = "/addAlipayInfo" ,method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    public ReturnMsg addAlipayInfo(
            String channelCode,
            String channelName,
            String merchantId,
            String payerMerchantId,
            String payerOperatorId,
            String alipayName,
            String remark,
            String tableMerchantId,
            String googleCode
    ) throws Exception {

        String newChannelCode = (channelCode == null || channelCode.isEmpty()) ?  "U1713775996149215734" : channelCode;
        String newChannelName = (channelName == null || channelName.isEmpty()) ? "三方淘宝周转码" : channelName;
        PayMerchantInfo merchantInfo = iPayMerchantInfoService.getOne(new QueryWrapper<PayMerchantInfo>().lambda().eq(PayMerchantInfo::getMerchantId, tableMerchantId));

        String googleKeyDecode =  EncryptUtil.aesDecrypt(merchantInfo.getGoogleKey(),walletPrivateKeySalt);
        if(!GoogleAuthenticator.check_code(googleKeyDecode, googleCode, System.currentTimeMillis())){
            throw new TranException(CodeConst.BUSINESS_ERROR_CODE,"google验证码错误");
        }

        if (merchantInfo.getAccountType().equals(EnumAccountType.MERCHANT.getName())) {
            if (merchantInfo.getAgentRate().equals(EnumAgentRate.OPERATOR.getName())) {
//                PayMerchantInfo parentMerchantInfo = iPayMerchantInfoService.getOne(new QueryWrapper<PayMerchantInfo>().lambda().eq(PayMerchantInfo::getParentId, tableMerchantId));
                payerMerchantId = merchantInfo.getParentId();
            }

        }

        CoinAlipayInfo info = iCoinAlipayInfoService.getOne(new QueryWrapper<CoinAlipayInfo>()
                .lambda()
                .eq(CoinAlipayInfo::getAlipayName, alipayName.trim()));

        if (info != null) {
            throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "账号重复");
        }


        String uuid = MySeq.getUUID();
        CoinAlipayInfo alipayInfo = new CoinAlipayInfo();
        alipayInfo.setUuid(uuid);//唯一ID
        alipayInfo.setChannelName(newChannelName);
        alipayInfo.setChannelCode(newChannelCode);
        alipayInfo.setMerchantId(payerMerchantId);
        alipayInfo.setPayerMerchantId(payerMerchantId); //码商
        alipayInfo.setPayerOperatorId(payerOperatorId);//码商操作员
        alipayInfo.setAlipayName(alipayName); //支付宝名称
        alipayInfo.setStatus(CodeConst.AlipayStatus.OFFLINE);
        alipayInfo.setIsBind(CodeConst.AlipayIsBindStatus.NO);
        alipayInfo.setRemark(HtmlUtil.delHTMLTag(remark));
        alipayInfo.setCreateTime(new Date());
        alipayInfo.setDate(DateUtil.getDate());
        iCoinAlipayInfoService.save(alipayInfo);

        //记录操作日志
        _log.info("操作员:{},操作时间:{},操作功能:{},{}", tableMerchantId, new Date(), "新增支付宝商家:", alipayName);

        return new ReturnMsg();
    }


    @RequestMapping(value = "/updateAlipayInfo" ,method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    public ReturnMsg updateAlipayInfo(
            String id,
            String channelCode,
            String channelName,
            String merchantId,
            String payerMerchantId,
            String payerOperatorId,
            String alipayName,
            String remark,
            String tableMerchantId,
            String googleCode
    ) throws Exception {

        String newChannelCode = (channelCode == null || channelCode.isEmpty()) ?  "U1713775996149215734" : channelCode;
        String newChannelName = (channelName == null || channelName.isEmpty()) ? "三方淘宝周转码" : channelName;
        PayMerchantInfo merchantInfo = iPayMerchantInfoService.getOne(new QueryWrapper<PayMerchantInfo>().lambda().eq(PayMerchantInfo::getMerchantId, tableMerchantId));

        String googleKeyDecode =  EncryptUtil.aesDecrypt(merchantInfo.getGoogleKey(),walletPrivateKeySalt);
        if(!GoogleAuthenticator.check_code(googleKeyDecode, googleCode, System.currentTimeMillis())){
            throw new TranException(CodeConst.BUSINESS_ERROR_CODE,"google验证码错误");
        }

//        CoinAlipayInfo info = iCoinAlipayInfoService.getOne(new QueryWrapper<CoinAlipayInfo>()
//                .lambda()
//                .eq(CoinAlipayInfo::getAlipayName, alipayName.trim()));
//
//        if (info != null) {
//            throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "账号重复");
//        }


        CoinAlipayInfo alipayInfo = new CoinAlipayInfo();
//        alipayInfo.setChannelName(newChannelName);
//        alipayInfo.setChannelCode(newChannelCode);
        alipayInfo.setMerchantId(payerMerchantId);
        alipayInfo.setPayerMerchantId(payerMerchantId); //码商
        alipayInfo.setPayerOperatorId(payerOperatorId);//码商操作员
        alipayInfo.setAlipayName(alipayName); //支付宝名称
//        alipayInfo.setStatus(CodeConst.AlipayStatus.OFFLINE);
//        alipayInfo.setIsBind(CodeConst.AlipayIsBindStatus.NO);
        alipayInfo.setRemark(HtmlUtil.delHTMLTag(remark));
//        alipayInfo.setCreateTime(new Date());
//        alipayInfo.setDate(DateUtil.getDate());
        iCoinAlipayInfoService.update(alipayInfo,new UpdateWrapper<CoinAlipayInfo>().lambda()
                .eq(CoinAlipayInfo::getId, id));
        return new ReturnMsg();
    }

    @RequestMapping(value = "/updateAlipayInfoStatus" ,method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    public ReturnMsg updateAlipayInfoStatus(
            String id,
            String status,
            String merchantId,
            String tableMerchantId
    ) throws Exception {

        PayMerchantInfo merchantInfo = iPayMerchantInfoService.getOne(new QueryWrapper<PayMerchantInfo>().lambda().eq(PayMerchantInfo::getMerchantId, tableMerchantId));

        if (merchantInfo.getAccountType().equals(EnumAccountType.MERCHANT.getName())) {
            if (merchantId == null || "".equals(merchantId)){
                throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "商户权限不足");
            }
        }
        //只改属于自己码商的状态
        CoinAlipayInfo info = iCoinAlipayInfoService.getOne(new QueryWrapper<CoinAlipayInfo>()
                .lambda()
                .eq(CoinAlipayInfo::getMerchantId,merchantId)
                .eq(CoinAlipayInfo::getId, id));

        if (info == null) {
            throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "商家不存在");
        }

        CoinAlipayInfo alipayInfo = new CoinAlipayInfo();
        alipayInfo.setStatus(status);
        iCoinAlipayInfoService.update(alipayInfo,new UpdateWrapper<CoinAlipayInfo>().lambda()
                .eq(CoinAlipayInfo::getId, id));

        return new ReturnMsg();
    }

    @RequestMapping(value = "/delAlipay" ,method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    public ReturnMsg delAlipay(
            String id,
            String merchantId,
            String payerMerchantId,
            String payerOperatorId,
            String tableMerchantId,
            String googleCode
    ) throws Exception {

        if(googleCode == null || "".equals(googleCode)){
            throw new TranException(CodeConst.BUSINESS_ERROR_CODE,"google验证码错误");
        }

        PayMerchantInfo merchantInfo = iPayMerchantInfoService.getOne(new QueryWrapper<PayMerchantInfo>().lambda().eq(PayMerchantInfo::getMerchantId, tableMerchantId));
        if(merchantInfo.getAccountType().equals(EnumAccountType.MERCHANT.getName())){
            if ("".equals(payerMerchantId) && EnumAgentRate.CARD.getName().equals(merchantInfo.getAgentRate())) {//码商等级
                throw new TranException(CodeConst.BUSINESS_ERROR_CODE,"码商权限不足");
            }
            if ("".equals(payerOperatorId) && EnumAgentRate.OPERATOR.getName().equals(merchantInfo.getAgentRate())) { //操作员等级
                throw new TranException(CodeConst.BUSINESS_ERROR_CODE,"操作员权限不足");
            }
        }
        String googleKeyDecode =  EncryptUtil.aesDecrypt(merchantInfo.getGoogleKey(),walletPrivateKeySalt);
        if(!GoogleAuthenticator.check_code(googleKeyDecode, googleCode, System.currentTimeMillis())){
            throw new TranException(CodeConst.BUSINESS_ERROR_CODE,"google验证码错误");
        }
        //只改属于自己码商的状态
        CoinAlipayInfo info = iCoinAlipayInfoService.getOne(new QueryWrapper<CoinAlipayInfo>()
                .lambda()
                .eq(CoinAlipayInfo::getMerchantId,payerMerchantId)
                .eq(CoinAlipayInfo::getId, id));

        if (info == null) {
            throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "支付宝商家不存在");
        }


        iCoinAlipayInfoService.remove(new QueryWrapper<CoinAlipayInfo>().lambda().eq(CoinAlipayInfo::getId, id));
        return new ReturnMsg();
    }

    @RequestMapping(value = "/unbindAlipay" ,method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    public ReturnMsg unbindAlipay(
            String id,
            String merchantId,
            String payerMerchantId,
            String payerOperatorId,
            String tableMerchantId
    ) throws Exception {


        PayMerchantInfo merchantInfo = iPayMerchantInfoService.getOne(new QueryWrapper<PayMerchantInfo>().lambda().eq(PayMerchantInfo::getMerchantId, tableMerchantId));

        if(merchantInfo.getAccountType().equals(EnumAccountType.MERCHANT.getName())){
            if ("".equals(payerMerchantId) && EnumAgentRate.CARD.getName().equals(merchantInfo.getAgentRate())) {//码商等级
                throw new TranException(CodeConst.BUSINESS_ERROR_CODE,"码商权限不足");
            }
            if ("".equals(payerOperatorId) && EnumAgentRate.OPERATOR.getName().equals(merchantInfo.getAgentRate())) { //操作员等级
                throw new TranException(CodeConst.BUSINESS_ERROR_CODE,"操作员权限不足");
            }
        }

        //只改属于自己码商的状态
        CoinAlipayInfo info = iCoinAlipayInfoService.getOne(new QueryWrapper<CoinAlipayInfo>()
                .lambda()
                .eq(CoinAlipayInfo::getMerchantId,payerMerchantId)
                .eq(CoinAlipayInfo::getId, id));

        if (info == null) {
            throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "支付宝商家不存在");
        }
        CoinAlipayInfo alipayInfo = new CoinAlipayInfo();
        alipayInfo.setIsBind(CodeConst.AlipayIsBindStatus.NO);
        iCoinAlipayInfoService.update(alipayInfo,new UpdateWrapper<CoinAlipayInfo>().lambda()
                .eq(CoinAlipayInfo::getId, id));//支付宝商家ID

        return new ReturnMsg();
    }

    @RequestMapping(value = "/unbindCoinHolder" ,method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    public ReturnMsg unbindCoinHolder(
            String id,
            String merchantId,
            String payerMerchantId,
            String payerOperatorId,
            String tableMerchantId
    ) throws Exception {


        PayMerchantInfo merchantInfo = iPayMerchantInfoService.getOne(new QueryWrapper<PayMerchantInfo>().lambda().eq(PayMerchantInfo::getMerchantId, tableMerchantId));

        if(merchantInfo.getAccountType().equals(EnumAccountType.MERCHANT.getName())){
            if ("".equals(payerMerchantId) && EnumAgentRate.CARD.getName().equals(merchantInfo.getAgentRate())) {//码商等级
                throw new TranException(CodeConst.BUSINESS_ERROR_CODE,"码商权限不足");
            }
            if ("".equals(payerOperatorId) && EnumAgentRate.OPERATOR.getName().equals(merchantInfo.getAgentRate())) { //操作员等级
                throw new TranException(CodeConst.BUSINESS_ERROR_CODE,"操作员权限不足");
            }
        }

        //只改属于自己码商的状态
        CoinHolderInfo info = iCoinHolderInfoService.getOne(new QueryWrapper<CoinHolderInfo>()
                .lambda()
                .eq(CoinHolderInfo::getMerchantId,payerMerchantId)
                .eq(CoinHolderInfo::getId, id));

        if (info == null) {
            throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "收款码不存在");
        }
        CoinHolderInfo coinHolderInfo = new CoinHolderInfo();
        coinHolderInfo.setIsBind(CodeConst.AlipayIsBindStatus.NO);
        iCoinHolderInfoService.update(coinHolderInfo,new UpdateWrapper<CoinHolderInfo>().lambda()
                .eq(CoinHolderInfo::getId, id));//支付宝商家ID

        return new ReturnMsg();
    }


    @RequestMapping(value = "/queryAlipayInfo",method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    public ReturnMsg queryAlipayInfo(
            String merchantId,
            String payerMerchantId,
            String payerOperatorId,
            String alipayName,
            String alipayUid,
            String status, //是否启用
            String isBind, //是否登录绑定
            String tableMerchantId,
            Integer page, Integer rows) throws TranException {

        PayMerchantInfo merchantInfo = iPayMerchantInfoService.getOne(new QueryWrapper<PayMerchantInfo>().lambda().eq(PayMerchantInfo :: getMerchantId, tableMerchantId));

        if(merchantInfo.getAccountType().equals(EnumAccountType.MERCHANT.getName())){
            if ((payerMerchantId == null || "".equals(payerMerchantId)) && EnumAgentRate.CARD.getName().equals(merchantInfo.getAgentRate())) {//码商等级
                throw new TranException(CodeConst.BUSINESS_ERROR_CODE,"码商权限不足");
            }
            if ((payerOperatorId == null || "".equals(payerOperatorId)) && EnumAgentRate.OPERATOR.getName().equals(merchantInfo.getAgentRate())) { //操作员等级
                throw new TranException(CodeConst.BUSINESS_ERROR_CODE,"操作员权限不足");
            }
        }
        //queryCardUserList
        Page<Map<String, Object>> iPage = new Page<>(page, rows);
        Map<String,Object> params = new HashMap<String,Object>();
        params.put("merchantId", merchantId);//码商ID
        params.put("payerMerchantId", payerMerchantId);//码商ID
        params.put("payerOperatorId", payerOperatorId);//码商
        params.put("alipayName", alipayName);//支付宝是否启用
        params.put("alipayUid", alipayUid);//支付宝是否启用
        params.put("status", status);//支付宝是否启用
        params.put("isBind", isBind);//支付宝已登录绑定否
        IPage<Map<String, Object>> result = iCoinAlipayInfoService.queryCoinAlipayInfoPage(iPage,params);
        Integer countTotal = iCoinAlipayInfoService.selectCountAlipay(params);

        Map<String,Object> resultMap = new HashMap<String,Object>();
        resultMap.put("data",result);
        resultMap.put("countTotal",countTotal);
        return new ReturnMsg(resultMap);
    }


    //支付宝信息列表
    @RequestMapping(value="/queryCoinAlipayInfoList",method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    public ReturnMsg queryCoinAlipayInfoList(
            String payerMerchantId,//码商
            String payerOperatorId,//操作员
            String tableMerchantId) throws TranException{
        //查询某一个代理
        if (tableMerchantId == null || "".equals(tableMerchantId)){
            throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "商户号不能为空");
        }

        PayMerchantInfo merchantInfo = iPayMerchantInfoService.getOne(new QueryWrapper<PayMerchantInfo>().lambda().eq(PayMerchantInfo :: getMerchantId, tableMerchantId));
        if(merchantInfo.getAccountType().equals(EnumAccountType.MERCHANT.getName())){
            if ((payerMerchantId == null || "".equals(payerMerchantId)) && EnumAgentRate.CARD.getName().equals(merchantInfo.getAgentRate())) {//码商等级
                throw new TranException(CodeConst.BUSINESS_ERROR_CODE,"码商权限不足");
            }
            if ((payerOperatorId == null || "".equals(payerOperatorId)) && EnumAgentRate.OPERATOR.getName().equals(merchantInfo.getAgentRate())) { //操作员等级
                throw new TranException(CodeConst.BUSINESS_ERROR_CODE,"操作员权限不足");
            }
        }

        Map<String,Object> params = new HashMap<String,Object>();
        params.put("payerMerchantId", payerMerchantId); //码商
        params.put("payerOperatorId", payerOperatorId);//码商操作员
        List<CoinAlipayInfo> coinAlipayInfoList = iCoinAlipayInfoService.queryCoinAlipayInfoList(params);
        return new ReturnMsg(coinAlipayInfoList);
    }


    //receive-cookie-deprecation=1;rtk=HGQZUWqD+OpE8XsBzGjgwcjo3xiHhNMDjKPuBQJgLXFYhNoczjF;ALI_PAMIR_SID=\"U59kSZeistpHVe7/w9dCuMQ/TU5#GUqwF5arS2GtoVR4KSlnbTU5\";zone=GZ00C;spanner=e9m86l/LU8O9staDlqYS0CpaIL3Gz96eXt2T4qEYgj0=;session.cookieNameId=ALIPAYJSESSIONID;ALIPAYJSESSIONID=RZ42fo88Kad3LLWPLtM65eCYhB98EjauthRZ55GZ00;auth_jwt=e30.eyJleHAiOjE3MTcyMjQ4MDM1NTIsInJsIjoiNSwwLDI3LDE5LDI4LDMwLDEzLDEwIiwic2N0IjoieFdFK3lCcHgyKzFYWUlWcStkNVgyZ1lGTHhtaXdsTlYyNTcyYjA4IiwidWlkIjoiMjA4ODUwMjYxMzM0ODU5MiJ9.MFRQX-uoUYCpyNkiStERYhFEKbYelBbtPDqljI8s8hk;__TRACERT_COOKIE_bucUserId=2088502613348592;ali_apache_tracktmp=\"uid=2088502613348592\";riskOriginalAccountMobileSendTime=-1;alipay=\"K1iSL1z0CjxduvqNQj7V1PGGaahEIdFkigK1vyfBJzjrJvtdjXZULFk=\";iw.userid=\"K1iSL1z0CjxduvqNQj7V1A==\";LoginForm=alipay_login_home;ctoken=tepwH3vr6GFqwxcS;riskCredibleMobileSendTime=-1;auth_goto_http_type=https;riskMobileCreditSendTime=-1;ctuMobileSendTime=-1;riskMobileAccoutSendTime=-1;mobileSendTime=-1;cna=6LDhHv/oYU4CAa+wEvZN8oxA;riskMobileBankSendTime=-1;credibleMobileSendTime=-1;CLUB_ALIPAY_COM=2088502613348592;

    //person:   payerAddr
    //business: uuid
    @RequestMapping(value = "/monitorToken", method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    public ReturnMsg monitorToken(String id, String businessType,HttpServletRequest request) throws Exception {
//        PayDepositInfo depositInfo = iPayDepositInfoService.getOne(new QueryWrapper<PayDepositInfo>().lambda().eq(PayDepositInfo::getWithdrawNo, withdrawNo));
//        if (depositInfo == null) {
//            throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "平台订单号地址为空");
//        }
        String token = request.getHeader("authorization");
        if (!authenticatorService.checkToken(token)) {
            throw new TranException(CodeConst.PERMISSION_CODE, "无权访问");
        }
        if (id == null){
            throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "ID为空");
        }
//        String cookieId = "alipay:cookie:" + id;
//        Object redisObj = redisUtil.get(cookieId);
        //String nodeTokenDomain = "http://localhost:3000"; //国内域名备案前 只能用IP http://120.78.3.140/node/ 反向代理到本地 node服务器 3000
        String targetUrl = nodeTokenDomain  + "/getAlipayToken";
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("id", id);
        params.put("token", token);
        String jsoinString = JSON.toJSONString(params);
        String resServerCookie = HttpClient4Utils.postJSON(targetUrl,jsoinString);
        //_log.info("resServer Cookie - {}",resServerCookie);
        String isBind = CodeConst.AlipayIsBindStatus.NO;
        //已过期或者不存在
        if (resServerCookie == null){
            throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "id:"+id+" 未取得alipay token");
        }else {
            JSONObject json = JSONObject.parseObject(resServerCookie);
            if (json == null) {
                throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "id:"+id+" 未取得alipay token");
            }
            if (!"0000".equals(json.getString("code"))) {
                throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "id:"+id+" 未取得alipay token:" + json.getString("code"));
            }
            String cookieString = String.valueOf(json.getString("cookie"));
            if(!"-1".equals(cookieString)) {
                //找到cookie
                Map<String, String> cookieMap = parseCookies(cookieString);
                String ctoken = cookieMap.get("ctoken");
                String uid = cookieMap.get("CLUB_ALIPAY_COM");

                //找出 uid的，如果有存在，则检查原有的 id，是否与传递过来的 id一致，若一致则更新，若不相同提示已绑定过
                if (businessType.equals(CodeConst.BusinessType.BUSINESS)) {

                    CoinAlipayInfo info = iCoinAlipayInfoService.getOne(new QueryWrapper<CoinAlipayInfo>().lambda().eq(CoinAlipayInfo::getAlipayUid, uid));
                    if (info != null) {
                        if (!String.valueOf(info.getUuid()).equals(id)) {
//                        return new ReturnMsg().setFail("UID:" + info.getAlipayUid() + "已绑定在商家" + info.getAlipayName());
                            throw new TranException(CodeConst.SYSTEM_ERROR_CODE, "UID:" + info.getAlipayUid() + "已绑定在商家" + info.getAlipayName());
                        }
                    }
                    //更新coinaplipayinfo表
                    CoinAlipayInfo alipayInfo = new CoinAlipayInfo();
                    alipayInfo.setAlipayUid(uid);
                    alipayInfo.setAlipayCtoken(ctoken);
                    alipayInfo.setAlipayCookie(String.valueOf(cookieString));
                    alipayInfo.setStatus(CodeConst.AlipayStatus.ONLINE);//绑定成功后则上线;
                    alipayInfo.setIsBind(CodeConst.AlipayIsBindStatus.YES);
                    iCoinAlipayInfoService.update(alipayInfo,new UpdateWrapper<CoinAlipayInfo>().lambda()
                            .eq(CoinAlipayInfo::getUuid, id));//支付宝商家ID

                }else if (businessType.equals(CodeConst.BusinessType.PERSON)){ //支付宝登录绑定在个人码
                    CoinHolderInfo info = iCoinHolderInfoService.getOne(new QueryWrapper<CoinHolderInfo>().lambda().eq(CoinHolderInfo::getAlipayUid, uid));
                    if (info != null) {
                        if (!String.valueOf(info.getPayerAddr()).equals(id)) {//个码
//                        return new ReturnMsg().setFail("UID:" + info.getAlipayUid() + "已绑定在商家" + info.getAlipayName());
                            throw new TranException(CodeConst.SYSTEM_ERROR_CODE, "UID:" + info.getAlipayUid() + "已绑定在个码" + info.getPayerName());
                        }
                    }
                    //更新 CoinHolderInfo 表 (个码 则：id为上传的二维码唯一ID payerAddr)
                    CoinHolderInfo holderInfo = new CoinHolderInfo();
                    holderInfo.setAlipayUid(uid);
                    holderInfo.setAlipayCtoken(ctoken);
                    holderInfo.setAlipayCookie(String.valueOf(cookieString));
                    holderInfo.setIsBind(CodeConst.AlipayIsBindStatus.YES);
                    iCoinHolderInfoService.update(holderInfo,new UpdateWrapper<CoinHolderInfo>().lambda()
                            .eq(CoinHolderInfo::getPayerAddr, id));
                }

                _log.info("alipay:cookie:{} {}",id,cookieString);
                isBind = CodeConst.AlipayIsBindStatus.YES;

            }
        }
        Map<String, Object> resultMap = new HashMap<String, Object>();
        resultMap.put("isBind",isBind);//订单成功支付状态
        return new ReturnMsg(resultMap);
    }

    @RequestMapping(value = "/monitorBarcodeStatus", method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    public ReturnMsg monitorBarcodeStatus(String id,String seconds,HttpServletRequest request) throws Exception {

        String token = request.getHeader("authorization");
        if (!authenticatorService.checkToken(token)) {
            throw new TranException(CodeConst.PERMISSION_CODE, "无权访问");
        }
        if (id == null){
            throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "ID为空");
        }
        //https://qr.alipay.com/_d?_b=PAI_LOGIN_DY&amp;securityId=web%257Cauthcenter_qrcode_login%257C98012669-997e-403e-a02d-1003b3e176dbRZ42
       // String barcodeId = "alipay:barcode:" + id;
//        Object redisObj = redisUtil.get(barcodeId);
        String targetUrl = nodeTokenDomain  + "/getAlipayBarcode";
        //_log.info("barcode targetUrl - {}",targetUrl);
        Map<String, Object> paramsBarcode = new HashMap<String, Object>();
        paramsBarcode.put("id", id);
        paramsBarcode.put("token", token);
        String jsoinString = JSON.toJSONString(paramsBarcode);
        String resServerBarcode = HttpClient4Utils.postJSON(targetUrl,jsoinString);
        //_log.info("resServer barcode - {}",resServerBarcode);

        String barcodeStatus = "waiting";
        String stat = "ok";
        //已过期或者不存在
        if (resServerBarcode == null){
            throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "id:"+id+" 未取得 barcode");
        }else {
            JSONObject jsonBarcode = JSONObject.parseObject(resServerBarcode);
            if (jsonBarcode == null) {
                throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "id:"+id+" 未取得alipay barcode");
            }
            if (!"0000".equals(jsonBarcode.getString("code"))) {
                throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "id:"+id+" 未取得alipay barcode:" + jsonBarcode.getString("code"));
            }
            String barcode = String.valueOf(jsonBarcode.getString("barcode"));
            String securityId = "";
            if(!"".equals(barcode)) {
//                Pattern pattern = Pattern.compile("securityId=([^&]*)");
//                Matcher matcher = pattern.matcher(qrcodeUrl);
//                if (matcher.find()) {
//                    // Extract the value of the securityId parameter
//                    securityId = matcher.group(1);
//                    System.out.println("Original securityId: " + securityId);
//
//                    // Replace %257C with |
//                    String decodedSecurityId = securityId.replace("%257C", "|");
//                    System.out.println("Decoded securityId: " + decodedSecurityId);
//                    securityId = decodedSecurityId;
//                } else {
//                    System.out.println("securityId parameter not found in the URL.");
//                }
                securityId = getLastFewChars(barcode,40);//98012669-997e-403e-a02d-1003b3e176dbRZ42 || 40位数字
            }

            if (!"".equals(securityId)) {
                //?securityId=web%7Cauthcenter_qrcode_login%7C7758c0c9-aafa-4115-b915-811caf7fe12aRZ42&_callback=light.request._callbacks.callback178
                //web|authcenter_qrcode_login|d309bde6-034d-47ee-bff9-891a7df696f2RZ42
                String securityUrl = "https://securitycore.alipay.com/barcode/barcodeProcessStatus.json";
                securityId = "web|authcenter_qrcode_login|".concat(securityId);
                Map<String, Object> params = new HashMap<String, Object>();
                params.put("securityId",  securityId);//web|authcenter_qrcode_login|7758c0c9-aafa-4115-b915-811caf7fe12aRZ42
                params.put("_callback","light.request._callbacks.callback" + seconds);
                //light.request._callbacks.callback36({"barcodeStatus":"waiting","stat":"ok"})
                //_log.info("barcode 请求参数:{}",params);
                String serverResult = HttpClient4Utils.httpGetWithHeader(securityUrl,params,"UTF-8",2000,"https://auth.alipay.com/");
                if (serverResult == null || "".equals(serverResult)) {
                    return new ReturnMsg().setFail("返回结果为空");
                }
                //_log.info("barcode 返回结果:{}",serverResult);
                String codeString = serverResult.substring(serverResult.indexOf('(') + 1, serverResult.lastIndexOf(')'));
                if (!"".equals(codeString)) {
                    JSONObject json = JSONObject.parseObject(codeString);
                    barcodeStatus = json.getString("barcodeStatus");
                    stat = json.getString("stat");
                    // Print the extracted values
//                    System.out.println("barcodeStatus: " + barcodeStatus);
//                    System.out.println("stat: " + stat);
                }

            }
        }
        Map<String, Object> resultMap = new HashMap<String, Object>();
        resultMap.put("barcodeStatus",barcodeStatus);//支付宝官网的扫码状态 扫码成功 已确认
        resultMap.put("stat",stat);//是否正确
        return new ReturnMsg(resultMap);
    }

    private static String getLastFewChars(String str, int numChars) {
        if (str == null || str.length() < numChars) {
            return str;
        }
        return str.substring(str.length() - numChars);
    }
    public static Map<String, String> parseCookies(String cookieString) {
        Map<String, String> cookieMap = new HashMap<>();
        String[] cookies = cookieString.split(";");

        for (String cookie : cookies) {
            String[] keyValue = cookie.split("=", 2);
            if (keyValue.length == 2) {
                String key = keyValue[0].trim();
                String value = keyValue[1].trim();
                cookieMap.put(key, value);
            }
        }

        return cookieMap;
    }




}
