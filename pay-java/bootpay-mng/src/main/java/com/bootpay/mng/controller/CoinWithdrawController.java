package com.bootpay.mng.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bootpay.common.authenticator.GoogleAuthenticator;
import com.bootpay.common.constants.CodeConst;
import com.bootpay.common.constants.MyRedisConst;
import com.bootpay.common.constants.enums.*;
import com.bootpay.common.excetion.TranException;
import com.bootpay.common.framework.RedisUtil;
import com.bootpay.common.utils.*;
import com.bootpay.common.utils.Signature;
import com.bootpay.common.utils.HttpClient4Utils;
import com.bootpay.core.entity.*;
import com.bootpay.core.service.*;
import com.bootpay.mng.service.*;
import org.apache.commons.httpclient.util.DateParseException;
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

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.*;

import org.tron.trident.core.ApiWrapper;
import org.tron.trident.core.contract.Contract;
import org.tron.trident.core.contract.Trc20Contract;
import org.tron.trident.core.exceptions.IllegalException;
import org.tron.trident.proto.Response.TransactionExtention;
import org.tron.trident.proto.Chain.Transaction;

/**
 * <p>
 * 信息管理
 * </p>
 *
 * @author 
 * @since 2020-03-02
 */
@Validated
@RestController
@RequestMapping("/app")
public class CoinWithdrawController {

    private Logger _log = LoggerFactory.getLogger(CoinWithdrawController.class);

    @Value("${app.privateAESKey}") //application.yml文件读取
    private String privateAESKey;

    @Value("${app.blockCCUrl}")
    private String blockCCUrl;

    @Value("${app.blockCCApiKey}")
    private String blockCCApiKey;

    @Value("${app.platformBalanceUrl}") //请求URL
    private String platformBalanceUrl;

    @Value("${app.platformId}") //外包ID
    private String platformId;

    @Value("${app.platformKey}") //加密密钥
    private String platformKey;

    //
    @Value("${app.walletPrivateKeySalt}") //application.yml文件读取
    private String walletPrivateKeySalt;

    @Value("${app.usdtContractAddress}") //application.yml文件读取
    private String usdtContractAddress;

    @Value("${app.trongridApiKey}") //application.yml文件读取
    private String trongridApiKey;

    @Autowired
    private AuthenticatorService authenticatorService;

    @Autowired
    private IPayPartnerBalanceService iPayPartnerBalanceService; //外包余额

    @Autowired
    private PartnerManagerService partnerManagerService;

    @Autowired
    private IPayPartnerInfoService iPayPartnerInfoService; //外包SERVICE

    @Autowired
    private ICardPayerInfoService iCardPayerInfoService; //持卡人service

    @Autowired
    private ICardPayerFlowInfoService iCardPayerFlowInfoService; //银行卡明细

    @Autowired
    private ICardPayerWithdrawInfoService iCardPayerWithdrawInfoService; //银行卡代付订单列表

    @Autowired
    private ICardPayerReturnInfoService iCardPayerReturnInfoService; //银行卡冲正订单列表

    @Autowired
    private ICoinPayerReturnInfoService iCoinPayerReturnInfoService; //钱包冲正

    @Autowired
    private IBankCodeInfoService iBankCodeInfoService;//银行通道

    @Autowired
    private ICardChannelInfoService iCardChannelInfoService; //卡通道

    @Autowired
    private ICardChannelFlowInfoService iCardChannelFlowInfoService; //卡通道明细

    @Autowired
    private CardChannelManagerService cardChannelManagerService;

    @Autowired
    private IPayMerchantInfoService iPayMerchantInfoService;

    @Autowired
    private IPayMerchantSelfRechargeService iPayMerchantSelfRechargeService; //商家自助充值

    @Autowired
    private IPayAgentChannelSiteService iPayAgentChannelSiteService; //代理通道

    @Autowired
    private IPayPlatformIncomeInfoService iPayPlatformIncomeInfoService;

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private IPayMerchantBalanceService balanceService; //商户余额服务

    @Autowired
    private IPayMerchantMoneyChangeService iPayMerchantMoneyChangeService; //商户流水明细

    @Autowired
    private IPayPartnerAmtMoneyChangeService iPayPartnerAmtMoneyChangeService;//跑量账变SERVICE

    @Autowired
    private IPayWithdrawInfoService iPayWithdrawInfoService;

    @Autowired
    private CardWithdrawManagerService cardWithdrawManagerService;

    @Autowired
    private MerchantAccountManagerService merchantAccountManagerService;

    @Autowired
    private  ICoinChannelWithdrawInfoService iCoinChannelWithdrawInfoService;

    @Autowired
    private ICoinPayerInfoService iCoinPayerInfoService;

    @Autowired
    private CoinWithdrawManagerService coinWithdrawManagerService;

    @Autowired
    private  ICoinPayerWithdrawInfoService iCoinPayerWithdrawInfoService;

    @Autowired
    private CoinChannelManagerService coinChannelManagerService;

    @Autowired
    private ICoinHolderDepositInfoService iCoinHolderDepositInfoService; //COIN代收

    @Autowired
    private ICoinHolderInfoService iCoinHolderInfoService;

    @Autowired
    private IPayMerchantChannelSiteService channelSiteService;

    @Autowired
    private IPayMerchantChannelSiteService iPayMerchantChannelSiteService; //商户通道成本服务

    @Autowired
    private IPayMerchantBalanceService iPayMerchantBalanceService;

    @Autowired
    private ICoinPayerWithdrawFlowService iCoinPayerWithdrawFlowService;

    /**
     * 三方管理
     * 暂停下发
     * 设为失败
     *
     * @事务操作 -- 事务会等待订单的状态发生改变后 才进行操作 原子一致性
     * @反向案例 -- 取款的一瞬间 排队中设为 打款中 /操作员操作成 "暂停下发" ｜ 设为失败
     */
    @RequestMapping(value = "/withdraw", method = {RequestMethod.POST})
    @ResponseBody
    @Transactional(rollbackFor = Exception.class)
    public ReturnMsg withdraw(
            @NotNull(message = "通道编号不能为空") String channelCode,
            @NotNull(message = "订单号不能为空") String withdrawNo,
            @NotNull(message = "订单金额不能为空") String amt,
            @NotNull(message = "收款人不能为空") String acctName,
            @NotNull(message = "收款人钱包") String acctAddr,
            String merchantNotifyUrl,
            String msg,
            @NotNull(message = "签名不能为空") String sign,
            @NotNull(message = "aesKey不能为空") String aesKey) throws TranException {

        try {

            if (channelCode == null || "".equals(channelCode)) {
                throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "通道号为空");
            }

            if (withdrawNo == null || "".equals(withdrawNo)) {
                throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "订单号为空");
            }

            if (amt == null || "".equals(amt)) {
                throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "订单金额为空");
            }

            if (acctName == null || "".equals(acctName)) {
                throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "收款人为空");
            }
            if (acctAddr == null || "".equals(acctAddr)) {
                throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "收款人钱包为空");
            }

            if (sign == null || "".equals(sign)) {
                throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "签名为空");
            }

            if (aesKey == null || "".equals(aesKey)) {
                throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "aeSKey为空");
            }

            //1.判断三方代付通道是否存在
            CoinChannelWithdrawInfo channelInfo = iCoinChannelWithdrawInfoService.getOne(new QueryWrapper<CoinChannelWithdrawInfo>()
                    .lambda().eq(CoinChannelWithdrawInfo::getChannelCode, channelCode));
            if (channelInfo == null) {
                throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "三方代付通道不存在");
            }

            //FIXME:同一个通道有商户、代理、总代
            PayMerchantInfo merchantInfo = iPayMerchantInfoService.getOne(new QueryWrapper<PayMerchantInfo>()
                    .lambda()
                    .eq(PayMerchantInfo::getChannelCode, channelCode)
                    .eq(PayMerchantInfo::getAgentRate,'0')
            ); //代付通道的商户

            if (merchantInfo == null) {
                throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "失败:商户ID不存在");
            }

            //通道最高/低限额
            //@金额守护
            BigDecimal mininum = new BigDecimal("1");
            if (BigDecimalUtils.lessThan(new BigDecimal(amt), mininum)) {
                throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "失败:单笔代付金额不能小于" + mininum);
            }

            //2.通道费率是否设置
            if (channelInfo.getChannelRate() == null) {
                throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "失败:通道费率未设置");
            }

            //3.钱包是否分配
            Map<String, Object> countParamsChannel = new HashMap<String, Object>();
            countParamsChannel.put("channelCode", channelCode);
            Integer getCountCoinPayerFromChannel = iCoinPayerInfoService.selectCountCoinPayer(countParamsChannel);
            if (getCountCoinPayerFromChannel == 0) {
                throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "失败:通道还未下挂钱包");
            }

            Map<String, Object> countParams = new HashMap<String, Object>();
            countParams.put("channelCode", channelCode);
            countParams.put("status", CodeConst.PayerCardStatusConst.CARD_STATUS_BELONG);
            Integer getCountCoinPayer = iCoinPayerInfoService.selectCountCoinPayer(countParams);

            if (getCountCoinPayer == 0) {
                throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "失败:钱包未分配");
            }

            String apiKeyDecrypt = EncryptUtil.aesDecrypt(channelInfo.getApiKey(),walletPrivateKeySalt);

            //4.验证签名 + aesKey
            Map<String, Object> params = new HashMap<String, Object>();
            params.put("channelCode", channelCode);//通道号
            params.put("withdrawNo", withdrawNo);//四方平台提交的订单号 W1577241046308040251
            params.put("amt", amt);//单位为PHP
            params.put("acctName", acctName.trim());
            params.put("acctAddr", acctAddr.trim()); //urlencode
            //params.put("merchantNotifyUrl", merchantNotifyUrl); //主动轮询方式
            String signCreator = Signature.getSign(params, apiKeyDecrypt);
            params.put("sign", signCreator);

            String aesKeyCreator = EncryptUtil.aesEncrypt(Signature.getSign(params, apiKeyDecrypt), privateAESKey);

            if (!signCreator.equals(sign)) {
                throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "失败:签名不正确");
            }
            if (!aesKeyCreator.equals(aesKey)) {
                throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "失败:aesKey不正确");
            }

            //5.超过单个[已分配]的钱包最高余额
            params.put("status", CodeConst.PayerCardStatusConst.CARD_STATUS_BELONG); //状态为已分配

            Map<String, Object> highestAmtMap = iCoinPayerInfoService.selectOneCoinPayerInfoOfHighestAmt(params);
            if (highestAmtMap == null || new BigDecimal(highestAmtMap.get("highestAmt").toString()).compareTo(new BigDecimal(amt)) < 0) {
                throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "失败:超过单个钱包最高可用额度");
            }

            /**
             * 6.所有满足条件的钱包
             * 已分配
             * 余额 > 提款金额
             * 提款金额 < 超过单卡最高可用额度
             * 新增条件:已上线 1 | 下线 -1 PAYER_ONLINE | 有跑分的钱包商才需要
             * */
            params.put("date", DateUtil.getDate()); //当天
            //params.put("payerOnline", CodeConst.PayerOnlineStatus.ONLINE); //0:在线 -1:下线
            List<CoinPayerInfo> coinPayerList = iCoinPayerInfoService.queryCoinPayerListAllowWithdraw(params);
            if (coinPayerList == null || coinPayerList.size() == 0) {
                throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "失败:钱包可用余额不足");
            }

            /**
             * 7.不超限的银行卡
             * 交易金额amt + 已汇金额 - 失败的金额 < 单张银行卡设定的限额
             * 同一收款人、同一付款人、同一金额 5分钟内不可重复
             * 卡号ArrayList列表
             * */

            //条件一：当月也不能超限 ,从当日不超限的卡 找出 当月也没有超限的卡
            ArrayList<CoinPayerInfo> coinPayerMonthWalletList = new ArrayList<>();
            for (CoinPayerInfo payerItem : coinPayerList) {

                Map<String, Object> paramsMonth = new HashMap<>();
                paramsMonth.put("payerAddr", payerItem.getPayerAddr());
                paramsMonth.put("flowType", CodeConst.CardFlowTypeConst.WITHDRAW); //下发
                paramsMonth.put("month", DateUtil.getMonth());
                //已出款 19600
                BigDecimal payerWithdrawNumAlreadySent = iCoinPayerInfoService.selectSumPayerAmtMonth(paramsMonth);
                //扣除冲正
                paramsMonth.remove("flowType");
                paramsMonth.put("flowType", CodeConst.CardFlowTypeConst.RETURN); //冲正
                BigDecimal payerWithdrawNumAlreadyReturn = iCoinPayerInfoService.selectSumPayerAmtMonth(paramsMonth);

                //已出款 19600 - 冲正金额 + 正要出款的金额 5000 amt
                BigDecimal payerWithdrawNumGoingToSend = payerWithdrawNumAlreadySent.subtract(payerWithdrawNumAlreadyReturn).add(new BigDecimal(amt));
                //超出金额 =  (已出款 19600 + 正要出款的金额 5000) - 限额 200000 > 0
                //不超限 如:满额20W也可以下发
                if (BigDecimalUtils.lessThan(payerWithdrawNumGoingToSend,payerItem.getPayerLimitMonth())) {
                    coinPayerMonthWalletList.add(payerItem);
                }
            }

            if (coinPayerMonthWalletList.isEmpty()) {
                _log.error("失败 -- 订单号:{} 所有付款钱包已超当月限额",withdrawNo);
                throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "失败:所有付款钱包已超当月限额");
            }

            //条件二：当日不能超限
            ArrayList<String> coinPayerTodayWalletList = new ArrayList<>();
            for (CoinPayerInfo payerItem : coinPayerMonthWalletList) {
                //coinPayerWalletList.add(payerItem.getPayerAddr());
                Map<String, Object> paramsToday = new HashMap<>();
                paramsToday.put("payerAddr", payerItem.getPayerAddr());
                paramsToday.put("flowType", CodeConst.CardFlowTypeConst.WITHDRAW); //下发
                paramsToday.put("date", DateUtil.getDate());
                //已出款 19600
                BigDecimal payerWithdrawNumAlreadySent = iCoinPayerInfoService.selectSumPayerAmtDay(paramsToday);
                //FIXME:扣除冲正
                paramsToday.remove("flowType");
                paramsToday.put("flowType", CodeConst.CardFlowTypeConst.RETURN); //冲正
                BigDecimal payerWithdrawNumAlreadyReturn = iCoinPayerInfoService.selectSumPayerAmtDay(paramsToday);

                //已出款 19600 - 冲正金额 + 正要出款的金额 5000 amt
                BigDecimal payerWithdrawNumGoingToSend = payerWithdrawNumAlreadySent.subtract(payerWithdrawNumAlreadyReturn).add(new BigDecimal(amt));
                //超出金额 =  (已出款 19600 + 正要出款的金额 5000) - 限额 200000 > 0
                //不超限 如:满额20W也可以下发
                if (BigDecimalUtils.lessThan(payerWithdrawNumGoingToSend,payerItem.getPayerLimitDay())) {
                    coinPayerTodayWalletList.add(payerItem.getPayerAddr());
                }
            }

            if (coinPayerTodayWalletList.isEmpty()) {
                _log.error("失败 -- 订单号:{} 所有付款钱包已超当日限额",withdrawNo);
                throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "失败:所有付款钱包已超当日限额");
            }

            /**
             * TODO:符合条件的卡列表 coinPayerWalletList ，但如果有 同一收款人、同一付款人、同一金额的卡在其中 ，须把那张卡移除，不然订单会很多失败
             * 因为同一收款人 同一付款人 同一金额 6分钟只能一次
             * 2022-12-17 14:17:04
             * 现在时间的6分钟前 2022-12-20 12:00:12
             * */

            int minsAgo = 6;
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.MINUTE, -minsAgo);
            String datetimeAgo = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(calendar.getTime());
            _log.warn("打款分配卡: 多少分钟前的时间 {}", datetimeAgo);

            ArrayList<String> finalCoinPayerWalletList = new ArrayList<>();
            for(String payerAddr : coinPayerTodayWalletList) {
                CoinPayerWithdrawInfo  lastPaidInfo = iCoinPayerWithdrawInfoService.getOne(new QueryWrapper<CoinPayerWithdrawInfo>().lambda()
                        .eq(CoinPayerWithdrawInfo::getPayerAddr, payerAddr)
                        .eq(CoinPayerWithdrawInfo::getAcctAddr, acctAddr)
                        .eq(CoinPayerWithdrawInfo::getAmt,amt)
                        .ge(CoinPayerWithdrawInfo::getTaskStartTime,datetimeAgo)
                );

                if (lastPaidInfo == null) {
                    finalCoinPayerWalletList.add(payerAddr);
                }
            }

            //8.payerListMap排序,取出排队中最少的一张卡 且 同一收款人、同一付款人、同一金额 5分钟内没有重复打个款的卡
            Map<String, Object> paramsPayerWallet = new HashMap<>();
            paramsPayerWallet.put("coinPayerWalletList", finalCoinPayerWalletList);
            //付款钱包 排队中最少的一张卡
            CoinPayerInfo lastCoinPayerInfo = iCoinPayerInfoService.selectOneInCoinListOfLowestProcessingNum(paramsPayerWallet);
            if (lastCoinPayerInfo == null) {
                throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "失败:钱包不存在");
            }

            /**
             * @以上若失败
             * @则不会变动钱包的余额及新增钱包流水
             * @订单还没有插入三方
             * */
            //9.服务:钱包变动流水 -- 提交多少就下发金额 不包含手续费
            String flowType = EnumFlowType.WITHDRAW.getName(); //代付
            String channelWithdrawNo = "C" + DateUtil.format(new Date(), DateUtil.FULL_TIME_FORMAT_EN) + "0" + System.currentTimeMillis();
            String remark = "代付->" + acctName + ":" + acctAddr;
            coinWithdrawManagerService.alterCoinPayerBalanceNAddFlowService(lastCoinPayerInfo, new BigDecimal(amt), flowType, withdrawNo,channelWithdrawNo, remark);

            //10. 更新card_payer_info 银行卡排队中的笔数
            Map<String, Object> paramsUpd = new HashMap<>();
            //paramsUpd.put("amt", new BigDecimal(amt).negate()); //不在此扣除 已在第9 扣除并记录钱包流水
            paramsUpd.put("payerProcessingNum", 1);
            paramsUpd.put("payerAddr", lastCoinPayerInfo.getPayerAddr());
            Integer processingNum = iCoinPayerInfoService.alterCoinPayerAmtNProcessingNum(paramsUpd);
            if (processingNum != 1) {
                throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "失败:更新钱包"+lastCoinPayerInfo.getPayerAddr()+"排队中的笔数失败");
            }

            //11.并插入钱包交易流水列表
            // 计算通道的下发手续费  = 下发手续费汇率 /100 * 下发金额 = 996
            BigDecimal payerRateFeeAmt = channelInfo.getChannelRate().divide(new BigDecimal("100"))
                    .multiply(new BigDecimal(amt)).setScale(2, BigDecimal.ROUND_HALF_UP);

            String withdrawRemark = "排队中"; //更新订单状态 处理中
            String status = CodeConst.WithdrawStatusConst.WITHDRAW_UNKNOWN;  //订单未知 手机还未获取订单 不在处理中

            //密码得解密
            String privateKey = EncryptUtil.aesDecrypt(lastCoinPayerInfo.getPayerPrivateKey(),walletPrivateKeySalt);
            String fromAddress = lastCoinPayerInfo.getPayerAddr();
            String toAddress= acctAddr.trim();
            String hashId =  "";

            //1.付款完有可能也会断网没有返回数据 应设为未知 防止重复打款
            //2.查询付款钱包是否有足够的矿工费b
            CoinPayerWithdrawInfo insertEntity = new CoinPayerWithdrawInfo();
            insertEntity.setHashId(hashId);
            insertEntity.setWithdrawNo(withdrawNo);//四方平台的订单号
            insertEntity.setChannelWithdrawNo(channelWithdrawNo);//随机生成
            insertEntity.setStatus(status);
            insertEntity.setAmt(new BigDecimal(amt)); //下发金额

            insertEntity.setPayerSingleFee(channelInfo.getChannelSingle()); //通道的单笔手续费
            insertEntity.setPayerRateFee(payerRateFeeAmt); //通道的下发手续费汇率

