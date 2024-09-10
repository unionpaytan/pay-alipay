package com.bootpay.common.utils;

import org.apache.commons.lang.StringUtils;

import java.lang.reflect.Field;
import java.util.*;

public class ParamUtils {

	
	
	/**
	 * 获取对象及父类的所有属性
	 * @param obj
	 * @return
	 */
	@SuppressWarnings({ "rawtypes"})
	public static Map getObjectToMap(Object obj) {
		List<Field> fieldList = new ArrayList<Field>();
		Map<String, Object> map = new HashMap<String, Object>();
		Class tempClass = obj.getClass();
		// 获取f对象对应类中的所有属性域
		while (tempClass != null) {// 当父类为null的时候说明到达了最上层的父类(Object类).
			fieldList.addAll(Arrays.asList(tempClass.getDeclaredFields()));
			tempClass = tempClass.getSuperclass(); // 得到父类,然后赋给自己
		}
		try {
			for (Field f : fieldList) {
				f.setAccessible(true);
				if(f.get(obj) != null && !"".equals(obj) && obj.toString().length()!=0){
					map.put(f.getName(), f.get(obj));
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		map.remove("class");
		return map;
	}
	

	
	/**
	 * map转java bean
	 * @param map
	 * @param beanClass
	 * @return
	 * @throws Exception
	 */
	public static Object mapToObject(Map<String, String> map, Class<?> beanClass) throws Exception {
		if (map == null)
			return null;

		Object obj = beanClass.newInstance();

		org.apache.commons.beanutils.BeanUtils.populate(obj, map);

		return obj;
	}


	
	
	public static String getUrlContent(Map<String, String> sortedParams) {
		StringBuffer content = new StringBuffer();
		List<String> keys = new ArrayList<String>(sortedParams.keySet());
		Collections.sort(keys);
		int index = 0;
		for (int i = 0; i < keys.size(); i++) {
			String key = keys.get(i);
			String value = sortedParams.get(key);
			if (!StringUtils.isBlank(value)) {
				content.append((index == 0 ? "" : "&") + key + "=" + value);
				index++;
			}
		}
		return content.toString();
	}
	
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public static SortedMap GetObjAllToSortedMap(Object obj) {
        List<Field> fieldList = new ArrayList<Field>();
        SortedMap map = new TreeMap();
        Class tempClass = obj.getClass();
        // 获取f对象对应类中的所有属性域
        while (tempClass != null) {// 当父类为null的时候说明到达了最上层的父类(Object类).
            fieldList.addAll(Arrays.asList(tempClass.getDeclaredFields()));
            tempClass = tempClass.getSuperclass(); // 得到父类,然后赋给自己
        }
        for (Field f : fieldList) {
            f.setAccessible(true);
            try {
                if(f.get(obj) != null){
                    map.put(f.getName(), f.get(obj));
                }   
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } 
        }
        return map;
    }


}
