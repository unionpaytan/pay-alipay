package com.bootpay.mng.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bootpay.common.authenticator.GoogleAuthenticator;
import com.bootpay.common.constants.CodeConst;
import com.bootpay.common.constants.CodeConst.GoogleConst;
import com.bootpay.common.constants.enums.EnumAccountType;
import com.bootpay.common.constants.enums.EnumFlowType;
import com.bootpay.common.constants.enums.EnumMerchantStatusType;
import com.bootpay.common.excetion.TranException;
import com.bootpay.common.framework.AvoidDuplicateFormToken;
import com.bootpay.common.framework.RedisUtil;
import com.bootpay.common.utils.DateUtil;
import com.bootpay.common.utils.EncryptUtil;
import com.bootpay.common.utils.HtmlUtil;
import com.bootpay.common.utils.ReturnMsg;
import com.bootpay.core.entity.*;
import com.bootpay.core.service.*;
import com.bootpay.mng.service.AuthenticatorService;
import com.bootpay.mng.service.CardChannelManagerService;
import com.bootpay.mng.service.CardWithdrawManagerService;
import com.bootpay.mng.service.MerchantAccountManagerService;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.*;

/**
 *
 * 钱包管理
 *
 * @author 
 * @since 08/06/2021
 *
 */
@RestController
@RequestMapping("/coin")
public class CoinPayerInfoController {

    private Logger _log = LoggerFactory.getLogger(CoinPayerInfoController.class);

    @Value("${app.privateAESKey}") //application.yml文件读取
    private String privateAESKey;

    @Autowired
    private AuthenticatorService authenticatorService;

    @Autowired
    private ICardPayerInfoService iCardPayerInfoService; //持卡人service

    @Autowired
    private ICoinPayerInfoService iCoinPayerInfoService; //钱包


    @Autowired
    private ICardPayerFlowInfoService iCardPayerFlowInfoService; //银行卡明细

    @Autowired
    private IBankCodeInfoService iBankCodeInfoService;//银行通道

    @Autowired
    private ICardChannelInfoService iCardChannelInfoService; //卡通道

    @Autowired
    private ICardChannelFlowInfoService iCardChannelFlowInfoService; //卡通道明细

    @Autowired
    private ICardPayerWithdrawInfoService iCardPayerWithdrawInfoService; //银行卡代付订单列表

    @Autowired
    private CardChannelManagerService cardChannelManagerService;

    @Autowired
    private IPayMerchantInfoService iPayMerchantInfoService;

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private CardWithdrawManagerService cardWithdrawManagerService;

    @Autowired
    private MerchantAccountManagerService merchantAccountManagerService;

    /**
     * 查询商家钱包列表
     * @param rows
     * @param page
     * @return
     */
    @RequestMapping(value = "/queryMerchantCoinPayerInfoPage", method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    public ReturnMsg queryMerchantCoinPayerInfoPage(
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

        String hasChannelCode = merchantInfo.getAccountType().equals(EnumAccountType.MERCHANT.getName()) ? merchantInfo.getChannelCode() : channelCode;

        if("".equals(hasChannelCode) || hasChannelCode == null){
            if (merchantInfo.getAccountType().equals(EnumAccountType.MERCHANT.getName())){
                throw new TranException(CodeConst.BUSINESS_ERROR_CODE,"权限不足");
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

        IPage<Map<String, Object>> pageMap = iCoinPayerInfoService.queryMerchantCoinPayerInfoPage(pages, params);

        Map<String, Object> resultMap = new HashMap<String, Object>();
        resultMap.put("pages", pageMap);
        return new ReturnMsg(resultMap);

    }


}