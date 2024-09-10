package com.bootpay.mng.controller;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.api.R;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bootpay.common.authenticator.GoogleAuthenticator;
import com.bootpay.common.constants.CodeConst;
import com.bootpay.common.constants.enums.EnumAccountType;
import com.bootpay.common.constants.enums.EnumAgentRate;
import com.bootpay.common.constants.enums.EnumChannelStatus;
import com.bootpay.common.constants.enums.EnumMailCodeType;
import com.bootpay.common.excetion.TranException;
import com.bootpay.common.framework.RedisUtil;
import com.bootpay.common.utils.*;
import com.bootpay.core.entity.*;
import com.bootpay.core.service.*;
import com.bootpay.mng.service.*;
import com.google.zxing.*;
import com.google.zxing.Writer;
import com.google.zxing.qrcode.QRCodeWriter;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.mail.Multipart;
import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.awt.image.ColorModel;
import java.io.*;
import java.math.BigDecimal;
import java.net.URL;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import org.apache.commons.io.FileUtils;
import org.springframework.util.ResourceUtils;
import org.apache.commons.io.FileUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.util.ResourceUtils;
import com.google.zxing.common.BitMatrix;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * <p>
 * 币址通道管理
 * </p>
 *
 * @author 
 * @since 2021-07-30
 */
@Validated
@RestController
@RequestMapping("/coin")
public class CoinHolderController {

    private Logger _log = LoggerFactory.getLogger(CoinHolderController.class);
    private static final int MAX_IMAGE_SIZE = 5 * 1024 * 1024; // 5 MB

    @Value("${app.privateAESKey}") //application.yml文件读取
    private String privateAESKey;

    @Value("${app.etherscanKey}")
    private String ETHERSCAN_KEY;


    @Value("${file.staticAccessPath}")
    private String staticAccessPath;

    @Autowired
    private AuthenticatorService authenticatorService;

    @Autowired
    private ICardChannelInfoService iCardChannelInfoService; //卡通道


    @Autowired
    private ICardChannelFlowInfoService iCardChannelFlowInfoService; //卡通道明细

    @Autowired
    private CardChannelManagerService cardChannelManagerService;

    @Autowired
    private CoinChannelManagerService coinChannelManagerService; //币址

    @Autowired
    private IPayMerchantInfoService iPayMerchantInfoService;

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private ICoinChannelDepositInfoService iCoinChannelDepositInfoService; //收

    @Autowired
    private ICoinChannelWithdrawFlowService iCoinChannelWithdrawFlowService; //COIN代付


    @Autowired
    private ICoinPayerWithdrawInfoService iCoinPayerWithdrawInfoService; //COIN代付

    @Autowired
    private ICoinPayerWithdrawFlowService iCoinPayerWithdrawFlowService;

    @Autowired
    private ICoinHolderDepositFlowService iCoinHolderDepositFlowService;

    @Autowired
    private ICoinPayerInfoService iCoinPayerInfoService;

    @Autowired
    private ICoinHolderInfoService iCoinHolderInfoService;

    @Autowired
    private ICoinHolderDepositInfoService iCoinHolderDepositInfoService;


    @Autowired
    private CoinWithdrawManagerService coinWithdrawManagerService;

    @Autowired
    private MerchantAccountManagerService merchantAccountManagerService;

    @Autowired
    private IWithdrawChannelInfoService iWithdrawChannelInfoService;

    @Autowired
    private IPayPlatformIncomeInfoService iPayPlatformIncomeInfoService;

    @Value("${app.walletPrivateKeySalt}") //application.yml文件读取
    private String walletPrivateKeySalt;

    @Autowired
    private IPayAgentBalanceService iPayAgentBalanceService;


    @Autowired
    private ICoinAlipayInfoService iCoinAlipayInfoService;


    /**
     * 获聚代收三方可用通道
     *
     * @return
     */
    @SuppressWarnings("unchecked")
    @RequestMapping(value = "/queryCoinChannelDepositInfo", method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    public ReturnMsg queryCoinChannelDepositInfo() {
        QueryWrapper<CoinChannelDepositInfo> queryWrapper = new QueryWrapper<CoinChannelDepositInfo>();
        queryWrapper.select("CHANNEL_CODE as channelCode", "CHANNEL_NAME as channelName", "REMARK as remark");
        queryWrapper.lambda()
                .eq(CoinChannelDepositInfo::getStatus, EnumChannelStatus.ENABLE.getName()).orderByAsc(CoinChannelDepositInfo::getChannelName);
        List<Map<String, Object>> list = iCoinChannelDepositInfoService.listMaps(queryWrapper);
        return new ReturnMsg(list);
    }


    /**
     * 查询代收钱包
     *
     * @return
     */
    @RequestMapping(value = "/queryCoinHolderInfoPage", method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    public ReturnMsg queryCoinHolderInfoPage(
            String payerMerchantId,
            String merchantId,
            String channelCode,
            String coinType,
            String payerName,
            String payerAddr,
            String payerIdentity,
            String payerOperatorId,
            String alipayInfoId,//商家码所属的支付宝商家ID
            String payerCodePrice,
            String status,
            String remark,
            String tableMerchantId,
            String beginTime,
            String endTime,
            Integer rows,
            Integer page,
            String businessType,
            HttpServletRequest request) throws TranException {

        PayMerchantInfo merchantInfo = iPayMerchantInfoService.getOne(new QueryWrapper<PayMerchantInfo>().lambda().eq(PayMerchantInfo::getMerchantId, tableMerchantId));

        //String hasChannelCode = merchantInfo.getAccountType().equals(EnumAccountType.MERCHANT.getName()) ? merchantInfo.getDepositChannelCode() : channelCode;

        if ("".equals(payerMerchantId) && merchantInfo.getAgentRate().equals(EnumAgentRate.CARD.getName())) { //码商
            if (merchantInfo.getAccountType().equals(EnumAccountType.MERCHANT.getName())) {
                throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "权限不足");
            }
        }

        if ("".equals(payerOperatorId) && merchantInfo.getAgentRate().equals(EnumAgentRate.OPERATOR.getName())) { //码商
            if (merchantInfo.getAccountType().equals(EnumAccountType.MERCHANT.getName())) {
                throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "码商操作员权限不足");
            }
        }

        Page<Map<String, Object>> pages = new Page<>(page, rows);
        Map<String, Object> params = new HashMap<String, Object>();
//        params.put("merchantId", merchantId);
        params.put("payerMerchantId", payerMerchantId);//码商
        params.put("payerOperatorId", payerOperatorId);//操作员
        params.put("alipayInfoId", alipayInfoId);
        params.put("channelCode", channelCode);
        params.put("coinType", coinType);
        params.put("payerName", payerName); //钱包名称
        params.put("payerAddr", payerAddr); //钱包名称
        params.put("payerIdentity", payerIdentity);//收款人
        params.put("payerCodePrice", payerCodePrice);
        params.put("status", status);
        params.put("businessType", businessType);
        params.put("remark", HtmlUtil.delHTMLTag(remark));
        params.put("beginTime", beginTime + " 00:00:00");
        params.put("endTime", endTime + " 23:59:59");

        IPage<Map<String, Object>> pageMap = iCoinHolderInfoService.queryCoinHolderInfoPage(pages, params);
        List<Map<String,Object>> records = pageMap.getRecords();
        List<Map<String,Object>> newRecords = new ArrayList<>();
        if (!records.isEmpty()) {
            for(Map<String,Object> itemRecord :records){
                Integer successTimes = iCoinHolderDepositInfoService.countCoinHolderDeposit(params);
                itemRecord.put("successTimes",successTimes);//当天成功次数
                newRecords.add(itemRecord);
            }
        }
        pageMap.setRecords(newRecords); //更新记录

        Map<String, Object> resultMap = new HashMap<String, Object>();
        resultMap.put("pages", pageMap);
        return new ReturnMsg(resultMap);

    }


    /**
     * @代收钱包
     */
    @RequestMapping(value = "/getCoinHolderCountMap", method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    public ReturnMsg getCoinHolderCountMap(
            String channelCode,
            String payerMerchantId,
            String payerOperatorId,
            String merchantId,//码商
            String status,
            String payerName,
            String payerAddr,
            String payerIdentity,
            String payerCodePrice,
            String coinType,
            String remark,
            String businessType,
            String tableMerchantId) throws TranException {

        PayMerchantInfo merchantInfo = iPayMerchantInfoService.getOne(new QueryWrapper<PayMerchantInfo>().lambda().eq(PayMerchantInfo::getMerchantId, tableMerchantId));

        if ("".equals(payerMerchantId) && merchantInfo.getAgentRate().equals(EnumAgentRate.CARD.getName())) { //码商
            if (merchantInfo.getAccountType().equals(EnumAccountType.MERCHANT.getName())) {
                throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "权限不足");
            }
        }

        if ("".equals(payerOperatorId) && merchantInfo.getAgentRate().equals(EnumAgentRate.OPERATOR.getName())) { //码商操作员
            if (merchantInfo.getAccountType().equals(EnumAccountType.MERCHANT.getName())) {
                throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "码商操作员权限不足");
            }
        }

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("payerMerchantId", payerMerchantId); //所属码商
        params.put("payerOperatorId", payerOperatorId);//操作员
//        params.put("merchantId", merchantId); //所属码商
        params.put("channelCode", channelCode);
        params.put("status", status);
        params.put("payerName", payerName); //持卡人
        params.put("payerAddr", payerAddr);
        params.put("payerIdentity", payerIdentity);
        params.put("payerCodePrice", payerCodePrice);
        params.put("coinType", coinType);
        params.put("remark", HtmlUtil.delHTMLTag(remark));
//        params.put("payerCodeTimes", "1");
        params.put("businessType", businessType);

        Integer countCoinHolder = iCoinHolderInfoService.selectCountCoinHolder(params);

        params.remove("status");
        params.put("status",CodeConst.PayerCardStatusConst.CARD_STATUS_BELONG);//已分配/挂码
        Integer countCoinHolderAvailable = iCoinHolderInfoService.selectCountCoinHolder(params);

        params.remove("payerCodePrice");

        String newPayerCodePrice = "200";
        params.put("payerCodePrice", newPayerCodePrice);
        Integer count200 = iCoinHolderInfoService.selectCountCoinHolder(params);

        newPayerCodePrice = "300";
        params.remove("payerCodePrice");
        params.put("payerCodePrice", newPayerCodePrice);
        Integer count300 = iCoinHolderInfoService.selectCountCoinHolder(params);

        newPayerCodePrice = "400";
        params.remove("payerCodePrice");
        params.put("payerCodePrice", newPayerCodePrice);
        Integer count400 = iCoinHolderInfoService.selectCountCoinHolder(params);

        newPayerCodePrice = "500";
        params.remove("payerCodePrice");
        params.put("payerCodePrice", newPayerCodePrice);
        Integer count500 = iCoinHolderInfoService.selectCountCoinHolder(params);

        newPayerCodePrice = "600";
        params.remove("payerCodePrice");
        params.put("payerCodePrice", newPayerCodePrice);
        Integer count600 = iCoinHolderInfoService.selectCountCoinHolder(params);

        newPayerCodePrice = "700";
        params.remove("payerCodePrice");
        params.put("payerCodePrice", newPayerCodePrice);
        Integer count700 = iCoinHolderInfoService.selectCountCoinHolder(params);

        newPayerCodePrice = "800";
        params.remove("payerCodePrice");
        params.put("payerCodePrice", newPayerCodePrice);
        Integer count800 = iCoinHolderInfoService.selectCountCoinHolder(params);

        newPayerCodePrice = "900";
        params.remove("payerCodePrice");
        params.put("payerCodePrice", newPayerCodePrice);
        Integer count900 = iCoinHolderInfoService.selectCountCoinHolder(params);

        newPayerCodePrice = "1000";
        params.remove("payerCodePrice");
        params.put("payerCodePrice", newPayerCodePrice);
        Integer count1000 = iCoinHolderInfoService.selectCountCoinHolder(params);



