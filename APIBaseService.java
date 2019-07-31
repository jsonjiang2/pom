package com.zhulin.api.service;

import com.alibaba.fastjson.JSON;
import com.zhulin.common.mysql.dbconfig.DBConfigEnum;
import com.zhulin.common.mysql.query.ZhulinQuery;
import com.zhulin.common.util.annotation.ZhulinTableFieldName;
import com.zhulin.common.util.annotation.ZhulinTableName;
import com.zhulin.common.util.date.DateUtils;
import com.zhulin.common.util.enumeration.AppStatus;
import com.zhulin.common.util.process.ProcessBack;
import com.zhulin.common.util.reflect.ReflectClassUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Date;

public class APIBaseService<Entity> {
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

    public APIBaseService() {
        logger = LoggerFactory.getLogger(this.getClass());
        Type parentType = this.getClass().getGenericSuperclass();
        // 转成参数类型接口
        ParameterizedType paramterType = (ParameterizedType) parentType;
        // 得到泛型类型
        Type[] types = paramterType.getActualTypeArguments();
        // 得到传入泛型的类
        entityClass = (Class<Entity>) types[0];
        ZhulinTableName zhulinTableName = entityClass.getAnnotation(ZhulinTableName.class);
        if(zhulinTableName != null && StringUtils.isNotEmpty(zhulinTableName.value())){
            entityTableName = zhulinTableName.value();
        }else{
            entityTableName = entityClass.getSimpleName().toLowerCase();
        }
    }

    public Entity getEntityById(Long id){
        if(id == null || id <= 0){
            logger.error("根据ID查询数据异常,id:{}",id);
            return null;
        }
        String sql = "SELECT * FROM "+entityTableName+" WHERE id = ?";
        Entity entity = ZhulinQuery.queryObject(DBConfigEnum.YSDB.getDbname(),sql,entityClass,new Object[]{id});
        return entity;
    }
    public int deleteEntityById(Long id){
        try{
            if(id == null || id <= 0){
                logger.error("根据ID物理删除数据异常,id:{}",id);
            }
            String sql = "DELETE FROM "+entityTableName+" WHERE id = ?";
            int c = ZhulinQuery.delete(DBConfigEnum.YSDB.getDbname(),sql,new Object[]{id});
            return c;
        }catch (Exception e){
            e.printStackTrace();
        }
        return -1;
    }
    public int logicDeleteEntityById(Long id){
        try{
            if(id == null || id <= 0){
                logger.error("根据ID物理删除数据异常,id:{}",id);
            }
            String sql = "UPDATE  "+entityTableName+" set isdelete = 1  WHERE id = ?";
            int c = ZhulinQuery.delete(DBConfigEnum.YSDB.getDbname(),sql,new Object[]{id});
            return c;
        }catch (Exception e){
            e.printStackTrace();
        }
        return -1;
    }
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
            Integer cc = ZhulinQuery.updateById(DBConfigEnum.YSDB.getDbname(),entity);
            if(cc > 0){
                return new ProcessBack(AppStatus.Success);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return new ProcessBack(AppStatus.Fail);
    }

    public ProcessBack preInsert(Entity entity){
        return new ProcessBack(AppStatus.Success);
    }

    public ProcessBack insert(Entity entity){
        try{
            ProcessBack back = preInsert(entity);
            if(back.getCode().equals(AppStatus.Fail.getCode())){
                logger.error("新增前验证未通过,back:{}",JSON.toJSONString(back));
                return back;
            }
            Long id = ZhulinQuery.insert(DBConfigEnum.YSDB.getDbname(),entity);
            if(id > 0){
                return new ProcessBack(AppStatus.Success,id);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return new ProcessBack(AppStatus.Fail);
    }
}
