package com.walmart.common.util.req;

import java.util.HashMap;
import java.util.Map;

public class PageUtil {
    private Integer start;
    private Integer size;

    public Integer getStart() {
        if(start == null || start <= 1){
            start = 0;
        }
        return start*getSize();
    }

    public void setStart(Integer start) {
        this.start = start;
    }

    public Integer getSize() {
        if(size == null || size <= 0){
            size = 20;
        }
        return size;
    }

    public void setSize(Integer size) {
        this.size = size;
    }

    public static Map toPage(Object object, Object totalElements) {
        Map map = new HashMap();
        map.put("content",object);
        map.put("totalElements",totalElements);
        return map;
    }
}