        Map<String, Object> resultMap = new HashMap<String, Object>();
        resultMap.put("countCoinHolder", countCoinHolder);
        resultMap.put("countCoinHolderAvailable", countCoinHolderAvailable);
        resultMap.put("count200", count200);
        resultMap.put("count300", count300);
        resultMap.put("count400", count400);
        resultMap.put("count500", count500);
        resultMap.put("count600", count600);
        resultMap.put("count700", count700);
        resultMap.put("count800", count800);
        resultMap.put("count900", count900);
        resultMap.put("count1000", count1000);
        return new ReturnMsg(resultMap);

    }


    /**
     * 钱包/收款修改
     * * 游戏名称    gameId
     * * 大小         3
     * * 单双         8
     * * 对子         2
     * * 豹子（三连号） 4
     * * PK10         6
     * @return ReturnMsg
     * @throws TranException
     */
    @RequestMapping(value = "/modifyCoinHolder", method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    public ReturnMsg modifyCoinHolder(
            String id,
            String channelCode,
            String gameId,
            String payerMerchantId,
            String merchantId,
            String alipayInfoId,//商家码所属的支付宝商家ID
            @NotNull(message = "钱包名称不能为空") String payerName,
            @NotNull(message = "钱包地址不能为空") String payerAddr,
            String isUid,
            String uid,
            String payerAddrOld,
            String coinType,
            String payerLimitDay,
            String payerLimitMonth,
            @NotNull(message = "钱包状态不能为空") String status,
            String payerOperatorId,//码商操作员
            String payerIdentity,//码商收款人
            String payerCodePrice,
            String payerCodeMinPrice,
            String payerCodeMaxPrice,
            String payerCodeTimes,
            String payerCodeShowTimes,//可拉单次数
            @NotNull(message = "google不能为空") String googleCode,
            String remark,
            String businessType,
            String tableMerchantId) throws Exception {
        try {

            PayMerchantInfo merchantInfo = iPayMerchantInfoService.getOne(new QueryWrapper<PayMerchantInfo>().lambda().eq(PayMerchantInfo::getMerchantId, tableMerchantId));

            if (merchantInfo.getAccountType().equals(EnumAccountType.MERCHANT.getName())) {
                if (!merchantInfo.getAgentRate().equals(EnumAgentRate.CARD.getName())) {
                    throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "权限不足");
                }
                merchantId = tableMerchantId;
            }
//            if (merchantInfo.getIsGoogleAuth().equals(CodeConst.GoogleConst.GOOGLE_AUTH_N)) {
//                throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "未开启google验证");
//            }

//            if (googleCode == null || "".equals(googleCode)) {
//                throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "google验证码为空");
//            }

//
//            String googleKeyDecode =  EncryptUtil.aesDecrypt(merchantInfo.getGoogleKey(),walletPrivateKeySalt);
//            if(!GoogleAuthenticator.check_code(googleKeyDecode, googleCode, System.currentTimeMillis())){
//                throw new TranException(CodeConst.BUSINESS_ERROR_CODE,"google验证码错误");
//            }

//            if (payerIdentity == null || "".equals(payerIdentity)) {
//                throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "收款人识别ID为空");
//            }
            if (payerAddr == null || "".equals(payerAddr)) {
                throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "收款码地址为空");
            }
            if (CodeConst.BusinessType.BUSINESS.equals(businessType)) {
                if (payerCodePrice == null || "".equals(payerCodePrice)) {
                    throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "收款码面值为空");
                }
            }else {
                //个人码
                if (BigDecimalUtils.greaterThan(new BigDecimal(payerCodeMinPrice),new BigDecimal(payerCodeMaxPrice))) {
                    throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "金额范围有误:" + new BigDecimal(payerCodeMinPrice) + " > " + new BigDecimal(payerCodeMaxPrice));
                }
            }

            String channelName = "";
            if (!"".equals(channelCode) && channelCode != null) {
                CoinChannelDepositInfo channelInfo = iCoinChannelDepositInfoService.getOne(new QueryWrapper<CoinChannelDepositInfo>()
                        .lambda().eq(CoinChannelDepositInfo::getChannelCode, channelCode));
                if (channelInfo != null) {
                    channelName = channelInfo.getChannelName();
                }

            }
            
            CoinAlipayInfo alipayInfo = iCoinAlipayInfoService.getOne(new QueryWrapper<CoinAlipayInfo>().lambda().eq(CoinAlipayInfo::getId, alipayInfoId));

            CoinHolderInfo info = new CoinHolderInfo();
            info.setMerchantId(merchantId);//钱包属于哪一个码商
            info.setPayerMerchantId(merchantId);//钱包属于哪一个码商
            info.setChannelCode(channelCode);
            info.setChannelName(channelName);

            info.setPayerName(alipayInfo!=null ? alipayInfo.getAlipayName() : payerName);
            info.setAlipayUid(alipayInfo!=null ? alipayInfo.getAlipayUid() : "");
            info.setPayerOperatorId(payerOperatorId);//码商操作员
            info.setPayerIdentity(payerIdentity);//收款人
            info.setPayerAddr(payerAddr.trim());//商家码地址若相同 则修改地址

            info.setIsUid(isUid);
            info.setUid(uid);
            info.setAlipayInfoId(alipayInfoId);
            info.setCoinType(coinType.trim()); //0-微信 1-支付宝 2-聚合码
            info.setStatus(status.trim()); //卡号数字串
            if (payerCodePrice != null) {
                info.setPayerCodePrice(new BigDecimal(payerCodePrice));
            }
            if (payerCodeMinPrice != null) {
                info.setPayerCodeMinPrice(new BigDecimal(payerCodeMinPrice));
            }
            if (payerCodeMaxPrice != null) {
                info.setPayerCodeMaxPrice(new BigDecimal(payerCodeMaxPrice));
            }
            if (payerCodeTimes != null) {
                info.setPayerCodeTimes(Integer.valueOf(payerCodeTimes));
            }
            if (payerCodeShowTimes != null) {
                info.setPayerCodeShowTimes(Integer.valueOf(payerCodeShowTimes));
            }

            info.setCreateTime(new Date());
            info.setDate(DateUtil.getDate());
            info.setRemark(HtmlUtil.delHTMLTag(remark)); //java过滤特殊字符
            info.setBusinessType(businessType);
            //Mybatis-Plus 更新语句
            boolean isUpdate = iCoinHolderInfoService.update(
                    info,
                    new UpdateWrapper<CoinHolderInfo>().lambda().eq(CoinHolderInfo::getId, id));

            if (!isUpdate) {
                throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "收款钱包修改有误");
            }
            return new ReturnMsg();
        } catch (Exception e) {
            e.printStackTrace();
            throw new TranException(CodeConst.BUSINESS_ERROR_CODE, e.toString());

        }
    }

    @RequestMapping(value = "/modifyAgentCoinHolder", method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    public ReturnMsg modifyAgentCoinHolder(
            String id,
            String channelCode,
            String gameId,
            String payerMerchantId,
            String merchantId,
            String alipayInfoId,//商家码所属的支付宝商家ID
            @NotNull(message = "钱包名称不能为空") String payerName,
            @NotNull(message = "钱包地址不能为空") String payerAddr,
            String payerAddrOld,
            String isUid,
            String uid,
            String coinType,
            String payerLimitDay,
            String payerLimitMonth,
            @NotNull(message = "钱包状态不能为空") String status,
            String payerOperatorId,//码商操作员
            String payerIdentity,//码商收款人
            String payerCodePrice,
            String payerCodeMinPrice,
            String payerCodeMaxPrice,
            String payerCodeTimes,
            String payerCodeShowTimes,//可拉单次数
            String googleCode,
            String remark,
            String businessType,
            String tableMerchantId) throws Exception {
        try {

            PayMerchantInfo merchantInfo = iPayMerchantInfoService.getOne(new QueryWrapper<PayMerchantInfo>().lambda().eq(PayMerchantInfo::getMerchantId, tableMerchantId));
//
//            if (payerIdentity == null || "".equals(payerIdentity)) {
//                throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "收款人识别ID为空");
//            }
            if (merchantInfo.getIsGoogleAuth().equals(CodeConst.GoogleConst.GOOGLE_AUTH_N)) {
                throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "未开启google验证");
            }

//            if (googleCode == null || "".equals(googleCode)) {
//                throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "google验证码为空");
//            }
//
//
//            String googleKeyDecode =  EncryptUtil.aesDecrypt(merchantInfo.getGoogleKey(),walletPrivateKeySalt);
//            if(!GoogleAuthenticator.check_code(googleKeyDecode, googleCode, System.currentTimeMillis())){
//                throw new TranException(CodeConst.BUSINESS_ERROR_CODE,"google验证码错误");
//            }

            if (payerAddr == null || "".equals(payerAddr)) {
                throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "收款码地址为空");
            }
            if (payerAddr == null || "".equals(payerAddr)) {
                throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "收款码地址为空");
            }
            if (CodeConst.BusinessType.BUSINESS.equals(businessType)) {
                if (payerCodePrice == null || "".equals(payerCodePrice)) {
                    throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "收款码面值为空");
                }
            }else {
                //个人码
                if (BigDecimalUtils.greaterThan(new BigDecimal(payerCodeMinPrice),new BigDecimal(payerCodeMaxPrice))) {
                    throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "金额范围有误:" + new BigDecimal(payerCodeMinPrice) + " > " + new BigDecimal(payerCodeMaxPrice));
                }
            }

            CoinHolderInfo holderInfo = iCoinHolderInfoService.getOne(new QueryWrapper<CoinHolderInfo>().lambda().eq(CoinHolderInfo::getId, id));
            if (holderInfo == null) {
                throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "收款码不存在");
            }
            if (merchantInfo.getAccountType().equals(EnumAccountType.MERCHANT.getName())) {
                if (!holderInfo.getPayerMerchantId().equals(tableMerchantId)){
                    throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "权限不足");
                }
                //如果是码商
//                if (merchantInfo.getAgentRate().equals(EnumAgentRate.CARD.getName())) {
//                    PayAgentBalance payAgentBalanceInfo = iPayAgentBalanceService.getOne(new QueryWrapper<PayAgentBalance>().lambda().eq(PayAgentBalance::getMerchantId, tableMerchantId));
//                    if (payAgentBalanceInfo != null) {
//                        if (BigDecimalUtils.lessEqual(payAgentBalanceInfo.getAccountBalance(),new BigDecimal(0))) {
//                            throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "码商上码额度不足,请联系客服充值");
//                        }
//                    }
//                }
            }


            String channelName = "";
            if (!"".equals(channelCode) && channelCode != null) {
                CoinChannelDepositInfo channelInfo = iCoinChannelDepositInfoService.getOne(new QueryWrapper<CoinChannelDepositInfo>()
                        .lambda().eq(CoinChannelDepositInfo::getChannelCode, channelCode));
                if (channelInfo != null) {
                    channelName = channelInfo.getChannelName();
                }
            }

            CoinAlipayInfo alipayInfo = iCoinAlipayInfoService.getOne(new QueryWrapper<CoinAlipayInfo>().lambda().eq(CoinAlipayInfo::getId, alipayInfoId));

            CoinHolderInfo info = new CoinHolderInfo();
            info.setMerchantId(merchantId);//钱包属于哪一个码商
            info.setPayerMerchantId(merchantId);//钱包属于哪一个码商
            info.setChannelCode(channelCode);
            info.setChannelName(channelName);

            info.setPayerName(alipayInfo!=null ? alipayInfo.getAlipayName() : payerName);
            info.setAlipayUid(alipayInfo!=null ? alipayInfo.getAlipayUid() : "");
            info.setPayerOperatorId(payerOperatorId);//码商操作员
            info.setPayerIdentity(payerIdentity);//收款人
            info.setPayerAddr(payerAddr.trim());

            info.setIsUid(isUid);
            info.setUid(uid);
            info.setAlipayInfoId(alipayInfoId);

            info.setCoinType(coinType.trim()); //0-微信 1-支付宝 2-聚合码
            info.setStatus(status.trim()); //卡号数字串
            if (payerCodePrice != null) {
                info.setPayerCodePrice(new BigDecimal(payerCodePrice));
            }
            if (payerCodeMinPrice != null) {
                info.setPayerCodeMinPrice(new BigDecimal(payerCodeMinPrice));
            }
            if (payerCodeMaxPrice != null) {
                info.setPayerCodeMaxPrice(new BigDecimal(payerCodeMaxPrice));
            }
            if (payerCodeTimes != null) {
                info.setPayerCodeTimes(Integer.valueOf(payerCodeTimes));
            }
            if (payerCodeShowTimes != null) {
                info.setPayerCodeShowTimes(Integer.valueOf(payerCodeShowTimes));
            }
            info.setCreateTime(new Date());
            info.setDate(DateUtil.getDate());
            info.setRemark(HtmlUtil.delHTMLTag(remark)); //java过滤特殊字符
            //Mybatis-Plus 更新语句
            boolean isUpdate = iCoinHolderInfoService.update(
                    info,
                    new UpdateWrapper<CoinHolderInfo>().lambda().eq(CoinHolderInfo::getId, id));

            if (!isUpdate) {
                throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "收款钱包修改有误");
            }
            return new ReturnMsg();
        } catch (Exception e) {
            e.printStackTrace();
            throw new TranException(CodeConst.BUSINESS_ERROR_CODE, e.toString());

        }
    }

    @RequestMapping(value = "/modifyOperatorCoinHolder", method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    public ReturnMsg modifyOperatorCoinHolder(
            String id,
            String channelCode,
            String gameId,
            String payerMerchantId,
            String merchantId,
            String alipayInfoId,//商家码所属的支付宝商家ID
            String isUid,
            String uid,
            @NotNull(message = "钱包名称不能为空") String payerName,
            @NotNull(message = "钱包地址不能为空") String payerAddr,
            String coinType,
            String payerLimitDay,
            String payerLimitMonth,
            @NotNull(message = "钱包状态不能为空") String status,
            String payerOperatorId,//码商操作员
            String payerIdentity,//码商收款人
            String payerCodePrice,
            String payerCodeMinPrice,
            String payerCodeMaxPrice,
            String payerCodeTimes,
            String payerCodeShowTimes,//可拉单次数
            String googleCode,
            String remark,
            String businessType,
            String tableMerchantId) throws Exception {
        try {

            PayMerchantInfo merchantInfo = iPayMerchantInfoService.getOne(new QueryWrapper<PayMerchantInfo>().lambda().eq(PayMerchantInfo::getMerchantId, tableMerchantId));

//            if (payerIdentity == null || "".equals(payerIdentity)) {
//                throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "收款人识别ID为空");
//            }
            if (payerAddr == null || "".equals(payerAddr)) {
                throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "收款码地址为空");
            }
            if (CodeConst.BusinessType.BUSINESS.equals(businessType)) {
                if (payerCodePrice == null || "".equals(payerCodePrice)) {
                    throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "收款码面值为空");
                }
            }else {
                //个人码
                if (BigDecimalUtils.greaterThan(new BigDecimal(payerCodeMinPrice),new BigDecimal(payerCodeMaxPrice))) {
                    throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "金额范围有误:" + new BigDecimal(payerCodeMinPrice) + " > " + new BigDecimal(payerCodeMaxPrice));
                }
            }

            CoinHolderInfo holderInfo = iCoinHolderInfoService.getOne(new QueryWrapper<CoinHolderInfo>().lambda().eq(CoinHolderInfo::getId, id));
            if (holderInfo == null) {
                throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "收款码为空");
            }
            if (merchantInfo.getAccountType().equals(EnumAccountType.MERCHANT.getName())) {
                if (!holderInfo.getPayerOperatorId().equals(tableMerchantId)){
                    throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "操作员权限不足");
                }
                //如果是码商操作员
