package com.bootpay.common.utils;

import org.apache.commons.lang.StringUtils;

import java.util.*;

public class SignUtil {
    public static final String sign_key = "sign";

    public static void main(String[] args) throws Exception {


    }

    /**
     * 均是排序后签名	url签名 例	aaa=111&ccc=444&ddd=333
     */
    public static final Integer sign_type_sign_after_sort_url = 200;
    /**
     * 均是排序后签名	名称值签名
     */
    public static final Integer sign_type_sign_after_sort_name_plus_value = 300;
    /**
     * 均是排序后签名	值签名
     */
    public static final Integer sign_type_sign_after_sort_only_value = 502;

    /**
     * 默认签名剔除名为 	值 这个是客户端上传的
     * 如果是校验上来的签名名称不是sign 需要自行先把 sign值剔除
     *
     * @param entityKeyValue_list 签名的键值对
     * @param user_sign_key       签名的密钥
     * @param sign_type           签名类型
     * @return 签名的值 统一返回小写
     */
    public static void add_sign(List<EntityKeyValue> entityKeyValue_list, String user_sign_key, Integer sign_type) {
        String sign_after_sort = sign_after_sort(entityKeyValue_list, user_sign_key, sign_type);
        entityKeyValue_list.add(new EntityKeyValue(SignUtil.sign_key, sign_after_sort));
    }

    /**
     * 默认签名剔除名为 	值 这个是客户端上传的
     * 如果是校验上来的签名名称不是sign 需要自行先把 sign值剔除
     *
     * @param entityKeyValue_list 签名的键值对
     * @param user_sign_key       签名的密钥
     * @param sign_type           签名类型
     * @return 签名的值 统一返回小写
     */
    public static String sign_after_sort(List<EntityKeyValue> entityKeyValue_list, String user_sign_key, Integer sign_type) {

        if (sign_type_sign_after_sort_url.equals(sign_type)) {
            return sign_after_sort_url(entityKeyValue_list, user_sign_key);
        }

        if (sign_type_sign_after_sort_name_plus_value.equals(sign_type)) {
            return sign_after_sort_name_plus_value(entityKeyValue_list, user_sign_key);
        }

        if (sign_type_sign_after_sort_only_value.equals(sign_type)) {
            return sign_after_sort_only_value(entityKeyValue_list, user_sign_key);
        }

        throw new RuntimeException("签名类型不存在!");
    }

    /**
     * 默认签名剔除名为 sign	值 这个是客户端上传的
     *
     * @param parameterMap  签名的键值对
     * @param user_sign_key 签名的密钥
     * @param sign_type     签名类型
     * @return 签名的值 统一返回小写
     */
    public static String sign_after_sort(Map parameterMap, String user_sign_key, Integer sign_type) {
        List<EntityKeyValue> convert_parameterMap_to_entityKeyValue = convert_parameterMap_to_entityKeyValue(parameterMap);
        return sign_after_sort(convert_parameterMap_to_entityKeyValue, user_sign_key, sign_type);
    }

    /**
     * 默认签名剔除名为 sign	值 这个是客户端上传的
     * <p>
     * 通过前置名称排序后 对值签名	名称或值为null的都替换空字符串参与签名
     * sign_key 是 kkkkkkk
     * 比如 键值对的值是
     * aaa 111
     * ddd	333
     * ccc	444
     * <p>
     * 第一步排序
     * aaa 111
     * ccc	444
     * ddd	333
     * <p>
     * 组装字符串	111444333
     * 加上密钥	111444333kkkkkkk
     * 最后md5
     *
     * @param entityKeyValue_list 签名的键值对
     * @param user_sign_key       签名的密钥
     * @return 签名的值 统一返回小写
     */
    public static String sign_after_sort_only_value(List<EntityKeyValue> entityKeyValue_list, String user_sign_key) {

        StringBuilder build_sign_string_after_sort_only_value = build_sign_string_after_sort_only_value(entityKeyValue_list, user_sign_key);

        String md5Normal = Md5Util.MD5Normal(build_sign_string_after_sort_only_value.toString());

        return md5Normal;
    }

    /**
     * @param entityKeyValue_list 签名的键值对
     * @param user_sign_key       签名的密钥
     * @return 签名的值 统一返回小写
     */
    public static StringBuilder build_sign_string_after_sort_only_value(List<EntityKeyValue> entityKeyValue_list, String user_sign_key) {

        Collections.sort(entityKeyValue_list);

        StringBuilder stringBuilder = new StringBuilder();

        for (EntityKeyValue entityKeyValue : entityKeyValue_list) {

            String key = entityKeyValue.getKey();
            //如果这个值名称 是签名的名称 不参与签名
            if (sign_key.equals(key)) {
                continue;
            }

            Object value = entityKeyValue.getValue();

            if (value == null) {
                value = "";
            }

            stringBuilder.append(value);
        }
        stringBuilder.append(user_sign_key);

        return stringBuilder;
    }

