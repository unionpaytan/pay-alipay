package com.bootpay.common.utils;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.bootpay.common.excetion.TranException;





/**
 * 
 * 描述 根据总行名称查询行联号
 * @author chenyu
 * @created 2017年10月28日 上午12:06:19
 */
public class BankNameGetBankNumUtil {
    
    private static ConcurrentMap<String,Object> concurrentMapWordCounts = new ConcurrentHashMap<String,Object>();
    
    static{
        concurrentMapWordCounts.put("兴业银行","309391000011");
        concurrentMapWordCounts.put("邮政储蓄银行","403100000004");
        concurrentMapWordCounts.put("招商银行","308584001024");
        concurrentMapWordCounts.put("中国银行","104100000004");
        concurrentMapWordCounts.put("中信银行","302100011106");
        concurrentMapWordCounts.put("工商银行","102100004951");
        concurrentMapWordCounts.put("光大银行","303100010077");
        concurrentMapWordCounts.put("广发银行","306581000003");
        concurrentMapWordCounts.put("华夏银行","304100040000");
        concurrentMapWordCounts.put("建设银行","105100000017");
        concurrentMapWordCounts.put("交通银行","301290011110");
        concurrentMapWordCounts.put("民生银行","305100000013");
        concurrentMapWordCounts.put("农业银行","103100000018");       
        concurrentMapWordCounts.put("平安银行","307584008005");
        concurrentMapWordCounts.put("浦发银行","310290000013");
        concurrentMapWordCounts.put("农业发展银行","103100000018");
        concurrentMapWordCounts.put("上海浦东发展银行","310290000013");
        concurrentMapWordCounts.put("深圳发展银行","307584008005");
        concurrentMapWordCounts.put("邮储银行","403100000004");
        concurrentMapWordCounts.put("中国工商银行","102100004951");
        concurrentMapWordCounts.put("中国光大银行","303100010077");
        concurrentMapWordCounts.put("中国建设银行","105100000017");
        concurrentMapWordCounts.put("中国民生银行","305100000013");
        concurrentMapWordCounts.put("中国农业发展银行","103100000018");
        concurrentMapWordCounts.put("中国农业银行","103100000018");
        concurrentMapWordCounts.put("中国邮政储蓄银行","403100000004");
        concurrentMapWordCounts.put("北京银行","313100000013");
        concurrentMapWordCounts.put("渤海银行","318110000014");
        concurrentMapWordCounts.put("海峡银行","313391080007");
        concurrentMapWordCounts.put("徽商银行","319361000013");
        concurrentMapWordCounts.put("厦门国际银行","781393010011");
        concurrentMapWordCounts.put("浙商银行","316331000018");        
    }
    
    public static String getBankCode(String bankName) throws TranException{
       Object value =  concurrentMapWordCounts.get(bankName);
       if(value == null){
           throw new TranException("-1","未匹配到该银行的行联号");
       }
       return value.toString();
    }

}
