package com.bootpay.common.utils;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class MapUtil {
    public static String mapToString(Map<String, String> map) throws UnsupportedEncodingException {
        StringBuilder stringBuilder = new StringBuilder();

        for (String key : map.keySet()) {
            if (stringBuilder.length() > 0) {
                stringBuilder.append("&");
            }
            String value = map.get(key);
            stringBuilder.append((key != null ? URLEncoder.encode(key, "UTF-8") : ""));
            stringBuilder.append("=");
            stringBuilder.append(value != null ? URLEncoder.encode(value, "UTF-8") : "");
        }

        return stringBuilder.toString();
    }

    public static Map<String, String> stringToMap(String input) throws UnsupportedEncodingException {
        Map<String, String> map = new HashMap<String, String>();

        String[] nameValuePairs = input.split("&");
        for (String nameValuePair : nameValuePairs) {
            String[] nameValue = nameValuePair.split("=");
            map.put(URLDecoder.decode(nameValue[0], "UTF-8"), nameValue.length > 1 ? URLDecoder.decode(
                    nameValue[1], "UTF-8") : "");
        }
        return map;
    }
}