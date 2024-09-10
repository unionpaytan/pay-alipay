package com.bootpay.core.entity;

import java.io.Serializable;
import java.sql.Time;
import java.sql.Timestamp;

/**
 * <p>
 *  银行名称
 * </p>
 *
 * @author
 * @since 2020-02-27
 */
public class EthInfo implements Serializable {

    private static final long serialVersionUID = 1L;
    private String contractAddress;
    private String from;
    private String gas;
    private String gasPrice;
    private String gasUsed;
    private String hash;
    private String to;
    private String tokenSymbol;
    private String value; //多少USDT
    private String timeStamp;

    public EthInfo(){
        super();
    }
    public EthInfo(String contractAddress, String from, String gas, String gasPrice, String gasUsed, String hash,String to,String tokenSymbol,String value,String timeStamp){
        this.contractAddress = contractAddress;
        this.from = from;
        this.gas = gas;
        this.gasPrice = gasPrice;
        this.gasUsed = gasUsed;
        this.hash = hash;
        this.to = to;
        this.tokenSymbol = tokenSymbol;
        this.value = value;
        this.timeStamp = timeStamp;
    }

    public String getContractAddress() {
        return contractAddress;
    }

    public void setContractAddress(String contractAddress) {
        this.contractAddress = contractAddress;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getGas() {
        return gas;
    }

    public void setGas(String gas) {
        this.gas = gas;
    }

    public String getGasPrice() {
        return gasPrice;
    }

    public void setGasPrice(String gasPrice) {
        this.gasPrice = gasPrice;
    }

    public String getGasUsed() {
        return gasUsed;
    }

    public void setGasUsed(String gasUsed) {
        this.gasUsed = gasUsed;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getTokenSymbol() {
        return tokenSymbol;
    }

    public void setTokenSymbol(String tokenSymbol) {
        this.tokenSymbol = tokenSymbol;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }

    @Override
    public String toString() {
        return "EthInfo{" +
                "contractAddress='" + contractAddress + '\'' +
                ", from='" + from + '\'' +
                ", gas='" + gas + '\'' +
                ", gasPrice='" + gasPrice + '\'' +
                ", gasUsed='" + gasUsed + '\'' +
                ", hash='" + hash + '\'' +
                ", to='" + to + '\'' +
                ", tokenSymbol='" + tokenSymbol + '\'' +
                ", value='" + value + '\'' +
                ", timeStamp=" + timeStamp +
                '}';
    }
}
