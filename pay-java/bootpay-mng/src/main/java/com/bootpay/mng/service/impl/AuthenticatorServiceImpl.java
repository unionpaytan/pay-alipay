package com.bootpay.mng.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.bootpay.common.authenticator.MailAuthenticator;
import com.bootpay.common.authenticator.TokenAuthenticator;
import com.bootpay.common.constants.CodeConst;
import com.bootpay.common.constants.MyRedisConst;
import com.bootpay.common.constants.enums.EnumMailCodeType;
import com.bootpay.common.excetion.TranException;
import com.bootpay.common.framework.RedisUtil;
import com.bootpay.common.utils.AESUtil;
import com.bootpay.common.utils.IPUtility;
import com.bootpay.common.utils.RandomUtils;
import com.bootpay.mng.service.AuthenticatorService;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;

@Service
public class AuthenticatorServiceImpl implements AuthenticatorService {

	private Logger _log = LoggerFactory.getLogger(AuthenticatorServiceImpl.class);

	@Autowired
	private MailAuthenticator mailAuthenticator;
	@Autowired
	private RedisUtil redisUtil;

	//发送邮件验证码 + 把验证码存在REDIS中 ，300秒后就过期
	@Override
	public void sendMailCode(String merchantId, String merchantName, String mail, String type) throws TranException {

		String code = RandomUtils.getRandomStringByLength(6).toUpperCase();
		//设置REDIS
     	if (type != null && !"".equals(type)) {
			redisUtil.set(merchantId + MyRedisConst.SPLICE + type, code, MyRedisConst.MAIL_CODE_TTL);
		} else {
			redisUtil.set(merchantId + MyRedisConst.SPLICE + "email", code, MyRedisConst.MAIL_CODE_TTL);
		}
		//发送邮件
		_log.info("开始发送邮件");
		mailAuthenticator.send(mail, EnumMailCodeType.getEnum(type).getIndex(), code, merchantName,EnumMailCodeType.getEnum(type).getIndex());
		_log.info("邮件已成功发送给:{}--->商户号:{},验证码类型:{}", mail,merchantId, type);
	}

	@Override
	public boolean checkMailCode(String merchantId, String mailCode, String type) throws TranException {
		Object redisMailCode = redisUtil.get(merchantId + MyRedisConst.SPLICE + type);
		//_log.info("邮箱验证码校验--->商户号:{},验证码类型:{},待校验验证码:{}", merchantId, type, mailCode);
		if (redisMailCode == null) {
			throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "验证码已经失效");
		}
		if (redisMailCode.toString().equals(mailCode.toUpperCase())) {
			return true;
		}
		return false;

	}

	@Override
	public String getToken(String merchantId,String accountType) {
		String token = TokenAuthenticator.getInstance().makeToken();
		JSONObject json = new JSONObject();
		json.put("token", token);
		json.put("merchantId", merchantId);//商户编号
		json.put("accountType",accountType); //新增商户等级;
		token = AESUtil.encrypt(json.toJSONString(), AESUtil.AES_KEY); //token加密
		redisUtil.set(merchantId + MyRedisConst.SPLICE + MyRedisConst.TOKEN_REDIS_KEY_NAME, token,MyRedisConst.LOGIN_TOKEN_TTL);
		return token;
	}

	@Override
	public boolean checkToken(String clientToken)  {
		if (StringUtils.isBlank(clientToken)) {
			return false;
		}
		JSONObject json = JSONObject.parseObject(AESUtil.decrypt(clientToken, AESUtil.AES_KEY));
		String merchantId = json.getString("merchantId");
		Object serverToken = redisUtil.get(merchantId + MyRedisConst.SPLICE + MyRedisConst.TOKEN_REDIS_KEY_NAME);
		if (serverToken == null) {
			return false;
		}
		if (!serverToken.equals(clientToken)) {
			return false;
		}
		// 刷新token过期时间 原理：因为REDIS缓存时间只有1小时，重新刷新时长，若更新失败，则表示过期了;
		if (!redisUtil.expire(merchantId + MyRedisConst.SPLICE + MyRedisConst.TOKEN_REDIS_KEY_NAME,MyRedisConst.LOGIN_TOKEN_TTL)) {
			return false;
		}
		//保存SPRING SECURITY的用户信息;
		return true;
	}

	@Override
	public String checkPermission(String clientToken)  {

		if (StringUtils.isBlank(clientToken)) {
			return "";
		}

		JSONObject json = JSONObject.parseObject(AESUtil.decrypt(clientToken, AESUtil.AES_KEY));
		String merchantId = json.getString("merchantId");
		Object serverToken = redisUtil.get(merchantId + MyRedisConst.SPLICE + MyRedisConst.TOKEN_REDIS_KEY_NAME);
		if (serverToken == null) {
			return "";
		}
		//从TOKEN内获取出登录用户的等级 商户 merchant or 管理员admin
		return json.getString("accountType");
	}


	@Override
	public boolean checkAPIRequestIp(String merchantId,HttpServletRequest request) throws TranException {
		String requestIp = IPUtility.getClientIp(request);
		_log.info("API代付请求IP校验--->商户号:{},IP{}", merchantId,requestIp);
		return redisUtil.sHasKey(merchantId + MyRedisConst.SPLICE + MyRedisConst.MERCHANT_IP_KEY_NAME, requestIp);
	}

	@Override
	public boolean checkWEBRequestIp(String merchantId,HttpServletRequest request) throws TranException {
		String requestIp = IPUtility.getClientIp(request);
		_log.info("WEB代付请求IP校验--->商户号:{},IP{}", merchantId,requestIp);
		return redisUtil.sHasKey(merchantId + MyRedisConst.SPLICE + MyRedisConst.MERCHANT_WEB_KEY_NAME, requestIp);
	}

}
