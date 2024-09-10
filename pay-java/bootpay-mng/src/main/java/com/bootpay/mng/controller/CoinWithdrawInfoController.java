package com.bootpay.mng.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bootpay.common.authenticator.GoogleAuthenticator;
import com.bootpay.common.constants.CodeConst;
import com.bootpay.common.constants.CodeConst.GoogleConst;
import com.bootpay.common.constants.enums.EnumAccountType;
import com.bootpay.common.constants.enums.EnumChannelStatus;
import com.bootpay.common.constants.enums.EnumFlowType;
import com.bootpay.common.constants.enums.EnumMerchantStatusType;
import com.bootpay.common.excetion.TranException;
import com.bootpay.common.framework.AvoidDuplicateFormToken;
import com.bootpay.common.framework.RedisUtil;
import com.bootpay.common.utils.*;
import com.bootpay.core.entity.*;
import com.bootpay.core.service.*;
import com.bootpay.mng.service.*;
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
public class CoinWithdrawInfoController {

    private Logger _log = LoggerFactory.getLogger(CoinWithdrawInfoController.class);

    @Value("${app.privateAESKey}") //application.yml文件读取
    private String privateAESKey;

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
    private ICoinChannelWithdrawInfoService iCoinChannelWithdrawInfoService; //COIN代付

    @Autowired
    private ICoinChannelDepositInfoService iCoinChannelDepositInfoService; //收

    @Autowired
    private ICoinChannelWithdrawFlowService iCoinChannelWithdrawFlowService; //COIN代付


    @Autowired
    private ICoinPayerWithdrawInfoService iCoinPayerWithdrawInfoService; //COIN代付

    @Autowired
    private  ICoinPayerWithdrawFlowService iCoinPayerWithdrawFlowService;
    @Autowired
    private ICoinPayerInfoService iCoinPayerInfoService;

    @Autowired
    private CoinWithdrawManagerService coinWithdrawManagerService;

    @Autowired
    private MerchantAccountManagerService merchantAccountManagerService;

    /**
     * 获取三方代付订单列表
     *
     * @param
     * @param rows 条数
     * @param page 页码
     * @return
     */
    @RequestMapping(value = "/queryCoinPayerWithdrawPage", method = {RequestMethod.POST, RequestMethod.GET}, name = "admin")
    @ResponseBody
    public ReturnMsg queryCoinPayerWithdrawPage(
            String merchantWithdrawNo,
            String withdrawNo,
            String channelWithdrawNo,
            String payerName,
            String coinType,
            String payerMerchantId, 
            String payerAddr,
            String acctName,
            String acctAddr,
            String channelCode,
            String status,
            String beginTime,
            String endTime,
            String remark,
            String tableMerchantId,
            @NotNull(message = "行数不能为空") Integer rows,
            @NotNull(message = "页码不能为空") Integer page
    ) throws TranException {

        PayMerchantInfo merchantInfo = iPayMerchantInfoService.getOne(new QueryWrapper<PayMerchantInfo>().lambda().eq(PayMerchantInfo::getMerchantId, tableMerchantId));
//        if (merchantInfo.getAccountType().equals(EnumAccountType.MERCHANT.getName())) {
//            throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "权限不足");
//        }

        Page<Map<String, Object>> pages = new Page<>(page, rows);

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("merchantWithdrawNo", merchantWithdrawNo);
        params.put("withdrawNo", withdrawNo);
        params.put("channelWithdrawNo", channelWithdrawNo);
        params.put("status", status);
        params.put("channelCode", channelCode);
        params.put("coinType", coinType); //0TRX | 1-ETH
        params.put("payerName", payerName);
        params.put("payerMerchantId", payerMerchantId); //卡主编号
        params.put("payerAddr", payerAddr);
        params.put("acctName", acctName); //PAYEE
        params.put("acctAddr", acctAddr);
        params.put("beginTime", beginTime + " 00:00:00");
        params.put("endTime", endTime + " 23:59:59");
        params.put("remark", HtmlUtil.delHTMLTag(remark)); //PAYEE
        IPage<Map<String, Object>> pageMap = iCoinPayerWithdrawInfoService.queryCoinPayerWithdrawInfoPage(pages, params);

        //TODO:params 增加  coinType 自动 人工 payerMerchantId 卡主编号
        //2统计交易订单
        BigDecimal sumCardPayerWithdraw = iCoinPayerWithdrawInfoService.sumCoinPayerWithdrawInfo(params);
        Integer countCardPayerWithdraw = iCoinPayerWithdrawInfoService.countCoinPayerWithdraw(params);

        params.put("status", ""); //下发手续费不受订单状态选项的影响
        Map<String, Object> sumAllWithdrawFeeMap = iCoinPayerWithdrawInfoService.sumCoinPayerWithdrawFee(params);

        params.put("status", CodeConst.WithdrawStatusConst.WITHDRAW_FAIL);
        Map<String, Object> sumFailWithdrawFeeMap = iCoinPayerWithdrawInfoService.sumCoinPayerWithdrawFee(params);

        //查询结果 map
        BigDecimal sumPayerSingleFee = new BigDecimal(sumAllWithdrawFeeMap.get("sumPayerSingleFee").toString());
        BigDecimal sumPayerRateFee   = new BigDecimal(sumAllWithdrawFeeMap.get("sumPayerRateFee").toString());

        BigDecimal sumPayerFailFee     = new BigDecimal(sumFailWithdrawFeeMap.get("sumPayerSingleFee").toString());
        BigDecimal sumPayerFailRateFee = new BigDecimal(sumFailWithdrawFeeMap.get("sumPayerRateFee").toString());

        Map<String, Object> resultMap = new HashMap<String, Object>();
        resultMap.put("sumCardPayerWithdraw", sumCardPayerWithdraw);
        resultMap.put("countCardPayerWithdraw", countCardPayerWithdraw);
        resultMap.put("sumCardPayerWithdrawFee", sumPayerSingleFee.subtract(sumPayerFailFee)); //总下发单笔手续费 - 失败的手续费
        resultMap.put("sumCardPayerWithdrawRateFee", sumPayerRateFee.subtract(sumPayerFailRateFee)); //总下发手续费汇率
        resultMap.put("pages", pageMap);

        return new ReturnMsg(resultMap);
    }

