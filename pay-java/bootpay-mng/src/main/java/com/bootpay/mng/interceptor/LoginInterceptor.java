package com.bootpay.mng.interceptor;


import com.bootpay.common.constants.CodeConst;
import com.bootpay.common.excetion.TranException;
import com.bootpay.mng.service.AuthenticatorService;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 登录拦截
 * 
 * @author Administrator
 *
 */
@Configuration 
public class LoginInterceptor implements HandlerInterceptor {

	private AuthenticatorService authenticatorService;

    public LoginInterceptor(AuthenticatorService authenticatorService){
        this.authenticatorService = authenticatorService;
    }

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
			throws Exception { // 请求进入这个拦截器
		String token = request.getHeader("authorization");
		HandlerMethod handlerMethod = (HandlerMethod) handler;
		RequestMapping annotation = handlerMethod.getMethodAnnotation(RequestMapping.class);
        //新增权限控制
		if (annotation != null){
			String annotationName = annotation.name();
			//请求接口为管理员等级
			if ("admin".equals(annotationName)){
				//查询登录用户的TOKEN中的等级是不是ADMIN
				if(!authenticatorService.checkPermission(token).equals("admin")){
					throw new TranException(CodeConst.PERMISSION_CODE,"无权访问");
				}
			}

		}

		if(authenticatorService.checkToken(token)){
			return true; // 有的话就返回true
		}

//	    ReturnMsg returnMsg = new ReturnMsg(CodeConst.TOKEN_CODE,"登录过期");
//	    String result = JSONObject.toJSONString(returnMsg);
//	    _log.info("登录拦截{}",result);
//	    response.setCharacterEncoding("UTF-8");  
//	    response.setContentType("application/json; charset=utf-8");  
//	    PrintWriter out = response.getWriter();  
//        out.append(result); 
		throw new TranException(CodeConst.TOKEN_CODE,"登录过期");
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
