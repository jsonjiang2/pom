package com.walmart.service;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.walmart.common.util.annotation.CustomTableName;


public class BaseService<Entity> {
	protected  static Logger logger;
	 /**
     * 数据库实体类型
     */
    protected final Class<Entity> entityClass;
    /**
     * 数据库表
     */
    protected final String entityTableName;

    public BaseService() {
        logger = LoggerFactory.getLogger(this.getClass());
        Type parentType = this.getClass().getGenericSuperclass();
        // 转成参数类型接口
        ParameterizedType paramterType = (ParameterizedType) parentType;
        // 得到泛型类型
        Type[] types = paramterType.getActualTypeArguments();
        // 得到传入泛型的类
        entityClass = (Class<Entity>) types[0];
        CustomTableName customTableName = entityClass.getAnnotation(CustomTableName.class);
        if(customTableName != null && StringUtils.isNotEmpty(customTableName.value())){
            entityTableName = customTableName.value();
        }else{
            entityTableName = entityClass.getSimpleName().toLowerCase();
        }
    }

}
