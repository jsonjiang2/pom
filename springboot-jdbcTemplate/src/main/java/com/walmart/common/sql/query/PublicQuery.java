package com.walmart.common.sql.query;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.sql.DataSource;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.ColumnListHandler;
import org.apache.commons.dbutils.handlers.MapListHandler;
import org.apache.commons.dbutils.handlers.ScalarHandler;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.walmart.common.config.ConfigResolve;
import com.walmart.common.config.DefaultDataBaseKey;
import com.walmart.common.sql.bean.OneSql;
import com.walmart.common.sql.bean.PublicBeanHandler;
import com.walmart.common.sql.bean.PublicBeanListHandler;
import com.walmart.common.sql.connection.MysqlConnection;
import com.walmart.common.sql.dbconfig.DBConfigEnum;
import com.walmart.common.util.annotation.CustomTableFieldName;
import com.walmart.common.util.annotation.CustomTableName;
import com.walmart.common.util.date.DateUtils;
import com.walmart.common.util.reflect.ReflectClassUtil;
import com.walmart.model.UserInfo;


public class PublicQuery {
	
	public static Logger logger = LoggerFactory.getLogger(PublicQuery.class);

    protected static QueryRunner getQueryRunnerByDbKey(String dataBaseKey){
        if(dataBaseKey == null){
            dataBaseKey = ConfigResolve.getDbJsonObjectConfig().getString(DefaultDataBaseKey.DEFAULTDATABASEKEY);
        }
        DataSource dataSource = MysqlConnection.getDataSource(dataBaseKey);
        if(dataSource == null){
            return null;
        }
        return new QueryRunner(dataSource);
    }

    protected static Connection getConnectionByDbKey(String dataBaseKey){
        if(dataBaseKey == null){
            dataBaseKey = ConfigResolve.getDbJsonObjectConfig().getString(DefaultDataBaseKey.DEFAULTDATABASEKEY);
        }
        return MysqlConnection.getConnection(dataBaseKey);
    }
    /**
     * 插入一条记录
     * @param sql
     * @param params
     * @return
     */
    public static long insert(String sql, Object[] params) {
        return insert(null, sql, params);
    }

    /**
     * 插入一条记录
     obj     * @param databaseKey
     * @return
     */
    public static long insert(String databaseKey, Object obj) {
        OneSql oneSql = getInsertSql(obj);
        Long l = insert(databaseKey, oneSql.getSql(), new ScalarHandler<Long>(), oneSql.getParams());
        if(l == null) {
            return -1l;
        }
        return l.longValue();
    }
    /**
     * 插入一条记录
     * @param sql
     * @param params
     * @return
     */
    public static long insert(String databaseKey, String sql, Object[] params) {
        Long l = insert(databaseKey, sql, new ScalarHandler<Long>(), params);
        if(l == null) {
            return -1l;
        }
        return l.longValue();
    }

    public static <T> T insert(OneSql oneSql, ResultSetHandler<T> rsh) {
        QueryRunner runner = getQueryRunnerByDbKey(oneSql.getDatabase());
        if(runner == null) return null;
        try {
            return runner.insert(oneSql.getSql(), rsh, oneSql.getParams());
        } catch (SQLException e) {
            e.printStackTrace();
        }finally {
            runner = null;
        }
        return null;
    }

