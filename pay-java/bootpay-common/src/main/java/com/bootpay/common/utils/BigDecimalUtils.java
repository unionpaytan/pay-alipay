package com.bootpay.common.utils;

import java.math.BigDecimal;



/** 
 * BigDecimal比较大小的工具类 
 * Created with IntelliJ IDEA. 
 * User: chen_daoliang 
 * Date: 2018/9/17 
 * Time: 12:25 
 * To change this template use File | Settings | File and Code Templates. 
 */  
public final class BigDecimalUtils {  
  
    private BigDecimalUtils() {  
  
    }  
  
    /** 
     * 判断num1是否小于num2 
     * 
     * @param num1 
     * @param num2 
     * @return num1小于num2返回true 
     */  
    public static boolean lessThan(BigDecimal num1, BigDecimal num2) {  
        return num1.compareTo(num2) == -1;  
    }  
  
    /** 
     * 判断num1是否小于等于num2 
     * 
     * @param num1 
     * @param num2 
     * @return num1小于或者等于num2返回true 
     */  
    public static boolean lessEqual(BigDecimal num1, BigDecimal num2) {  
        return (num1.compareTo(num2) == -1) || (num1.compareTo(num2) == 0);  
    }  
  
    /** 
     * 判断num1是否大于num2 
     * 
     * @param num1 
     * @param num2 
     * @return num1大于num2返回true 
     */  
    public static boolean greaterThan(BigDecimal num1, BigDecimal num2) {  
        return num1.compareTo(num2) == 1;  
    }  
  
    /** 
     * 判断num1是否大于等于num2 
     * 
     * @param num1 
     * @param num2 
     * @return num1大于或者等于num2返回true 
     */  
    public static boolean greaterEqual(BigDecimal num1, BigDecimal num2) {  
        return (num1.compareTo(num2) == 1) || (num1.compareTo(num2) == 0);  
    }  
  
    /** 
     * 判断num1是否等于num2 
     * 
     * @param num1 
     * @param num2 
     * @return num1等于num2返回true 
     */  
    public static boolean equal(BigDecimal num1, BigDecimal num2) {  
        return num1.compareTo(num2) == 0;  
    }

    public static boolean isNumeric(final String str) {
        java.util.regex.Pattern pattern=java.util.regex.Pattern.compile("^(([1-9]{1}\\d*)|([0]{1}))(\\.(\\d){0,4})?$"); // 判断小数点后6位的数字的正则表达式
        java.util.regex.Matcher match=pattern.matcher(str);
        if(match.matches()==false)
        {
            return false;
        }
        else
        {
            return true;
        }
    }


    public static boolean isRmbNumeric(final String str) {
        java.util.regex.Pattern pattern=java.util.regex.Pattern.compile("^(([1-9]{1}\\d*)|([0]{1}))(\\.(\\d){0,2})?$"); // 判断小数点后6位的数字的正则表达式
        java.util.regex.Matcher match=pattern.matcher(str);
        if(match.matches()==false)
        {
            return false;
        }
        else
        {
            return true;
        }
    }

    public static boolean isUsdtNumeric(final String str) {
        java.util.regex.Pattern pattern=java.util.regex.Pattern.compile("^(([1-9]{1}\\d*)|([0]{1}))(\\.(\\d){0,3})?$"); // 判断小数点后6位的数字的正则表达式
        java.util.regex.Matcher match=pattern.matcher(str);
        if(match.matches()==false)
        {
            return false;
        }
        else
        {
            return true;
        }
    }

}
