package com.walmart.common.sql.bean;

import org.apache.commons.dbutils.BasicRowProcessor;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.RowProcessor;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class PublicBeanListHandler<T> implements ResultSetHandler<List<T>> {
    private final Class<? extends T> type;
    private final RowProcessor convert;

    public PublicBeanListHandler(Class<? extends T> type) {
        this(type,new BasicRowProcessor(new PublicBeanProcessor()));
    }

    public PublicBeanListHandler(Class<? extends T> type, RowProcessor convert) {
        this.type = type;
        this.convert = convert;
    }

    public List<T> handle(ResultSet resultSet) throws SQLException {
        return this.convert.toBeanList(resultSet,this.type);
    }
}