    public static <T> T insert(String databaseKey, String sql, ResultSetHandler<T> rsh, Object[] params) {
        logger.debug("插入语句： insert sql=" + sql + "，params=" + Arrays.toString(params));
        QueryRunner runner = getQueryRunnerByDbKey(databaseKey);
        if(runner == null) return null;
        try {
            return runner.insert(sql, rsh, params);
        } catch (SQLException e) {
            e.printStackTrace();
        }finally {
            runner = null;
        }
        return null;
    }
    /**
     * 获取插入语句
     * @param <T>
     * @param
     * @param isInherit  是否包含继承类的字段  如果包含需要设置为public
     * @return
     */
    private static <T> OneSql getInsertSql(T b, String databaseKey, boolean isInherit){
        Class<?> cls = b.getClass();
        StringBuilder sqlSbl = new StringBuilder();
        String tabName = cls.getSimpleName();
        CustomTableName tableName = cls.getAnnotation(CustomTableName.class);
        if(tableName != null && StringUtils.isNotEmpty(tableName.value())){
            tabName = tableName.value();
        }
        sqlSbl.append("insert into ")
                .append(tabName)
                .append(" (");

        Field[] fields = cls.getDeclaredFields();
        if(isInherit){
            fields = ReflectClassUtil.getAllFields(b);
        }

        List<Object> lists = new ArrayList<Object>();
        int i = 0;
        for(Field f : fields){
            try {
                String fname = f.getName();
                Object o = null;
                o = ReflectClassUtil.getValByField(cls, fname, b);
                if(o != null){
                    if(i > 0){
                        sqlSbl.append(",");
                    }
                    CustomTableFieldName tableFieldName = f.getAnnotation(CustomTableFieldName.class);
                    if(tableFieldName != null && StringUtils.isNotEmpty(tableFieldName.value())){
                        //设置了数据库表字段
                        fname = tableFieldName.value();
                    }
                    sqlSbl.append(fname);
                    lists.add(o);
                    i++;
                }
            } catch (Exception e) {
            }
        }
        if(i > 0){
            sqlSbl.append(") values (");
            Object[] obs = new Object[i];
            for(int j = 0 ; j < i; j++){
                obs[j] = lists.get(j);
                if(j > 0){
                    sqlSbl.append(",");
                }
                sqlSbl.append("?");
            }
            sqlSbl.append(")");
            return new OneSql(sqlSbl.toString() , -1 , obs , databaseKey);
        }
        throw new NullPointerException(tabName + "插入的sql语句为空");
    }

