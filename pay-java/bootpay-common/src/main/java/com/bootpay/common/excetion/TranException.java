package com.bootpay.common.excetion;

/**
 * 异常类
 * @author Administrator
 *
 */
public class TranException extends Exception{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	

	
	private String code = null;
	

	/**
	 * 错误描述，不用做客户端展示
	 */
	private String message = "";


	public TranException(){
		super();
	}


	/**
	 * 
	 * @param msgcod 错误代码
	 * @param desc 详细错误信息描述
	 * @param args 动态参数
	 */
	public TranException(String code,String message){
		super(message);
		this.code = code;
		this.message = message;
	}


	public String getCode() {
		return code;
	}


	public void setCode(String code) {
		this.code = code;
	}


	public String getMessage() {
		return message;
	}


	public void setMessage(String message) {
		this.message = message;
	}
	

}
