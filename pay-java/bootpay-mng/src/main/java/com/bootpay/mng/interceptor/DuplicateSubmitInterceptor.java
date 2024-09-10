package com.bootpay.mng.interceptor;

import java.lang.reflect.Method;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;
import org.springframework.web.servlet.resource.ResourceHttpRequestHandler;

import com.bootpay.common.constants.CodeConst;
import com.bootpay.common.excetion.TranException;
import com.bootpay.common.framework.AvoidDuplicateFormToken;
import com.bootpay.common.framework.RedisUtil;

public class DuplicateSubmitInterceptor extends HandlerInterceptorAdapter {

	private Logger log = LoggerFactory.getLogger(DuplicateSubmitInterceptor.class);

	private RedisUtil redisUtil;

	public DuplicateSubmitInterceptor(RedisUtil redisUtil) {
		this.redisUtil = redisUtil;
	}

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
			throws Exception {

		if (handler instanceof ResourceHttpRequestHandler) {
			return true;
		}

		HandlerMethod handlerMethod = (HandlerMethod) handler;

		Method method = handlerMethod.getMethod();

		AvoidDuplicateFormToken annotation = method.getAnnotation(AvoidDuplicateFormToken.class);
		// 查看是否有注解
		if (annotation != null) {
			return isDuplicateSubmit(request);
		}
		return super.preHandle(request, response, handler);
	}

	/**
	 * 判断是否重复提交表单.
	 *
	 * @param request
	 *            the request
	 * @return the boolean
	 * @author : Hu weihui
	 */
	private boolean isDuplicateSubmit(HttpServletRequest request) throws TranException {
		// 请求头是否有token，没有则为非法提交
		String userToken = request.getHeader("authorization");
		Object clientoken = redisUtil.get(userToken);
		// 查看cache内是否有token,有则为重复提交
		if (clientoken != null) {
			log.info("表单重复提交：用户token: {}", userToken);
			throw new TranException(CodeConst.BUSINESS_ERROR_CODE, "3秒内不允许提交重复的数据");
		} else {
			// 没有token则当做首次/二次提交，记录在cache
			redisUtil.set(userToken, userToken, 3L);
		}
		return true;
	}
}
