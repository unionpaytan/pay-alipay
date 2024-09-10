package com.bootpay.common.utils;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.math.NumberUtils;

/**
 * 金额转换类
 * 
 */
public class MoneyUtils {

	/**
	 * 金额 元 格式转换对象
	 */
	public static DecimalFormat FORMAT_YUAN = new DecimalFormat("0.00");

	/**
	 * 金额 分 格式转换对象
	 */
	public static DecimalFormat FORMAT_FEN = new DecimalFormat("0");

	/**
	 * 转成 分
	 * 
	 * @param obj
	 * @return
	 */
	public static Double toDoubleCent(Object obj) {
		return NumberUtils.toDouble(BigDecimal.valueOf(NumberUtils.toDouble((String) obj)).multiply(new BigDecimal(100)).toString());
	}

	/**
	 * 转成 分
	 * 
	 * @param obj
	 * @return
	 */
	public static String toStrCent(Object obj) {
		String cent = formatCent(toDoubleCent(obj));
		return cent == null || "0".equals(cent) ? "" : cent;
	}

	/**
	 * 转成 元
	 * 
	 * @param obj
	 * @return
	 */
	public static Double toDoubleYuan(Object obj) {
		return NumberUtils.toDouble(BigDecimal.valueOf(NumberUtils.toDouble((String) obj)).divide(new BigDecimal(100)).toString());
	}

	/**
	 * 转成 元
	 * 
	 * @param obj
	 * @return
	 */
	public static String toStrYuan(Object obj) {
		String yuan = formatYuan(toDoubleYuan(obj));
		return yuan;
	}

	/**
	 * 格式化分 ，变成没有小数点的数字字符串
	 * 
	 * @param obj
	 * @return
	 */
	private static String formatCent(Object obj) {
		return FORMAT_FEN.format(obj);
	}

	/**
	 * 格式化元 ，变成两位小数点的数字字符串
	 * 
	 * @param obj
	 * @return
	 */
	private static String formatYuan(Object obj) {
		if(obj == null || obj.toString().length() <= 0){
			return "";
		}
		return FORMAT_YUAN.format(obj);
	}

	 public static void main(String[] args) {
		 Map<String,Object> map = new HashMap<String,Object>();
		 map.put("a", null);
	 System.out.println(toStrYuan("500"));
	 }
}
