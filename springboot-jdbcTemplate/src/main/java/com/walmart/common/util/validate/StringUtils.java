package com.walmart.common.util.validate;

public class StringUtils {
    public static boolean isEmpty(String templateFiletype) {
        return templateFiletype == null || templateFiletype.equals("");
    }


}