//                if (merchantInfo.getAgentRate().equals(EnumAgentRate.OPERATOR.getName())) {
//                    PayMerchantInfo operatorInfo = iPayMerchantInfoService.getOne(new QueryWrapper<PayMerchantInfo>().lambda().eq(PayMerchantInfo :: getMerchantId, tableMerchantId));
//                    PayAgentBalance payAgentBalanceInfo = iPayAgentBalanceService.getOne(new QueryWrapper<PayAgentBalance>().lambda().eq(PayAgentBalance::getMerchantId, operatorInfo.getParentId()));
//                    if (payAgentBalanceInfo != null) {
//                        if (BigDecimalUtils.lessEqual(payAgentBalanceInfo.getAccountBalance(),new BigDecimal(0))) {
//                            throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "码商上码额度不足,请联系客服充值");
//                        }
//                    }
//                }
            }


            String channelName = "";
            if (!"".equals(channelCode) && channelCode != null) {
                CoinChannelDepositInfo channelInfo = iCoinChannelDepositInfoService.getOne(new QueryWrapper<CoinChannelDepositInfo>()
                        .lambda().eq(CoinChannelDepositInfo::getChannelCode, channelCode));
                if (channelInfo != null) {
                    channelName = channelInfo.getChannelName();
                }
            }
            CoinAlipayInfo alipayInfo = iCoinAlipayInfoService.getOne(new QueryWrapper<CoinAlipayInfo>().lambda().eq(CoinAlipayInfo::getId, alipayInfoId));

            CoinHolderInfo info = new CoinHolderInfo();
            info.setMerchantId(merchantId);//钱包属于哪一个码商
            info.setPayerMerchantId(merchantId);//钱包属于哪一个码商
            info.setChannelCode(channelCode);
            info.setChannelName(channelName);

            info.setPayerName(alipayInfo!=null ? alipayInfo.getAlipayName() : payerName);
            info.setAlipayUid(alipayInfo!=null ? alipayInfo.getAlipayUid() : "");
            info.setPayerOperatorId(payerOperatorId);//码商操作员
            info.setPayerIdentity(payerIdentity);//收款人
            info.setPayerAddr(payerAddr.trim());
            info.setIsUid(isUid);
            info.setUid(uid);
            info.setAlipayInfoId(alipayInfoId);

            info.setCoinType(coinType.trim()); //0-微信 1-支付宝 2-聚合码
            info.setStatus(status.trim()); //卡号数字串
            if (payerCodePrice != null) {
                info.setPayerCodePrice(new BigDecimal(payerCodePrice));
            }
            if (payerCodeMinPrice != null) {
                info.setPayerCodeMinPrice(new BigDecimal(payerCodeMinPrice));
            }
            if (payerCodeMaxPrice != null) {
                info.setPayerCodeMaxPrice(new BigDecimal(payerCodeMaxPrice));
            }
            if (payerCodeTimes != null) {
                info.setPayerCodeTimes(Integer.valueOf(payerCodeTimes));
            }
            if (payerCodeShowTimes != null) {
                info.setPayerCodeShowTimes(Integer.valueOf(payerCodeShowTimes));
            }

            info.setCreateTime(new Date());
            info.setDate(DateUtil.getDate());
            info.setRemark(HtmlUtil.delHTMLTag(remark)); //java过滤特殊字符
            //Mybatis-Plus 更新语句
            boolean isUpdate = iCoinHolderInfoService.update(
                    info,
                    new UpdateWrapper<CoinHolderInfo>().lambda().eq(CoinHolderInfo::getId, id));

            if (!isUpdate) {
                throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "收款钱包修改有误");
            }
            return new ReturnMsg();
        } catch (Exception e) {
            e.printStackTrace();
            throw new TranException(CodeConst.BUSINESS_ERROR_CODE, e.toString());

        }
    }


    @RequestMapping(value = "/delCoinHolderInfo", method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    @Transactional(rollbackFor = Exception.class)
    public ReturnMsg delCoinHolderInfo(
            @NotNull(message = "钱包地址不能为空") String payerAddr,
            @NotNull(message = "管理员ID不能为空") String tableMerchantId,
            String googleCode) throws Exception {

        PayMerchantInfo merchantInfo = iPayMerchantInfoService.getOne(new QueryWrapper<PayMerchantInfo>().lambda().eq(PayMerchantInfo::getMerchantId, tableMerchantId));
        //_log.info("merchantInfo.getAgentRate() - {}",merchantInfo.getAgentRate());
        if (merchantInfo.getAccountType().equals(EnumAccountType.MERCHANT.getName())) {
            if (!merchantInfo.getAgentRate().equals(EnumAgentRate.CARD.getName()) && !merchantInfo.getAgentRate().equals(EnumAgentRate.OPERATOR.getName())) { //码商
                throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "权限不足");
            }
        }

        //银行卡
        CoinHolderInfo coinHolderInfo = iCoinHolderInfoService.getOne(new QueryWrapper<CoinHolderInfo>().lambda().eq(CoinHolderInfo::getPayerAddr, payerAddr));
        if (coinHolderInfo == null) {
            throw new TranException(CodeConst.SYSTEM_ERROR_CODE, "钱包地址不存在");
        }

        if (merchantInfo.getAgentRate().equals(EnumAgentRate.CARD.getName())) {
            if (!coinHolderInfo.getPayerMerchantId().equals(tableMerchantId)) {
                throw new TranException(CodeConst.SYSTEM_ERROR_CODE, "码商权限不足");
            }
        }

        if (merchantInfo.getAgentRate().equals(EnumAgentRate.OPERATOR.getName())) {
            if (!coinHolderInfo.getPayerOperatorId().equals(tableMerchantId)) {
                throw new TranException(CodeConst.SYSTEM_ERROR_CODE, "操作员权限不足");
            }
        }

//        if (merchantInfo.getAccountType().equals(EnumAccountType.ADMIN.getName())) {
//            String googleKeyDecode =  EncryptUtil.aesDecrypt(merchantInfo.getGoogleKey(),walletPrivateKeySalt);
//            if(!GoogleAuthenticator.check_code(googleKeyDecode, googleCode, System.currentTimeMillis())){
//                throw new TranException(CodeConst.BUSINESS_ERROR_CODE,"google验证码错误");
//            }
//        }

        //检查收款码是否处于收款中，若处于收款中，则不删除
        CoinHolderDepositInfo coinHolderDepositInfo =   iCoinHolderDepositInfoService.getOne(new QueryWrapper<CoinHolderDepositInfo>().lambda()
                .eq(CoinHolderDepositInfo::getPayerAddr, payerAddr)
                .eq(CoinHolderDepositInfo::getStatus,CodeConst.WithdrawStatusConst.WITHDRAW_UNKNOWN)
        );
        if (coinHolderDepositInfo != null) {
            throw new TranException(CodeConst.SYSTEM_ERROR_CODE, "此收款码正在收款中");
        }

        //fixme:只在订单成功时扣除码商的额度
        //后台管理员删除不返回上码额度
//        if (merchantInfo.getAgentRate().equals(EnumAgentRate.CARD.getName()) || merchantInfo.getAgentRate().equals(EnumAgentRate.OPERATOR.getName())) {
//            //检查是否已成功收过款，如有收过款，则不再退回上码额度
//            CoinHolderDepositInfo coinHolderDepositSuccess =   iCoinHolderDepositInfoService.getOne(new QueryWrapper<CoinHolderDepositInfo>().lambda()
//                    .eq(CoinHolderDepositInfo::getPayerAddr, payerAddr)
//                    .eq(CoinHolderDepositInfo::getStatus,CodeConst.WithdrawStatusConst.WITHDRAW_SUCCESS)
//            );
//            if (coinHolderDepositSuccess == null) {
//                //9.订单失败 》 增加码商的收款额度
//                iPayAgentBalanceService.alterAgentBalance(coinHolderInfo.getPayerMerchantId(), coinHolderInfo.getPayerCodePrice(), null);
//                Map<String, Object> params = new HashMap<String, Object>();
//                params.put("merchantId", coinHolderInfo.getPayerMerchantId());
//                PayAgentBalanceFlow latestRecord = iPayAgentBalanceFlowService.selectLatestBalanceChangeForUpdate(params);
//
//                String withdrawNo = MySeq.getWithdrawNo();
//                //没有流水 要新插入
//                if (latestRecord == null) {
//                    PayAgentBalanceFlow addMoneyChangeEntity = new PayAgentBalanceFlow();
//                    addMoneyChangeEntity.setWithdrawNo(withdrawNo);
//                    addMoneyChangeEntity.setMerchantId(coinHolderInfo.getPayerMerchantId()); //某一个代理
//                    addMoneyChangeEntity.setAmt(coinHolderInfo.getPayerCodePrice()); // 提交代付金额
//                    addMoneyChangeEntity.setAmtBefore(new BigDecimal(0));
//                    addMoneyChangeEntity.setAmtNow(coinHolderInfo.getPayerCodePrice());
//                    addMoneyChangeEntity.setRemark(withdrawNo + "删除二维码退回上码额度");
//                    addMoneyChangeEntity.setCreateTime(new Date());
//                    iPayAgentBalanceFlowService.save(addMoneyChangeEntity);
//                }else {
//
//                    PayAgentBalanceFlow moneyChangeEntity = new PayAgentBalanceFlow();
//                    moneyChangeEntity.setMerchantId(coinHolderInfo.getPayerMerchantId());
//                    moneyChangeEntity.setWithdrawNo(withdrawNo);
//                    moneyChangeEntity.setAmt(coinHolderInfo.getPayerCodePrice()); //资金变动
//                    moneyChangeEntity.setAmtBefore(latestRecord.getAmtNow());
//                    moneyChangeEntity.setAmtNow(latestRecord.getAmtNow().add(coinHolderInfo.getPayerCodePrice()));
//                    moneyChangeEntity.setCreateTime(new Date());
//                    moneyChangeEntity.setRemark(withdrawNo + "删除二维码退回上码额度");
//                    iPayAgentBalanceFlowService.save(moneyChangeEntity);
//                }
//            }
//        }

        //删除钱包
        boolean isRemovePayerCardNo = iCoinHolderInfoService.remove(new QueryWrapper<CoinHolderInfo>().lambda().eq(CoinHolderInfo::getPayerAddr, payerAddr));
        if (!isRemovePayerCardNo) {
            throw new TranException(CodeConst.SYSTEM_ERROR_CODE, "删除钱包失败");
        }

        return new ReturnMsg();

    }


    /**
     * 钱包状态
     */
    @RequestMapping(value = "/modifyCoinHolderStatus", method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    @Transactional(rollbackFor = Exception.class)
    public ReturnMsg modifyCoinHolderStatus(@NotNull(message = "钱包地址不能为空") String payerAddr,
                                            String payerMerchantId,
                                            String payerOperatorId,
                                            @NotNull(message = "请选择状态") String status,
                                            @NotNull(message = "管理员ID不能为空") String tableMerchantId
    ) throws TranException {
        //管理员
        PayMerchantInfo merchantInfo = iPayMerchantInfoService.getOne(new QueryWrapper<PayMerchantInfo>().lambda().eq(PayMerchantInfo::getMerchantId, tableMerchantId));

        //1.查询卡原来所属的通道
        CoinHolderInfo holderInfo = iCoinHolderInfoService.getOne(new QueryWrapper<CoinHolderInfo>().lambda().eq(CoinHolderInfo::getPayerAddr, payerAddr));
        if (holderInfo == null) {
            throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "收款钱包不存在");
        }

        if(merchantInfo.getAccountType().equals(EnumAccountType.MERCHANT.getName())){
            if ("".equals(payerMerchantId) && EnumAgentRate.CARD.getName().equals(merchantInfo.getAgentRate())) {//码商等级
                throw new TranException(CodeConst.BUSINESS_ERROR_CODE,"码商权限不足");
            }
            if ("".equals(payerOperatorId) && EnumAgentRate.OPERATOR.getName().equals(merchantInfo.getAgentRate())) { //操作员等级
                throw new TranException(CodeConst.BUSINESS_ERROR_CODE,"操作员权限不足");
            }
        }

        //2.获取卡的余额及所属通道
        // BigDecimal payerAmt = iCardPayerInfoServiceOne.getPayerAmt(); //卡余额
        // String cardChannelCode = iCardPayerInfoServiceOne.getCardChannelCode();//所属通道
        // String exPayerCardStatus = iCardPayerInfoServiceOne.getPayerCardStatus();//银行卡原来的状态
        // String channelName = iCardPayerInfoServiceOne.getCardChannelName(); //所属通道名称

        //3.修改银行卡的状态
        CoinHolderInfo entity = new CoinHolderInfo();
        entity.setStatus(status);
        boolean isUpdated = iCoinHolderInfoService.update(entity, new UpdateWrapper<CoinHolderInfo>().lambda().eq(CoinHolderInfo::getPayerAddr, payerAddr));
        if (!isUpdated) {
            throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "钱包状态修改失败");
        }

        return new ReturnMsg();
    }

    @RequestMapping(value = "/modifyOperatorCoinHolderStatus", method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    @Transactional(rollbackFor = Exception.class)
    public ReturnMsg modifyOperatorCoinHolderStatus(@NotNull(message = "钱包地址不能为空") String payerAddr,
                                            @NotNull(message = "请选择状态") String status,
                                            @NotNull(message = "管理员ID不能为空") String tableMerchantId
    ) throws TranException {
        //管理员
        PayMerchantInfo merchantInfo = iPayMerchantInfoService.getOne(new QueryWrapper<PayMerchantInfo>().lambda().eq(PayMerchantInfo::getMerchantId, tableMerchantId));

        //1.查询卡原来所属的通道
        CoinHolderInfo holderInfo = iCoinHolderInfoService.getOne(new QueryWrapper<CoinHolderInfo>().lambda().eq(CoinHolderInfo::getPayerAddr, payerAddr));
        if (holderInfo == null) {
            throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "收款钱包不存在");
        }

        if (merchantInfo.getAccountType().equals(EnumAccountType.MERCHANT.getName())) {
            if (!holderInfo.getPayerOperatorId().equals(tableMerchantId)){
                throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "操作员权限不足");
            }
            //如果是码商操作员
//            if (merchantInfo.getAgentRate().equals(EnumAgentRate.OPERATOR.getName())) {
//                PayMerchantInfo operatorInfo = iPayMerchantInfoService.getOne(new QueryWrapper<PayMerchantInfo>().lambda().eq(PayMerchantInfo :: getMerchantId, tableMerchantId));
//                PayAgentBalance  payAgentBalanceInfo = iPayAgentBalanceService.getOne(new QueryWrapper<PayAgentBalance>().lambda().eq(PayAgentBalance :: getMerchantId,operatorInfo.getParentId()));
//                //PayAgentBalance payAgentBalanceInfo = iPayAgentBalanceService.getOne(new QueryWrapper<PayAgentBalance>().lambda().eq(PayAgentBalance::getMerchantId, tableMerchantId));
//                if (payAgentBalanceInfo != null) {
//                    if (BigDecimalUtils.lessEqual(payAgentBalanceInfo.getAccountBalance(),new BigDecimal(0))) {
//                        throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "码商上码额度不足,请联系客服充值");
//                    }
//                }
//            }
        }

        //2.获取卡的余额及所属通道
        // BigDecimal payerAmt = iCardPayerInfoServiceOne.getPayerAmt(); //卡余额
        // String cardChannelCode = iCardPayerInfoServiceOne.getCardChannelCode();//所属通道
        // String exPayerCardStatus = iCardPayerInfoServiceOne.getPayerCardStatus();//银行卡原来的状态
        // String channelName = iCardPayerInfoServiceOne.getCardChannelName(); //所属通道名称

        //3.修改银行卡的状态
        CoinHolderInfo entity = new CoinHolderInfo();
        entity.setStatus(status);
        boolean isUpdated = iCoinHolderInfoService.update(entity, new UpdateWrapper<CoinHolderInfo>().lambda().eq(CoinHolderInfo::getPayerAddr, payerAddr));
        if (!isUpdated) {
            throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "钱包状态修改失败");
        }

        return new ReturnMsg();
    }


    /**
     * 注册代收钱包
     */
    @RequestMapping(value = "/registerCoinHolder", method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    public ReturnMsg registerCoinHolder(
            String channelCode,
            String payerMerchantId,//码商
            String payerOperatorId,//码商操作员
            String merchantId,//码商
            String alipayInfoId,//商家码所属的支付宝商家ID
            String isUid,
            String uid,
            String payerName,
            String payerIdentity,//收款人
            @NotNull(message = "收款码不能为空") String payerAddr,
            String coinType,
            String payerLimitDay,
            String payerLimitMonth,
            String deviceId,
            @NotNull(message = "钱包状态不能为空") String status,
            String payerCodePrice,
            String payerCodeMinPrice,
            String payerCodeMaxPrice,
            String payerCodeTimes,//可成功次数
            String payerCodeShowTimes,//可拉单次数
            String googleCode,
            String emailCode,
            String remark,
            String businessType,
            String tableMerchantId) throws Exception {

        PayMerchantInfo merchantInfo = iPayMerchantInfoService.getOne(new QueryWrapper<PayMerchantInfo>().lambda().eq(PayMerchantInfo::getMerchantId, tableMerchantId));
        if (merchantInfo.getAccountType().equals(EnumAccountType.MERCHANT.getName())) {
            if (!merchantInfo.getAgentRate().equals(EnumAgentRate.CARD.getName())) {
                throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "权限不足");
            }
            merchantId = tableMerchantId;
        }

        if (merchantInfo.getIsGoogleAuth().equals(CodeConst.GoogleConst.GOOGLE_AUTH_N)) {
            throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "未开启google验证");
        }

