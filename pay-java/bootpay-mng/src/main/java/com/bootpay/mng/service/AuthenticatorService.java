package com.bootpay.mng.service;

import com.bootpay.common.excetion.TranException;

import javax.servlet.http.HttpServletRequest;

/**
 * 验证接口
 * @author Administrator
 *
 */
public interface AuthenticatorService {
	
	
	/**
	 * 发送邮箱验证码
	 * @param merchantId 商户ID
	 * @param type 短信类型  login 登录验证码
	 * @return
	 * @throws TranException
	 */
	public void sendMailCode(String merchantId,String userName,String mail,String type) throws TranException; //throws TranException
	
	
	/**
	 * 校验验证码
	 * @param merchantId
	 * @param mailCode
	 * @param type 短信类型  login 登录验证码
	 * @throws TranException
	 */
	public boolean checkMailCode(String merchantId,String mailCode,String type)throws TranException;
	
	
	/**
	 * 获取token
	 * @param merchantId
	 * @return
	 * @throws TranException
	 */
	public String getToken(String merchantId,String accountType);
	
	
	/**
	 * 校验token
	 * @param merchantId
	 * @return
	 * @throws TranException
	 */
	public boolean checkToken(String token);

	/**
	 * 校验token并判断是否有管理员权限 admin
	 * @param merchantId
	 * @return
	 * @throws TranException
	 */
	public String checkPermission(String token);
	
	/**
	 * 判断请求IP是否为绑定IP
	 * @param merchantId
	 * @param ip
	 * @return
	 * @throws TranException
	 */
	public boolean checkAPIRequestIp(String merchantId,HttpServletRequest request)throws TranException;

	/**
	 * 判断WEB请求IP是否为绑定IP
	 * @param merchantId
	 * @param ip
	 * @return
	 * @throws TranException
	 */
	public boolean checkWEBRequestIp(String merchantId,HttpServletRequest request)throws TranException;
}
