package com.bootpay.common.utils;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.bootpay.common.excetion.TranException;


public class BankNameNoUtil {
    
    private static ConcurrentMap<String,Object> concurrentMapWordCounts = new ConcurrentHashMap<String,Object>();
    
    static{
        concurrentMapWordCounts.put("兴业银行","CIB");
        concurrentMapWordCounts.put("邮政储蓄银行","PSBC");
        concurrentMapWordCounts.put("招商银行","CMB");
        concurrentMapWordCounts.put("中国银行","BOC");
        concurrentMapWordCounts.put("中信银行","CITIC");
        concurrentMapWordCounts.put("工商银行","ICBC");
        concurrentMapWordCounts.put("光大银行","CEB");
        concurrentMapWordCounts.put("广发银行","GDB");
        concurrentMapWordCounts.put("华夏银行","HXB");
        concurrentMapWordCounts.put("建设银行","CCB");
        concurrentMapWordCounts.put("交通银行","BCM");
        concurrentMapWordCounts.put("民生银行","CMBC");
        concurrentMapWordCounts.put("农业银行","ABC");       
        concurrentMapWordCounts.put("平安银行","PAB");
        concurrentMapWordCounts.put("浦发银行","SPDB");
        concurrentMapWordCounts.put("农业发展银行","ADBC");
        concurrentMapWordCounts.put("上海浦东发展银行","SPDB");
        concurrentMapWordCounts.put("深圳发展银行","SDB");
        concurrentMapWordCounts.put("邮储银行","PSBC");
        concurrentMapWordCounts.put("中国工商银行","ICBC");
        concurrentMapWordCounts.put("中国光大银行","CEB");
        concurrentMapWordCounts.put("中国建设银行","CCB");
        concurrentMapWordCounts.put("中国民生银行","CMBC");
        concurrentMapWordCounts.put("中国农业发展银行","ABC");
        concurrentMapWordCounts.put("中国农业银行","ABC");
        concurrentMapWordCounts.put("中国邮储银行","PSBC");
        concurrentMapWordCounts.put("北京银行","BOB");
        concurrentMapWordCounts.put("渤海银行","CBHB");
        concurrentMapWordCounts.put("海峡银行","FJHXB");
        concurrentMapWordCounts.put("徽商银行","HSB");
        concurrentMapWordCounts.put("厦门国际银行","XIB");
        concurrentMapWordCounts.put("浙商银行","CZB");
        concurrentMapWordCounts.put("农商银行","RCB");
        concurrentMapWordCounts.put("农村信用合作社","RCB");
        concurrentMapWordCounts.put("上海银行","SHB");
    }
    
    public static String getBankCode(String bankName) throws TranException{
       Object value =  concurrentMapWordCounts.get(bankName);
       if(value == null){
           throw new TranException("-1","暂不支持该银行");
       }
       return value.toString();
    }

}
