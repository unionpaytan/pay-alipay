package com.bootpay.mng.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bootpay.common.authenticator.GoogleAuthenticator;
import com.bootpay.common.constants.CodeConst;
import com.bootpay.common.constants.MyRedisConst;
import com.bootpay.common.constants.enums.EnumAccountType;
import com.bootpay.common.constants.enums.EnumAgentRate;
import com.bootpay.common.constants.enums.EnumFlowType;
import com.bootpay.common.constants.enums.EnumMerchantStatusType;
import com.bootpay.common.excetion.TranException;
import com.bootpay.common.framework.RedisUtil;
import com.bootpay.common.utils.*;
import com.bootpay.core.entity.*;
import com.bootpay.core.service.*;
import com.bootpay.mng.service.*;
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
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.*;

/**
 * <p>
 * 币址通道管理
 * </p>
 *
 * @author 
 * @since 2021-08-12
 */
@Validated
@RestController
@RequestMapping("/coin/deposit")
public class CoinDepositInfoController {

    private Logger _log = LoggerFactory.getLogger(CoinDepositInfoController.class);

    @Value("${app.privateAESKey}") //application.yml文件读取
    private String privateAESKey;

    @Value("${app.walletPrivateKeySalt}") //application.yml文件读取
    private String walletPrivateKeySalt;


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
    private IPayAgentChannelSiteService iPayAgentChannelSiteService; //代理通道


    @Autowired
    private IPayMerchantMoneyChangeService iPayMerchantMoneyChangeService; //商户流水明细

    @Autowired
    private IPayMerchantBalanceService iPayMerchantBalanceService; //商户余额服务

    @Autowired
    private IPayAgentBalanceService iPayAgentBalanceService; //商户余额服务


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
    private ICoinHolderDepositInfoService iCoinHolderDepositInfoService; //COIN代收

    @Autowired
    private  ICoinPayerWithdrawFlowService iCoinPayerWithdrawFlowService;
    @Autowired
    private ICoinPayerInfoService iCoinPayerInfoService;

    @Autowired
    private CoinWithdrawManagerService coinWithdrawManagerService;

    @Autowired
    private MerchantAccountManagerService merchantAccountManagerService;

    @Autowired
    private ICoinHolderInfoService iCoinHolderInfoService;

    @Autowired
    private IPayDepositInfoService iPayDepositInfoService;

    @Autowired
    private NotifyService notifyService; //通知商家服务

    @Autowired
    private IPayMerchantChannelSiteService iPayMerchantChannelSiteService; //通知商家服务

    @Autowired
    private IPayAgentBalanceFlowService iPayAgentBalanceFlowService;

    @Autowired
    private IIpListService ipListService;

    @Autowired
    private IIpBlockListService ipBlockListService;

    @Autowired
    private BarcodeManagerService barcodeManagerService;

    @Autowired
    private IAgentProfitBalanceService iAgentProfitBalanceService;

