package com.bootpay.mng.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;

import com.bootpay.channel.constants.DepositChannelCode;
import com.bootpay.channel.vo.ChannelResultVo;
import com.bootpay.common.constants.CodeConst;
import com.bootpay.common.constants.MyRedisConst;
import com.bootpay.common.constants.enums.EnumFlowType;
import com.bootpay.common.constants.enums.EnumMerchantStatusType;
import com.bootpay.common.constants.enums.EnumRequestType;
import com.bootpay.common.constants.enums.EnumWithdrawStatus;
import com.bootpay.common.excetion.TranException;
import com.bootpay.common.framework.RedisUtil;
import com.bootpay.common.utils.*;
import com.bootpay.core.entity.*;
import com.bootpay.core.service.*;
import com.bootpay.mng.amqueue.AMProducerUtils;
import com.bootpay.mng.service.CoinChannelManagerService;
import com.bootpay.mng.service.MerchantAccountManagerService;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import javax.jms.JMSException;
import javax.jms.Session;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

@Service
public class MerchantAccountManagerServiceImpl implements MerchantAccountManagerService {

    private static Logger _log = LoggerFactory.getLogger(MerchantAccountManagerServiceImpl.class);

    @Value("${app.redisPayUrl}") //application.yml文件读取
    private String redisPayUrl;

    @Value("${app.walletPrivateKeySalt}") //application.yml文件读取
    private String walletPrivateKeySalt;

    @Autowired
    private RedisUtil redisUtil;
    
    @Autowired
    private IPayMerchantChannelSiteService iPayMerchantChannelSiteService; //商家通道
    @Autowired
    private IPayAgentChannelSiteService iPayAgentChannelSiteService; //代理通道

    @Autowired
    private IPayMerchantBalanceService balanceService;
    @Autowired
    private IPayPlatformIncomeInfoService incomeInfoService;
    @Autowired
    private IPayMerchantFlowInfoService flowInfoService;
    @Autowired
    private IPayMerchantInfoService infoService;
    @Autowired
    private IWithdrawChannelInfoService channelInfoService;
    @Autowired
    private IPayWithdrawInfoService iPayWithdrawInfoService;
    @Autowired
    private IPubCheckBankService checkBankService;
    @Autowired
    private IWithdrawChannelInfoService iWithdrawChannelInfoService;
    @Autowired
    private AMProducerUtils amProducerUtils;

    @Autowired
    private IPayMerchantInfoService iPayMerchantInfoService; //商家表==>PayMerchantInfo

    @Autowired
    private IPayMerchantMoneyChangeService iPayMerchantMoneyChangeService; //商户流水明细

    @Autowired
    private ICardPayerInfoService iCardPayerInfoService; //持卡人service

    @Autowired
    private ICoinPayerInfoService iCoinPayerInfoService; //付款钱包

    @Autowired
    private ICardChannelInfoService iCardChannelInfoService; //卡通道

    @Autowired
    private  ICoinChannelWithdrawInfoService iCoinChannelWithdrawInfoService;

    @Autowired
    private CoinChannelManagerService coinChannelManagerService; //币址

    @Autowired
    private IPayMerchantBalanceService iPayMerchantBalanceService; //商户总余额

    @Autowired
    private ICoinChannelDepositInfoService iCoinChannelDepositInfoService;

    @Autowired
    private ICoinHolderDepositInfoService iCoinHolderDepositInfoService;

    @Autowired
    private ICoinHolderInfoService iCoinHolderInfoService;

    @Autowired
    private IPayAgentBalanceService  iPayAgentBalanceService;

    @Autowired
    private IPayDepositInfoService iPayDepositInfoService;


    @Override
    @Transactional(rollbackFor = Exception.class) //充值回滚
    @SuppressWarnings({"deprecation"})
    public void merchantRecharge(BigDecimal amt,
                                 BigDecimal amtCredit,
                                 String merchantId,
                                 String merchantName,
                                 String remark,
                                 String managerName,
                                 String label,
                                 String channelCode,
                                 String merchantWithdrawNo,
                                 String withdrawNo,
                                 String flowType) throws TranException {

        // 当前商家信息 通道信息
        QueryWrapper<PayMerchantChannelSite> queryRateWrapper = new QueryWrapper<>();
        queryRateWrapper.lambda()
                .eq(PayMerchantChannelSite::getMerchantId, merchantId)
                .eq(PayMerchantChannelSite::getChannelCode,channelCode);
        PayMerchantChannelSite currentMerchantChannelSite = iPayMerchantChannelSiteService.getOne(queryRateWrapper);

        if (currentMerchantChannelSite == null) {
            throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "商户未配置通道费率");
        }

        //WARN:钱包充值时会影响到商户余额的变动
        //当前商家信息 即有代收和代付 修改余额时如何记录商户资金账变明细？
        //PayMerchantInfo currentMerchantInfo = iPayMerchantInfoService.getOne(new QueryWrapper<PayMerchantInfo>().lambda().eq(PayMerchantInfo::getMerchantId, merchantId));


        // 获取商家的充值的代收/代付通道信息
        WithdrawChannelInfo withdrawChannelInfo = iWithdrawChannelInfoService.getOne(new QueryWrapper<WithdrawChannelInfo>().lambda().eq(WithdrawChannelInfo::getChannelCode, channelCode));
        //1 通道充值成本
        BigDecimal channelCostAmt = new BigDecimal(0);
        //2 代理商充值分成
        BigDecimal agentProfit = new BigDecimal(0);
        //3 平台分成
        BigDecimal platformProfit = new BigDecimal(0);
        //4 上级代理的通道成本
        BigDecimal parentRechargeCost = new BigDecimal(0);

        ///通道充值成本 350 /100
        /*channelCostAmt = new BigDecimal(withdrawChannelInfo.getRechargeCost())
                .multiply(rechargeAmt)
                .divide(new BigDecimal("100"))
                .setScale(2, BigDecimal.ROUND_HALF_UP);*/

        // 修改商户余额
        Integer index = balanceService.alterMerchantBalance(merchantId, amtCredit, null);
        if (index != 1) {
            throw new TranException(CodeConst.SYSTEM_ERROR_CODE, "充值失败:商户余额修改失败");
        }

        //当前通道:PayMerchantChannelSite{id=128, merchantId=M1574843286608316742, merchantRate=0.40, singlePoundage=1.00, channelCode=S015}
        //_log.info("当前商户:{},当前通道:{}",currentMerchantInfo,currentMerchantChannelSite);
        //String depositSeqNumber = MySeq.getWithdrawNo();
        //代理商手续费 + 记录流水明细

        /**
         * 代理商手续费 ｜ 三方才有 私卡分下发时计算
         * */
        //  agentProfit = alterAgentBalance(agentProfit, currentMerchantInfo, rechargeAmt, depositSeqNumber);

