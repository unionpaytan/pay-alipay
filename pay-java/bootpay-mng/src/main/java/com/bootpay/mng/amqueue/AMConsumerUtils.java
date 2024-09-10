package com.bootpay.mng.amqueue;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;

import com.bootpay.channel.constants.DepositChannelCode;
import com.bootpay.channel.constants.WithdrChannelCode;

import com.bootpay.channel.vo.ChannelResultVo;
import com.bootpay.common.constants.CodeConst;
import com.bootpay.common.constants.MyAmQueueName;
import com.bootpay.common.constants.MyRedisConst;
import com.bootpay.common.constants.enums.EnumFlowType;
import com.bootpay.common.constants.enums.EnumRequestType;
import com.bootpay.common.constants.enums.EnumWithdrawStatus;
import com.bootpay.common.excetion.TranException;
import com.bootpay.common.framework.RedisUtil;
import com.bootpay.common.utils.*;
import com.bootpay.core.entity.*;
import com.bootpay.core.service.*;
import com.bootpay.mng.service.AuthenticatorService;
import com.bootpay.mng.service.NotifyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.jms.Session;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 消费者
 *
 * @author Administrator
 */
@Service
public class AMConsumerUtils {

    private Logger _log = LoggerFactory.getLogger(AMConsumerUtils.class);

    @Value("${app.receiverMailAddress}")
    private String receiverMailAddress;

    @Value("${app.redisPayUrl}") //application.yml文件读取
    private String redisPayUrl;

    @Autowired
    private AuthenticatorService authenticatorService;

    @Autowired
    private IPayWithdrawInfoService iPayWithdrawInfoService;

    @Autowired
    private RedisUtil redisUtil;
    @Autowired
    private IPayWithdrawInfoService infoService;
    @Autowired
    private NotifyService notifyService;
    @Autowired
    private AMProducerUtils amProducerUtils;
    @Autowired
    private IPayMerchantBalanceService balanceService;
    @Autowired
    private IPayPlatformIncomeInfoService incomeInfoService;

    @Autowired
    private IPayMerchantMoneyChangeService iPayMerchantMoneyChangeService; //商户流水明细

    @Autowired
    private IPayMerchantChannelSiteService iPayMerchantChannelSiteService; //商户通道成本服务

    @Autowired
    private IPayDepositInfoService iPayDepositInfoService;

    @Autowired
    private IWithdrawChannelInfoService iWithdrawChannelInfoService;

    @Autowired
    private IPayMerchantInfoService iPayMerchantInfoService;

    @Autowired
    private ICoinChannelDepositInfoService iCoinChannelDepositInfoService;

    @Autowired
    private ICardPayerInfoService iCardPayerInfoService;

    @Autowired
    private ICoinHolderDepositInfoService iCoinHolderDepositInfoService;

    @Autowired
    private ICoinHolderInfoService iCoinHolderInfoService;

    @Autowired
    private IPayAgentBalanceService  iPayAgentBalanceService;

    /**
     * 使用@JmsListener注解来监听指定的某个队列内的消息,是否有新增,有的话则取出队列内消息进行处理
     **/
    //activeMQ Queue sendMail receive message:
    // {"mailType":"modifyPwd",
    // "mailbox":"chan@gmail.com",
    // "merchantId":"S1587642832242500341",
    // "merchantName":"安杰鲁"}
    @JmsListener(destination = MyAmQueueName.SEND_MAIL_QUEUE_NAME)
    public void receiveSendMail(String message) throws TranException {
        _log.info("接收到邮箱队列的消息");
        _log.info("activeMQ Queue sendMail receive message:{}", message);
        JSONObject mailJson = JSONObject.parseObject(message);
        // 发送登录验证码
       try {
            authenticatorService.sendMailCode(mailJson.getString("merchantId"),mailJson.getString("merchantName"),mailJson.getString("mailbox"),mailJson.getString("mailType"));
		} catch (TranException e) {
			e.printStackTrace();
			_log.error("activeMQ sendMailQueue error{}",e.getMessage());

		}
    }

