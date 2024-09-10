package com.bootpay.common.constants.enums;


/**
 * 提现类型
 * @author Administrator
 *
 */
public enum EnumWithdrawStatus {

	UNKNOWN("0","UNKNOWN"),// web前端未提交(队列一条一条)
	SUCCESS("1","SUCCESS"),//成功
	FAIL("2","FAIL"),//失败
	PROCESSING("3","PROCESSING"), //(web前端提交)
	//ONGOING("5","ONGOING"), //app打开打款中
	EXPIRED("-1","EXPIRED");//超时

	private String name;
	private String index;

	private EnumWithdrawStatus(String name, String index) {
		this.name = name;
		this.index = index;
	}

	public static EnumWithdrawStatus getEnum(String name) {    
        for (EnumWithdrawStatus c : EnumWithdrawStatus.values()) {
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