//          insertEntity.setPayerBankType(lastCardPayerInfo.getPayerBankType()); //0:自动 -1:人工手动
//          insertEntity.setPayerMerchantId(lastCardPayerInfo.getPayerMerchantId());
//          insertEntity.setPayerBankCode(lastCardPayerInfo.getBankCode()); //付款人银行名称

            insertEntity.setPayerName(lastCoinPayerInfo.getPayerName());
            insertEntity.setPayerAddr(lastCoinPayerInfo.getPayerAddr()); //付款人钱包地址
//          insertEntity.setPayerPhone(lastCardPayerInfo.getPayerPhone());
//            insertEntity.setReceiverBank(receiverBank != null ? receiverBank.trim() : ""); //收款人银行名称

            insertEntity.setWithdrawPause(CodeConst.WithdrawPauseConst.WITHDRAW_PAUSE_Y); //TODO:打款先让前端APP 暂停收到 打款任务

            insertEntity.setAcctName(acctName);
            insertEntity.setAcctAddr(acctAddr);
            insertEntity.setChannelCode(channelCode);
            insertEntity.setChannelName(channelInfo.getChannelName());
            insertEntity.setChannelFeeType(CodeConst.ChannelFeeTypeConst.FEE_COMBINE);//手续费直接扣除
            insertEntity.setMsg(msg);
            insertEntity.setRemark(withdrawRemark);
            insertEntity.setCreateTime(new Date());
            //insertEntity.setTaskStartTime(new Date()); //处理时间

//            if (CodeConst.WithdrawStatusConst.WITHDRAW_SUCCESS.equals(status)){
//                insertEntity.setTaskEndTime(new Date()); //订单完成时间
//            }

            insertEntity.setDate(DateUtil.getDate());

            boolean isInsertEntity = iCoinPayerWithdrawInfoService.save(insertEntity);
            if (!isInsertEntity) {
                throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "三方订单[" + withdrawNo + "]插入列表失败");
            }

            //12.扣除通道的手续费总额 + 记录通道手续费变动流水 |
            BigDecimal alterAmt = payerRateFeeAmt.add(channelInfo.getChannelSingle());  //下发手续费+单笔手续费

            String channelRemark = "代付 - " + lastCoinPayerInfo.getPayerName() + ":" + lastCoinPayerInfo.getPayerAddr() + "->" + acctName + ":" + acctAddr;
            String managerName = "";
            coinChannelManagerService.alterCoinChannelBalanceService(
                    merchantInfo.getMerchantId(),
                    alterAmt.negate(), //代付 扣除
                    CodeConst.CardFlowTypeConst.WITHDRAW,
                    withdrawNo, //四方的订单号
                    channelWithdrawNo,//三方订单号
                    channelCode,
                    managerName,
                    channelInfo.getChannelName(),
                    channelRemark
            );
            //MAP结果返回:订单状态给四方平台
            Map<String, Object> resultMap = new HashMap<String, Object>();
            resultMap.put("channelCode", channelCode); //通道号
            resultMap.put("withdrawNo", withdrawNo); //唯一订单号
            resultMap.put("channelWithdrawNo", channelWithdrawNo); //通道流水订单号
            resultMap.put("amt", amt); //交易金额 元
            resultMap.put("status", status);
            resultMap.put("payerName", lastCoinPayerInfo.getPayerName()); //付款人
            resultMap.put("payerAddr", lastCoinPayerInfo.getPayerAddr()); //付款者卡号
            resultMap.put("acctName", acctName); //urlencode
            resultMap.put("acctAddr", acctAddr);

            String respSign = Signature.getSign(resultMap, apiKeyDecrypt);

            resultMap.put("sign", respSign);
            resultMap.put("aesKey", EncryptUtil.aesEncrypt(respSign, privateAESKey));
            return new ReturnMsg(resultMap);

        } catch (Exception e) {
            e.printStackTrace();

            _log.error("=====>>>>>三方平台插入订单: "+ withdrawNo + "异常 " + e.getMessage());

            Map<String, Object> resultMap = new HashMap<String, Object>();
            resultMap.put("withdrawNo", withdrawNo); //唯一订单号
            resultMap.put("amt", amt); //交易金额 元
            resultMap.put("remark", e.getMessage());
            resultMap.put("sign", Signature.getSign(resultMap, privateAESKey));  //通道会出错 取 privateAESKey
            throw new TranException(CodeConst.BUSINESS_ERROR_CODE, JSON.toJSONString(resultMap));
        }
    }

