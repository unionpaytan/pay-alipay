package com.bootpay.common.constants;

/**
 * AM队列名称
 * @author Administrator
 *
 */
public class MyAmQueueName {

	/**
	 * 代付处理队列
	 */
	public static final String WITHDRAW_QUEUE_NAME = "withdrawQueue_30";
	public static final String DEPOSIT_QUEUE_NAME = "depositQueue_30";
	/**
	 * 是否还存在代付处理队列
	 */
	public static final String WITHDRAW_QUEUE_NAME_ON_PROCESSING = "WithdrawQueueONPROCESSING_30";

	/**
	 * 发送邮件队列
	 */
	public static final String SEND_MAIL_QUEUE_NAME = "sendMailQueue_30";

	/**
	 * 发送提现状态通知队列
	 */
	public static final String SEND_WITHDRAW_NOTIFY_QUEUE_NAME = "sendWithdrawNotifyQueue_30";

	/**
	 * 退费队列
	 */
	public static final String REFUND_QUEUE_NAME = "refundQueue_30";

	/**
	 * 佣金队列
	 * */
	public static  final  String COMMISSION_QUEUE_NAME = "commissionQueue_30";
}