    public static <T> Integer updateById(String databaseKey,T t){
        try{
            Class<?> cls = t.getClass();
            String tabName = cls.getSimpleName();
            CustomTableName tableName = cls.getAnnotation(CustomTableName.class);
            if(tableName != null && StringUtils.isNotEmpty(tableName.value())){
                tabName = tableName.value();
            }
            Field[] fields = cls.getDeclaredFields();
            StringBuilder sb = new StringBuilder();
            Long id = 0L;
            for(Field f : fields){
                try {
                    String fname = f.getName();
                    Object o = null;
                    o = ReflectClassUtil.getValByField(cls, fname, t);
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
                String sql = " UPDATE  "+tabName+" set "+str+"  WHERE id = ? ";
                int c = update(databaseKey,sql,new Object[]{id});
                if(c > 0){
                    return c;
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return -1;
    }

    /**
     * 获取插入语句
     * @param <T>
     * @param
     * @return
     */
    public static <T> OneSql getInsertSql(T b){
        return getInsertSql(b, null);
    }
    /**
     * 获取插入语句
     * @param <T>
     * @param
     * @return
     */
    public static <T> OneSql getInsertSqlByIsInherit(T b, String databaseKey,boolean isInherit){
        return getInsertSql(b, databaseKey, isInherit);
    }

    /**
     * 获取插入语句
     * @param <T>
     * @param
     * @return
     */
    public static <T> OneSql getInsertSql(T b, String databaseKey){
        return getInsertSql(b, databaseKey, false);
    }

    public static OneSql getBatchInsertSQL(List<?> lists, Class cls, String dataBaseKey) throws Exception {
        if(lists == null || lists.size() == 0) return null;
        try {
            StringBuilder insertSQL = new StringBuilder();
            StringBuilder valueSQL = new StringBuilder();
            List<Object> params = new ArrayList<Object>();
            String tabName = cls.getSimpleName();
            CustomTableName zhulinTableName = (CustomTableName) cls.getAnnotation(CustomTableName.class);
            if(zhulinTableName != null && StringUtils.isNotEmpty(zhulinTableName.value())){
                tabName = zhulinTableName.value();
            }

            insertSQL.append("INSERT INTO ").append(tabName).append(" (");
            Field[] fields = cls.getDeclaredFields();
            int i = 0;
            for(Field f : fields){
                try {

                    String fname = f.getName();
                    Object o = null;
                    o = ReflectClassUtil.getValByField(cls, fname, lists.get(0));
                    if(o != null){
                        if(i > 0){
                            insertSQL.append(",");
                        }
                        CustomTableFieldName tableFieldName = f.getAnnotation(CustomTableFieldName.class);
                        if(tableFieldName != null && StringUtils.isNotEmpty(tableFieldName.value())){
                            //设置了数据库表字段
                            fname = tableFieldName.value();
                        }
                        insertSQL.append(fname);
                        i++;
                    }
                } catch (Exception e) {
                }
            }
            int x = 0;
            for (Object list : lists) {
                i = 0;
                valueSQL.append("(");
                for(Field f : fields){
                    try {
                        String fname = f.getName();
                        Object o = null;
                        o = ReflectClassUtil.getValByField(cls, fname, list);
                        if(o != null){
                            if(i > 0){
                                valueSQL.append(",");
                            }
                            valueSQL.append("?");
                            params.add(o);
                            i++;
                        }
                    } catch (Exception e) {
                    }
                }
                valueSQL.append(")");
                if(x < lists.size() - 1) {
                    valueSQL.append(",");
                }
                x++;
            }
            insertSQL.append(") values ");
            insertSQL.append(valueSQL);
            return new OneSql(insertSQL.toString(), lists.size(), params.toArray(), dataBaseKey);
        } catch (SecurityException e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * 查询记录数量
     * @param sql
     * @param params
     * @return
     */
    public static long count(String databaseKey, String sql, Object[] params) {
        QueryRunner runner = getQueryRunnerByDbKey(databaseKey);
        if(runner == null) return -1;
        try {
            String countSql = null;
            if(sql.toLowerCase().contains("group by")){
                countSql = "select count(*) from (" + sql + ") t";
            }else {
                countSql = "select count(*) " + sql.substring(sql.toLowerCase().indexOf("from"));
            }
            return runner.query(countSql, new ScalarHandler<Long>(), params);
        } catch (SQLException e) {
            e.printStackTrace();
        }finally {
            runner = null;
        }
        return -1;
    }

    /**
     * 更新操作，返回影响行数
     * @param sql
     * @param params
     * @return
     */
    public static int update(String sql, Object[] params) {
        return update(null, sql, params);
    }
    /**
     * 删除操作，返回影响行数
     * @param sql
     * @param params
     * @return -1表示处理出错
     */
    public static int delete(String databaseKey, String sql, Object[] params) {
        logger.debug("data delete sql=" + sql + "，params=" + Arrays.toString(params));
         QueryRunner runner = getQueryRunnerByDbKey(databaseKey);
        if(runner == null) return -1;
        try {
            return runner.update(sql, params);
        } catch (SQLException e) {
            e.printStackTrace();
        }finally {
            runner = null;
        }
        return -1;
    }
    /**
     * 更新操作，返回影响行数
     * @param sql
     * @param params
     * @return -1表示处理出错
     */
    public static int update(String databaseKey, String sql, Object[] params) {
        logger.debug("data update sql=" + sql + "，params=" + Arrays.toString(params));
        //修改不允许delete操作
        if(isDeleteCommard(sql)) {
            return -1;
        }
        QueryRunner runner = getQueryRunnerByDbKey(databaseKey);
        if(runner == null) return -1;
        try {
            return runner.update(sql, params);
        } catch (SQLException e) {
            e.printStackTrace();
        }finally {
            runner = null;
        }
        return -1;
    }

    protected static boolean isDeleteCommard(String sql) {
        String copySQL = sql;
        copySQL = copySQL.trim().toLowerCase();
        if(copySQL.startsWith("delete")) {
            return true;
        }
        return false;
    }

    /**
     * 批量处理
     * @param sqls 里面每一个Sql结构必须保持一致 且数据源要一致
     * @return
     */
    public static boolean updateBatch(String databasekey, List<OneSql> sqls, int efRows) {
        if(sqls == null || sqls.size() == 0) return true;
        Connection connection=getConnectionByDbKey(databasekey);
        // DbUtils
        QueryRunner runner = new QueryRunner();
        PreparedStatement stmt = null;
        try {
            for (int i = 0; i < sqls.size(); i++) {
                OneSql sql = sqls.get(i);
                if (i == 0) {
                    stmt = connection.prepareStatement(sql.getSql());
                }
                runner.fillStatement(stmt, sql.getParams());
                stmt.addBatch();
            }
            // 执行，返回结果为每个sql影响的行情
            int[] callbacks = stmt.executeBatch();
            long rows = 0;
            for (int i : callbacks) {
                rows = rows + i;
            }
            if (rows == efRows || efRows == -2) {
                return true;
            }else {
                logger.error(" 批量处理SQL影响行数不一: 期望影响：" + efRows + "行，实际影响：" + rows + "行");
            }
        } catch (SQLException e) {
            logger.error("批量处理SQL出现异常【{}】", JSON.toJSONString(sqls));
            logger.error( " 批量处理SQL出现异常导致数据回滚: " + e.getMessage());
        } finally {
            try {
                DbUtils.close(stmt);
                stmt = null;
                if (connection!=null){
                    connection.close();
                }
            } catch (SQLException e) {
                logger.error("批量处理SQL释放Statement异常", e);
            }
        }
        return false;
    }

    public static <T> List<T> query2List(String databaseKey, String sql, Class<T> clazz, Object[] params) {
        logger.debug("data query sql=" + sql + "，params=" + Arrays.toString(params));
        QueryRunner runner = getQueryRunnerByDbKey(databaseKey);
        if(runner == null) return null;
        try {
            List<T> list =  runner.query(sql, new PublicBeanListHandler<T>(clazz), params);
            if(list == null || list.size() == 0) {
                list = new ArrayList<T>();
            }
            return list;
        } catch (SQLException e) {
            e.printStackTrace();
        }finally {
            runner = null;
        }
        return new ArrayList<T>();
    }

    public static int execute(String databaseKey, String sql, Object[] params) {
        logger.debug("data execute sql=" + sql + "，params=" + Arrays.toString(params));
        //程序不允许delete操作
        if(isDeleteCommard(sql)) {
            return -1;
        }
        QueryRunner runner = getQueryRunnerByDbKey(databaseKey);
        if(runner == null) return -1;
        try {
            return runner.execute(sql, params);
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }finally {
            runner = null;
        }
        return -1;
    }

    public static <T> List<T> queryColumn(String databaseKey, String sql, Class<T> clazz, Object[] params,String columnName) {
        logger.debug("data query sql=" + sql + "，params=" + Arrays.toString(params));
        QueryRunner runner = getQueryRunnerByDbKey(databaseKey);
        if(runner == null) return null;
        try {
            List<T> list =  runner.query(sql, new ColumnListHandler<T>(columnName), params);
            if(list == null) {
                list = new ArrayList<T>();
            }
            return list;
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }finally {
            runner = null;
        }
        return null;
    }

    public static <T> T queryObject(String databaseKey, String sql, Class<T> clazz, Object[] params) {
        logger.debug("data query sql=" + sql + "，params=" + Arrays.toString(params));
        QueryRunner runner = getQueryRunnerByDbKey(databaseKey);
        if(runner == null) return null;
        try {
            T t =  runner.query(sql, new PublicBeanHandler<T>(clazz), params);
            return t;
        } catch (SQLException e) {
            e.printStackTrace();
        }finally {
            runner = null;
        }
        return null;
    }

    public static List<Map<String,Object>> query2Map(String databaseKey, String sql, Object[] params){
        logger.debug("data query sql=" + sql + "，params=" + Arrays.toString(params));
        List<Map<String,Object>> result=new ArrayList<Map<String,Object>>();
        QueryRunner runner = getQueryRunnerByDbKey(databaseKey);
        if(runner == null) return null;
        try {
            result=runner.query(sql,new MapListHandler(),params);
        } catch (SQLException e) {
            e.printStackTrace();
        }finally {
            runner = null;
        }
        return result;
    }

    /**
     * 获取 in 条件参数
     * @param paramters
     * @return
     */
    public static String convertInParamters(List paramters){
        StringBuffer stringBuffer=new StringBuffer();
        if (CollectionUtils.isNotEmpty(paramters)){
            for (Object param:paramters){
                if (stringBuffer.length()==0){
                    stringBuffer.append(param);
                }else {
                    stringBuffer.append(",").append(param);
                }
            }
        }
        return stringBuffer.toString();
    }

    /**
     * 获取 in 条件参数
     * @param paramters
     * @return
     */
    public static String convertInParamters(Set paramters){
        StringBuffer stringBuffer=new StringBuffer();
        if (CollectionUtils.isNotEmpty(paramters)){
            for (Object param:paramters){
                if (stringBuffer.length()==0){
                    stringBuffer.append(param);
                }else {
                    stringBuffer.append(",").append(param);
                }
            }
        }
        return stringBuffer.toString();
    }

    public static void main(String[] args) {
        UserInfo user = new UserInfo();
        user.setUsername("123");
        Long cc = PublicQuery.insert(DBConfigEnum.YSDB.getDbname(),user);
        System.out.println(cc);

        String updatSql = "update user set avatar = '的是范德萨发的说法的是' where id = ?";
        int cc2 = PublicQuery.update(DBConfigEnum.YSDB.getDbname(),updatSql,new Object[]{10});
        System.out.println(cc2);

        String sel = "select * from `user` where id = ?";
        List<Map<String,Object>> seuser  = PublicQuery.query2Map(DBConfigEnum.YSDB.getDbname(),sel,new Object[]{10});
        System.out.println(JSON.toJSONString(seuser));


    }

}
