package com.bootpay.common.utils;


import java.util.Map;

import com.bootpay.common.constants.CodeConst;

public class ReturnMsg {
	private String code;
	private String message;
	private Object data;

	public ReturnMsg() {
	}

	public ReturnMsg(Object data) {
		this.data = data;
	}

	public ReturnMsg(String code, String message) {
		this.code = code;
		this.message = message;
	}

	public Object getData() {
		return this.data;
	}

	public void setData(Object data) {
		this.data = data;
	}

	public String getCode() {
		return this.code == null ? CodeConst.SUCCESS_CODE : this.code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getMessage() {
		return this.message == null ? CodeConst.SUCCESS_MSG : this.message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public ReturnMsg setFail() {
		setCode("9999");
		return this;
	}

	public ReturnMsg setFail(String msg) {
		setCode("9999");
		setMessage(msg);
		return this;
	}

	public ReturnMsg setFail(String msg, String url) {
		setCode(CodeConst.BUSINESS_ERROR_CODE);
		setMessage(msg);

		return this;
	}

	public ReturnMsg setFail(String msg, Map<?, ?> data) {
		setCode(CodeConst.BUSINESS_ERROR_CODE);
		setMessage(msg);

		return this;
	}

	public ReturnMsg setFail(String msg, String url, Map<?, ?> data) {
		setCode(CodeConst.BUSINESS_ERROR_CODE);
		setMessage(msg);

		return this;
	}

	public ReturnMsg setSuccess() {
		setCode(CodeConst.SUCCESS_CODE);
		return this;
	}

	public ReturnMsg setSuccess(String msg) {
		setCode(CodeConst.SUCCESS_CODE);
		setMessage(msg);
		return this;
	}



	public ReturnMsg setSuccess(Map<String,Object> data) {
		setData(data);
		return this;
	}

	public ReturnMsg setSuccess(String msg, String url, Map<?, ?> data) {
		setCode(CodeConst.SUCCESS_CODE);
		setMessage(msg);
		setData(data);
		return this;
	}

	public void setMsg(String code, String Message) {
		this.code = code;
		this.message = Message;
	}

}