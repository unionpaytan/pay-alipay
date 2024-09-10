package com.bootpay.mng.interceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import com.bootpay.common.utils.IPUtility;
import com.bootpay.common.constants.CodeConst;
import com.bootpay.common.excetion.TranException;
import com.bootpay.mng.service.AuthenticatorService;

/**
 * 登录拦截
 * 
 * @author Administrator
 *
 */
@Configuration
public class CheckIpInterceptor implements HandlerInterceptor {

	private AuthenticatorService authenticatorService;

	public CheckIpInterceptor(AuthenticatorService authenticatorService) {
		this.authenticatorService = authenticatorService;
	}

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
			throws Exception { // 请求进入这个拦截器

		if(request.getParameter("merchantId") == null){
			throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "商户号为空");
		}

		if("".equals(request.getParameter("merchantId"))){
			throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "商户号为空");
		}

		if (authenticatorService.checkAPIRequestIp(request.getParameter("merchantId"), request)) {
			return true;
		} else {
			throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "IP["+IPUtility.getClientIp(request)+"]未绑定");
		}
	}

	@Override
	public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
			ModelAndView modelAndView) throws Exception {

	}

	@Override
	public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex)
			throws Exception {

	}

}
