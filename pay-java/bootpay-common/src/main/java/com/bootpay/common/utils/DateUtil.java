package com.bootpay.common.utils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * 日期函数
 *
 */
public class DateUtil {

	public static String FULL_TIME_FORMAT_EN = "yyyyMMddHHmmss";
	public static String FULL_TIME_FORMAT_CH = "yyyy年MM月dd日HH点mm分";

	public static String FORMAT_DATE = "yyyy-MM-dd HH:mm:ss";

	/**
	 * 日期格式 dd/MM/yyyy 分割线:斜杠
	 */
	public static String FORMAT_DD_MM_YYYY_SLASH = "dd/MM/yyyy";
	/**
	 * 日期格式 dd/MM/yyyy 分割线:斜杠
	 */
	public static String FORMAT_MM_DD_YYYY_SLASH = "MM/dd/yyyy";

	/**
	 * 日期格式 yyyyMMdd
	 */
	public static String FORMAT_YYYYMMDD = "yyyyMMdd";

	/**
	 * 时间格式 HH:mm:ss
	 */
	public static String FORMAT_HHMMSS = "HH:mm:ss";

	public static Long getTimestamp() {
		return System.currentTimeMillis();
	}

	/**
	 * 要获得秒级时间戳，可以使用毫秒级时间戳除以1000即可
	 * https://www.cnblogs.com/-beyond/p/11462323.html#jinzhi
	 * 时间截 Date date = new Date();
	 * */
	public  static  Long getTimestampTillSeconds(){
		Long seconds = new Date().getTime();
		return seconds / 1000;
	}

	public static String getDate() {
		return format(new Date(),"yyyy-MM-dd");
	}
	public static String getMonth() {
		return format(new Date(),"yyyy-MM");
	}

	/**
	 * 格式化时间
	 *
	 * @param date
	 * @param format
	 * @return
	 */
	public static String format(Date date, String format) {
		return new SimpleDateFormat(format).format(date);
	}

	public static Date parse(String date, String format) {
		SimpleDateFormat d = new SimpleDateFormat(format);
		try {
			return d.parse(date);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static String parse(String dateTime) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		try {
			Date date = sdf.parse(dateTime);
			return format(date, FULL_TIME_FORMAT_EN);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 切换时间格式
	 *
	 * @param date
	 * @param srcFormat
	 * @param descFormat
	 * @return
	 */
	public static String switchFormat(String date, String srcFormat, String descFormat) {
		SimpleDateFormat src = new SimpleDateFormat(srcFormat);
		SimpleDateFormat desc = new SimpleDateFormat(descFormat);
		try {
			return desc.format(src.parse(date));
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 获取当月第一天
	 *
	 * @return
	 */
	public static String getMonthFirstDay() {
		Calendar cal = Calendar.getInstance();
		return getMonthFirstDay(cal, FORMAT_YYYYMMDD);
	}

	public static String getMonthFirstDay(Date date, String format) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		return getMonthFirstDay(cal, format);
	}

	public static String convertDateToString(Date date, String format) {
		DateFormat d = new SimpleDateFormat(format);
		return d.format(date);
	}

	public static String getMonthFirstDay(long millis, String format) {
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(millis);
		return getMonthFirstDay(cal, format);
	}

	private static String getMonthFirstDay(Calendar cal, String format) {
		SimpleDateFormat sdf = new SimpleDateFormat(format);
		// 当前月的第一天
		cal.set(GregorianCalendar.DAY_OF_MONTH, 1);
		return sdf.format(cal.getTime());
	}

	public static String getMonthEndDay() {
		Calendar cal = Calendar.getInstance();
		return getMonthEndDay(cal.getTime(), FORMAT_YYYYMMDD);
	}

	/**
	 * 获取当月最后一天
	 *
	 * @return
	 */
	public static String getMonthEndDay(Date date, String format) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		return getMonthEndDay(cal, format);
	}

	public static String getMonthEndDay(long millis, String format) {
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(millis);
		return getMonthEndDay(cal, format);
	}

	private static String getMonthEndDay(Calendar cal, String format) {
		SimpleDateFormat datef = new SimpleDateFormat(format);
		cal.set(Calendar.DATE, 1);
		cal.roll(Calendar.DATE, -1);
		Date endTime = cal.getTime();
		return datef.format(endTime);
	}

	public static Date addDate(Date date, int field, int num) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		calendar.add(field, num);
		return calendar.getTime();
	}

	public static Date addDate(Long millis, int field, int num) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(millis);
		calendar.add(field, num);
		return calendar.getTime();
	}

	public static String now() {
		return new SimpleDateFormat(FULL_TIME_FORMAT_EN).format(new Date());
	}

	/**
	 * 判断 date 是否在 某个时间范围内
	 *
	 * @param date
	 * @param bTime
	 *            开始时间 HH:mm:ss
	 * @param eTime
	 *            结束时间 HH:mm:ss
	 * @return
	 */
	public static boolean isEffectiveDate(Date date, String bTime, String eTime) {
		try {
			String strDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date).substring(11, 19);
			Long nowTime = new SimpleDateFormat(FORMAT_HHMMSS).parse(strDate).getTime();
			Long startTime = new SimpleDateFormat(FORMAT_HHMMSS).parse(bTime).getTime();
			Long endTime = new SimpleDateFormat(FORMAT_HHMMSS).parse(eTime).getTime();
			if (startTime <= nowTime && nowTime <= endTime) {
				return true;
			}
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}



	/**
	 * 判断时间格式是否正确
	 * @param str
	 * @param Dateformat
	 * @return
	 */
	public static boolean isValidDate(String str,String Dateformat) {
		boolean convertSuccess = true;
		SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");
		try {
			// 设置lenient为false.
			// 否则SimpleDateFormat会比较宽松地验证日期，比如2007/02/29会被接受，并转换成2007/03/01
			format.setLenient(false);
			format.parse(str);
		} catch (ParseException e) {
			e.printStackTrace();
			convertSuccess = false;
		}
		return convertSuccess;
	}



}
