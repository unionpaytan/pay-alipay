package com.bootpay.common.constants.enums;


/**
 * 谷歌验证码操作类型
 * @author Administrator
 *
 */
public enum EnumGoogleType {

	BIND_GOOGLE("bind","绑定谷歌验证"),
	CANCEL_GOOGLE("cancel","取消谷歌验证");
	
	
	
	private String name;
	private String index;

	private EnumGoogleType(String name, String index) {
		this.name = name;
		this.index = index;
	}

	public static EnumGoogleType getEnum(String name) {    
        for (EnumGoogleType c : EnumGoogleType.values()) {
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
