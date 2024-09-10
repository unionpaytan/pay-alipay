package com.bootpay.common.constants.enums;

public enum EnumAgentRate {
    MERCHANT("0","商户"),
    AGENT_RATE1("1","普通代理"),
    AGENT_RATE2("2","总代理商"),
    CARD("-1","码商"),
    OPERATOR("-2","码商操作员"),
    FOLLOWERS("-3","收款人");

    private String name;
    private String index;

    private EnumAgentRate(String name,String index){
        this.name = name;
        this.index = index;
    }

    public static EnumAgentRate getEnum(String name) {
        for (EnumAgentRate c : EnumAgentRate.values()) {
            if (c.getName().equals(name)) {
                return c;
            }
        }
        return null;
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
