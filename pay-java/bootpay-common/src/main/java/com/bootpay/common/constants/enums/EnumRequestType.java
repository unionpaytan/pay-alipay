package com.bootpay.common.constants.enums;


/**
 * 提现请求类型
 * @author Administrator
 *
 */
public enum EnumRequestType {

	API("API","API代付"),
	WEB("WEB","商户后台代付"),
	PLATFORM("PLATFORM","平台利润提现");
	
	
	
	private String name;
	private String index;

	private EnumRequestType(String name, String index) {
		this.name = name;
		this.index = index;
	}

	public static EnumRequestType getEnum(String name) {    
        for (EnumRequestType c : EnumRequestType.values()) {
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