    /**
     * 默认签名剔除参数名为 sign	值 这个是客户端上传的
     * 通过前置名称排序后 对值签名	名称或值为null的都替换空字符串参与签名
     * sign_key 是 kkkkkkk
     * 比如 键值对的值是
     * aaa 111
     * ddd	333
     * ccc	444
     * <p>
     * 第一步排序
     * aaa 111
     * ccc	444
     * ddd	333
     * <p>
     * 组装字符串	aaa111ccc444ddd333
     * 加上密钥	aaa111ccc444ddd333kkkkkkk
     * 最后md5
     *
     * @param entityKeyValue_list 签名的键值对
     * @param user_sign_key       签名的密钥
     * @return 签名的值 统一返回小写
     */
    public static String sign_after_sort_only_value(Map parameterMap, String user_sign_key) {

        List<EntityKeyValue> convert_parameterMap_to_entityKeyValue = convert_parameterMap_to_entityKeyValue(parameterMap);

        return sign_after_sort_only_value(convert_parameterMap_to_entityKeyValue, user_sign_key);
    }

    /**
     * 默认签名剔除参数名为 sign	值 这个是客户端上传的
     * 通过前置名称排序后 对值签名	名称或值为null的都替换空字符串参与签名
     * sign_key 是 kkkkkkk
     * 比如 键值对的值是
     * aaa 111
     * ddd	333
     * ccc	444
     * <p>
     * 第一步排序
     * aaa 111
     * ccc	444
     * ddd	333
     * <p>
     * 组装字符串	aaa=111&ccc=444&ddd=333
     * 加上密钥	aaa=111&ccc=444&ddd=333kkkkkkk
     * 最后md5
     *
     * @param entityKeyValue_list 签名的键值对
     * @param user_sign_key       签名的密钥
     * @return 签名的值 统一返回小写
     */
    public static String sign_after_sort_url(List<EntityKeyValue> entityKeyValue_list, String user_sign_key) {

        StringBuilder build_sign_string_after_sort_url = build_sign_string_after_sort_url(entityKeyValue_list, user_sign_key);

        String md5Normal = Md5Util.MD5Normal(build_sign_string_after_sort_url.toString());

        return md5Normal;
    }

    /**
     * @param entityKeyValue_list 签名的键值对
     * @param user_sign_key       签名的密钥
     * @return 签名的值 统一返回小写
     */
    public static StringBuilder build_sign_string_after_sort_url(List<EntityKeyValue> entityKeyValue_list, String user_sign_key) {

        Collections.sort(entityKeyValue_list);

        StringBuilder stringBuilder = new StringBuilder();

        for (EntityKeyValue entityKeyValue : entityKeyValue_list) {

            String key = entityKeyValue.getKey();
            Object value = entityKeyValue.getValue();

            //如果这个值名称 是签名的名称 不参与签名
            if (sign_key.equals(key)) {
                continue;
            }

            if (value == null) {
                value = "";
            }
            if (key == null) {
                key = "";
            }
            stringBuilder.append("&");
            stringBuilder.append(key);
            stringBuilder.append("=");
            stringBuilder.append(value);
        }

        if (stringBuilder.length() > 0) {
            stringBuilder.deleteCharAt(0);
        }

        stringBuilder.append(user_sign_key);

        return stringBuilder;
    }

    /**
     * 默认签名剔除参数名为 sign	值 这个是客户端上传的
     * 通过前置名称排序后 对值签名	名称或值为null的都替换空字符串参与签名
     * sign_key 是 kkkkkkk
     * 比如 键值对的值是
     * aaa 111
     * ddd	333
     * ccc	444
     * <p>
     * 第一步排序
     * aaa 111
     * ccc	444
     * ddd	333
     * <p>
     * 组装字符串	aaa111ccc444ddd333
     * 加上密钥	aaa111ccc444ddd333kkkkkkk
     * 最后md5
     *
     * @param entityKeyValue_list 签名的键值对
     * @param user_sign_key       签名的密钥
     * @return 签名的值 统一返回小写
     */
    public static String sign_after_sort_url(Map parameterMap, String user_sign_key) {

        List<EntityKeyValue> convert_parameterMap_to_entityKeyValue = convert_parameterMap_to_entityKeyValue(parameterMap);

        return sign_after_sort_url(convert_parameterMap_to_entityKeyValue, user_sign_key);
    }

