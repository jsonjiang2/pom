package com.walmart.common.sql.service;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Date;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.walmart.common.sql.dbconfig.DBConfigEnum;
import com.walmart.common.sql.query.PublicQuery;
import com.walmart.common.util.annotation.CustomTableFieldName;
import com.walmart.common.util.annotation.CustomTableName;
import com.walmart.common.util.date.DateUtils;
import com.walmart.common.util.enumeration.AppStatus;
import com.walmart.common.util.process.ProcessBack;
import com.walmart.common.util.reflect.ReflectClassUtil;

public class ZhulinBaseService<Entity>{
    protected  static Logger logger;
    @SuppressWarnings("unchecked")
    /**
     * 数据库实体类型
     */
    protected final Class<Entity> entityClass;
    /**
     * 数据库表
     */
    protected final String entityTableName;

    public ZhulinBaseService() {
        logger = LoggerFactory.getLogger(this.getClass());
        Type parentType = this.getClass().getGenericSuperclass();
        // 转成参数类型接口
        ParameterizedType paramterType = (ParameterizedType) parentType;
        // 得到泛型类型
        Type[] types = paramterType.getActualTypeArguments();
        // 得到传入泛型的类
        entityClass = (Class<Entity>) types[0];
        CustomTableName zhulinTableName = entityClass.getAnnotation(CustomTableName.class);
        if(zhulinTableName != null && StringUtils.isNotEmpty(zhulinTableName.value())){
            entityTableName = zhulinTableName.value();
        }else{
            entityTableName = entityClass.getSimpleName().toLowerCase();
        }
    }

    //更新前校验
    public ProcessBack preUpdateById(Entity entity){
        return new ProcessBack(AppStatus.Success);
    }

    public ProcessBack updateById(Entity entity){
        try{
            ProcessBack back =  preUpdateById(entity);
            if(back.getCode().equals(AppStatus.Fail.getCode())){
                logger.error("更新前验证未通过,back:{}", JSON.toJSONString(back));
                return back;
            }
            Field[] fields = entityClass.getDeclaredFields();
            StringBuilder sb = new StringBuilder();
            Long id = 0L;
            for(Field f : fields){
                try {
                    String fname = f.getName();
                    Object o = null;
                    o = ReflectClassUtil.getValByField(entityClass, fname, entity);
                    if(o != null){
                        if(fname.equals("id")){
                            id = (Long)o;
                            continue;
                        }
                        CustomTableFieldName tableFieldName = f.getAnnotation(CustomTableFieldName.class);
                        if(tableFieldName != null && StringUtils.isNotEmpty(tableFieldName.value())){
                            //设置了数据库表字段
                            fname = tableFieldName.value();
                        }
                        String fType = f.getGenericType().toString();
                        if(fType.equals("class java.lang.String")){
                            sb.append(", "+fname+" = '"+o+"'");
                        }else if(fType.equals("class java.util.Date")){
                            Date date = (Date)o;
                            String dateStr =  DateUtils.formatDate(date);
                            sb.append(", "+fname+" = '"+dateStr+"'");
                        }else {
                            sb.append(", "+fname+" = "+o+"");
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if(sb.length() > 0){
                String str = sb.toString().substring(1);
                String sql = " UPDATE  "+entityTableName+" set "+str+"  WHERE id = ? ";
                int c = PublicQuery.update(DBConfigEnum.YSDB.getDbname(),sql,new Object[]{id});
                if(c > 0){
                    return new ProcessBack(AppStatus.Success);
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return new ProcessBack(AppStatus.Fail);
    }


 
}