    /**
     * 获取三方代付订单列表
     *
     * @param
     * @param rows 条数
     * @param page 页码
     * @return
     */
    @RequestMapping(value = "/queryCoinPayerDepositPage", method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    public ReturnMsg queryCoinPayerDepositPage(
            String merchantId,
            String merchantName,
            String merchantWithdrawNo,
            String withdrawNo,
            String channelWithdrawNo,
            String payerName,
            String payerCodePrice,
            String coinType,
            String payerMerchantId,
            String payerOperatorId,
            String payerIdentity,
            String fromAddr,
            String payerAddr,
            String channelCode,
            String status,
            String beginTime,
            String endTime,
            String remark,
            String clientIp,
            String tableMerchantId,
            @NotNull(message = "行数不能为空") Integer rows,
            @NotNull(message = "页码不能为空") Integer page
    ) throws TranException {

        PayMerchantInfo merchantInfo = iPayMerchantInfoService.getOne(new QueryWrapper<PayMerchantInfo>().lambda().eq(PayMerchantInfo::getMerchantId, tableMerchantId));
        if (payerMerchantId == null && payerOperatorId == null) {
            if (merchantInfo.getAccountType().equals(EnumAccountType.MERCHANT.getName())) {
                throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "权限不足");
            }
        }
        Page<Map<String, Object>> pages = new Page<>(page, rows);

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("merchantId", merchantId);
        params.put("merchantName", merchantName);
        params.put("payerCodePrice", payerCodePrice);
        params.put("merchantWithdrawNo", merchantWithdrawNo);
        params.put("withdrawNo", withdrawNo);
        params.put("channelWithdrawNo", channelWithdrawNo);
        params.put("status", status);
        params.put("channelCode", channelCode);
        params.put("coinType", coinType); //0TRX | 1-ETH
        params.put("payerName", payerName);
        params.put("payerMerchantId", payerMerchantId); //码商编号
        params.put("payerOperatorId", payerOperatorId); //码商操作员编号
        params.put("payerIdentity", payerIdentity); //收款人编号
        params.put("fromAddr", fromAddr); //付款钱包
        params.put("payerAddr", payerAddr);
        params.put("clientIp", clientIp);
        params.put("beginTime", beginTime + " 00:00:00");
        params.put("endTime", endTime + " 23:59:59");
        params.put("remark", HtmlUtil.delHTMLTag(remark)); //PAYEE
        IPage<Map<String, Object>> pageMap = iCoinHolderDepositInfoService.queryCoinHolderDepositInfoPage(pages, params);

        List<Map<String,Object>> records = pageMap.getRecords();
        List<Map<String,Object>> newRecords = new ArrayList<>();
        long beginMills = System.currentTimeMillis();
        if (!records.isEmpty()) {
            for(Map<String,Object> itemRecord :records){
                String itemClientIp = String.valueOf(itemRecord.get("clientIp"));//一个收款商户可以对应多个收款通道
                Map<String,Object> ipParams = new HashMap<String,Object>();
                ipParams.put("clientIp",itemClientIp);
                ipParams.put("beginTime",beginTime + " 00:00:00");
                ipParams.put("endTime",endTime + " 23:59:59");
                //_log.info("ipParams:{}",ipParams);
                Integer countBlockIpTimes = ipBlockListService.selectCountIp(ipParams);
                Integer countIpTimes = ipListService.selectCountIp(ipParams);
                itemRecord.put("countBlockIpTimes",countBlockIpTimes);//码商的所有码
                itemRecord.put("countIpTimes",countIpTimes);//码商挂码总数
                newRecords.add(itemRecord);
            }
        }
        pageMap.setRecords(newRecords); //更新记录

        long endMills = System.currentTimeMillis();
         _log.info("订单列表获取付款人IP毫秒数:{}",(endMills - beginMills));

        //TODO:params 增加  coinType 自动 人工 payerMerchantId 卡主编号
        //2统计交易订单
        Map<String, Object> depositInfoMap = iCoinHolderDepositInfoService.sumCoinHolderDepositInfo(params);
        Integer countCoinPayerWithdraw = iCoinHolderDepositInfoService.countCoinHolderDeposit(params);

        params.put("status", CodeConst.WithdrawStatusConst.WITHDRAW_SUCCESS); //代收手续费只有在功才会记入
        Map<String, Object> successDepositInfoMap = iCoinHolderDepositInfoService.sumCoinHolderDepositInfo(params);

        Map<String, Object> sumAllWithdrawFeeMap = iCoinHolderDepositInfoService.sumCoinHolderDepositFee(params);
        Integer successCount = iCoinHolderDepositInfoService.countCoinHolderDeposit(params);

        //查询结果 map
        BigDecimal sumPayerSingleFee = new BigDecimal(sumAllWithdrawFeeMap.get("sumPayerSingleFee").toString());
        BigDecimal sumPayerRateFee   = new BigDecimal(sumAllWithdrawFeeMap.get("sumPayerRateFee").toString());

        Map<String, Object> resultMap = new HashMap<String, Object>();
        resultMap.put("sumCoinPayerAmt", depositInfoMap.get("sumCoinPayerAmt").toString()); //总交易
        resultMap.put("sumCoinPayerAmtCredit", depositInfoMap.get("sumCoinPayerAmtCredit").toString()); //总入账
        resultMap.put("countCoinPayerWithdraw", countCoinPayerWithdraw);

        resultMap.put("sumCoinPayerWithdrawFee", sumPayerSingleFee); //总下发单笔 手续费 - 失败的手续费
        resultMap.put("sumCoinPayerWithdrawRateFee", sumPayerRateFee); //总下发手续费汇率

        resultMap.put("successAmt", successDepositInfoMap.get("sumCoinPayerAmt")); //成功交易金额
        resultMap.put("successCount", successCount); //成功次数
        resultMap.put("pages", pageMap);

        return new ReturnMsg(resultMap);
    }

