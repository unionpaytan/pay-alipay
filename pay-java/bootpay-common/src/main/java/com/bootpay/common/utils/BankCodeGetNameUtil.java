package com.bootpay.common.utils;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.bootpay.common.excetion.TranException;


public class BankCodeGetNameUtil {
    
    private static ConcurrentMap<String,Object> concurrentMapWordCounts = new ConcurrentHashMap<String,Object>();
    
    static{
        concurrentMapWordCounts.put("ABC","农业银行");
        concurrentMapWordCounts.put("CITIC","中信银行");
        concurrentMapWordCounts.put("HXB","华夏银行");
        concurrentMapWordCounts.put("ICBC","工商银行");
        concurrentMapWordCounts.put("CMB","招商银行");
        concurrentMapWordCounts.put("CCB","建设银行");
        concurrentMapWordCounts.put("SHB","上海银行");
        concurrentMapWordCounts.put("BJRCB","北京银行");
        concurrentMapWordCounts.put("BOC","中国银行");
        concurrentMapWordCounts.put("GDB","广发银行");
        concurrentMapWordCounts.put("NJCB","南京银行");
        concurrentMapWordCounts.put("HZCB","杭州银行");
        concurrentMapWordCounts.put("SPDB","浦发银行");       
        concurrentMapWordCounts.put("CEB","光大银行");
        concurrentMapWordCounts.put("SDB","平安银行");
        concurrentMapWordCounts.put("PSBC","邮政储蓄银行");
        concurrentMapWordCounts.put("EGBANK","恒丰银行");
        concurrentMapWordCounts.put("SDB","深圳发展银行");
        concurrentMapWordCounts.put("SRCB","农商银行");
        concurrentMapWordCounts.put("SDB","深圳发展银行");

    }
    
    public static String getBankCode(String bankName) throws TranException{
       Object value =  concurrentMapWordCounts.get(bankName);
       if(value == null){
           throw new TranException("-1","暂不支持该银行");
       }
       return value.toString();
    }

}