//        if (googleCode == null || "".equals(googleCode)) {
//            throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "google验证码为空");
//        }
//        if (emailCode == null || "".equals(emailCode)) {
//            throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "邮箱验证码为空");
//        }

//        String googleKeyDecode =  EncryptUtil.aesDecrypt(merchantInfo.getGoogleKey(),walletPrivateKeySalt);
//        if(!GoogleAuthenticator.check_code(googleKeyDecode, googleCode, System.currentTimeMillis())){
//            throw new TranException(CodeConst.BUSINESS_ERROR_CODE,"google验证码错误");
//        }

//        if (!authenticatorService.checkMailCode(merchantInfo.getMerchantId(), emailCode, EnumMailCodeType.WALLET.getName())) {
//            return new ReturnMsg(CodeConst.BUSINESS_ERROR_CODE, "邮箱验证码错误");
//        }

//        if (payerIdentity == null || "".equals(payerIdentity)) {
//            throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "收款人识别ID为空");
//        }
        if (payerAddr == null || "".equals(payerAddr)) {
            throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "收款码地址为空");
        }
        if (CodeConst.BusinessType.BUSINESS.equals(businessType)) {
            if (payerCodePrice == null || "".equals(payerCodePrice)) {
                throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "收款码面值为空");
            }
        }else {
            //个人码
            if (BigDecimalUtils.greaterThan(new BigDecimal(payerCodeMinPrice),new BigDecimal(payerCodeMaxPrice))) {
                throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "金额范围有误:" + new BigDecimal(payerCodeMinPrice) + " > " + new BigDecimal(payerCodeMaxPrice));
            }
        }

        //添加钱包
        CoinHolderInfo coinHolderInfo = iCoinHolderInfoService.getOne(new QueryWrapper<CoinHolderInfo>().lambda().eq(CoinHolderInfo::getPayerAddr, payerAddr.trim()));
        if (coinHolderInfo != null) {
            throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "" + payerAddr + " 钱包重复");
        }
        if (alipayInfoId == null) { alipayInfoId = "";}
        CoinAlipayInfo alipayInfo = iCoinAlipayInfoService.getOne(new QueryWrapper<CoinAlipayInfo>().lambda().eq(CoinAlipayInfo::getId, alipayInfoId));

        //获取通道名称
        String channelName = "";
        if (!"".equals(channelCode) && channelCode != null) {
            CoinChannelDepositInfo channelInfo = iCoinChannelDepositInfoService.getOne(new QueryWrapper<CoinChannelDepositInfo>()
                    .lambda().eq(CoinChannelDepositInfo::getChannelCode, channelCode));
            channelName = channelInfo.getChannelName();
        }

        CoinHolderInfo info = new CoinHolderInfo();
        info.setChannelCode(channelCode);
        info.setChannelName(channelName);

        info.setMerchantId(merchantId);//收款码属于哪一个码商
        info.setPayerMerchantId(merchantId);//收款码属于哪一个码商
        info.setPayerOperatorId(payerOperatorId);//属于哪一个码商下挂的操作员
        info.setPayerIdentity(payerIdentity);//收款码属于哪一个收款人
        info.setAlipayInfoId(alipayInfoId);
        info.setPayerName(alipayInfo!=null ? alipayInfo.getAlipayName() : payerName);
        info.setAlipayUid(alipayInfo!=null ? alipayInfo.getAlipayUid() : "");
        info.setPayerAddr(payerAddr.trim());

        info.setIsUid(isUid);
        info.setUid(uid);
        info.setAlipayInfoId(alipayInfoId);

        info.setIsBind(alipayInfo!=null ? alipayInfo.getIsBind() : "0"); //是否已绑定

        info.setCoinType(coinType.trim()); //0-微信 1-支付宝 2-聚合码
        info.setStatus(status.trim()); //是否可用
        if (payerCodePrice != null) {
            info.setPayerCodePrice(new BigDecimal(payerCodePrice));
        }
        if (payerCodeMinPrice != null) {
            info.setPayerCodeMinPrice(new BigDecimal(payerCodeMinPrice)); //个人码起始金额
        }
        if (payerCodeMaxPrice != null) {
            info.setPayerCodeMaxPrice(new BigDecimal(payerCodeMaxPrice)); //个人码结束金额
        }

        info.setPayerCodeTimes(payerCodeTimes!= null ? Integer.valueOf(payerCodeTimes) : 1);//至少1次
        info.setPayerCodeShowTimes(payerCodeShowTimes != null ? Integer.valueOf(payerCodeShowTimes) : 10);
        info.setBusinessType(businessType);
        info.setCreateTime(new Date());
        info.setDate(DateUtil.getDate());
        info.setRemark(HtmlUtil.delHTMLTag(remark)); //java过滤特殊字符
        iCoinHolderInfoService.save(info);

        return new ReturnMsg();
    }


    @RequestMapping(value = "/registerAgentCoinHolder", method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    public ReturnMsg registerAgentCoinHolder(
            String channelCode,
            String payerMerchantId,//码商
            String payerOperatorId,//码商操作员
            String alipayInfoId,//商家码所属的支付宝商家ID
            String merchantId,//码商
            String isUid,
            String uid,
            String payerName,
            String payerIdentity,//收款人
            @NotNull(message = "收款码不能为空") String payerAddr,
            String coinType,
            String payerLimitDay,
            String payerLimitMonth,
            String deviceId,
            @NotNull(message = "钱包状态不能为空") String status,
            String payerCodePrice,
            String payerCodeMinPrice,
            String payerCodeMaxPrice,
            String payerCodeTimes,//可成功次数
            String payerCodeShowTimes,//可拉单次数
            String googleCode,
            String emailCode,
            String remark,
            String businessType,
            String tableMerchantId) throws Exception {

        PayMerchantInfo merchantInfo = iPayMerchantInfoService.getOne(new QueryWrapper<PayMerchantInfo>().lambda().eq(PayMerchantInfo::getMerchantId, tableMerchantId));
        if (merchantInfo.getAccountType().equals(EnumAccountType.MERCHANT.getName())) {
            if (!merchantInfo.getAgentRate().equals(EnumAgentRate.CARD.getName())) {
                throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "权限不足");
            }
            merchantId = tableMerchantId;
        }

        if (merchantInfo.getIsGoogleAuth().equals(CodeConst.GoogleConst.GOOGLE_AUTH_N)) {
            throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "未开启google验证");
        }

