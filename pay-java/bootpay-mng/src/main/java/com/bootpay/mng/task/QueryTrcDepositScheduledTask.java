package com.bootpay.mng.task;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.bootpay.common.constants.CodeConst;
import com.bootpay.common.constants.MyRedisConst;
import com.bootpay.common.constants.enums.EnumFlowType;
import com.bootpay.common.constants.enums.EnumMerchantStatusType;
import com.bootpay.common.excetion.TranException;
import com.bootpay.common.framework.RedisUtil;
import com.bootpay.common.utils.DateUtil;
import com.bootpay.common.utils.HttpClient4Utils;
import com.bootpay.core.entity.*;
import com.bootpay.core.service.*;
import com.bootpay.mng.amqueue.AMProducerUtils;
import com.bootpay.mng.service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;

/**
 * 定时查询代付订单状态
 *
 * @author Administrator
 */
@Component
public class QueryTrcDepositScheduledTask {

    private Logger _log = LoggerFactory.getLogger(QueryTrcDepositScheduledTask.class);

    @Value("${app.usdtTokenAddress}")
    private String usdtTokenAddress;

    @Value("${app.receiverTokenAddress}")
    private String receiverTokenAddress;

    @Value("${app.merchantPlatformId}") //外包ID
    private String merchantPlatformId;

    @Autowired
    private RedisUtil redisUtil;
    @Autowired
    private IPayWithdrawInfoService infoService;

    @Autowired
    private IPayPlatformIncomeInfoService incomeInfoService;

    @Autowired
    private AMProducerUtils amProducerUtils;

    @Autowired
    private IPayMerchantInfoService iPayMerchantInfoService; //商家表服务==>PayMerchantInfo

    @Autowired
    private IPayMerchantChannelSiteService iPayMerchantChannelSiteService; //商户通道成本服务

    @Autowired
    private IPayAgentChannelSiteService iPayAgentChannelSiteService; //代理通道

    @Autowired
    private IPayMerchantBalanceService balanceService; //商户余额服务

    @Autowired
    private IPayMerchantMoneyChangeService iPayMerchantMoneyChangeService; //商户流水明细

    @Autowired
    private ICoinHolderDepositInfoService iCoinHolderDepositInfoService; //COIN代收


    @Autowired
    private IPayDepositInfoService iPayDepositInfoService;

    @Autowired
    private NotifyService notifyService;

    @Autowired
    private ICoinHolderInfoService iCoinHolderInfoService;

    @Autowired
    private CoinWithdrawManagerService coinWithdrawManagerService;

    @Autowired
    private MerchantAccountManagerService merchantAccountManagerService;


    @Autowired
    private CoinChannelManagerService coinChannelManagerService; //币址

    @Autowired
    private IPayPartnerDepositInfoService iPayPartnerDepositInfoService;

    @Autowired
    private IPayPartnerBalanceService iPayPartnerBalanceService;

    @Autowired
    private PartnerManagerService partnerManagerService;

    /**
     * @ 每30s执行一次
     * @ 读取缓存中的收款订单数据
     * @ 读取数据库中未支付的所有订单
     * @ 超时 300秒的全部设为失败
     * @ 300秒内的订单 查询节点网络 若有支付成功的订单 则更新订单为成功状态
     */
    @Scheduled(cron = "0/30 * * * * ?")
    public void scheduled() throws Exception {
        // TODO:待升级为缓存处理
        /*Set<String> withdrawSet = redisUtil.sGet(MyRedisConst.QUERY_DEPOSIT_CACHE_SET_NAME);
        if (withdrawSet == null || withdrawSet.size() == 0) {
            return;
        }*/
        int seconds = 60 * 10; //60秒 10分种
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.SECOND, -seconds);

        _log.warn("查询TRC充值任务==>> ");

        //1.读取数据库中的所有未支付的订单 不用带时间
        Map<String, Object> queryParams = new HashMap<String, Object>();
        queryParams.put("status", CodeConst.WithdrawStatusConst.WITHDRAW_UNKNOWN); //查询待支付的订单 是否网络节点已有相同钱包地址、金额

        List<PayPartnerDepositInfo> onGoingList = iPayPartnerDepositInfoService.selectPartnerDepositInfoList(queryParams);

