package com.bootpay.core.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;

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
public class TransInfo implements Serializable {

    private static final long serialVersionUID = 1L;
    private String transaction_id;
    private String from;
    private String to;
    private String type;
    private String value;
    private Timestamp block_timestamp;
    private TokenInfo token_info;

    public TransInfo(){
        super();
    }
    public TransInfo(String transaction_id,String from,String to,String type,String value,Timestamp block_timestamp,TokenInfo token_info){
        this.transaction_id = transaction_id;
        this.from = from;
        this.to = to;
        this.type = type;
        this.value = value;
        this.block_timestamp = block_timestamp;
        this.token_info = token_info;
    }

    public String getTransaction_id() {
        return transaction_id;
    }

    public void setTransaction_id(String transaction_id) {
        this.transaction_id = transaction_id;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Timestamp getBlock_timestamp() {
        return block_timestamp;
    }

    public void setBlock_timestamp(Timestamp block_timestamp) {
        this.block_timestamp = block_timestamp;
    }

    public TokenInfo getToken_info() {
        return token_info;
    }

    public void setToken_info(TokenInfo token_info) {
        this.token_info = token_info;
    }

    @Override
    public String toString() {
        return "TransInfo{" +
                "transaction_id='" + transaction_id + '\'' +
                ", from='" + from + '\'' +
                ", to='" + to + '\'' +
                ", type='" + type + '\'' +
                ", value='" + value + '\'' +
                ", block_timestamp=" + block_timestamp +
                ", token_info=" + token_info +
                '}';
    }
}
