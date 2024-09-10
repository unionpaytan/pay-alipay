package com.bootpay.common.utils;

import java.util.List;

public class ListUtil {
    /**
     * 获取list中存放的最后一个元素
     * @param list
     * @param <T>
     * @return
     */
    public static <T> T getLastElement(List<T> list) {
        return list.get(list.size() - 1);
    }
}