//    @RequestMapping(value = "/notifyUrl", method = {RequestMethod.POST})
//    @ResponseBody
//    public ReturnMsg notifyUrl(
//            @NotNull(message = "通道编号不能为空") String cardChannelCode,
//            @NotNull(message = "订单号不能为空") String withdrawNo,
//            @NotNull(message = "订单金额不能为空") String amt,
//            @NotNull(message = "收款人不能为空") String receiverName,
//            @NotNull(message = "收款人卡号") String receiverCardNo,
//            @NotNull(message = "签名不能为空") String sign) throws Exception {
//        return new ReturnMsg("-1");
////        try {
////            return new ReturnMsg("-1");
////        } catch (Exception e) {
////            throw new TranException(CodeConst.BUSINESS_ERROR_CODE, e.getMessage());
////
////        }
//    }

    /**
     * 商家代付自助充值订单 | TRX|ETH可查询自动上分
     *
     * @param
     * @param rows 条数
     * @param page 页码
     * @return
     */
    @RequestMapping(value = "/queryPayMerchantSelfRechargePage", method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    public ReturnMsg queryPayMerchantSelfRechargePage(
            String withdrawNo,
            String payerName,
            String merchantId,
            String hashId,
            String payerAddr,
            String beginTime,
            String endTime,
            String remark,
            String tableMerchantId,
            @NotNull(message = "行数不能为空") Integer rows,
            @NotNull(message = "页码不能为空") Integer page
    ) throws TranException {

        PayMerchantInfo merchantInfo = iPayMerchantInfoService.getOne(new QueryWrapper<PayMerchantInfo>().lambda().eq(PayMerchantInfo::getMerchantId, tableMerchantId));
        if (merchantInfo == null) {
            throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "商户ID不存在");
        }
        //商家 0
        if (merchantInfo.getAccountType().equals(EnumAccountType.MERCHANT.getName()) && "0".equals(merchantInfo.getAgentRate())){
            merchantId = tableMerchantId;
        }

        Page<Map<String, Object>> pages = new Page<>(page, rows);

        Map<String, Object> params = new HashMap<String, Object>();

        params.put("withdrawNo", withdrawNo);
        params.put("payerName", payerName);
        params.put("payerAddr", payerAddr);
        params.put("merchantId", merchantId); //商家ID
        params.put("hashId", hashId); //hash id
        params.put("beginTime", beginTime + " 00:00:00");
        params.put("endTime", endTime + " 23:59:59");
        params.put("remark", HtmlUtil.delHTMLTag(remark)); //PAYEE
        IPage<Map<String, Object>> pageMap = iPayMerchantSelfRechargeService.queryPayMerchantSelfRechargePage(pages, params);

        Map<String, Object> resultMap = new HashMap<String, Object>();
        //resultMap.put("sumCardPayerWithdraw", sumCardPayerWithdraw);

        resultMap.put("pages", pageMap);
        return new ReturnMsg(resultMap);
    }

    /**
     * 系统操作权限
     * 商家自助充值订单 操作
     * 不放开给代理权限
     * @param
     * @param
     * @param
     * @return
     */
    @RequestMapping(value = "/setMerchantSelfRechargeStatus", method = {RequestMethod.POST, RequestMethod.GET},name = "admin")
    @ResponseBody
    public ReturnMsg setMerchantSelfRechargeStatus(
            String status,
            String withdrawNo,
            String tableMerchantId
    ) throws TranException {

//        PayMerchantInfo merchantInfo = iPayMerchantInfoService.getOne(new QueryWrapper<PayMerchantInfo>().lambda().eq(PayMerchantInfo::getMerchantId, tableMerchantId));
//        if (merchantInfo == null) {
//            throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "ID不存在");
//        }
//        // 卡主 -1
////        if (merchantInfo.getAccountType().equals(EnumAccountType.MERCHANT.getName())){
////            throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "权限不足");
////        }
//
//        PayMerchantSelfRecharge payMerchantSelfRecharge = iPayMerchantSelfRechargeService.getOne(new QueryWrapper<PayMerchantSelfRecharge>().lambda()
//                .eq(PayMerchantSelfRecharge::getWithdrawNo, withdrawNo));
//
//        if (payMerchantSelfRecharge == null) {
//            throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "订单[" + withdrawNo + "]不存在");
//        }
//
//        if (!"0".equals(payMerchantSelfRecharge.getStatus())){
//            throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "订单状态不允许修改");
//        }
//
////        if (merchantInfo.getAccountType().equals(EnumAccountType.MERCHANT.getName()) && !payMerchantSelfRecharge.getPayerMerchantId().equals(tableMerchantId)) {
////            throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "卡主[" + tableMerchantId + "]权限不足"); //卡主只可操作自己名下的订单
////        }
//
//        //1.银行卡
//        CoinPayerInfo coinPayerInfo = iCoinPayerInfoService.getOne(new QueryWrapper<CoinPayerInfo>().lambda().eq(CoinPayerInfo::getPayerAddr, payMerchantSelfRecharge.getPayerAddr()));
//        if (coinPayerInfo == null) {
//            throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "钱包不存在");
//        }
//
//
//        String remark = "";
//        if ("1".equals(status)){
//            remark = "成功:["+merchantInfo.getMerchantName()+"]" + "确认充值";
//        }else if ("-1".equals(status)){
//            remark = "失败:["+merchantInfo.getMerchantName()+"]" + "驳回充值";
//        }
//        PayMerchantSelfRecharge updEntity = new PayMerchantSelfRecharge();
//        updEntity.setStatus(status);
//        updEntity.setRemark(remark);
//        boolean isUpd = iPayMerchantSelfRechargeService.update(updEntity,
//                new QueryWrapper<PayMerchantSelfRecharge>().lambda().in(PayMerchantSelfRecharge::getWithdrawNo, withdrawNo));
//        if (!isUpd) {
//            throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "订单状态更新不成功");
//        }
//        //成功充值
//        int isMerchantRecharge = 0;
//        if ("1".equals(status)){
//            /**
//             * @Service
//             * 银行卡余额变动+流水变动服务
//             * */
//            String flowType = EnumFlowType.RECHARGE.getName(); //充值
//            String channelWithdrawNo = "C" + DateUtil.format(new Date(), DateUtil.FULL_TIME_FORMAT_EN) + "0" + System.currentTimeMillis();
//            remark = remark + ">" + coinPayerInfo.getPayerName() + ">" +  coinPayerInfo.getPayerAddr();
//            coinWithdrawManagerService.alterCoinPayerBalanceNAddFlowService(coinPayerInfo, payMerchantSelfRecharge.getAmt(), flowType, withdrawNo,channelWithdrawNo,remark);
//
//            //7.如果商家就加 上分 "商户充值" + 流水 PayMerchantMoneyChange
//            Map<String, Object> paramsMerchant = new HashMap<String, Object>();
//            paramsMerchant.put("agentRate", "0"); //商家0
//            paramsMerchant.put("channelCode", coinPayerInfo.getChannelCode()); //通道号
//            paramsMerchant.put("status", EnumMerchantStatusType.NORMAL.getName()); //自动上分只加到可用商户
//
//            PayMerchantInfo curMerchantInfo = iPayMerchantInfoService.findOneMerchantByCardChannelCode(paramsMerchant);
//            if (curMerchantInfo != null) {
//                remark = "["+merchantInfo.getMerchantName()+"]" + "确认充值";
//                merchantAccountManagerService.merchantRecharge(payMerchantSelfRecharge.getAmt(),
//                        curMerchantInfo.getMerchantId(),
//                        curMerchantInfo.getMerchantName(),
//                        remark,
//                        curMerchantInfo.getMerchantName(),
//                        curMerchantInfo.getLabel(),
//                        coinPayerInfo.getChannelCode(),
//                        channelWithdrawNo,
//                        EnumFlowType.RECHARGE.getName());
//                isMerchantRecharge = 1;
//            }
//        }

        Map<String, Object> resultMap = new HashMap<String, Object>();
        resultMap.put("isMerchantRecharge", "0");
        return new ReturnMsg(resultMap);
    }

    /**
     * 获取三方订单流水列表
     *
     * @param
     * @param rows 条数
     * @param page 页码
     * @return
     */
    @RequestMapping(value = "/queryCoinAgentWithdrawList", method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    public ReturnMsg queryCoinAgentWithdrawList(
            String merchantWithdrawNo,
            String withdrawNo,
            String channelWithdrawNo,
            String payerName,
            String coinType,
            String payerMerchantId,
            String payerBankCode,
            String payerAddr,
            String acctName,
            String acctAddr,
            String channelCode,
            String status,
            String beginTime,
            String endTime,
            String remark,
            String merchantId,
            @NotNull(message = "行数不能为空") Integer rows,
            @NotNull(message = "页码不能为空") Integer page
    ) throws TranException {

        PayMerchantInfo merchantInfo = iPayMerchantInfoService.getOne(new QueryWrapper<PayMerchantInfo>().lambda().eq(PayMerchantInfo::getMerchantId, merchantId));
        if (merchantInfo == null) {
            throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "权限不足");
        }

        Page<Map<String, Object>> pages = new Page<>(page, rows);

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("merchantWithdrawNo", merchantWithdrawNo);
        params.put("withdrawNo", withdrawNo);
        params.put("channelWithdrawNo", channelWithdrawNo);
        params.put("status", status);
        params.put("channelCode", channelCode);
        params.put("coinType", coinType); //0自动 -1:人工手动
        params.put("payerName", payerName);
        params.put("payerAddr", payerAddr);
        params.put("acctName", acctName); //PAYEE
        params.put("acctAddr", acctAddr);
        params.put("beginTime", beginTime + " 00:00:00");
        params.put("endTime", endTime + " 23:59:59");
        params.put("remark", HtmlUtil.delHTMLTag(remark)); //PAYEE
        IPage<Map<String, Object>> pageMap = iCoinPayerWithdrawInfoService.queryCoinPayerWithdrawInfoPage(pages, params);

        //TODO:params 增加  payerBankType 自动 人工 payerMerchantId 卡主编号
        //2统计交易订单
        BigDecimal sumCardPayerWithdraw = iCoinPayerWithdrawInfoService.sumCoinPayerWithdrawInfo(params);
        Integer countCardPayerWithdraw = iCoinPayerWithdrawInfoService.countCoinPayerWithdraw(params);

        //params.put("status", ""); //下发手续费不受订单状态选项的影响
       // Map<String, Object> sumAllWithdrawFeeMap = iCardPayerWithdrawInfoService.sumCardPayerWithdrawFee(params);

        //params.put("status", CodeConst.WithdrawStatusConst.WITHDRAW_FAIL);
        //Map<String, Object> sumFailWithdrawFeeMap = iCardPayerWithdrawInfoService.sumCardPayerWithdrawFee(params);

        //查询结果 map
        //BigDecimal sumPayerFee = new BigDecimal(sumAllWithdrawFeeMap.get("sumPayerFee").toString());
        //BigDecimal sumPayerRateFee = new BigDecimal(sumAllWithdrawFeeMap.get("sumPayerRateFee").toString());

        //BigDecimal sumPayerFailFee = new BigDecimal(sumFailWithdrawFeeMap.get("sumPayerFee").toString());
        //BigDecimal sumPayerFailRateFee = new BigDecimal(sumFailWithdrawFeeMap.get("sumPayerRateFee").toString());

        Map<String, Object> resultMap = new HashMap<String, Object>();
        resultMap.put("sumCardPayerWithdraw", sumCardPayerWithdraw);
        resultMap.put("countCardPayerWithdraw", countCardPayerWithdraw);
        //resultMap.put("sumCardPayerWithdrawFee", sumPayerFee.subtract(sumPayerFailFee)); //总下发单笔手续费 - 失败的手续费
       // resultMap.put("sumCardPayerWithdrawRateFee", sumPayerRateFee.subtract(sumPayerFailRateFee)); //总下发手续费汇率
        resultMap.put("pages", pageMap);

        return new ReturnMsg(resultMap);
    }




    /**
     * 获取三方 冲正订单流水列表
     *
     * @param
     * @param rows 条数
     * @param page 页码
     * @return
     */
    @RequestMapping(value = "/queryCoinPayerReturnPage", method = {RequestMethod.POST, RequestMethod.GET}, name = "admin")
    @ResponseBody
    public ReturnMsg queryCoinPayerReturnPage(
            String withdrawNo,
            String channelWithdrawNo,
            String payerName,
            String payerAddr,
            String acctName,
            String acctAddr,
            String channelCode,
            String withdrawStatus,
            String beginTime,
            String endTime,
            String remark,
            String tableMerchantId,
            @NotNull(message = "行数不能为空") Integer rows,
            @NotNull(message = "页码不能为空") Integer page) throws TranException {

        PayMerchantInfo merchantInfo = iPayMerchantInfoService.getOne(new QueryWrapper<PayMerchantInfo>().lambda().eq(PayMerchantInfo::getMerchantId, tableMerchantId));

        Page<CoinPayerReturnInfo> pages = new Page<>(page, rows);

        QueryWrapper<CoinPayerReturnInfo> queryWrapper = new QueryWrapper<>();

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("WITHDRAW_NO", withdrawNo);
        params.put("CHANNEL_WITHDRAW_NO", channelWithdrawNo);
        //params.put("WITHDRAW_STATUS", withdrawStatus);
        params.put("CHANNEL_CODE", channelCode);
        params.put("PAYER_NAME", payerName);
        params.put("PAYER_ADDR", payerAddr);
        params.put("ACCT_NAME", acctName); //PAYEE
        params.put("ACCT_ADDR", acctAddr);

        queryWrapper = queryWrapper.allEq(params, false)
                .like(StringUtils.isNotBlank(remark), "REMARK", HtmlUtil.delHTMLTag(remark))
                .ge(StringUtils.isNotBlank(beginTime), "CREATE_TIME", beginTime + " 00:00:00")
                .le(StringUtils.isNotBlank(endTime), "CREATE_TIME", endTime + " 23:59:59");
        queryWrapper.orderByDesc("ID");
        //page
        IPage<CoinPayerReturnInfo> pageMap = iCoinPayerReturnInfoService.page(pages, queryWrapper);

        //2统计交易订单
        Map<String, Object> paramsSearch = new HashMap<String, Object>();
        paramsSearch.put("withdrawNo", withdrawNo);
        paramsSearch.put("channelWithdrawNo", channelWithdrawNo);
        paramsSearch.put("status", withdrawStatus);
        paramsSearch.put("channelCode", channelCode);
        paramsSearch.put("payerName", payerName);
        paramsSearch.put("payerAddr", payerAddr);
        paramsSearch.put("acctName", acctName); //PAYEE
        paramsSearch.put("acctAddr", acctAddr);
        paramsSearch.put("beginTime", beginTime + " 00:00:00");
        paramsSearch.put("endTime", endTime + " 23:59:59");

        BigDecimal sumCardPayerWithdraw = iCoinPayerReturnInfoService.sumCoinPayerReturnInfo(paramsSearch);
        Integer countCardPayerWithdraw = iCoinPayerReturnInfoService.countCoinPayerReturn(paramsSearch);

        paramsSearch.put("status", ""); //下发手续费不受订单状态选项的影响
        Map<String, Object> sumAllWithdrawFeeMap = iCoinPayerReturnInfoService.sumCoinPayerReturnFee(paramsSearch);

        paramsSearch.put("status", CodeConst.WithdrawStatusConst.WITHDRAW_FAIL);
        Map<String, Object> sumFailWithdrawFeeMap = iCoinPayerReturnInfoService.sumCoinPayerReturnFee(paramsSearch);

        //查询结果 map
        BigDecimal sumPayerFee = new BigDecimal(sumAllWithdrawFeeMap.get("sumPayerSingleFee").toString());
        BigDecimal sumPayerRateFee = new BigDecimal(sumAllWithdrawFeeMap.get("sumPayerRateFee").toString());

        BigDecimal sumPayerFailFee = new BigDecimal(sumFailWithdrawFeeMap.get("sumPayerSingleFee").toString());
        BigDecimal sumPayerFailRateFee = new BigDecimal(sumFailWithdrawFeeMap.get("sumPayerRateFee").toString());

        Map<String, Object> resultMap = new HashMap<String, Object>();
        resultMap.put("sumCardPayerWithdraw", sumCardPayerWithdraw);
        resultMap.put("countCardPayerWithdraw", countCardPayerWithdraw);
        resultMap.put("sumCardPayerWithdrawFee", sumPayerFee.subtract(sumPayerFailFee)); //总下发单笔手续费 - 失败的手续费
        resultMap.put("sumCardPayerWithdrawRateFee", sumPayerRateFee.subtract(sumPayerFailRateFee)); //总下发手续费汇率
        resultMap.put("pages", pageMap);

        return new ReturnMsg(resultMap);
    }

    /**
     * 获取商家三方 冲正订单流水列表
     *
     * @param
     * @param rows 条数
     * @param page 页码
     * @return
     */
    @RequestMapping(value = "/queryMerchantCoinPayerReturnPage", method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    public ReturnMsg queryMerchantCoinPayerReturnPage(
            String withdrawNo,
            String channelWithdrawNo,
            String payerName,
            String payerAddr,
            String acctName,
            String acctAddr,
            String withdrawStatus,
            String beginTime,
            String endTime,
            String remark,
            String tableMerchantId,
            @NotNull(message = "行数不能为空") Integer rows,
            @NotNull(message = "页码不能为空") Integer page) throws TranException {

        if (tableMerchantId == null || "".equals(tableMerchantId)) {
            throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "商户号为空");
        }

        PayMerchantInfo merchantInfo = iPayMerchantInfoService.getOne(new QueryWrapper<PayMerchantInfo>().lambda().eq(PayMerchantInfo::getMerchantId, tableMerchantId));
        if (merchantInfo == null) {
            throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "无此商户号");
        }
        //取得商家的通道号 cardChannelCode
        String channelCode = (merchantInfo.getChannelCode() != null && !"".equals(merchantInfo.getChannelCode())) ? merchantInfo.getChannelCode() : "";
        if ("".equals(channelCode)) {
            throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "商户还未分配通道");
        }

        Page<CoinPayerReturnInfo> pages = new Page<>(page, rows);
        QueryWrapper<CoinPayerReturnInfo> queryWrapper = new QueryWrapper<>();

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("WITHDRAW_NO", withdrawNo);
        params.put("CHANNEL_WITHDRAW_NO", channelWithdrawNo);
        params.put("CHANNEL_CODE", channelCode);
        params.put("PAYER_NAME", payerName);
        params.put("PAYER_ADDR", payerAddr);
        params.put("ACCT_NAME", acctName); //PAYEE
        params.put("ACCT_ADDR", acctAddr);
        params.put("TASK_RETURN", CodeConst.TaskReturnConst.TASK_RETURN_Y); //查询默认已冲正的订单

        queryWrapper = queryWrapper.allEq(params, false)
                .like(StringUtils.isNotBlank(remark), "REMARK", HtmlUtil.delHTMLTag(remark))
                .ge(StringUtils.isNotBlank(beginTime), "CREATE_TIME", beginTime + " 00:00:00")
                .le(StringUtils.isNotBlank(endTime), "CREATE_TIME", endTime + " 23:59:59");
        queryWrapper.orderByDesc("ID");
        //page
        IPage<CoinPayerReturnInfo> pageMap = iCoinPayerReturnInfoService.page(pages, queryWrapper);

        //2统计交易订单
        Map<String, Object> paramsSearch = new HashMap<String, Object>();
        paramsSearch.put("withdrawNo", withdrawNo);
        paramsSearch.put("channelWithdrawNo", channelWithdrawNo);
        paramsSearch.put("channelCode", channelCode);
        paramsSearch.put("payerName", payerName);
        paramsSearch.put("payerAddr", payerAddr);
        paramsSearch.put("acctName", acctName); //PAYEE
        paramsSearch.put("acctAddr", acctAddr);
        paramsSearch.put("beginTime", beginTime);
        paramsSearch.put("endTime", endTime);
        paramsSearch.put("taskReturn", CodeConst.TaskReturnConst.TASK_RETURN_Y); //查询默认已冲正的订单

        BigDecimal sumCardPayerWithdraw = iCoinPayerReturnInfoService.sumCoinPayerReturnInfo(paramsSearch);
        Integer countCardPayerWithdraw = iCoinPayerReturnInfoService.countCoinPayerReturn(paramsSearch);

        paramsSearch.put("status", ""); //下发手续费不受订单状态选项的影响
        Map<String, Object> sumAllWithdrawFeeMap = iCoinPayerReturnInfoService.sumCoinPayerReturnFee(paramsSearch);

        paramsSearch.put("status", CodeConst.WithdrawStatusConst.WITHDRAW_FAIL);
        Map<String, Object> sumFailWithdrawFeeMap = iCoinPayerReturnInfoService.sumCoinPayerReturnFee(paramsSearch);

        //查询结果 map
        BigDecimal sumPayerFee = new BigDecimal(sumAllWithdrawFeeMap.get("sumPayerFee").toString());
        BigDecimal sumPayerRateFee = new BigDecimal(sumAllWithdrawFeeMap.get("sumPayerRateFee").toString());

        BigDecimal sumPayerFailFee = new BigDecimal(sumFailWithdrawFeeMap.get("sumPayerFee").toString());
        BigDecimal sumPayerFailRateFee = new BigDecimal(sumFailWithdrawFeeMap.get("sumPayerRateFee").toString());

        Map<String, Object> resultMap = new HashMap<String, Object>();
        resultMap.put("sumCardPayerWithdraw", sumCardPayerWithdraw);
        resultMap.put("countCardPayerWithdraw", countCardPayerWithdraw);
        resultMap.put("sumCardPayerWithdrawFee", sumPayerFee.subtract(sumPayerFailFee)); //总下发单笔手续费 - 失败的手续费
        resultMap.put("sumCardPayerWithdrawRateFee", sumPayerRateFee.subtract(sumPayerFailRateFee)); //总下发手续费汇率
        resultMap.put("pages", pageMap);

        return new ReturnMsg(resultMap);
    }


    /**
     * 设为失败
     *
     * @param withdrawNo
     * @return
     * @throws Exception
     * @WARNING:有任务开始时间的订单,超时5分钟出现修正状态
     */
    @RequestMapping(value = "/setWithdrawStatusFail", method = {RequestMethod.POST, RequestMethod.GET}, name = "admin")
    @ResponseBody
    @Transactional(rollbackFor = Exception.class)
    public ReturnMsg setWithdrawStatusFail(
            @NotBlank(message = "平台订单号不能为空") String withdrawNo,
            @NotBlank(message = "管理员ID不能为空") String tableMerchantId) throws TranException {

        try {

            //查询管理员
            PayMerchantInfo merchantInfo = iPayMerchantInfoService.getOne(new QueryWrapper<PayMerchantInfo>().lambda().eq(PayMerchantInfo::getMerchantId, tableMerchantId));

            CoinPayerWithdrawInfo coinPayerWithdrawInfoOne = iCoinPayerWithdrawInfoService.getOne(new QueryWrapper<CoinPayerWithdrawInfo>().lambda()
                    .eq(CoinPayerWithdrawInfo::getWithdrawNo, withdrawNo));

            if (coinPayerWithdrawInfoOne == null) {
                throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "订单不存在");
            }

            //订单[排队中]才可修正 + 查询的任务也不可设为失败
            if (!coinPayerWithdrawInfoOne.getTaskType().equals(CodeConst.TaskTypeConst.TRANSFER_TO_CARD)) {
                throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "非打款任务,不可设为失败");
            }

            //订单[排队中]才可修正 + 查询的任务也不可设为失败
            if (!coinPayerWithdrawInfoOne.getStatus().equals(CodeConst.WithdrawStatusConst.WITHDRAW_UNKNOWN)) {
                throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "订单状态非[排队中],不可设为失败");
            }

            //1.找到哪个钱包要退
            CoinPayerInfo coinPayerInfo = iCoinPayerInfoService.getOne(new QueryWrapper<CoinPayerInfo>().lambda()
                    .eq(CoinPayerInfo::getPayerAddr, coinPayerWithdrawInfoOne.getPayerAddr()));

            if (coinPayerInfo == null) {
                throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "付款钱包不存在");
            }
            //持卡人原有金额
            //BigDecimal amtBefore = cardPayerInfo.getPayerAmt();

            //2.记录银行卡 交易流水列表/退款
            String withdrawRemark = "[" + merchantInfo.getMerchantName() + "]设为失败"; //更新订单状态

            //1.设置为失败 2.退款 3.记录退款流水 4.返回通道手续费 5.记录通道流水
            CoinPayerWithdrawInfo entity = new CoinPayerWithdrawInfo();
            entity.setStatus(CodeConst.WithdrawStatusConst.WITHDRAW_FAIL);
            entity.setRemark(withdrawRemark);
            entity.setTaskEndTime(new Date());
            boolean isUpd = iCoinPayerWithdrawInfoService.update(entity, new QueryWrapper<CoinPayerWithdrawInfo>()
                    .lambda().in(CoinPayerWithdrawInfo::getWithdrawNo, withdrawNo));
            if (!isUpd) {
                throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "订单状态更新不成功");
            }

            /**
             * TODO:得升级为服务Service 重复代码过多
             * */
            String flowType = EnumFlowType.RECEIVE.getName(); //冲正
            String channelWithdrawNo = coinPayerWithdrawInfoOne.getChannelWithdrawNo();
            String remark = "冲正->" + coinPayerInfo.getPayerName() + ":" + coinPayerInfo.getPayerAddr();
            coinWithdrawManagerService.alterCoinPayerBalanceNAddFlowService(coinPayerInfo, coinPayerWithdrawInfoOne.getAmt(), flowType, withdrawNo,channelWithdrawNo, remark);

            //找出对应通道的商户并退款
            PayMerchantInfo payMerchantInfo = iPayMerchantInfoService.getOne(new QueryWrapper<PayMerchantInfo>().lambda()
                    .eq(PayMerchantInfo::getAgentRate,"0")
                    .eq(PayMerchantInfo::getChannelCode, coinPayerWithdrawInfoOne.getChannelCode()));

            //3.加手续费流水 + 加通道余额
            String channelRemark = "退回 - " + coinPayerInfo.getPayerName() + ":" + coinPayerInfo.getPayerAddr() + "->" + coinPayerWithdrawInfoOne.getAcctName() + ":" + coinPayerWithdrawInfoOne.getAcctAddr();

            coinChannelManagerService.alterCoinChannelBalanceService(
                    payMerchantInfo.getMerchantId(),//商户ID 非管理员ID
                    coinPayerWithdrawInfoOne.getPayerSingleFee().add(coinPayerWithdrawInfoOne.getPayerRateFee()), //单笔手续费 + 下发汇率
                    CodeConst.CardFlowTypeConst.RETURN,
                    withdrawNo, //四方的订单号
                    coinPayerWithdrawInfoOne.getChannelWithdrawNo(),
                    coinPayerWithdrawInfoOne.getChannelCode(),
                    merchantInfo.getMerchantId(),//哪个四方平台商户提交的
                    coinPayerWithdrawInfoOne.getChannelName(),
                    channelRemark
            );

            //5.返回结果
            Map<String, Object> resultMap = new HashMap<String, Object>();
            resultMap.put("withdrawNo", withdrawNo);
            resultMap.put("amt", coinPayerWithdrawInfoOne.getAmt());
            resultMap.put("payerFee", coinPayerWithdrawInfoOne.getPayerSingleFee().add(coinPayerWithdrawInfoOne.getPayerRateFee()));
            return new ReturnMsg(resultMap);

        } catch (Exception e) {
            return new ReturnMsg().setFail(e.getMessage());
        }

    }

    /**
     * 设为强制失败
     * 开放权限给商户
     * @param withdrawNo
     * @return
     * @throws Exception
     * @WARNING:有任务开始时间的订单,超时5分钟出现修正状态
     */
    @RequestMapping(value = "/setWithdrawStatusForceFail", method = {RequestMethod.POST, RequestMethod.GET}, name = "admin")
    @ResponseBody
    @Transactional(rollbackFor = Exception.class)
    public ReturnMsg setWithdrawStatusForceFail(
            @NotBlank(message = "google不能为空") String googleCode,
            @NotBlank(message = "平台订单号不能为空") String withdrawNo,
            @NotBlank(message = "管理员ID不能为空") String tableMerchantId) throws TranException {
        try {

            //查询管理员
            PayMerchantInfo merchantInfo = iPayMerchantInfoService.getOne(new QueryWrapper<PayMerchantInfo>().lambda().eq(PayMerchantInfo::getMerchantId, tableMerchantId));

            String googleKeyDecode =  EncryptUtil.aesDecrypt(merchantInfo.getGoogleKey(),walletPrivateKeySalt);
            if(!GoogleAuthenticator.check_code(googleKeyDecode, googleCode, System.currentTimeMillis())){
                throw new TranException(CodeConst.BUSINESS_ERROR_CODE,"google验证码错误");
            }

            CoinPayerWithdrawInfo coinPayerWithdrawInfoOne = iCoinPayerWithdrawInfoService.getOne(new QueryWrapper<CoinPayerWithdrawInfo>().lambda()
                    .eq(CoinPayerWithdrawInfo::getWithdrawNo, withdrawNo));

            if (coinPayerWithdrawInfoOne == null) {
                throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "订单[" + withdrawNo + "]不存在");
            }

            //商户+代理
            if (merchantInfo.getAccountType().equals(EnumAccountType.MERCHANT.getName())) {
                //如果是商户 检查订单是否是该商户提交的订单
                if (!"0".equals(merchantInfo.getAgentRate())){
                    throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "代理无操作权限");
                }
                if (!coinPayerWithdrawInfoOne.getChannelCode().equals(merchantInfo.getChannelCode())){
                    throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "商户无操作权限");
                }
            }

            //订单[排队中]才可修正 + 查询的任务也不可设为失败
            if (!coinPayerWithdrawInfoOne.getStatus().equals(CodeConst.WithdrawStatusConst.WITHDRAW_NO_CLEAR)) {
                throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "订单状态非[未知],不可修改订单状态");
            }

            //1.找到哪个用户要退
            CoinPayerInfo coinPayerInfo = iCoinPayerInfoService.getOne(new QueryWrapper<CoinPayerInfo>().lambda()
                    .eq(CoinPayerInfo::getPayerAddr, coinPayerWithdrawInfoOne.getPayerAddr()));

            if (coinPayerInfo == null) {
                throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "付款钱包不存在");
            }
            //持卡人原有金额
            //BigDecimal amtBefore = cardPayerInfo.getPayerAmt();

            //2.记录银行卡 交易流水列表/退款
            String withdrawRemark = "[" + merchantInfo.getMerchantName() + "]强制设为失败"; //更新订单状态

            //1.设置为失败 2.退款 3.记录退款流水 4.返回通道手续费 5.记录通道流水
            CoinPayerWithdrawInfo updCoinPayerWithdrawEntity = new CoinPayerWithdrawInfo();
            updCoinPayerWithdrawEntity.setStatus(CodeConst.WithdrawStatusConst.WITHDRAW_FAIL);
            updCoinPayerWithdrawEntity.setRemark(withdrawRemark);
            updCoinPayerWithdrawEntity.setTaskEndTime(new Date());
            boolean isUpd = iCoinPayerWithdrawInfoService.update(updCoinPayerWithdrawEntity,
                    new QueryWrapper<CoinPayerWithdrawInfo>().lambda().in(CoinPayerWithdrawInfo::getWithdrawNo, withdrawNo));
            if (!isUpd) {
                throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "订单状态更新不成功");
            }


            String flowType = EnumFlowType.RECEIVE.getName(); //冲正
            //String cardWithdrawNo = "C" + DateUtil.format(new Date(), DateUtil.FULL_TIME_FORMAT_EN) + "0" + System.currentTimeMillis();
            String channelWithdrawNo = coinPayerWithdrawInfoOne.getChannelWithdrawNo();
            String remark = "冲正" + coinPayerInfo.getPayerName() + coinPayerInfo.getPayerAddr();
            coinWithdrawManagerService.alterCoinPayerBalanceNAddFlowService(coinPayerInfo, coinPayerWithdrawInfoOne.getAmt(), flowType, withdrawNo,channelWithdrawNo, remark);


            //找出对应通道的商户并退款 (商户等级为 0 )
            PayMerchantInfo payMerchantInfo = iPayMerchantInfoService.getOne(new QueryWrapper<PayMerchantInfo>().lambda()
                    .eq(PayMerchantInfo::getAgentRate,"0")
                    .eq(PayMerchantInfo::getChannelCode, coinPayerWithdrawInfoOne.getChannelCode()));


            //3.加手续费流水
            //4.加通道余额
            String channelRemark = "冲正 - " + coinPayerInfo.getPayerName() + ":" + coinPayerInfo.getPayerAddr()
                    + "->"
                    + coinPayerWithdrawInfoOne.getAcctName() + ":" + coinPayerWithdrawInfoOne.getAcctAddr();

            //退回手续费 + 矿工费不退回
            coinChannelManagerService.alterCoinChannelBalanceService(
                    payMerchantInfo.getMerchantId(),//商家ID
                    coinPayerWithdrawInfoOne.getPayerSingleFee().add(coinPayerWithdrawInfoOne.getPayerRateFee()), //单笔手续费 + 下发汇率
                    CodeConst.CardFlowTypeConst.RETURN,
                    withdrawNo, //四方的订单号
                    coinPayerWithdrawInfoOne.getChannelWithdrawNo(),
                    coinPayerWithdrawInfoOne.getChannelCode(),
                    merchantInfo.getMerchantId(),//哪个四方平台商户提交的
                    coinPayerWithdrawInfoOne.getChannelName(),
                    channelRemark
            );

            //5.返回结果
            Map<String, Object> resultMap = new HashMap<String, Object>();
            resultMap.put("withdrawNo", withdrawNo);
            resultMap.put("amt", coinPayerWithdrawInfoOne.getAmt());
            resultMap.put("payerFee", coinPayerWithdrawInfoOne.getPayerSingleFee().add(coinPayerWithdrawInfoOne.getPayerRateFee())); //退回 单笔手续费 + 下发汇率
            return new ReturnMsg(resultMap);

        } catch (Exception e) {
            return new ReturnMsg().setFail(e.getMessage());
        }

    }


    /**
     * 设为冲正
     * 人工设为冲正 ：关闭
     * @param withdrawNo
     * @return
     * @throws Exception
     * @WARNING:有任务开始时间的订单,超时5分钟出现修正状态
     */
    @RequestMapping(value = "/setWithdrawStatusReturn", method = {RequestMethod.POST, RequestMethod.GET}, name = "admin")
    @ResponseBody
    @Transactional(rollbackFor = Exception.class)
    public ReturnMsg setWithdrawStatusReturn(
            @NotBlank(message = "平台订单号不能为空") String withdrawNo,
            @NotBlank(message = "管理员ID不能为空") String tableMerchantId
    ) throws TranException {
        return new ReturnMsg();
//        try {
//
//            //查询管理员
//            PayMerchantInfo merchantInfo = iPayMerchantInfoService.getOne(new QueryWrapper<PayMerchantInfo>().lambda().eq(PayMerchantInfo::getMerchantId, tableMerchantId));
//
//            CoinPayerReturnInfo coinPayerReturnInfo = iCoinPayerReturnInfoService.getOne(new QueryWrapper<CoinPayerReturnInfo>().lambda()
//                    .eq(CoinPayerReturnInfo::getWithdrawNo, withdrawNo));
//
//            if (coinPayerReturnInfo == null) {
//                throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "冲正订单不存在");
//            }
//
//            //订单[排队中]才可修正 + 查询的任务也不可设为失败
//            if (!coinPayerReturnInfo.getTaskReturn().equals(CodeConst.TaskReturnConst.TASK_RETURN_N)) {
//                throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "订单已冲正");
//            }
//
//            //1.找到哪个用户要退
//            CoinPayerInfo payerInfo = iCoinPayerInfoService.getOne(new QueryWrapper<CoinPayerInfo>().lambda()
//                    .eq(CoinPayerInfo::getPayerAddr, coinPayerReturnInfo.getPayerAddr()));
//
//            if (payerInfo == null) {
//                throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "付款钱包不存在");
//            }
//
//            //2.设置冲正
//            CoinPayerReturnInfo returnEntity = new CoinPayerReturnInfo();
//            returnEntity.setTaskReturn(CodeConst.TaskReturnConst.TASK_RETURN_Y);
//            returnEntity.setRemark("已冲正");
//            returnEntity.setTaskEndTime(new Date());
//            boolean isUpd = iCoinPayerReturnInfoService.update(returnEntity, new QueryWrapper<CoinPayerReturnInfo>()
//                    .lambda().in(CoinPayerReturnInfo::getWithdrawNo, withdrawNo));
//            if (!isUpd) {
//                throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "订单冲正不成功");
//            }
//
//            /**
//             * @Service
//             * 银行卡余额变动+流水变动服务
//             * */
//            String flowType = EnumFlowType.RECEIVE.getName(); //冲正
//            String channelWithdrawNo = coinPayerReturnInfo.getChannelWithdrawNo();
//            String remark = "冲正 - " + coinPayerReturnInfo.getPayerName() + coinPayerReturnInfo.getPayerAddr();
//            coinWithdrawManagerService.alterCoinPayerBalanceNAddFlowService(payerInfo, coinPayerReturnInfo.getAmt(), flowType, withdrawNo,channelWithdrawNo, remark);
//
//
//            //查询商户订单
//            PayWithdrawInfo withdrawInfo = iPayWithdrawInfoService.getOne(new QueryWrapper<PayWithdrawInfo>().lambda().eq(PayWithdrawInfo::getWithdrawNo, coinPayerReturnInfo.getWithdrawNo()));
//            if (withdrawInfo == null) {
//                throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "商户订单不存在");
//            }
//
//            //由查询商户订单号查出哪个商户ID
//            PayMerchantInfo currentMerchantInfo = iPayMerchantInfoService.getOne(new QueryWrapper<PayMerchantInfo>().lambda().eq(PayMerchantInfo::getMerchantId, withdrawInfo.getMerchantId()));
//            if (currentMerchantInfo == null) { //==null
//                throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "商户不存在");
//            }
//
//            //3.加手续费流水 -- 高并发也会有bug
//            //4.加通道余额
//            String channelRemark = "冲正 - " + payerInfo.getPayerName() + ":" + payerInfo.getPayerAddr()
//                    + "->"
//                    + coinPayerReturnInfo.getAcctName() + ":" + coinPayerReturnInfo.getAcctAddr();
//
//            coinChannelManagerService.alterCoinChannelBalanceService(
//                    currentMerchantInfo.getMerchantId(),
//                    coinPayerReturnInfo.getPayerSingleFee().add(coinPayerReturnInfo.getPayerRateFee()), //单笔手续费 + 下发汇率
//                    CodeConst.CardFlowTypeConst.RETURN,
//                    withdrawNo, //四方的订单号
//                    coinPayerReturnInfo.getChannelWithdrawNo(),
//                    coinPayerReturnInfo.getChannelCode(),
//                    coinPayerReturnInfo.getChannelName(),//哪个四方平台商户提交的
//                    coinPayerReturnInfo.getChannelName(),
//                    channelRemark
//            );
//
//
//            //3.加银行卡余额 扣款加余额守护 退款不用 --> 已在server服务流水中退 不可再重复退款
//           /* Map<String, Object> paramsUpd = new HashMap<>();
//            paramsUpd.put("amt", cardPayerReturnInfo.getAmt()); //加卡余额
//            paramsUpd.put("payerProcessingNum", 0); //排队中扣减一笔
//            paramsUpd.put("payerCardNo", cardPayerReturnInfo.getPayerCardNo());
//            // paramsUpd.put("payerBalance", null);
//            Integer cardPayerBalanceIndex = iCardPayerInfoService.alterCardPayerAmtNProcessingNum(paramsUpd);
//
//            if (cardPayerBalanceIndex != 1) {
//                throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "退回银行卡余额失败");
//            }*/
//
//            //4.加商户余额
//            //fixme:联表处理
//
//            //5.加商户变动流水
//            Integer isGetBackIndex = getReturnLatestMoneyRecord(withdrawInfo, coinPayerReturnInfo.getAmt());
//            //记录退款流水表失败 不退款
//            if (isGetBackIndex != 1) {
//                throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "退回商户退款流水表失败");
//            }
//
//            //6.商户退回提现/代付
//            Integer indexBalance = balanceService.alterMerchantBalance(withdrawInfo.getMerchantId(), coinPayerReturnInfo.getAmt(), null);
//
//            if (indexBalance != 1) {
//                _log.info("=====>>>>>refundQueuey修改商家:{}退费:{}失败", withdrawInfo.getMerchantId(), coinPayerReturnInfo.getAmt());
//                throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "退回商户余额失败");
//            }
//            //7.扣除代理的分成
//
//            BigDecimal agentProfit = new BigDecimal(0);
//            BigDecimal agentSingleProfit = new BigDecimal(0);  //初始代理单笔分成 为 0元
//            BigDecimal agentRateProfit = new BigDecimal(0); //初始代理下发分成 为 0元
//            //递归取得代理的单笔总分成
//            agentSingleProfit = alterAgentSingleBalance(agentSingleProfit, currentMerchantInfo, withdrawInfo);
//
//            //下发单笔费率总分成
//            agentRateProfit = alterAgentRateBalance(agentRateProfit, currentMerchantInfo, withdrawInfo);
//
//            agentProfit = agentProfit.add(agentSingleProfit).add(agentRateProfit);
//            //8.扣除计算平台的利润
//            iPayPlatformIncomeInfoService.alterPlatformBalance(withdrawInfo.getChannelCode(), agentProfit, null);
//
//            //9.返回结果
//            Map<String, Object> resultMap = new HashMap<String, Object>();
//            resultMap.put("withdrawNo", withdrawNo);
//            resultMap.put("amt", coinPayerReturnInfo.getAmt());
//            resultMap.put("payerFee", coinPayerReturnInfo.getPayerSingleFee().add(coinPayerReturnInfo.getPayerRateFee()));
//            return new ReturnMsg(resultMap);
//
//        } catch (Exception e) {
//            return new ReturnMsg().setFail(e.getMessage());
//        }

    }

    /**
     * 设为强制冲正
     *
     * @param withdrawNo
     * @return
     * @throws Exception
     * @WARNING:有任务开始时间的订单,超时5分钟出现修正状态
     */
    @RequestMapping(value = "/setWithdrawStatusForceReturn", method = {RequestMethod.POST, RequestMethod.GET}, name = "admin")
    @ResponseBody
    @Transactional(rollbackFor = Exception.class)
    public ReturnMsg setWithdrawStatusForceReturn(
            @NotBlank(message = "google不能为空") String googleCode,
            @NotBlank(message = "平台订单号不能为空") String withdrawNo,
            @NotBlank(message = "管理员ID不能为空") String tableMerchantId) throws Exception {

        //查询管理员
        PayMerchantInfo merchantInfo = iPayMerchantInfoService.getOne(new QueryWrapper<PayMerchantInfo>().lambda().eq(PayMerchantInfo::getMerchantId, tableMerchantId));

        String googleKeyDecode =  EncryptUtil.aesDecrypt(merchantInfo.getGoogleKey(),walletPrivateKeySalt);
        if(!GoogleAuthenticator.check_code(googleKeyDecode, googleCode, System.currentTimeMillis())){
            throw new TranException(CodeConst.BUSINESS_ERROR_CODE,"google验证码错误");
        }

        if (merchantInfo.getAccountType().equals(EnumAccountType.MERCHANT.getName())) {
            throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "权限不足");
        }
        //已在冲正列表内的订单不再进行冲正
        CoinPayerReturnInfo coinPayerReturnOne = iCoinPayerReturnInfoService.getOne(new QueryWrapper<CoinPayerReturnInfo>().lambda()
                .eq(CoinPayerReturnInfo::getWithdrawNo, withdrawNo));

        if (coinPayerReturnOne != null) {
            throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "订单[" + withdrawNo + "]已冲正,不可再二次冲正");
        }

        //2.查询订单是否已经付款成功
        CoinPayerWithdrawInfo coinPayerWithdrawInfoOne = iCoinPayerWithdrawInfoService.getOne(new QueryWrapper<CoinPayerWithdrawInfo>().lambda()
                .eq(CoinPayerWithdrawInfo::getWithdrawNo, withdrawNo)
                .eq(CoinPayerWithdrawInfo::getStatus, CodeConst.WithdrawStatusConst.WITHDRAW_SUCCESS)
        );

        if (coinPayerWithdrawInfoOne == null) {
            throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "订单[" + withdrawNo + "]非成功状态,不可冲正");
        }

        //3.找到哪个用户要退
        CoinPayerInfo payerInfo = iCoinPayerInfoService.getOne(new QueryWrapper<CoinPayerInfo>().lambda()
                .eq(CoinPayerInfo::getPayerAddr, coinPayerWithdrawInfoOne.getPayerAddr()));

        if (payerInfo == null) {
            throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "付款钱包不存在");
        }

        //3.插入到冲正订单列表内
        CoinPayerReturnInfo insertEntity = new CoinPayerReturnInfo();
        insertEntity.setWithdrawNo(coinPayerWithdrawInfoOne.getWithdrawNo());//四方平台的订单号
        insertEntity.setChannelWithdrawNo(coinPayerWithdrawInfoOne.getChannelWithdrawNo());//银行卡订单号
        insertEntity.setStatus(coinPayerWithdrawInfoOne.getStatus());
        insertEntity.setAmt(coinPayerWithdrawInfoOne.getAmt()); //下发金额 USDT

        insertEntity.setPayerSingleFee(coinPayerWithdrawInfoOne.getPayerSingleFee()); //通道的单笔手续费
        insertEntity.setPayerRateFee(coinPayerWithdrawInfoOne.getPayerRateFee()); //通道的下发手续费汇率


        insertEntity.setPayerAddr(coinPayerWithdrawInfoOne.getPayerAddr());
        insertEntity.setPayerName(coinPayerWithdrawInfoOne.getPayerName());

        insertEntity.setAcctAddr(coinPayerWithdrawInfoOne.getAcctAddr());
        insertEntity.setAcctName(coinPayerWithdrawInfoOne.getAcctName());
        insertEntity.setChannelCode(coinPayerWithdrawInfoOne.getChannelCode());
        insertEntity.setChannelName(coinPayerWithdrawInfoOne.getChannelName());
        insertEntity.setChannelFeeType(coinPayerWithdrawInfoOne.getChannelFeeType());
        insertEntity.setCreateTime(coinPayerWithdrawInfoOne.getCreateTime()); //插入冲正订单列表的时间 以原订单的时间为准
        insertEntity.setDate(coinPayerWithdrawInfoOne.getDate()); //原有订单日期
        insertEntity.setTaskStartTime(coinPayerWithdrawInfoOne.getTaskStartTime()); //原有订单处理时间
        insertEntity.setTaskEndTime(new Date()); //冲正的时间
        insertEntity.setTaskReturn(CodeConst.TaskReturnConst.TASK_RETURN_Y); //默认已冲正
        insertEntity.setRemark("[" + merchantInfo.getMerchantName() + "]强制冲正");

        boolean isInsertEntity = iCoinPayerReturnInfoService.save(insertEntity);
        if (!isInsertEntity) {
            _log.error("冲正订单[" + coinPayerWithdrawInfoOne.getWithdrawNo() + "]插入列表失败");
            throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "冲正订单[" + coinPayerWithdrawInfoOne.getWithdrawNo() + "]插入列表失败");
        }

        /**
         * @Service
         * 银行卡余额变动+流水变动服务
         * */
        String flowType = EnumFlowType.RECEIVE.getName(); //冲正
        String channelWithdrawNo = coinPayerWithdrawInfoOne.getChannelWithdrawNo();
        String remark = "[" + merchantInfo.getMerchantName() + "]强制冲正-" + coinPayerWithdrawInfoOne.getPayerName() +"-"+ coinPayerWithdrawInfoOne.getPayerAddr();
        coinWithdrawManagerService.alterCoinPayerBalanceNAddFlowService(payerInfo, coinPayerWithdrawInfoOne.getAmt(), flowType, withdrawNo,channelWithdrawNo, remark);


        //3.加手续费流水
        PayWithdrawInfo withdrawInfo = iPayWithdrawInfoService.getOne(new QueryWrapper<PayWithdrawInfo>().lambda().eq(PayWithdrawInfo::getWithdrawNo, coinPayerWithdrawInfoOne.getWithdrawNo()));
        if (withdrawInfo == null) {
            throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "商户订单[" + withdrawInfo.getWithdrawNo() + "]不存在");
        }

        //由查询商户订单号查出哪个商户ID
        PayMerchantInfo currentMerchantInfo = iPayMerchantInfoService.getOne(new QueryWrapper<PayMerchantInfo>().lambda().eq(PayMerchantInfo::getMerchantId, withdrawInfo.getMerchantId()));
        if (currentMerchantInfo == null) { //==null
            throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "商户不存在");
        }
        //4.加通道余额
        String channelRemark = "[" + merchantInfo.getMerchantName() + "]强制冲正-" + payerInfo.getPayerName() + ":" + payerInfo.getPayerAddr()
                + "->"
                + coinPayerWithdrawInfoOne.getAcctName() + ":" + coinPayerWithdrawInfoOne.getAcctAddr();

        coinChannelManagerService.alterCoinChannelBalanceService(
                currentMerchantInfo.getMerchantId(), //不是管理员的ID
                coinPayerWithdrawInfoOne.getPayerSingleFee().add(coinPayerWithdrawInfoOne.getPayerRateFee()), //单笔手续费 + 下发汇率 (手续费增加)
                CodeConst.CardFlowTypeConst.RETURN,
                withdrawNo, //四方的订单号
                coinPayerWithdrawInfoOne.getChannelWithdrawNo(),
                coinPayerWithdrawInfoOne.getChannelCode(),
                merchantInfo.getMerchantId(),//哪个四方平台商户提交的
                coinPayerWithdrawInfoOne.getChannelName(),
                channelRemark
        );


        //4.加商户余额
        //查询商户订单


        //5.加商户变动流水
        Integer isGetBackIndex = getReturnLatestMoneyRecord(withdrawInfo, coinPayerWithdrawInfoOne.getAmt());
        //记录退款流水表失败 不退款
        if (isGetBackIndex != 1) {
            throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "退回商户退款流水表失败");
        }

        //6.商户退回提现/代付
        Integer indexBalance = balanceService.alterMerchantBalance(withdrawInfo.getMerchantId(), coinPayerWithdrawInfoOne.getAmt(), null);

        if (indexBalance != 1) {
            _log.info("=====>>>>>refundQueuey修改商家:{}退费:{}失败", withdrawInfo.getMerchantId(), coinPayerWithdrawInfoOne.getAmt());
            throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "退回商户余额失败");
        }
        //7.扣除代理的分成

        BigDecimal agentProfit = new BigDecimal(0);
        BigDecimal agentSingleProfit = new BigDecimal(0);  //初始代理单笔分成 为 0元
        BigDecimal agentRateProfit = new BigDecimal(0); //初始代理下发分成 为 0元
        //递归取得代理的单笔总分成
        agentSingleProfit = alterAgentSingleBalance(agentSingleProfit, currentMerchantInfo, withdrawInfo);

        //下发单笔费率总分成
        agentRateProfit = alterAgentRateBalance(agentRateProfit, currentMerchantInfo, withdrawInfo);

        agentProfit = agentProfit.add(agentSingleProfit).add(agentRateProfit);
        //8.扣除计算平台的利润
        iPayPlatformIncomeInfoService.alterPlatformBalance(withdrawInfo.getChannelCode(), agentProfit, null);

        //9.返回结果
        Map<String, Object> resultMap = new HashMap<String, Object>();
        resultMap.put("withdrawNo", withdrawNo);
        resultMap.put("amt", coinPayerWithdrawInfoOne.getAmt());
        resultMap.put("payerFee", coinPayerWithdrawInfoOne.getPayerSingleFee().add(coinPayerWithdrawInfoOne.getPayerRateFee()));
        return new ReturnMsg(resultMap);
    }



    /**
     * 代付订单设为成功
     *
     * @param withdrawNo
     * @return
     * @throws Exception
     * @WARNING:有任务开始时间的订单,超时5分钟出现修正状态
     */
    @RequestMapping(value = "/setWithdrawStatusSuccess", method = {RequestMethod.POST, RequestMethod.GET}, name = "admin")
    @ResponseBody
    @Transactional(rollbackFor = Exception.class)
    public ReturnMsg setWithdrawStatusSuccess(
            @NotBlank(message = "平台订单号不能为空") String withdrawNo,
            @NotBlank(message = "管理员ID不能为空") String tableMerchantId
    ) throws TranException {

        //查询管理员
        PayMerchantInfo merchantInfo = iPayMerchantInfoService.getOne(new QueryWrapper<PayMerchantInfo>().lambda().eq(PayMerchantInfo::getMerchantId, tableMerchantId));

        CoinPayerWithdrawInfo coinPayerWithdrawInfoOne = iCoinPayerWithdrawInfoService.getOne(new QueryWrapper<CoinPayerWithdrawInfo>().lambda()
                .eq(CoinPayerWithdrawInfo::getWithdrawNo, withdrawNo));

        if (coinPayerWithdrawInfoOne == null) {
            throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "订单不存在");
        }

        //订单[排队中]才可修正 + 查询的任务也不可设为失败
        if (!coinPayerWithdrawInfoOne.getTaskType().equals(CodeConst.TaskTypeConst.TRANSFER_TO_CARD)) {
            throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "非打款任务,不可设为成功");
        }

        //订单[排队中]才可修正 + 查询的任务也不可设为失败
        if (!coinPayerWithdrawInfoOne.getStatus().equals(CodeConst.WithdrawStatusConst.WITHDRAW_UNKNOWN)) {
            throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "订单状态非[排队中],不可设为成功");
        }

        CoinPayerInfo payerInfo = iCoinPayerInfoService.getOne(new QueryWrapper<CoinPayerInfo>().lambda()
                .eq(CoinPayerInfo::getPayerAddr, coinPayerWithdrawInfoOne.getPayerAddr()));

        if (payerInfo == null) {
            throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "付款钱包不存在");
        }
        //持卡人原有金额
        BigDecimal amtBefore = payerInfo.getAmt();

        //@设置订单为失败状态

        String withdrawRemark = "[" + merchantInfo.getMerchantName() + "]设为成功"; //更新订单状态

        //1.设置为成功 修改订单状态
        CoinPayerWithdrawInfo entity = new CoinPayerWithdrawInfo();
        entity.setStatus(CodeConst.WithdrawStatusConst.WITHDRAW_SUCCESS);
        entity.setRemark(withdrawRemark);
        entity.setTaskEndTime(new Date());
        boolean isUpd = iCoinPayerWithdrawInfoService.update(entity, new QueryWrapper<CoinPayerWithdrawInfo>()
                .lambda().in(CoinPayerWithdrawInfo::getWithdrawNo, withdrawNo));
        if (!isUpd) {
            throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "订单状态更新不成功");
        }

        //5.返回结果给前端
        Map<String, Object> resultMap = new HashMap<String, Object>();
        resultMap.put("withdrawNo", withdrawNo);
        resultMap.put("amt", coinPayerWithdrawInfoOne.getAmt());
        resultMap.put("payerFee", coinPayerWithdrawInfoOne.getPayerSingleFee().add(coinPayerWithdrawInfoOne.getPayerRateFee()));
        return new ReturnMsg(resultMap);
    }


    /**
     * 卡主人工 处理中
     *
     * @param withdrawNo
     * @return
     * @throws Exception
     * @WARNING:有任务开始时间的订单,超时5分钟出现修正状态 事务不能TRY CATCH 否则不会走事务
     */
    @RequestMapping(value = "/setWithdrawStatusProcess", method = {RequestMethod.POST})
    @ResponseBody
    @Transactional(rollbackFor = Exception.class)
    public ReturnMsg setWithdrawStatusProcess(
            @NotBlank(message = "平台订单号不能为空") String withdrawNo,
            @NotBlank(message = "操作员ID不能为空") String tableMerchantId,
            @NotBlank(message = "订单状态有误") String status) throws TranException {

        //查询管理员
//        PayMerchantInfo merchantInfo = iPayMerchantInfoService.getOne(new QueryWrapper<PayMerchantInfo>().lambda().eq(PayMerchantInfo::getMerchantId, tableMerchantId));
//
//        if (merchantInfo == null){
//            throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "无操作权限");
//        }
//        if (!status.equals(CodeConst.WithdrawStatusConst.WITHDRAW_ONGOING)) {
//            throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "订单状态有误");
//        }
//
//        CoinPayerWithdrawInfo coinPayerWithdrawInfoOne = iCoinPayerWithdrawInfoService.getOne(new QueryWrapper<CoinPayerWithdrawInfo>().lambda()
//                .eq(CoinPayerWithdrawInfo::getWithdrawNo, withdrawNo));
//
//        if (coinPayerWithdrawInfoOne == null) {
//            throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "订单[" + withdrawNo + "]不存在");
//        }
//
////        if (merchantInfo.getAccountType().equals(EnumAccountType.MERCHANT.getName()) && !cardPayerWithdrawInfoOne.getPayerMerchantId().equals(merchantId)) {
////            throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "卡主[" + merchantId + "]权限不足"); //卡主只可操作自己名下的订单
////        }
//
//        //订单[排队中]才可修正 + 查询的任务也不可设为失败
//        if (!coinPayerWithdrawInfoOne.getTaskType().equals(CodeConst.TaskTypeConst.TRANSFER_TO_CARD)) {
//            throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "非打款任务,不可人工操作");
//        }
//
//        //订单[排队中]才可修正 + 查询的任务也不可设为失败
//        if (!coinPayerWithdrawInfoOne.getStatus().equals(CodeConst.WithdrawStatusConst.WITHDRAW_UNKNOWN)) {
//            throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "订单状态非[排队中],不可再次处理");
//        }
//
//
//        CoinPayerInfo coinPayerInfo = iCoinPayerInfoService.getOne(new QueryWrapper<CoinPayerInfo>().lambda()
//                .eq(CoinPayerInfo::getPayerAddr, coinPayerWithdrawInfoOne.getPayerAddr()));
//
//        if (coinPayerInfo == null) {
//            throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "付款钱包不存在");
//        }
//        //持卡人原有金额
//        // BigDecimal amtBefore = payerInfo.getPayerAmt();
//
//        //@设置订单为处理中状态
//
//        String withdrawRemark = "处理中";
//        //1.设置为成功 修改订单状态
//        Date taskStartTime = new Date();
//        CoinPayerWithdrawInfo updCoinPayerWithdrawEntity = new CoinPayerWithdrawInfo();
//        updCoinPayerWithdrawEntity.setStatus(CodeConst.WithdrawStatusConst.WITHDRAW_ONGOING);
//        updCoinPayerWithdrawEntity.setRemark(withdrawRemark);
//        updCoinPayerWithdrawEntity.setTaskStartTime(taskStartTime);
//        boolean isUpd = iCoinPayerWithdrawInfoService.update(updCoinPayerWithdrawEntity, new QueryWrapper<CoinPayerWithdrawInfo>().lambda().in(CoinPayerWithdrawInfo::getWithdrawNo, withdrawNo));
//        if (!isUpd) {
//            throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "订单状态更新不成功");
//        }
//
//        //5.返回结果给前端
//        Map<String, Object> resultMap = new HashMap<String, Object>();
//        resultMap.put("withdrawNo", withdrawNo);
//        resultMap.put("status", CodeConst.WithdrawStatusConst.WITHDRAW_ONGOING);
//        resultMap.put("taskStartTime", taskStartTime);
//        return new ReturnMsg(resultMap);
        return new ReturnMsg();
    }

    /**
     * 商户代付自助充值
     * TODO:升级为自行监控入款
     *
     * @param
     * @return
     * @throws
     */
    @RequestMapping(value = "/merchantSelfRecharge", method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    public ReturnMsg merchantSelfRecharge(
            String coinType,
            @NotBlank(message = "商户ID不能为空") String merchantId,
            @NotBlank(message = "充值金额不能为空") String amtUsdt,
            @NotBlank(message = "充值钱包不能为空") String payerAddr,
            String remark) throws TranException {

//        if (!"0".equals(coinType)) {
//            throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "只支持TRX公链协议代付充值");
//        }
//
//        //查询管理员
//        PayMerchantInfo merchantInfo = iPayMerchantInfoService.getOne(new QueryWrapper<PayMerchantInfo>().lambda().eq(PayMerchantInfo::getMerchantId, merchantId.trim()));
//
//        if (merchantInfo == null) {
//            throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "商户ID不存在");
//        }
//
//        // usdt 金额守护
//        if (!BigDecimalUtils.isUsdtNumeric(amtUsdt)) {
//            throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "非正确的USDT金额格式:" + amtUsdt);
//        }
//        if (BigDecimalUtils.lessThan(new BigDecimal(amtUsdt), new BigDecimal("1.00"))) {
//            throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "单笔订单金额不能小于" + new BigDecimal("10") + "元");
//        }
//
//        CoinPayerInfo coinPayerInfo = iCoinPayerInfoService.getOne(new QueryWrapper<CoinPayerInfo>().lambda()
//                .eq(CoinPayerInfo::getPayerAddr, payerAddr));
//
//        if (coinPayerInfo == null) {
//            throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "代付钱包[" + coinPayerInfo.getPayerAddr() + "]不存在");
//        }
//        if (!coinPayerInfo.getStatus().equals(CodeConst.PayerCardStatusConst.CARD_STATUS_BELONG)) {
//            throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "代付钱包[" + coinPayerInfo.getPayerAddr() + "]已下架");
//        }
//
//        //优惠立减 0.55-0.99之间的随机数
//        BigDecimal amtBonus = new BigDecimal(Math.random() / 10).setScale(4, BigDecimal.ROUND_HALF_UP);
//        if (BigDecimalUtils.lessEqual(amtBonus, new BigDecimal(0.050))) {
//            amtBonus = amtBonus.add(new BigDecimal(0.048)).setScale(4, BigDecimal.ROUND_HALF_UP);
//        }
//
//        //BigDecimal usdtLive = new BigDecimal(6.48); //USDT实时汇率
//        BigDecimal  amtUsdtFormat;
//        amtUsdtFormat = new BigDecimal(amtUsdt).setScale(3,BigDecimal.ROUND_HALF_UP);
//        BigDecimal amt = amtUsdtFormat.subtract(amtBonus).setScale(3, BigDecimal.ROUND_HALF_UP);
//
//
//        String redis_payerAddr_amt = payerAddr + MyRedisConst.SPLICE + amt;
//        if (redisUtil.get(redis_payerAddr_amt) != null) {
//            //优惠立减 重新计算
//            amtBonus = amtBonus.subtract(new BigDecimal(Math.random() / 100).setScale(3,BigDecimal.ROUND_HALF_UP)); //改为3位数
//            //重新计算入账金额
//            amt = new BigDecimal(amtUsdt).subtract(amtBonus);
//            redis_payerAddr_amt = payerAddr + MyRedisConst.SPLICE + amt;
//        };
//        int timeTTL = "0".equals(coinType) ? 60 * 10 + 20 : 60 * 20 + 20;  //10*60; 订单 trx 10分钟失效 | eth 20分钟
//        redisUtil.set(redis_payerAddr_amt, redis_payerAddr_amt, timeTTL);
//
//        PayMerchantSelfRecharge selfRechargeInfo = iPayMerchantSelfRechargeService.getOne(new QueryWrapper<PayMerchantSelfRecharge>().lambda()
//                .eq(PayMerchantSelfRecharge::getAmt, amt)
//                .eq(PayMerchantSelfRecharge::getPayerAddr,payerAddr)
//                .eq(PayMerchantSelfRecharge::getStatus, CodeConst.SelfRechargeStatus.PROCESS)
//        );
//        if (selfRechargeInfo != null) {
//            throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "同个钱包有未处理相同USDT金额的订单,请重新发起订单");
//        }
//
//
//        String withdrawNo = MySeq.getWithdrawNo();
//        PayMerchantSelfRecharge entity = new PayMerchantSelfRecharge();
//        entity.setWithdrawNo(withdrawNo);
//        entity.setAmtUsdt(amtUsdtFormat); //交易金额
//        entity.setAmtBonus(amtBonus); //优惠
//        entity.setAmt(amt);//入账金额
//        entity.setMerchantId(merchantId.trim());//哪个商家
//        //entity.setPayerMerchantId(coinPayerInfo.getPayerMerchantId()); 卡商
//        entity.setPayerAddr(payerAddr);
//        entity.setStatus(CodeConst.SelfRechargeStatus.PROCESS);
//        entity.setCreateTime(new Date());
//        entity.setRemark(HtmlUtil.delHTMLTag(remark));
//        boolean isSave = iPayMerchantSelfRechargeService.save(entity);
//        if (!isSave) {
//            throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "自助充值有误");
//        }
//
//        //5.返回结果给前端
//        Map<String, Object> resultMap = new HashMap<String, Object>();
//        resultMap.put("withdrawNo", withdrawNo);
//        resultMap.put("amtUsdt", amtUsdtFormat);
//        resultMap.put("amtBonus", amtBonus);
//        resultMap.put("amt", amt);
//        resultMap.put("payerAddr", payerAddr);
//        return new ReturnMsg(resultMap);
        return new ReturnMsg();

    }

    /**
     * 商户代收自助充值
     *
     * @param
     * @return
     * @throws
     */
    @RequestMapping(value = "/merchantHolderSelfRecharge", method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    public ReturnMsg merchantHolderSelfRecharge(
            @NotBlank(message = "google不能为空") String googleCode,
            @NotBlank(message = "商户ID不能为空") String merchantId,
            @NotBlank(message = "充值金额不能为空") String amt,
            @NotBlank(message = "充值钱包不能为空") String payerAddr,
            String remark) throws TranException {

        //商户的IDD
//        PayMerchantInfo merchantInfo = iPayMerchantInfoService.getOne(new QueryWrapper<PayMerchantInfo>().lambda().eq(PayMerchantInfo::getMerchantId, merchantId.trim()));
//
//        if (merchantInfo == null) {
//            throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "商户ID不存在");
//        }
//
//        if (!GoogleAuthenticator.check_code(merchantInfo.getGoogleKey(), googleCode, System.currentTimeMillis())) {
//            throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "验证码错误");
//        }
//
//        //收款钱包
//        CoinHolderInfo coinHolderInfo = iCoinHolderInfoService.getOne(new QueryWrapper<CoinHolderInfo>().lambda().eq(CoinHolderInfo::getPayerAddr, payerAddr));
//        if (coinHolderInfo == null) {
//            throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "钱包不存在");
//        }
//
//        if (!coinHolderInfo.getStatus().equals(CodeConst.PayerCardStatusConst.CARD_STATUS_BELONG)) {
//            throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "充值钱包[" + coinHolderInfo.getPayerAddr() + "]已下架");
//        }
//
//        //查询此钱包不是否属于这个商户
//        if (!coinHolderInfo.getChannelCode().equals(merchantInfo.getDepositChannelCode())){
//            throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "商户未分配此钱包,充值失败");
//        }
//
//        //只查询商户代收通道的配置
//        PayMerchantChannelSite currentChannelSite = iPayMerchantChannelSiteService.getOne(new QueryWrapper<PayMerchantChannelSite>()
//                .lambda()
//                .eq(PayMerchantChannelSite::getMerchantId, merchantId)
//                .eq(PayMerchantChannelSite::getPayType, CodeConst.PayType.RECEIVE_IN));
//
//        if (currentChannelSite == null) {
//            throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "商户未分配通道,充值失败");
//        }
//
//        String flowType = EnumFlowType.RECHARGE.getName(); //充值
//        Map<String, Object> paramsMerchant = new HashMap<String, Object>();
//        paramsMerchant.put("agentRate", "0"); //商家0
//        paramsMerchant.put("channelCode", currentChannelSite.getChannelCode()); //充值通道号
//        paramsMerchant.put("status", EnumMerchantStatusType.NORMAL.getName()); //自动上分只加到可用商户
//
//        PayMerchantInfo currentMerchantInfo = iPayMerchantInfoService.findOneMerchantByCardChannelCode(paramsMerchant); //商户ID
//
//        if (currentMerchantInfo == null) {
//            throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "商户未分配此钱包,充值失败");
//        }
//
//        String withdrawNo = MySeq.getWithdrawNo();
//        String channelWithdrawNo = "C" + DateUtil.format(new Date(), DateUtil.FULL_TIME_FORMAT_EN) + "0" + System.currentTimeMillis();
//        remark += "["+merchantInfo.getMerchantName()+"]手动补单[" + coinHolderInfo.getPayerName() +"]"+ coinHolderInfo.getPayerAddr();
//
//
//        //1.商家余额+流水表
//        BigDecimal merchantRateFee = new BigDecimal(amt).divide(new BigDecimal("100")).multiply(new BigDecimal(currentChannelSite.getMerchantRate())).setScale(4,BigDecimal.ROUND_HALF_UP);
//        BigDecimal merchantSingleFee = currentChannelSite.getSinglePoundage();//单笔手续费;
//
//        merchantAccountManagerService.merchantRecharge(new BigDecimal(amt), currentMerchantInfo.getMerchantId(), currentMerchantInfo.getMerchantName(), remark, currentMerchantInfo.getMerchantName(), currentMerchantInfo.getLabel(),currentChannelSite.getChannelCode(),withdrawNo,EnumFlowType.RECHARGE.getName());
//
//        //2.钱包更改余额+流水表
//        coinWithdrawManagerService.alterCoinHolderBalanceNAddFlowService(coinHolderInfo, new BigDecimal(0),new BigDecimal(amt), flowType, channelWithdrawNo,channelWithdrawNo,remark);
//
//        //3.代理分润+流水表
//        BigDecimal agentSingleProfit = new BigDecimal(0);  //初始代理单笔分成 为 0元
//        BigDecimal agentRateProfit = new BigDecimal(0);    //初始代理下发分成 为 0元
//
//        //递归取得代理的单笔总分成 withdrawNo,channelWithdrawNo,rechargeAmt,PayMerchantChannelSite
//        alterAgentSingleBalance(agentSingleProfit, currentMerchantInfo, withdrawNo,channelWithdrawNo,new BigDecimal(amt),currentChannelSite);
//
//        //递归下发单笔费率总分成
//        alterAgentRateBalance(agentRateProfit, currentMerchantInfo, withdrawNo,channelWithdrawNo,new BigDecimal(amt),currentChannelSite);
//
//
//        //4.记录代收商家手续费
//        coinChannelManagerService.alterCoinChannelBalanceService(
//                currentMerchantInfo.getMerchantId(),
//                merchantRateFee.add(merchantSingleFee).negate(),//商家手续费  == 补单要扣除商家的手续费
//                CodeConst.CardFlowTypeConst.RECHARGE, //补单充值
//                withdrawNo,//平台订单号
//                channelWithdrawNo,//三方通道号
//                currentChannelSite.getChannelCode(), //通道编号
//                merchantInfo.getMerchantId(),//此ID为管理员
//                merchantInfo.getDepositChannelName(), //收款钱包自助补单 充值
//                remark
//        );
//
//        //返回结果给前端
//        Map<String, Object> resultMap = new HashMap<String, Object>();
//        resultMap.put("withdrawNo", withdrawNo);
//        resultMap.put("save", "1");
//        return new ReturnMsg(resultMap);
        return new ReturnMsg();
    }


    /**
     * 获取卡主新订单
     *
     * @param
     * @return
     * @throws Exception
     * @WARNING:有任务开始时间的订单,超时5分钟出现修正状态 事务不能TRY CATCH 否则不会走事务
     */
    @RequestMapping(value = "/queryNewOrder", method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    public ReturnMsg queryNewOrder(
            @NotBlank(message = "ID不能为空") String tableMerchantId) throws TranException {
        //查询管理员
        PayMerchantInfo merchantInfo = iPayMerchantInfoService.getOne(new QueryWrapper<PayMerchantInfo>().lambda().eq(PayMerchantInfo::getMerchantId, tableMerchantId));
        if (merchantInfo == null) {
            throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "商户不存在");
        }

        CoinHolderDepositInfo cardPayerWithdrawInfoOne = iCoinHolderDepositInfoService.getOne(new QueryWrapper<CoinHolderDepositInfo>().lambda()
                .eq(CoinHolderDepositInfo::getStatus, CodeConst.WithdrawStatusConst.WITHDRAW_UNKNOWN)
                .eq(CoinHolderDepositInfo::getPayerMerchantId,tableMerchantId));
        String newOrder = cardPayerWithdrawInfoOne == null ? "0" : "1";
        //5.返回结果给前端
        Map<String, Object> resultMap = new HashMap<String, Object>();
        resultMap.put("newOrder", newOrder);
        return new ReturnMsg(resultMap);

    }

    /**
     * 强制成功
     *
     * @param withdrawNo
     * @return
     * @throws Exception
     * @WARNING:有任务开始时间的订单,超时5分钟出现修正状态
     */
    @RequestMapping(value = "/setWithdrawStatusForceSuccess", method = {RequestMethod.POST, RequestMethod.GET}, name = "admin")
    @ResponseBody
    @Transactional(rollbackFor = Exception.class)
    public ReturnMsg setWithdrawStatusForceSuccess(
            @NotBlank(message = "google不能为空") String googleCode,
            @NotBlank(message = "平台订单号不能为空") String withdrawNo,
            @NotBlank(message = "管理员ID不能为空") String tableMerchantId) throws TranException {
        try {

            //查询管理员
            PayMerchantInfo merchantInfo = iPayMerchantInfoService.getOne(new QueryWrapper<PayMerchantInfo>().lambda().eq(PayMerchantInfo::getMerchantId, tableMerchantId));

            String googleKeyDecode =  EncryptUtil.aesDecrypt(merchantInfo.getGoogleKey(),walletPrivateKeySalt);
            if(!GoogleAuthenticator.check_code(googleKeyDecode, googleCode, System.currentTimeMillis())){
                throw new TranException(CodeConst.BUSINESS_ERROR_CODE,"google验证码错误");
            }

            CoinPayerWithdrawInfo coinPayerWithdrawInfo = iCoinPayerWithdrawInfoService.getOne(new QueryWrapper<CoinPayerWithdrawInfo>().lambda()
                    .eq(CoinPayerWithdrawInfo::getWithdrawNo, withdrawNo));

            if (coinPayerWithdrawInfo == null) {
                throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "订单[" + withdrawNo + "]不存在");
            }

            //商户+代理
            if (merchantInfo.getAccountType().equals(EnumAccountType.MERCHANT.getName())) {
                //如果是商户 检查订单是否是该商户提交的订单
                if (!"0".equals(merchantInfo.getAgentRate())){
                    throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "代理无操作权限");
                }
                if (!coinPayerWithdrawInfo.getChannelCode().equals(merchantInfo.getChannelCode())){
                    throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "商户无操作权限");
                }
            }

            //强制成功：订单[未知]才可修正
            if (!coinPayerWithdrawInfo.getStatus().equals(CodeConst.WithdrawStatusConst.WITHDRAW_NO_CLEAR)) {
                throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "订单状态非[未知],不可修改订单状态");
            }

            CoinPayerInfo payerInfo = iCoinPayerInfoService.getOne(new QueryWrapper<CoinPayerInfo>().lambda()
                    .eq(CoinPayerInfo::getPayerAddr, coinPayerWithdrawInfo.getPayerAddr()));

            if (payerInfo == null) {
                throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "钱包不存在");
            }
            //持卡人原有金额
            BigDecimal amtBefore = payerInfo.getAmt();

            //@设置订单为成功状态

            String withdrawRemark = "[" + merchantInfo.getMerchantName() + "]强制设为成功"; //更新订单状态
            //1.设置为成功 修改订单状态
            CoinPayerWithdrawInfo updateCoinPayerWithdrawEntity = new CoinPayerWithdrawInfo();
            updateCoinPayerWithdrawEntity.setStatus(CodeConst.WithdrawStatusConst.WITHDRAW_SUCCESS);
            updateCoinPayerWithdrawEntity.setRemark(withdrawRemark);
            updateCoinPayerWithdrawEntity.setTaskEndTime(new Date());
            boolean isUpdate = iCoinPayerWithdrawInfoService.update(updateCoinPayerWithdrawEntity, new QueryWrapper<CoinPayerWithdrawInfo>().lambda().in(CoinPayerWithdrawInfo::getWithdrawNo, withdrawNo));
            if (!isUpdate) {
                throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "订单状态更新不成功");
            }

            //5.返回结果给前端
            Map<String, Object> resultMap = new HashMap<String, Object>();
            resultMap.put("withdrawNo", withdrawNo);
            resultMap.put("amt", coinPayerWithdrawInfo.getAmt());
            resultMap.put("payerFee", coinPayerWithdrawInfo.getPayerRateFee());
            return new ReturnMsg(resultMap);

        } catch (Exception e) {
            return new ReturnMsg().setFail(e.getMessage());
        }
    }

    /**
     * 订单修正
     *
     * @param withdrawNo
     * @return
     * @throws Exception
     * @WARNING:有任务开始时间的订单,超时5分钟出现修正状态
     */
    @RequestMapping(value = "/setWithdrawStatusFix", method = {RequestMethod.POST, RequestMethod.GET}, name = "admin")
    @ResponseBody
    @Transactional(rollbackFor = Exception.class)
    public ReturnMsg setWithdrawStatusFix(
            @NotBlank(message = "平台订单号不能为空") String withdrawNo,
            @NotBlank(message = "管理员ID不能为空") String tableMerchantId) throws TranException {
        _log.info("订单自动修正功能暂时关闭");
        return new ReturnMsg();
//        try {
//            //查询管理员
//            PayMerchantInfo merchantInfo = iPayMerchantInfoService.getOne(new QueryWrapper<PayMerchantInfo>().lambda().eq(PayMerchantInfo::getMerchantId, tableMerchantId));
//
//            CoinPayerWithdrawInfo coinPayerWithdrawInfoOne = iCoinPayerWithdrawInfoService.getOne(new QueryWrapper<CoinPayerWithdrawInfo>().lambda()
//                    .eq(CoinPayerWithdrawInfo::getWithdrawNo, withdrawNo));
//
//            if (coinPayerWithdrawInfoOne == null) {
//                throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "订单不存在");
//            }
//
//            //订单状态非[未知],不可修正
//            if (!coinPayerWithdrawInfoOne.getStatus().equals(CodeConst.WithdrawStatusConst.WITHDRAW_NO_CLEAR)) {
//                throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "订单状态非[未知],不可修正");
//            }
//
//            //新增不同日期不可修正，因为建行APP只可查询当天的记录 DateUtil.getDate()
//            if (!coinPayerWithdrawInfoOne.getDate().equals(DateUtil.getDate())) {
//                throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "订单日期与修正日期[不同],不可修正");
//            }
//
//            CoinPayerInfo payerInfo = iCoinPayerInfoService.getOne(new QueryWrapper<CoinPayerInfo>().lambda()
//                    .eq(CoinPayerInfo::getPayerAddr, coinPayerWithdrawInfoOne.getPayerAddr()));
//
//            if (payerInfo == null) {
//                throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "钱包不存在");
//            }
//
//            //TODO:查询HASH ID
//            //网络当中的HASH ID的状态是否成功 直接返回结果 而不用调用钱包APP进行查询
//
//            //@设置订单为修正状态
//            // 订单从[打款任务]变更为[查询任务] == 查询一次可能断网 ,需要再次查询
//            // 但二次查询需等待订单状态再次变更为 "未知"
//            //
//            String withdrawRemark = "等待修正"; //更新订单状态
//            CoinPayerWithdrawInfo updCoinPayerWithdrawEntity = new CoinPayerWithdrawInfo();
//            updCoinPayerWithdrawEntity.setStatus(CodeConst.WithdrawStatusConst.WITHDRAW_UNKNOWN); //排队中 等待前端APP修正
//            updCoinPayerWithdrawEntity.setTaskType(CodeConst.TaskTypeConst.TRANSFER_QUERY); //改变订单的状态 (不再打款)
//            //updCardPayerWithdrawEntity.setTaskStartTime(new Date()); //订单修正开始时间 | 修正时间应由前端app调用
//            //updCardPayerWithdrawEntity.setTaskQueryStartTime(new Date()); //每次修正（重新修正）时的时间 修正时间应由前端app调用
//            updCoinPayerWithdrawEntity.setRemark(withdrawRemark);
//            boolean isUpdate = iCoinPayerWithdrawInfoService.update(updCoinPayerWithdrawEntity, new QueryWrapper<CoinPayerWithdrawInfo>().lambda().in(CoinPayerWithdrawInfo::getWithdrawNo, withdrawNo));
//
//            if (!isUpdate) {
//                throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "订单状态更新不成功");
//            }
//
//            //5.返回结果
//            Map<String, Object> resultMap = new HashMap<String, Object>();
//            resultMap.put("withdrawNo", withdrawNo);
//            resultMap.put("taskType", CodeConst.TaskTypeConst.TRANSFER_QUERY);
//            return new ReturnMsg(resultMap);
//
//        } catch (Exception e) {
//            return new ReturnMsg().setFail(e.getMessage());
//        }
    }


    /**
     * 订单暂停/恢复
     *
     * @param withdrawNo
     * @return
     * @throws Exception
     * @WARNING:有任务开始时间的订单,超时5分钟出现修正状态
     */
    @RequestMapping(value = "/setWithdrawStatusPause", method = {RequestMethod.POST, RequestMethod.GET}, name = "admin")
    @ResponseBody
    @Transactional(rollbackFor = Exception.class)
    public ReturnMsg setWithdrawStatusPause(
            @NotBlank(message = "平台订单号不能为空") String withdrawNo,
            @NotBlank(message = "google不能为空") String googleCode,
            @NotBlank(message = "管理员ID不能为空") String tableMerchantId
    ) throws TranException {

        try {
            //查询管理员
            PayMerchantInfo merchantInfo = iPayMerchantInfoService.getOne(new QueryWrapper<PayMerchantInfo>().lambda().eq(PayMerchantInfo::getMerchantId, tableMerchantId));
            String googleKeyDecode =  EncryptUtil.aesDecrypt(merchantInfo.getGoogleKey(),walletPrivateKeySalt);
            if(!GoogleAuthenticator.check_code(googleKeyDecode, googleCode, System.currentTimeMillis())){
                throw new TranException(CodeConst.BUSINESS_ERROR_CODE,"google验证码错误");
            }

            CoinPayerWithdrawInfo coinPayerWithdrawInfoOne = iCoinPayerWithdrawInfoService.getOne(new QueryWrapper<CoinPayerWithdrawInfo>().lambda()
                    .eq(CoinPayerWithdrawInfo::getWithdrawNo, withdrawNo));

            if (coinPayerWithdrawInfoOne == null) {
                throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "订单不存在");
            }

            CoinPayerInfo payerInfo = iCoinPayerInfoService.getOne(new QueryWrapper<CoinPayerInfo>().lambda()
                    .eq(CoinPayerInfo::getPayerAddr, coinPayerWithdrawInfoOne.getPayerAddr()));

            if (payerInfo == null) {
                throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "钱包不存在");
            }

            //订单状态非[排队中],暂停/恢复
            if (!coinPayerWithdrawInfoOne.getStatus().equals(CodeConst.WithdrawStatusConst.WITHDRAW_UNKNOWN)) {
                throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "订单状态非[排队中],暂停/恢复");
            }

            //持卡人原有金额
            BigDecimal amtBefore = payerInfo.getAmt();

            //@设置订单为修正状态
            // 订单从[打款任务]变更为[查询任务] == 查询一次可能断网 ,需要再次查询
            // 但二次查询需等待订单状态再次变更为 "未知"
            //

            String withdrawPause = coinPayerWithdrawInfoOne.getWithdrawPause().equals(CodeConst.WithdrawPauseConst.WITHDRAW_PAUSE_Y) ? CodeConst.WithdrawPauseConst.WITHDRAW_PAUSE_N : CodeConst.WithdrawPauseConst.WITHDRAW_PAUSE_Y;
            String txt = coinPayerWithdrawInfoOne.getWithdrawPause().equals(CodeConst.WithdrawPauseConst.WITHDRAW_PAUSE_Y) ? "已审核" : "未审";
            String remark = coinPayerWithdrawInfoOne.getWithdrawPause().equals(CodeConst.WithdrawPauseConst.WITHDRAW_PAUSE_Y) ? "已审核" : "未审";

            CoinPayerWithdrawInfo updCoinPayerWithdrawEntity = new CoinPayerWithdrawInfo();
            updCoinPayerWithdrawEntity.setWithdrawPause(withdrawPause);
            updCoinPayerWithdrawEntity.setRemark(remark);
            boolean isUpdate = iCoinPayerWithdrawInfoService.update(updCoinPayerWithdrawEntity,
                    new QueryWrapper<CoinPayerWithdrawInfo>().lambda().in(CoinPayerWithdrawInfo::getWithdrawNo, withdrawNo));

            if (!isUpdate) {
                throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "订单[" + txt + "]失败");
            }

            //5.返回结果
            Map<String, Object> resultMap = new HashMap<String, Object>();
            resultMap.put("withdrawNo", withdrawNo);
            return new ReturnMsg(resultMap);

        } catch (Exception e) {
            return new ReturnMsg().setFail(e.getMessage());
        }
    }

    /**
     * 查询上游三方订单
     */
    @RequestMapping(value = "/queryWithdraw", method = {RequestMethod.POST})
    @ResponseBody
    @Transactional(rollbackFor = Exception.class)
    public ReturnMsg queryWithdraw(
            @NotNull(message = "通道编号不能为空") String channelCode,
            @NotNull(message = "订单号不能为空") String withdrawNo,
            @NotNull(message = "签名不能为空") String sign,
            @NotNull(message = "aesKey不能为空") String aesKey) throws TranException {

        try {

            if (channelCode == null || "".equals(channelCode)) {
                throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "通道号为空");
            }

            if (withdrawNo == null || "".equals(withdrawNo)) {
                throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "订单号为空");
            }

            if (sign == null || "".equals(sign)) {
                throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "签名为空");
            }

            if (aesKey == null || "".equals(aesKey)) {
                throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "aeSKey为空");
            }


            //1.通道是否存在
            CoinChannelWithdrawInfo channelInfo = iCoinChannelWithdrawInfoService.getOne(
                    new QueryWrapper<CoinChannelWithdrawInfo>().lambda().eq(CoinChannelWithdrawInfo::getChannelCode, channelCode));
            if (channelInfo == null) {
                throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "三方通道不存在");
            }

            String apiKeyDecrypt = EncryptUtil.aesDecrypt(channelInfo.getApiKey(),walletPrivateKeySalt);

            //4.验证签名 + aesKey
            Map<String, Object> params = new HashMap<String, Object>();
            params.put("channelCode", channelCode.trim());//商户号
            params.put("withdrawNo", withdrawNo.trim());//四方平台提交的订单号 W1577241046308040251

            String signCreator = Signature.getSign(params, apiKeyDecrypt);
            params.put("sign", signCreator);

            String aesKeyCreator = EncryptUtil.aesEncrypt(Signature.getSign(params, apiKeyDecrypt), privateAESKey);

            if (!signCreator.equals(sign)) {
                throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "签名不正确");
            }
            if (!aesKeyCreator.equals(aesKey)) {
                throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "aesKey不正确");
            }

            CoinPayerWithdrawInfo coinPayerWithdrawInfo = iCoinPayerWithdrawInfoService.getOne(new QueryWrapper<CoinPayerWithdrawInfo>().lambda()
                    .eq(CoinPayerWithdrawInfo::getWithdrawNo, withdrawNo));

            if (coinPayerWithdrawInfo == null) {
                throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "三方订单号不存在");
            }

            //MAP结果返回:订单状态给下游四方平台
            Map<String, Object> resultMap = new HashMap<String, Object>();
            resultMap.put("channelCode", channelCode); //通道号
            resultMap.put("withdrawNo", withdrawNo); //唯一订单号
            resultMap.put("channelWithdrawNo", coinPayerWithdrawInfo.getChannelWithdrawNo()); //银行卡订单号
            resultMap.put("amt", coinPayerWithdrawInfo.getAmt()); //交易金额 元
            resultMap.put("status", coinPayerWithdrawInfo.getStatus());
            resultMap.put("payerName", coinPayerWithdrawInfo.getPayerName()); //付款人
            resultMap.put("payerAddr", coinPayerWithdrawInfo.getPayerAddr()); //付款者卡号
            resultMap.put("acctName", coinPayerWithdrawInfo.getAcctName());
            resultMap.put("acctAddr", coinPayerWithdrawInfo.getAcctAddr());
            resultMap.put("remark", coinPayerWithdrawInfo.getRemark());

            String respSign = Signature.getSign(resultMap, apiKeyDecrypt);

            resultMap.put("sign", respSign);
            resultMap.put("aesKey", EncryptUtil.aesEncrypt(respSign, privateAESKey));
            return new ReturnMsg(resultMap);

        } catch (Exception e) {
            // e.printStackTrace();
            // _log.error("=====>>>>>三方平台订单查询异常,{}", e.getMessage());
            Map<String, Object> resultMap = new HashMap<String, Object>();
            resultMap.put("withdrawNo", withdrawNo); //唯一订单号
            resultMap.put("remark", e.getMessage());
            resultMap.put("sign", Signature.getSign(resultMap, privateAESKey));  //通道会出错 取 privateAESKey
            throw new TranException(CodeConst.BUSINESS_ERROR_CODE, JSON.toJSONString(resultMap));
        }
    }


    /**
     * 获取三方订单成功订单
     *
     * @param
     * @param - rows                  条数
     * @param - page                  页码
     * @return
     * admin 权限
     */
    @RequestMapping(value = "/querySuccessOrder", method = {RequestMethod.POST, RequestMethod.GET}, name = "admin")
    @ResponseBody
    public ReturnMsg querySuccessOrder(
            String withdrawStatus,// no need to use the param
            String payType,
            String beginTime,
            String endTime,
            String remark,
            String tableMerchantId
    ) throws TranException {

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("payType",payType); //0-付 1-收
        params.put("status", CodeConst.WithdrawStatusConst.WITHDRAW_SUCCESS);
        params.put("beginTime", beginTime); //每月1号 00:00:00
        params.put("endTime", endTime); //当月当前时间
        //1.统计成功交易金额
        String accountBalance = "";
        BigDecimal amtTotal = new BigDecimal(0);
        long withdrawTotal =0L;

        if ("0".equals(payType)){//代付
            amtTotal      = iCoinPayerWithdrawInfoService.sumCoinPayerWithdrawInfo(params); //总交易成功金额
            withdrawTotal = (long)iCoinPayerWithdrawInfoService.countCoinPayerWithdraw(params); //成功交易笔数

        }else if ("1".equals(payType)){
            Map<String,Object> sumMap = iCoinHolderDepositInfoService.sumCoinHolderDepositInfo(params);
            if (sumMap != null){
                amtTotal = new BigDecimal(sumMap.get("sumCoinPayerWithdraw").toString());
                withdrawTotal = (long) sumMap.get("countCoinPayerWithdraw");
            }
            //加上手动充值部分 只能读取商家的 代理的等级不可以读取 也不可以读取负数 否则新增的总金额会变少
            params.put("flowType",EnumFlowType.RECHARGE.getName()); //0-充值
            params.put("agentRate", EnumAgentRate.MERCHANT.getName());//0-商家
            params.put("amt", 0);//0-商家
            Map<String,Object> rechargeMap = iPayMerchantMoneyChangeService.selectCountSumMerchantMoneyChange(params);
            if (rechargeMap != null) {
                BigDecimal rechargeAmtTotal = (BigDecimal) rechargeMap.get("sumAmt");
                amtTotal = amtTotal.add(rechargeAmtTotal);
                withdrawTotal += (long)rechargeMap.get("countWithdrawTotal");
            }

        }else {
            return new ReturnMsg();
        }

        //2.发送时间+当前时间的跑量到远端服务器
        Map<String, Object> reqMap = new HashMap<>();
        reqMap.put("beginTime", beginTime);
        reqMap.put("endTime", endTime);
        reqMap.put("payType",payType); //0-付 1-收
        reqMap.put("withdrawTotal",withdrawTotal);//成功交易笔数
        reqMap.put("amtTotal", amtTotal); //总交易成功金额
        reqMap.put("merchantId", platformId);
        reqMap.put("withdrawNo", MySeq.getWithdrawNo());//订单号
        reqMap.put("sign", Signature.getSign(reqMap, platformKey));

        //3.获得手续费余额
        String serverResult = HttpClient4Utils.sendHttpRequest(platformBalanceUrl, reqMap, "utf-8", true);
        _log.info("服务器返回手续费=====>>>>>{}", serverResult);

        JSONObject json = JSONObject.parseObject(serverResult);
        if ("0000".equals(json.getString("code"))) {
            //验签
            JSONObject dataJsonObj = json.getJSONObject("data");

            Map<String, Object> respMap = new HashMap<String, Object>();
            respMap.put("merchantId", dataJsonObj.getString("merchantId"));
            respMap.put("accountBalance", dataJsonObj.getString("accountBalance"));

            String signCreator = Signature.getSign(respMap, platformKey);
            if (signCreator.equals(dataJsonObj.getString("sign"))) {
                accountBalance = dataJsonObj.getString("accountBalance");
            }
        }

        Map<String, Object> resultMap = new HashMap<String, Object>();
        resultMap.put("accountBalance", accountBalance);
        resultMap.put("amtTotal",amtTotal); //代付总交易量


        return new ReturnMsg(resultMap);
    }


    //退回商家余额
    private Integer getReturnLatestMoneyRecord(PayWithdrawInfo withdrawInfo, BigDecimal refundAmt) {

        //流水表 可避免二次 退款
        PayMerchantMoneyChange moneyChangeRecord = iPayMerchantMoneyChangeService.getOne(new QueryWrapper<PayMerchantMoneyChange>().lambda()
                .eq(PayMerchantMoneyChange::getWithdrawNo, withdrawInfo.getWithdrawNo())
                .eq(PayMerchantMoneyChange::getMerchantId, withdrawInfo.getMerchantId())
                .eq(PayMerchantMoneyChange::getFlowType, EnumFlowType.RECEIVE.getName())
        );

        if (moneyChangeRecord != null) {
            _log.info("=====>>>>>refundQueue商家:{}退费:{}失败,原因:流水已记录,不可重复修正订单", withdrawInfo.getMerchantId(), refundAmt);
            return 0;
        }

        //查询商户最新的MONEY_CHANGE 下发流水记录 不锁表了 流水表记录过多 直接用 insertLatestMoneyChangeBySelect
//        PayMerchantMoneyChange moneyChangeParams = new PayMerchantMoneyChange();
//        moneyChangeParams.setMerchantId(withdrawInfo.getMerchantId()); //某一个商家
//        moneyChangeParams.setAmt(refundAmt); // 冲正 商户款项增加
//
//        //1.插入流水表
//        Integer latestRecordId = iPayMerchantMoneyChangeService.insertLatestMoneyChangeBySelect(moneyChangeParams);
//        if (latestRecordId != 1) {
//            return 0;
//        }
//
//        _log.warn("退款插入流水表是获取插入的用户id:{}", latestRecordId);
//        //2.获取插入的用户
//        PayMerchantMoneyChange latestRecordPayMerchant = iPayMerchantMoneyChangeService.getById(moneyChangeParams.getId());

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("merchantId", withdrawInfo.getMerchantId());
        PayMerchantMoneyChange latestRecord = iPayMerchantMoneyChangeService.selectLatestMoneyChangeForUpdate(params);

        //更新时间
        _log.info("退款流水记录===>>>商家:{},账变前:{},账变后:{}", withdrawInfo.getMerchantId(), latestRecord.getAmtNow(), latestRecord.getAmtNow().add(refundAmt));

        //3.记录MONEY_CHANGE 退款流水
        PayMerchantMoneyChange moneyChangeEntity = new PayMerchantMoneyChange();
        moneyChangeEntity.setFlowType(EnumFlowType.RECEIVE.getName()); //冲正 退回款项
        moneyChangeEntity.setMerchantWithdrawNo(withdrawInfo.getMerchantWithdrawNo());
        moneyChangeEntity.setWithdrawNo(withdrawInfo.getWithdrawNo());
        moneyChangeEntity.setMerchantId(withdrawInfo.getMerchantId());
        moneyChangeEntity.setAmt(refundAmt); //资金变动
        moneyChangeEntity.setCreditAmt(refundAmt); //资金变动
        moneyChangeEntity.setAmtBefore(latestRecord.getAmtNow());
        moneyChangeEntity.setAmtNow(latestRecord.getAmtNow().add(refundAmt)); // 退款是新增

        //moneyChangeEntity.setCreateTime(new Date());
        moneyChangeEntity.setRemark("退回商家[" + withdrawInfo.getMerchantId() + "][" + refundAmt + "]");
        moneyChangeEntity.setChannelName(withdrawInfo.getChannelCode());
        moneyChangeEntity.setChannelCode(withdrawInfo.getChannelCode());
        boolean isSaveMoneyChange = iPayMerchantMoneyChangeService.save(moneyChangeEntity);
        //boolean isUpdateMoneyChange = iPayMerchantMoneyChangeService.update(moneyChangeEntity, new QueryWrapper<PayMerchantMoneyChange>().lambda().in(PayMerchantMoneyChange::getId, moneyChangeParams.getId()));

        if (!isSaveMoneyChange) {
            _log.info("=====>>>>>refundQueuey记入商家:{}退费流水表:{}失败", withdrawInfo.getMerchantId(), refundAmt);
            return 0;
        } else {
            return 1;
        }
    }


    //扣除修改代理商的余额
    //扣除记录代理商的流水
    //商户 1元 代理 0.7 总代 0.6 == 单笔
    //channelCode
    @SuppressWarnings({"deprecation"})
    private BigDecimal alterAgentSingleBalance(BigDecimal agentsProfit, PayMerchantInfo curMerchantInfo, PayWithdrawInfo withdrawInfo) throws TranException {

        //当前商户汇率
        PayAgentChannelSite curMerchantChannelSite = iPayAgentChannelSiteService.getOne(
                new QueryWrapper<PayAgentChannelSite>().lambda()
                        .eq(PayAgentChannelSite::getMerchantId, curMerchantInfo.getMerchantId())
                        .eq(PayAgentChannelSite::getChannelCode, curMerchantInfo.getChannelCode())
                .eq(PayAgentChannelSite::getPayType,CodeConst.PayType.SEND_OUT)
        );
        //上级商户
        PayMerchantInfo parentMerchantInfo = iPayMerchantInfoService.getOne(new QueryWrapper<PayMerchantInfo>().lambda().eq(PayMerchantInfo::getMerchantId, curMerchantInfo.getParentId()));
        //找到上级代理
        if (parentMerchantInfo != null) {

            //上级代理通道信息
            PayAgentChannelSite parentChannelSite = iPayAgentChannelSiteService.getOne(new QueryWrapper<PayAgentChannelSite>().lambda()
                    .eq(PayAgentChannelSite::getMerchantId, parentMerchantInfo.getMerchantId())
                    .eq(PayAgentChannelSite::getPayType,CodeConst.PayType.SEND_OUT)
            );
            //代理手续费 = 商户成本 - 代理成本
            _log.info("商户单笔下发手续费:{},代理成本:{}", curMerchantChannelSite.getSinglePoundage(), parentChannelSite.getSinglePoundage());
            BigDecimal parentPayProfit = curMerchantChannelSite.getSinglePoundage().subtract(parentChannelSite.getSinglePoundage());

            Integer parentIndex = balanceService.alterMerchantBalance(parentMerchantInfo.getMerchantId(), parentPayProfit.negate(), null);
            if (parentIndex != 1) throw new TranException(CodeConst.SYSTEM_ERROR_CODE, "分配代理商利润失败");
            _log.info("===>>>代理:{},单笔分成:{}", parentMerchantInfo.getMerchantId(), parentPayProfit);

            //继续查询是否有代理的上级代理 即总代
            /*BigDecimal getAmtBefore = new BigDecimal(0);
            BigDecimal getAmtNow = new BigDecimal(0);

            Map<String,Object> params = new HashMap<String,Object>();
            params.put("merchantId", parentMerchantInfo.getMerchantId()); //某一个代理 | WARNING:高并发时不准确
            PayMerchantMoneyChange latestRecord =  iPayMerchantMoneyChangeService.selectLatestMoneyChangeForUpdate(params);

            if (latestRecord != null ) {
                getAmtBefore = latestRecord.getAmtNow();
                getAmtNow = latestRecord.getAmtNow();
            }*/
            //FIXME:充值bug修复 若money_change变中没有上级代理,则查找上级代理的余额为 null,会导致 插入代理商分润流水失败

            Map<String, Object> params = new HashMap<String, Object>();
            params.put("merchantId", parentMerchantInfo.getMerchantId()); //某一个代理 | WARNING:高并发时不准确
            PayMerchantMoneyChange latestRecord = iPayMerchantMoneyChangeService.selectLatestMoneyChangeForUpdate(params);
            //没有流水 要新插入
            if (latestRecord == null) {

                PayMerchantMoneyChange addMoneyChangeEntity = new PayMerchantMoneyChange();

                addMoneyChangeEntity.setMerchantId(parentMerchantInfo.getMerchantId()); //某一个代理
                addMoneyChangeEntity.setFlowType(EnumFlowType.RECEIVE.getName()); //代付类型
                addMoneyChangeEntity.setMerchantWithdrawNo(withdrawInfo.getMerchantWithdrawNo());
                addMoneyChangeEntity.setWithdrawNo(withdrawInfo.getWithdrawNo());
                addMoneyChangeEntity.setAmt(parentPayProfit); // 提交代付金额
                addMoneyChangeEntity.setCreditAmt(parentPayProfit); //实际入账金额
                addMoneyChangeEntity.setAmtBefore(new BigDecimal(0));
                addMoneyChangeEntity.setAmtNow(parentPayProfit);

                //addMoneyChangeEntity.setCreateTime(new Date());
                addMoneyChangeEntity.setRemark("扣除代理[" + parentMerchantInfo.getMerchantId() + "]单笔分成");
                addMoneyChangeEntity.setChannelName(parentMerchantInfo.getChannelName());
                addMoneyChangeEntity.setChannelCode(parentChannelSite.getChannelCode());

                boolean isAddMoneyChangeEntity = iPayMerchantMoneyChangeService.save(addMoneyChangeEntity);

                if (!isAddMoneyChangeEntity) {
                    throw new TranException(CodeConst.SYSTEM_ERROR_CODE, "代付:插入代理商分润流水失败");
                }
            } else {

//                PayMerchantMoneyChange moneyLatestChangeEntity = new PayMerchantMoneyChange();
//                moneyLatestChangeEntity.setMerchantId(parentMerchantInfo.getMerchantId()); //某一个商家
//                moneyLatestChangeEntity.setAmt(parentPayProfit.negate()); // 扣除 代理款项

                //1.插入流水表
//                Integer latestRecordId = iPayMerchantMoneyChangeService.insertLatestMoneyChangeBySelect(moneyLatestChangeEntity);
//                if (latestRecordId != 1) {
//                    throw new TranException(CodeConst.SYSTEM_ERROR_CODE, "充值失败:插入代理商分润流水失败");
//                }

                //2.获取插入的用户
                // PayMerchantMoneyChange latestRecordPayMerchant = iPayMerchantMoneyChangeService.getById(moneyLatestChangeEntity.getId());
                //更新时间
                _log.info("代理充值流水记录===>>>商家:{},账变前:{},账变后:{}", parentMerchantInfo.getMerchantId(), latestRecord.getAmtNow(), latestRecord.getAmtNow().subtract(parentPayProfit));

                //3.记录MONEY_CHANGE 退款流水
                PayMerchantMoneyChange moneyChangeEntity = new PayMerchantMoneyChange();
                moneyChangeEntity.setMerchantId(parentMerchantInfo.getMerchantId());
                moneyChangeEntity.setFlowType(EnumFlowType.RECEIVE.getName()); //充值状态
                moneyChangeEntity.setMerchantWithdrawNo(withdrawInfo.getMerchantWithdrawNo());
                moneyChangeEntity.setWithdrawNo(withdrawInfo.getWithdrawNo());
                moneyChangeEntity.setAmt(parentPayProfit); //资金变动
                moneyChangeEntity.setCreditAmt(parentPayProfit); //实际入账金额
                moneyChangeEntity.setAmtBefore(latestRecord.getAmtNow());
                moneyChangeEntity.setAmtNow(latestRecord.getAmtNow().subtract(parentPayProfit));
                //moneyChangeEntity.setCreateTime(new Date());
                moneyChangeEntity.setRemark("扣除代理[" + parentMerchantInfo.getMerchantId() + "]单笔分成[" + parentPayProfit + "]");
                moneyChangeEntity.setChannelName(withdrawInfo.getChannelCode());
                moneyChangeEntity.setChannelCode(withdrawInfo.getChannelCode());
                boolean isSaveMoneyChange = iPayMerchantMoneyChangeService.save(moneyChangeEntity);
                // boolean isUpdateMoneyChange = iPayMerchantMoneyChangeService.update(moneyChangeEntity, new QueryWrapper<PayMerchantMoneyChange>().lambda().in(PayMerchantMoneyChange::getId, moneyLatestChangeEntity.getId()));
                if (!isSaveMoneyChange) {
                    _log.info("=====>>>>>扣除代理商下发/代付余额变动明细表:{},代付流水表:{}失败", parentMerchantInfo.getMerchantId(), parentPayProfit);
                    throw new TranException(CodeConst.SYSTEM_ERROR_CODE, "扣除代付分成给代理商[" + parentMerchantInfo.getMerchantId() + "],金额[" + parentPayProfit + "]失败");
                }
            }
            agentsProfit = agentsProfit.add(parentPayProfit);
            return alterAgentSingleBalance(agentsProfit, parentMerchantInfo, withdrawInfo);
        }
        return agentsProfit;
    }


    //扣除修改代理商的余额
    //记录代理商的流水
    //商户汇率 0.6 代理 0.5 总代 0.4 * 下发金额
    //channelCode
    @SuppressWarnings({"deprecation"})
    private BigDecimal alterAgentRateBalance(BigDecimal agentsProfit, PayMerchantInfo curMerchantInfo, PayWithdrawInfo withdrawInfo) throws TranException {

        //当前商户汇率
        PayAgentChannelSite curMerchantChannelSite = iPayAgentChannelSiteService.getOne(
                new QueryWrapper<PayAgentChannelSite>().lambda()
                        .eq(PayAgentChannelSite::getMerchantId, curMerchantInfo.getMerchantId())
                        .eq(PayAgentChannelSite::getChannelCode, curMerchantInfo.getChannelCode())
                        .eq(PayAgentChannelSite::getPayType,CodeConst.PayType.SEND_OUT)
        );
        //上级商户
        PayMerchantInfo parentMerchantInfo = iPayMerchantInfoService.getOne(new QueryWrapper<PayMerchantInfo>().lambda().eq(PayMerchantInfo::getMerchantId, curMerchantInfo.getParentId()));
        //找到上级代理
        if (parentMerchantInfo != null) {
            //上级代理通道信息
            PayAgentChannelSite parentChannelSite = iPayAgentChannelSiteService.getOne(new QueryWrapper<PayAgentChannelSite>().lambda()
                    .eq(PayAgentChannelSite::getMerchantId, parentMerchantInfo.getMerchantId())
                    .eq(PayAgentChannelSite::getPayType,CodeConst.PayType.SEND_OUT)
            );
            //代理手续费 = （商户成本 - 代理成本） * 下发金额
            _log.info("商户下发手续费率*下发金额:{},代理成本:{}", curMerchantChannelSite.getMerchantRate(), parentChannelSite.getMerchantRate());
            BigDecimal parentPayProfitRate = new BigDecimal(curMerchantChannelSite.getMerchantRate()).subtract(new BigDecimal(parentChannelSite.getMerchantRate()));
            //BigDecimal parentPayProfit = parentPayProfitRate.multiply(queryResultInfo.getWithdrawAmt());
            BigDecimal parentPayProfit = parentPayProfitRate.divide(new BigDecimal("100")).multiply(withdrawInfo.getAmt()).setScale(2, BigDecimal.ROUND_HALF_UP);

            //扣除
            Integer parentIndex = balanceService.alterMerchantBalance(parentMerchantInfo.getMerchantId(), parentPayProfit.negate(), null);
            if (parentIndex != 1) throw new TranException(CodeConst.SYSTEM_ERROR_CODE, "扣除代理商利润失败");
            _log.info("===>>>扣除代理:{},单笔下发手续费率*下发金额分成:{}", parentMerchantInfo.getMerchantId(), parentPayProfit);


            Map<String, Object> params = new HashMap<String, Object>();
            params.put("merchantId", parentMerchantInfo.getMerchantId()); //某一个代理 | WARNING:高并发时不准确
            PayMerchantMoneyChange latestRecord = iPayMerchantMoneyChangeService.selectLatestMoneyChangeForUpdate(params);
            //没有流水 要新插入
            if (latestRecord == null) {

                PayMerchantMoneyChange addMoneyChangeEntity = new PayMerchantMoneyChange();

                addMoneyChangeEntity.setMerchantId(parentMerchantInfo.getMerchantId()); //某一个代理
                addMoneyChangeEntity.setFlowType(EnumFlowType.RECEIVE.getName()); //代付类型
                addMoneyChangeEntity.setMerchantWithdrawNo(withdrawInfo.getMerchantWithdrawNo());
                addMoneyChangeEntity.setWithdrawNo(withdrawInfo.getWithdrawNo());
                addMoneyChangeEntity.setAmt(parentPayProfit); // 提交代付金额
                addMoneyChangeEntity.setCreditAmt(parentPayProfit); //实际入账金额
                addMoneyChangeEntity.setAmtBefore(new BigDecimal(0));
                addMoneyChangeEntity.setAmtNow(parentPayProfit);

                //addMoneyChangeEntity.setCreateTime(new Date());
                addMoneyChangeEntity.setRemark("扣除代理[" + parentMerchantInfo.getMerchantId() + "]下发分成");
                addMoneyChangeEntity.setChannelName(parentMerchantInfo.getChannelName());
                addMoneyChangeEntity.setChannelCode(parentChannelSite.getChannelCode());

                boolean isAddMoneyChangeEntity = iPayMerchantMoneyChangeService.save(addMoneyChangeEntity);

                if (!isAddMoneyChangeEntity) {
                    throw new TranException(CodeConst.SYSTEM_ERROR_CODE, "代付:插入代理商分润流水失败");
                }
            } else {

//                PayMerchantMoneyChange moneyLatestChangeEntity = new PayMerchantMoneyChange();
//                moneyLatestChangeEntity.setMerchantId(parentMerchantInfo.getMerchantId()); //某一个商家
//                moneyLatestChangeEntity.setAmt(parentPayProfit.negate()); // 退款 减少代理的款项

                //1.插入流水表
//                Integer latestRecordId = iPayMerchantMoneyChangeService.insertLatestMoneyChangeBySelect(moneyLatestChangeEntity);
//                if (latestRecordId != 1) {
//                    throw new TranException(CodeConst.SYSTEM_ERROR_CODE, "失败:扣除代理商分润流水失败");
//                }

                //2.获取插入的用户
                //  PayMerchantMoneyChange latestRecordPayMerchant = iPayMerchantMoneyChangeService.getById(moneyLatestChangeEntity.getId());
                //更新时间
                _log.info("代理流水记录===>>>商家:{},账变前:{},账变后:{}", parentMerchantInfo.getMerchantId(), latestRecord.getAmtNow(), latestRecord.getAmtNow().subtract(parentPayProfit));

                //3.记录MONEY_CHANGE 退款流水
                PayMerchantMoneyChange moneyChangeEntity = new PayMerchantMoneyChange();
                moneyChangeEntity.setMerchantId(parentMerchantInfo.getMerchantId());
                moneyChangeEntity.setFlowType(EnumFlowType.RECEIVE.getName()); //退回状态
                moneyChangeEntity.setMerchantWithdrawNo(withdrawInfo.getMerchantWithdrawNo());
                moneyChangeEntity.setWithdrawNo(withdrawInfo.getWithdrawNo());
                moneyChangeEntity.setAmt(parentPayProfit); //成交金额
                moneyChangeEntity.setCreditAmt(parentPayProfit); //实际入账金额
                moneyChangeEntity.setAmtBefore(latestRecord.getAmtNow());
                moneyChangeEntity.setAmtNow(latestRecord.getAmtNow().subtract(parentPayProfit));

                //moneyChangeEntity.setCreateTime(new Date());
                moneyChangeEntity.setRemark("扣除代理[" + parentMerchantInfo.getMerchantId() + "]下发分成[" + parentPayProfit + "]");
                moneyChangeEntity.setChannelName(withdrawInfo.getChannelCode());
                moneyChangeEntity.setChannelCode(withdrawInfo.getChannelCode());
                boolean isSaveMoneyChange = iPayMerchantMoneyChangeService.save(moneyChangeEntity);
                //boolean isUpdateMoneyChange = iPayMerchantMoneyChangeService.update(moneyChangeEntity, new QueryWrapper<PayMerchantMoneyChange>().lambda().in(PayMerchantMoneyChange::getId, moneyLatestChangeEntity.getId()));

                if (!isSaveMoneyChange) {
                    _log.info("=====>>>>>扣除代理商下发/代付余额变动明细表:{},代付流水表:{}失败", parentMerchantInfo.getMerchantId(), parentPayProfit);
                    throw new TranException(CodeConst.SYSTEM_ERROR_CODE, "扣除代理商分成[" + parentMerchantInfo.getMerchantId() + "],金额[" + parentPayProfit + "]失败");
                }
            }
            agentsProfit = agentsProfit.add(parentPayProfit);
            return alterAgentRateBalance(agentsProfit, parentMerchantInfo, withdrawInfo);
        }
        return agentsProfit;
    }


    /**
     * 修改代理商的余额
     * 记录代理商的流水
     *  商户 1元 代理 0.7 总代 0.6 == 单笔
     */
    @SuppressWarnings({"deprecation"})
    @Transactional(rollbackFor = Exception.class)
    public BigDecimal alterAgentSingleBalance(BigDecimal agentsProfit, PayMerchantInfo curMerchantInfo, String withdrawNo,String channelWithdrawNo,BigDecimal rechargeAmt,PayMerchantChannelSite currentChannelSite) throws TranException {

        //当前商户汇率
        PayAgentChannelSite curMerchantChannelSite = iPayAgentChannelSiteService.getOne(
                new QueryWrapper<PayAgentChannelSite>().lambda()
                        .eq(PayAgentChannelSite::getMerchantId, curMerchantInfo.getMerchantId())
                        .eq(PayAgentChannelSite::getChannelCode, currentChannelSite.getChannelCode())
                        .eq(PayAgentChannelSite::getPayType,CodeConst.PayType.RECEIVE_IN)
        );
        //上级商户
        PayMerchantInfo parentMerchantInfo = iPayMerchantInfoService.getOne(new QueryWrapper<PayMerchantInfo>().lambda().eq(PayMerchantInfo::getMerchantId, curMerchantInfo.getParentId()));
        //找到上级代理
        if (parentMerchantInfo != null) {
            //上级代理通道信息
            PayAgentChannelSite parentChannelSite = iPayAgentChannelSiteService.getOne(new QueryWrapper<PayAgentChannelSite>().lambda()
                    .eq(PayAgentChannelSite::getMerchantId, parentMerchantInfo.getMerchantId())
                    .eq(PayAgentChannelSite::getChannelCode,currentChannelSite.getChannelCode())
                    .eq(PayAgentChannelSite::getPayType,CodeConst.PayType.RECEIVE_IN)
            );
            //上级代理商未分配代收通道 以防出错 直接返回 0
            if (parentChannelSite == null){ return new BigDecimal(0); }
            //代理手续费 = 商户成本 - 代理成本 (如果是充值金额为负数 则单笔手续费应为扣减)
            _log.info("商户单笔代收手续费:{},代理成本:{}", curMerchantChannelSite.getSinglePoundage(), parentChannelSite.getSinglePoundage());
            BigDecimal parentSingleProfit = curMerchantChannelSite.getSinglePoundage().subtract(parentChannelSite.getSinglePoundage());

            if (BigDecimalUtils.lessThan(rechargeAmt,new BigDecimal(0))){
                parentSingleProfit = parentSingleProfit.negate();
            }
            Integer parentIndex = iPayMerchantBalanceService.alterMerchantBalance(parentMerchantInfo.getMerchantId(), parentSingleProfit, null);
            if (parentIndex != 1) throw new TranException(CodeConst.SYSTEM_ERROR_CODE, "分配代理商利润失败");
            _log.info("===>>>代理:{},单笔分成:{}", parentMerchantInfo.getMerchantId(), parentSingleProfit);

            //继续查询是否有代理的上级代理 即总代
            //FIXME:充值bug修复 若money_change变中没有上级代理,则查找上级代理的余额为 null,会导致 插入代理商分润流水失败
            //以防死锁 selectLatestMoneyChange
            Map<String, Object> params = new HashMap<String, Object>();
            params.put("merchantId", parentMerchantInfo.getMerchantId()); //某一个代理
            PayMerchantMoneyChange latestRecord = iPayMerchantMoneyChangeService.selectLatestMoneyChangeForUpdate(params);

            //没有流水 要新插入
            if (latestRecord == null) {

                PayMerchantMoneyChange addMoneyChangeEntity = new PayMerchantMoneyChange();
                addMoneyChangeEntity.setMerchantId(parentMerchantInfo.getMerchantId()); //某一个代理
                addMoneyChangeEntity.setPayType(CodeConst.PayType.RECEIVE_IN);
                addMoneyChangeEntity.setFlowType(EnumFlowType.RECHARGE.getName()); //代付类型
                addMoneyChangeEntity.setMerchantWithdrawNo("");//设为空先
                addMoneyChangeEntity.setWithdrawNo(withdrawNo);
                addMoneyChangeEntity.setAmt(parentSingleProfit); // 提交代付金额
                addMoneyChangeEntity.setCreditAmt(parentSingleProfit); //实际入账金额
                addMoneyChangeEntity.setAmtBefore(new BigDecimal(0));
                addMoneyChangeEntity.setAmtNow(parentSingleProfit);

                //addMoneyChangeEntity.setCreateTime(new Date());
                addMoneyChangeEntity.setRemark("代理[" + parentMerchantInfo.getMerchantId() + "]单笔代收分成["+parentSingleProfit+"]");
                addMoneyChangeEntity.setChannelName(parentMerchantInfo.getChannelName());
                addMoneyChangeEntity.setChannelCode(parentChannelSite.getChannelCode());

                boolean isAddMoneyChangeEntity = iPayMerchantMoneyChangeService.save(addMoneyChangeEntity);

                if (!isAddMoneyChangeEntity) {
                    _log.error("代收失败:插入代理商:{}分润流水失败",parentMerchantInfo.getMerchantId());
                    throw new TranException(CodeConst.SYSTEM_ERROR_CODE, "代收失败:插入代理商分润流水失败");
                }
            } else {

                //更新时间
                _log.info("代理代收单笔流水记录===>>>商家:{},账变前:{},账变后:{}", parentMerchantInfo.getMerchantId(), latestRecord.getAmtNow(), latestRecord.getAmtNow().add(parentSingleProfit));

                //3.记录MONEY_CHANGE 充值流水
                PayMerchantMoneyChange moneyChangeEntity = new PayMerchantMoneyChange();
                moneyChangeEntity.setMerchantId(parentMerchantInfo.getMerchantId());
                moneyChangeEntity.setPayType(CodeConst.PayType.RECEIVE_IN);
                moneyChangeEntity.setFlowType(EnumFlowType.RECHARGE.getName()); //充值状态
                moneyChangeEntity.setMerchantWithdrawNo("");
                moneyChangeEntity.setWithdrawNo(withdrawNo);
                moneyChangeEntity.setAmt(parentSingleProfit); //资金变动
                moneyChangeEntity.setCreditAmt(parentSingleProfit); //实际入账金额
                moneyChangeEntity.setAmtBefore(latestRecord.getAmtNow());
                moneyChangeEntity.setAmtNow(latestRecord.getAmtNow().add(parentSingleProfit));
                //moneyChangeEntity.setCreateTime(new Date());
                moneyChangeEntity.setRemark("代理[" + parentMerchantInfo.getMerchantId() + "]单笔代收分成[" + parentSingleProfit + "]");
                moneyChangeEntity.setChannelName(currentChannelSite.getChannelCode());
                moneyChangeEntity.setChannelCode(currentChannelSite.getChannelCode());
                boolean isSaveMoneyChange = iPayMerchantMoneyChangeService.save(moneyChangeEntity);
                if (!isSaveMoneyChange) {
                    _log.info("=====>>>>>代理商充值/代收余额变动明细表:{},代收流水表:{}失败", parentMerchantInfo.getMerchantId(), parentSingleProfit);
                    throw new TranException(CodeConst.SYSTEM_ERROR_CODE, "代收分成给代理商[" + parentMerchantInfo.getMerchantId() + "],金额[" + parentSingleProfit + "]失败");
                }
            }
            agentsProfit = agentsProfit.add(parentSingleProfit);
            return alterAgentSingleBalance(agentsProfit, parentMerchantInfo, withdrawNo,channelWithdrawNo,rechargeAmt,currentChannelSite); //递归
        }
        return agentsProfit;
    }


    @SuppressWarnings({"deprecation"})
    @Transactional(rollbackFor = Exception.class)
    public BigDecimal alterAgentRateBalance(BigDecimal agentsProfit, PayMerchantInfo curMerchantInfo, String withdrawNo,String channelWithdrawNo,BigDecimal rechargeAmt,PayMerchantChannelSite currentChannelSite) throws TranException {

        //当前商户汇率
        PayAgentChannelSite curMerchantChannelSite = iPayAgentChannelSiteService.getOne(
                new QueryWrapper<PayAgentChannelSite>().lambda()
                        .eq(PayAgentChannelSite::getMerchantId, curMerchantInfo.getMerchantId())
                        .eq(PayAgentChannelSite::getChannelCode, currentChannelSite.getChannelCode())
                        .eq(PayAgentChannelSite::getPayType,CodeConst.PayType.RECEIVE_IN)
        );
        //上级商户
        PayMerchantInfo parentMerchantInfo = iPayMerchantInfoService.getOne(new QueryWrapper<PayMerchantInfo>().lambda().eq(PayMerchantInfo::getMerchantId, curMerchantInfo.getParentId()));
        //找到上级代理
        if (parentMerchantInfo != null) {

            //上级代理通道信息
            PayAgentChannelSite parentChannelSite = iPayAgentChannelSiteService.getOne(new QueryWrapper<PayAgentChannelSite>().lambda()
                    .eq(PayAgentChannelSite::getMerchantId, parentMerchantInfo.getMerchantId())
                    .eq(PayAgentChannelSite::getChannelCode, currentChannelSite.getChannelCode())
                    .eq(PayAgentChannelSite::getPayType,CodeConst.PayType.RECEIVE_IN));

            //上级代理商未分配代收通道 以防出错 直接返回 0
            if (parentChannelSite == null){ return new BigDecimal(0); }

            //代理手续费 = （商户成本 - 代理成本） * 下发金额
            _log.info("商户代收手续费率 * 代收金额:{},代理成本:{}", curMerchantChannelSite.getMerchantRate(), parentChannelSite.getMerchantRate());
            BigDecimal parentPayProfitRate = new BigDecimal(curMerchantChannelSite.getMerchantRate()).subtract(new BigDecimal(parentChannelSite.getMerchantRate()));

            BigDecimal parentPayProfit = parentPayProfitRate.divide(new BigDecimal("100")).multiply(rechargeAmt).setScale(4, BigDecimal.ROUND_HALF_UP);

            Integer parentIndex = iPayMerchantBalanceService.alterMerchantBalance(parentMerchantInfo.getMerchantId(), parentPayProfit, null);
            if (parentIndex != 1) throw new TranException(CodeConst.SYSTEM_ERROR_CODE, "分配代理商利润失败");
            _log.info("===>>>代理:{},单笔下发手续费率*下发金额分成:{}", parentMerchantInfo.getMerchantId(), parentPayProfit);


            Map<String, Object> params = new HashMap<String, Object>();
            params.put("merchantId", parentMerchantInfo.getMerchantId());
            PayMerchantMoneyChange latestRecord = iPayMerchantMoneyChangeService.selectLatestMoneyChangeForUpdate(params);
            //没有流水 要新插入
            if (latestRecord == null) {

                PayMerchantMoneyChange addMoneyChangeEntity = new PayMerchantMoneyChange();

                addMoneyChangeEntity.setMerchantId(parentMerchantInfo.getMerchantId()); //某一个代理
                addMoneyChangeEntity.setFlowType(EnumFlowType.RECHARGE.getName()); //充值类型
                addMoneyChangeEntity.setPayType(CodeConst.PayType.RECEIVE_IN);
                addMoneyChangeEntity.setMerchantWithdrawNo("");
                addMoneyChangeEntity.setWithdrawNo(withdrawNo);
                addMoneyChangeEntity.setAmt(parentPayProfit); // 提交代付金额
                addMoneyChangeEntity.setCreditAmt(parentPayProfit); //实际入账金额
                addMoneyChangeEntity.setAmtBefore(new BigDecimal(0));
                addMoneyChangeEntity.setAmtNow(parentPayProfit);
                //addMoneyChangeEntity.setCreateTime(new Date());
                addMoneyChangeEntity.setRemark("代理[" + parentMerchantInfo.getMerchantId() + "]代收分成");
                addMoneyChangeEntity.setChannelName(parentMerchantInfo.getChannelName());
                addMoneyChangeEntity.setChannelCode(parentChannelSite.getChannelCode());

                boolean isAddMoneyChangeEntity = iPayMerchantMoneyChangeService.save(addMoneyChangeEntity);

                if (!isAddMoneyChangeEntity) {
                    _log.error("代收失败:插入代理商:{}分润流水失败",parentMerchantInfo.getMerchantId());
                    throw new TranException(CodeConst.SYSTEM_ERROR_CODE, "代收失败:插入代理商分润流水失败");
                }
            } else {

                //更新时间
                _log.info("代理代收流水记录===>>>商家:{},账变前:{},账变后:{}", parentMerchantInfo.getMerchantId(), latestRecord.getAmtNow(), latestRecord.getAmtNow().add(parentPayProfit));

                //3.记录MONEY_CHANGE 退款流水
                PayMerchantMoneyChange moneyChangeEntity = new PayMerchantMoneyChange();
                moneyChangeEntity.setMerchantId(parentMerchantInfo.getMerchantId());
                moneyChangeEntity.setPayType(CodeConst.PayType.RECEIVE_IN);
                moneyChangeEntity.setFlowType(EnumFlowType.RECHARGE.getName()); //充值状态
                moneyChangeEntity.setMerchantWithdrawNo("");
                moneyChangeEntity.setWithdrawNo(withdrawNo);
                moneyChangeEntity.setAmt(parentPayProfit); //资金变动
                moneyChangeEntity.setCreditAmt(parentPayProfit); //实际入账金额
                moneyChangeEntity.setAmtBefore(latestRecord.getAmtNow());
                moneyChangeEntity.setAmtNow(latestRecord.getAmtNow().add(parentPayProfit));

                //moneyChangeEntity.setCreateTime(new Date());
                moneyChangeEntity.setRemark("代理[" + parentMerchantInfo.getMerchantId() + "]代收分成[" + parentPayProfit + "]");
                moneyChangeEntity.setChannelName(currentChannelSite.getChannelCode());
                moneyChangeEntity.setChannelCode(currentChannelSite.getChannelCode());
                boolean isSaveMoneyChange = iPayMerchantMoneyChangeService.save(moneyChangeEntity);

                // boolean isUpdateMoneyChange = iPayMerchantMoneyChangeService.update(moneyChangeEntity, new QueryWrapper<PayMerchantMoneyChange>().lambda().in(PayMerchantMoneyChange::getId, moneyLatestChangeEntity.getId()));

                if (!isSaveMoneyChange) {
                    _log.info("=====>>>>>代理商充值/代收余额变动明细表:{},代收流水表:{}失败", parentMerchantInfo.getMerchantId(), parentPayProfit);
                    throw new TranException(CodeConst.SYSTEM_ERROR_CODE, "代收分成给代理商[" + parentMerchantInfo.getMerchantId() + "],金额[" + parentPayProfit + "]失败");
                }
            }
            agentsProfit = agentsProfit.add(parentPayProfit);
            return alterAgentRateBalance(agentsProfit, parentMerchantInfo, withdrawNo,channelWithdrawNo,rechargeAmt,currentChannelSite);
        }
        return agentsProfit;
    }


    public BigDecimal getUsdtLive() {

        BigDecimal usdtLive = new BigDecimal(6.480);

        Map<String, Object> reqMap = new HashMap<>();
        reqMap.put("api_key", blockCCApiKey);
        String serverResult = HttpClient4Utils.sendGet(blockCCUrl, reqMap);

        if (serverResult != null) {
            JSONArray json = JSON.parseArray(serverResult);
            JSONObject usdt = (JSONObject) json.get(33);
            usdtLive = (BigDecimal) usdt.get("r");
        }

        _log.info("USDT - {}", usdtLive);

        return usdtLive;
    }

    //测试付款
    public static void main(String[] args) throws IllegalException, InterruptedException {
        //queryAccountBalance();
        //transferToken();
        transferTrx();
    }

    public static void queryAccountBalance(){
        String usdtContractAddress = "TR7NHqjeKQxGTCi8q8ZY4pL8otSzgjLj6t";
        String trongridApiKey="943a1281-3edd-4822-9ea7-19a6f6c793b3";
        String privateKey = "7E1F460914250F96FD8459417DB339DA6FC0EA9DF7A6FCA3833FE5045C8B08B1";//钱包私钥
        String fromAddress = "TAudtm7tYLW4Jqxiw7Y98fGdJSgJ4oF999";//钱包地址
        BigDecimal sun = new BigDecimal(1000000); //百万 million

        ApiWrapper wrapper = ApiWrapper.ofMainnet(privateKey, trongridApiKey);

        //USDT余额
        Contract contract = wrapper.getContract(usdtContractAddress);
        Trc20Contract token = new Trc20Contract(contract, fromAddress, wrapper);
        BigInteger usdtBalance = token.balanceOf(fromAddress);
        System.out.println("usdt Balance:" + usdtBalance + " sun");
        BigInteger usdt = usdtBalance.divide(BigInteger.valueOf(Long.parseLong(sun.toString())));
        System.out.println("usdt:" + usdt + " ");



        //TRX余额
        BigDecimal minTrxBalance = sun.multiply(new BigDecimal(10)); //10 trx
        BigDecimal trxBalance = new BigDecimal(String.valueOf(wrapper.getAccountBalance(fromAddress)));
        System.out.println("trxBalance:" + trxBalance + " sun");

        wrapper.close();
    }

    //转账USDT
    public static void transferToken(){

        String usdtContractAddress = "TR7NHqjeKQxGTCi8q8ZY4pL8otSzgjLj6t";
        String trongridApiKey="943a1281-3edd-4822-9ea7-19a6f6c793b3";
        String privateKey = "7E1F460914250F96FD8459417DB339DA6FC0EA9DF7A6FCA3833FE5045C8B08B1";//钱包私钥
        String fromAddress = "TAudtm7tYLW4Jqxiw7Y98fGdJSgJ4oF999";//钱包地址
        String toAddress = "TUioo83U1ryey8523iAFZ98xXPc6AvdWQ2";//666
        BigDecimal sun = new BigDecimal(1000000); //百万 million

        //USDT转账

        ApiWrapper wrapper = ApiWrapper.ofMainnet(privateKey, trongridApiKey);

        //USDT
        Contract contract = wrapper.getContract(usdtContractAddress);
        Trc20Contract token = new Trc20Contract(contract, fromAddress, wrapper);
        String amount = "3.2";//usdt
        String amountStr = String.valueOf(sun.multiply(new BigDecimal(amount)));
        if(amountStr.contains(".")) {
            int indexOf = amountStr.indexOf(".");
            amountStr = amountStr.substring(0, indexOf);
        }
        long amountLong = Long.parseLong(amountStr); //8 USDT
        //能量充为0
        int power = 0;
        //消耗能量上限 15TRX
        long feeLimit = Long.parseLong(String.valueOf(sun.multiply(new BigDecimal(15))));

        String hashId = token.transfer(toAddress, amountLong, power, "", feeLimit);
        System.out.println("USDT转账HASH - "+ hashId);

        wrapper.close();
    }
    //转账TRX
    public static void transferTrx() throws IllegalException, InterruptedException {

        String trongridApiKey="943a1281-3edd-4822-9ea7-19a6f6c793b3";
        String privateKey = "7E1F460914250F96FD8459417DB339DA6FC0EA9DF7A6FCA3833FE5045C8B08B1";//钱包私钥
        String fromAddress = "TAudtm7tYLW4Jqxiw7Y98fGdJSgJ4oF999";//钱包地址
        String toAddress = "TUioo83U1ryey8523iAFZ98xXPc6AvdWQ2";//666
        BigDecimal sun = new BigDecimal(1000000); //百万 million


        //TRX转账
        ApiWrapper wrapper = ApiWrapper.ofMainnet(privateKey, trongridApiKey);

        for (int i=0;i<10;i++){

            BigDecimal amount = new BigDecimal("10.2").add(new BigDecimal(i));//trx
            String amountStr = String.valueOf(sun.multiply(amount));
            //金额去除小数点
            if(amountStr.contains(".")) {
                int indexOf = amountStr.indexOf(".");
                amountStr = amountStr.substring(0, indexOf);
            }
            long amountLong = Long.parseLong(amountStr); //10.8 USDT

            TransactionExtention transaction = wrapper.transfer(fromAddress,toAddress, amountLong);
            Transaction signedTxn = wrapper.signTransaction(transaction);
            String hashId = wrapper.broadcastTransaction(signedTxn); //HASH ID

            System.out.println("TRX转账HASH "+ i +"- "+ hashId);

            //强制sleep
            Thread.sleep(1000);
        }


        wrapper.close(); //记得关闭
    }


}