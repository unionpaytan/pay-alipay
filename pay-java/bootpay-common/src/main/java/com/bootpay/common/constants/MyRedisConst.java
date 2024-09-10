package com.bootpay.common.constants;

public class MyRedisConst {
	
	
	/**
	 * redis token name
	 */
	public static final String TOKEN_REDIS_KEY_NAME=  "token";
	
	
	/**
	 * 提现处理缓存列表
	 */
	public static final String QUERY_WITHDRAW_CACHE_SET_NAME=  "cacheQueryWithdrawInfoSet_30";

	/**
	 * 代收单处理缓存列表
	 */
	public static final String QUERY_DEPOSIT_CACHE_SET_NAME=  "cacheQueryDepositInfoSet_30";
	
	
	
	/**
	 * 提现失败退费列表
	 */
	public static final String REFUND_CACHE_SET_NAME=  "cacheRefundInfoSet_30";



	public static final String MERCHANT_IP_KEY_NAME=  "IP";

	public static final String MERCHANT_WEB_KEY_NAME=  "WEB";
	
	
	/**
	 * 邮箱验证码失效时间
	 */
	public static final Long MAIL_CODE_TTL = 60L*5;//5分钟
	
	/**
	 * 修改登录token 无操作的失效时间
	 */
	public static final Long LOGIN_TOKEN_TTL = 2*60*60L;//2*60分钟 2小时后登录过期 60s * 60min = 1hour

	
	/**
	 * 拼接字符串
	 */
	public static final String SPLICE = ":";
}
