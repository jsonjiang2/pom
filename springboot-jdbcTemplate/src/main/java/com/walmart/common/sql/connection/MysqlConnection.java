package com.walmart.common.sql.connection;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONObject;
import com.walmart.common.config.ConfigResolve;
import com.walmart.common.config.DefaultDataBaseKey;
import com.walmart.common.sql.dbconfig.DatabaseConfig;
import com.zaxxer.hikari.HikariDataSource;

/**
 *  db数据库链接
 *  author 小松鼠
 *  contact phpfzh@sina.com
 */
public class MysqlConnection {

    private static Logger logger = LoggerFactory.getLogger(MysqlConnection.class);

    private static Map<String,DatabaseConfig> databaseConfigs ;

    private static Map<String, DataSource> dataSources = new HashMap<String, DataSource>();

    static {
        initDatabaseConfigs();
    }

    //初始化数据库配置
    public static  synchronized Map<String,DatabaseConfig> initDatabaseConfigs(){
            if(databaseConfigs == null){
                databaseConfigs = new HashMap<String, DatabaseConfig>();
                try{
                	//读取数据库配置文件信息
                    JSONObject dbConfig = ConfigResolve.getDbJsonObjectConfig();
                    if(dbConfig == null || !dbConfig.containsKey(DefaultDataBaseKey.DATABASEKEYS)){
                        return databaseConfigs;
                    }
                    JSONObject databases = dbConfig.getJSONObject(DefaultDataBaseKey.DATABASEKEYS);
                    if(databases != null && !databases.isEmpty()){
                        for (String database:databases.keySet()){
                            JSONObject obj = databases.getJSONObject(database);
                            String host = obj.getString("dbhost");
                            String dbName = obj.getString("dbName");
                            String dbUser = obj.getString("dbUser");
                            String dbpsw = obj.getString("dbpsw");
                            // 开启mysql的executeBath功能
                            boolean rewriteBatchedStatements = obj.containsKey("rewriteBatchedStatements") ? obj.getBooleanValue("rewriteBatchedStatements") : false;
                            DatabaseConfig databaseConfig = new DatabaseConfig();
                            databaseConfig.setDatabaseKey(database);
                            databaseConfig.setDbName(dbName);
                            databaseConfig.setHost(host);
                            databaseConfig.setDbUser(dbUser);
                            databaseConfig.setDbPwd(dbpsw);
                            databaseConfig.setRewriteBatchedStatements(rewriteBatchedStatements);
                            databaseConfigs.put(database,databaseConfig);
                            logger.info("数据库配置初始化成功，{}",database);
                        }
                    }
                }catch (Exception e){
                    e.printStackTrace();
                    logger.error("数据库配置初始化异常,{}",e);
                }
            }
            return databaseConfigs;
    }
    //根据数据库配置key 获取数据源
    public static DatabaseConfig getDatabaseConfig(String databaseKey){
        if(databaseConfigs == null){
            databaseConfigs = initDatabaseConfigs();
        }
        if(databaseConfigs == null){
            return null;
        }
        return databaseConfigs.get(databaseKey);
    }

    public static synchronized DataSource getDataSource(String databaseKey){
        DatabaseConfig databaseConfig = getDatabaseConfig(databaseKey);
        if(databaseConfig == null){
            return null;
        }
        try{
            DataSource dataSource = dataSources.get(databaseConfig);
            if(dataSource == null){
                dataSource = getHikariDataSource(databaseConfig);
                databaseConfig.setOk(true);
            }
            if(dataSource == null){
                throw new RuntimeException("数据源建立异常" + "," + databaseKey + ",host:" + databaseConfig.getHost() + ",dbName:"
                        + databaseConfig.getDbName() + ",dbUser:" + databaseConfig.getDbUser());
            }
            return dataSource;
        }catch (Exception e){
            e.printStackTrace();
            throw new RuntimeException("数据源建立异常" + "," + databaseKey + ",host:" + databaseConfig.getHost() + ",dbName:"
                    + databaseConfig.getDbName() + ",dbUser:" + databaseConfig.getDbUser());
        }
    }