    /**
     * 默认签名剔除参数名为 sign	值 这个是客户端上传的
     * 通过前置名称排序后 对值签名	名称或值为null的都替换空字符串参与签名
     * sign_key 是 kkkkkkk
     * 比如 键值对的值是
     * aaa 111
     * ddd	333
     * ccc	444
     * <p>
     * 第一步排序
     * aaa 111
     * ccc	444
     * ddd	333
     * <p>
     * 组装字符串	aaa111ccc444ddd333
     * 加上密钥	aaa111ccc444ddd333kkkkkkk
     * 最后md5
     *
     * @param entityKeyValue_list 签名的键值对
     * @param user_sign_key       签名的密钥
     * @return 签名的值 统一返回小写
     */
    public static String sign_after_sort_name_plus_value(List<EntityKeyValue> entityKeyValue_list, String user_sign_key) {

        StringBuilder build_sign_string_after_sort_name_plus_value = build_sign_string_after_sort_name_plus_value(entityKeyValue_list, user_sign_key);

        String md5Normal = Md5Util.MD5Normal(build_sign_string_after_sort_name_plus_value.toString());

        return md5Normal;
    }

    /**
     * @param entityKeyValue_list 签名的键值对
     * @param user_sign_key       签名的密钥
     * @return 签名的值 统一返回小写
     */
    public static StringBuilder build_sign_string_after_sort_name_plus_value(List<EntityKeyValue> entityKeyValue_list, String user_sign_key) {

        Collections.sort(entityKeyValue_list);

        StringBuilder stringBuilder = new StringBuilder();

        for (EntityKeyValue entityKeyValue : entityKeyValue_list) {

            String key = entityKeyValue.getKey();
            Object value = entityKeyValue.getValue();
            //如果这个值名称 是签名的名称 不参与签名
            if (sign_key.equals(key)) {
                continue;
            }

            if (key == null) {
                key = "";
            }
            if (value == null) {
                value = "";
            }

            stringBuilder.append(key);
            stringBuilder.append(value);
        }

        stringBuilder.append(user_sign_key);

        return stringBuilder;
    }

    /**
     * @create 签名 sign
     */
    public  static <K, V> void orderByValue(LinkedHashMap<K, V> m, final Comparator<? super V> c) {

        List<Map.Entry<K, V>> entries = new ArrayList<>(m.entrySet());

        Collections.sort(entries, new Comparator<Map.Entry<K, V>>() {
            @Override
            public int compare(Map.Entry<K, V> lhs, Map.Entry<K, V> rhs) {
                return c.compare(lhs.getValue(), rhs.getValue());
            }
        });

        m.clear();
        for(Map.Entry<K, V> e : entries) {
            m.put(e.getKey(), e.getValue());
        }
    }

    public static String createSign(TreeMap<String, Object> params, String privateKey){
        StringBuilder sb = new StringBuilder();
        // 将参数以参数名的字典升序排序
        Map<String, Object> sortParams = new TreeMap<String, Object>(params);
        // 遍历排序的字典,并拼接"key=value"格式
        for (Map.Entry<String, Object> entry : sortParams.entrySet()) {
            String key = entry.getKey();
            String value =  entry.getValue().toString();
            if (!StringUtils.isEmpty(value))
                sb.append("&").append(key).append("=").append(value);
        }
        String stringA = sb.toString().replaceFirst("&","");
        String stringSignTemp = stringA + "&"+"key="+privateKey;
        //将签名使用MD5加密并全部字母变为大写
         String signValue = Md5Util.MD5(stringSignTemp).toUpperCase();
        // System.out.println("stringA+privateKey后MD5加密+转换全部大写生成sign为：       "+signValue);
        return signValue;
    }

    public static String createSign(TreeMap<String, Object> params, String privateKey,boolean withoutKeyString ){
        StringBuilder sb = new StringBuilder();
        // 将参数以参数名的字典升序排序
        Map<String, Object> sortParams = new TreeMap<String, Object>(params);
        // 遍历排序的字典,并拼接"key=value"格式
        for (Map.Entry<String, Object> entry : sortParams.entrySet()) {
            String key = entry.getKey();
            String value =  entry.getValue().toString();
            if (!StringUtils.isEmpty(value))
                sb.append("&").append(key).append("=").append(value);
        }
        String stringA = sb.toString().replaceFirst("&","");
        //String stringSignTemp = withoutKeyString ? stringA + "&"+privateKey : stringA + "&key="+privateKey;
        String stringSignTemp = stringA + "&"+privateKey; //without key string "&key="+privateKey
        //将签名使用MD5加密并全部字母变为大写
        String signValue = Md5Util.MD5(stringSignTemp).toUpperCase();
        // System.out.println("stringA+privateKey后MD5加密+转换全部大写生成sign为：       "+signValue);
        return signValue;
    }

