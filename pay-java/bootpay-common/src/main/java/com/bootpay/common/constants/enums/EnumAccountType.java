package com.bootpay.common.constants.enums;


/**
 * 邮箱验证码类型
 * @author Administrator
 *
 */
public enum EnumAccountType {

	ADMINISTRATOR("administrator","超级管理员"),
	ADMIN("admin","管理员"),
	MERCHANT("merchant","普通商户"),
	AGENT("agent","代理"),
	WHOLESELLER("wholeSeller","码商"),
	FOLLOWERS("followers","收款人");

	private String name;
	private String index;

	private EnumAccountType(String name, String index) {
		this.name = name;
		this.index = index;
	}

	public static EnumAccountType getEnum(String name) {    
        for (EnumAccountType c : EnumAccountType.values()) {
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