    /**
     * 代付处理队列
     * listener出现监听失败 抛出为null
     *
     * @param
     * @param
     */
    @JmsListener(destination = MyAmQueueName.WITHDRAW_QUEUE_NAME)
    public void withdrawHandleQueue(String withdrawInfoJson){
        _log.info("activeMQ Queue withdrawHandleQueue receive message{}", withdrawInfoJson);
        PayWithdrawInfo payWithdrawInfo = JSONObject.parseObject(withdrawInfoJson, PayWithdrawInfo.class);

        //没有采用事务锁表了 数据量过大  PayMerchantMoneyChange 获取商户流水表准确的余额数据
        Integer indexGetLatestMoneyRecord = getLatestMoneyRecord(payWithdrawInfo);
        if (indexGetLatestMoneyRecord != 1){ //正式环境为 != 1
            //表示没有获得流水 退出当前queue 让订单不请求三方服务器
            return;
        }
        /**
         * @新增判断 如果数据库中 30秒内已经有相同的 收款人+相同金额，则提示失败，不再让请求三方的数据库
         * 从activemq队列内进行判断 redis缓存没有用强同步化 有可能造成相同金额绕过检测
         * */

        //商家订单号如果重复 禁止提交
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.SECOND, -30); // 30秒种前的时间
        String howManySecondsAgo = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(calendar.getTime());

        QueryWrapper<PayWithdrawInfo> queryWrapper = new QueryWrapper<>();
        Map<String,Object> params = new HashMap<String,Object>();
        params.put("MERCHANT_ID", payWithdrawInfo.getMerchantId());
        params.put("AMT", payWithdrawInfo.getAmt());
        params.put("ACCT_ADDR", payWithdrawInfo.getAcctAddr());
        queryWrapper = queryWrapper.allEq(params, false).ge("CREATE_TIME", howManySecondsAgo); //忽略value为null 值为null空 则忽略
        queryWrapper.orderByDesc("ID");
        List<PayWithdrawInfo> withdrawInfoList = iPayWithdrawInfoService.list(queryWrapper);
        //30秒内有相同的订单 则表示重复了 已扣费 再人工去冲正
        if (withdrawInfoList.size() > 1) {
            _log.error("ERROR:==>订单重复:{},收款人:{},收款钱包:{},金额:{}",payWithdrawInfo.getWithdrawNo(),payWithdrawInfo.getAcctName(),payWithdrawInfo.getAcctAddr(),payWithdrawInfo.getAmt());
            return;
        }

        //流水写入成功,请求三方服务器
        _log.info("=====>>>>>商家:{},代付/下发:{}流水写入成功,请求三方服务器", payWithdrawInfo.getMerchantId(), payWithdrawInfo.getAmt());


