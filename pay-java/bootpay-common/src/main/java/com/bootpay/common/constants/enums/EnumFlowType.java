package com.bootpay.common.constants.enums;


/**
 * 支付渠道编号
 * @author Administrator
 *
 */
public enum EnumFlowType {

	RECHARGE("0","充值"),//后台手动充值
	RECEIVE("1","代收"), //代收（玩家充值） 无冲正 | 代付 冲正
	WITHDRAW("2","下发");

	
	private String name;
	private String index;

	private EnumFlowType(String name, String index) {
		this.name = name;
		this.index = index;
	}

	public static EnumFlowType getEnum(String name) {    
        for (EnumFlowType c : EnumFlowType.values()) {
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
