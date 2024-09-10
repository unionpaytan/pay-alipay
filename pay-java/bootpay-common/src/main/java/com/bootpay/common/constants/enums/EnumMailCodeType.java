package com.bootpay.common.constants.enums;


/**
 * 邮箱验证码类型
 * @author Administrator
 *
 */
public enum EnumMailCodeType {

	LOGIN("login","登录验证"),
	MODIFYPWD("modifyPwd","修改登录密码"),
	ADMINADD("adminAdd","新增管理员"),
	ADMINEDIT("adminEdit","修改管理员"),
	ADMINDEL("adminDel","删除管理员"),
	ADMINSTATUS("adminStatus","更改管理员状态"),
	MERCHANTADD("merchantAdd","新增商户"),
	MERCHANTEDIT("merchantEdit","修改商户"),
	CARD("cardAdd","新增/修改/删除码商"), //和前端cardPeople codeType 一致
	MERCHANTAPIIP("merchantApiIp","修改白名单IP"),
	MERCHANTRESETGOOGLE("merchantResetGoogle","重置谷歌"),
	MERCHANTRESETAPI("merchantResetApi","重置商户密码/私钥"),
	WALLET("wallet","添加钱包");

	
	private String name;
	private String index;

	private EnumMailCodeType(String name, String index) {
		this.name = name;
		this.index = index;
	}

	public static EnumMailCodeType getEnum(String name) {    
        for (EnumMailCodeType c : EnumMailCodeType.values()) {
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