        try {
            //进行提现处理｜提交到服务器｜返回处理中或者处理失败｜如果处理失败则立即退费(如银行卡不存在之类的)

            ChannelResultVo channelResultVo = new ChannelResultVo();
            channelResultVo.setMerchantId(payWithdrawInfo.getMerchantId());
            channelResultVo.setWithdrawNo(payWithdrawInfo.getWithdrawNo());
            channelResultVo.setWithdrawAmt(payWithdrawInfo.getAmt());

            //修改下单提现状态
            PayWithdrawInfo updateWrapper = new PayWithdrawInfo();
            updateWrapper.setWithdrawStatus(EnumWithdrawStatus.PROCESSING.getName());
            updateWrapper.setRemark("处理中");
           // updateWrapper.setMerchantWithdrawNo(channelResultVo.getMerchantWihtdrawNo()); //商户订单号号
            //updateWrapper.setChannelWithdrawNo(channelResultVo.getChannelWithdrawNo()); ////更新上游渠道订单号
            infoService.update(updateWrapper,
                    new UpdateWrapper<PayWithdrawInfo>()
                            .lambda()
                            .eq(PayWithdrawInfo::getMerchantId, payWithdrawInfo.getMerchantId())
                            .eq(PayWithdrawInfo::getMerchantWithdrawNo, payWithdrawInfo.getMerchantWithdrawNo()));

//                _log.info("activeMQ Queue =====>>>>> 取得提现数组{},并依次写入Redis{}", withdrawInfoJson, channelResultVo);
//                redisUtil.sSet(MyRedisConst.QUERY_WITHDRAW_CACHE_SET_NAME, JSONObject.toJSONString(channelResultVo));


        } catch (Exception e) {
            e.printStackTrace();
            _log.error("activeMQ Queue withdrawHandleQueue 订单处理异常:{}", withdrawInfoJson);
            //WARNING:网络订单异常  不可退回余额 应为 未知 UN_CLEAR
        }
    }

    /**
     * 退费队列
     *
     * @param
     * @param
     * @ [merchantWihtdrawNo=null, merchantId=M1573558001590227100, withdrawNo=W1574150981687407313, withdrawStatus=2, withdrawRemark=null,
     * channelCode=S025, platformProfit=null, createTime=null, requestType=null, isNotify=false]
     */
    @JmsListener(destination = MyAmQueueName.REFUND_QUEUE_NAME)
    public void refundQueue(String message)  {

        _log.info("activeMQ Queue refundQueue 接收退费队列信息:{}", message);
        ChannelResultVo channelResultVo = JSONObject.parseObject(message, ChannelResultVo.class);


        BigDecimal refundAmt = channelResultVo.getWithdrawAmt();

        _log.info("=====>>>>>refundQueue接收的退费信息:{}", channelResultVo);
        Integer isGetBackIndex = getReturnLatestMoneyRecord(channelResultVo,refundAmt);
        //记录退款流水表失败 不退款
        if (isGetBackIndex != 1) {
            return;
        }

        //商户退回提现/代付
        balanceService.alterMerchantBalance(channelResultVo.getMerchantId(), refundAmt, null);
    }

    /**
     * 提现状态通知队列
     *
     * @param
     * @param
     */
    @JmsListener(destination = MyAmQueueName.SEND_WITHDRAW_NOTIFY_QUEUE_NAME)
    public void receiveWithdrawNotifyQueue(String message) throws Exception {
        _log.info("activeMQ Queue sendNofityQueue receive message{}", message);
        JSONObject notifyJson = JSONObject.parseObject(message);
        //mybatis-plus
        PayWithdrawInfo payWithdrawInfo = infoService.getOne(new QueryWrapper<PayWithdrawInfo>()
                .lambda()
                .eq(PayWithdrawInfo::getMerchantId, notifyJson.getString("merchantId"))
                .eq(PayWithdrawInfo::getMerchantWithdrawNo, notifyJson.getString("merchantWithdrawNo"))
        );
        //withdrawStatus 订单的状态
        notifyService.notifyMerchant(payWithdrawInfo, notifyJson.getString("withdrawStatus"));

    }

    /**
     * 佣金处理队列
     *
     * @param
     * @param
     */
    @JmsListener(destination = MyAmQueueName.COMMISSION_QUEUE_NAME)
    public void commissionQueue(String commission) {
        //收到佣金
        _log.info("activeMQ Queue 收到佣金消息{}", commission);
    }

    @Transactional(rollbackFor = Exception.class) //余额流水表
    public Integer getLatestMoneyRecord(PayWithdrawInfo payWithdrawInfo)  {
        //1.查询是否商家是否已有相同的流水记录
        PayMerchantMoneyChange moneyChangeRecord = iPayMerchantMoneyChangeService.getOne(new QueryWrapper<PayMerchantMoneyChange>().lambda()
                .eq(PayMerchantMoneyChange::getWithdrawNo, payWithdrawInfo.getWithdrawNo())
                .eq(PayMerchantMoneyChange::getMerchantId, payWithdrawInfo.getMerchantId())
                .eq(PayMerchantMoneyChange::getFlowType, EnumFlowType.WITHDRAW.getName())
        );
        if (moneyChangeRecord != null) {
            _log.error("=====>>>>>activemq ==> 接收消息商家:{}代付:{}失败,原因:流水已记录,不可重复下单", payWithdrawInfo.getMerchantId(), payWithdrawInfo.getAmt());
            //重复订单
            //  throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "流水订单号重复");
            return 0; //退出单条取款 queue
        }


        //2.查询商户最新的MONEY_CHANGE 下发流水记录 事务锁表 避免脏读

//        PayMerchantMoneyChange moneyChangeParams = new PayMerchantMoneyChange();
//        moneyChangeParams.setMerchantId(payWithdrawInfo.getMerchantId()); //某一个商家
//        if (curMerchantChannelSite.getChannelFeeType().equals(CodeConst.ChannelFeeTypeConst.FEE_COMBINE)) {
//            moneyChangeParams.setAmt(payWithdrawInfo.getAmt().add(payWithdrawInfo.getMerchantFee()).negate()); // 取款
//        }else {
//            moneyChangeParams.setAmt(payWithdrawInfo.getAmt().negate()); // 取款
//        }


        //1.插入流水表
        _log.info("插入商家:[{}]代付流水表",payWithdrawInfo.getMerchantId());

//        Integer latestRecordId = iPayMerchantMoneyChangeService.insertLatestMoneyChangeBySelect(moneyChangeParams);
//        _log.info("代付插入流水表时获取插入的用户id:{}",moneyChangeParams.getId());
//        if (latestRecordId != 1) { return 0; }
//        //2.获取插入的用户
//        PayMerchantMoneyChange latestRecordPayMerchant = iPayMerchantMoneyChangeService.getById(moneyChangeParams.getId());

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("merchantId", payWithdrawInfo.getMerchantId());
        PayMerchantMoneyChange latestRecord = iPayMerchantMoneyChangeService.selectLatestMoneyChangeForUpdate(params);

        //更新时间
        _log.info("商家代付流水记录===>>>商家:{},账变前:{},账变后:{}", payWithdrawInfo.getMerchantId(), latestRecord.getAmtNow(), latestRecord.getAmtNow().subtract(payWithdrawInfo.getAmt()));

        //3.修改记录MONEY_CHANGE 下发流水
        PayMerchantMoneyChange moneyChangeEntity = new PayMerchantMoneyChange();
        moneyChangeEntity.setFlowType(EnumFlowType.WITHDRAW.getName());
        moneyChangeEntity.setMerchantId(payWithdrawInfo.getMerchantId());// 少了一个商户ID
        moneyChangeEntity.setMerchantWithdrawNo(payWithdrawInfo.getMerchantWithdrawNo());
        moneyChangeEntity.setWithdrawNo(payWithdrawInfo.getWithdrawNo());
        moneyChangeEntity.setAmt(payWithdrawInfo.getAmt()); //取款金额

        moneyChangeEntity.setAmtBefore(latestRecord.getAmtNow());

        moneyChangeEntity.setCreditAmt(payWithdrawInfo.getAmt()); //取款+手续费
        moneyChangeEntity.setAmtNow(latestRecord.getAmtNow().subtract(payWithdrawInfo.getAmt()));

        //moneyChangeEntity.setCreateTime(new Date());
        moneyChangeEntity.setRemark("商家[" + payWithdrawInfo.getMerchantId() + "]代付[" + payWithdrawInfo.getAmt() + "]");
        moneyChangeEntity.setChannelName(payWithdrawInfo.getChannelName());
        moneyChangeEntity.setChannelCode(payWithdrawInfo.getChannelCode());
        boolean isSaveMoneyChange = iPayMerchantMoneyChangeService.save(moneyChangeEntity);
        // boolean isUpdateMoneyChange = iPayMerchantMoneyChangeService.update(moneyChangeEntity,new QueryWrapper<PayMerchantMoneyChange>().lambda().in(PayMerchantMoneyChange :: getId, moneyChangeParams.getId()));

        //4.写入流水失败不请求服务器 以防查不到帐
        //问题：共有50条订单 ，其中3条插入流水时发生失败，3条无流水记录，47条有记录，要怎么办？
        //activemq一条一条发送，发送失败，则activemq会自己抛出异常
        if (!isSaveMoneyChange) {
            _log.error("=====>>>>>商家:{},代付/下发:{}流水写入失败", payWithdrawInfo.getMerchantId(), payWithdrawInfo.getAmt());
            return 0;
        } else {
            return 1;
        }
    }

    //为数据源的默认隔离级别
    /**
     * 缺省读取MySql数据库的default级别
     * @Transactional(isolation = Isolation.READ_UNCOMMITTED)：读取未提交数据(会出现脏读, 不可重复读) 基本不使用
     * @Transactional(isolation = Isolation.READ_COMMITTED)：  读取已提交数据(会出现不可重复读和幻读)
     * @Transactional(isolation = Isolation.REPEATABLE_READ)：可重复读(会出现幻读) ====>>> mysql
     * @Transactional(isolation = Isolation.SERIALIZABLE)：串行化
     * */
    @Transactional(rollbackFor = Exception.class)
    public Integer getReturnLatestMoneyRecord(ChannelResultVo channelResultVo, BigDecimal refundAmt)  {

        //流水表 可避免二次 退款
        PayMerchantMoneyChange moneyChangeRecord = iPayMerchantMoneyChangeService.getOne(new QueryWrapper<PayMerchantMoneyChange>().lambda()
                .eq(PayMerchantMoneyChange::getWithdrawNo, channelResultVo.getWithdrawNo())
                .eq(PayMerchantMoneyChange::getMerchantId, channelResultVo.getMerchantId())
                .eq(PayMerchantMoneyChange::getFlowType, EnumFlowType.RECEIVE.getName())
        );

        if (moneyChangeRecord != null) {
            _log.info("=====>>>>>refundQueue商家:{}退费:{}失败,原因:流水已记录,不可重复修正订单", channelResultVo.getMerchantId(), refundAmt);
            return 0;
        }

        //查询商户最新的MONEY_CHANGE 下发流水记录 不锁表了 流水表记录过多 直接用 insertLatestMoneyChangeBySelect --- 反而造成：锁表更频繁
        //PayMerchantMoneyChange moneyChangeParams = new PayMerchantMoneyChange();
       // moneyChangeParams.setMerchantId(channelResultVo.getMerchantId()); //某一个商家
       // moneyChangeParams.setAmt(refundAmt); // 冲正 商户款项增加

        //1.插入流水表
        //Integer latestRecordId = iPayMerchantMoneyChangeService.insertLatestMoneyChangeBySelect(moneyChangeParams);
        //if (latestRecordId != 1) { return 0; }

        //_log.warn("退款插入流水表是获取插入的用户id:{}",latestRecordId); // moneyChangeParams.getId();
        //2.获取插入的用户
        //PayMerchantMoneyChange latestRecordPayMerchant = iPayMerchantMoneyChangeService.getById(moneyChangeParams.getId());
        //更新时间
        //_log.info("退款流水记录===>>>商家:{},账变前:{},账变后:{}", channelResultVo.getMerchantId(), latestRecordPayMerchant.getAmtBefore(), latestRecordPayMerchant.getAmtNow());
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("merchantId", channelResultVo.getMerchantId());
        PayMerchantMoneyChange latestRecord = iPayMerchantMoneyChangeService.selectLatestMoneyChangeForUpdate(params);

        //3.记录MONEY_CHANGE 退款流水
        PayMerchantMoneyChange moneyChangeEntity = new PayMerchantMoneyChange();
        moneyChangeEntity.setFlowType(EnumFlowType.RECEIVE.getName()); //冲正 退回款项
        moneyChangeEntity.setMerchantId(channelResultVo.getMerchantId());
        moneyChangeEntity.setMerchantWithdrawNo(channelResultVo.getMerchantWihtdrawNo());
        moneyChangeEntity.setWithdrawNo(channelResultVo.getWithdrawNo());
        moneyChangeEntity.setAmt(refundAmt); //资金变动
        moneyChangeEntity.setCreditAmt(refundAmt); //资金变动
        moneyChangeEntity.setAmtBefore(latestRecord.getAmtNow());
        moneyChangeEntity.setAmtNow(latestRecord.getAmtNow().add(refundAmt));
        ///moneyChangeEntity.setCreateTime(new Date());
        moneyChangeEntity.setRemark("退回商家[" + channelResultVo.getMerchantId() + "][" + refundAmt + "]");
        moneyChangeEntity.setChannelName(channelResultVo.getChannelCode());
        moneyChangeEntity.setChannelCode(channelResultVo.getChannelCode());
        boolean isSaveMoneyChange = iPayMerchantMoneyChangeService.save(moneyChangeEntity);
        // boolean isUpdateMoneyChange = iPayMerchantMoneyChangeService.update(moneyChangeEntity,new QueryWrapper<PayMerchantMoneyChange>().lambda().in(PayMerchantMoneyChange :: getId, moneyChangeParams.getId()));

        if (!isSaveMoneyChange) {
            _log.info("=====>>>>>refundQueue记入商家:{}退费流水表:{}失败", channelResultVo.getMerchantId(), refundAmt);
            return 0;
        } else {
            return 1;
        }
    }

}