    /**
     * 查询代理下级商户银行卡列表
     * @return
     */
    @RequestMapping(value = "/queryAgentsMerchantCoinPayerInfoPage", method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    public ReturnMsg queryAgentsMerchantCoinPayerInfoPage(
            String channelCode,//查询某一个银行卡通道
            String channelName,
            String coinType,
            String status,
            String payerName,
            String payerAddr,
            String tableMerchantId,//代理ID
            Integer rows,
            Integer page) throws TranException {

        //查询某一个代理
        if (tableMerchantId == null || "".equals(tableMerchantId)){
            throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "代理号不能为空");
        }

        PayMerchantInfo merchantInfo = iPayMerchantInfoService.getOne(new QueryWrapper<PayMerchantInfo>().lambda().eq(PayMerchantInfo :: getMerchantId, tableMerchantId));
        if(merchantInfo == null){
            throw new TranException(CodeConst.BUSINESS_ERROR_CODE,"无此代理号");
        }

        //根据ParentId找到下级商户
        List<PayMerchantInfo> merchantInfoList = iPayMerchantInfoService.list(new QueryWrapper<PayMerchantInfo>().lambda().eq(PayMerchantInfo::getParentId,tableMerchantId));
        if (merchantInfoList.isEmpty()){
            throw new TranException(CodeConst.BUSINESS_ERROR_CODE,"代理名下还无下级商户");
        }

        List<String> list = new ArrayList<>();
        List<String> channelCodeList = new ArrayList<>(); //银行卡所属通道

        for (PayMerchantInfo info: merchantInfoList){
            list.add(info.getMerchantId()); //总代直接下挂的商户(无中间代理)
            channelCodeList.add(info.getChannelCode());//总代直接下级商户的通道号

            List<PayMerchantInfo>  agentList = iPayMerchantInfoService.list(new QueryWrapper<PayMerchantInfo>().lambda().eq(PayMerchantInfo::getParentId,info.getMerchantId()));
            if (agentList != null){
                for (PayMerchantInfo info2:agentList){
                    list.add(info2.getMerchantId());//总代下级代理下挂的商户
                    channelCodeList.add(info2.getChannelCode()); //总代下级代理的下挂商户
                }
            }
        }
        //加入总代的ID才可查到代理
        list.add(tableMerchantId);
        //总代 下级：代理+商户
        //代理 下级：纯商户
        _log.info("===>>>代理:{},下级商户:{}",tableMerchantId,list);

        if (channelCodeList.isEmpty()){
            throw new TranException(CodeConst.BUSINESS_ERROR_CODE,"代理名下商户暂未分配银行卡");
        }

        Page<Map<String, Object>> pages = new Page<>(page, rows);
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("channelCode", channelCode);//某一个卡通道 但只限于代理名下
        params.put("channelCodeList",channelCodeList);//代理名下商户的卡通道列表
        params.put("channelName", channelName);
        params.put("coinType", coinType); //持卡人银行
        params.put("payerName", payerName); //持卡人
        params.put("status", status);
        params.put("payerAddr", payerAddr);
        params.put("date", DateUtil.getDate());

        IPage<Map<String, Object>> pageMap = iCoinPayerInfoService.queryAgentsCoinPayerInfoPage(pages, params);

        //代理下级商户银行卡单卡单笔最高可提现
        Map<String, Object> highestAmtMap = iCoinPayerInfoService.queryAgentsCoinPayerHighestAmt(params);
        //代理下级商户银行卡总余额
        BigDecimal sumPayerAmt = iCoinPayerInfoService.queryAgentsCoinPayerSumAmt(params);

        //_log.info("银行卡列表===>>>{}", pageMap.getRecords()); //封装
        Map<String, Object> resultMap = new HashMap<String, Object>();
        resultMap.put("pages", pageMap);
        resultMap.put("highestAmt", (highestAmtMap != null) ? highestAmtMap.get("highestAmt").toString() : 0);
        resultMap.put("sumPayerAmt", sumPayerAmt);
        return new ReturnMsg(resultMap);

    }


}