        //2.TRON网络节点数据(已确认数据)
        // https://api.trongrid.io/v1/accounts/TRHkmDQRMYk3o3Zqd5PKKrwuk7XqjN49Up/transactions/trc20?only_confirmed=true
        // https://api.trongrid.io/v1/accounts/TRHkmDQRMYk3o3Zqd5PKKrwuk7XqjN49Up/transactions/trc20?only_confirmed=true&min_timestamp=1627355208000
        for (PayPartnerDepositInfo info : onGoingList) {
            String payerAddr = receiverTokenAddress;//唯一收款地址
            BigDecimal amtUsdt = info.getAmtUsdt(); //支付usdt金额
            Map<String, Object> reqMap = new HashMap<>();
            reqMap.put("only_confirmed", true);
            reqMap.put("limit", 100);
            reqMap.put("min_timestamp",calendar.getTimeInMillis()); //before 6mins timestamp

            String tronUrl = "https://api.trongrid.io/v1/accounts/"+payerAddr+"/transactions/trc20";
            _log.info("tronUrl =====>>>>>{}", tronUrl);
            String serverResult = HttpClient4Utils.sendGet(tronUrl, reqMap);
            _log.info("TRON网络节点数据返回=====>>>>>{}", serverResult);

            JSONObject json = JSONObject.parseObject(serverResult);
            if (json != null){
                JSONObject meta = JSONObject.parseObject(json.get("meta").toString());
                _log.info("JSON OBJECT:{}", meta.get("page_size").toString());
                if (!"0".equals(meta.get("page_size").toString())) {
                    //有数据才做判断

                    //封装TRANS类
                    BigDecimal sun_unit = new BigDecimal(1000000);

                    List<TransInfo> list = JSONObject.parseArray(json.get("data").toString(), TransInfo.class);
                    for (TransInfo transInfo : list) {
                        //_log.info("TRON 网络节点Type:{},transaction id:{},amt:{},from:{},to:{},date:{}",
                        //transInfo.getType(),transInfo.getTransaction_id(),
                         //transInfo.getValue(),transInfo.getFrom(),transInfo.getTo(),transInfo.getBlock_timestamp());
                        //只要转账的才记录 Transfer | Approval
                        String amtStr = String.valueOf(info.getAmtUsdt().multiply(sun_unit));
                        String amtValue = amtStr.substring(0,amtStr.lastIndexOf("."));
                        _log.info("getAmtUsdt:{},getValue:{},receiver:{}",amtValue,transInfo.getValue(),transInfo.getTo());

                        //WARN:transInfo.getTokenInfo().getAddress() 确保是USDT 以防有相同的TRC20 TOKEN
                        //_log.info("token_info - {}",transInfo.getToken_info().getAddress());
                        //多增加个类型 以防止通过授权的方式绕过
                        if ("Transfer".equals(transInfo.getType()) && transInfo.getTo().equalsIgnoreCase(receiverTokenAddress) && amtValue.equals(transInfo.getValue()) && usdtTokenAddress.equals(transInfo.getToken_info().getAddress())) {
                            _log.info("付款成功 HASH ID - {}",transInfo.getTransaction_id());
                            addHolderAndFlow(info,transInfo);
                        }

                    }
                }
            }

        }
    }

    @Transactional(rollbackFor = Exception.class)
    @SuppressWarnings("deprecation")
    public void addHolderAndFlow(PayPartnerDepositInfo depositInfo,TransInfo transInfo) throws Exception {

        //预先判断 -- 如果有相同的HASH ID 则不再添加
        PayPartnerDepositInfo alreadyHasDepositInfo = iPayPartnerDepositInfoService.getOne(new QueryWrapper<PayPartnerDepositInfo>().lambda()
                .eq(PayPartnerDepositInfo::getHashId, transInfo.getTransaction_id()));
        if (alreadyHasDepositInfo != null ){
            _log.error("已有相同订单记录:{}",transInfo.getTransaction_id());
            throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "已有相同订单记录");
        }
        //1.更新表CoinHolderDepositInfo
        PayPartnerDepositInfo holderEntity = new PayPartnerDepositInfo();
        holderEntity.setHashId(transInfo.getTransaction_id());
        holderEntity.setStatus(CodeConst.WithdrawStatusConst.WITHDRAW_SUCCESS);
        holderEntity.setEndTime(new Date());//transInfo.getBlock_timestamp()
        holderEntity.setRemark("支付成功");
        boolean isUpdHolder = iPayPartnerDepositInfoService.update(holderEntity,new UpdateWrapper<PayPartnerDepositInfo>().lambda()
                .eq(PayPartnerDepositInfo::getWithdrawNo, depositInfo.getWithdrawNo())); //保存更改
        if (!isUpdHolder){
            _log.error("充值订单:{}更新失败",depositInfo.getWithdrawNo());
            throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "充值订单更新失败");
        }

        //2.更改PayPartnerBalance余额+流水表
        iPayPartnerBalanceService.alterAccountFee(depositInfo.getMerchantId(), depositInfo.getAmt(), null);

        partnerManagerService.partnerFeeChange(depositInfo.getWithdrawNo(),depositInfo.getAmt(),merchantPlatformId,CodeConst.PartnerFlowType.RECHARGE);

        //7.记录订单成功的缓存
        int timeTTL = "0".equals(CodeConst.CoinType.TRX) ? 60 * 10 : 60 * 20;  //10*60; 订单 trx 10分钟失效 | eth 20分钟
        //平台订号号:钱包地址:金额
        String redis_success = "partner:" + depositInfo.getWithdrawNo() + MyRedisConst.SPLICE + "success";
        redisUtil.set(redis_success,redis_success,timeTTL);
    }


}
