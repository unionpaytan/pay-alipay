package com.bootpay.common.utils;



public class DateUtils {
    
//    
//    public static final String DATE_FORMAT_TODAY="yyyy-MM-dd";
//    
//    public static final String DATE_FORMAT_TOSECOND="yyyy-MM-dd HH:mm:ss";
//    
//    /**
//     * 
//     * 描述 获取当前系统时间
//     * @author chenyu
//     * @created 2016年12月14日 下午6:26:42
//     * @param format 格式化类型
//     * @return
//     */
//    public static String getDate(String format){
//       Date date = new Date();
//       SimpleDateFormat sdf = new SimpleDateFormat(format);
//       return sdf.format(date);
//    }
//    
//    
//    /**
//     * 
//     * 描述  
//     * @author chenyu
//     * @created 2016年12月16日 上午8:59:04
//     * @param formate 格式化
//     * @param time  偏移天数 1m为一分钟,1h为一小时，1d为一天
//     * @return
//     */
//    public static String getSpecifiesDate(String formate,String time){
//        Date date = new Date();
//        Calendar calendar = new GregorianCalendar();
//        calendar.setTime(date);
//        String timeType = time.substring(time.length()-1);
//        Integer addTime =Integer.parseInt(time.substring(0,time.length()-1));
//        switch (timeType) {
//            case "m":
//                calendar.add(Calendar.MINUTE,addTime);
//                break;
//            case "h":
//                calendar.add(Calendar.HOUR,addTime);
//                break;
//            case "d" :
//                calendar.add(Calendar.DATE, addTime);
//                break;
//        }
//        date = calendar.getTime();
//        SimpleDateFormat sdf  = new SimpleDateFormat(formate==null||"".equals(formate) ? "yyyy-MM-dd HH:mm:ss" : formate);
//        return sdf.format(date);
//
//    }
//    
//    
//    public static int getOutTimeByMinute(String time){
//        String timeType = time.substring(time.length()-1);
//        Integer addTime =Integer.parseInt(time.substring(0,time.length()-1));
//        Integer minute = 0;
//        switch (timeType) {
//            case "m":
//                minute = addTime;
//                break;
//            case "h":
//                minute = addTime*60;
//                break;
//            case "d" :
//                minute = addTime*60*24;
//                break;
//        }
//        return minute;
//        
//    }
//    public static void main(String[] args) {
//        System.out.println(getSpecifiesDate("yyyyMMddHHmmss","1m"));
//        System.out.println(getOutTimeByMinute("2h"));
//    }

   
}
