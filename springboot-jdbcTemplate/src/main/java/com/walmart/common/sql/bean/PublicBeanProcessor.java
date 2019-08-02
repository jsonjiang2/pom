package com.walmart.common.sql.bean;

import org.apache.commons.dbutils.BeanProcessor;

import java.beans.PropertyDescriptor;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Arrays;

public class PublicBeanProcessor extends BeanProcessor {

    @Override
    protected int[] mapColumnsToProperties(ResultSetMetaData rsmd, PropertyDescriptor[] props) throws SQLException {
        int cols = rsmd.getColumnCount();
        int[] columnToProperty = new int[cols + 1];
        Arrays.fill(columnToProperty, -1);

        for(int col = 1; col <= cols; ++col) {
            String columnName = rsmd.getColumnLabel(col);
            if (null == columnName || 0 == columnName.length()) {
                columnName = rsmd.getColumnName(col);
            }

            for(int i = 0; i < props.length; ++i) {
                if (equalsIgnoreInABC(columnName,props[i].getName())) {
                    columnToProperty[col] = i;
                    break;
                }
            }
        }

        return columnToProperty;
    }

    /**比较两个字符串英文字母是否相等（比较时忽略空字符、忽略大小写、非英文字符）*/
    public static boolean equalsIgnoreInABC(String str1, String str2){
        if(str1==null && str2==null){
            return true;
        }
        if (str1==null){
            return false;
        }
        if (str2==null){
            return false;
        }
        String temp1=str1.replaceAll("[^(A-Za-z)]", "").toLowerCase();
        String temp2=str2.replaceAll("[^(A-Za-z)]", "").toLowerCase();
        return temp1.equals(temp2);
    }
}
