package com.bootpay.mng.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.bootpay.common.constants.CodeConst;
import com.bootpay.common.utils.EncryptUtil;
import com.bootpay.common.utils.HttpClient4Utils;
import com.bootpay.common.utils.Signature;
import com.bootpay.core.entity.*;
import com.bootpay.core.service.ICoinPayerWithdrawInfoService;
import com.bootpay.core.service.IPayDepositInfoService;
import com.bootpay.core.service.IPayMerchantInfoService;
import com.bootpay.mng.service.NotifyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.HashMap;
import java.util.Map;

@Service
public class NotifyServiceImpl implements NotifyService {

    private Logger _log = LoggerFactory.getLogger(NotifyServiceImpl.class);

    @Autowired
    private IPayMerchantInfoService infoService;
    @Autowired
    private IPayDepositInfoService iPayDepositInfoService;
    @Autowired
    private ICoinPayerWithdrawInfoService iCoinPayerWithdrawInfoService;

    @Value("${app.walletPrivateKeySalt}") //application.yml文件读取
    private String walletPrivateKeySalt;

    /**
     * 代付
     * */
    @Override
    public void notifyMerchant(PayWithdrawInfo withdrawInfo, String withdrawStatus) throws Exception {

        String refNo="";
        String sentAddress = "";//出款GCASH账号
        CoinPayerWithdrawInfo coinPayerWithdrawInfo = iCoinPayerWithdrawInfoService.getOne(new QueryWrapper<CoinPayerWithdrawInfo>().lambda().eq(CoinPayerWithdrawInfo::getWithdrawNo, withdrawInfo.getWithdrawNo()));
        if (coinPayerWithdrawInfo!=null){
            if (!"".equals(coinPayerWithdrawInfo.getHashId())){
                refNo = coinPayerWithdrawInfo.getHashId();
                sentAddress = coinPayerWithdrawInfo.getPayerAddr();
            }
        }
        //取得某一个商家的信息
        PayMerchantInfo merchantInfo = infoService.getOne(new QueryWrapper<PayMerchantInfo>().lambda().eq(PayMerchantInfo::getMerchantId, withdrawInfo.getMerchantId()));
        String dataDecrypt = EncryptUtil.aesDecrypt(merchantInfo.getApiKey(),walletPrivateKeySalt); //商户的API KEY要解密

        Map<String, Object> notifyMap = new HashMap<String, Object>();
        notifyMap.put("merchantId", merchantInfo.getMerchantId());
        notifyMap.put("withdrawNo", withdrawInfo.getWithdrawNo());//平台订单号
        notifyMap.put("merchantWithdrawNo", withdrawInfo.getMerchantWithdrawNo());//ref. no.
        notifyMap.put("acctName", withdrawInfo.getAcctName());
        notifyMap.put("acctAddr", withdrawInfo.getAcctAddr());
        notifyMap.put("sentAddress", sentAddress);//出款GCASH账号
        notifyMap.put("amt", withdrawInfo.getAmt());//代付的金额
        notifyMap.put("withdrawStatus", (withdrawStatus != null && !"".equals(withdrawStatus)) ? withdrawStatus : withdrawInfo.getWithdrawStatus());
        notifyMap.put("refNo", refNo);//出款后的 REF. NO. 失败则为空
        notifyMap.put("withdrawRateFee", withdrawInfo.getMerchantRateFee());//按比例收费的手续费
        notifyMap.put("withdrawSingleFee", withdrawInfo.getMerchantFee());//商家下发单笔手续费
        notifyMap.put("sign", Signature.getSign(notifyMap, dataDecrypt));
        //notifyMap.put("mintFee",withdrawInfo.getMintFee());//矿工手续费不参与验签(下发失败 没退回)
        _log.info("代付订单:{},通知:{}", withdrawInfo.getMerchantWithdrawNo(),notifyMap);
        /*
         * @通知给下游商家Url
         * */
        if ("".equals(withdrawInfo.getMerchantNotifyUrl())){
            _log.info("代付订单状态通知:商户回调地址为空");
        }else {
            String result = HttpClient4Utils.sendHttpRequest(withdrawInfo.getMerchantNotifyUrl(), notifyMap, "utf-8", true);
            _log.info("代付订单状态通知:{},商户端返回---》{}", withdrawInfo.getMerchantNotifyUrl(), result);
        }

    }