//        if (googleCode == null || "".equals(googleCode)) {
//            throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "google验证码为空");
//        }
//
//        String googleKeyDecode =  EncryptUtil.aesDecrypt(merchantInfo.getGoogleKey(),walletPrivateKeySalt);
//        if(!GoogleAuthenticator.check_code(googleKeyDecode, googleCode, System.currentTimeMillis())){
//            throw new TranException(CodeConst.BUSINESS_ERROR_CODE,"google验证码错误");
//        }

        //如果是码商
        if (merchantInfo.getAgentRate().equals(EnumAgentRate.CARD.getName())) {
            PayAgentBalance payAgentBalanceInfo = iPayAgentBalanceService.getOne(new QueryWrapper<PayAgentBalance>().lambda().eq(PayAgentBalance::getMerchantId, tableMerchantId));
            if (payAgentBalanceInfo != null) {
                if (BigDecimalUtils.lessEqual(payAgentBalanceInfo.getAccountBalance(),new BigDecimal(0))) {
                    throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "码商上码额度不足,请联系客服充值");
                }
            }
        }

//        if (payerIdentity == null || "".equals(payerIdentity)) {
//            throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "收款人识别ID为空");
//        }
        if (payerAddr == null || "".equals(payerAddr)) {
            throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "收款码地址为空");
        }
        if (CodeConst.BusinessType.BUSINESS.equals(businessType)) {
            if (payerCodePrice == null || "".equals(payerCodePrice)) {
                throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "收款码面值为空");
            }
        }else {
            //个人码
            if (BigDecimalUtils.greaterThan(new BigDecimal(payerCodeMinPrice),new BigDecimal(payerCodeMaxPrice))) {
                throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "金额范围有误:" + new BigDecimal(payerCodeMinPrice) + " > " + new BigDecimal(payerCodeMaxPrice));
            }
        }

        //添加钱包
        CoinHolderInfo coinHolderInfo = iCoinHolderInfoService.getOne(new QueryWrapper<CoinHolderInfo>().lambda().eq(CoinHolderInfo::getPayerAddr, payerAddr.trim()));
        if (coinHolderInfo != null) {
            throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "" + payerAddr + " 钱包重复");
        }
        CoinAlipayInfo alipayInfo = iCoinAlipayInfoService.getOne(new QueryWrapper<CoinAlipayInfo>().lambda().eq(CoinAlipayInfo::getId, alipayInfoId));

        //获取通道名称
        String channelName = "";
        if (!"".equals(channelCode) && channelCode != null) {
            CoinChannelDepositInfo channelInfo = iCoinChannelDepositInfoService.getOne(new QueryWrapper<CoinChannelDepositInfo>()
                    .lambda().eq(CoinChannelDepositInfo::getChannelCode, channelCode));
            channelName = channelInfo.getChannelName();
        }

        CoinHolderInfo info = new CoinHolderInfo();
        info.setMerchantId(merchantId);//收款码属于哪一个码商
        info.setPayerMerchantId(merchantId);//收款码属于哪一个码商
        info.setChannelCode(channelCode);
        info.setChannelName(channelName);

        info.setAlipayInfoId(alipayInfoId);
        info.setPayerName(alipayInfo!=null ? alipayInfo.getAlipayName() : payerName);
        info.setAlipayUid(alipayInfo!=null ? alipayInfo.getAlipayUid() : "");
        info.setPayerOperatorId(payerOperatorId);//属于哪一个码商下挂的操作员
        info.setPayerIdentity(payerIdentity);//收款码属于哪一个收款人
        info.setPayerAddr(payerAddr.trim());
        info.setAlipayInfoId(alipayInfoId);
        info.setIsUid(isUid);
        info.setUid(uid);
        info.setCoinType(coinType.trim()); //0-微信 1-支付宝 2-聚合码
        info.setStatus(status.trim()); //卡号数字串
        if (payerCodePrice != null) {
            info.setPayerCodePrice(new BigDecimal(payerCodePrice));
        }
        if (payerCodeMinPrice != null) {
            info.setPayerCodeMinPrice(new BigDecimal(payerCodeMinPrice)); //个人码起始金额
        }
        if (payerCodeMaxPrice != null) {
            info.setPayerCodeMaxPrice(new BigDecimal(payerCodeMaxPrice)); //个人码结束金额
        }

        info.setPayerCodeTimes(Integer.valueOf(payerCodeTimes));
        info.setPayerCodeShowTimes(payerCodeShowTimes != null ? Integer.valueOf(payerCodeShowTimes) : 10);
        info.setBusinessType(businessType);
        info.setCreateTime(new Date());
        info.setDate(DateUtil.getDate());
        info.setRemark(HtmlUtil.delHTMLTag(remark)); //java过滤特殊字符
        iCoinHolderInfoService.save(info);

//        iPayAgentBalanceService.alterAgentBalance(merchantId, new BigDecimal(payerCodePrice).negate(), null);
//        Map<String, Object> params = new HashMap<String, Object>();
//        params.put("merchantId", merchantId);
//        PayAgentBalanceFlow latestRecord = iPayAgentBalanceFlowService.selectLatestBalanceChangeForUpdate(params);

//        String withdrawNo = MySeq.getWithdrawNo();
//        //没有流水 要新插入
//        if (latestRecord == null) {
//            PayAgentBalanceFlow addMoneyChangeEntity = new PayAgentBalanceFlow();
//            addMoneyChangeEntity.setWithdrawNo(withdrawNo);
//            addMoneyChangeEntity.setMerchantId(merchantId); //某一个代理
//            addMoneyChangeEntity.setAmt(new BigDecimal(payerCodePrice)); // 提交代付金额
//            addMoneyChangeEntity.setAmtBefore(new BigDecimal(0));
//            addMoneyChangeEntity.setAmtNow(new BigDecimal(payerCodePrice).negate());
//            addMoneyChangeEntity.setRemark(withdrawNo + "扣除上码额度");
//            addMoneyChangeEntity.setCreateTime(new Date());
//            iPayAgentBalanceFlowService.save(addMoneyChangeEntity);
//        }else {
//            PayAgentBalanceFlow moneyChangeEntity = new PayAgentBalanceFlow();
//            moneyChangeEntity.setMerchantId(merchantId);
//            moneyChangeEntity.setWithdrawNo(withdrawNo);
//            moneyChangeEntity.setAmt(new BigDecimal(payerCodePrice)); //资金变动
//            moneyChangeEntity.setAmtBefore(latestRecord.getAmtNow());
//            moneyChangeEntity.setAmtNow(latestRecord.getAmtNow().add(new BigDecimal(payerCodePrice).negate()));
//            moneyChangeEntity.setCreateTime(new Date());
//            moneyChangeEntity.setRemark(withdrawNo + "扣除上码额度");
//            iPayAgentBalanceFlowService.save(moneyChangeEntity);
//        }
        return new ReturnMsg();
    }

    @RequestMapping(value = "/registerOperatorCoinHolder", method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    public ReturnMsg registerOperatorCoinHolder(
            String channelCode,
            String payerMerchantId,//码商
            String payerOperatorId,//码商操作员
            String merchantId,//码商
            String alipayInfoId,//商家码所属的支付宝商家ID
            String isUid,
            String uid,
            String payerName,
            String payerIdentity,//收款人
            @NotNull(message = "收款码不能为空") String payerAddr,
            String coinType,
            String payerLimitDay,
            String payerLimitMonth,
            String deviceId,
            @NotNull(message = "钱包状态不能为空") String status,
            String payerCodePrice,
            String payerCodeMinPrice,
            String payerCodeMaxPrice,
            String payerCodeTimes,//可成功次数
            String payerCodeShowTimes,//可拉单次数
            String googleCode,
            String emailCode,
            String remark,
            String businessType,
            String tableMerchantId) throws Exception {

        PayMerchantInfo merchantInfo = iPayMerchantInfoService.getOne(new QueryWrapper<PayMerchantInfo>().lambda().eq(PayMerchantInfo::getMerchantId, tableMerchantId));
        if (merchantInfo.getAccountType().equals(EnumAccountType.MERCHANT.getName())) {
            if (!merchantInfo.getAgentRate().equals(EnumAgentRate.OPERATOR.getName())) {
                throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "操作员权限不足");
            }
            merchantId = merchantInfo.getParentId();
        }

        if (merchantInfo.getIsGoogleAuth().equals(CodeConst.GoogleConst.GOOGLE_AUTH_N)) {
            throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "未开启google验证");
        }

//        if (googleCode == null || "".equals(googleCode)) {
//            throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "google验证码为空");
//        }

//        String googleKeyDecode =  EncryptUtil.aesDecrypt(merchantInfo.getGoogleKey(),walletPrivateKeySalt);
//        if(!GoogleAuthenticator.check_code(googleKeyDecode, googleCode, System.currentTimeMillis())){
//            throw new TranException(CodeConst.BUSINESS_ERROR_CODE,"google验证码错误");
//        }

        //如果是码商操作员
        if (merchantInfo.getAgentRate().equals(EnumAgentRate.OPERATOR.getName())) {
            PayMerchantInfo operatorInfo = iPayMerchantInfoService.getOne(new QueryWrapper<PayMerchantInfo>().lambda().eq(PayMerchantInfo :: getMerchantId, tableMerchantId));
            PayAgentBalance payAgentBalanceInfo = iPayAgentBalanceService.getOne(new QueryWrapper<PayAgentBalance>().lambda().eq(PayAgentBalance::getMerchantId, operatorInfo.getParentId()));
            if (payAgentBalanceInfo != null) {
                if (BigDecimalUtils.lessEqual(payAgentBalanceInfo.getAccountBalance(),new BigDecimal(0))) {
                    throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "码商上码额度不足,请联系客服充值");
                }
            }
        }

//        if (payerIdentity == null || "".equals(payerIdentity)) {
//            throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "收款人识别ID为空");
//        }
        if (payerAddr == null || "".equals(payerAddr)) {
            throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "收款码地址为空");
        }
        if (CodeConst.BusinessType.BUSINESS.equals(businessType)) {
            if (payerCodePrice == null || "".equals(payerCodePrice)) {
                throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "收款码面值为空");
            }
        }else {
            //个人码
            if (BigDecimalUtils.greaterThan(new BigDecimal(payerCodeMinPrice),new BigDecimal(payerCodeMaxPrice))) {
                throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "金额范围有误:" + new BigDecimal(payerCodeMinPrice) + " > " + new BigDecimal(payerCodeMaxPrice));
            }
        }


        //添加钱包
        CoinHolderInfo coinHolderInfo = iCoinHolderInfoService.getOne(new QueryWrapper<CoinHolderInfo>().lambda().eq(CoinHolderInfo::getPayerAddr, payerAddr.trim()));
        if (coinHolderInfo != null) {
            throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "" + payerAddr + " 钱包重复");
        }
        CoinAlipayInfo alipayInfo = iCoinAlipayInfoService.getOne(new QueryWrapper<CoinAlipayInfo>().lambda().eq(CoinAlipayInfo::getId, alipayInfoId));

        //获取通道名称
        String channelName = "";
        if (!"".equals(channelCode) && channelCode != null) {
            CoinChannelDepositInfo channelInfo = iCoinChannelDepositInfoService.getOne(new QueryWrapper<CoinChannelDepositInfo>()
                    .lambda().eq(CoinChannelDepositInfo::getChannelCode, channelCode));
            channelName = channelInfo.getChannelName();
        }

        CoinHolderInfo info = new CoinHolderInfo();
        info.setChannelCode(channelCode);
        info.setChannelName(channelName);
        info.setMerchantId(merchantInfo.getParentId());//收款码属于哪一个码商
        info.setPayerMerchantId(merchantInfo.getParentId());//收款码属于哪一个码商

        info.setIsUid(isUid);
        info.setUid(uid);
        info.setAlipayInfoId(alipayInfoId);
        info.setPayerName(alipayInfo!=null ? alipayInfo.getAlipayName() : payerName);
        info.setAlipayUid(alipayInfo!=null ? alipayInfo.getAlipayUid() : "");
        info.setPayerOperatorId(tableMerchantId);//属于哪一个码商下挂的操作员
        info.setPayerIdentity(payerIdentity);//收款码属于哪一个收款人
        info.setPayerAddr(payerAddr.trim());

        info.setCoinType(coinType.trim()); //0-微信 1-支付宝 2-聚合码
        info.setStatus(status.trim()); //卡号数字串
        if (payerCodePrice != null) {
            info.setPayerCodePrice(new BigDecimal(payerCodePrice));
        }
        if (payerCodeMinPrice != null) {
            info.setPayerCodeMinPrice(new BigDecimal(payerCodeMinPrice)); //个人码起始金额
        }
        if (payerCodeMaxPrice != null) {
            info.setPayerCodeMaxPrice(new BigDecimal(payerCodeMaxPrice)); //个人码结束金额
        }
        info.setPayerCodeTimes(payerCodeTimes!= null ? Integer.valueOf(payerCodeTimes) : 1);
        info.setPayerCodeShowTimes(payerCodeShowTimes != null ? Integer.valueOf(payerCodeShowTimes) : 10);
        info.setBusinessType(businessType);
        info.setCreateTime(new Date());
        info.setDate(DateUtil.getDate());
        info.setRemark(HtmlUtil.delHTMLTag(remark)); //java过滤特殊字符
        iCoinHolderInfoService.save(info);