    @RequestMapping(value = "/queryCoinHolderDepositList", method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    public ReturnMsg queryCoinHolderDepositList(
            String merchantId,
            String merchantName,
            String beginTime,
            String endTime,
            HttpServletRequest request) throws TranException {

        //管理员查询 商户不可查询全部订单
        if (merchantId == null || "".equals(merchantId)) {
            String token = request.getHeader("authorization");
            if (!authenticatorService.checkPermission(token).equals("admin")) {
                throw new TranException(CodeConst.PERMISSION_CODE, "无权访问");
            }
        }

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("merchantId",merchantId);
        params.put("merchantName",merchantName);
        params.put("beginTime", beginTime + " 00:00:00");
        params.put("endTime", endTime + " 23:59:59");

        //统计订单数
        Map<String, Object> totalNumSumAmtMap = iCoinHolderDepositInfoService.queryTotalNumSumAmt(params);
        //统计成功订单数
        params.put("status",CodeConst.WithdrawStatusConst.WITHDRAW_SUCCESS);
        Map<String, Object> totalSuccessNumSumAmtMap = iCoinHolderDepositInfoService.queryTotalNumSumAmt(params);


        Map<String,Object> profitMap =  iAgentProfitBalanceService.selectSumAccountBalance(params); //码商分润总余额
        BigDecimal sumProfitBalance = new BigDecimal(0);
        if (profitMap != null) {
            sumProfitBalance = new BigDecimal(profitMap.get("sumAccountBalance").toString());
        }

        //String totalSuccessRate = String.valueOf(new BigDecimal(String.valueOf(totalSuccessNumSumAmtMap.get("countNum"))).divide(new BigDecimal(String.valueOf(totalNumSumAmtMap.get("countNum")))));
        Map<String, Object> result = new HashMap<String, Object>();
        result.put("totalCountNum", totalNumSumAmtMap.get("countNum"));
        result.put("totalSumAmt", totalNumSumAmtMap.get("sumAmt"));
        result.put("totalSuccessCountNum", totalSuccessNumSumAmtMap.get("countNum"));
        result.put("totalSuccessSumAmt", totalSuccessNumSumAmtMap.get("sumAmt"));
        result.put("totalSumProfit",sumProfitBalance);
        return new ReturnMsg(result);
    }

    @RequestMapping(value = "/queryFollowerOrderList", method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    public ReturnMsg queryFollowerOrderList(
            String merchantWithdrawNo,
            String withdrawNo,
            String channelWithdrawNo,
            String payerName,
            String coinType,
            String payerMerchantId,
            String payerIdentity,
            String fromAddr,
            String payerAddr,
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
        if (payerIdentity == null) {
            if (merchantInfo.getAccountType().equals(EnumAccountType.MERCHANT.getName())) {
                throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "权限不足");
            }
        }
        Page<Map<String, Object>> pages = new Page<>(page, rows);

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("merchantWithdrawNo", merchantWithdrawNo);
        params.put("withdrawNo", withdrawNo);
        params.put("channelWithdrawNo", channelWithdrawNo);
        params.put("status", status);
        params.put("channelCode", channelCode);
        params.put("coinType", coinType); //0TRX | 1-ETH
        params.put("payerName", payerName);
        params.put("payerMerchantId", payerMerchantId); //码商编号
        params.put("payerIdentity", payerIdentity); //收款人编号
        //params.put("fromAddr", fromAddr); //付款钱包
        params.put("payerAddr", payerAddr);
        params.put("beginTime", beginTime + " 00:00:00");
        params.put("endTime", endTime + " 23:59:59");
        params.put("remark", HtmlUtil.delHTMLTag(remark)); //PAYEE
        IPage<Map<String, Object>> pageMap = iCoinHolderDepositInfoService.queryCoinHolderDepositInfoPage(pages, params);

        //TODO:params 增加  coinType 自动 人工 payerMerchantId 卡主编号
        //2统计交易订单
        Map<String, Object> depositInfoMap = iCoinHolderDepositInfoService.sumCoinHolderDepositInfo(params);
        Integer countCoinPayerWithdraw = iCoinHolderDepositInfoService.countCoinHolderDeposit(params);

        params.put("status", CodeConst.WithdrawStatusConst.WITHDRAW_SUCCESS); //代收手续费只有在功才会记入
        Map<String, Object> sumAllWithdrawFeeMap = iCoinHolderDepositInfoService.sumCoinHolderDepositFee(params);


        //查询结果 map
        BigDecimal sumPayerSingleFee = new BigDecimal(sumAllWithdrawFeeMap.get("sumPayerSingleFee").toString());
        BigDecimal sumPayerRateFee   = new BigDecimal(sumAllWithdrawFeeMap.get("sumPayerRateFee").toString());

        Map<String, Object> resultMap = new HashMap<String, Object>();
        resultMap.put("sumCoinPayerAmt", depositInfoMap.get("sumCoinPayerAmt").toString()); //总交易
        resultMap.put("sumCoinPayerAmtCredit", depositInfoMap.get("sumCoinPayerAmtCredit").toString()); //总入账
        resultMap.put("countCoinPayerWithdraw", countCoinPayerWithdraw);

        resultMap.put("sumCoinPayerWithdrawFee", sumPayerSingleFee); //总下发单笔 手续费 - 失败的手续费
        resultMap.put("sumCoinPayerWithdrawRateFee", sumPayerRateFee); //总下发手续费汇率
        resultMap.put("pages", pageMap);

        return new ReturnMsg(resultMap);
    }

    /**
     * 查询代理下级商户银行卡列表
     * @return
     */
    @RequestMapping(value = "/queryAgentsMerchantCoinHolderInfoPage", method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    public ReturnMsg queryAgentsMerchantCoinHolderInfoPage(
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
        List<PayMerchantInfo> merchantInfoList = iPayMerchantInfoService.list(new QueryWrapper<PayMerchantInfo>()
                .lambda()
                .eq(PayMerchantInfo::getParentId,tableMerchantId)
        );
        if (merchantInfoList.isEmpty()){
            throw new TranException(CodeConst.BUSINESS_ERROR_CODE,"代理名下还无下级商户");
        }

        List<String> list = new ArrayList<>();
        List<String> channelCodeList = new ArrayList<>(); //银行卡所属通道

        for (PayMerchantInfo info: merchantInfoList){
            list.add(info.getMerchantId()); //总代直接下挂的商户(无中间代理)
            channelCodeList.add(info.getDepositChannelCode());//总代直接下级商户(代收)的通道号

            List<PayMerchantInfo>  agentList = iPayMerchantInfoService.list(new QueryWrapper<PayMerchantInfo>().lambda().eq(PayMerchantInfo::getParentId,info.getMerchantId()));
            if (agentList != null){
                for (PayMerchantInfo info2:agentList){
                    list.add(info2.getMerchantId());//总代下级代理下挂的商户
                    channelCodeList.add(info2.getDepositChannelCode()); //总代下级代理的下挂商户
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

        IPage<Map<String, Object>> pageMap = iCoinHolderInfoService.queryAgentsCoinHolderInfoPage(pages, params);


        //代理下级商户银行卡总余额
        BigDecimal sumPayerAmt = iCoinHolderInfoService.queryAgentsCoinHolderSumAmt(params);

        //_log.info("银行卡列表===>>>{}", pageMap.getRecords()); //封装
        Map<String, Object> resultMap = new HashMap<String, Object>();
        resultMap.put("pages", pageMap);
        resultMap.put("sumPayerAmt", sumPayerAmt);
        return new ReturnMsg(resultMap);

    }
    @RequestMapping(value = "/setDepositPreStatusSuccess", method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    @Transactional(rollbackFor = Exception.class)
    public ReturnMsg setDepositPreStatusSuccess(String googleCode,
                                                @NotBlank(message = "平台订单号不能为空") String withdrawNo,
                                                @NotBlank(message = "管理员ID不能为空") String tableMerchantId) throws Exception {
        PayMerchantInfo merchantInfo = iPayMerchantInfoService.getOne(new QueryWrapper<PayMerchantInfo>().lambda().eq(PayMerchantInfo::getMerchantId, tableMerchantId));
        if (!merchantInfo.getAgentRate().equals(EnumAgentRate.FOLLOWERS.getName())) {
            String googleKeyDecode = EncryptUtil.aesDecrypt(merchantInfo.getGoogleKey(), walletPrivateKeySalt);
            if (!GoogleAuthenticator.check_code(googleKeyDecode, googleCode, System.currentTimeMillis())) {
                throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "google验证码错误");
            }

        }

        CoinHolderDepositInfo holderDepositInfo = iCoinHolderDepositInfoService.getOne(new QueryWrapper<CoinHolderDepositInfo>().lambda()
                .eq(CoinHolderDepositInfo::getWithdrawNo, withdrawNo));

        if (holderDepositInfo == null) {
            throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "订单不存在");
        }

        //订单[排队中]才可修正 + 查询的任务也不可设为失败
        if (!holderDepositInfo.getTaskType().equals(CodeConst.TaskTypeConst.TRANSFER_TO_CARD)) {
            throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "非打款任务,不可设为成功");
        }

        //订单[排队中]才可修正 + 查询的任务也不可设为失败
        if (!holderDepositInfo.getStatus().equals(CodeConst.WithdrawStatusConst.WITHDRAW_UNKNOWN)) {
            throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "订单状态非[待支付],不可设为成功");
        }

        if (merchantInfo.getMerchantName().equals(EnumAccountType.MERCHANT.getName())) {
            if (!holderDepositInfo.getPayerIdentity().equals(tableMerchantId)){
                throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "权限不足");
            }
        }

        CoinHolderInfo holderInfo = iCoinHolderInfoService.getOne(new QueryWrapper<CoinHolderInfo>().lambda()
                .eq(CoinHolderInfo::getPayerAddr, holderDepositInfo.getPayerAddr()));

        if (holderInfo == null) {
            throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "收款码不存在");
        }

        CoinHolderDepositInfo holderEntity = new CoinHolderDepositInfo();
        holderEntity.setRemark("收款待审核");
        holderEntity.setPreStatus(CodeConst.PreStatusConst.WITHDRAW_SUCCESS);//由收款人收到款项
        boolean isUpdHolder = iCoinHolderDepositInfoService.update(holderEntity,new UpdateWrapper<CoinHolderDepositInfo>().lambda()
                .eq(CoinHolderDepositInfo::getWithdrawNo, withdrawNo)); //保存更改
        if (!isUpdHolder){
            throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "三方订单更新失败");
        }
        Map<String, Object> resultMap = new HashMap<String, Object>();
        resultMap.put("withdrawNo",withdrawNo);
        return new ReturnMsg(resultMap);

    }
    /**
     * 代收订单设为成功
     *
     * @param withdrawNo
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/setDepositStatusSuccess", method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    @Transactional(rollbackFor = Exception.class)
    public ReturnMsg setDepositStatusSuccess(
            String googleCode,
            @NotBlank(message = "平台订单号不能为空") String withdrawNo,
            @NotBlank(message = "管理员ID不能为空") String tableMerchantId
    ) throws Exception {

        //查询管理员(谁操作成功的)
        PayMerchantInfo merchantInfo = iPayMerchantInfoService.getOne(new QueryWrapper<PayMerchantInfo>().lambda().eq(PayMerchantInfo::getMerchantId, tableMerchantId));
        //收款人直接确认订单
        if (merchantInfo.getAgentRate().equals(EnumAgentRate.FOLLOWERS.getName())) {
            if (!merchantInfo.getDoubleConfirm().equals(CodeConst.DoubleConfirm.NO)) {
                //表示订单需要审核，但访问的地址却不是 /setDepositPreStatusSuccess
                throw new TranException(CodeConst.BUSINESS_ERROR_CODE,"无权操作");
            }
        }else {
            if (merchantInfo.getAccountType().equals(EnumAccountType.MERCHANT.getName()) && !merchantInfo.getAgentRate().equals(EnumAgentRate.CARD.getName()) && !merchantInfo.getAgentRate().equals(EnumAgentRate.OPERATOR.getName())) {
                throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "权限不足");
            }
            if (merchantInfo.getAccountType().equals(EnumAccountType.ADMIN.getName())){
                if (googleCode == null) {
                    throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "google验证码为空");
                }
                String googleKeyDecode =  EncryptUtil.aesDecrypt(merchantInfo.getGoogleKey(),walletPrivateKeySalt);
                if(!GoogleAuthenticator.check_code(googleKeyDecode, googleCode, System.currentTimeMillis())){
                    throw new TranException(CodeConst.BUSINESS_ERROR_CODE,"google验证码错误");
                }
            }

        }


        CoinHolderDepositInfo holderDepositInfo = iCoinHolderDepositInfoService.getOne(new QueryWrapper<CoinHolderDepositInfo>().lambda()
                .eq(CoinHolderDepositInfo::getWithdrawNo, withdrawNo));

        if (holderDepositInfo == null) {
            throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "订单不存在");
        }

        //订单[排队中]才可修正 + 查询的任务也不可设为失败
//        if (!holderDepositInfo.getTaskType().equals(CodeConst.TaskTypeConst.TRANSFER_TO_CARD)) {
//            throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "非打款任务,不可设为成功");
//        }

        //订单[排队中]才可修正 + 查询的任务也不可设为失败
        if (!holderDepositInfo.getStatus().equals(CodeConst.WithdrawStatusConst.WITHDRAW_UNKNOWN) && !holderDepositInfo.getStatus().equals(CodeConst.WithdrawStatusConst.WITHDRAW_NO_CLEAR)) {
            throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "订单状态非[待支付],不可设为成功");
        }

        if (merchantInfo.getMerchantName().equals(EnumAccountType.MERCHANT.getName())) {
            if (!merchantInfo.getAgentRate().equals(EnumAgentRate.FOLLOWERS.getName())) {
                if (!holderDepositInfo.getPayerMerchantId().equals(tableMerchantId) && merchantInfo.getAgentRate().equals(EnumAgentRate.CARD.getName())){
                    throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "码商权限不足");
                }

                if (!holderDepositInfo.getPayerOperatorId().equals(tableMerchantId) && merchantInfo.getAgentRate().equals(EnumAgentRate.OPERATOR.getName())){
                    throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "码商操作员权限不足");
                }
            }else {
                if (!holderDepositInfo.getPayerIdentity().equals(tableMerchantId)){
                    throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "权限不足");
                }
            }

        }

        //WARNING:因为码可能会先删除,隔天再补单的
//        CoinHolderInfo holderInfo = iCoinHolderInfoService.getOne(new QueryWrapper<CoinHolderInfo>().lambda()
//                .eq(CoinHolderInfo::getPayerAddr, holderDepositInfo.getPayerAddr()));
//        if (holderInfo == null) {
//            throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "收款码不存在");
//        }
//
        ReturnMsg res = barcodeManagerService.barcodeSuccess(merchantInfo,withdrawNo,"","","","",holderDepositInfo,null,null,"0");//0-手动设为成功
        if (!res.getCode().equals("0000")) {
            throw new TranException(CodeConst.BUSINESS_ERROR_CODE, res.getMessage());
        }
        //FINALLY:返回结果给前端
        Map<String, Object> resultMap = new HashMap<String, Object>();
        resultMap.put("withdrawNo",withdrawNo);
        return new ReturnMsg(resultMap);
    }


    /**
     * 代收订单设为失败
     * @关闭设为失败功能
     * @param withdrawNo
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/setDepositStatusFail", method = {RequestMethod.POST, RequestMethod.GET},name = "admin")
    @ResponseBody
    @Transactional(rollbackFor = Exception.class)
    public ReturnMsg setDepositStatusFail(
            String googleCode,
            @NotBlank(message = "平台订单号不能为空") String withdrawNo,
            @NotBlank(message = "管理员ID不能为空") String tableMerchantId
    ) throws Exception {

//        //查询管理员(谁操作的)
//        PayMerchantInfo merchantInfo = iPayMerchantInfoService.getOne(new QueryWrapper<PayMerchantInfo>().lambda().eq(PayMerchantInfo::getMerchantId, tableMerchantId));
//        //收款人直接确认订单
//        if (merchantInfo.getAgentRate().equals(EnumAgentRate.FOLLOWERS.getName())) {
//            if (!merchantInfo.getDoubleConfirm().equals(CodeConst.DoubleConfirm.NO)) {
//                //表示订单需要审核，但访问的地址却不是 /setDepositPreStatusSuccess
//                throw new TranException(CodeConst.BUSINESS_ERROR_CODE,"无权操作");
//            }
//        }else {
//            if (merchantInfo.getAccountType().equals(EnumAccountType.MERCHANT.getName()) && !merchantInfo.getAgentRate().equals(EnumAgentRate.CARD.getName()) && !merchantInfo.getAgentRate().equals(EnumAgentRate.OPERATOR.getName())) {
//                throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "权限不足");
//            }
//            if (merchantInfo.getAccountType().equals(EnumAccountType.ADMIN.getName())){
//                if (googleCode == null) {
//                    throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "google验证码为空");
//                }
//                String googleKeyDecode =  EncryptUtil.aesDecrypt(merchantInfo.getGoogleKey(),walletPrivateKeySalt);
//                if(!GoogleAuthenticator.check_code(googleKeyDecode, googleCode, System.currentTimeMillis())){
//                    throw new TranException(CodeConst.BUSINESS_ERROR_CODE,"google验证码错误");
//                }
//            }
//
//        }
//
//
//        CoinHolderDepositInfo holderDepositInfo = iCoinHolderDepositInfoService.getOne(new QueryWrapper<CoinHolderDepositInfo>().lambda()
//                .eq(CoinHolderDepositInfo::getWithdrawNo, withdrawNo));
//
//        if (holderDepositInfo == null) {
//            throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "订单不存在");
//        }
//        //订单[排队中]才可修正 + 查询的任务也不可设为失败
//        if (!holderDepositInfo.getStatus().equals(CodeConst.WithdrawStatusConst.WITHDRAW_UNKNOWN)  && !holderDepositInfo.getStatus().equals(CodeConst.WithdrawStatusConst.WITHDRAW_NO_CLEAR)) {
//            throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "订单状态非[待支付],不可设为失败");
//        }
//
//        if (merchantInfo.getMerchantName().equals(EnumAccountType.MERCHANT.getName())) {
//            if (!merchantInfo.getAgentRate().equals(EnumAgentRate.FOLLOWERS.getName())) {
//                if (!holderDepositInfo.getPayerMerchantId().equals(tableMerchantId) && merchantInfo.getAgentRate().equals(EnumAgentRate.CARD.getName())){
//                    throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "码商权限不足");
//                }
//
//                if (!holderDepositInfo.getPayerOperatorId().equals(tableMerchantId) && merchantInfo.getAgentRate().equals(EnumAgentRate.OPERATOR.getName())){
//                    throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "码商操作员权限不足");
//                }
//            }else {
//                if (!holderDepositInfo.getPayerIdentity().equals(tableMerchantId)){
//                    throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "权限不足");
//                }
//            }
//
//        }
//
//        Date endTime = new Date();
//        //1.更新 三方订单 CoinHolderDepositInfo
//        CoinHolderDepositInfo holderEntity = new CoinHolderDepositInfo();
//        holderEntity.setPreStatus(CodeConst.WithdrawStatusConst.WITHDRAW_UNKNOWN);//0 未收 1-已收
//        holderEntity.setStatus(CodeConst.WithdrawStatusConst.WITHDRAW_FAIL);
//        holderEntity.setTaskEndTime(new Date());
//        holderEntity.setRemark("["+merchantInfo.getMerchantName()+"]设为失败");
//        boolean isUpdHolder = iCoinHolderDepositInfoService.update(holderEntity,new UpdateWrapper<CoinHolderDepositInfo>().lambda()
//                .eq(CoinHolderDepositInfo::getWithdrawNo, withdrawNo)); //保存更改
//        if (!isUpdHolder){
//            _log.error("三方代收订单:{}更新失败",withdrawNo);
//            throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "三方订单更新失败");
//        }
//
//        //2.更新代收订单列表 PayDepositInfo
//        PayDepositInfo depositEntity = new PayDepositInfo();
//        depositEntity.setWithdrawStatus(CodeConst.WithdrawStatusConst.WITHDRAW_FAIL);
//        depositEntity.setEndTime(endTime);
//        boolean isUpdDeposit = iPayDepositInfoService.update(depositEntity,new UpdateWrapper<PayDepositInfo>().lambda()
//                .eq(PayDepositInfo::getWithdrawNo, withdrawNo));
//        if (!isUpdDeposit){
//            _log.error("代收订单:{}更新失败",withdrawNo);
//            throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "订单更新失败");
//        }
//
//        //3.更改CoinHolderInfo 数量
//        //iCoinHolderInfoService.alterCoinHolderCodeTimes(holderDepositInfo.getPayerAddr(),-1); //--1负负+1
//
//
//        //4.清空缓存 redis
//        String redis_payerAddr_amt = holderDepositInfo.getPayerAddr() + MyRedisConst.SPLICE + holderDepositInfo.getAmtPay();
//        if (redisUtil.get(redis_payerAddr_amt) != null) {
//            redisUtil.del(redis_payerAddr_amt);
//        }
//
//        //5.通知下游商家订单已支付失败
//        PayDepositInfo depositInfo = iPayDepositInfoService.getOne(new QueryWrapper<PayDepositInfo>().lambda().eq(PayDepositInfo::getWithdrawNo, holderDepositInfo.getWithdrawNo()));
//        notifyService.notifyMerchantDeposit(depositInfo, depositInfo.getWithdrawStatus());

        //FINALLY:返回结果给前端
        Map<String, Object> resultMap = new HashMap<String, Object>();
        resultMap.put("withdrawNo",withdrawNo);
        return new ReturnMsg(resultMap);
    }

}