    public static Connection getConnection(String databaseKey){
        DatabaseConfig databaseConfig = getDatabaseConfig(databaseKey);
        if(databaseConfig == null){
            return null;
        }
        try{
            DataSource dataSource = getDataSource(databaseKey);
            Connection connection = dataSource.getConnection();
            return connection;
        }catch(Exception e){
            logger.error("连接MySQL失败，databaseKey: " + databaseKey);
            logger.error(databaseKey + ",连接出现异常！" + e.getMessage() + "," + databaseKey + ",host:" + databaseConfig.getHost() + ",dbName:"
                    + databaseConfig.getDbName() + ",dbUser:" + databaseConfig.getDbUser());
            throw new RuntimeException("无法与数据库建立连接");
        }
    }

    public static Connection getConnection(){
        String defaultDataBaseKey = ConfigResolve.getDbJsonObjectConfig().getString(DefaultDataBaseKey.DEFAULTDATABASEKEY);
        return getConnection(defaultDataBaseKey);
    }

    private static synchronized HikariDataSource getHikariDataSource(DatabaseConfig databaseConfig){
        HikariDataSource source = (HikariDataSource) dataSources.get(databaseConfig.getDatabaseKey());
        if(source !=  null){
            return source;
        }
        try{
            source = new HikariDataSource();
            source.setDriverClassName("com.mysql.jdbc.Driver");
            String jdbcUrl = "";
            if(databaseConfig.getRewriteBatchedStatements()){
                jdbcUrl = "jdbc:mysql://"+databaseConfig.getHost()+"/"+databaseConfig.getDbName()+"?useUnicode=true&rewriteBatchedStatements=true&characterEncoding=UTF-8";
            }else{
                jdbcUrl = "jdbc:mysql://"+databaseConfig.getHost()+"/"+databaseConfig.getDbName()+"?useUnicode=true&characterEncoding=UTF-8";
            }
            source.setJdbcUrl(jdbcUrl);
            source.setUsername(databaseConfig.getDbUser());
            source.setPassword(databaseConfig.getDbPwd());
            // 等待连接池分配连接的最大时长（毫秒），超过这个时长还没可用的连接则发生SQLException， 缺省:30秒
            source.setConnectionTimeout(30000);
            // 一个连接idle状态的最大时长（毫秒），超时则被释放（retired），缺省:10分钟
            source.setIdleTimeout(600000);
            // 一个连接的生命时长（毫秒），超时而且没被使用则被释放（retired），缺省:30分钟，建议设置比数据库超时时长少30秒，参考MySQL
            // wait_timeout参数
            source.setMaxLifetime(30 * 60 * 1000);
            // 连接池中允许的最大连接数。缺省值：10；推荐的公式：((core_count * 2) + effective_spindle_count)
            source.setMaximumPoolSize(30);
            // 池中最小空闲链接数量
            source.setMinimumIdle(5);
            // 连接只读数据库时配置为true
            source.setReadOnly(false);
            source.setValidationTimeout(3000);
            source.setLoginTimeout(5);
            source.addDataSourceProperty("cachePrepStmts", true);
            source.addDataSourceProperty("prepStmtCacheSize", 500);
            source.addDataSourceProperty("prepStmtCacheSqlLimit", 2048);
            source.setAutoCommit(true);
            source.setConnectionTestQuery("show tables");
            dataSources.put(databaseConfig.getDatabaseKey(), source);
        }catch (Exception e){
            e.printStackTrace();
            logger.error("数据池加载异常,DatabaseKey:{},dbName:{},{}",
                    databaseConfig.getDatabaseKey(),databaseConfig.getDbName(),e);
        }
        return source;
    }
    //注销mysql连接
    public synchronized static void close() {
        if (dataSources == null || dataSources.isEmpty()) {
            return;
        }
        logger.info("注销mysql连接 start");
        Collection<DataSource> c = dataSources.values();
        Iterator<DataSource> it = c.iterator();
        for (; it.hasNext(); ) {
            try {
                DataSource dbs = it.next();
                if (dbs != null) {
                    HikariDataSource bdbs = (HikariDataSource) dbs;
                    if (!bdbs.isClosed()) {
                        bdbs.close();
                        logger.info("关闭mysql连接：" + dbs.getConnection().toString());
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