//        iPayAgentBalanceService.alterAgentBalance(merchantId, new BigDecimal(payerCodePrice).negate(), null);
//        Map<String, Object> params = new HashMap<String, Object>();
//        params.put("merchantId", merchantId);
//        PayAgentBalanceFlow latestRecord = iPayAgentBalanceFlowService.selectLatestBalanceChangeForUpdate(params);
//
//        String withdrawNo = MySeq.getWithdrawNo();
//        //没有流水 要新插入
//        if (latestRecord == null) {
//            PayAgentBalanceFlow addMoneyChangeEntity = new PayAgentBalanceFlow();
//            addMoneyChangeEntity.setWithdrawNo(withdrawNo);
//            addMoneyChangeEntity.setMerchantId(merchantId); //某一个代理
//            addMoneyChangeEntity.setAmt(new BigDecimal(payerCodePrice)); // 提交代付金额
//            addMoneyChangeEntity.setAmtBefore(new BigDecimal(0));
//            addMoneyChangeEntity.setAmtNow(new BigDecimal(payerCodePrice).negate());
//            addMoneyChangeEntity.setRemark(withdrawNo + "扣除上码额度");
//            addMoneyChangeEntity.setCreateTime(new Date());
//            iPayAgentBalanceFlowService.save(addMoneyChangeEntity);
//        }else {
//            PayAgentBalanceFlow moneyChangeEntity = new PayAgentBalanceFlow();
//            moneyChangeEntity.setMerchantId(merchantId);
//            moneyChangeEntity.setWithdrawNo(withdrawNo);
//            moneyChangeEntity.setAmt(new BigDecimal(payerCodePrice)); //资金变动
//            moneyChangeEntity.setAmtBefore(latestRecord.getAmtNow());
//            moneyChangeEntity.setAmtNow(latestRecord.getAmtNow().add(new BigDecimal(payerCodePrice).negate()));
//            moneyChangeEntity.setCreateTime(new Date());
//            moneyChangeEntity.setRemark(withdrawNo + "扣除上码额度");
//            iPayAgentBalanceFlowService.save(moneyChangeEntity);
//        }


        return new ReturnMsg();
    }




    /**
     * 钱包代收流水明细
     */
    @RequestMapping(value = "/queryCoinHolderFlowPage", method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    public ReturnMsg queryCoinHolderFlowPage(
            String withdrawNo,
            String channelWithdrawNo,
            String payerName,
            String payerAddr,
            String payerMerchantId,
            String payerIdentity,
            String channelCode,
            String flowType,
            String coinType,
            String beginTime,
            String endTime,
            String remark,
            @NotNull(message = "行数不能为空") Integer rows,
            @NotNull(message = "页码不能为空") Integer page,
            String tableMerchantId
    ) throws TranException {

        PayMerchantInfo merchantInfo = iPayMerchantInfoService.getOne(new QueryWrapper<PayMerchantInfo>().lambda().eq(PayMerchantInfo::getMerchantId, tableMerchantId));
        String hasChannelCode = merchantInfo.getAccountType().equals(EnumAccountType.MERCHANT.getName()) ? merchantInfo.getDepositChannelCode() : channelCode;

        if ("".equals(hasChannelCode) || hasChannelCode == null) {
            if (merchantInfo.getAccountType().equals(EnumAccountType.MERCHANT.getName())) {
                throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "权限不足");
            }
        }

        Page<CoinHolderDepositFlow> pages = new Page<>(page, rows);

        QueryWrapper<CoinHolderDepositFlow> queryWrapper = new QueryWrapper<>();

        Map<String, Object> params = new HashMap<String, Object>();

        params.put("CHANNEL_CODE", hasChannelCode);
        params.put("WITHDRAW_NO", withdrawNo);
        params.put("CHANNEL_WITHDRAW_NO", channelWithdrawNo);
        params.put("FLOW_TYPE", flowType);
        params.put("COIN_TYPE", coinType);
        params.put("REMARK", HtmlUtil.delHTMLTag(remark));
        params.put("PAYER_NAME", payerName);
        params.put("PAYER_ADDR", payerAddr);
        params.put("PAYER_IDENTITY", payerIdentity);
        params.put("PAYER_MERCHANT_ID", payerMerchantId);

        queryWrapper = queryWrapper.allEq(params, false)
                .like(StringUtils.isNotBlank(remark), "REMARK", HtmlUtil.delHTMLTag(remark))
                .ge(StringUtils.isNotBlank(beginTime), "CREATE_TIME", beginTime + " 00:00:00")
                .le(StringUtils.isNotBlank(endTime), "CREATE_TIME", endTime + " 23:59:59");
        queryWrapper.orderByDesc("ID");
        //page
        IPage<CoinHolderDepositFlow> pageMap = iCoinHolderDepositFlowService.page(pages, queryWrapper);

        //2统计交易订单
//        Map<String, Object> paramsCount = new HashMap<String, Object>();
//        paramsCount.put("flowType", "");
//        Map<String, Object> countFlowInfoMap = iCoinHolderDepositFlowService.selectPayerCountRecharge(paramsCount);

        //3//按条件搜索充值金额
        Map<String, Object> paramsSearch = new HashMap<String, Object>();

        paramsSearch.put("withdrawNo", withdrawNo);
        paramsSearch.put("channelWithdrawNo", channelWithdrawNo);
        paramsSearch.put("channelCode", channelCode);
        paramsSearch.put("payerName", payerName);
        paramsSearch.put("payerAddr", payerAddr);
        paramsSearch.put("payerMerchantId", payerMerchantId);
        paramsSearch.put("payerIdentity", payerIdentity);
        paramsSearch.put("remark", HtmlUtil.delHTMLTag(remark));
        paramsSearch.put("flowType", flowType);
        paramsSearch.put("coinType", coinType);
        paramsSearch.put("beginTime", beginTime == null ? DateUtil.getDate() + " 00:00:00" : beginTime + " 00:00:00");
        paramsSearch.put("endTime", endTime == null ? DateUtil.getDate() + " 23:59:59" : endTime + " 23:59:59");
        Map<String, Object> countMapBySearchMap = iCoinHolderDepositFlowService.selectPayerCountRechargeBySearch(paramsSearch);

        //查询结果 map
        Map<String, Object> resultMap = new HashMap<String, Object>();
        //resultMap.put("countFlowInfo", countFlowInfoMap);
        resultMap.put("countMapBySearch", countMapBySearchMap);
        resultMap.put("pages", pageMap);

        return new ReturnMsg(resultMap);
    }

    /**
     * 获取代付通道列表 分页
     *
     * @param
     * @param rows 条数
     * @param page 页码
     * @return
     */
    @RequestMapping(value = "/queryWithdrawChannelInfoPage", method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    public ReturnMsg queryWithdrawChannelInfoPage(String channelCode, String channelStatus, String channelName, Integer rows, Integer page) {
        Page<WithdrawChannelInfo> pages = new Page<>(page, rows);
        QueryWrapper<WithdrawChannelInfo> queryWrapper = new QueryWrapper<>();
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("CHANNEL_CODE", channelCode);
        params.put("CHANNEL_STATUS", channelStatus);
        queryWrapper = queryWrapper.allEq(params, false); //忽略value为null 值为null空 则忽略
        queryWrapper.orderByDesc("CHANNEL_CODE");
        queryWrapper.lambda().like(StringUtils.isNotBlank(channelName), WithdrawChannelInfo::getChannelName, channelName);
        IPage<WithdrawChannelInfo> pageMap = iWithdrawChannelInfoService.page(pages, queryWrapper);
        return new ReturnMsg(pageMap);
    }


    /**
     * 创建通道
     *
     * @param channelInfo
     * @return
     */
    @RequestMapping(value = "/addWithdrawChannelInfo", method = {RequestMethod.POST, RequestMethod.GET}, name = "admin")
    @ResponseBody
    public ReturnMsg addWithdrawChannelInfo(WithdrawChannelInfo channelInfo) throws TranException {
        if (!DateUtil.isValidDate(channelInfo.getBeginWithdrawTime(), DateUtil.FORMAT_HHMMSS) || !DateUtil.isValidDate(channelInfo.getEndWithdrawTime(), DateUtil.FORMAT_HHMMSS)) {
            throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "代付时间格式不正确");
        }
        WithdrawChannelInfo query = iWithdrawChannelInfoService.getOne(new QueryWrapper<WithdrawChannelInfo>().lambda().eq(WithdrawChannelInfo::getChannelCode, channelInfo.getChannelCode()));
        if (query != null) {
            throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "通道编号重复");
        }
        channelInfo.setChannelFeeType(channelInfo.getChannelFeeType()); //手续费结算方式
        channelInfo.setChannelStatus(EnumChannelStatus.ENABLE.getName());
        channelInfo.setCreateTime(new Date());
        iWithdrawChannelInfoService.save(channelInfo);

        //初始化通道余额
        PayPlatformIncomeInfo incomeInfo = new PayPlatformIncomeInfo();
        incomeInfo.setChannelType(CodeConst.ChannelTypeConst.DEPOSIT);//代收
        incomeInfo.setChannelFeeType(channelInfo.getChannelFeeType());
        incomeInfo.setChannelCode(channelInfo.getChannelCode());
        incomeInfo.setChannelName(channelInfo.getChannelName());
        incomeInfo.setPlatformAccountBalance(new BigDecimal("0.0000"));
        iPayPlatformIncomeInfoService.save(incomeInfo);

        return new ReturnMsg();
    }

    /**
     * @修改代收通道
     **/
    @RequestMapping(value = "/modifyWithdrawChannelInfo", method = {RequestMethod.POST, RequestMethod.GET}, name = "admin")
    @ResponseBody
    public ReturnMsg modifyWithdrawChannelInfo(@NotBlank(message = "id不能为空") String id,
                                               String channelName,
                                               String channelCode,
                                               String channelStatus,
                                               String channelFeeType,
                                               String rechargeCost,
                                               String singleCost,
                                               String singleMinimum,
                                               String singleHighest,
                                               String singleRechargeCost,
                                               String dayQuota,
                                               String beginWithdrawTime,
                                               String endWithdrawTime) throws TranException {
        if (!DateUtil.isValidDate(beginWithdrawTime, DateUtil.FORMAT_HHMMSS) || !DateUtil.isValidDate(endWithdrawTime, DateUtil.FORMAT_HHMMSS)) {
            throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "代付时间格式不正确");
        }
        WithdrawChannelInfo channelInfo = new WithdrawChannelInfo();
        channelInfo.setChannelName(channelName);
        channelInfo.setChannelCode(channelCode);
        channelInfo.setChannelFeeType(channelFeeType); //手续费
        channelInfo.setChannelStatus(channelStatus);
        channelInfo.setRechargeCost(rechargeCost);
        channelInfo.setSingleCost(new BigDecimal(singleCost));
        channelInfo.setSingleMinimum(new BigDecimal(singleMinimum));
        channelInfo.setSingleHighest(new BigDecimal(singleHighest));
        channelInfo.setBeginWithdrawTime(beginWithdrawTime);
        channelInfo.setEndWithdrawTime(endWithdrawTime);
        channelInfo.setSingleRechargeCost(new BigDecimal(singleRechargeCost));
        channelInfo.setDayQuota(new BigDecimal(dayQuota));
        iWithdrawChannelInfoService.update(channelInfo, new UpdateWrapper<WithdrawChannelInfo>().lambda().eq(WithdrawChannelInfo::getId, id));
        return new ReturnMsg();
    }

    /**
     * 删除代收通道
     *
     * @param
     * @return
     */
    @RequestMapping(value = "/delWithdrawChannelInfo", method = {RequestMethod.POST, RequestMethod.GET}, name = "admin")
    @ResponseBody
    public ReturnMsg delWithdrawChannelInfo(
            @NotBlank(message = "通道id不能为空") String channelCode,
            String tableMerchantId,
            String googleCode) throws Exception {

        PayMerchantInfo merchantInfo = iPayMerchantInfoService.getOne(new QueryWrapper<PayMerchantInfo>().lambda().eq(PayMerchantInfo::getMerchantId, tableMerchantId));
//        if(merchantInfo.getAccountType().equals(EnumAccountType.MERCHANT.getName())){
//            throw new TranException(CodeConst.BUSINESS_ERROR_CODE,"权限不足");
//        }
        if (googleCode == null || "".equals(googleCode)) {
            throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "google验证码错误");
        }

        String googleKeyDecode =  EncryptUtil.aesDecrypt(merchantInfo.getGoogleKey(),walletPrivateKeySalt);
        if(!GoogleAuthenticator.check_code(googleKeyDecode, googleCode, System.currentTimeMillis())){
            throw new TranException(CodeConst.BUSINESS_ERROR_CODE,"google验证码错误");
        }

        iWithdrawChannelInfoService.remove(new QueryWrapper<WithdrawChannelInfo>().lambda().eq(WithdrawChannelInfo::getChannelCode, channelCode));
        return new ReturnMsg();
    }


    /**
     * 查询商家代收钱包列表
     *
     * @param rows
     * @param page
     * @return
     */
    @RequestMapping(value = "/queryMerchantCoinHolderInfoPage", method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    public ReturnMsg queryMerchantCoinHolderInfoPage(
            String channelCode,
            String channelName,
            String status,
            String payerName,
            String payerAddr,
            String coinType,
            String tableMerchantId,
            Integer rows,
            Integer page) throws TranException {

        PayMerchantInfo merchantInfo = iPayMerchantInfoService.getOne(new QueryWrapper<PayMerchantInfo>().lambda().eq(PayMerchantInfo::getMerchantId, tableMerchantId));
        //收款钱包通道
        String hasChannelCode = merchantInfo.getAccountType().equals(EnumAccountType.MERCHANT.getName()) ? merchantInfo.getDepositChannelCode() : channelCode;

        if ("".equals(hasChannelCode) || hasChannelCode == null) {
            if (merchantInfo.getAccountType().equals(EnumAccountType.MERCHANT.getName())) {
                throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "权限不足");
            }
        }

        Page<Map<String, Object>> pages = new Page<>(page, rows);
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("channelCode", hasChannelCode);
        params.put("channelName", channelName);
        params.put("payerName", payerName); //持卡人
        params.put("payerAddr", payerAddr);
        params.put("status", status);
        params.put("coinType", coinType);
        params.put("date", DateUtil.getDate());
        params.put("month", DateUtil.getMonth());

        IPage<Map<String, Object>> pageMap = iCoinHolderInfoService.queryMerchantCoinHolderInfoPage(pages, params);

        Map<String, Object> resultMap = new HashMap<String, Object>();
        resultMap.put("pages", pageMap);
        return new ReturnMsg(resultMap);

    }



    /**
     * 批量修改卡状态
     *
     * @param
     * @param
     * @param
     * @param
     * @return
     */
    @RequestMapping(value = "/batchModifyPayerInfoStatus", method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    public ReturnMsg batchModifyPayerInfoStatus(
            @NotNull(message = "请选择收款码") String payerAddrs,
            @NotNull(message = "状态不能为空") String status,
            String tableMerchantId
    ) throws TranException {
        //管理员
        PayMerchantInfo merchantInfo = iPayMerchantInfoService.getOne(new QueryWrapper<PayMerchantInfo>().lambda().eq(PayMerchantInfo::getMerchantId, tableMerchantId));
        //如果是码商
//        if (merchantInfo.getAgentRate().equals(EnumAgentRate.CARD.getName())) {
//            PayAgentBalance payAgentBalanceInfo = iPayAgentBalanceService.getOne(new QueryWrapper<PayAgentBalance>().lambda().eq(PayAgentBalance::getMerchantId, tableMerchantId));
//            if (payAgentBalanceInfo != null) {
//                if (BigDecimalUtils.lessEqual(payAgentBalanceInfo.getAccountBalance(),new BigDecimal(0))) {
//                    throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "码商上码额度不足,请联系客服充值");
//                }
//            }
//        }
//        if (merchantInfo.getAgentRate().equals(EnumAgentRate.OPERATOR.getName())) {
//            PayMerchantInfo operatorInfo = iPayMerchantInfoService.getOne(new QueryWrapper<PayMerchantInfo>().lambda().eq(PayMerchantInfo :: getMerchantId, tableMerchantId));
//            PayAgentBalance  payAgentBalanceInfo = iPayAgentBalanceService.getOne(new QueryWrapper<PayAgentBalance>().lambda().eq(PayAgentBalance :: getMerchantId,operatorInfo.getParentId()));
//            if (payAgentBalanceInfo != null) {
//                if (BigDecimalUtils.lessEqual(payAgentBalanceInfo.getAccountBalance(),new BigDecimal(0))) {
//                    throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "码商上码额度不足,请联系客服充值");
//                }
//            }
//        }

        List<String> payerList = Arrays.asList(payerAddrs.split(","));

        List<CoinHolderInfo> coinHolderList = iCoinHolderInfoService.list(new QueryWrapper<CoinHolderInfo>().lambda().in(CoinHolderInfo::getPayerAddr, payerList));
        for (CoinHolderInfo holderInfo:coinHolderList) {
            if (merchantInfo.getAgentRate().equals(EnumAgentRate.CARD.getName())) {
                if (!holderInfo.getPayerMerchantId().equals(tableMerchantId)){
                    throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "码商权限不足");
                }
            }
            if (merchantInfo.getAgentRate().equals(EnumAgentRate.OPERATOR.getName())) {
                if (!holderInfo.getPayerOperatorId().equals(tableMerchantId)){
                    throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "操作员权限不足");
                }
            }
        }

        CoinHolderInfo entity = new CoinHolderInfo();
        entity.setStatus(status);
        iCoinHolderInfoService.update(entity, new QueryWrapper<CoinHolderInfo>().lambda().in(CoinHolderInfo::getPayerAddr, payerList));


        return new ReturnMsg();
    }

    @RequestMapping(value = "/batchDelPayerInfoStatus", method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    public ReturnMsg batchDelPayerInfoStatus(
            @NotNull(message = "请选择收款码") String payerAddrs,
            @NotNull(message = "状态不能为空") String status,
            String tableMerchantId
    ) throws TranException {
        //管理员
        PayMerchantInfo merchantInfo = iPayMerchantInfoService.getOne(new QueryWrapper<PayMerchantInfo>().lambda().eq(PayMerchantInfo::getMerchantId, tableMerchantId));

        List<String> payerList = Arrays.asList(payerAddrs.split(","));

        List<CoinHolderInfo> coinHolderList = iCoinHolderInfoService.list(new QueryWrapper<CoinHolderInfo>().lambda().in(CoinHolderInfo::getPayerAddr, payerList));
        for (CoinHolderInfo holderInfo:coinHolderList) {
            if (merchantInfo.getAgentRate().equals(EnumAgentRate.CARD.getName())) {
                if (!holderInfo.getPayerMerchantId().equals(tableMerchantId)){
                    throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "码商权限不足");
                }
            }
            if (merchantInfo.getAgentRate().equals(EnumAgentRate.OPERATOR.getName())) {
                if (!holderInfo.getPayerOperatorId().equals(tableMerchantId)){
                    throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "操作员权限不足");
                }
            }

            CoinHolderDepositInfo coinHolderDepositInfo =   iCoinHolderDepositInfoService.getOne(new QueryWrapper<CoinHolderDepositInfo>().lambda()
                    .eq(CoinHolderDepositInfo::getPayerAddr, holderInfo.getPayerAddr())
                    .eq(CoinHolderDepositInfo::getStatus,CodeConst.WithdrawStatusConst.WITHDRAW_UNKNOWN)
            );
            if (coinHolderDepositInfo != null) {
                throw new TranException(CodeConst.SYSTEM_ERROR_CODE, "收款码 "+holderInfo.getPayerName()+" 正在收款中");
            }

            iCoinHolderInfoService.remove(new QueryWrapper<CoinHolderInfo>().lambda().eq(CoinHolderInfo::getPayerAddr, holderInfo.getPayerAddr()));

        }

        return new ReturnMsg();
    }

    @RequestMapping(value = "/batchUpdateCoinChannelCode", method = {RequestMethod.POST, RequestMethod.GET},name = "admin")
    @ResponseBody
    public ReturnMsg batchUpdateCoinChannelCode(
            String channelCode,
            String channelName,
            @NotNull(message = "请选择收款码") String payerAddrs,
            @NotNull(message = "google验证码为空") String googleCode,
            String tableMerchantId
    ) throws Exception {
        //管理员
        PayMerchantInfo merchantInfo = iPayMerchantInfoService.getOne(new QueryWrapper<PayMerchantInfo>().lambda().eq(PayMerchantInfo::getMerchantId, tableMerchantId));
        if (merchantInfo.getAccountType().equals(EnumAccountType.MERCHANT.getName())) {
            throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "权限不足");
        }

        if (googleCode == null || "".equals(googleCode)) {
            throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "google验证码错误");
        }

        String googleKeyDecode =  EncryptUtil.aesDecrypt(merchantInfo.getGoogleKey(),walletPrivateKeySalt);
        if(!GoogleAuthenticator.check_code(googleKeyDecode, googleCode, System.currentTimeMillis())){
            throw new TranException(CodeConst.BUSINESS_ERROR_CODE,"google验证码错误");
        }

        List<String> payerList = Arrays.asList(payerAddrs.split(","));
        CoinHolderInfo entity = new CoinHolderInfo();
        if (!"".equals(channelCode)) {
            CoinChannelDepositInfo channelInfo = iCoinChannelDepositInfoService.getOne(new QueryWrapper<CoinChannelDepositInfo>().lambda().eq(CoinChannelDepositInfo::getChannelCode, channelCode));

            entity.setChannelCode(channelCode);
            entity.setChannelName(channelInfo.getChannelName());
        }

        iCoinHolderInfoService.update(entity, new QueryWrapper<CoinHolderInfo>().lambda().in(CoinHolderInfo::getPayerAddr, payerList));