        //平台手续费 = 充值金额 1000 - 计算商户入账金额 996 - 代理手续费 - 平台成本
        //  platformProfit = rechargeAmt.subtract(merchantRechargeBalance).subtract(parentRechargeCost).subtract(agentProfit).subtract(channelCostAmt);

        //单笔充值差价
        //  BigDecimal platformSingle = currentMerchantChannelSite.getMerchantRechargeCost().subtract(withdrawChannelInfo.getSingleRechargeCost());
        //平台利润 = 平台手续费 - 扣除通道成本
        //  platformProfit = platformProfit.subtract(platformSingle);
        // 修改平台利润余额
        //   Integer incomeInfoIndex = incomeInfoService.alterPlatformBalance(withdrawChannelInfo.getChannelCode(), platformProfit, null);
        //   if (incomeInfoIndex != 1) {
        //       throw new TranException(CodeConst.SYSTEM_ERROR_CODE, "充值失败:修改平台分成失败");
        //   }

       // _log.info("==>打印商户充值信息:{},充值金额:{},实到到帐金额:{},代理商手续费:{},平台成本:{},平台手续费:{}", merchantId, rechargeAmt, merchantRechargeBalance, agentProfit, channelCostAmt, platformProfit);

        if (withdrawNo == null || "".equals(withdrawNo)) {
            withdrawNo = MySeq.getWithdrawNo();
        }

