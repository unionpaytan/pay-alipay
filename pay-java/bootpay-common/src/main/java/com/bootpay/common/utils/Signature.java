package com.bootpay.common.utils;

//import org.xml.sax.SAXException;

//import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

/**
 * @签名
 * */
public class Signature {

    public static String getSign(Map<String,Object> map,String key){
        ArrayList<String> list = new ArrayList<String>();
        for(Map.Entry<String,Object> entry:map.entrySet()){
            if(entry.getValue()!=null && !"".equals(entry.getValue().toString())){
                list.add(entry.getKey() + "=" + entry.getValue() + "&");
            }
        }
        int size = list.size();
        String [] arrayToSort = list.toArray(new String[size]);
        Arrays.sort(arrayToSort, String.CASE_INSENSITIVE_ORDER);
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < size; i ++) {
            sb.append(arrayToSort[i]);
        }
        String result = sb.toString();
        result += "key=" + key;
        //System.out.println(result);
        result = MD5.MD5Encode(result).toUpperCase();
        return result;
    }

    public static String getSignWithNull(Map<String,Object> map,String key){
        ArrayList<String> list = new ArrayList<String>();
        for(Map.Entry<String,Object> entry:map.entrySet()){
//            if(entry.getValue()!=null && !"".equals(entry.getValue().toString())){
                list.add(entry.getKey() + "=" + entry.getValue() + "&");
//            }
        }
        int size = list.size();
        String [] arrayToSort = list.toArray(new String[size]);
        Arrays.sort(arrayToSort, String.CASE_INSENSITIVE_ORDER);
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < size; i ++) {
            sb.append(arrayToSort[i]);
        }
        String result = sb.toString();
        result += "key=" + key;
        //System.out.println(result);
        result = MD5.MD5Encode(result).toUpperCase();
        return result;
    }


    /**
     * 检验数据里面的签名是否合法，避免数据在传输的过程中被第三方篡改
     * @return API签名是否合法
     * @throws ParserConfigurationException
     * @throws IOException
     * @throws SAXException
     */
    public static boolean checkIsSignValidFromMap(Map<String,Object> map,String key)  {
    	boolean isCheck= true;
			try{
			    String signFromAPIResponse = map.get("sign").toString();
			    if(signFromAPIResponse=="" || signFromAPIResponse == null){
			    	isCheck = false;
			    }
			    //清掉返回数据对象里面的Sign数据（不能把这个数据也加进去进行签名），然后用签名算法进行签名
			    map.put("sign","");
			    //将API返回的数据根据用签名算法进行计算新的签名，用来跟API返回的签名进行比较
			    String signForAPIResponse = Signature.getSign(map,key);
			    if(!signForAPIResponse.equals(signFromAPIResponse)){
			        //签名验不过，表示这个API返回的数据有可能已经被篡改了
			    	isCheck = false;
			    }
			    
			}catch(Exception e){
				isCheck = false;
				e.printStackTrace();
			}
			return isCheck;
    }


}
