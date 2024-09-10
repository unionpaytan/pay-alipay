package com.bootpay.core.entity;

import java.io.Serializable;
import java.sql.Timestamp;

/**
 * <p>
 *  银行名称
 * </p>
 *
 * @author
 * @since 2020-02-27
 */
public class TokenInfo implements Serializable {

    private static final long serialVersionUID = 1L;
    private String address;
    private String name;
    private String symbol;


    public TokenInfo(){
        super();
    }
    public TokenInfo(String address, String name, String symbol){
        this.address = address;
        this.name = name;
        this.symbol = symbol;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    @Override
    public String toString() {
        return "TokenInfo{" +
                "address='" + address + '\'' +
                ", name='" + name + '\'' +
                ", symbol='" + symbol + '\'' +
                '}';
    }
}