        //1.充值流水
        PayMerchantFlowInfo flowInfo = new PayMerchantFlowInfo();
        flowInfo.setPayType(withdrawChannelInfo.getPayType()); //代收或者代付
        flowInfo.setMerchantId(merchantId);
        flowInfo.setMerchantName(merchantName);
        flowInfo.setFlowType(flowType);//EnumFlowType.RECHARGE.getName()
        flowInfo.setMerchantWithdrawNo(merchantWithdrawNo);
        flowInfo.setWithdrawNo(withdrawNo);
        flowInfo.setLabel(label);     //渠道分组标识
        flowInfo.setAmt(amt); //充值总额
        flowInfo.setFee(platformProfit);   //平台分成 platformAmt  **********
        flowInfo.setAgentFee(agentProfit); //代理商总分成 ***********
        flowInfo.setCreditAmt(amtCredit); //实际入帐金额
        flowInfo.setChannelCost(channelCostAmt); //通道成本
        //flowInfo.setCreateTime(new Date());
        flowInfo.setRemark(StringUtils.isBlank(remark) ? managerName : remark);
        flowInfo.setManagerName(managerName);
        flowInfo.setChannelName(withdrawChannelInfo.getChannelName());
        flowInfo.setChannelCode(withdrawChannelInfo.getChannelCode());
        boolean isFlowChange = flowInfoService.save(flowInfo);
        if (!isFlowChange) {
            _log.error("=====>>>>>充值给商家:{},充值流水表写入{}失败", merchantId, amtCredit);
            //throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "退费流水表记录失败");
            throw new TranException(CodeConst.SYSTEM_ERROR_CODE, "充值给商家[" + merchantId + "],金额[" + amtCredit + "]失败");
        }

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("merchantId", merchantId); //商家自己
        PayMerchantMoneyChange latestRecord = iPayMerchantMoneyChangeService.selectLatestMoneyChangeForUpdate(params);
        //2.账变明细
        if (latestRecord == null) {

            PayMerchantMoneyChange addMoneyChangeEntity = new PayMerchantMoneyChange();
            addMoneyChangeEntity.setMerchantId(merchantId); //某一个商家
            addMoneyChangeEntity.setMerchantWithdrawNo(merchantWithdrawNo);
            addMoneyChangeEntity.setWithdrawNo(withdrawNo);
            addMoneyChangeEntity.setPayType(withdrawChannelInfo.getPayType());
            addMoneyChangeEntity.setFlowType(flowType); //充值类型
            addMoneyChangeEntity.setAmt(amt); // 提交充值金额
            addMoneyChangeEntity.setCreditAmt(amtCredit); //实际入账金额
            addMoneyChangeEntity.setAmtBefore(new BigDecimal(0)); //不同处在之前为0
            addMoneyChangeEntity.setAmtNow(amtCredit);

            //addMoneyChangeEntity.setCreateTime(new Date());
            addMoneyChangeEntity.setRemark(StringUtils.isBlank(remark) ? managerName : remark);
            addMoneyChangeEntity.setChannelName(withdrawChannelInfo.getChannelName());
            addMoneyChangeEntity.setChannelCode(withdrawChannelInfo.getChannelCode());

            boolean isAddMoneyChangeEntity = iPayMerchantMoneyChangeService.save(addMoneyChangeEntity);

            if (!isAddMoneyChangeEntity) {
                throw new TranException(CodeConst.SYSTEM_ERROR_CODE, "充值失败:插入流水表失败");
            }

        } else {
//
//            PayMerchantMoneyChange addMoneyChangeEntity = new PayMerchantMoneyChange();
//            addMoneyChangeEntity.setMerchantId(merchantId); //某一个商家
//            addMoneyChangeEntity.setAmt(merchantRechargeBalance); // 充值 商户款项增加 实际入账金额
//
//            //1.插入流水表
//            Integer latestRecordId = iPayMerchantMoneyChangeService.insertLatestMoneyChangeBySelect(addMoneyChangeEntity);
//            _log.info("充值流水表时获取插入的用户id:{}", addMoneyChangeEntity.getId());
//            if (latestRecordId != 1) {
//                throw new TranException(CodeConst.SYSTEM_ERROR_CODE, "充值失败:充值流水插入失败");
//            }
//            //2.获取插入的用户
//            PayMerchantMoneyChange latestRecordPayMerchant = iPayMerchantMoneyChangeService.getById(addMoneyChangeEntity.getId());
//            //更新时间
             _log.info("商家充值流水记录===>>>商家:{},账变前:{},账变后:{}", merchantId, latestRecord.getAmtNow(), latestRecord.getAmtNow().add(amtCredit));

            //3.记录MONEY_CHANGE 下发流水
            PayMerchantMoneyChange moneyChangeEntity = new PayMerchantMoneyChange();
            moneyChangeEntity.setMerchantId(merchantId);
            moneyChangeEntity.setPayType(withdrawChannelInfo.getPayType());
            moneyChangeEntity.setFlowType(flowType); //充值状态
            moneyChangeEntity.setMerchantWithdrawNo(merchantWithdrawNo);
            moneyChangeEntity.setWithdrawNo(withdrawNo);
            moneyChangeEntity.setAmt(amt); //提交充值金额
            moneyChangeEntity.setCreditAmt(amtCredit); //实际入账金额
            moneyChangeEntity.setAmtBefore(latestRecord.getAmtNow());
            moneyChangeEntity.setAmtNow(latestRecord.getAmtNow().add(amtCredit));

           // moneyChangeEntity.setCreateTime(new Date());
            moneyChangeEntity.setRemark(StringUtils.isBlank(remark) ? managerName : remark);
            moneyChangeEntity.setChannelName(withdrawChannelInfo.getChannelName());
            moneyChangeEntity.setChannelCode(withdrawChannelInfo.getChannelCode());
            boolean isSaveMoneyChange = iPayMerchantMoneyChangeService.save(moneyChangeEntity);

            if (!isSaveMoneyChange) {
                throw new TranException(CodeConst.SYSTEM_ERROR_CODE, "充值给商家[" + merchantId + "],金额[" + amtCredit + "]失败");
            }

        }

    }


    @Override
    @Transactional(rollbackFor = Exception.class) //充值回滚
    @SuppressWarnings({"deprecation"})
    public void merchantFeeRecharge(BigDecimal rechargeAmt,
                                 String withdrawNo,
                                 String merchantId,
                                 String merchantName,
                                 String remark,
                                 String managerName) throws TranException {

        //1.当前商家信息
        PayMerchantInfo currentMerchantInfo = iPayMerchantInfoService.getOne(new QueryWrapper<PayMerchantInfo>().lambda().eq(PayMerchantInfo::getMerchantId, merchantId));
        if (currentMerchantInfo == null){
            throw new TranException(CodeConst.SYSTEM_ERROR_CODE, "商户ID不存在");
        }
        //3.记录通道手续费流水表
        //通道手续费充值 ==> (充值)金额 ,流水类型, 充值类别 ,下挂卡流水号,通道流水号,通道编号 管理员名称 备注 --》 通道不再充值手续费 改为商户充值手续费
        //目前商户有多个不同类别的通道
        coinChannelManagerService.alterCoinChannelBalanceService(
                merchantId,
                rechargeAmt,
                CodeConst.CardFlowTypeConst.RECHARGE,
                withdrawNo,
                withdrawNo,
                "",
                merchantName,
               "",
                remark
        );
    }

    /**
     * @ API提现 服务实现
     * @ 加入事务  锁表 确保商家余额更新完毕
     */
    @Override
    @Transactional(rollbackFor = Exception.class) //得开启事务
    @SuppressWarnings("deprecation")
    public void batchWithdraw(PayMerchantInfo merchantInfo, PayMerchantChannelSite payMerchantChannelSite,
                              WithdrawChannelInfo channelInfo, List<PayWithdrawInfo> list, String type) throws TranException, InterruptedException {

        if (list == null || list.size() == 0) {
            throw new TranException(CodeConst.SYSTEM_ERROR_CODE, "代付数据不允许为空");
        }

        _log.info("【批量代付】 ---》商户编号:{},代付数据:{}", merchantInfo.getMerchantId(), list.toString());



        //统计下发的交易金额
        BigDecimal countRequestWithdrawAmt = new BigDecimal("0.00");



        for (PayWithdrawInfo info : list) {
            //强制暂停
            Thread.sleep(10);
            //平台订单号
            String withdrawNo = MySeq.getWithdrawNo();
            String MERCHANT_ID_WITHDRAW_NO = "";
            String MERCHANT_ID_WITHDRAW_ACCTNAME_AMT = "";

            // 金额守护
            if (!BigDecimalUtils.isNumeric(info.getAmt().toString())) {
                throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "非正确的金额格式:" + info.getAmt());
            }




            //以上所有条件满足后 再进行订单是否重复的判断
            if (StringUtils.isBlank(info.getMerchantWithdrawNo())) {
                MERCHANT_ID_WITHDRAW_NO = merchantInfo.getMerchantId() + MyRedisConst.SPLICE + withdrawNo.trim();
            }else {
                MERCHANT_ID_WITHDRAW_NO = merchantInfo.getMerchantId() + MyRedisConst.SPLICE + info.getMerchantWithdrawNo().trim();
            }
            //新增REDIS守护，直到数据库已填加订单，再清空REDIS,30秒后自动清空缓存
            Object merchantWithdrawToken = redisUtil.get(MERCHANT_ID_WITHDRAW_NO);
            if (merchantWithdrawToken != null) {
                throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "商户订单号["+info.getMerchantWithdrawNo().trim()+"]已存在缓存中,请核实交易流水,稍候重试");
            } else {
                // 没有token则当做首次/二次提交，记录在cache 30L秒后过期 以防队列无插入数据库
                redisUtil.set(MERCHANT_ID_WITHDRAW_NO, MERCHANT_ID_WITHDRAW_NO, 30L);
            }

            //新增REDIS守护，3s内不可提交相同的订单
            MERCHANT_ID_WITHDRAW_ACCTNAME_AMT = merchantInfo.getMerchantId() + MyRedisConst.SPLICE + info.getAcctAddr() +  MyRedisConst.SPLICE  + info.getAmt();
            Object merchantAmtToken = redisUtil.get(MERCHANT_ID_WITHDRAW_ACCTNAME_AMT);
            if (merchantAmtToken != null) {
                throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "下单失败:为了避免有异议,30秒内无法提交相同收款账号["+info.getAcctAddr()+"]相同金额["+info.getAmt()+"]的订单,请修改金额后重试!");
            } else {
                // 3L 3秒后自动过期
                boolean isSet = redisUtil.set(MERCHANT_ID_WITHDRAW_ACCTNAME_AMT, MERCHANT_ID_WITHDRAW_ACCTNAME_AMT, 30L);
                if (!isSet) {
                    throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "[" + MERCHANT_ID_WITHDRAW_ACCTNAME_AMT + "]缓存插入失败,请稍候重试");
                }
                _log.warn("Redis缓存插入成功===>>> 商户/订单:{},缓存:{}",MERCHANT_ID_WITHDRAW_NO,MERCHANT_ID_WITHDRAW_ACCTNAME_AMT);
            }




            //商家订单号如果是空 则用平台订单号代替
            if (StringUtils.isBlank(info.getMerchantWithdrawNo())) {
                info.setMerchantWithdrawNo(withdrawNo);
            }
            //_log.info("【商户代付】===>>>商户号:{},商家订单号:{},代付金额:{},通道成本:{},代理分成:{},平台分成:{}", merchantInfo.getMerchantId(), info.getMerchantWithdrawNo(), info.getAmt(), channelInfo.getSingleCost(), agentProfit, platformProfit);
            //更新每一条代付数据
            //代付流水
            info.setAgentProfit(new BigDecimal(0));      //此处计算不准确
            info.setAcctAddr(info.getAcctAddr());//收款人钱包地址
            info.setMerchantId(merchantInfo.getMerchantId());
            info.setMerchantName(merchantInfo.getMerchantName());
            info.setWithdrawNo(withdrawNo);
            info.setWithdrawStatus(EnumWithdrawStatus.UNKNOWN.getName());
            info.setMerchantFee(new BigDecimal(0)); //单笔下发手续费
            info.setMerchantRateFee(new BigDecimal(0)); //下发手续费率 * 下发金额
            info.setChannelCode("");
            info.setChannelName("");
            info.setCreateTime(new Date());
            info.setDate(DateUtil.getDate());
            info.setRequestType(type);
            countRequestWithdrawAmt = countRequestWithdrawAmt.add(info.getAmt());
        }

        //********* FOR循环结束 *********

        /**
         * @先获取商户余额 - 再扣除商户余额 此处要事务锁表 防止脏读
         * @PayMerchantBalance merchantBalance = balanceService.getOne(new QueryWrapper<PayMerchantBalance>().lambda()
         * .eq(PayMerchantBalance::getMerchantId, merchantInfo.getMerchantId()));
         * @锁表更新为FOR UPDATE
         * @会等到commit后 下一个事务查询后才修改
         * */
        PayMerchantBalance merchantBalance = balanceService.selectOneByMerchantIDForUpdate(merchantInfo.getMerchantId());

        _log.info("【批量代付】商户余额---》商户ID:{},商户余额:{}", merchantInfo.getMerchantId(), merchantBalance.getAccountBalance());
        // 计算商户余额 总扣除金额 单笔代付手续费 * 代付笔数 + 代付金额
        BigDecimal countWithdrawFee = new BigDecimal(0);


        _log.info("【批量代付】代付手续费---》商户ID:{},代付手续费{},商户账户需扣除金额{}", merchantInfo.getMerchantId(), countWithdrawFee, countRequestWithdrawAmt.negate());


        _log.warn("商户[{}]下单前的余额:{}", merchantBalance.getMerchantId(), merchantBalance.getAccountBalance());

        if (BigDecimalUtils.greaterEqual(merchantBalance.getAccountBalance(), countRequestWithdrawAmt)) {

            //保存用户订单时发生插入失败 所有的列表都失败
            boolean flag = iPayWithdrawInfoService.saveBatch(list);

            if (!flag) {
                _log.error("【批量代付】插入批次失败,商户ID：{},插入数据{}", merchantInfo.getMerchantId(), list.toString());
                throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "批量代付执行失败");
            }
           /* 订单已生成
            如果没有主键则锁表 https://www.jb51.net/article/157930.htm
            预先扣除商户余额 但得等代付成功后 才分配给代理商利润
            如果预先扣除商户余额失败了 也不会有商家余额变动流水 所以修正订单的时候要先判断是否有代付流水后才可补余额
            如果错误 则回滚
            不可改为 null*/

            Integer index = balanceService.alterMerchantBalance(merchantInfo.getMerchantId(), countRequestWithdrawAmt.negate(), merchantBalance.getAccountBalance()); //不可改为null
            //生产环境为 !=1 事务强制更新
            if (index != 1) {
                _log.error("[批量代付]扣除商户余额失败,商户ID:{},原账户金额{},扣减金额{}", merchantInfo.getMerchantId(), merchantBalance.getAccountBalance(), countRequestWithdrawAmt.toString());
                throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "扣除商户余额失败,请重新提交");
            }

            PayMerchantBalance merchantBalanceAfter = balanceService.getOne(new QueryWrapper<PayMerchantBalance>().lambda()
                    .eq(PayMerchantBalance::getMerchantId, merchantInfo.getMerchantId()));
            _log.warn("商户[{}]下单后的余额:{}", merchantBalanceAfter.getMerchantId(), merchantBalanceAfter.getAccountBalance());


        } else {
            _log.info("【批量代付】账户余额不足,商户ID{}", merchantInfo.getMerchantId());
            throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "账户余额不足");
        }

        // 全部无误后  扣除余额后 发送提现队列到服务器 （一条一条排队执行）
        // 注意 记录商家金额-代付/下发流水表 jms有时会出错 造成流水插入不进去

        //问题：共有50条订单 ，其中3条插入流水时发生失败，3条无流水记录，47条有记录，要怎么办？
        //用队列一条一条解决，但注意有网络波动的因素，3条订单就提交失败了 成为未提交的状态
        //创建一个事务时  @@TRANCOUNT 加一
        //提交一个事务时  @@TRANCOUNT 减一
        //当 @@TRANCOUNT 为 0 时发出 COMMIT TRANSACTION 将会导致出现错误，因为没有相应的 BEGIN TRANSACTION

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
            @Override
            public void afterCommit() {
                //操作二
                //确保扣除商家提交的订单总余额 再一条一条记录流水
                PayMerchantBalance merchantBalanceCommit = balanceService.getOne(new QueryWrapper<PayMerchantBalance>().lambda()
                        .eq(PayMerchantBalance::getMerchantId, merchantInfo.getMerchantId()));
                //_log.warn("商户[{}]下单后的余额:{}", merchantBalanceCommit.getMerchantId(), merchantBalanceCommit.getAccountBalance());
                _log.warn("事务提交===>>>商家:{}下单后的余额{},发送一条一条提款流水", merchantBalanceCommit.getMerchantId(), merchantBalanceCommit.getAccountBalance());
                for (PayWithdrawInfo sendInfo : list) {
                    amProducerUtils.sendWithdrawHandleQueue(sendInfo);
                }
            }
        });
    }
    @Override
    //@Transactional(rollbackFor = Exception.class) //开启事务 后订单 毫秒数---》16567
    @SuppressWarnings("deprecation")
    public ReturnMsg singleDeposit(PayMerchantChannelSite selectedPayMerchantChannelInfo,
                                   WithdrawChannelInfo selectedChannelInfo,
                                   PayMerchantInfo merchantInfo,
                                   String selectedChannelCode,
                                   String merchantId,
                                   String merchantWithdrawNo,
                                   BigDecimal amtFormat, //提交的订单充值金额 元
                                   String clientIp,
                                   String extraParam,
                                   String merchantNotifyUrl,
                                   String merchantChannelCode,
                                   String coinType) throws Exception {
        //强制暂停
        Thread.sleep(10);

        String res = apiDeposit(
                selectedChannelCode,
                merchantId,
                merchantWithdrawNo,
                String.valueOf(amtFormat),
                clientIp,
                extraParam,
                merchantNotifyUrl,
                merchantChannelCode,
                coinType);
        JSONObject jsonObject = JSON.parseObject(res);
        _log.info("synchronized 返回结果 resJson - {}",jsonObject);

        if ("0000".equals(jsonObject.getString("code"))) {
            if (jsonObject.getString("data") != null) {
                return new ReturnMsg(JSON.parseObject(jsonObject.getString("data")));
            }else {
                return new ReturnMsg().setFail("请求通道失败:无码订单");
            }

        }else {
            return new ReturnMsg().setFail(jsonObject.getString("message"));
        }

    }


    public String apiDeposit(String _selectedChannelCode,
                                       String merchantId,
                                       String merchantWithdrawNo,
                                       String amt,
                                       String clientIp,
                                       String extraParam,
                                       String merchantNotifyUrl,
                                       String merchantChannelCode,
                                       String coinType) throws Exception {


        String productCode = merchantChannelCode;//支付产品通道号 8001

        if (!BigDecimalUtils.isNumeric(String.valueOf(amt))) {
            throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "非正确的金额格式:" + amt);
        }

        //1.商家订单号 30内不可以重复
        String REDIS_MERCHANT_ID_WITHDRAW_NO = "C:" + merchantId + MyRedisConst.SPLICE + merchantWithdrawNo.trim();
        Object merchantWithdrawToken = redisUtil.get(REDIS_MERCHANT_ID_WITHDRAW_NO);  //新增REDIS守护，直到数据库已填加订单，再清空REDIS,30秒后自动清空缓存
        if (merchantWithdrawToken != null) {
            throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "商户订单号[" + merchantWithdrawNo.trim() + "]已存在");
        } else {
            // 没有token则当做首次/二次提交，记录在cache 30L秒后过期 以防队列无插入数据库
            redisUtil.set(REDIS_MERCHANT_ID_WITHDRAW_NO, REDIS_MERCHANT_ID_WITHDRAW_NO, 30L);
        }

        //2.数据库查找是否已存在相同的商家订单号
        PayDepositInfo depositInfo = iPayDepositInfoService.getOne(new QueryWrapper<PayDepositInfo>().lambda().eq(PayDepositInfo::getMerchantWithdrawNo, merchantWithdrawNo));
        if (depositInfo != null) {
            throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "商户订单号重复");
        }

        //3.保留72小时 防止掉单后来查
        String robotMerchantWithdrawNo = "robot:".concat(merchantWithdrawNo.trim());
        redisUtil.set(robotMerchantWithdrawNo, robotMerchantWithdrawNo, 60 * 60 * 24 * 3);//72小时

        //4.获取三方上游代收通道信息
        WithdrawChannelInfo selectedChannelInfo = iWithdrawChannelInfoService.getOne(new QueryWrapper<WithdrawChannelInfo>().lambda()
                .eq(WithdrawChannelInfo::getChannelCode,_selectedChannelCode));

        //5.获取商户分配的通道
        PayMerchantChannelSite selectedPayMerchantChannelInfo = iPayMerchantChannelSiteService.getOne(new QueryWrapper<PayMerchantChannelSite>()
                .lambda()
                .eq(PayMerchantChannelSite::getMerchantId, merchantId)
                .eq(PayMerchantChannelSite::getProductCode,productCode)
                .eq(PayMerchantChannelSite::getPayType, CodeConst.PayType.RECEIVE_IN)
                .eq(PayMerchantChannelSite::getChannelCode,_selectedChannelCode)
        );
        //6商户信息
        PayMerchantInfo merchantInfo = iPayMerchantInfoService.getOne(new QueryWrapper<PayMerchantInfo>().lambda().eq(PayMerchantInfo::getMerchantId, merchantId));

