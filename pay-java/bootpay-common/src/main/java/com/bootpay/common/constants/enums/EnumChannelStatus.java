package com.bootpay.common.constants.enums;


/**
 * 支付渠道状态
 * @author Administrator
 *
 */
public enum EnumChannelStatus {

	ENABLE("1","启用"),
	DISABLE("0","禁用");


	private String name;
	private String index;

	private EnumChannelStatus(String name, String index) {
		this.name = name;
		this.index = index;
	}

	public static EnumChannelStatus getEnum(String name) {    
        for (EnumChannelStatus c : EnumChannelStatus.values()) {
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
