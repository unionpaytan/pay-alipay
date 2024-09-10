package com.bootpay.mng.amqueue;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.bootpay.channel.vo.ChannelResultVo;
import com.bootpay.common.constants.MyAmQueueName;
import com.bootpay.core.entity.PayMerchantChannelSite;
import com.bootpay.core.entity.PayMerchantInfo;
import com.bootpay.core.entity.PayWithdrawInfo;
import com.bootpay.core.entity.WithdrawChannelInfo;
import org.apache.activemq.command.ActiveMQQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.core.JmsMessagingTemplate;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.support.QosSettings;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.jms.DeliveryMode;
import java.util.HashMap;
import java.util.Map;

/**
 * 消息队列生产者
 * @author Administrator
 *
 */
@Service
public class AMProducerUtils {
	
	private Logger _log = LoggerFactory.getLogger(AMProducerUtils.class);

	@Resource
	private JmsMessagingTemplate jmsMessagingTemplate;

	/**
	 * 发送Mail验证码队列
	 * @param
	 * @param
	 */
	public void sendMailQueue(String merchantId,String merchantName,String mailbox,String mailType){
		// 发送登录验证码
		JSONObject mail = new JSONObject();
		mail.put("merchantId", merchantId);
		mail.put("merchantName", merchantName);
		mail.put("mailbox", mailbox);
		mail.put("mailType",mailType);
		ActiveMQQueue destination = new ActiveMQQueue(MyAmQueueName.SEND_MAIL_QUEUE_NAME);
		_log.info("activeMQ发送Mail验证码队列信息--->queueName：{},message：{}",MyAmQueueName.SEND_MAIL_QUEUE_NAME,mail);
		jmsMessagingTemplate.convertAndSend(destination, mail.toJSONString());
	}

	/**
	 * 提现处理队列
	 * @param
	 * @param
	 */
	public void sendWithdrawHandleQueue(PayWithdrawInfo batchList){
		ActiveMQQueue destination = new ActiveMQQueue(MyAmQueueName.WITHDRAW_QUEUE_NAME);
		_log.info("activeMQ提现处理队列信息--->queueName：{},message：{}",MyAmQueueName.WITHDRAW_QUEUE_NAME,batchList);
		jmsMessagingTemplate.convertAndSend(destination, JSONObject.toJSONString(batchList));
	}

	/**
	 * 退费队列
	 * @param
	 * @param
	 * @[merchantWihtdrawNo=null, merchantId=M1573558001590227100, withdrawNo=W1574150981687407313, withdrawStatus=2, withdrawRemark=null, channelCode=S025, platformProfit=null, createTime=null, requestType=null, isNotify=false]
	 */
	public void sendRefundHandleQueue(ChannelResultVo withdrawInfo){
		ActiveMQQueue destination = new ActiveMQQueue(MyAmQueueName.REFUND_QUEUE_NAME);
		_log.info("activeMQ发送退费队列信息--->queueName:{},message:{}",MyAmQueueName.REFUND_QUEUE_NAME,withdrawInfo);
		jmsMessagingTemplate.convertAndSend(destination, JSONObject.toJSONString(withdrawInfo));
	}

	/**
	 * 提现状态异步通知队列
	 * @param
	 * @param
	 */
	public void sendWithdrawNotifyQueue(String merchantId,String merchantWithdrawNo,String withdrawStatus){
		JSONObject notifyJson = new JSONObject();
		notifyJson.put("merchantId", merchantId);
		notifyJson.put("merchantWithdrawNo", merchantWithdrawNo);
		notifyJson.put("withdrawStatus",withdrawStatus);
		ActiveMQQueue destination = new ActiveMQQueue(MyAmQueueName.SEND_WITHDRAW_NOTIFY_QUEUE_NAME);
		_log.info("activeMQ发送[提现状态异步通知]队列信息--->queueName{},message{}",MyAmQueueName.SEND_WITHDRAW_NOTIFY_QUEUE_NAME,notifyJson);
		jmsMessagingTemplate.convertAndSend(destination, JSONObject.toJSONString(notifyJson));
	}

	/**
	 * 佣金通知
	 * */
	public  void sendCommissionQueue(String merchandId,String merchantWithdrawNo){
		JSONObject commisionJsonObj = new JSONObject();
		commisionJsonObj.put("merchantId",merchandId);
		commisionJsonObj.put("merchantWithdrawNo",merchantWithdrawNo);
		ActiveMQQueue destination = new ActiveMQQueue(MyAmQueueName.COMMISSION_QUEUE_NAME);
		_log.info("activeMQ发送[佣金通知]队列信息--->queueName{},message{}",MyAmQueueName.COMMISSION_QUEUE_NAME,commisionJsonObj);
		jmsMessagingTemplate.convertAndSend(destination,JSONObject.toJSONString(commisionJsonObj));

	}

	//@弃用
	public String sendDepositHandleQueue(PayMerchantChannelSite selectedPayMerchantChannelInfo,
										 WithdrawChannelInfo selectedChannelInfo,
										 PayMerchantInfo merchantInfo,
										 String selectedChannelCode,
										 String merchantId,
										 String merchantWithdrawNo,
										 String amt, //提交的订单充值金额 元
										 String extraParam,
										 String merchantNotifyUrl,
										 String merchantChannelCode,
										 String coinType){

		ActiveMQQueue destination = new ActiveMQQueue(MyAmQueueName.DEPOSIT_QUEUE_NAME);
		Map<String,Object> depositMap = new HashMap<>();
		depositMap.put("selectedChannelCode",selectedChannelCode);
		depositMap.put("merchantId",merchantId);
		depositMap.put("merchantWithdrawNo",merchantWithdrawNo);
		depositMap.put("amt",amt);
		depositMap.put("extraParam",extraParam);
		depositMap.put("merchantNotifyUrl",merchantNotifyUrl);
		depositMap.put("merchantChannelCode",merchantChannelCode);
		depositMap.put("coinType",coinType);

		String depositList = JSON.toJSONString(depositMap);
		_log.info("activemq 提交支付订单处理队列信息--->queueName：{},message：{}",MyAmQueueName.DEPOSIT_QUEUE_NAME,depositList);

		return jmsMessagingTemplate.convertSendAndReceive(destination, depositList, String.class);
	}

}
