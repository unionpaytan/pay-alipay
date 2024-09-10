package com.bootpay.common.utils;



import java.util.UUID;

/**
 * @Description: 生成全局唯一序列号工具类
 * @version V1.0
 */
public class MySeq {

	private static String pay_seq_prefix = "P"; //付款
	private static String withdraw_seq_prefix = "W"; //代付下单取款
	private static String merchant_seq_prefix = "M"; //商户
	private static String partner_seq_prefix = "P"; //外包
	private static String admin_seq_prefix = "S"; //后台
	private static String tunnel_seq_prefix = "T"; //通道 TUNNEL
	private static String card_seq_prefix = "C"; //银行卡流水 卡Card
	private static String coin_seq_prefix = "U"; //coin
	private static String deposit_seq_prefix = "D"; //deposit 存款

	/**
	 * 获取支付订单
	 * @return
	 */
	public static String getPayNo() {
		return getSeq(pay_seq_prefix, RandomUtils.buildIntRandom(6));
	}


	/**
	 * 获取代付订单号
	 * @return
	 */
	public static String getCardWithdrawNo() {
		return getSeq(card_seq_prefix, RandomUtils.buildIntRandom(6));
	}

	/**
	 * 获取代付订单号
	 * @return
	 */
	public static String getWithdrawNo() {
		return getSeq(withdraw_seq_prefix, RandomUtils.buildIntRandom(6));
	}

	public static String getDepositNo() {
		return getSeq(deposit_seq_prefix, RandomUtils.buildIntRandom(6));
	}

	/**
	 * 获取商户号
	 * @return
	 */
	public static String getMerchantNo() {
		return getSeq(merchant_seq_prefix, RandomUtils.buildIntRandom(6));
	}

	/**
	 * 获取外包号
	 * @return
	 */
	public static String getPartnerNo() {
		return getSeq(partner_seq_prefix, RandomUtils.buildIntRandom(6));
	}

	/**
	 * 获取对接通道号
	 * @return
	 */
	public static String getSeqChannelNo() {
		return getSeq(tunnel_seq_prefix, RandomUtils.buildIntRandom(6));
	}

	/**
	 * 获取对接通道号
	 * @return
	 */
	public static String getSeqCoinNo() {
		return getSeq(coin_seq_prefix, RandomUtils.buildIntRandom(6));
	}


	/**
	 * 获取管理员号
	 * @return
	 */
	public static String getSystemUser() {
		return getSeq(admin_seq_prefix, RandomUtils.buildIntRandom(6));
	}


	public static String getUUID() {
		return UUID.randomUUID().toString().replaceAll("-", "");
	}

	/**
	 * 生成商户APIKEY
	 * @return
	 */
	public static String getAPIkey() {
		return UUID.randomUUID().toString().replaceAll("-", "");
	}

	public static String getPassword() {
		return UUID.randomUUID().toString().replaceAll("-", "").substring(0,6);
	}


	private static String getSeq(String prefix, String random) {
		return prefix+getSeqString()+random;
	}



	static String getSeqString() {
		return System.currentTimeMillis()+"";
	}



}