    /**
     * 支付通知
     * */
    @Override
    @Transactional
    public void notifyMerchantDeposit(PayDepositInfo depositInfo, String withdrawStatus) throws Exception {
        //取得某一个商家的信息
        PayMerchantInfo merchantInfo = infoService.getOne(new QueryWrapper<PayMerchantInfo>().lambda().eq(PayMerchantInfo::getMerchantId, depositInfo.getMerchantId()));
        String dataDecrypt = EncryptUtil.aesDecrypt(merchantInfo.getApiKey(),walletPrivateKeySalt); //商户的API KEY要解密


        Map<String, Object> notifyMap = new HashMap<String, Object>();

        notifyMap.put("merchantWithdrawNo", depositInfo.getMerchantWithdrawNo());
        notifyMap.put("withdrawNo", depositInfo.getWithdrawNo());
        notifyMap.put("coinType",depositInfo.getCoinType());//0-微信,1-支付宝,2-聚合
        notifyMap.put("merchantId", merchantInfo.getMerchantId());

        notifyMap.put("amt", String.valueOf(depositInfo.getAmt()));//订单金额 rmb
//        notifyMap.put("amtBonus", String.valueOf(depositInfo.getAmtBonus()));//随机优惠立减
        notifyMap.put("amtPay", String.valueOf(depositInfo.getAmtPay()));//实际支付金额
//        notifyMap.put("amtCredit", String.valueOf(depositInfo.getAmtCredit()));//商家扣除手续费的入账金额
//        notifyMap.put("merchantRateFee", String.valueOf(depositInfo.getMerchantRateFee()));//代收手续费
//        notifyMap.put("merchantSingleFee", String.valueOf(depositInfo.getMerchantFee()));//代收单笔手续费
        notifyMap.put("withdrawStatus", (withdrawStatus != null && !"".equals(withdrawStatus)) ? withdrawStatus : depositInfo.getWithdrawStatus());
        notifyMap.put("extraParam", depositInfo.getExtraParam());//商家拓展参数
        notifyMap.put("merchantNotifyUrl", depositInfo.getMerchantNotifyUrl()); //回调网址不空则参与验签
        notifyMap.put("sign", Signature.getSign(notifyMap, dataDecrypt));

        _log.info("代收订单状态通知商户---》{}", notifyMap);

        //若父无事务,是否依然会调用？
        //强制COMMIT 商家有没有返回无关紧要
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
            @Override
            public void afterCommit() {
                /*
                 * @通知给下游商家Url
                 * */
                // String result =  HttpClient4Utils.sendPost(withdrawInfo.getMerchantNotifyUrl(), notifyMap);
                if (depositInfo.getMerchantNotifyUrl() != null && !"".equals(depositInfo.getMerchantNotifyUrl())) {
                    //修改超时时间为3秒
                    try {

                        String result = HttpClient4Utils.sendHttpRequest(depositInfo.getMerchantNotifyUrl(), notifyMap, "utf-8", true,3 * 1000);
                        if (result != null) {
                            _log.info("代收订单状态通知:{},用户端返回---》{}", depositInfo.getMerchantNotifyUrl() != null ? depositInfo.getMerchantNotifyUrl() : "", result);
                            if ("success".equalsIgnoreCase(result)) { //前台返回SUCCESS （不区分大小写）
                                //更新订单回调状态
                                PayDepositInfo deposit = new PayDepositInfo();
                                deposit.setReturnType(CodeConst.ReturnType.YES);
                                iPayDepositInfoService.update(deposit, new UpdateWrapper<PayDepositInfo>().lambda()
                                        .eq(PayDepositInfo::getWithdrawNo, depositInfo.getWithdrawNo()));
                            }
                        }


                    } catch (Exception e) {
                        _log.error("TASK 订单号 "+depositInfo.getWithdrawNo()+" 通知下游商家出错 - " + e);
                    }

                }
            }
        });

    }


}
