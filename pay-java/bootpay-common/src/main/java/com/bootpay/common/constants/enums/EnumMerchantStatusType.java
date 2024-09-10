package com.bootpay.common.constants.enums;


/**
 * 支付渠道编号
 * @author Administrator
 *
 */
public enum EnumMerchantStatusType {

	NORMAL("0","正常"),
	FAIL("1","禁止");
	
	
	private String name;
	private String index;

	private EnumMerchantStatusType(String name, String index) {
		this.name = name;
		this.index = index;
	}

	public static EnumMerchantStatusType getEnum(String name) {    
        for (EnumMerchantStatusType c : EnumMerchantStatusType.values()) {
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