//        List<CardPayerInfo> cardPayerList = iCardPayerInfoService.list(new QueryWrapper<CardPayerInfo>().lambda().in(CardPayerInfo::getPayerCardNo, cardPayerNosList));
//        for (CardPayerInfo cardPayerInfo:cardPayerList) {
//
//        }

        return new ReturnMsg();
    }

    @RequestMapping(value = "/uploadImage", method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    public ReturnMsg uploadImage(@RequestParam("codeImage")  MultipartFile[] multipartFiles, MultipartHttpServletRequest multipartHttpServletRequest, HttpServletRequest request){

        String businessType = request.getHeader("businessType"); //0-个人码 1-商家码
        String token = request.getHeader("authorization");
        String sourceFilename = request.getHeader("fileName");
        if (!authenticatorService.checkToken(token)) {
            return new ReturnMsg().setFail("无权访问");
        }
        _log.info("sourceFilename - {}",sourceFilename);
//        for (MultipartFile file : multipartFiles) {
//            if (!file.isEmpty()) {
//                String originalFilename = file.getOriginalFilename();
//                _log.info("token:{}, businessType:{}, filename:{}", token, businessType, originalFilename);
//            } else {
//                _log.info("One of the files is empty.");
//            }
//        }
        Map<String, MultipartFile> files = multipartHttpServletRequest.getFileMap();
        MultipartFile file = files.get("codeImage");

//        _log.info("token:{},businessType:{},{}",token,businessType, file.getName());
        String tableMerchantId = request.getParameter("tableMerchantId");
        PayMerchantInfo merchantInfo = iPayMerchantInfoService.getOne(new QueryWrapper<PayMerchantInfo>().lambda().eq(PayMerchantInfo :: getMerchantId, tableMerchantId));
        if (merchantInfo == null) {
            return new ReturnMsg().setFail("商户号不存在");
        }
        if (merchantInfo.getAccountType().equals(EnumAccountType.MERCHANT.getName()) && (!merchantInfo.getAgentRate().equals(EnumAgentRate.CARD.getName()) && !merchantInfo.getAgentRate().equals(EnumAgentRate.OPERATOR.getName()))) {
            return new ReturnMsg().setFail("权限不足");
        }

        if (!file.isEmpty()){
            String originFileName = file.getOriginalFilename();
            //String suffix = originFileName.substring(originFileName.lastIndexOf('.'));
            assert originFileName != null;
            String suffix = originFileName.substring(originFileName.lastIndexOf('.')).toLowerCase();
            // 验证文件类型，只允许上传.png和.jpg文件
            if (!(".png".equals(suffix) || ".jpg".equals(suffix))) {
                return new ReturnMsg().setFail("只允许上传.png和.jpg文件");
            }

            String fileName = "";
            String address = "";
            if (businessType.equals(CodeConst.BusinessType.PERSON)) {
//                String date = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date().getTime());
//                fileName = date + UUID.randomUUID().toString().replace("-","") + ".png";
                fileName = UUID.randomUUID().toString().replace("-","") + ".png";
                address = fileName.substring(0,fileName.length() - 4);
            }else {
                fileName = sourceFilename.substring(0,sourceFilename.length() - 4)+ ".png";
                address = fileName.substring(0,fileName.length() - 4);
                //如果 fileName已重复 则不再添加
                CoinHolderInfo holderInfo = iCoinHolderInfoService.getOne(new QueryWrapper<CoinHolderInfo>().lambda().eq(CoinHolderInfo::getPayerAddr, address));
                if (holderInfo != null) {
                    return new ReturnMsg().setFail("订单编号已重复");
                }
            }

            File dir = new File(staticAccessPath);
            if (!dir.exists()){
                dir.mkdirs();
            }
            try{
                file.transferTo(new File(dir,fileName));
            }catch(IOException e){
                e.printStackTrace();
                return new ReturnMsg().setFail();
            }
            //String address = request.getScheme().concat("://").concat(request.getServerName()).concat(":").concat(String.valueOf(request.getServerPort())).concat("/api/file/").concat();

            _log.info("上传:{},file address:{}",tableMerchantId,address);

            return  new ReturnMsg(address);
        }
        return new ReturnMsg().setFail();
    }

    @RequestMapping(value = "/generateQrcodeByUid", method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    public ReturnMsg generateQrcodeByUid(String uid,String payerCodePrice,String tableMerchantId){

        PayMerchantInfo merchantInfo = iPayMerchantInfoService.getOne(new QueryWrapper<PayMerchantInfo>().lambda().eq(PayMerchantInfo :: getMerchantId, tableMerchantId));
        if (merchantInfo == null) {
            return new ReturnMsg().setFail("商户号不存在");
        }
        if (merchantInfo.getAccountType().equals(EnumAccountType.MERCHANT.getName()) && (!merchantInfo.getAgentRate().equals(EnumAgentRate.CARD.getName()) && !merchantInfo.getAgentRate().equals(EnumAgentRate.OPERATOR.getName()))) {
            return new ReturnMsg().setFail("权限不足");
        }

        if (uid == null) {
            return new ReturnMsg().setFail("uid不存在");
        }

        if (!uid.isEmpty()){
            String suffix = ".png";
            String date = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date().getTime());
            String fileName = date + UUID.randomUUID().toString().replace("-","")+suffix;
            File dir = new File(staticAccessPath);
            if (!dir.exists()){
                dir.mkdirs();
            }

            String imageUrl = "alipays://platformapi/startapp?appId=20000123&actionType=scan&biz_data={\"s\": \"money\",\"u\": \""+uid+"\",\"a\": \""+payerCodePrice+"\",\"m\":\"购物收款\"}";
            generateAndSaveQRCode(imageUrl,staticAccessPath.concat(fileName), "PNG");

            String address = fileName.substring(0,fileName.length() - 4);
            _log.info("uid上传:{},file address:{}",tableMerchantId,address);
            return  new ReturnMsg(address);

        }
        return new ReturnMsg().setFail();
    }

    /**
     * 查询代收钱包
     *
     * @return
     */
    @RequestMapping(value = "/queryFollowerOrcodeList", method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    public ReturnMsg queryFollowerOrcodeList(
            String payerMerchantId,
            String merchantId,
            String channelCode,
            String coinType,
            String payerName,
            String payerAddr,
            String payerIdentity,
            String payerCodePrice,
            String status,
            String remark,
            String tableMerchantId,
            Integer rows,
            Integer page,
            HttpServletRequest request) throws TranException {

        PayMerchantInfo merchantInfo = iPayMerchantInfoService.getOne(new QueryWrapper<PayMerchantInfo>().lambda().eq(PayMerchantInfo::getMerchantId, tableMerchantId));

        String hasChannelCode = merchantInfo.getAccountType().equals(EnumAccountType.MERCHANT.getName()) ? merchantInfo.getDepositChannelCode() : channelCode;

        if ("".equals(payerIdentity) || payerIdentity == null) {
            if (merchantInfo.getAccountType().equals(EnumAccountType.MERCHANT.getName())) {
                throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "权限不足");
            }
        }

        Page<Map<String, Object>> pages = new Page<>(page, rows);
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("merchantId", merchantId);
        params.put("payerMerchantId", payerMerchantId);
        params.put("channelCode", hasChannelCode);
        params.put("coinType", coinType);
        params.put("payerName", payerName); //钱包名称
        params.put("payerAddr", payerAddr); //钱包名称
        params.put("payerIdentity", payerIdentity);//收款人
        params.put("payerCodePrice", payerCodePrice);
        params.put("status", status);
        params.put("remark", HtmlUtil.delHTMLTag(remark));


        IPage<Map<String, Object>> pageMap = iCoinHolderInfoService.queryCoinHolderInfoPage(pages, params);


        Map<String, Object> resultMap = new HashMap<String, Object>();
        resultMap.put("pages", pageMap);
        return new ReturnMsg(resultMap);

    }

    /**
     * @代收钱包
     */
    @RequestMapping(value = "/queryFollowerOrcodeCount", method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    public ReturnMsg queryFollowerOrcodeCount(
            String channelCode,
            String payerMerchantId,
            String merchantId,//码商
            String status,
            String payerName,
            String payerAddr,
            String payerIdentity,
            String payerCodePrice,
            String coinType,
            String remark,
            String tableMerchantId) throws TranException {

        PayMerchantInfo merchantInfo = iPayMerchantInfoService.getOne(new QueryWrapper<PayMerchantInfo>().lambda().eq(PayMerchantInfo::getMerchantId, tableMerchantId));

        if ("".equals(payerIdentity) || payerIdentity == null) {
            if (merchantInfo.getAccountType().equals(EnumAccountType.MERCHANT.getName())) {
                throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "权限不足");
            }
        }

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("payerMerchantId", payerMerchantId); //所属码商
        params.put("merchantId", merchantId); //所属码商
        params.put("channelCode", channelCode);
        params.put("status", status);
        params.put("payerName", payerName); //持卡人
        params.put("payerAddr", payerAddr);
        params.put("payerIdentity", payerIdentity);
        params.put("payerCodePrice", payerCodePrice);
        params.put("coinType", coinType);
        params.put("remark", HtmlUtil.delHTMLTag(remark));

        Integer countCoinHolder = iCoinHolderInfoService.selectCountCoinHolder(params);

        Map<String, Object> resultMap = new HashMap<String, Object>();
        resultMap.put("countCoinHolder", countCoinHolder);
        return new ReturnMsg(resultMap);

    }


    @RequestMapping(value = "/batchUploadImage", method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    public ReturnMsg batchUploadImage(HttpServletRequest request) throws TranException {
        // 打印接收到的数据
        // 获取alipayInfoId
        String alipayInfoId = (String) request.getParameter("alipayInfoId");
        String files = request.getParameter("files");
        JSONArray jsonArray = JSONArray.parseArray(files);

        String saveFilename = "";
        List<Map<String,Object>> imagesList = new ArrayList<>();
        for (Object obj : jsonArray) {
            JSONObject jsonObj = (JSONObject) obj;
            String fileName = jsonObj.getString("fileName");
            String imageUrl = jsonObj.getString("imageUrl");
            String base64Image = imageUrl; //.split(',')[1] 前端已去掉 data:image/png;base64, 部分
            saveFilename = fileName.substring(0, fileName.length() - 4);
            //String uniqueFileName = UUID.randomUUID().toString() + ".png";
            String filePath = staticAccessPath + saveFilename + ".png"; // 替换为实际的保存路径

            if (containsChineseOrSpace(saveFilename)) {
                return new ReturnMsg().setFail("" + saveFilename + "含有中文名称或空格,请输入正确的订单编号");
            }
            //检查是否已有重复
            CoinHolderInfo holderInfo = iCoinHolderInfoService.getOne(new QueryWrapper<CoinHolderInfo>().lambda().eq(CoinHolderInfo::getPayerAddr, saveFilename));
            if (holderInfo != null) {
                return new ReturnMsg().setFail("二维码" + saveFilename + "已存在");
            }
            boolean isSave = saveBase64Image(base64Image, filePath);
            if (!isSave) {
                return new ReturnMsg().setFail("保存二维码" + saveFilename + "有误");
            }else {
                //添加进 coinHolderInfo
                Map<String, Object> params = new HashMap<String, Object>();
                params.put("name",saveFilename);
                params.put("payerAddr",saveFilename);
                imagesList.add(params);
            }
        }

        return new ReturnMsg(imagesList); //返回图片名称

    }

    //删除已付款 某个支付宝商家的
    @RequestMapping(value = "/batchDelPayerPayedList", method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    public ReturnMsg batchDelPayerPayedList(
             String businessType,
             String alipayInfoId,
             String payerMerchantId,
             String payerOperatorId,
             String tableMerchantId
    ) throws TranException {
        //管理员
        PayMerchantInfo merchantInfo = iPayMerchantInfoService.getOne(new QueryWrapper<PayMerchantInfo>().lambda().eq(PayMerchantInfo::getMerchantId, tableMerchantId));

        if (payerMerchantId == null || payerOperatorId == null ) {
            if (merchantInfo.getAccountType().equals(EnumAccountType.MERCHANT.getName())) {
                if (merchantInfo.getAgentRate().equals(EnumAgentRate.CARD.getName()) && payerMerchantId == null) {
                    throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "码商权限不足");
                }
                if (merchantInfo.getAgentRate().equals(EnumAgentRate.OPERATOR.getName()) && payerOperatorId == null) {
                    throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "码商操作员权限不足");
                }
            }
        }
        //收款款项 payerCodeTimes 才会为 0
        iCoinHolderInfoService.remove(new QueryWrapper<CoinHolderInfo>().lambda()
                .eq(CoinHolderInfo::getStatus, CodeConst.PayerCardStatusConst.CARD_STATUS_DISABLE)
                .eq(CoinHolderInfo::getBusinessType,businessType)
                .eq(CoinHolderInfo::getAlipayInfoId,alipayInfoId)
                .eq(CoinHolderInfo::getPayerCodeTimes,0)
        );

        return new ReturnMsg();
    }

    private static boolean containsChineseOrSpace(String str) {
        // 正则表达式检查中文字符和空格
        Pattern pattern = Pattern.compile("[\\u4e00-\\u9fa5\\s]");
        return pattern.matcher(str).find();
    }
    // 检查Base64字符串是否是有效图片且不超过指定大小
    public static boolean isValidImage(String base64Image) {
        try {
            byte[] imageBytes = Base64.getDecoder().decode(base64Image);
            if (imageBytes.length > MAX_IMAGE_SIZE) {
                System.out.println("The image size exceeds the 5MB limit.");
                return false;
            }

            ByteArrayInputStream bis = new ByteArrayInputStream(imageBytes);
            BufferedImage bufferedImage = ImageIO.read(bis);

            if (bufferedImage == null) {
                return false;
            }

            // 检查图片的基本属性（如颜色模式）
            ColorModel cm = bufferedImage.getColorModel();
            return (cm != null && cm.getPixelSize() > 0);

        } catch (IOException e) {
            return false;
        }
    }

    // 保存Base64编码的图片为PNG文件
    public static  boolean saveBase64Image(String base64Image, String filePath) {
        if (!isValidImage(base64Image)) {
            System.out.println("The provided string is not a valid image or exceeds the size limit.");
            return false;
        }
        try {
            byte[] imageBytes = Base64.getDecoder().decode(base64Image);
            ByteArrayInputStream bis = new ByteArrayInputStream(imageBytes);
            BufferedImage bufferedImage = ImageIO.read(bis);

            if (bufferedImage != null) {
                File outputFile = new File(filePath);
                ImageIO.write(bufferedImage, "png", outputFile);
                System.out.println("Image saved to: " + filePath);
                return true;
            } else {
                System.out.println("Could not decode the image.");
                return false;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    private static void generateAndSaveQRCode(String text, String filePath, String fileType) {
        int size = 300;

        Map<EncodeHintType, Object> hints = new HashMap<>();
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");

        try {
            Writer writer = new QRCodeWriter();
            BitMatrix bitMatrix = writer.encode(text, BarcodeFormat.QR_CODE, size, size, hints);

            BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_RGB);
            for (int x = 0; x < size; x++) {
                for (int y = 0; y < size; y++) {
                    image.setRGB(x, y, bitMatrix.get(x, y) ? 0xFF000000 : 0xFFFFFFFF);
                }
            }

            File qrCodeFile = new File(filePath);
            ImageIO.write(image, fileType, qrCodeFile);
            System.out.println("QR Code generated and saved successfully.");

        } catch (WriterException | IOException e) {
            e.printStackTrace();
        }
    }

}