//        try {
            if (DepositChannelCode.CHANNEL_CODE_ALIPAY_POOL.equals(_selectedChannelCode) || DepositChannelCode.CHANNEL_CODE_ALIPAY_POOL_ZZM.equals(_selectedChannelCode)){
                return alipayPool(selectedPayMerchantChannelInfo,
                        selectedChannelInfo,
                        merchantInfo,
                        _selectedChannelCode,
                        merchantId,merchantWithdrawNo,
                        new BigDecimal(amt),
                        extraParam,
                        merchantNotifyUrl,
                        merchantChannelCode,
                        coinType);
            } else {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("code",CodeConst.BUSINESS_ERROR_CODE);
                jsonObject.put("message","支付产品通道不存在");
                jsonObject.put("data",null);
                return jsonObject.toJSONString();

            }

//        } catch (Exception  e) {
//            JSONObject jsonObject = new JSONObject();
//            jsonObject.put("code",CodeConst.BUSINESS_ERROR_CODE);
//            jsonObject.put("message","支付产品通道出错:"+ e.getMessage());
//            jsonObject.put("data",null);
//            return jsonObject.toJSONString();
//        }
    }

    public  String alipayPool(PayMerchantChannelSite selectedPayMerchantChannelInfo,
                             WithdrawChannelInfo selectedChannelInfo,
                             PayMerchantInfo merchantInfo,
                             String _selectedChannelCode,
                             String merchantId,
                             String merchantWithdrawNo,
                             BigDecimal amtFormat, //提交的订单充值金额 元
                             String extraParam,
                             String merchantNotifyUrl,
                             String merchantChannelCode,String coinType) throws Exception {

        // 3.判断三方代收通道是否存在
        CoinChannelDepositInfo coinChannelDepositInfo = iCoinChannelDepositInfoService.getOne(new QueryWrapper<CoinChannelDepositInfo>()
                .lambda().eq(CoinChannelDepositInfo::getChannelCode, selectedPayMerchantChannelInfo.getChannelCode()));
        if (coinChannelDepositInfo == null) {
//                throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "三方代收通道[" + selectedPayMerchantChannelInfo.getChannelCode() + "]不存在");
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("code",CodeConst.BUSINESS_ERROR_CODE);
            jsonObject.put("message","上游通道不存在");
            jsonObject.put("data",null);
            return jsonObject.toJSONString();
        }
        //周转码无浮动 | 个人固码有浮动(因固码有金额范围 所以只有相同金额 相同卡时才进行 金额变动)
        BigDecimal amtBonus = new BigDecimal(0);
        //如果不是周转码则要金额浮动
        String businessType =""; //CodeConst.BusinessType.PERSON;
        if (selectedPayMerchantChannelInfo.getChannelCode().equals(DepositChannelCode.CHANNEL_CODE_ALIPAY_POOL_ZZM)) {
            businessType = CodeConst.BusinessType.BUSINESS;   //周转码
        }else {
            businessType = CodeConst.BusinessType.PERSON;
        }
        //4.通道是否分配了收款钱包
        //TODO:码商的上码额度若已满 则所属的所有码不再参与收款
        //long beginMills = System.currentTimeMillis();
        _log.info("amtFormat - {}",amtFormat);
        Map<String, Object> paramsHolder = new HashMap<String, Object>();
        paramsHolder.put("channelCode", selectedPayMerchantChannelInfo.getChannelCode());
        paramsHolder.put("coinType",coinType);// 0-微 1-支 2-聚
        paramsHolder.put("businessType",businessType);
        paramsHolder.put("payerCodePrice",amtFormat);//商家码有固定金额（可收款的面额 金额已格式化），个人码金额不限制，如:300-1000之间的任意金额
        paramsHolder.put("payerCodeShowTimes",1);//可拉单次数 >=1
        paramsHolder.put("payerCodeTimes",1);//可收款次数 >=1
        paramsHolder.put("status", CodeConst.PayerCardStatusConst.CARD_STATUS_BELONG);
        paramsHolder.put("accountBalance",0);//码商的余额应 >0
        paramsHolder.put("withdrawStatus",EnumWithdrawStatus.UNKNOWN.getName());//订单状态

        CoinHolderInfo lastCoinHolderInfo = new CoinHolderInfo();
        List<CoinHolderInfo> coinHolderList = iCoinHolderInfoService.queryCoinHolderListAllowDeposit(paramsHolder);
        _log.info("businessType - {}",businessType);
        //long endMills = System.currentTimeMillis();
       // _log.info("服务器获取CoinHolderInfo相差秒数:{}",(endMills - beginMills) / 1000);

        if (coinHolderList == null || coinHolderList.size() == 0) {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("code",CodeConst.BUSINESS_ERROR_CODE);
            jsonObject.put("message","无码订单/E");//E -- empty
            jsonObject.put("data",null);
            return jsonObject.toJSONString();
        }


        //如果是个人码，只有要缓存数据
        //coinHolderList可分配的卡 -- 如何设置可收款范围 (300-1000)任意金额
        if (!selectedPayMerchantChannelInfo.getChannelCode().equals(DepositChannelCode.CHANNEL_CODE_ALIPAY_POOL_ZZM)) {

            ArrayList<String> coinHolderWalletAllowList = new ArrayList<>();
            ArrayList<Integer> coinHolderWalletAllowWeight = new ArrayList<>();
            for (CoinHolderInfo holderItem : coinHolderList) {
                coinHolderWalletAllowList.add(holderItem.getPayerAddr());//收款人钱包地址GCash
                coinHolderWalletAllowWeight.add(Integer.valueOf(holderItem.getCoinWeight()));//收款钱包权重
            }
            WeightedRoundUtil weightedRoundUtilCoinHolder = new WeightedRoundUtil(coinHolderWalletAllowList,coinHolderWalletAllowWeight);
            String selectedCoinHolderPayerAddr = weightedRoundUtilCoinHolder.getNextChannel();
            //最后得出的二维码
            lastCoinHolderInfo = iCoinHolderInfoService.getOne(new QueryWrapper<CoinHolderInfo>().lambda().eq(CoinHolderInfo::getPayerAddr, selectedCoinHolderPayerAddr));

        }else {

            QueryWrapper<PayDepositInfo> queryWrapper = new QueryWrapper<>();
            Map<String,Object> params = new HashMap<String,Object>();
            params.put("AMT_PAY", amtFormat);
            params.put("WITHDRAW_STATUS", EnumWithdrawStatus.UNKNOWN.getName());
            queryWrapper = queryWrapper.allEq(params, false)
                    .eq("AMT_PAY",amtFormat)
                    .eq("WITHDRAW_STATUS",EnumWithdrawStatus.UNKNOWN.getName()).eq("CHANNEL_CODE",_selectedChannelCode); //忽略value为null 值为null空 则忽略
            List<PayDepositInfo> depositInfoList = iPayDepositInfoService.list(queryWrapper); //所有 个人码 299.97，但如果是个码，一个码可以生成多个不同的金额，
            ArrayList<String> payerAddrList = new ArrayList<>();
            for (PayDepositInfo payerAddrItem : depositInfoList) {
                payerAddrList.add(payerAddrItem.getPayerAddr());
            }
            //_log.info("正在支付的二维码: - {}",payerAddrList);

            //FIXME:2.查询是否有支付 非常耗资源
            long beginMillsUnpaid = System.currentTimeMillis();

            ArrayList<String> coinHolderWalletAllowList = new ArrayList<>();
            ArrayList<Integer> coinHolderWalletAllowWeight = new ArrayList<>();

            //WARNING:禁止用FOR loop 再查询 sql语句 会导致 mysql慢查询
            for (CoinHolderInfo holderItem : coinHolderList) {
                //   PayDepositInfo unPaidInfo = iPayDepositInfoService.getOne(new QueryWrapper<PayDepositInfo>().lambda()
//                    .eq(PayDepositInfo::getAmtPay, amtPay)
//                    .eq(PayDepositInfo::getPayerAddr,holderItem.getPayerAddr())
//                    .eq(PayDepositInfo::getWithdrawStatus,EnumWithdrawStatus.UNKNOWN.getName())
//            );
//          if (unPaidInfo == null) {
//                coinHolderWalletAllowList.add(holderItem.getPayerAddr());//收款人钱包地址GCash
//                coinHolderWalletAllowWeight.add(Integer.valueOf(holderItem.getCoinWeight()));//收款钱包权重
//          }
                //添加 coinHolderList 不包含 (299.97 未支付的二维码)
                if (!payerAddrList.contains(holderItem.getPayerAddr())) {
                    coinHolderWalletAllowList.add(holderItem.getPayerAddr());//收款人钱包地址GCash
                    coinHolderWalletAllowWeight.add(Integer.valueOf(holderItem.getCoinWeight()));//收款钱包权重
                }
            }
            long endMillsUnpaid = System.currentTimeMillis();
            _log.info("查询是否有未支付订单时间,相差:{}",(endMillsUnpaid - beginMillsUnpaid) / 1000);

            //充值金额 满足条件的记录
            if (coinHolderWalletAllowList.size() == 0) {
                System.out.println("coinHolderWalletAllowList为空,无码订单.");
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("code",CodeConst.BUSINESS_ERROR_CODE);
                jsonObject.put("message","无码订单/E");
                jsonObject.put("data",null);
                return jsonObject.toJSONString();
            }

            WeightedRoundUtil weightedRoundUtilCoinHolder = new WeightedRoundUtil(coinHolderWalletAllowList,coinHolderWalletAllowWeight);
            String selectedCoinHolderPayerAddr = weightedRoundUtilCoinHolder.getNextChannel();
            //最后得出的二维码
             lastCoinHolderInfo = iCoinHolderInfoService.getOne(new QueryWrapper<CoinHolderInfo>().lambda().eq(CoinHolderInfo::getPayerAddr, selectedCoinHolderPayerAddr));
        }
        //7.查询未成功的订单列表里是否有相同的金额 ***** 防止回调到其他的付款人
        //Object locked = new Object();
        synchronized (this){
            String redisKey = lastCoinHolderInfo.getPayerAddr() + MyRedisConst.SPLICE + amtFormat;
            Object redisUnpaid = redisUtil.get(redisKey);

            PayDepositInfo unpaidDepositInfo = iPayDepositInfoService.getOne(new QueryWrapper<PayDepositInfo>().lambda()
                    .eq(PayDepositInfo::getAmtPay, amtFormat)
                    .eq(PayDepositInfo::getPayerAddr,lastCoinHolderInfo.getPayerAddr())
                    .eq(PayDepositInfo::getWithdrawStatus,EnumWithdrawStatus.UNKNOWN.getName())
            );
            if (redisUnpaid != null || unpaidDepositInfo != null ) {
                amtBonus = new BigDecimal(Math.random() / 10).setScale(2, BigDecimal.ROUND_HALF_UP);//0.01-0.09
                if (amtBonus.equals(new BigDecimal(0))) {
                    amtBonus = new BigDecimal(0.01);
                }
            }
            _log.info("merchantWithdrawNo: {},payerAddr:{},bonus:{}",merchantWithdrawNo,lastCoinHolderInfo.getPayerAddr(),amtBonus);
            //6.查看300秒内 是否有收款钱包内有相同的收款金额 若有则修改BONUS以便区分不同的收款金额
            // 商家码 getPayerAddr== 预先上传的代付订单编号outTradeNo，而非随机的 payerAddr
            BigDecimal amtPay = amtFormat.subtract(amtBonus).setScale(2, BigDecimal.ROUND_HALF_UP);
            String redis_payerAddr_amt = lastCoinHolderInfo.getPayerAddr() + MyRedisConst.SPLICE + amtPay;

            if (redisUtil.get(redis_payerAddr_amt) != null) {
                _log.error("无码订单 redis - {}",redisUtil.get(redis_payerAddr_amt));
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("code",CodeConst.BUSINESS_ERROR_CODE);
                jsonObject.put("message","无码订单/R");//redis有缓存
                jsonObject.put("data",null);
                return jsonObject.toJSONString();
            };

            PayDepositInfo unPaidInfo = iPayDepositInfoService.getOne(new QueryWrapper<PayDepositInfo>().lambda()
                    .eq(PayDepositInfo::getAmtPay, amtPay)
                    .eq(PayDepositInfo::getPayerAddr,lastCoinHolderInfo.getPayerAddr())
                    .eq(PayDepositInfo::getWithdrawStatus,EnumWithdrawStatus.UNKNOWN.getName())
            );
            if (unPaidInfo != null) {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("code",CodeConst.BUSINESS_ERROR_CODE);
                jsonObject.put("message","无码订单/M");//MYSQL 有数据
                jsonObject.put("data",null);
                return jsonObject.toJSONString();
            }

            //8.更新二维码被拉单的次数
            CoinHolderInfo updCoinHolder = new CoinHolderInfo();
            updCoinHolder.setPayerCodeShowTimes(lastCoinHolderInfo.getPayerCodeShowTimes() -  1); //扣除拉单次数
            boolean showCodeTimes = iCoinHolderInfoService.update(updCoinHolder, new QueryWrapper<CoinHolderInfo>().lambda().in(CoinHolderInfo::getPayerAddr, lastCoinHolderInfo.getPayerAddr()));
            if (!showCodeTimes) {
                throw new TranException(CodeConst.BUSINESS_ERROR_CODE,"更新二维码提单次数有误");
            }
            //设置缓存
            //TODO:是否在收款次数为0时也设为禁用?运营后再做相应调整
            int timeTTL =  60 * 10; //订单10分钟才失效
            redisUtil.set(redis_payerAddr_amt, redis_payerAddr_amt, timeTTL);

            //9.获取通道信息
            WithdrawChannelInfo withdrawChannelInfo = iWithdrawChannelInfoService.getOne(new QueryWrapper<WithdrawChannelInfo>().lambda().eq(WithdrawChannelInfo :: getChannelCode, selectedPayMerchantChannelInfo.getChannelCode()));

            //10.加入代收订单表 PayDepositInfo
            String withdrawNo = MySeq.getDepositNo(); //平台订单号
            //String channelWithdrawNo = "C" + DateUtil.format(new Date(), DateUtil.FULL_TIME_FORMAT_EN) + "0" + System.currentTimeMillis();  //上游三方通道订单号
            String channelWithdrawNo = ""; //还无上游支付宝+微信的订单号
            //商户的手续费计算
            //代收手续费
            BigDecimal merchantRateFee =  amtFormat.divide(new BigDecimal("100")).multiply(new BigDecimal(selectedPayMerchantChannelInfo.getMerchantRate())).setScale(2, BigDecimal.ROUND_HALF_UP);
            //单笔手续费
            BigDecimal merchantSingleFee = selectedPayMerchantChannelInfo.getSinglePoundage(); //单笔
            //商户入账金额
            BigDecimal amtCredit = amtFormat.add(merchantRateFee.negate()).add(merchantSingleFee.negate()); //余额扣除的方式进行结算

            PayDepositInfo payDepositInfo = new PayDepositInfo();
            payDepositInfo.setMerchantId(merchantId);
            payDepositInfo.setMerchantName(merchantInfo.getMerchantName());
            payDepositInfo.setWithdrawStatus(CodeConst.WithdrawStatusConst.WITHDRAW_UNKNOWN); //订单未知
            payDepositInfo.setMerchantWithdrawNo(merchantWithdrawNo);//商户订单号
            payDepositInfo.setWithdrawNo(withdrawNo);//订单号
            payDepositInfo.setChannelWithdrawNo(channelWithdrawNo); //上游通道流水号 coin_holder_deposit_info

            payDepositInfo.setRequestType(EnumRequestType.API.getName()); //API提交
            payDepositInfo.setMerchantNotifyUrl(merchantNotifyUrl); //异步

            payDepositInfo.setPayerName(lastCoinHolderInfo.getPayerName()); //收款人
            payDepositInfo.setPayerAddr(lastCoinHolderInfo.getPayerAddr());//钱包的二维码固码链接地址 (得区分是1-微信、2-支付宝、3-聚合码)
            payDepositInfo.setMerchantRateFee(merchantRateFee); //下发费率
            payDepositInfo.setMerchantFee(merchantSingleFee);

            payDepositInfo.setAmt(amtFormat); //订单金额
            payDepositInfo.setAmtBonus(amtBonus);//优惠
            payDepositInfo.setAmtPay(amtPay);  //玩家实际应支付的金额
            payDepositInfo.setAmtCredit(amtCredit); //返回商家的收入

            payDepositInfo.setChannelCode(selectedPayMerchantChannelInfo.getChannelCode());
            payDepositInfo.setChannelName(withdrawChannelInfo.getChannelName());
            payDepositInfo.setChannelFeeType(selectedPayMerchantChannelInfo.getChannelFeeType()); //1-余额扣除 2-分开结算
            payDepositInfo.setExtraParam(extraParam);
            payDepositInfo.setCreateTime(new Date());
            payDepositInfo.setDate(DateUtil.getDate());

            boolean isSavePayDepositInfo = iPayDepositInfoService.save(payDepositInfo); //新增商户订单表
            if (!isSavePayDepositInfo){
//                throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "订单生成失败");
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("code",CodeConst.BUSINESS_ERROR_CODE);
                jsonObject.put("message","订单生成失败");
                jsonObject.put("data",null);
                return jsonObject.toJSONString();
            }

            /**
             * 只是订单
             * 并不代表支付成功 所有没有订单账变流水 只有成功的订单才插入流水
             *
             * */
            Date createTime = new Date();

            CoinHolderDepositInfo deposit =  new CoinHolderDepositInfo();
            deposit.setWithdrawNo(withdrawNo);
            deposit.setChannelWithdrawNo(channelWithdrawNo);
            deposit.setStatus(CodeConst.WithdrawStatusConst.WITHDRAW_UNKNOWN); //待支付
            deposit.setMerchantId(merchantId);

            deposit.setPayerMerchantId(lastCoinHolderInfo.getMerchantId());//码商
            deposit.setPayerOperatorId(lastCoinHolderInfo.getPayerOperatorId());//码商操作员
            deposit.setPayerIdentity(lastCoinHolderInfo.getPayerIdentity());//收款人

            deposit.setAmt(amtFormat);
            deposit.setAmtBonus(amtBonus);
            deposit.setAmtPay(amtFormat.subtract(amtBonus));
            deposit.setAmtCredit(amtCredit);

            deposit.setPayerName(lastCoinHolderInfo.getPayerName());//收款人名称
            deposit.setPayerAddr(lastCoinHolderInfo.getPayerAddr());//收款人二维码ID

            deposit.setAlipayUid(lastCoinHolderInfo.getAlipayUid());//支付宝UID

            deposit.setPayerRateFee(merchantRateFee);
            deposit.setPayerSingleFee(merchantSingleFee);

            deposit.setNotifyUrl(merchantNotifyUrl);
            deposit.setCoinType(coinType); //
            deposit.setChannelCode(selectedPayMerchantChannelInfo.getChannelCode());
            deposit.setChannelName(withdrawChannelInfo.getChannelName());
            deposit.setChannelFeeType(selectedPayMerchantChannelInfo.getChannelFeeType());
            deposit.setTaskType(CodeConst.TaskTypeConst.TRANSFER_TO_CARD); //打款
            deposit.setTaskStartTime(createTime);
            deposit.setCreateTime(createTime);
            deposit.setDate(DateUtil.getDate());
            boolean isSaveDeposit = iCoinHolderDepositInfoService.save(deposit);

            if (!isSaveDeposit){
//                throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "通道订单生成失败");
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("code",CodeConst.BUSINESS_ERROR_CODE);
                jsonObject.put("message","通道订单生成失败");
                jsonObject.put("data",null);
                return jsonObject.toJSONString();
            }

            //0-trx 10分钟有效 1-ETH 20分钟有效 REDIS 钱包地址:商户单号:金额:时间戳:平台订单号
            String redis_payUrl_key = "";
            String redis_payUrl_key_encode = "";
            String payerUrlValue = ""; //返回给前端的支付链接
            String encode_payerUrlValue = "";
            String timestamp =  "" + System.currentTimeMillis();
            //收款钱包地址:商户单号:平台订单号:订单金额:优惠立减:实付金额:timestamp:收款人名称
            payerUrlValue = lastCoinHolderInfo.getPayerAddr()
                    + MyRedisConst.SPLICE + merchantWithdrawNo
                    + MyRedisConst.SPLICE + withdrawNo
                    + MyRedisConst.SPLICE + amtFormat
                    + MyRedisConst.SPLICE + amtBonus
                    + MyRedisConst.SPLICE + String.valueOf(amtPay)
                    + MyRedisConst.SPLICE +  timestamp
                    + MyRedisConst.SPLICE +  lastCoinHolderInfo.getPayerName()
                    + MyRedisConst.SPLICE +  coinType
                    + MyRedisConst.SPLICE + _selectedChannelCode
                    + MyRedisConst.SPLICE + businessType
            ;


            MessageDigest md = MessageDigest.getInstance("md5");
            byte md5[] =  md.digest(payerUrlValue.getBytes());
            encode_payerUrlValue = Base64.getEncoder().encodeToString(md5);
            redis_payUrl_key_encode = redisPayUrl.concat(URLEncoder.encode(encode_payerUrlValue,"utf-8")); //加密 防止伪造链接
            redis_payUrl_key = redisPayUrl.concat(encode_payerUrlValue); //加密 防止伪造链接

            redisUtil.set(redis_payUrl_key,payerUrlValue,timeTTL);

            String apiKeyDecrypt = EncryptUtil.aesDecrypt(merchantInfo.getApiKey(),walletPrivateKeySalt); //商户的API KEY要解密

            //参数全部为字符串
            Map<String, Object> resultMap = new HashMap<String, Object>();

            resultMap.put("merchantWithdrawNo", merchantWithdrawNo);//商家订单号
            resultMap.put("withdrawNo", withdrawNo);//商家订单号
            resultMap.put("merchantId", merchantId);
            resultMap.put("amt", String.valueOf(amtFormat)); //订单金额

//            resultMap.put("amtBonus",String.valueOf(amtBonus));//随机优惠立减
            resultMap.put("amtPay", String.valueOf(amtFormat.subtract(amtBonus))); //入账金额(玩家付款此金额后自动回调)

//            resultMap.put("amtCredit", String.valueOf(amtCredit)); //商户入账金额
//            resultMap.put("merchantRateFee",String.valueOf(merchantRateFee));
//            resultMap.put("merchantSingleFee", String.valueOf(merchantSingleFee));
            resultMap.put("withdrawStatus", EnumWithdrawStatus.UNKNOWN.getName()); //待支付
            resultMap.put("coinType",lastCoinHolderInfo.getCoinType());//1-微信 2-支付宝 3-聚合码
            resultMap.put("extraParam", extraParam); //商家上传的拓展参数
//            resultMap.put("payerName", lastCoinHolderInfo.getPayerName()); //收款人名称
            resultMap.put("payerUrl",redis_payUrl_key_encode); //支付链接 生产订单的时候
            resultMap.put("merchantNotifyUrl", merchantNotifyUrl); //回调网址不空则参与验签
            resultMap.put("sign", Signature.getSign(resultMap, apiKeyDecrypt));

            JSONObject jsonObject = new JSONObject();
            jsonObject.put("code",CodeConst.SUCCESS_CODE);
            jsonObject.put("message","正常");
            jsonObject.put("data",JSON.toJSONString(resultMap));

            return jsonObject.toJSONString();

         }


    }






}
