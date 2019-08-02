package com.walmart.common.sql.bean;

import org.apache.commons.dbutils.BasicRowProcessor;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.RowProcessor;

import java.sql.ResultSet;
import java.sql.SQLException;

public class PublicBeanHandler<T> implements ResultSetHandler<T> {
    private final Class<? extends T> type;
    private final RowProcessor convert;

    public PublicBeanHandler(Class<? extends T> type) {
        this(type,new BasicRowProcessor(new PublicBeanProcessor()));
    }

    public PublicBeanHandler(Class<? extends T> type, RowProcessor convert) {
        this.type = type;
        this.convert = convert;
    }

    public T handle(ResultSet resultSet) throws SQLException {
        return resultSet.next() ? this.convert.toBean(resultSet,this.type):null;
    }
}