    /**
     * 默认签名剔除名为 	值 这个是客户端上传的
     * 如果是校验上来的签名名称不是sign 需要自行先把 sign值剔除
     *
     * @param entityKeyValue_list 签名的键值对
     * @param user_sign_key       签名的密钥
     * @param sign_type           签名类型
     * @return 签名的值 统一返回小写
     */
    public static String build_sign_string_after_sort(List<EntityKeyValue> entityKeyValue_list, String user_sign_key, Integer sign_type) {

        if (sign_type_sign_after_sort_url.equals(sign_type)) {
            return build_sign_string_after_sort_url(entityKeyValue_list, user_sign_key).toString();
        }

        if (sign_type_sign_after_sort_name_plus_value.equals(sign_type)) {
            return build_sign_string_after_sort_name_plus_value(entityKeyValue_list, user_sign_key).toString();
        }

        if (sign_type_sign_after_sort_only_value.equals(sign_type)) {
            return build_sign_string_after_sort_only_value(entityKeyValue_list, user_sign_key).toString();
        }

        throw new RuntimeException("签名类型不存在!");
    }

    /**
     * 默认签名剔除参数名为 sign	值 这个是客户端上传的
     * 通过前置名称排序后 对值签名	名称或值为null的都替换空字符串参与签名
     * sign_key 是 kkkkkkk
     * 比如 键值对的值是
     * aaa 111
     * ddd	333
     * ccc	444
     * <p>
     * 第一步排序
     * aaa 111
     * ccc	444
     * ddd	333
     * <p>
     * 组装字符串	aaa111ccc444ddd333
     * 加上密钥	aaa111ccc444ddd333kkkkkkk
     * 最后md5
     *
     * @param entityKeyValue_list 签名的键值对
     * @param user_sign_key       签名的密钥
     * @return 签名的值 统一返回小写
     */
    public static String sign_after_sort_name_plus_value(Map parameterMap, String user_sign_key) {

        List<EntityKeyValue> convert_parameterMap_to_entityKeyValue = convert_parameterMap_to_entityKeyValue(parameterMap);

        return sign_after_sort_name_plus_value(convert_parameterMap_to_entityKeyValue, user_sign_key);
    }

    /**
     * 将请求的参数map转换为 List<EntityKeyValue>
     *
     * @param parameterMap
     * @return List<EntityKeyValue>
     */
    public static List<EntityKeyValue> convert_parameterMap_to_entityKeyValue(Map parameterMap) {

        List<EntityKeyValue> entityKeyValue_list = new LinkedList<EntityKeyValue>();

        Set<String> keySet = parameterMap.keySet();

        for (String parameter_name : keySet) {
            String[] parameter_values = (String[]) parameterMap.get(parameter_name);
            if (parameter_values != null) {
                for (String parameter_value : parameter_values) {
                    entityKeyValue_list.add(new EntityKeyValue(parameter_name, parameter_value));
                }
            }
        }

        return entityKeyValue_list;
    }

    /**
     * 清理值为null的键值对 List<EntityKeyValue>
     *
     * @param entityKeyValue_list
     */
    public static void clear_entityKeyValue_null_value(List<EntityKeyValue> entityKeyValue_list) {

        for (int i = 0; i < entityKeyValue_list.size(); i++) {

            EntityKeyValue entityKeyValue = entityKeyValue_list.get(i);

            Object value = entityKeyValue.getValue();

            if (value == null) {
                entityKeyValue_list.remove(i);
            } else {
                i++;
            }

        }
    }

    /**
     * 清理值为null 或toString 后的字符串为空的 的键值对 List<EntityKeyValue>
     *
     * @param entityKeyValue_list
     */
    public static void clear_entityKeyValue_empty_value(List<EntityKeyValue> entityKeyValue_list) {

        for (int i = 0; i < entityKeyValue_list.size() && entityKeyValue_list.size() > 0; ) {

            EntityKeyValue entityKeyValue = entityKeyValue_list.get(i);

            Object value = entityKeyValue.getValue();

            if (value == null || value.toString().isEmpty()) {
                entityKeyValue_list.remove(i);
            } else {
                i++;
            }

        }
    }
}
