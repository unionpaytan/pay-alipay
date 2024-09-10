package com.bootpay.common.utils;


public class EntityKeyValue implements Comparable<EntityKeyValue>{

    private String key;
    private Object value;
    public String getKey() {
        return key;
    }
    public void setKey(String key) {
        this.key = key;
    }
    public Object getValue() {
        return value;
    }
    public void setValue(Object value) {
        this.value = value;
    }
    public EntityKeyValue(String key, Object value) {
        super();
        this.key = key;
        this.value = value;
    }
    public EntityKeyValue( ) {

    }
    @Override
    public int compareTo(EntityKeyValue o) {


        String key2 = o.getKey();

        if(key == null && key2 != null){
            return -1;
        }

        if(key != null && key2 == null){
            return 1;
        }

        if(key == null && key2 == null){
            return 0;
        }

        int compareTo = this.key.compareTo(key2);

        return compareTo;
    }
}
