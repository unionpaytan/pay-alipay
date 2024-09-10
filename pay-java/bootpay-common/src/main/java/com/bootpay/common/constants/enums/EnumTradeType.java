package com.bootpay.common.constants.enums;


/**
 * 支付渠道编号
 * @author Administrator
 *
 */
public enum EnumTradeType {

	UNIONPAY("UNIONPAY","快捷支付"),
	QQNATIVE("QQNATIVE","QQ扫码"),
	WXNATIVE("WXNATIVE","微信扫码"),
	ZFBNATIVE("ZFBNATIVE","支付宝扫码"),
	WXWAP("WXWAP","微信H5"),
	ZFBWAP("ZFBWAP","支付宝H5"),
	GATEWAY("GATEWAY","网关支付");

	private String name;
	private String index;

	private EnumTradeType(String name, String index) {
		this.name = name;
		this.index = index;
	}

	public static EnumTradeType getEnum(String name) {    
        for (EnumTradeType c : EnumTradeType.values()) {
            if (c.getName().equals(name)) {
                return c;
            }
        }
        return null;
    }
	
	public String getValue() {
		return this.index;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getIndex() {
		return index;
	}

	public void setIndex(String index) {
		this.index = index;
	}
}
